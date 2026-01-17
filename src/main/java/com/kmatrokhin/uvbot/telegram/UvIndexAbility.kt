package com.kmatrokhin.uvbot.telegram

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.I18nProperties
import com.kmatrokhin.uvbot.dto.LocationInfo
import com.kmatrokhin.uvbot.dto.UserSignUp
import com.kmatrokhin.uvbot.entities.UserLanguage
import com.kmatrokhin.uvbot.repositories.LocationRepository
import com.kmatrokhin.uvbot.repositories.UserRepository
import com.kmatrokhin.uvbot.services.LocationInfoService
import com.kmatrokhin.uvbot.services.RecommendationService
import com.kmatrokhin.uvbot.services.UserService
import io.sentry.Sentry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.*
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient

const val UVI_REQUEST_TEXT = "Get UVI"
const val SETTINGS_TEXT: String = "Settings and help"

@Service
class UvIndexAbility(
    telegramClient: TelegramClient,
    private val userService: UserService,
    private val locationInfoService: LocationInfoService,
    private val userRepository: UserRepository,
    private val recommendationService: RecommendationService,
    private val locationRepository: LocationRepository,
    private val i18nProperties: I18nProperties
) : AbilityBot(
    telegramClient, "uv_advisor_bot",
    MapDBContext.onlineInstance("uv_bot.db")
),
    SpringLongPollingBot {

    @Value("\${telegram.token}")
    private lateinit var telegramToken: String

    @EventListener
    fun onStart(event: ContextRefreshedEvent) {
        runCatching { onRegister() }
            .onFailure { Sentry.captureException(it) }
    }

    fun start(): Ability {
        return Ability.builder()
            .name("start")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { context -> sendInitMessage(context.update(), UserLanguage.ENGLISH) }
            .build()
    }

    fun sendInitMessage(update: Update, language: UserLanguage) {
        runCatching {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(startKeyboard())
                    .text(i18nProperties.get(language, "init_message"))
                    .chatId(AbilityUtils.getChatId(update))
                    .build()
            )
        }.onFailure { Sentry.captureException(it) }
    }

    fun mainKeyboard(): ReplyKeyboardMarkup {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(
                KeyboardRow(
                    uvIndexButton(),
                    locationButton(),
                    manageSubscription()
                )
            )
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
            .build()
    }

    private fun getLanguage(chatId: Long): UserLanguage {
        return userRepository.findByChatId(chatId)?.language ?: UserLanguage.ENGLISH
    }

    private fun manageSubscription(): KeyboardButton {
        return KeyboardButton.builder()
            .text("⚙️ $SETTINGS_TEXT")
            .build()
    }

    private fun startKeyboard(): ReplyKeyboardMarkup {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(
                KeyboardRow(
                    locationButton()
                )
            )
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
            .build()
    }

    fun showMainMenu(update: Update, language: UserLanguage) {
        runCatching {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(mainKeyboard())
                    .text(i18nProperties.get(language, "main_menu"))
                    .chatId(AbilityUtils.getChatId(update))
                    .build()
            )
        }.onFailure { Sentry.captureException(it) }
    }

    fun locationButton(): KeyboardButton {
        return KeyboardButton.builder()
            .requestLocation(true)
            .text("📍 Send location")
            .build()
    }

    fun uvIndexButton(): KeyboardButton {
        return KeyboardButton.builder()
            .text("☀️ $UVI_REQUEST_TEXT")
            .build()
    }

    fun sendUvIndexWhenLocationIsSent(): ReplyFlow {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action { _: BaseAbilityBot, update: Update ->
                runCatching { sendUviMessage(update) }.onFailure {
                    Sentry.captureException(
                        it
                    )
                }
            }
            .build()
    }

    fun sendUvIndexWhenItsRequested(): ReplyFlow {
        return ReplyFlow.builder(db)
            .onlyIf { update: Update ->
                Flag.TEXT.test(update) && (update.message?.text?.contains(UVI_REQUEST_TEXT) == true)
            }
            .action { _: BaseAbilityBot, update: Update ->
                runCatching { sendUviMessage(update) }.onFailure {
                    Sentry.captureException(
                        it
                    )
                }
            }
            .build()
    }

    private fun sendUviMessage(update: Update) {
        val message = update.message ?: return
        val chatId = AbilityUtils.getChatId(update)

        val locationInfo: LocationInfo = when {
            message.hasLocation() -> {
                val location = message.location
                val coordinates = Coordinates(location.latitude, location.longitude)

                val language = getLanguage(chatId)
                locationInfoService.getLocationInfo(coordinates, language)
            }

            AbilityUtils.isUserMessage(update) -> {
                val userEntity = userRepository.findByChatId(chatId)
                    ?: run {
                        sendInitMessage(update, UserLanguage.ENGLISH)
                        return
                    }

                val locationEntity = locationRepository.findByUserEntity(userEntity)
                    ?: run {
                        sendInitMessage(update, userEntity.language)
                        return
                    }

                locationInfoService.getLocationInfo(
                    coordinates = locationEntity.coordinates(),
                    language = userEntity.language,
                    locationName = locationEntity.name
                )
            }

            else -> return
        }

        val user = message.from
        val userName: String? = user.userName?.let { "@$it" }
            ?: listOfNotNull(user.firstName, user.lastName).joinToString(" ").ifBlank { null }

        val userSignUp = UserSignUp(chatId = chatId, name = userName, locationInfo = locationInfo)
        val userEntity = userService.signUpOrUpdate(userSignUp)

        silent.execute(
            SendChatAction.builder()
                .chatId(chatId)
                .action(ActionType.TYPING.toString())
                .build()
        )
        silent.execute(
            SendMessage.builder()
                .replyMarkup(mainKeyboard())
                .text(recommendationService.createRecommendationText(locationInfo, userEntity.language))
                .parseMode("html")
                .chatId(chatId)
                .build()
        )
    }

    override fun creatorId(): Long {
        return System.getenv("CREATOR_ID").toLong()
    }

    override fun getBotToken(): String {
        return telegramToken
    }

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer {
        return this
    }

    public override fun addExtension(abilityExtension: AbilityExtension) {
        super.addExtension(abilityExtension)
    }
}

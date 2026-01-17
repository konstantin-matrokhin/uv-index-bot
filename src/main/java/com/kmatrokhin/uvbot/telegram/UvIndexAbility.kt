package com.kmatrokhin.uvbot.telegram

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.I18nProperties
import com.kmatrokhin.uvbot.dto.LocationInfo
import com.kmatrokhin.uvbot.dto.UserSignUp
import com.kmatrokhin.uvbot.entities.UserEntity
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
import org.telegram.telegrambots.abilitybots.api.db.DBContext
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext
import org.telegram.telegrambots.abilitybots.api.objects.*
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender
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
import java.util.*

@Service
class UvIndexAbility(
    telegramClient: TelegramClient,
    private val userService: UserService,
    private val locationInfoService: LocationInfoService,
    private val userRepository: UserRepository,
    private val recommendationService: RecommendationService,
    private val locationRepository: LocationRepository,
    private val i18nProperties: I18nProperties
) : AbilityBot(telegramClient, "uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db")), SpringLongPollingBot {
    @Value("\${telegram.token}")
    private lateinit var telegramToken: String

    @EventListener
    fun onStart(event: ContextRefreshedEvent) {
        runCatching { onRegister() }
            .onFailure { e -> Sentry.captureException(e) }
    }

    fun start(): Ability {
        return Ability.builder()
            .name("start")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { context: MessageContext ->
                sendInitMessage(context!!.update(), UserLanguage.ENGLISH)
            }
            .build()
    }

    fun sendInitMessage(update: Update, language: UserLanguage) {
        try {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(startKeyboard())
                    .text(i18nProperties.get(language, "init_message"))
                    .chatId(AbilityUtils.getChatId(update))
                    .build()
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
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
        return Optional.ofNullable<UserEntity>(userRepository.findByChatId(chatId))
            .map(UserEntity::language).orElse(UserLanguage.ENGLISH)
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
        try {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(mainKeyboard())
                    .text(i18nProperties.get(language, "main_menu"))
                    .chatId(AbilityUtils.getChatId(update))
                    .build()
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
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
            .action { _: BaseAbilityBot, update: Update -> sendUviMessage(update) }.build()
    }

    fun sendUvIndexWhenItsRequested(): ReplyFlow {
        return ReplyFlow.builder(db)
            .onlyIf { update: Update ->
                Flag.TEXT.test(update) && (update.message.text.contains(
                    UVI_REQUEST_TEXT
                ))
            }
            .action { _: BaseAbilityBot, update: Update -> sendUviMessage(update) }
            .build()
    }

    private fun sendUviMessage(update: Update) {
        try {
            if (update.message == null) {
                return
            }
            val chatId = AbilityUtils.getChatId(update)
            val locationInfo: LocationInfo
            if (update.message.hasLocation()) {
                val location = update.message.location
                val coordinates = Coordinates(location.latitude, location.longitude)
                locationInfo = locationInfoService.getLocationInfo(coordinates, null)
            } else if (AbilityUtils.isUserMessage(update)) {
                val userEntityOpt = userRepository.findByChatId(chatId)
                if (userEntityOpt != null) {
                    val locationEntity = locationRepository.findByUserEntity(userEntityOpt)
                    val coordinates = locationEntity!!.coordinates()
                    locationInfo = locationInfoService.getLocationInfo(coordinates, locationEntity.name)
                } else {
                    sendInitMessage(update, UserLanguage.ENGLISH)
                    return
                }
            } else {
                return
            }

            val userName = update.message.from.userName
            val userSignUp = UserSignUp(
                chatId, if (userName != null) "@$userName" else update.message
                    .from
                    .firstName + " " + update.message.from.lastName, locationInfo
            )
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
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
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

    override fun getSilent(): SilentSender {
        return super.getSilent()
    }

    val dB: DBContext
        get() = super.db

    public override fun addExtension(abilityExtension: AbilityExtension) {
        super.addExtension(abilityExtension)
    }

    companion object {
        private const val UVI_REQUEST_TEXT = "Get UVI"
        const val SETTINGS_TEXT: String = "Settings and help"
    }
}

package com.kmatrokhin.uvbot.telegram

import com.kmatrokhin.uvbot.dto.I18nProperties
import com.kmatrokhin.uvbot.entities.UserEntity
import com.kmatrokhin.uvbot.entities.UserLanguage
import com.kmatrokhin.uvbot.repositories.UserRepository
import com.kmatrokhin.uvbot.services.UserService
import io.sentry.Sentry
import jakarta.annotation.PostConstruct
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot
import org.telegram.telegrambots.abilitybots.api.objects.Flag
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import java.util.*

@Service
@RequiredArgsConstructor
class SettingsAbility(
    private val uvIndexAbility: UvIndexAbility,
    private val userService: UserService,
    private val i18nProperties: I18nProperties,
    private val userRepository: UserRepository
) : AbilityExtension {

    @PostConstruct
    fun init() {
        uvIndexAbility.addExtension(this)
    }

    fun unsubscribeFlow(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update) && update.callbackQuery.data
                    .equals("unsubscribe", ignoreCase = true)
            }
            .action { _: BaseAbilityBot, update: Update ->
                unsubscribe(
                    update,
                    getLanguage(AbilityUtils.getChatId(update))
                )
            }.build()
    }

    fun subscribeFlow(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update) && update.callbackQuery.data
                    .equals("subscribe", ignoreCase = true)
            }
            .action { _: BaseAbilityBot, update: Update ->
                subscribe(
                    update,
                    getLanguage(AbilityUtils.getChatId(update))
                )
            }.build()
    }

    fun unsubscribe(update: Update, language: UserLanguage) {
        try {
            val chatId = AbilityUtils.getChatId(update)
            userService.setSubscription(chatId, false)
            uvIndexAbility.getSilent().send(i18nProperties.get(language, "unsubscribe_reply"), chatId)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    fun subscribe(update: Update, language: UserLanguage) {
        try {
            val chatId = AbilityUtils.getChatId(update)
            userService.setSubscription(chatId, true)
            uvIndexAbility.getSilent().send(i18nProperties.get(language, "subscribe_reply"), chatId)
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    fun sendSettingsAndHelp(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.TEXT.test(update) && (update.message.text
                    .contains(UvIndexAbility.SETTINGS_TEXT))
            }
            .action { bot: BaseAbilityBot, update: Update ->
                try {
                    val chatId = AbilityUtils.getChatId(update)
                    val language = getLanguage(chatId)
                    uvIndexAbility.getSilent().execute(
                        SendMessage.builder()
                            .chatId(chatId)
                            .text(i18nProperties.get(language, "settings_menu_title"))
                            .replyMarkup(helpInlineKeyboard(update, language))
                            .build()
                    )
                } catch (e: Exception) {
                    Sentry.captureException(e)
                }
            }
            .build()
    }

    private fun getLanguage(chatId: Long): UserLanguage {
        return Optional.ofNullable<UserEntity>(userRepository.findByChatId(chatId))
            .map(UserEntity::language).orElse(UserLanguage.ENGLISH)
    }

    private fun helpInlineKeyboard(update: Update, language: UserLanguage): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.builder().keyboard(
            listOf(
                InlineKeyboardRow(
                    if (userService.isSubscribed(AbilityUtils.getChatId(update))) unsubscribeButton(language) else subscribeButton(
                        language
                    )
                ),
                InlineKeyboardRow(cannotSendLocationButton(language)),
                InlineKeyboardRow(lang())
            )
        ).build()
    }

    private fun cannotSendLocationButton(language: UserLanguage): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .text(i18nProperties.get(language, "cannot_send_location_button"))
            .callbackData("cannot_send_location")
            .build()
    }

    private fun ourTgChannelButton(): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .text("Out telegram channel")
            .callbackData("our_tg_channel")
            .build()
    }

    private fun lang(): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .text("Language | Язык")
            .callbackData("lang_menu")
            .build()
    }

    fun cannotSendLocation(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update) && update.callbackQuery.data
                    .equals("cannot_send_location", ignoreCase = true)
            }
            .action { _: BaseAbilityBot, update: Update ->
                uvIndexAbility.getSilent().send(
                    i18nProperties.get(getLanguage(AbilityUtils.getChatId(update)), "cannot_send_location"),
                    AbilityUtils.getChatId(update)
                )
            }.build()
    }

    fun langMenuCallback(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update)
                        && update.callbackQuery.data.equals("lang_menu", ignoreCase = true)
            }.action { _: BaseAbilityBot, update: Update ->
                uvIndexAbility.getSilent().execute(
                    SendMessage.builder()
                        .replyMarkup(langMenu())
                        .text("Choose a language\nВыберите язык")
                        .chatId(AbilityUtils.getChatId(update))
                        .build()
                )
            }
            .build()
    }

    fun langSelectEnCallback(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update) && update.callbackQuery.data
                    .equals("lang_en", ignoreCase = true)
            }
            .action { bot: BaseAbilityBot, update: Update ->
                userService.setLanguage(AbilityUtils.getChatId(update), UserLanguage.ENGLISH)
                uvIndexAbility.getSilent().execute(
                    SendMessage.builder()
                        .text("Done!")
                        .chatId(AbilityUtils.getChatId(update))
                        .build()
                )
            }
            .build()
    }

    fun langSelectRuCallback(): ReplyFlow {
        return ReplyFlow.builder(uvIndexAbility.db)
            .onlyIf { update: Update ->
                Flag.CALLBACK_QUERY.test(update) && update.callbackQuery.data
                    .equals("lang_ru", ignoreCase = true)
            }
            .action { _: BaseAbilityBot, update: Update ->
                userService.setLanguage(AbilityUtils.getChatId(update), UserLanguage.RUSSIAN)
                uvIndexAbility.getSilent().execute(
                    SendMessage.builder()
                        .text("Готово!")
                        .chatId(AbilityUtils.getChatId(update))
                        .build()
                )
            }
            .build()
    }

    fun langMenu(): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.builder()
            .keyboard(
                listOf(
                    InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                            .text("🇬🇧 English")
                            .callbackData("lang_en")
                            .build()
                    ), InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                            .text("🇷🇺 Russian")
                            .callbackData("lang_ru")
                            .build()
                    )
                )
            )
            .build()
    }


    fun subscribeButton(language: UserLanguage): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .callbackData("subscribe")
            .text(i18nProperties.get(language, "subscribe_button"))
            .build()
    }

    fun unsubscribeButton(language: UserLanguage): InlineKeyboardButton {
        return InlineKeyboardButton.builder()
            .callbackData("unsubscribe")
            .text(i18nProperties.get(language, "unsubscribe_button"))
            .build()
    }
}

package com.kmatrokhin.uvbot.telegram;

import com.kmatrokhin.uvbot.dto.I18nProperties;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.entities.UserLanguage;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

import static com.kmatrokhin.uvbot.telegram.UvIndexAbility.SETTINGS_TEXT;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Service
@RequiredArgsConstructor
//@DependsOn("userService")
public class SettingsAbility implements AbilityExtension {
    private final UvIndexAbility uvIndexAbility;
    private final UserService userService;
    private final I18nProperties i18nProperties;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        uvIndexAbility.addExtension(this);
    }

    public ReplyFlow unsubscribeFlow() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("unsubscribe"))
            .action((bot, update) -> unsubscribe(update, getLanguage(getChatId(update)))).build();
    }

    public ReplyFlow subscribeFlow() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("subscribe"))
            .action((bot, update) -> subscribe(update, getLanguage(getChatId(update)))).build();
    }

    public void unsubscribe(Update update, UserLanguage language) {
        Long chatId = getChatId(update);
        userService.setSubscription(chatId, false);
        uvIndexAbility.getSilent().send(i18nProperties.get(language, "unsubscribe_reply"), chatId);
    }

    public void subscribe(Update update, UserLanguage language) {
        Long chatId = getChatId(update);
        userService.setSubscription(chatId, true);
        uvIndexAbility.getSilent().send(i18nProperties.get(language, "subscribe_reply"), chatId);
    }

    public ReplyFlow sendSettingsAndHelp() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.TEXT.test(update) && (update.getMessage().getText().contains(SETTINGS_TEXT)))
            .action((bot, update) -> {
                Long chatId = getChatId(update);
                UserLanguage language = getLanguage(chatId);
                uvIndexAbility.getSilent().execute(
                    SendMessage.builder()
                        .chatId(chatId)
                        .text(i18nProperties.get(language, "settings_menu_title"))
                        .replyMarkup(helpInlineKeyboard(update, language))
                        .build()
                );
            })
            .build();
    }

    private UserLanguage getLanguage(Long chatId) {
        return userRepository.findByChatId(chatId).map(UserEntity::getLanguage).orElse(UserLanguage.ENGLISH);
    }

    private InlineKeyboardMarkup helpInlineKeyboard(Update update, UserLanguage language) {
        return InlineKeyboardMarkup.builder().keyboard(List.of(
            new InlineKeyboardRow(
                userService.isSubscribed(getChatId(update)) ? unsubscribeButton(language) : subscribeButton(language)
            ),
            new InlineKeyboardRow(cannotSendLocationButton(language)),
            new InlineKeyboardRow(lang())
        )).build();
    }

    private InlineKeyboardButton cannotSendLocationButton(UserLanguage language) {
        return InlineKeyboardButton.builder()
            .text(i18nProperties.get(language, "cannot_send_location_button"))
            .callbackData("cannot_send_location")
            .build();
    }

    private InlineKeyboardButton ourTgChannelButton() {
        return InlineKeyboardButton.builder()
            .text("Out telegram channel")
            .callbackData("our_tg_channel")
            .build();
    }

    private InlineKeyboardButton lang() {
        return InlineKeyboardButton.builder()
            .text("Language | Язык")
            .callbackData("lang_menu")  
            .build();
    }

    public ReplyFlow cannotSendLocation() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("cannot_send_location"))
            .action((bot, update) -> uvIndexAbility.getSilent().send(i18nProperties.get(getLanguage(getChatId(update)), "cannot_send_location"), getChatId(update))).build();
    }

    public ReplyFlow ourTgChannel() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("our_tg_channel"))
            .action((bot, update) -> uvIndexAbility.getSilent().send("""
                Our telegram channel: @muskrat_dev
                """, getChatId(update))).build();
    }

    public ReplyFlow langMenuCallback() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update)
                && update.getCallbackQuery().getData().equalsIgnoreCase("lang_menu")
            ).action((bot, update) -> uvIndexAbility.getSilent().execute(SendMessage.builder()
                .replyMarkup(langMenu())
                .text("Choose a language\nВыберите язык")
                .chatId(getChatId(update))
                .build()))
            .build();
    }

    public ReplyFlow langSelectEnCallback() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("lang_en"))
            .action((bot, update) -> {
                userService.setLanguage(getChatId(update), UserLanguage.ENGLISH);
                uvIndexAbility.getSilent().execute(SendMessage.builder()
                    .text("Done!")
                    .chatId(getChatId(update))
                    .build());
            })
            .build();
    }

    public ReplyFlow langSelectRuCallback() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("lang_ru"))
            .action((bot, update) -> {
                userService.setLanguage(getChatId(update), UserLanguage.RUSSIAN);
                uvIndexAbility.getSilent().execute(SendMessage.builder()
                    .text("Готово!")
                    .chatId(getChatId(update))
                    .build());
            })
            .build();
    }

    public InlineKeyboardMarkup langMenu() {
        return InlineKeyboardMarkup.builder()
            .keyboard(List.of(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text("🇬🇧 English")
                    .callbackData("lang_en")
                    .build()
            ), new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text("🇷🇺 Russian")
                    .callbackData("lang_ru")
                    .build()
            )))
            .build();
    }


    public InlineKeyboardButton subscribeButton(UserLanguage language) {
        return InlineKeyboardButton.builder()
            .callbackData("subscribe")
            .text(i18nProperties.get(language, "subscribe_button"))
            .build();
    }

    public InlineKeyboardButton unsubscribeButton(UserLanguage language) {
        return InlineKeyboardButton.builder()
            .callbackData("unsubscribe")
            .text(i18nProperties.get(language, "unsubscribe_button"))
            .build();
    }

}

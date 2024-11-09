package com.kmatrokhin.uvbot.telegram;

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
public class SettingsAbility implements AbilityExtension {
    private final UvIndexAbility uvIndexAbility;
    private final UserService userService;

    @PostConstruct
    public void init() {
        uvIndexAbility.addExtension(this);
    }

    public ReplyFlow unsubscribeFlow() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("unsubscribe"))
            .action((bot, update) -> unsubscribe(update)).build();
    }

    public ReplyFlow subscribeFlow() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("subscribe"))
            .action((bot, update) -> subscribe(update)).build();
    }

    public void unsubscribe(Update update) {
        Long chatId = getChatId(update);
        userService.setSubscription(chatId, false);
        uvIndexAbility.getSilent().send("You have unsubscribed from notifications 😪 Come back soon to always stay protected from the sun", chatId);
    }

    public void subscribe(Update update) {
        Long chatId = getChatId(update);
        userService.setSubscription(chatId, true);
        uvIndexAbility.getSilent().send("Hooray! We will now send notifications when the UV index changes!", chatId);
    }

    public ReplyFlow sendSettingsAndHelp() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.TEXT.test(update) && (update.getMessage().getText().contains(SETTINGS_TEXT)))
            .action((bot, update) -> {
                uvIndexAbility.getSilent().execute(SendMessage.builder()
                    .chatId(getChatId(update))
                    .text("What do you want to do?")
                    .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(helpInlineKeyboard(update))
                        .build()
                    )
                    .build());
            })
            .build();
    }

    private List<InlineKeyboardRow> helpInlineKeyboard(Update update) {
        return List.of(
            new InlineKeyboardRow(
                userService.isSubscribed(getChatId(update)) ? unsubscribeButton() : subscribeButton()
            ),
            new InlineKeyboardRow(cannotSendLocationButton())
//            new InlineKeyboardRow(ourTgChannelButton())
        );
    }

    private InlineKeyboardButton cannotSendLocationButton() {
        return InlineKeyboardButton.builder()
            .text("Cannot send geolocation")
            .callbackData("cannot_send_location")
            .build();
    }

    private InlineKeyboardButton ourTgChannelButton() {
        return InlineKeyboardButton.builder()
            .text("Out telegram channel")
            .callbackData("our_tg_channel")
            .build();
    }

    public ReplyFlow cannotSendLocation() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("cannot_send_location"))
            .action((bot, update) -> uvIndexAbility.getSilent().send("""
                – In Telegram for Windows, it is not possible to send a location.
                – To send a location in macOS, manually send it using the button with a paperclip icon next to the message input field. The button in the bot might not work.
                
                Use a mobile device to send a location.""", getChatId(update))).build();
    }

    public ReplyFlow ourTgChannel() {
        return ReplyFlow.builder(uvIndexAbility.getDB())
            .onlyIf(update -> Flag.CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equalsIgnoreCase("our_tg_channel"))
            .action((bot, update) -> uvIndexAbility.getSilent().send("""
                Our telegram channel: @muskrat_dev
                """, getChatId(update))).build();
    }


    public InlineKeyboardButton subscribeButton() {
        return InlineKeyboardButton.builder()
            .callbackData("subscribe")
            .text("Subscribe")
            .build();
    }

    public InlineKeyboardButton unsubscribeButton() {
        return InlineKeyboardButton.builder()
            .callbackData("unsubscribe")
            .text("Unsubscribe")
            .build();
    }

}

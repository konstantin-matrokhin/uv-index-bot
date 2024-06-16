package com.kmatrokhin.uvbot.telegram;

import com.kmatrokhin.uvbot.services.UserService;
import com.kmatrokhin.uvbot.services.UvIndexService;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.objects.*;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Service
@Slf4j
public class UvIndexAbility extends AbilityBot implements SpringLongPollingBot {
    private final UvIndexService uvIndexService;
    private final UserService userService;

    public UvIndexAbility(TelegramClient telegramClient, UvIndexService uvIndexService, UserService userService) {
        super(telegramClient, "uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db"));
        this.uvIndexService = uvIndexService;
        this.userService = userService;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        super.onRegister();
    }

    public Ability ability() {
        return Ability.builder()
            .name("start")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action(messageContext -> showMenu(messageContext.chatId()))
            .build();
    }

    public void showMenu(Long chatId) {
        silent.execute(SendMessage.builder()
            .replyMarkup(ReplyKeyboardMarkup.builder()
                .keyboard(List.of(
                    new KeyboardRow(
                        locationButton()
                    ))
                )
                .oneTimeKeyboard(false)
                .resizeKeyboard(true)
                .build()
            )
            .text("Main menu")
            .chatId(chatId)
            .build());
    }

    public KeyboardButton locationButton() {
        return KeyboardButton.builder()
            .requestLocation(true)
            .text("Send new location")
            .build();
    }

    public InlineKeyboardButton subscribeButton() {
        return InlineKeyboardButton.builder()
            .callbackData("subscribe")
            .text("Subscribe")
            .build();
    }

    public ReplyFlow subscribeReply() {
        return ReplyFlow.builder(db)
            .onlyIf(upd -> upd.getCallbackQuery().getData().equalsIgnoreCase("subscribe"))
            .action((bot, update) -> {
                silent.send("Subscribed!:)", getChatId(update));
            })
            .build();
    }

    public ReplyFlow sendUvIndexWhenLocationIsSent() {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action((bot, update) -> {
                Long chatId = getChatId(update);
                Location location = update.getMessage().getLocation();
                userService.signUp(chatId, location.getLatitude(), location.getLongitude());
                String uvIndex = uvIndexService.getUvIndex(location.getLatitude(), location.getLongitude());
                String text = "UV Index at " + location.getLatitude() + ";" + location.getLongitude() + " is " + uvIndex;
                silent.execute(SendMessage.builder()
                    .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(List.of(
                            new InlineKeyboardRow(subscribeButton())
                        ))
                        .build()
                    )
                    .text(text)
                    .chatId(getChatId(update))
                    .build());
            })
            .build();
    }

    @Override
    public long creatorId() {
        return Long.parseLong(System.getenv("CREATOR_ID"));
    }

    @Override
    public String getBotToken() {
        return System.getenv("TELEGRAM_TOKEN");
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}

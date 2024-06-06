package com.kmatrokhin.uvbot;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Service
@Slf4j
public class UvIndexAbility extends AbilityBot implements SpringLongPollingBot {
    private final UvIndexService uvIndexService;

    public UvIndexAbility(TelegramClient telegramClient, UvIndexService uvIndexService) {
        super(telegramClient, "@uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db"));
        this.uvIndexService = uvIndexService;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        super.onRegister();
    }

    public ReplyFlow sendUvIndexWhenLocationIsSent() {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action((bot, update) -> {
                Location location = update.getMessage().getLocation();
                String uvIndex = uvIndexService.getUvIndex(location.getLatitude(), location.getLongitude());
                String text = "UV Index at " + location.getLatitude() + ";" + location.getLongitude() + " is " + uvIndex;
                silent.execute(SendMessage.builder()
                    .replyMarkup(ReplyKeyboardMarkup.builder()
                        .keyboardRow(
                            new KeyboardRow(KeyboardButton.builder()
                                .requestLocation(true)
                                .text("Send new location")
                                .build())
                        )
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

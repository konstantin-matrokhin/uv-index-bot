package com.kmatrokhin.uvbot.telegram;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.services.LocationInfoService;
import com.kmatrokhin.uvbot.services.UserService;
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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;

@Service
@Slf4j
public class UvIndexAbility extends AbilityBot implements SpringLongPollingBot {
    private final UserService userService;
    private final LocationInfoService locationInfoService;
    private final UserRepository userRepository;

    public UvIndexAbility(TelegramClient telegramClient, UserService userService, LocationInfoService locationInfoService, UserRepository userRepository) {
        super(telegramClient, "uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db"));
        this.userService = userService;
        this.locationInfoService = locationInfoService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        super.onRegister();
    }

    public Ability start() {
        return Ability.builder()
            .name("start")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action(context -> {
                if (userRepository.findByChatId(context.chatId()).isEmpty()) {
                    sendInitMessage(context.update());
                } else {
                    showMainMenu(context.update());
                }
            })
            .build();
    }

    public void sendInitMessage(Update update) {
        silent.execute(
            SendMessage.builder()
                .replyMarkup(ReplyKeyboardMarkup.builder()
                    .keyboardRow(new KeyboardRow(
                        locationButton()
                    ))
                    .oneTimeKeyboard(false)
                    .resizeKeyboard(true)
                    .build()
                )
                .text("Please send your location")
                .chatId(getChatId(update))
                .build()
        );
    }

    public void showMainMenu(Update update) {
        silent.execute(
            SendMessage.builder()
                .replyMarkup(ReplyKeyboardMarkup.builder()
                    .keyboardRow(new KeyboardRow(
                        locationButton(),
                        manageSubscription()
                    ))
                    .oneTimeKeyboard(false)
                    .resizeKeyboard(true)
                    .build()
                )
                .text("Welcome back!")
                .chatId(getChatId(update))
                .build()
        );
    }

    private KeyboardButton manageSubscription() {
        return KeyboardButton.builder()
            .text("⚙️ Manage my subscription")
            .build();
    }

    public KeyboardButton locationButton() {
        return KeyboardButton.builder()
            .requestLocation(true)
            .text("📍 Send new location")
            .build();
    }

    public InlineKeyboardButton subscribeButton() {
        return InlineKeyboardButton.builder()
            .callbackData("subscribe")
            .text("Subscribe")
            .build();
    }

    public ReplyFlow sendUvIndexWhenLocationIsSent() {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action((bot, update) -> {
                Long chatId = getChatId(update);
                Location location = update.getMessage().getLocation();
                Coordinates coordinates = Coordinates.of(location.getLatitude(), location.getLongitude());
                LocationInfo locationInfo = locationInfoService.getLocationInfo(coordinates);
                userService.signUpOrUpdate(update.getMessage().getFrom().getUserName(), chatId, locationInfo);
                silent.send("UV index in " + locationInfo.getName() + " now is " + locationInfo.getUvIndex(), chatId);
                boolean isSubscribed = userRepository.findByChatId(chatId).isPresent();
                if (isSubscribed) {
                    return;
                }

                silent.execute(SendMessage.builder()
                    .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                .callbackData("subscribe")
                                .text("Subscribe")
                                .build()
                        ))
                        .build())
                    .text("Do you want to subscribe to daily UV index?")
                    .chatId(chatId)
                    .build()
                );
            }).build();
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

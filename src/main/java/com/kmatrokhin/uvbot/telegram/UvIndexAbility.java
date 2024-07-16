package com.kmatrokhin.uvbot.telegram;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.services.LocationInfoService;
import com.kmatrokhin.uvbot.services.RecommendationService;
import com.kmatrokhin.uvbot.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.objects.*;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.isUserMessage;

@Service
@Slf4j
public class UvIndexAbility extends AbilityBot implements SpringLongPollingBot {
    private static final String UVI_REQUEST_TEXT = "UVI at my location";

    private final UserService userService;
    private final LocationInfoService locationInfoService;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    private final LocationRepository locationRepository;

    @Value("${telegram.token}")
    private String telegramToken;

    public UvIndexAbility(TelegramClient telegramClient, UserService userService, LocationInfoService locationInfoService, UserRepository userRepository, RecommendationService recommendationService, LocationRepository locationRepository) {
        super(telegramClient, "uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db"));
        this.userService = userService;
        this.locationInfoService = locationInfoService;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.locationRepository = locationRepository;
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
                .replyMarkup(startKeyboard())
                .text("Please send your location")
                .chatId(getChatId(update))
                .build()
        );
    }

    private ReplyKeyboardMarkup mainKeyboard() {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(
                uvIndexButton(),
                locationButton(),
                manageSubscription()
            ))
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
            .build();
    }

    private ReplyKeyboardMarkup startKeyboard() {
        return ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(
                locationButton()
            ))
            .oneTimeKeyboard(false)
            .resizeKeyboard(true)
            .build();
    }

    public void showMainMenu(Update update) {
        silent.execute(
            SendMessage.builder()
                .replyMarkup(mainKeyboard())
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

    public KeyboardButton uvIndexButton() {
        return KeyboardButton.builder()
            .text("☀️ " + UVI_REQUEST_TEXT)
            .build();
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

    public ReplyFlow sendUvIndexWhenLocationIsSent() {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action((bot, update) -> sendUviMessage(update)).build();
    }

    public ReplyFlow sendUvIndexWhenItsRequested() {
        return ReplyFlow.builder(db)
            .onlyIf(update -> Flag.TEXT.test(update) && update.getMessage().getText().contains(UVI_REQUEST_TEXT))
            .action((bot, update) -> sendUviMessage(update))
            .build();
    }

    private void sendUviMessage(Update update) {
        if (update.getMessage() == null) {
            return;
        }

        Long chatId = getChatId(update);
        Coordinates coordinates;
        if (update.getMessage().hasLocation()) {
            Location location = update.getMessage().getLocation();
            coordinates = Coordinates.of(location.getLatitude(), location.getLongitude());
        } else if (isUserMessage(update)) {
            Optional<UserEntity> userEntityOpt = userRepository.findByChatId(chatId);
            if (userEntityOpt.isPresent()) {
                LocationEntity locationEntity = locationRepository.getByUserEntity(userEntityOpt.get());
                coordinates = locationEntity.coordinates();
            } else {
                sendInitMessage(update);
                return;
            }
        } else {
            return;
        }
        LocationInfo locationInfo = locationInfoService.getLocationInfo(coordinates);
        userService.signUpOrUpdate(update.getMessage().getFrom().getUserName(), chatId, locationInfo);

        silent.execute(
            SendChatAction.builder()
                .chatId(chatId)
                .action(ActionType.TYPING.toString())
                .build()
        );
        silent.execute(SendMessage.builder()
            .replyMarkup(mainKeyboard())
            .text(recommendationService.createRecommendationText(locationInfo))
            .parseMode("html")
            .chatId(chatId)
            .build()
        );
    }

    @Override
    public long creatorId() {
        return Long.parseLong(System.getenv("CREATOR_ID"));
    }

    @Override
    public String getBotToken() {
        return telegramToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}

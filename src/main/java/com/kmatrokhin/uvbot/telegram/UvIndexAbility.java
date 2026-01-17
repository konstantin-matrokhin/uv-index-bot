package com.kmatrokhin.uvbot.telegram;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.I18nProperties;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.UserSignUp;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.entities.UserLanguage;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.services.LocationInfoService;
import com.kmatrokhin.uvbot.services.RecommendationService;
import com.kmatrokhin.uvbot.services.UserService;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.ReplyFlow;
import org.telegram.telegrambots.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.getChatId;
import static org.telegram.telegrambots.abilitybots.api.util.AbilityUtils.isUserMessage;

@Service
@Slf4j
public class UvIndexAbility extends AbilityBot implements SpringLongPollingBot {
    private static final String UVI_REQUEST_TEXT = "Get UVI";
    public static final String SETTINGS_TEXT = "Settings and help";

    private final UserService userService;
    private final LocationInfoService locationInfoService;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;
    private final LocationRepository locationRepository;
    private final I18nProperties i18nProperties;

    @Value("${telegram.token}")
    private String telegramToken;

    public UvIndexAbility(
        TelegramClient telegramClient, UserService userService,
        LocationInfoService locationInfoService, UserRepository userRepository,
        RecommendationService recommendationService, LocationRepository locationRepository,
        I18nProperties i18nProperties) {
        super(telegramClient, "uv_advisor_bot", MapDBContext.onlineInstance("uv_bot.db"));
        this.userService = userService;
        this.locationInfoService = locationInfoService;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
        this.locationRepository = locationRepository;
        this.i18nProperties = i18nProperties;
    }

    @EventListener
    public void onStart(ContextRefreshedEvent event) {
        try {
            super.onRegister();
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    public Ability start() {
        return Ability.builder()
            .name("start")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action(context -> {
                sendInitMessage(context.update(), UserLanguage.ENGLISH);
            })
            .build();
    }

    public void sendInitMessage(Update update, UserLanguage language) {
        try {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(startKeyboard())
                    .text(i18nProperties.get(language, "init_message"))
                    .chatId(getChatId(update))
                    .build()
            );
        }  catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    public ReplyKeyboardMarkup mainKeyboard() {
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

    private UserLanguage getLanguage(Long chatId) {
        return Optional.ofNullable(userRepository.findByChatId(chatId)).map(UserEntity::getLanguage).orElse(UserLanguage.ENGLISH);
    }

    private KeyboardButton manageSubscription() {
        return KeyboardButton.builder()
            .text("⚙️ " + SETTINGS_TEXT)
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

    public void showMainMenu(Update update, UserLanguage language) {
        try {
            silent.execute(
                SendMessage.builder()
                    .replyMarkup(mainKeyboard())
                    .text(i18nProperties.get(language, "main_menu"))
                    .chatId(getChatId(update))
                    .build()
            );
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    public KeyboardButton locationButton() {
        return KeyboardButton.builder()
            .requestLocation(true)
            .text("📍 Send location")
            .build();
    }

    public KeyboardButton uvIndexButton() {
        return KeyboardButton.builder()
            .text("☀️ " + UVI_REQUEST_TEXT)
            .build();
    }

    public ReplyFlow sendUvIndexWhenLocationIsSent() {
        return ReplyFlow.builder(db)
            .onlyIf(Flag.LOCATION)
            .action((bot, update) -> sendUviMessage(update)).build();
    }

    public ReplyFlow sendUvIndexWhenItsRequested() {
        return ReplyFlow.builder(db)
            .onlyIf(update -> Flag.TEXT.test(update) && (update.getMessage().getText().contains(UVI_REQUEST_TEXT)))
            .action((bot, update) -> sendUviMessage(update))
            .build();
    }

    private void sendUviMessage(Update update) {
        try {
            if (update.getMessage() == null) {
                return;
            }
            Long chatId = getChatId(update);
            LocationInfo locationInfo;
            if (update.getMessage().hasLocation()) {
                Location location = update.getMessage().getLocation();
                Coordinates coordinates = Coordinates.of(location.getLatitude(), location.getLongitude());
                locationInfo = locationInfoService.getLocationInfo(coordinates);
            } else if (isUserMessage(update)) {
                Optional<UserEntity> userEntityOpt = Optional.ofNullable(userRepository.findByChatId(chatId));
                if (userEntityOpt.isPresent()) {
                    LocationEntity locationEntity = locationRepository.findByUserEntity(userEntityOpt.get());
                    Coordinates coordinates = locationEntity.coordinates();
                    locationInfo = locationInfoService.getLocationInfo(coordinates, locationEntity.getName());
                } else {
                    sendInitMessage(update, UserLanguage.ENGLISH);
                    return;
                }
            } else {
                return;
            }

            String userName = update.getMessage().getFrom().getUserName();
            UserSignUp userSignUp = new UserSignUp(chatId, userName != null ? "@" + userName : update.getMessage()
                .getFrom()
                .getFirstName() + " " + update.getMessage().getFrom().getLastName(), locationInfo);
            UserEntity userEntity = userService.signUpOrUpdate(userSignUp);

            silent.execute(
                SendChatAction.builder()
                    .chatId(chatId)
                    .action(ActionType.TYPING.toString())
                    .build()
            );
            silent.execute(SendMessage.builder()
                .replyMarkup(mainKeyboard())
                .text(recommendationService.createRecommendationText(locationInfo, userEntity.getLanguage()))
                .parseMode("html")
                .chatId(chatId)
                .build()
            );
        } catch (Exception e) {
            Sentry.captureException(e);
        }
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

    @Override
    public SilentSender getSilent() {
        return super.getSilent();
    }

    public DBContext getDB() {
        return super.db;
    }

    public void addExtension(AbilityExtension abilityExtension) {
        super.addExtension(abilityExtension);
    }
}

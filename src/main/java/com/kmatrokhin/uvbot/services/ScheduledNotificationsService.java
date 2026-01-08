package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.events.UserBlockedBotEvent;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.telegram.UvIndexAbility;
import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationsService {
    private final TelegramClient telegramClient;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationInfoService locationInfoService;
    private final RecommendationService recommendationService;
    private final UvIndexAbility uvIndexAbility;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Scheduled(cron = "@hourly")
    public void scheduledNotificationsForUsers() {
        List<LocationEntity> allLocations = locationRepository.findAll();
        log.info("Scheduled updating UV info scheduled for {} locations", allLocations.size());
        for (LocationEntity loc : allLocations) {
            try {
                UserEntity userEntity = loc.getUserEntity();
                if (!userEntity.getIsSubscribed()) {
                    continue;
                }
                Long chatId = userEntity.getChatId();
                LocationInfo locationInfo = locationInfoService.getLocationInfo(loc.coordinates(), loc.getName());
                float lastUvIndex = loc.getLastUvIndex();
                float newIndex = locationInfo.getWeather().getUvi();
                if (Math.abs(lastUvIndex - newIndex) >= 0.9) {
                    loc.setLastUvIndex(newIndex);
                    try {
                        telegramClient.execute(SendMessage.builder()
                            .replyMarkup(uvIndexAbility.mainKeyboard())
                            .text(recommendationService.createRecommendationText(locationInfo, userEntity.getLanguage()))
                            .parseMode("html")
                            .chatId(chatId)
                            .build()
                        );
                    } catch (TelegramApiException e) {
                        Sentry.captureException(e);
                        log.error(e.getMessage());
                        if (e.getMessage().contains("403")) {
                            applicationEventPublisher.publishEvent(new UserBlockedBotEvent(userEntity, loc));
                            locationRepository.delete(loc);
                            userRepository.delete(userEntity);
                        }
                    }
                }
            } catch (Exception e) {
                Sentry.captureException(e);
            }
        }
    }
}

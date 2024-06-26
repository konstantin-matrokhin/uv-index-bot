package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    @Scheduled(cron = "@hourly")
    public void scheduledNotifications() {
        List<LocationEntity> allLocations = locationRepository.findAll();
        log.info("Scheduled updating UV info scheduled for {} locations", allLocations.size());
        for (LocationEntity loc : allLocations) {
            Long chatId = loc.getUserEntity().getChatId();
            LocationInfo locationInfo = locationInfoService.getLocationInfo(loc.coordinates());
            float oldIndex = loc.getLastUvIndex();
            float newIndex = locationInfo.getUvIndex().getValue();
            float delta = Math.abs(oldIndex - newIndex);
            String text;
            if (delta > 0) {
                loc.setLastUvIndex(newIndex);
                text = recommendationService.createRecommendationText(locationInfo);
            } else {
                return;
            }
            try {
                telegramClient.execute(SendMessage.builder()
                    .text(text)
                    .chatId(chatId)
                    .build()
                );
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                if (e.getMessage().contains("403")) {
                    locationRepository.delete(loc);
                    userRepository.delete(loc.getUserEntity());
                }
            }
        }
    }
}

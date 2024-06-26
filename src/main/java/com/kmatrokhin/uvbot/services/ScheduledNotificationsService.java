package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.UvIndex;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.telegram.UvIndexAbility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
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

    @Transactional
    @Scheduled(cron = "@hourly")
//    @Scheduled(initialDelay = 0, fixedDelay = 60_000)
    public void scheduledNotifications() {
        List<LocationEntity> allLocations = locationRepository.findAll();
        log.info("Scheduled updating UV info scheduled for {} locations", allLocations.size());
        for (LocationEntity loc : allLocations) {
            Long chatId = loc.getUserEntity().getChatId();
            LocationInfo locationInfo = locationInfoService.getLocationInfo(loc.coordinates());
            UvIndex oldIndex = new UvIndex(loc.getLastUvIndex());
            UvIndex newIndex = locationInfo.getUvIndex();
            if (!oldIndex.getHarm().equals(newIndex.getHarm())) {
                loc.setLastUvIndex(newIndex.getValue());
                try {
                    telegramClient.execute(SendMessage.builder()
                            .replyMarkup(InlineKeyboardMarkup.builder()
                                .build())
                            .text(recommendationService.createRecommendationText(locationInfo))
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
}
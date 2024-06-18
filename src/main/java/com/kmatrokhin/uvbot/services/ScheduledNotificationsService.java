package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledNotificationsService {
    private final TelegramClient telegramClient;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final LocationInfoService locationInfoService;

    @Scheduled(cron = "@hourly")
    public void scheduledNotifications() {
        log.info("Sending scheduled notifications for {} users", userRepository.count());
        for (LocationEntity loc : locationRepository.findAll()) {
            Long chatId = loc.getUserEntity().getChatId();
            Coordinates coordinates = Coordinates.of(loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue());
            String text = locationInfoService.getLocationInfo(coordinates);
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

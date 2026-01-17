package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.entities.LocationEntity
import com.kmatrokhin.uvbot.events.UserBlockedBotEvent
import com.kmatrokhin.uvbot.repositories.LocationRepository
import com.kmatrokhin.uvbot.repositories.UserRepository
import com.kmatrokhin.uvbot.telegram.UvIndexAbility
import io.sentry.Sentry
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient
import kotlin.math.abs

@Service
class ScheduledNotificationsService(
    private val telegramClient: TelegramClient,
    private val locationRepository: LocationRepository,
    private val userRepository: UserRepository,
    private val locationInfoService: LocationInfoService,
    private val recommendationService: RecommendationService,
    private val uvIndexAbility: UvIndexAbility,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "@hourly")
    fun scheduledNotificationsForUsers() {
        val allLocations: List<LocationEntity> = locationRepository.findAll() as List<LocationEntity>
        log.info(
            "Scheduled updating UV info scheduled for {} locations",
            allLocations.size
        )
        for (loc in allLocations) {
            Thread.sleep(5000)
            try {
                val userEntity = loc.userEntity
                if (!userEntity.isSubscribed) {
                    continue
                }
                val chatId = userEntity.chatId
                val locationInfo = locationInfoService.getLocationInfo(loc.coordinates(), loc.name)
                val lastUvIndex: Float = loc.lastUvIndex!!
                val newIndex = locationInfo.weather.uvi
                if (abs(lastUvIndex - newIndex) >= 0.9) {
                    loc.lastUvIndex = newIndex
                    try {
                        telegramClient.execute(
                            SendMessage.builder()
                                .replyMarkup(uvIndexAbility.mainKeyboard())
                                .text(
                                    recommendationService.createRecommendationText(
                                        locationInfo,
                                        userEntity.language
                                    )
                                )
                                .parseMode("html")
                                .chatId(chatId)
                                .build()
                        )
                    } catch (e: TelegramApiException) {
                        Sentry.captureException(e)
                        log.error(e.message)
                        if (e.message?.contains("403") ?: false) {
                            applicationEventPublisher.publishEvent(UserBlockedBotEvent(userEntity, loc))
                            locationRepository.delete(loc)
                            userRepository.delete(userEntity)
                        }
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
    }
}

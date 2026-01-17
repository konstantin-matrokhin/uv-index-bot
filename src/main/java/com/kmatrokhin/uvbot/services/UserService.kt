package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.UserSignUp
import com.kmatrokhin.uvbot.entities.LocationEntity
import com.kmatrokhin.uvbot.entities.UserEntity
import com.kmatrokhin.uvbot.entities.UserLanguage
import com.kmatrokhin.uvbot.events.UserRegisteredEvent
import com.kmatrokhin.uvbot.repositories.LocationRepository
import com.kmatrokhin.uvbot.repositories.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserService(
    private val userRepository: UserRepository,
    private val locationRepository: LocationRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun signUpOrUpdate(userSignUp: UserSignUp): UserEntity {
        val chatId = userSignUp.chatId
        val username = userSignUp.name
        val locationInfo = userSignUp.locationInfo

        val coordinates = locationInfo.coordinates

        val userEntityOpt = userRepository.findByChatId(chatId)
        val userEntity: UserEntity
        if (userEntityOpt != null) {
            userEntity = userEntityOpt
            userEntity.name = username
            userEntity.isSubscribed = true
            val locationEntity = locationRepository.findByUserEntity(userEntity)
                ?: throw IllegalStateException("Location not found for chatId ${userEntity.chatId}")
            locationEntity.name = locationInfo.name
            locationEntity.latitude = coordinates.latitude
            locationEntity.longitude = coordinates.longitude
            locationEntity.lastUvIndex = locationInfo.weather.uvi
        } else {
            userEntity = UserEntity(
                chatId = chatId,
                name = username,
                isSubscribed = true,
                createdAt = Instant.now()
            )
            val newLocation = LocationEntity(
                name = locationInfo.name,
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                lastUvIndex = locationInfo.weather.uvi,
                userEntity = userEntity,
                createdAt = Instant.now()
            )
            userRepository.save(userEntity)
            locationRepository.save<LocationEntity?>(newLocation)
            applicationEventPublisher.publishEvent(UserRegisteredEvent(userEntity, newLocation))
        }
        return userEntity
    }

    @Transactional
    fun setSubscription(chatId: Long, isSubscribed: Boolean) {
        userRepository.findByChatId(chatId)?.isSubscribed = isSubscribed
    }

    @Transactional
    fun isSubscribed(chatId: Long): Boolean {
        return userRepository.findByChatId(chatId)?.isSubscribed ?: false
    }

    @Transactional
    fun setLanguage(chatId: Long, language: UserLanguage) {
        userRepository.findByChatId(chatId)?.language = language
    }
}

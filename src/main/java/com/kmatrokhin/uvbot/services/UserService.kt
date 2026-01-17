package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.LocationInfo
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

        val existingUser = userRepository.findByChatId(chatId) ?: run {
            val userEntity = createUser(chatId, username)
            val newLocation = createLocation(userEntity, locationInfo)
            userRepository.save(userEntity)
            locationRepository.save(newLocation)
            applicationEventPublisher.publishEvent(UserRegisteredEvent(userEntity, newLocation))
            return userEntity
        }
        return updateExistingUser(existingUser, locationInfo, username, coordinates)
    }

    private fun createUser(chatId: Long, username: String?): UserEntity {
        return UserEntity(
            chatId = chatId,
            name = username,
            isSubscribed = true,
            createdAt = Instant.now()
        )
    }

    private fun createLocation(userEntity: UserEntity, locationInfo: LocationInfo): LocationEntity {
        return LocationEntity(
            name = locationInfo.name,
            latitude = locationInfo.coordinates.latitude,
            longitude = locationInfo.coordinates.longitude,
            lastUvIndex = locationInfo.weather.uvi,
            userEntity = userEntity,
            createdAt = Instant.now()
        )
    }

    private fun updateExistingUser(
        existingUser: UserEntity,
        locationInfo: LocationInfo,
        username: String?,
        coordinates: Coordinates
    ): UserEntity {
        existingUser.name = username
        existingUser.isSubscribed = true
        val locationEntity = locationRepository.findByUserEntity(existingUser)?.let {
            it.apply {
                name = locationInfo.name
                latitude = coordinates.latitude
                longitude = coordinates.longitude
                lastUvIndex = locationInfo.weather.uvi
            }
        }
        return existingUser
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

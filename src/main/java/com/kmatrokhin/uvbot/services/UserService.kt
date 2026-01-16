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

        val userEntityOpt = userRepository!!.findByChatId(chatId)
        val coordinates = locationInfo!!.coordinates
        val userEntity: UserEntity
        if (userEntityOpt!!.isPresent()) {
            userEntity = userEntityOpt.get()
            userEntity.name = username
            userEntity.isSubscribed = true
            val locationEntity = locationRepository!!.getByUserEntity(userEntity)
            locationEntity.name = locationInfo.name
            locationEntity.latitude = coordinates.latitude
            locationEntity.longitude = coordinates.longitude
            locationEntity.lastUvIndex = locationInfo.weather.uvi
        } else {
            userEntity = UserEntity()
            userEntity.chatId = chatId
            userEntity.name = username
            userEntity.isSubscribed = true
            userEntity.createdAt = Instant.now()
            val newLocation = LocationEntity()
            newLocation.name = locationInfo.name
            newLocation.latitude = coordinates.latitude
            newLocation.longitude = coordinates.longitude
            newLocation.lastUvIndex = locationInfo.weather.uvi
            newLocation.userEntity = userEntity
            newLocation.createdAt = Instant.now()
            userRepository.save<UserEntity?>(userEntity)
            locationRepository!!.save<LocationEntity?>(newLocation)
            applicationEventPublisher!!.publishEvent(UserRegisteredEvent(userEntity, newLocation))
        }
        return userEntity
    }

    @Transactional
    fun setSubscription(chatId: Long?, isSubscribed: Boolean) {
        userRepository!!.getByChatId(chatId).isSubscribed = isSubscribed
    }

    @Transactional
    fun isSubscribed(chatId: Long?): Boolean {
        return userRepository!!.findByChatId(chatId)!!.map<Boolean>(UserEntity::isSubscribed).orElse(false)
    }

    @Transactional
    fun setLanguage(chatId: Long?, language: UserLanguage) {
        userRepository!!.getByChatId(chatId).language = language
    }
}

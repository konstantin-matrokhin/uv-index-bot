package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.UserSignUp;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.entities.UserLanguage;
import com.kmatrokhin.uvbot.events.UserRegisteredEvent;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public UserEntity signUpOrUpdate(UserSignUp userSignUp) {
        Long chatId = userSignUp.getChatId();
        String username = userSignUp.getName();
        LocationInfo locationInfo = userSignUp.getLocationInfo();

        Optional<UserEntity> userEntityOpt = userRepository.findByChatId(chatId);
        Coordinates coordinates = locationInfo.getCoordinates();
        UserEntity userEntity;
        if (userEntityOpt.isPresent()) {
            userEntity = userEntityOpt.get();
            userEntity.setName(username);
            userEntity.setSubscribed(true);
            LocationEntity locationEntity = locationRepository.getByUserEntity(userEntity);
            locationEntity.setName(locationInfo.getName());
            locationEntity.setLatitude(coordinates.getLatitude());
            locationEntity.setLongitude(coordinates.getLongitude());
            locationEntity.setLastUvIndex(locationInfo.getWeather().getUvi());
        } else {
            userEntity = new UserEntity();
            userEntity.setChatId(chatId);
            userEntity.setName(username);
            userEntity.setSubscribed(true);
            userEntity.setCreatedAt(Instant.now());
            LocationEntity newLocation = new LocationEntity();
            newLocation.setName(locationInfo.getName());
            newLocation.setLatitude(coordinates.getLatitude());
            newLocation.setLongitude(coordinates.getLongitude());
            newLocation.setLastUvIndex(locationInfo.getWeather().getUvi());
            newLocation.setUserEntity(userEntity);
            newLocation.setCreatedAt(Instant.now());
            userRepository.save(userEntity);
            locationRepository.save(newLocation);
            applicationEventPublisher.publishEvent(new UserRegisteredEvent(userEntity, newLocation));
        }
        return userEntity;
    }

    @Transactional
    public void setSubscription(Long chatId, boolean isSubscribed) {
        userRepository.getByChatId(chatId).setSubscribed(isSubscribed);
    }

    @Transactional
    public boolean isSubscribed(Long chatId) {
        return userRepository.findByChatId(chatId).map(UserEntity::isSubscribed).orElse(false);
    }

    @Transactional
    public void setLanguage(Long chatId, UserLanguage language) {
        userRepository.getByChatId(chatId).setLanguage(language);
    }
}

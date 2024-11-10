package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.UserSignUp;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.entities.UserLanguage;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

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
            userEntity
                .setName(username)
                .setIsSubscribed(true);
            locationRepository.getByUserEntity(userEntity)
                .setName(locationInfo.getName())
                .setLatitude(coordinates.getLatitude())
                .setLongitude(coordinates.getLongitude())
                .setLastUvIndex(locationInfo.getWeather().getUvi());
        } else {
            userEntity = new UserEntity()
                .setChatId(chatId)
                .setName(username)
                .setIsSubscribed(true)
                .setCreatedAt(Instant.now());
            LocationEntity newLocation = new LocationEntity()
                .setName(locationInfo.getName())
                .setLatitude(coordinates.getLatitude())
                .setLongitude(coordinates.getLongitude())
                .setLastUvIndex(locationInfo.getWeather().getUvi())
                .setUserEntity(userEntity)
                .setCreatedAt(Instant.now());
            userRepository.save(userEntity);
            locationRepository.save(newLocation);
        }
        return userEntity;
    }

    @Transactional
    public void setSubscription(Long chatId, boolean isSubscribed) {
        userRepository.getByChatId(chatId).setIsSubscribed(isSubscribed);
    }

    @Transactional
    public boolean isSubscribed(Long chatId) {
        return userRepository.findByChatId(chatId).map(UserEntity::getIsSubscribed).orElse(false);
    }

    @Transactional
    public void setLanguage(Long chatId, UserLanguage language) {
        userRepository.getByChatId(chatId).setLanguage(language);
    }
}

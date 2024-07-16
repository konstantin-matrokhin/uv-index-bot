package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
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
    public void signUpOrUpdate(String username, Long chatId, LocationInfo locationInfo) {
        Optional<UserEntity> userEntityOpt = userRepository.findByChatId(chatId);
        Coordinates coordinates = locationInfo.getCoordinates();
        if (userEntityOpt.isPresent()) {
            UserEntity userEntity = userEntityOpt.get();
            userEntity.setName(username);
            locationRepository.getByUserEntity(userEntity)
                .setName(locationInfo.getName())
                .setLatitude(coordinates.getLatitude())
                .setLongitude(coordinates.getLongitude())
                .setLastUvIndex(locationInfo.getWeather().getUvi());
        } else {
            UserEntity newUser = new UserEntity()
                .setChatId(chatId)
                .setName(username)
                .setCreatedAt(Instant.now());
            LocationEntity newLocation = new LocationEntity()
                .setName(locationInfo.getName())
                .setLatitude(coordinates.getLatitude())
                .setLongitude(coordinates.getLongitude())
                .setLastUvIndex(locationInfo.getWeather().getUvi())
                .setUserEntity(newUser)
                .setCreatedAt(Instant.now());
            userRepository.save(newUser);
            locationRepository.save(newLocation);
        }
    }

}

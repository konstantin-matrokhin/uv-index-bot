package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.repositories.LocationRepository;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    @Transactional
    public void signUp(Long chatId, Coordinates coordinates) {
        UserEntity userEntity = userRepository.findByChatId(chatId).orElse(new UserEntity()
            .setId(UUID.randomUUID())
            .setChatId(chatId));
        LocationEntity locationEntity = locationRepository.findByUserEntity(userEntity).orElse(new LocationEntity()
                .setId(UUID.randomUUID())
                .setUserEntity(userEntity)
            )
            .setName("Unknown")
            .setLatitude(coordinates.getLatitude().floatValue())
            .setLongitude(coordinates.getLongitude().floatValue());
        userRepository.save(userEntity);
        locationRepository.save(locationEntity);
    }

}

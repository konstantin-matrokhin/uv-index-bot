package com.kmatrokhin.uvbot.events;

import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserBlockedBotEvent {
    private final UserEntity userEntity;
    private final LocationEntity locationEntity;
}

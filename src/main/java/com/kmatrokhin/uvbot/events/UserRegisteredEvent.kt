package com.kmatrokhin.uvbot.events

import com.kmatrokhin.uvbot.entities.LocationEntity
import com.kmatrokhin.uvbot.entities.UserEntity

data class UserRegisteredEvent(
    val userEntity: UserEntity,
    val locationEntity: LocationEntity
)

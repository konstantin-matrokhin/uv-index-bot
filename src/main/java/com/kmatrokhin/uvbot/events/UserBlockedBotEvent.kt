package com.kmatrokhin.uvbot.events

import com.kmatrokhin.uvbot.entities.LocationEntity
import com.kmatrokhin.uvbot.entities.UserEntity

data class UserBlockedBotEvent (
    val userEntity: UserEntity? = null,
    val locationEntity: LocationEntity? = null
)

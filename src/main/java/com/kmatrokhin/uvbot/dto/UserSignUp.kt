package com.kmatrokhin.uvbot.dto

data class UserSignUp (
    var chatId: Long? = null,
    var name: String? = null,
    var locationInfo: LocationInfo? = null
)

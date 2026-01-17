package com.kmatrokhin.uvbot.dto

data class UserSignUp (
    var chatId: Long,
    var name: String? = null,
    var locationInfo: LocationInfo
)

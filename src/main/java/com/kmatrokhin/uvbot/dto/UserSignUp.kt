package com.kmatrokhin.uvbot.dto

data class UserSignUp (
    val chatId: Long,
    val name: String? = null,
    val locationInfo: LocationInfo
)

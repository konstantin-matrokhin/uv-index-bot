package com.kmatrokhin.uvbot.chatgpt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Message (
    var role: String,
    var content: String
)

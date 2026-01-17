package com.kmatrokhin.uvbot.chatgpt

data class ChatRequest(
    val model: String,
    val sysPrompt: String,
    val userPrompt: String
) {
    val messages: List<Message> = listOf(
        Message("system", sysPrompt),
        Message("user", userPrompt)
    )
    val n = 1
    val temperature = 1.2
}

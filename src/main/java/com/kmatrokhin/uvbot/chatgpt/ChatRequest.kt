package com.kmatrokhin.uvbot.chatgpt

data class ChatRequest(
    val model: String?,
    val sysPrompt: String,
    val userPrompt: String
) {
    var messages: MutableList<Message?> = ArrayList()
    val n = 1
    val temperature = 1.2

    init {
        this.messages.add(Message("system", sysPrompt))
        this.messages.add(Message("user", userPrompt))
    }
}

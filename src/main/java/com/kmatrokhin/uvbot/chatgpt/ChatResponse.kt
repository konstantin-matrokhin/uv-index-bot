package com.kmatrokhin.uvbot.chatgpt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatResponse(
    val choices: List<Choice>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(
        val index: Int = 0,
        val message: Message
    )
}

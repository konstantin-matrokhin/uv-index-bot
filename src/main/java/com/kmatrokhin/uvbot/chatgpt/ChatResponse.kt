package com.kmatrokhin.uvbot.chatgpt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatResponse(
    var choices: List<Choice>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(
        var index: Int = 0,
        var message: Message
    )
}

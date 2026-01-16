package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.chatgpt.ChatGPTClient
import com.kmatrokhin.uvbot.chatgpt.ChatRequest
import com.kmatrokhin.uvbot.chatgpt.ChatResponse
import com.kmatrokhin.uvbot.dto.LocationInfo
import com.kmatrokhin.uvbot.entities.UserLanguage
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
@Slf4j
class ChatGPTService(
    private var chatGPTClient: ChatGPTClient
) {
    private val log = LoggerFactory.getLogger(javaClass)


    @Value("\${openai.key}")
    private val apiKey: String? = null

    @Value("\${openai.model}")
    private val model: String? = null

    @Value("\${openai.uv.prompt.system}")
    private lateinit var systemPrompt: String

    @Value("\${openai.uv.prompt.user}")
    private val userPromptTemplate: String? = null

    fun getChatResponse(locationInfo: LocationInfo, userLanguage: UserLanguage): ChatResponse? {
        val weather = locationInfo.weather
        val userPrompt = String.format(
            userPromptTemplate!!,
            weather.uvi,
            weather.temperature,
            if (weather.isDay) "день" else "ночь",
            locationInfo.name
        )
        val language = if (userLanguage == UserLanguage.ENGLISH) "английском" else "русском"
        val chatRequest = ChatRequest(model, systemPrompt.format(language), userPrompt)
        val authorizationHeader = "Bearer $apiKey"
        log.info("Request to ChatGPT: $chatRequest")
        val chatCompletion = chatGPTClient?.getChatCompletion(authorizationHeader, chatRequest)
        log.info("Response from ChatGPT: $chatCompletion")
        return chatCompletion
    }
}

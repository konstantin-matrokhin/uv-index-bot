package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.chatgpt.ChatGPTClient;
import com.kmatrokhin.uvbot.chatgpt.ChatRequest;
import com.kmatrokhin.uvbot.chatgpt.ChatResponse;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.Weather;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatGPTService {
    private final ChatGPTClient chatGPTClient;
    @Value("${openai.key}")
    private String apiKey;
    @Value("${openai.model}")
    private String model;
    @Value("${openai.uv.prompt.system}")
    private String systemPrompt;
    @Value("${openai.uv.prompt.user}")
    private String userPromptTemplate;

    public ChatResponse getChatResponse(LocationInfo locationInfo) {
        Weather weather = locationInfo.getWeather();
        String userPrompt = String.format(userPromptTemplate, weather.getUvi(), weather.getTemperature(), weather.isDay() ? "день" : "ночь", locationInfo.getName());
        ChatRequest chatRequest = new ChatRequest(model, systemPrompt, userPrompt);
        String authorizationHeader = "Bearer " + apiKey;
        log.info("Request to ChatGPT: " + chatRequest);
        ChatResponse chatCompletion = chatGPTClient.
            getChatCompletion(authorizationHeader, chatRequest);
        log.info("Response from ChatGPT: " + chatCompletion);
        return chatCompletion;
    }
}

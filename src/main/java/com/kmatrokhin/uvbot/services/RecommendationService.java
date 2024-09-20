package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.chatgpt.ChatResponse;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final ChatGPTService chatGPTService;

    @Value("${openai.enabled:true}")
    private boolean openaiEnabled;

    public String createRecommendationText(LocationInfo locationInfo) {
        Weather weather = locationInfo.getWeather();
        StringBuilder recommendation = new StringBuilder();
        recommendation
            .append("☀️ <b>UV index:</b> ").append(weather.getUvi())
            .append(" (").append(weather.getUvHarm().getText()).append(")").append("\n")
            .append("🌡️️ <b>Temperature:</b> ").append(weather.getTemperature()).append("°C\n")
            .append("📍 <b>Place:</b> ").append(locationInfo.getName()).append("\n\n");
        if (openaiEnabled) {
            ChatResponse chatResponse = chatGPTService.getChatResponse(locationInfo);
            String aiRecommendation = chatResponse.getChoices().get(0).getMessage().getContent();
            recommendation
                .append("🤖 <b>AI recommendation:</b> ")
                .append(aiRecommendation)
                .append("\n\n");
        }
        return recommendation.toString();
    }
}

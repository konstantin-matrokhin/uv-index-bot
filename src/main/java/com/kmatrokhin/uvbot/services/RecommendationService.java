package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.chatgpt.ChatResponse;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final ChatGPTService chatGPTService;

    public String createRecommendationText(LocationInfo locationInfo) {
        Weather weather = locationInfo.getWeather();
        ChatResponse chatResponse = chatGPTService.getChatResponse(locationInfo);
        String aiRecommendation = chatResponse.getChoices().get(0).getMessage().getContent();
        StringBuilder recommendation = new StringBuilder();
        recommendation
            .append("☀️ <b>UV index:</b> ").append(weather.getUvi()).append("\n\n")
            .append("🌡️️ <b>Temperature:</b> ").append(weather.getTemperature()).append("°C\n\n")
            .append("📍 <b>Place:</b> ").append(locationInfo.getName())
            .append(" (").append(weather.getUvHarm().toString()).append(")").append("\n\n")
            .append("🤖 <b>AI Recommendation:</b> ")
            .append(aiRecommendation)
            .append("\n");
        return recommendation.toString();
    }
}

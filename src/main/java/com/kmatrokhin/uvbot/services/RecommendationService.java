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
            .append("☀️ <b>УФ индекс:</b> ").append(weather.getUvi())
            .append(" (").append(weather.getUvHarm().getText()).append(")").append("\n\n")
            .append("🌡️️ <b>Температура:</b> ").append(weather.getTemperature()).append("°C\n\n")
            .append("📍 <b>Место:</b> ").append(locationInfo.getName());
        if (openaiEnabled) {
            ChatResponse chatResponse = chatGPTService.getChatResponse(locationInfo);
            String aiRecommendation = chatResponse.getChoices().get(0).getMessage().getContent();
            recommendation
                .append("🤖 <b>Рекомендация ИИ:</b> ")
                .append(aiRecommendation)
                .append("\n");
        }
        return recommendation.toString();
    }
}

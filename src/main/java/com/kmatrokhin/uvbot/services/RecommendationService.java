package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.chatgpt.ChatResponse;
import com.kmatrokhin.uvbot.dto.I18nProperties;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.Weather;
import com.kmatrokhin.uvbot.entities.UserLanguage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final ChatGPTService chatGPTService;
    private final I18nProperties i18nProperties;

    @Value("${openai.enabled:true}")
    private boolean openaiEnabled;

    public String createRecommendationText(LocationInfo locationInfo, UserLanguage userLanguage) {
        Weather weather = locationInfo.getWeather();

        String aiRecommendation = "";
        if (openaiEnabled) {
            ChatResponse chatResponse = chatGPTService.getChatResponse(locationInfo, userLanguage);
            aiRecommendation = chatResponse.getChoices().get(0).getMessage().getContent();
        }

        Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("uvi", weather.getUvi());
        valueMap.put("uvi_level", weather.getUvHarm().getText());
        valueMap.put("temperature", weather.getTemperature());
        valueMap.put("place", locationInfo.getName());
        valueMap.put("ai_recommendation", aiRecommendation);

        StringSubstitutor stringSubstitutor = new StringSubstitutor(valueMap);
        String recommendation = i18nProperties.get(userLanguage, "recommendation");
        return stringSubstitutor.replace(recommendation);
    }
}

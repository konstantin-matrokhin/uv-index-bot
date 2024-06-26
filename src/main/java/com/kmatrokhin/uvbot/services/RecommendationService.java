package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.LocationInfo;
import org.springframework.stereotype.Service;

@Service
public class RecommendationService {
    public static final String BASE_TEMPLATE = "UVI in %s is %.2f.";

    public String createRecommendationText(LocationInfo locationInfo) {
        String initStr = String.format(BASE_TEMPLATE, locationInfo.getName(), locationInfo.getUvIndex().getValue());
        StringBuilder recommendation = new StringBuilder(initStr);
        recommendation.append("\n");
        recommendation.append(
            switch (locationInfo.getUvIndex().getHarm()) {
                case LOW -> "You can safely enjoy being outside!";
                case MODERATE -> "Seek shade during midday hours! Slip on a shirt, slop on sunscreen and slap on hat!";
                case HIGH -> "Avoid being outside during midday hours! Make sure you seek shade! Shirt, sunscreen and hat are a must!";
                case VERY_HIGH, EXTREME -> "Extra protection needed. Be careful outside, especially during late morning through mid-afternoon. If your shadow is shorter than you, seek shade and wear protective clothing, a wide-brimmed hat, and sunglasses, and generously apply a minimum of  SPF-15, broad-spectrum sunscreen on exposed skin.";
            }
        );
        recommendation.append("\n");
        return recommendation.toString();
    }
}

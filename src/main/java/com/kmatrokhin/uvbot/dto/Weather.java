package com.kmatrokhin.uvbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Weather {
    private final float uvi;
    private final float temperature;
    private final boolean isDay;

    public Harm getUvHarm() {
        return Harm.fromIndex(uvi);
    }

    @Getter
    public enum Harm {
        LOW(2, "Low"),
        MODERATE(5, "Moderate"),
        HIGH(7, "High"),
        VERY_HIGH(10, "Very high"),
        EXTREME(Float.POSITIVE_INFINITY, "Extreme");

        private final float maxUvIndex;
        private final String text;

        Harm(float maxUvIndex, String text) {
            this.maxUvIndex = maxUvIndex;
            this.text = text;
        }

        public static Harm fromIndex(float uvIndex) {
            for (Harm harm : Harm.values()) {
                if (uvIndex <= harm.maxUvIndex) {
                    return harm;
                }
            }
            throw new IllegalArgumentException();
        }
    }
}

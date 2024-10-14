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
        LOW(2, "low"),
        MODERATE(5, "moderate"),
        HIGH(7, "high"),
        VERY_HIGH(10, "very_high"),
        EXTREME(Float.POSITIVE_INFINITY, "extreme");

        private final float maxUvIndex;
        private final String i18nKey;

        Harm(float maxUvIndex, String i18nKey) {
            this.maxUvIndex = maxUvIndex;
            this.i18nKey = i18nKey;
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

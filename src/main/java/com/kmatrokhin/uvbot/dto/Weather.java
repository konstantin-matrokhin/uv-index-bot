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
        LOW(2),
        MODERATE(5),
        HIGH(7),
        VERY_HIGH(10),
        EXTREME(Float.POSITIVE_INFINITY);

        private final float maxUvIndex;

        Harm(float maxUvIndex) {
            this.maxUvIndex = maxUvIndex;
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

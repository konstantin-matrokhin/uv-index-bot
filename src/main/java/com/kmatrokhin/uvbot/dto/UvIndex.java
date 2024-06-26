package com.kmatrokhin.uvbot.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UvIndex {
    private final float value;

    public Harm getHarm() {
        return Harm.fromIndex(value);
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

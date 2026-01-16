package com.kmatrokhin.uvbot.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(
    var uvi: Float = 0f,
    var temperature: Float = 0f,
    var isDay: Boolean = false
) {
    val uvHarm: Harm
        get() = Harm.fromIndex(uvi)

    enum class Harm(val thresholdUvIndex: Float) {
        LOW(2f),
        MODERATE(5f),
        HIGH(7f),
        VERY_HIGH(10f),
        EXTREME(Float.POSITIVE_INFINITY);

        companion object {
            fun fromIndex(uvIndex: Float): Harm =
                entries.first { uvIndex <= it.thresholdUvIndex }
        }
    }
}

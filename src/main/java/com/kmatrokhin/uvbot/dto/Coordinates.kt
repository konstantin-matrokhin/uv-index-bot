package com.kmatrokhin.uvbot.dto

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        @JvmStatic
        fun of(latitude: Double, longitude: Double): Coordinates {
            return Coordinates(latitude, longitude)
        }
    }
}

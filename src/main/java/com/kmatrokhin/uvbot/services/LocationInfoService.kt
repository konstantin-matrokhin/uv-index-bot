package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.LocationInfo
import com.kmatrokhin.uvbot.entities.UserLanguage
import org.springframework.stereotype.Service

@Service
class LocationInfoService(
    private val weatherService: WeatherService,
    private val geocodingService: GeocodingService
) {
    fun getLocationInfo(
        coordinates: Coordinates,
        language: UserLanguage,
        locationName: String? = null
    ): LocationInfo {
        val weather = weatherService.getWeather(coordinates)
        val resolvedName = locationName ?: geocodingService.getLocationName(coordinates, language)
        return LocationInfo(resolvedName, coordinates, weather)
    }
}

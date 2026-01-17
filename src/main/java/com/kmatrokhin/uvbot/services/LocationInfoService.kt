package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.LocationInfo
import org.springframework.stereotype.Service

@Service
class LocationInfoService(
    private val weatherService: WeatherService,
    private val geocodingService: GeocodingService
) {
    fun getLocationInfo(
        coordinates: Coordinates,
        locationName: String? = null
    ): LocationInfo {
        val weather = weatherService.getWeather(coordinates)
        return LocationInfo(locationName ?: geocodingService.getLocationName(coordinates), coordinates, weather)
    }
}

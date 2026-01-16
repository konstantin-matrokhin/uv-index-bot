package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.LocationInfo
import org.springframework.stereotype.Service

@Service
class LocationInfoService(
    private val weatherService: WeatherService,
    private val geocodingService: GeocodingService
) {

    fun getLocationInfo(coordinates: Coordinates): LocationInfo {
        return getLocationInfo(coordinates, null)
    }

    fun getLocationInfo(coordinates: Coordinates, locationName: String?): LocationInfo {
        var locationName = locationName
        val weather = weatherService!!.getWeather(coordinates)
        if (locationName == null) {
            locationName = geocodingService!!.getLocationName(coordinates)
        }
        return LocationInfo(locationName!!, coordinates, weather)
    }
}

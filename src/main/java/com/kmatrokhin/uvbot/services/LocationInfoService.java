package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import com.kmatrokhin.uvbot.dto.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final WeatherService weatherService;
    private final GeocodingService geocodingService;

    public LocationInfo getLocationInfo(Coordinates coordinates) {
        Weather weather = weatherService.getWeather(coordinates);
        String locationName = geocodingService.getLocationName(coordinates);
        return new LocationInfo(locationName, coordinates, weather);
    }
}

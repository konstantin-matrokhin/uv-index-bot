package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final UvIndexService uvIndexService;
    private final GeocodingService geocodingService;

    public String getLocationInfo(Coordinates coordinates) {
        String uvIndex = uvIndexService.getUvIndex(coordinates);
        String locationName = geocodingService.getLocationName(coordinates);
        return "UV index in " + locationName + " now is " + uvIndex;
    }
}

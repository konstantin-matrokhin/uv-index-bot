package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.LocationInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationInfoService {
    private final UvIndexService uvIndexService;
    private final GeocodingService geocodingService;

    public LocationInfo getLocationInfo(Coordinates coordinates) {
        float uvIndex = uvIndexService.getUvIndex(coordinates);
        String locationName = geocodingService.getLocationName(coordinates);
        return new LocationInfo(locationName, coordinates, uvIndex);
    }
}

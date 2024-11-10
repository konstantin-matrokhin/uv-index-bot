package com.kmatrokhin.uvbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.kmatrokhin.uvbot.dto.Coordinates;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodingService {
    private static final String URL_TEMPLATE = "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s&zoom=18&addressdetails=1&email=remillary@gmail.com";
    private final HttpExchangeService httpExchangeService;

    @SneakyThrows
    public String getLocationName(Coordinates coordinates) {
        log.info("Resolving location name for {}", coordinates);
        JsonNode jsonNode = httpExchangeService.request(URL_TEMPLATE, coordinates);
        String addressType = jsonNode.get("addresstype").asText();
        if (jsonNode.at("/address/city").isTextual()) {
            return jsonNode.at("/address/city").textValue();
        }
        return jsonNode.at("/address/" + addressType).textValue();
    }
}

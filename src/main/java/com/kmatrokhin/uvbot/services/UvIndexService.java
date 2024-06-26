package com.kmatrokhin.uvbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.UvIndex;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UvIndexService {
    private static final String URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=shortwave_radiation,direct_radiation,uv_index,uv_index_clear_sky&forecast_days=1&forecast_hours=1";
    private final HttpExchangeService httpExchangeService;

    @SneakyThrows
    public UvIndex getUvIndex(Coordinates coordinates) {
        log.info("Requesting uv index for coordinates: {}", coordinates);
        JsonNode jsonNode = httpExchangeService.request(URL_TEMPLATE, coordinates);
        double uvIndexValue = jsonNode.at("/hourly/uv_index").iterator().next().asDouble();
        return new UvIndex((float) uvIndexValue);
    }
}

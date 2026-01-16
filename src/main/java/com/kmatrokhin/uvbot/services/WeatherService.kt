package com.kmatrokhin.uvbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.kmatrokhin.uvbot.dto.Coordinates;
import com.kmatrokhin.uvbot.dto.Weather;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final String URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,is_day,precipitation,rain&hourly=uv_index,uv_index_clear_sky&forecast_days=1&forecast_hours=1";
    private final HttpExchangeService httpExchangeService;

    @SneakyThrows
    public Weather getWeather(Coordinates coordinates) {
        log.info("Requesting weather for coordinates: {}", coordinates);
        JsonNode jsonNode = httpExchangeService.request(URL_TEMPLATE, coordinates);
        float uvIndexValue = (float) jsonNode.at("/hourly/uv_index").iterator().next().asDouble();
        float temperature = (float) jsonNode.at("/current/temperature_2m").asDouble();
        boolean isDay = jsonNode.at("/current/is_day").asInt() == 1;
        return new Weather(uvIndexValue, temperature, isDay);
    }
}

package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.Weather
import lombok.SneakyThrows
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WeatherService(
    private var httpExchangeService: HttpExchangeService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SneakyThrows
    fun getWeather(coordinates: Coordinates): Weather {
        log.info("Requesting weather for coordinates: {}", coordinates)
        val jsonNode = httpExchangeService!!.request(URL_TEMPLATE, coordinates)
        val uvIndexValue = jsonNode.at("/hourly/uv_index").iterator().next().asDouble().toFloat()
        val temperature = jsonNode.at("/current/temperature_2m").asDouble().toFloat()
        val isDay = jsonNode.at("/current/is_day").asInt() == 1
        return Weather(uvIndexValue, temperature, isDay)
    }

    companion object {
        private const val URL_TEMPLATE =
            "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&current=temperature_2m,is_day,precipitation,rain&hourly=uv_index,uv_index_clear_sky&forecast_days=1&forecast_hours=1"
    }
}

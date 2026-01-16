package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import lombok.SneakyThrows
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GeocodingService(
    private val httpExchangeService: HttpExchangeService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SneakyThrows
    fun getLocationName(coordinates: Coordinates): String? {
        log.info("Resolving location name for {}", coordinates)
        val jsonNode = httpExchangeService!!.request(URL_TEMPLATE, coordinates)
        val addressType = jsonNode.get("addresstype").asText()
        if (jsonNode.at("/address/city").isTextual) {
            return jsonNode.at("/address/city").textValue()
        }
        return jsonNode.at("/address/$addressType").textValue()
    }

    companion object {
        private const val URL_TEMPLATE =
            "https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}&zoom=18&addressdetails=1&email=konstant.matrokhin@gmail.com"
    }
}

package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.I18nProperties
import com.kmatrokhin.uvbot.entities.UserLanguage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GeocodingService(
    private val httpExchangeService: HttpExchangeService,
    private val i18nProperties: I18nProperties
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun getLocationName(coordinates: Coordinates): String =
        getLocationName(coordinates, UserLanguage.ENGLISH)

    fun getLocationName(coordinates: Coordinates, language: UserLanguage): String {
        log.info("Resolving location name for {}", coordinates)

        val acceptLanguage = when (language) {
            UserLanguage.RUSSIAN -> "ru,en"
            UserLanguage.ENGLISH -> "en,ru"
        }

        val jsonNode = httpExchangeService.request(
            "https://nominatim.openstreetmap.org/reverse" +
                "?format=jsonv2" +
                "&lat=${coordinates.latitude}" +
                "&lon=${coordinates.longitude}" +
                "&zoom=10" +
                "&addressdetails=1" +
                "&layer=address" +
                "&accept-language=$acceptLanguage" +
                "&email=konstant.matrokhin@gmail.com"
        )

        val error = jsonNode.path("error").asText("")
        if (error.isNotBlank()) {
            log.info("Nominatim reverse failed: {}", error)
            return i18nProperties.get(language, "unknown_place")
        }

        val address = jsonNode.path("address")
        val settlementKeys = listOf("city", "town", "village", "hamlet", "municipality", "locality")
        for (key in settlementKeys) {
            val value = address.path(key)
            if (value.isTextual && value.asText().isNotBlank()) {
                return value.asText()
            }
        }

        val displayName = jsonNode.path("display_name").asText("")
        val fallback = displayName.substringBefore(",").trim()
        return fallback.ifBlank { i18nProperties.get(language, "unknown_place") }
    }
}

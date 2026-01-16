package com.kmatrokhin.uvbot.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kmatrokhin.uvbot.dto.Coordinates
import lombok.SneakyThrows
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.util.*

@Service
class HttpExchangeService(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @SneakyThrows
    fun request(urlTemplate: String, coordinates: Coordinates): JsonNode {
        val parameterizedUrl = parameterizeUrl(urlTemplate, coordinates.latitude!!, coordinates.longitude!!)
        val request = HttpRequest.newBuilder()
            .uri(URI.create(parameterizedUrl))
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", "uv-index-tg-bot/1.1 (konstantin.matrokhin@gmail.com)")
            .GET()
            .build()
        log.info("GET {}", request.uri())
        val send = httpClient!!.send(request, HttpResponse.BodyHandlers.ofString())
        val body = send.body()
        val jsonNode = objectMapper!!.readTree(body)
        log.info("Got response from {}: {}", request.uri(), jsonNode.toString())
        return jsonNode
    }

    private fun parameterizeUrl(template: String, lat: Double, lon: Double): String {
        return template
            .replace("{lat}", DF.format(lat))
            .replace("{lon}", DF.format(lon))
    }

    companion object {
        private val DF: DecimalFormat

        init {
            val symbols = DecimalFormatSymbols.getInstance(Locale.US)
            symbols.setDecimalSeparator('.')
            DF = DecimalFormat("0.######", symbols)
        }
    }
}

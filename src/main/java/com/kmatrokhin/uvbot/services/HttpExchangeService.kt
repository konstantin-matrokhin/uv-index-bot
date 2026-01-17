package com.kmatrokhin.uvbot.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit

@Service
class HttpExchangeService(
    private val httpClient: HttpClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun request(urlTemplate: String): JsonNode {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(urlTemplate))
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", "uv-index-tg-bot/1.1 (konstantin.matrokhin@gmail.com)")
            .GET()
            .build()

        log.info("GET {}", request.uri())

        val body = try {
            httpClient
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .orTimeout(5, TimeUnit.SECONDS)
                .join()
                .body()
        } catch (e: CompletionException) {
            throw e.cause ?: e
        }

        val jsonNode = objectMapper.readTree(body)
        log.info("Got response from {}", request.uri())
        return jsonNode
    }
}

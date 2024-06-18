package com.kmatrokhin.uvbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kmatrokhin.uvbot.dto.Coordinates;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpExchangeService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public JsonNode request(String urlTemplate, Coordinates coordinates) {
        String parameterizedUrl = parameterizeUrl(urlTemplate, coordinates.getLatitude(), coordinates.getLongitude());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(parameterizedUrl))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();
        log.info("GET {}", request.uri());
        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = send.body();
        JsonNode jsonNode = objectMapper.readTree(body);
        log.info("Got response from {}: {}", request.uri(), jsonNode.toString());
        return jsonNode;
    }

    private String parameterizeUrl(String urlTemplate, Double lat, Double lon) {
        return urlTemplate.formatted(lat, lon);
    }
}

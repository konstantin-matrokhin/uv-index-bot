package com.kmatrokhin.uvbot.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UvIndexService {
    private static final String URL_TEMPLATE = "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=shortwave_radiation,direct_radiation,uv_index,uv_index_clear_sky&forecast_days=1&forecast_hours=1";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public String getUvIndex(Double latitude, Double longitude) {
        log.info("Requesting uv index for latitude {}, longitude {}", latitude, longitude);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(parameterizeUrl(latitude, longitude)))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        log.info("GET {}", request.uri());
        HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
        String body = send.body();
        JsonNode jsonNode = objectMapper.readTree(body);
        log.info("Got response: {}", jsonNode.toString());
        return String.valueOf(jsonNode.at("/hourly/uv_index").iterator().next().asDouble());
    }

    private String parameterizeUrl(Double lat, Double lon) {
        return URL_TEMPLATE.formatted(lat, lon);
    }
}

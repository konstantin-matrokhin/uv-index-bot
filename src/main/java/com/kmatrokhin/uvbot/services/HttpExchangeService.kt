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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpExchangeService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final DecimalFormat DF;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        symbols.setDecimalSeparator('.');
        DF = new DecimalFormat("0.######", symbols);
    }

    @SneakyThrows
    public JsonNode request(String urlTemplate, Coordinates coordinates) {
        String parameterizedUrl = parameterizeUrl(urlTemplate, coordinates.getLatitude(), coordinates.getLongitude());
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(parameterizedUrl))
            .timeout(Duration.ofSeconds(5))
            .header("User-Agent", "uv-index-tg-bot/1.1 (konstantin.matrokhin@gmail.com)")
            .GET()
            .build();
        log.info("GET {}", request.uri());
        HttpResponse<String> send = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = send.body();
        JsonNode jsonNode = objectMapper.readTree(body);
        log.info("Got response from {}: {}", request.uri(), jsonNode.toString());
        return jsonNode;
    }

    private String parameterizeUrl(String template, double lat, double lon) {
        return template
            .replace("{lat}", DF.format(lat))
            .replace("{lon}", DF.format(lon));
    }
}

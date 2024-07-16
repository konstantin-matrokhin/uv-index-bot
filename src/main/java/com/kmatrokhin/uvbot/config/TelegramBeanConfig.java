package com.kmatrokhin.uvbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.net.http.HttpClient;

@Configuration
public class TelegramBeanConfig {
    @Value("${telegram.token}")
    private String telegramToken;

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramToken);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}

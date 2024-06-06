package com.kmatrokhin.uvbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Configuration
public class TelegramBeanConfig {
    @Bean
    public String telegramToken() {
        return System.getenv("TELEGRAM_TOKEN");
    }

    @Bean
    public TelegramClient telegramClient() {
        return new OkHttpTelegramClient(telegramToken());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

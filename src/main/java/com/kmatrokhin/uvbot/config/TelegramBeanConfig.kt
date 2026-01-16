package com.kmatrokhin.uvbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.net.http.HttpClient

@Configuration
class TelegramBeanConfig {
    @Value($$"${telegram.token}")
    private lateinit var telegramToken: String

    @Bean
    fun telegramClient(): TelegramClient {
        return OkHttpTelegramClient(telegramToken)
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun httpClient(): HttpClient? {
        return HttpClient.newHttpClient()
    }
}

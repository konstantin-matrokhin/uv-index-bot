package com.kmatrokhin.uvbot.chatgpt

import com.kmatrokhin.uvbot.dto.I18nProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(I18nProperties::class)
class I18nConfig 

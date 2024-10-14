package com.kmatrokhin.uvbot.config;

import com.kmatrokhin.uvbot.dto.I18nProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(I18nProperties.class)
public class I18nConfigurationProperties {
}

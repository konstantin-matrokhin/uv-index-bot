package com.kmatrokhin.uvbot.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {
    private Map<String, String> dictionary;
}

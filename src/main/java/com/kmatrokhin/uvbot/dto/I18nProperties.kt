package com.kmatrokhin.uvbot.dto;

import com.kmatrokhin.uvbot.entities.UserLanguage;
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
    private Map<String, Map<String, String>> dictionary;

    public String get(UserLanguage userLanguage, String key) {
        return dictionary.get(userLanguage.toString()).get(key);
    }
}

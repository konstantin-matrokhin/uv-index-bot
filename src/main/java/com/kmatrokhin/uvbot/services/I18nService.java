package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.dto.I18nLang;
import com.kmatrokhin.uvbot.dto.I18nProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class I18nService {
    private final I18nProperties i18nProperties;

    public String getText(I18nLang lang, String key) {
        Map<String, String> langTranslations = i18nProperties.getLanguages().get(lang);
        if (langTranslations == null) {
            throw new IllegalArgumentException("Language {" + lang + "} not found");
        }
        String value = langTranslations.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Key {" + key + "} not found");
        }
        return value;
    }

    public boolean inAnyLanguage(String value) {
//        i18nProperties.getLanguages().values().stream()
//            .forEach(lang -> lang.entrySet().stream().forEach((k, v) -> value.));
        return false;
    }
}

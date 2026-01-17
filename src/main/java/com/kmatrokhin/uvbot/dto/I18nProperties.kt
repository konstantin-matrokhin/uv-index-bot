package com.kmatrokhin.uvbot.dto

import com.kmatrokhin.uvbot.entities.UserLanguage
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "i18n")
class I18nProperties {
    var dictionary: Map<String, Map<String, String>> = emptyMap()

    fun get(userLanguage: UserLanguage, key: String): String {
        return dictionary[userLanguage.name]?.get(key) ?: throw IllegalArgumentException("Unknown key '$key'")
    }
}

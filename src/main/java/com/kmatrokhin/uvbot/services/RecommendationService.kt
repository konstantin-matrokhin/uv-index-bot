package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.dto.I18nProperties
import com.kmatrokhin.uvbot.dto.LocationInfo
import com.kmatrokhin.uvbot.dto.Weather.Harm
import com.kmatrokhin.uvbot.entities.UserLanguage
import org.apache.commons.text.StringSubstitutor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val chatGPTService: ChatGPTService,
    private val i18nProperties: I18nProperties
) {
    @Value($$"${openai.enabled:true}")
    private val openaiEnabled = false

    fun createRecommendationText(locationInfo: LocationInfo, userLanguage: UserLanguage): String {
        val weather = locationInfo.weather

        var aiRecommendation = ""
        if (openaiEnabled) {
            val chatResponse = chatGPTService!!.getChatResponse(locationInfo, userLanguage)
            aiRecommendation = chatResponse?.choices?.get(0)?.message?.content ?: ""
        }

        val valueMap: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        valueMap["uvi"] = weather.uvi
        valueMap["uvi_level"] = getHarmText(weather.uvHarm, userLanguage)
        valueMap["temperature"] = weather.temperature
        valueMap["place"] = locationInfo.name
        //        valueMap.put("ai_recommendation", aiRecommendation);

        val stringSubstitutor = StringSubstitutor(valueMap)
        val recommendation = i18nProperties!!.get(userLanguage, "recommendation")
        return stringSubstitutor.replace(recommendation)
    }

    private fun getHarmText(harm: Harm, userLanguage: UserLanguage): String? {
        val key = when (harm) {
            Harm.LOW -> "harm_low"
            Harm.MODERATE -> "harm_moderate"
            Harm.HIGH -> "harm_high"
            Harm.VERY_HIGH -> "harm_very_high"
            Harm.EXTREME -> "harm_extreme"
        }
        return i18nProperties!!.get(userLanguage, key)
    }
}

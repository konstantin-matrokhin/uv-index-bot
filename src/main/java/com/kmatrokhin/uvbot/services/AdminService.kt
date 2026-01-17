package com.kmatrokhin.uvbot.services

import com.kmatrokhin.uvbot.events.StatsRequestedEvent
import com.kmatrokhin.uvbot.events.UserBlockedBotEvent
import com.kmatrokhin.uvbot.events.UserRegisteredEvent
import com.kmatrokhin.uvbot.repositories.UserRepository
import com.kmatrokhin.uvbot.telegram.UvIndexAbility
import io.sentry.Sentry
import jakarta.annotation.PostConstruct
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension

@Service
class AdminService(
    private var uvIndexAbility: UvIndexAbility,
    private var userRepository: UserRepository
) : AbilityExtension {
    @PostConstruct
    fun init() {
        uvIndexAbility.addExtension(this)
    }

    @EventListener
    fun onNewUserRegistered(event: UserRegisteredEvent) {
        try {
            val userEntity = event.userEntity
            val locationEntity = event.locationEntity
            val msg = """
                ✅ New user registered!
                Name: ${userEntity?.name ?: "no name"} (id: ${userEntity?.chatId})
                Location: ${locationEntity!!.name}
                """
            uvIndexAbility.getSilent()?.send(msg, adminChatId())
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    @EventListener
    fun onUserBlocked(event: UserBlockedBotEvent) {
        try {
            val userEntity = event.userEntity
            val locationEntity = event.locationEntity
            val msg = """
                ❌ User blocked the bot!
                Name: ${userEntity?.name ?: "no name"} (id: ${userEntity?.chatId})
                Location: ${locationEntity!!.name}
                """
            uvIndexAbility.getSilent()?.send(msg, adminChatId())
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    @Scheduled(cron = "0 0 7 * * *")
    @EventListener(StatsRequestedEvent::class)
    fun stats() {
        try {
            val subscribedUsers = userRepository.findSubscribedUsers()
            val usernamesList =
                subscribedUsers.mapNotNull { it.name }.joinToString(", ")
            val msg = """
                📅 Daily stats:
                - ${subscribedUsers.size} users subscribed
                - List of users: $usernamesList
                
                """
            uvIndexAbility.getSilent()?.send(msg, adminChatId())
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun adminChatId(): Long {
        return uvIndexAbility.creatorId()
    }
}

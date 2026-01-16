package com.kmatrokhin.uvbot.repositories

import com.kmatrokhin.uvbot.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<UserEntity?, UUID?> {
    fun findByChatId(chatId: Long?): Optional<UserEntity>

    fun getByChatId(chatId: Long?): UserEntity {
        return findByChatId(chatId).orElseThrow()
    }

    @Query("from UserEntity u where u.isSubscribed = true")
    fun findSubscribedUsers(): MutableList<UserEntity?>?
}

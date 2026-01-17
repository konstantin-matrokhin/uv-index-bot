package com.kmatrokhin.uvbot.entities

import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "user", schema = "public")
class UserEntity (
    @Id
    var id: UUID = UUID.randomUUID(),
    var chatId: Long,
    var name: String? = null,
    var isSubscribed: Boolean,
    var createdAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "lang")
    var language: UserLanguage = UserLanguage.ENGLISH
)

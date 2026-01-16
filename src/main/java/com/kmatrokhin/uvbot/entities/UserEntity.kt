package com.kmatrokhin.uvbot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user", schema = "public")
@Getter
@Setter
public class UserEntity {
    @Id
    private UUID id = UUID.randomUUID();
    private Long chatId;
    private String name;
    private Boolean isSubscribed;
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "lang")
    private UserLanguage language = UserLanguage.ENGLISH;
}

package com.kmatrokhin.uvbot.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private Instant createdAt = Instant.now();
}

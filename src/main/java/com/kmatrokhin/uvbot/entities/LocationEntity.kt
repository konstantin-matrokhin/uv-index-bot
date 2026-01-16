package com.kmatrokhin.uvbot.entities;

import com.kmatrokhin.uvbot.dto.Coordinates;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "location")
@Getter
@Setter
public class LocationEntity {
    @Id
    private UUID id = UUID.randomUUID();
    private Double latitude;
    private Double longitude;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;
    private String name;
    private Float lastUvIndex;
    private Instant createdAt = Instant.now();

    public Coordinates coordinates() {
        return Coordinates.of(latitude, longitude);
    }
}

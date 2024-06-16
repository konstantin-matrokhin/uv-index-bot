package com.kmatrokhin.uvbot.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "location")
@Getter
@Setter
public class LocationEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    private String name;

    private Float latitude;

    private Float longitude;
}

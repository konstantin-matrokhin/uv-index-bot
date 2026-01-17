package com.kmatrokhin.uvbot.entities

import com.kmatrokhin.uvbot.dto.Coordinates
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "location")
class LocationEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    var latitude: Double,
    var longitude: Double,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var userEntity: UserEntity,
    var name: String? = null,
    var lastUvIndex: Float? = null,
    var createdAt: Instant = Instant.now()
) {
    fun coordinates(): Coordinates {
        return Coordinates(latitude, longitude)
    }
}

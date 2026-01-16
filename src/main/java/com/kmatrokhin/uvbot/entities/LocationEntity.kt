package com.kmatrokhin.uvbot.entities

import com.kmatrokhin.uvbot.dto.Coordinates
import com.kmatrokhin.uvbot.dto.Coordinates.Companion.of
import jakarta.persistence.*
import java.time.Instant
import java.util.*

@Entity
@Table(name = "location")
class LocationEntity(
    @Id
    var id: UUID = UUID.randomUUID(),
    var latitude: Double? = null,
    var longitude: Double? = null,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var userEntity: UserEntity? = null,
    var name: String? = null,
    var lastUvIndex: Float? = null,
    var createdAt: Instant? = Instant.now()
) {
    fun coordinates(): Coordinates {
        return of(latitude, longitude)
    }
}

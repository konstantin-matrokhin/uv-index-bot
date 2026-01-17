package com.kmatrokhin.uvbot.repositories

import com.kmatrokhin.uvbot.entities.LocationEntity
import com.kmatrokhin.uvbot.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LocationRepository : JpaRepository<LocationEntity, UUID> {
    fun findByUserEntity(userEntity: UserEntity): LocationEntity?

    fun getByUserEntity(userEntity: UserEntity): LocationEntity {
        return findByUserEntity(userEntity) ?: throw IllegalArgumentException("User not found")
    }
}

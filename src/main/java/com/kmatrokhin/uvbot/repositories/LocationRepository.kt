package com.kmatrokhin.uvbot.repositories;

import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {
    Optional<LocationEntity> findByUserEntity(UserEntity userEntity);

    default LocationEntity getByUserEntity(UserEntity userEntity) {
        return findByUserEntity(userEntity).orElseThrow();
    }
}

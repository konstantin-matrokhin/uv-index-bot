package com.kmatrokhin.uvbot.repositories;

import com.kmatrokhin.uvbot.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByChatId(Long chatId);

    default UserEntity getByChatId(Long chatId) {
        return findByChatId(chatId).orElseThrow();
    }
}

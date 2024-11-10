package com.kmatrokhin.uvbot.services;

import com.kmatrokhin.uvbot.entities.LocationEntity;
import com.kmatrokhin.uvbot.entities.UserEntity;
import com.kmatrokhin.uvbot.events.StatsRequestedEvent;
import com.kmatrokhin.uvbot.events.UserBlockedBotEvent;
import com.kmatrokhin.uvbot.events.UserRegisteredEvent;
import com.kmatrokhin.uvbot.repositories.UserRepository;
import com.kmatrokhin.uvbot.telegram.UvIndexAbility;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.abilitybots.api.util.AbilityExtension;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService implements AbilityExtension {
    private final UvIndexAbility uvIndexAbility;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        uvIndexAbility.addExtension(this);
    }

//    public Ability statsCommand() {
//        return Ability.builder()
//            .name("stats")
//            .locality(Locality.USER)
//            .privacy(Privacy.CREATOR)
//            .action(context -> {
//                stats();
//            })
//            .build();
//    }


    @EventListener
    public void onNewUserRegistered(UserRegisteredEvent event) {
        UserEntity userEntity = event.getUserEntity();
        LocationEntity locationEntity = event.getLocationEntity();
        String msg = """
            ✅ New user registered!
            Name: %s (id: %s)
            Location: %s
            """.formatted(userEntity.getName(), userEntity.getChatId(), locationEntity.getName());
        uvIndexAbility.getSilent().send(msg, adminChatId());
    }

    public void onUserBlocked(UserBlockedBotEvent event) {
        UserEntity userEntity = event.getUserEntity();
        LocationEntity locationEntity = event.getLocationEntity();
        String msg = """
            ❌ User blocked the bot!
            Name: %s (id: %s)
            Location: %s
            """.formatted(userEntity.getName(), userEntity.getChatId(), locationEntity.getName());
        uvIndexAbility.getSilent().send(msg, adminChatId());
    }

    @Scheduled(cron = "0 0 7 * * *")
    @EventListener(StatsRequestedEvent.class)
//    @Scheduled(fixedDelay = 100_000, initialDelay = 0)
    public void stats() {
        List<UserEntity> subscribedUsers = userRepository.findSubscribedUsers();
        String usernamesList = subscribedUsers.stream().map(UserEntity::getName).collect(Collectors.joining(", "));
        String msg = """
            📅 Daily stats:
            - %s users subscribed
            - List of users: %s
            """.formatted(subscribedUsers.size(), usernamesList);
        uvIndexAbility.getSilent().send(msg, adminChatId());
    }

    private Long adminChatId() {
        return uvIndexAbility.creatorId();
    }

}

package com.example.instaclone.repository;

import com.example.instaclone.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findChatByName(String name);
    List<Chat> findByParticipantContaining(String participant);
}

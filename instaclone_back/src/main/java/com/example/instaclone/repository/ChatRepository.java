package com.example.instaclone.repository;

import com.example.instaclone.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findChatByName(String name);
    List<Chat> findByParticipantContaining(String participant);
}

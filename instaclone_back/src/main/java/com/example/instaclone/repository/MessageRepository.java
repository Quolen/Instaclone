package com.example.instaclone.repository;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChat(Chat chat);
}
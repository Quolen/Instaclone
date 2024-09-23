package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.repository.ChatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat save(Chat chat) {
        log.info("Saving chat: {}", chat);
        return chatRepository.save(chat);
    }

    public Chat findChatByName(String name) {
        log.info("Finding chat by name: {}", name);
        return chatRepository.findChatByName(name);
    }

    public List<Chat> findByParticipant(String participant) {
        log.info("Finding chats by participant: {}", participant);
        return chatRepository.findByParticipantContaining(participant);
    }
}

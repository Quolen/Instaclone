package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.repository.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Chat save(Chat chat) {
        logger.info("Saving chat: {}", chat);
        return chatRepository.save(chat);
    }

    public Chat findChatByName(String name) {
        logger.info("Finding chat by name: {}", name);
        return chatRepository.findChatByName(name);
    }

    public List<Chat> findByParticipant(String participant) {
        logger.info("Finding chats by participant: {}", participant);
        return chatRepository.findByParticipantContaining(participant);
    }
}

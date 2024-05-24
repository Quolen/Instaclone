package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message save(Message message) {
        logger.info("Saving message: {}", message);
        return messageRepository.save(message);
    }

    public List<Message> findAllByChat(Chat chat) {
        logger.info("Finding all messages for chat: {}", chat);
        return messageRepository.findAllByChat(chat);
    }
}

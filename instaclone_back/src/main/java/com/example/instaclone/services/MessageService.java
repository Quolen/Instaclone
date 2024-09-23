package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.repository.MessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message save(Message message) {
        log.info("Saving message: {}", message);
        return messageRepository.save(message);
    }

    public List<Message> findAllByChat(Chat chat) {
        log.info("Finding all messages for chat: {}", chat);
        return messageRepository.findAllByChat(chat);
    }
}

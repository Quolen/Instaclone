package com.example.instaclone.web;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.example.instaclone.dto.ChatNameDTO;
import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.services.ChatService;
import com.example.instaclone.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;
    private final MessageService messageService;

    @MessageMapping("/chat/{to}")
    public void sendMessage(@DestinationVariable String to, Message message) {
        log.info("Handling send message: sender={}, content={}, to={}", message.getSender(), message.getContent(), to);
        Chat chat = createAndOrGetChat(to);
        message.setChat(chat);
        message.setT_stamp(generateTimeStamp());
        message = messageService.save(message);
        log.info("Message saved with id: {}", message.getMs_id());

        simpMessagingTemplate.convertAndSend("/topic/messages/" + to, message);
        log.info("Message sent to topic /topic/messages/{}", to);
    }

    @PostMapping("/getChats")
    public List<Chat> getChats(@RequestBody String user) {
        log.info("Getting chats for user: {}", user);
        return chatService.findByParticipant(user);
    }

    @PostMapping("/getMessages")
    public List<Message> getMessages(@RequestBody ChatNameDTO chatNameDTO) {
        String chatName = chatNameDTO.getChat();
        log.info("Getting messages for chat: {}", chatName);
        Chat chat = chatService.findChatByName(chatName);
        if (chat != null) {
            List<Message> messages = messageService.findAllByChat(chat);
            log.info("Found {} messages for chat: {}", messages.size(), chatName);
            return messages;
        } else {
            log.info("Found no chat with name: {}", chatName);
            return new ArrayList<>();
        }
    }

    public Chat createAndOrGetChat(String name) {
        log.info("Creating or getting chat with name: {}", name);
        Chat chat = chatService.findChatByName(name);
        if (chat == null) {
            chat = new Chat(name);
            chat = chatService.save(chat);
            log.info("Created new chat: {}", chat);
        }
        return chat;
    }

    private String generateTimeStamp() {
        Instant i = Instant.now();
        String date = i.toString();
        log.info("Source: {}", i);
        int endRange = date.indexOf('T');
        date = date.substring(0, endRange);
        date = date.replace('-', '/');
        log.info("Date extracted: {}", date);
        String time = Integer.toString(i.atZone(ZoneOffset.UTC).getHour() + 2);
        time += ":";

        int minutes = i.atZone(ZoneOffset.UTC).getMinute();
        if (minutes > 9) {
            time += Integer.toString(minutes);
        } else {
            time += "0" + minutes;
        }

        log.info("Time extracted: {}", time);
        return date + "-" + time;
    }
}

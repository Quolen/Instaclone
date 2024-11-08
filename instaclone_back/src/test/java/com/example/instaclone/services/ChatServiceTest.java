package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = new Chat();
        chat.setName("TestChat");
        chat.setParticipant("TestUser");
    }

    @Test
    void save_shouldReturnSavedChat() {
        when(chatRepository.save(chat)).thenReturn(chat);

        Chat savedChat = chatService.save(chat);

        assertNotNull(savedChat);
        assertEquals("TestChat", savedChat.getName());
        verify(chatRepository).save(chat);
    }

    @Test
    void findChatByName_shouldReturnChatWhenFound() {
        String chatName = "TestChat";
        when(chatRepository.findChatByName(chatName)).thenReturn(chat);

        Chat foundChat = chatService.findChatByName(chatName);

        assertNotNull(foundChat);
        assertEquals("TestChat", foundChat.getName());
        verify(chatRepository).findChatByName(chatName);
    }

    @Test
    void findByParticipant_shouldReturnChatsWhenParticipantFound() {
        String participant = "TestUser";
        List<Chat> chats = List.of(chat);
        when(chatRepository.findByParticipantContaining(participant)).thenReturn(chats);

        List<Chat> foundChats = chatService.findByParticipant(participant);

        assertNotNull(foundChats);
        assertEquals(1, foundChats.size());
        assertEquals("TestUser", foundChats.get(0).getParticipant());
        verify(chatRepository).findByParticipantContaining(participant);
    }
}
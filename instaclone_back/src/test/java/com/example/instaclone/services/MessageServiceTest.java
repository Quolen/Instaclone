package com.example.instaclone.services;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import com.example.instaclone.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private MessageRepository messageRepository;

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Captor
    private ArgumentCaptor<Chat> chatArgumentCaptor;

    @Test
    void save_shouldSaveMessage() {
        // Given
        Message message = new Message();
        message.setContent("Hello World");

        when(messageRepository.save(message)).thenReturn(message);

        // When
        Message result = messageService.save(message);

        // Then
        verify(messageRepository).save(messageArgumentCaptor.capture());
        Message capturedMessage = messageArgumentCaptor.getValue();

        assertThat(capturedMessage.getContent()).isEqualTo("Hello World");
        assertThat(result).isEqualTo(message);
    }

    @Test
    void findAllByChat_shouldReturnMessagesForChat() {
        // Given
        Chat chat = new Chat();
        Message message1 = new Message();
        message1.setContent("Message 1");
        Message message2 = new Message();
        message2.setContent("Message 2");

        List<Message> messages = List.of(message1, message2);
        when(messageRepository.findAllByChat(chat)).thenReturn(messages);

        // When
        List<Message> result = messageService.findAllByChat(chat);

        // Then
        verify(messageRepository).findAllByChat(chatArgumentCaptor.capture());
        Chat capturedChat = chatArgumentCaptor.getValue();

        assertThat(capturedChat).isEqualTo(chat);
        assertThat(result).hasSize(2).containsExactly(message1, message2);
    }
}
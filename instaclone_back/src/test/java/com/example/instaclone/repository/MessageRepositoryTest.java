package com.example.instaclone.repository;

import com.example.instaclone.entity.Chat;
import com.example.instaclone.entity.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:latest");

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    private Chat chat;
    private Message message1, message2;

    @BeforeEach
    void setUp() {
        chat = new Chat("Chat Room 1");
        chatRepository.save(chat);

        message1 = new Message("User1", "2024-10-11T10:00:00", "Hello!", chat);
        message2 = new Message("User2", "2024-10-11T11:00:00", "Hi!", chat);

        messageRepository.save(message1);
        messageRepository.save(message2);
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        chatRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mySQLContainer.isCreated()).isTrue();
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnMessageWhenFindAllByChat() {
        List<Message> messages = messageRepository.findAllByChat(chat);
        assertThat(messages.size()).isEqualTo(2);
        assertThat(messages).contains(message1, message2);

    }
}
package com.example.instaclone.repository;

import com.example.instaclone.entity.Chat;
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
class ChatRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer
            = new MySQLContainer<>("mysql:latest");

    @Autowired
    ChatRepository chatRepository;

    @BeforeEach
    void setUp() {
        Chat chat = new Chat();
        chat.setName("Test Chat");
        chat.setParticipant("John");
        chatRepository.save(chat);
    }

    @AfterEach
    void tearDown() {
        chatRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mysqlContainer.isCreated()).isTrue();
        assertThat(mysqlContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnChatWhenFindChatByName() {
        Chat chat = chatRepository.findChatByName("Test Chat");
        assertThat(chat).isNotNull();
        assertThat(chat.getName()).isEqualTo("Test Chat");
    }

    @Test
    void shouldNotReturnChatWhenFindChatByName() {
        Chat chat = chatRepository.findChatByName("Wrong Name");
        assertThat(chat).isNull();
    }

    @Test
    void shouldFindByParticipantContaining() {
        List<Chat> chats = chatRepository.findByParticipantContaining("John");
        assertThat(chats.size()).isEqualTo(1);
        assertThat(chats.get(0).getParticipant()).isEqualTo("John");
    }

    @Test
    void shouldNotFindByParticipantContaining() {
        List<Chat> chats = chatRepository.findByParticipantContaining("Wrong Name");
        assertThat(chats.size()).isEqualTo(0);
    }
}
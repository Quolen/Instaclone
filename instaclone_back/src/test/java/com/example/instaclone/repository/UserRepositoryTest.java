package com.example.instaclone.repository;

import com.example.instaclone.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer
            = new MySQLContainer<>("mysql:latest");

    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John");
        user.setLastname("Doe");
        user.setUsername("johndoe123");
        user.setEmail("johndoe@example.com");
        user.setPassword("password123");
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mySQLContainer.isCreated()).isTrue();
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnUserWhenFindUserByUsername() {
        Optional<User> foundUser = userRepository.findUserByUsername("johndoe123");

        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get().getUsername()).isEqualTo("johndoe123");
    }

    @Test
    void shouldReturnUserWhenFindUserByEmail() {
        Optional<User> foundUser = userRepository.findUserByEmail("johndoe@example.com");

        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get().getEmail()).isEqualTo("johndoe@example.com");
    }

    @Test
    void shouldReturnUserWhenFindUserById() {
        Optional<User> foundUser = userRepository.findUserById(user.getId());

        assertThat(foundUser.isPresent()).isTrue();
        assertThat(foundUser.get().getName()).isEqualTo("John");
    }
}
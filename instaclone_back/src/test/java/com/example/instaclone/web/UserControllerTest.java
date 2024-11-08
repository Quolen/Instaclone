package com.example.instaclone.web;

import com.example.instaclone.dto.UserDTO;
import com.example.instaclone.entity.User;
import com.example.instaclone.repository.UserRepository;
import com.example.instaclone.security.JWTTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class UserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private String jwtToken;

    private static final String USER_PATH = "/api/user";

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer =
            new MySQLContainer<>("mysql:latest");


    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("expectedUsername");
        user.setName("Test");
        user.setLastname("User");
        user.setPassword("password");
        userRepository.save(user);

        jwtToken = jwtTokenProvider.generateToken(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void canGetCurrentUser() {
        //When
        UserDTO userDTO = webTestClient.get()
                .uri(USER_PATH + "/")
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult().getResponseBody();

        //Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getUsername()).isEqualTo("expectedUsername");
        assertThat(userDTO.getFirstname()).isEqualTo("Test");
        assertThat(userDTO.getLastname()).isEqualTo("User");
    }

    @Test
    void canGetUserProfile() {
        //When
        UserDTO userDTO = webTestClient.get()
                .uri(USER_PATH + "/" + user.getId())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult().getResponseBody();

        //Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getUsername()).isEqualTo("expectedUsername");
        assertThat(userDTO.getFirstname()).isEqualTo("Test");
        assertThat(userDTO.getLastname()).isEqualTo("User");
    }

    @Test
    void getUserByUsername() {
        //When
        UserDTO userDTO = webTestClient.get()
                .uri(USER_PATH + "/username/" + user.getUsername())
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult().getResponseBody();

        //Then
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getUsername()).isEqualTo("expectedUsername");
        assertThat(userDTO.getFirstname()).isEqualTo("Test");
        assertThat(userDTO.getLastname()).isEqualTo("User");
    }

    @Test
    void updateUser() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername(user.getUsername());
        updatedUserDTO.setFirstname("updatedFirstName");
        updatedUserDTO.setLastname("updatedLastName");
        updatedUserDTO.setBio("updatedBio");

        UserDTO userDTO = webTestClient.patch()
                .uri(USER_PATH + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .body(Mono.just(updatedUserDTO), UserDTO.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDTO.class)
                .returnResult().getResponseBody();

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getFirstname()).isEqualTo("updatedFirstName");
        assertThat(userDTO.getLastname()).isEqualTo("updatedLastName");
        assertThat(userDTO.getBio()).isEqualTo("updatedBio");
        assertThat(userDTO.getUsername()).isEqualTo("expectedUsername");
    }

    @Test
    void updateUser_shouldFailForInvalidFields() {
        UserDTO invalidUserDTO = new UserDTO();
        invalidUserDTO.setUsername("expectedUsername");
        invalidUserDTO.setFirstname("");
        invalidUserDTO.setLastname("UpdatedLastName");

        webTestClient.patch()
                .uri(USER_PATH + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .body(Mono.just(invalidUserDTO), UserDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.firstname").isEqualTo("must not be empty");
    }

    @Test
    void updateUser_shouldReturnValidationErrors_whenRequiredFieldsAreMissing() {
        UserDTO invalidUserDTO = new UserDTO();
        invalidUserDTO.setLastname("UpdatedLastName");

        webTestClient.patch()
                .uri(USER_PATH + "/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .body(Mono.just(invalidUserDTO), UserDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.username").isEqualTo("must not be empty")
                .jsonPath("$.firstname").isEqualTo("must not be empty");
    }
}

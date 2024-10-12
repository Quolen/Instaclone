package com.example.instaclone.repository;

import com.example.instaclone.entity.Post;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer
            = new MySQLContainer<>("mysql:latest");

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    User user;
    Post post1, post2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("user");
        user.setLastname("userLastname");
        user.setUsername("user123");
        user.setPassword("password");
        userRepository.save(user);

        post1 = new Post();
        post1.setTitle("Post 1");
        post1.setCaption("Caption 1");
        post1.setLocation("Location 1");
        post1.setUser(user);
        postRepository.save(post1);

        post2 = new Post();
        post2.setTitle("Post 2");
        post2.setCaption("Caption 2");
        post2.setLocation("Location 2");
        post2.setUser(user);
        postRepository.save(post2);
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mySQLContainer.isCreated()).isTrue();
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnPostWhenFindAllByUserOrderByCreatedAtDesc() {
        List<Post> posts = postRepository.findAllByUserOrderByCreatedAtDesc(user);
        assertThat(posts.size()).isEqualTo(2);
        assertThat(posts.get(0).getTitle()).isEqualTo("Post 2");
        assertThat(posts.get(1).getTitle()).isEqualTo("Post 1");
    }

    @Test
    void shouldReturnPostWhenFindAllByOrderByCreatedAtDesc() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();

        assertThat(posts.size()).isEqualTo(2);
        assertThat(posts.get(0).getTitle()).isEqualTo(post2.getTitle());
    }

    @Test
    void shouldReturnPostWhenFindPostByIdAndUser() {
        Optional<Post> post = postRepository.findPostByIdAndUser(post1.getId(), user);

        assertThat(post.isPresent()).isTrue();
        assertThat(post.get().getTitle()).isEqualTo("Post 1");
    }
}
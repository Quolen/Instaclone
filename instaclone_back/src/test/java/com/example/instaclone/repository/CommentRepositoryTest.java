package com.example.instaclone.repository;

import com.example.instaclone.entity.Comment;
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
class CommentRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer
            = new MySQLContainer<>("mysql:latest");

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    PostRepository postRepository;
    @Autowired
    UserRepository userRepository;

    User testUser;
    Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("testName");
        testUser.setLastname("testLastName");
        testUser.setUsername("testUser");
        testUser.setPassword("password");
        userRepository.save(testUser);

        testPost = new Post();
        testPost.setTitle("Test Post");
        testPost.setCaption("Test Caption");
        testPost.setUser(testUser);
        postRepository.save(testPost);

        Comment testComment1 = new Comment();
        testComment1.setMessage("Test Comment1");
        testComment1.setPost(testPost);
        testComment1.setUsername(testUser.getUsername());
        testComment1.setUserId(testUser.getId());
        commentRepository.save(testComment1);

        Comment testComment2 = new Comment();
        testComment2.setMessage("Test Comment2");
        testComment2.setPost(testPost);
        testComment2.setUsername(testUser.getUsername());
        testComment2.setUserId(testUser.getId());
        commentRepository.save(testComment2);

    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mySQLContainer.isCreated()).isTrue();
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnCommentListWhenFindAllByPost() {
        List<Comment> comments = commentRepository.findAllByPost(testPost);
        assertThat(comments.size()).isEqualTo(2);
        assertThat(comments.get(0).getMessage()).isEqualTo("Test Comment1");
        assertThat(comments.get(1).getMessage()).isEqualTo("Test Comment2");
    }

    @Test
    void shouldReturnListCommentDescDateWhenFindAllByPostOrderByCreatedAtDesc() {
        List<Comment> comments = commentRepository.findAllByPostOrderByCreatedAtDesc(testPost);
        assertThat(comments.size()).isEqualTo(2);
        assertThat(comments.get(0).getMessage()).isEqualTo("Test Comment2");
        assertThat(comments.get(1).getMessage()).isEqualTo("Test Comment1");
    }

    @Test
    void shouldReturnCommentWhenFindByIdAndUserId() {
        Comment comment = commentRepository.findAll().get(0);

        Optional<Comment> optionalComment = commentRepository.findByIdAndUserId(comment.getId(), testUser.getId());
        assertThat(optionalComment.isPresent()).isTrue();
        assertThat(optionalComment.get().getId()).isEqualTo(comment.getId());
        assertThat(optionalComment.get().getUsername()).isEqualTo(comment.getUsername());
        assertThat(optionalComment.get().getPost()).isEqualTo(comment.getPost());
        assertThat(optionalComment.get().getCreatedAt()).isEqualTo(comment.getCreatedAt());
        assertThat(optionalComment.get().getMessage()).isEqualTo(comment.getMessage());
    }
}
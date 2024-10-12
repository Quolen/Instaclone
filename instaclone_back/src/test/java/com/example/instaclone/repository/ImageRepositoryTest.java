package com.example.instaclone.repository;

import com.example.instaclone.entity.ImageModel;
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

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ImageRepositoryTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer
            = new MySQLContainer<>("mysql:latest");

    @Autowired
    ImageRepository imageRepository;

    @BeforeEach
    void setUp() {
        byte[] sampleImage = "sample image data".getBytes(StandardCharsets.UTF_8);

        ImageModel imageModel1 = new ImageModel();
        imageModel1.setImageBytes(sampleImage);
        imageModel1.setUserId(1L);
        imageModel1.setPostId(1L);
        imageModel1.setName("Image1");
        imageRepository.save(imageModel1);

        ImageModel imageModel2 = new ImageModel();
        imageModel2.setImageBytes(sampleImage);
        imageModel2.setUserId(2L);
        imageModel2.setPostId(2L);
        imageModel2.setName("Image2");
        imageRepository.save(imageModel2);
    }

    @AfterEach
    void tearDown() {
        imageRepository.deleteAll();
    }

    @Test
    void canEstablishConnection() {
        assertThat(mySQLContainer.isCreated()).isTrue();
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    void shouldReturnImageWhenFindByUserId() {
        Optional<ImageModel> imageModel = imageRepository.findByUserId(1L);

        assertThat(imageModel.isPresent()).isTrue();
        assertThat(imageModel.get().getName()).isEqualTo("Image1");
        assertThat(imageModel.get().getUserId()).isEqualTo(1L);
        assertThat(imageModel.get().getImageBytes()).isEqualTo("sample image data".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldReturnImageWhenFindByPostId() {
        Optional<ImageModel> imageModel = imageRepository.findByPostId(2L);

        assertThat(imageModel.isPresent()).isTrue();
        assertThat(imageModel.get().getName()).isEqualTo("Image2");
        assertThat(imageModel.get().getPostId()).isEqualTo(2L);

    }

    @Test
    void shouldReturnImageWhenFindByUserIdAndPostId() {
        Optional<ImageModel> imageModel = imageRepository.findByUserIdAndPostId(1L, 1L);

        assertThat(imageModel.isPresent()).isTrue();
        assertThat(imageModel.get().getName()).isEqualTo("Image1");
        assertThat(imageModel.get().getPostId()).isEqualTo(1L);
    }
}
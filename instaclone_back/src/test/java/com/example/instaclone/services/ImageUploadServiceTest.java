package com.example.instaclone.services;

import com.example.instaclone.entity.ImageModel;
import com.example.instaclone.entity.Post;
import com.example.instaclone.entity.User;
import com.example.instaclone.repository.ImageRepository;
import com.example.instaclone.repository.UserRepository;
import com.example.instaclone.s3.S3Buckets;
import com.example.instaclone.s3.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private S3Buckets s3Buckets;

    @InjectMocks
    private ImageUploadService imageUploadService;

    @Test
    void uploadImageToUser_shouldUploadAndSaveImage() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        User user = new User();
        user.setId(1L);
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));
        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");

        // When
        imageUploadService.uploadImageToUser(file, principal);

        // Then
        ArgumentCaptor<String> s3KeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ImageModel> imageCaptor = ArgumentCaptor.forClass(ImageModel.class);

        verify(s3Service).putObject(eq("bucket-name"), s3KeyCaptor.capture(), eq(file.getBytes()));
        verify(imageRepository).save(imageCaptor.capture());

        assertTrue(s3KeyCaptor.getValue().contains("profile-images/1/test.jpg"));
        assertEquals("test.jpg", imageCaptor.getValue().getName());
        assertEquals("profile-images/1/test.jpg", imageCaptor.getValue().getS3Key());
    }

    @Test
    void deleteProfileImage_shouldDeleteExistingImage() {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        User user = new User();
        user.setId(1L);
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));

        ImageModel existingImage = new ImageModel();
        existingImage.setS3Key("profile-images/1/test.jpg");
        when(imageRepository.findByUserIdAndPostId(user.getId(), null)).thenReturn(Optional.of(existingImage));

        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");

        // When
        imageUploadService.deleteProfileImage(principal);

        // Then
        verify(s3Service).deleteObject("bucket-name", "profile-images/1/test.jpg");
        verify(imageRepository).delete(existingImage);
    }

    @Test
    void uploadImageToPost_shouldUploadAndSaveImageForPost() throws IOException {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test-post.jpg", "image/jpeg", "image content".getBytes());
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        User user = new User();
        user.setId(1L);
        Post post = new Post();
        post.setId(2L);
        user.setPosts(List.of(post));
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));
        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");

        // When
        imageUploadService.uploadImageToPost(file, principal, post.getId());

        // Then
        ArgumentCaptor<String> s3KeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ImageModel> imageCaptor = ArgumentCaptor.forClass(ImageModel.class);

        verify(s3Service).putObject(eq("bucket-name"), s3KeyCaptor.capture(), eq(file.getBytes()));
        verify(imageRepository).save(imageCaptor.capture());

        assertTrue(s3KeyCaptor.getValue().contains("post-images/2/test-post.jpg"));
        assertEquals("test-post.jpg", imageCaptor.getValue().getName());
        assertEquals("post-images/2/test-post.jpg", imageCaptor.getValue().getS3Key());
        assertEquals(post.getId(), imageCaptor.getValue().getPostId());
    }

    @Test
    void getImageToUser_shouldRetrieveAndReturnImage() {
        // Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        User user = new User();
        user.setId(1L);
        ImageModel image = new ImageModel();
        image.setS3Key("profile-images/1/test.jpg");
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));
        when(imageRepository.findByUserIdAndPostId(user.getId(), null)).thenReturn(Optional.of(image));

        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");
        byte[] imageBytes = "image content".getBytes();
        when(s3Service.getObject("bucket-name", "profile-images/1/test.jpg")).thenReturn(imageBytes);

        // When
        ImageModel result = imageUploadService.getImageToUser(principal);

        // Then
        assertEquals(imageBytes, result.getImageBytes());
        assertEquals("profile-images/1/test.jpg", result.getS3Key());
    }

    @Test
    void getProfileImageToPost_shouldRetrieveImageBytesFromS3() {
        // Given
        Long userId = 1L;
        ImageModel image = new ImageModel();
        image.setUserId(userId);
        image.setS3Key("profile-images/1/test.jpg");

        when(imageRepository.findByUserIdAndPostId(userId, null)).thenReturn(Optional.of(image));
        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");
        byte[] imageBytes = "test image bytes".getBytes();
        when(s3Service.getObject("bucket-name", "profile-images/1/test.jpg")).thenReturn(imageBytes);

        // When
        ImageModel result = imageUploadService.getProfileImageToPost(userId);

        // Then
        verify(s3Service).getObject("bucket-name", "profile-images/1/test.jpg");
        assertEquals(imageBytes, result.getImageBytes());
    }

    @Test
    void getImageToPost_shouldRetrieveImageBytesFromS3() {
        // Given
        Long postId = 1L;
        ImageModel image = new ImageModel();
        image.setPostId(postId);
        image.setS3Key("post-images/1/test.jpg");

        when(imageRepository.findByPostId(postId)).thenReturn(Optional.of(image));
        when(s3Buckets.getImgBucket()).thenReturn("bucket-name");
        byte[] imageBytes = "post image bytes".getBytes();
        when(s3Service.getObject("bucket-name", "post-images/1/test.jpg")).thenReturn(imageBytes);

        // When
        ImageModel result = imageUploadService.getImageToPost(postId);

        // Then
        verify(s3Service).getObject("bucket-name", "post-images/1/test.jpg");
        assertEquals(imageBytes, result.getImageBytes());
    }
}

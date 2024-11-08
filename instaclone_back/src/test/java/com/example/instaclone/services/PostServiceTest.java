package com.example.instaclone.services;

import com.example.instaclone.dto.PostDTO;
import com.example.instaclone.entity.ImageModel;
import com.example.instaclone.entity.Post;
import com.example.instaclone.entity.User;
import com.example.instaclone.exceptions.PostNotFoundException;
import com.example.instaclone.repository.ImageRepository;
import com.example.instaclone.repository.PostRepository;
import com.example.instaclone.repository.UserRepository;
import com.example.instaclone.s3.S3Buckets;
import com.example.instaclone.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private S3Buckets s3Buckets;

    @Captor
    private ArgumentCaptor<Post> postArgumentCaptor;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        post = new Post();
        post.setId(1L);
        post.setUser(user);
        post.setCaption("Test Caption");
        post.setLocation("Test Location");
        post.setTitle("Test Title");
        post.setLikes(0);
    }

    @Test
    void createPost_shouldCreateNewPost() {
        // Given
        PostDTO postDTO = new PostDTO(null, "Test Title", "Test Caption", "Test Location", "john_doe", null, 0, new HashSet<>());
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // When
        Post result = postService.createPost(postDTO, principal);

        // Then
        verify(postRepository).save(postArgumentCaptor.capture());
        Post capturedPost = postArgumentCaptor.getValue();

        assertThat(capturedPost.getTitle()).isEqualTo(postDTO.getTitle());
        assertThat(capturedPost.getCaption()).isEqualTo(postDTO.getCaption());
        assertThat(capturedPost.getLocation()).isEqualTo(postDTO.getLocation());
        assertThat(capturedPost.getUser()).isEqualTo(user);
        assertThat(result).isEqualTo(post);
    }

    @Test
    void getAllPosts_shouldReturnAllPosts() {
        // Given
        List<Post> posts = List.of(post);
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(posts);

        // When
        List<Post> result = postService.getAllPosts();

        // Then
        verify(postRepository).findAllByOrderByCreatedAtDesc();
        assertThat(result).hasSize(1).contains(post);
    }

    @Test
    void getPostById_shouldReturnPostForUser() {
        // Given
        Long postId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(postRepository.findPostByIdAndUser(postId, user)).thenReturn(Optional.of(post));

        // When
        Post result = postService.getPostById(postId, principal);

        // Then
        verify(postRepository).findPostByIdAndUser(postId, user);
        assertThat(result).isEqualTo(post);
    }

    @Test
    void getPostById_shouldThrowExceptionIfPostNotFound() {
        // Given
        Long postId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(postRepository.findPostByIdAndUser(postId, user)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.getPostById(postId, principal))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("Post cannot be found for username: " + user.getEmail());
    }

    @Test
    void getAllPostsForUser_shouldReturnPostsForUser() {
        // Given
        List<Post> posts = List.of(post);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(postRepository.findAllByUserOrderByCreatedAtDesc(user)).thenReturn(posts);

        // When
        List<Post> result = postService.getAllPostForUser(principal);

        // Then
        verify(postRepository).findAllByUserOrderByCreatedAtDesc(user);
        assertThat(result).hasSize(1).contains(post);
    }

    @Test
    void likePost_shouldAddLikeIfNotLiked() {
        // Given
        String username = "john_doe";
        Long postId = 1L;
        post.setLikedUsers(new HashSet<>());
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // When
        Post result = postService.likePost(postId, username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLikes()).isEqualTo(1);
        assertThat(result.getLikedUsers()).contains(username);
    }

    @Test
    void likePost_shouldRemoveLikeIfAlreadyLiked() {
        // Given
        String username = "john_doe";
        Long postId = 1L;
        post.setLikedUsers(new HashSet<>(Set.of(username)));
        post.setLikes(1);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // When
        Post result = postService.likePost(postId, username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLikes()).isEqualTo(0);
        assertThat(result.getLikedUsers()).doesNotContain(username);


        verify(postRepository).save(postArgumentCaptor.capture());
        Post capturedPost = postArgumentCaptor.getValue();
        assertThat(capturedPost.getLikes()).isEqualTo(0);
        assertThat(capturedPost.getLikedUsers()).doesNotContain(username);
    }

    @Test
    void deletePost_shouldDeletePostAndImageFromS3() {
        // Given
        Long postId = 1L;
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getUsername());
        when(userRepository.findUserByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(postRepository.findPostByIdAndUser(postId, user)).thenReturn(Optional.of(post));
        ImageModel imageModel = new ImageModel();  // Simulate an image associated with the post
        imageModel.setS3Key("test-s3-key");
        when(imageRepository.findByPostId(postId)).thenReturn(Optional.of(imageModel));

        // When
        postService.deletePost(postId, principal);

        // Then
        verify(postRepository).delete(post);
        verify(imageRepository).delete(imageModel);
        verify(s3Service).deleteObject(s3Buckets.getImgBucket(), imageModel.getS3Key());
    }
}

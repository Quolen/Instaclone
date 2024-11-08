package com.example.instaclone.services;

import com.example.instaclone.dto.CommentDTO;
import com.example.instaclone.entity.Comment;
import com.example.instaclone.entity.Post;
import com.example.instaclone.entity.User;
import com.example.instaclone.repository.CommentRepository;
import com.example.instaclone.repository.PostRepository;
import com.example.instaclone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private CommentDTO commentDTO;
    private Principal principal;
    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        commentDTO = new CommentDTO();
        commentDTO.setMessage("Test message");

        principal = mock(Principal.class);

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        post = new Post();
        post.setId(1L);
    }

    @Test
    void saveComment_shouldReturnSavedComment() {
        when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Comment comment = new Comment();
        comment.setMessage(commentDTO.getMessage());
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(principal.getName()).thenReturn("testUser");

        Comment savedComment = commentService.saveComment(1L, commentDTO, principal);

        assertNotNull(savedComment);
        assertEquals(commentDTO.getMessage(), savedComment.getMessage());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void getAllCommentsForPost_shouldReturnCommentsForPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        List<Comment> comments = List.of(new Comment());
        when(commentRepository.findAllByPostOrderByCreatedAtDesc(post)).thenReturn(comments);

        List<Comment> foundComments = commentService.getAllCommentsForPost(1L);

        assertNotNull(foundComments);
        assertEquals(1, foundComments.size());
        verify(commentRepository).findAllByPostOrderByCreatedAtDesc(post);
    }

    @Test
    void deleteComment_shouldDeleteCommentWhenExists() {
        Comment comment = new Comment();
        comment.setId(1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.DeleteComment(1L);

        verify(commentRepository).delete(comment);
    }
}
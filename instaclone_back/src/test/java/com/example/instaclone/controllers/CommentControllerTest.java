package com.example.instaclone.controllers;

import com.example.instaclone.dto.CommentDTO;
import com.example.instaclone.entity.Comment;
import com.example.instaclone.exceptions.AuthorizationException;
import com.example.instaclone.exceptions.CommentNotFoundException;
import com.example.instaclone.exceptions.PostNotFoundException;
import com.example.instaclone.mapper.CommentMapper;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.CommentService;
import com.example.instaclone.services.CustomUserDetailService;
import com.example.instaclone.validations.ResponseErrorValidation;
import com.example.instaclone.web.CommentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CommentMapper commentMapper;

    @MockBean
    private ResponseErrorValidation responseErrorValidation;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @Autowired
    public CommentControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        jwtToken = "mock-jwt-token";

        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("testUser");

        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authentication.isAuthenticated()).willReturn(true);

        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
    }

    @Test
    public void testCreateComment() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setMessage("Test Comment");

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setMessage("Test Comment");

        given(responseErrorValidation.mapValidationService(any())).willReturn(null);
        given(commentService.saveComment(anyLong(), any(CommentDTO.class), any(Principal.class))).willReturn(comment);
        given(commentMapper.commentToCommentDTO(any(Comment.class))).willReturn(commentDTO);

        String commentJson = "{\"message\":\"Test Comment\"}";

        mockMvc.perform(post("/api/comment/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Test Comment"));
    }

    @Test
    public void testGetAllCommentsToPost() throws Exception {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setMessage("Test Comment");

        List<CommentDTO> commentDTOList = Arrays.asList(commentDTO);

        given(commentService.getAllCommentsForPost(anyLong())).willReturn(Arrays.asList(new Comment()));
        given(commentMapper.commentToCommentDTO(any(Comment.class))).willReturn(commentDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/comment/1/all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Test Comment"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        mockMvc.perform(post("/api/comment/1/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment was deleted"));
    }

    // Additional tests for error cases

    @Test
    public void testCreateComment_InvalidInput() throws Exception {
        CommentDTO commentDTO = new CommentDTO();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        when(responseErrorValidation.mapValidationService(bindingResult)).thenReturn(ResponseEntity.badRequest().build());

        mockMvc.perform(post("/api/comment/1/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllCommentsToPost_PostNotFound() throws Exception {
        when(commentService.getAllCommentsForPost(anyLong())).thenThrow(new PostNotFoundException("Post not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/comment/1/all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteComment_Unauthorized() throws Exception {
        doThrow(new AuthorizationException("You are not authorized to delete this comment."))
                .when(commentService).DeleteComment(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/comment/1/delete")
                        .header("Authorization", "Bearer " + jwtToken) // Provide invalid or expired token
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("You are not authorized to delete this comment."));
    }

    @Test
    public void testDeleteComment_CommentNotFound() throws Exception {
        doThrow(new CommentNotFoundException("Comment not found"))
                .when(commentService).DeleteComment(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/comment/1/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Comment not found"));
    }
}

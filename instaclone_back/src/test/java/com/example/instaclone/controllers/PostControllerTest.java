package com.example.instaclone.controllers;

import com.example.instaclone.dto.PostDTO;
import com.example.instaclone.entity.Post;
import com.example.instaclone.facade.PostFacade;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.CustomUserDetailService;
import com.example.instaclone.services.PostService;
import com.example.instaclone.validations.ResponseErrorValidation;
import com.example.instaclone.web.PostController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
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

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(PostController.class)
public class PostControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private PostFacade postFacade;

    @MockBean
    private ResponseErrorValidation responseErrorValidation;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @Autowired
    public PostControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private String jwtToken;

    @BeforeEach
    public void setUp() {
        jwtToken = "mock-jwt-token";

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("testUser");

        Authentication authentication = Mockito.mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authentication.isAuthenticated()).willReturn(true);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        given(authenticationManager.authenticate(any(Authentication.class))).willReturn(authentication);
    }

    @Test
    public void testCreatePost() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Test Title");
        postDTO.setCaption("Test Caption");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Title");
        post.setCaption("Test Caption");

        given(responseErrorValidation.mapValidationService(any())).willReturn(null);
        given(postService.createPost(any(PostDTO.class), any(Principal.class))).willReturn(post);
        given(postFacade.postToPostDTO(any(Post.class))).willReturn(postDTO);

        String postJson = "{\"title\":\"Test Title\",\"caption\":\"Test Caption\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.caption").value("Test Caption"));
    }

    @Test
    public void testGetAllPosts() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Test Title");
        postDTO.setCaption("Test Caption");

        List<PostDTO> postDTOList = Arrays.asList(postDTO);

        given(postService.getAllPosts()).willReturn(Arrays.asList(new Post()));
        given(postFacade.postToPostDTO(any(Post.class))).willReturn(postDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].caption").value("Test Caption"));
    }

    @Test
    public void testGetAllPostsForUser() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Test Title");
        postDTO.setCaption("Test Caption");

        List<PostDTO> postDTOList = Arrays.asList(postDTO);

        given(postService.getAllPostForUser(any(Principal.class))).willReturn(Arrays.asList(new Post()));
        given(postFacade.postToPostDTO(any(Post.class))).willReturn(postDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/post/user/posts")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].caption").value("Test Caption"));
    }

    @Test
    public void testLikePost() throws Exception {
        PostDTO postDTO = new PostDTO();
        postDTO.setTitle("Test Title");
        postDTO.setCaption("Test Caption");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Title");
        post.setCaption("Test Caption");

        given(postService.likePost(anyLong(), anyString())).willReturn(post);
        given(postFacade.postToPostDTO(any(Post.class))).willReturn(postDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/1/testUser/like")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.caption").value("Test Caption"));
    }

    @Test
    public void testDeletePost() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/1/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post was deleted"));
    }

    // Additional tests for error cases

    @Test
    public void testCreatePostWithValidationErrors() throws Exception {
        given(responseErrorValidation.mapValidationService(any())).willReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        String postJson = "{}"; // Invalid JSON to trigger validation errors

        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLikePostWithInvalidPostId() throws Exception {
        given(postService.likePost(anyLong(), anyString())).willThrow(new RuntimeException("Post not found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/999/testUser/like")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeletePostWithInvalidPostId() throws Exception {
        Mockito.doThrow(new RuntimeException("Post not found")).when(postService).deletePost(anyLong(), any(Principal.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/post/999/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNotFound());
    }
}

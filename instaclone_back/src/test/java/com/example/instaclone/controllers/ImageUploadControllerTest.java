package com.example.instaclone.controllers;

import com.example.instaclone.entity.ImageModel;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.CustomUserDetailService;
import com.example.instaclone.services.ImageUploadService;
import com.example.instaclone.web.ImageUploadController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ImageUploadController.class)
public class ImageUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

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
    public void testUploadImageToUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"));
    }

    @Test
    public void testUploadImageToPost() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/1/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"));
    }

    @Test
    public void testGetImageForUser() throws Exception {
        ImageModel imageModel = new ImageModel();
        imageModel.setName("test.jpg");
        imageModel.setImageBytes("test image".getBytes());

        given(imageUploadService.getImageToUser(any(Principal.class))).willReturn(imageModel);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/image/profileImage")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.jpg"));
    }

    @Test
    public void testGetProfileImageToPost() throws Exception {
        ImageModel imageModel = new ImageModel();
        imageModel.setName("test.jpg");
        imageModel.setImageBytes("test image".getBytes());

        given(imageUploadService.getProfileImageToPost(anyLong())).willReturn(imageModel);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/image/profileImage/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.jpg"));
    }

    @Test
    public void testDeleteImageForUser() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/image/profileImage/delete")
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile image deleted successfully"));
    }

    @Test
    public void testGetImageToPost() throws Exception {
        ImageModel imageModel = new ImageModel();
        imageModel.setName("test.jpg");
        imageModel.setImageBytes("test image".getBytes());

        given(imageUploadService.getImageToPost(anyLong())).willReturn(imageModel);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/image/1/image")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test.jpg"));
    }

    // Additional tests for error cases

    @Test
    public void testUploadImageToUserWithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());

        Mockito.doThrow(new IOException("Image upload failed")).when(imageUploadService).uploadImageToUser(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testUploadImageToPostWithError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());

        Mockito.doThrow(new IOException("Image upload failed")).when(imageUploadService).uploadImageToPost(any(), any(), anyLong());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/image/1/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isInternalServerError());
    }
}

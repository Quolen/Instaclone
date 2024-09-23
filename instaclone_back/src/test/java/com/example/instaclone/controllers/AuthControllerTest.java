package com.example.instaclone.controllers;

import com.example.instaclone.payload.request.LoginRequest;
import com.example.instaclone.payload.request.SignupRequest;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.security.SecurityConstants;
import com.example.instaclone.services.CustomUserDetailService;
import com.example.instaclone.services.UserService;
import com.example.instaclone.validations.ResponseErrorValidation;
import com.example.instaclone.web.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private ResponseErrorValidation responseErrorValidation;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @MockBean
    private UserService userService;

    @Autowired
    public AuthControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser
    void testAuthenticateUser_Success() throws Exception {
        // Mocking JWT token
        String mockJwtToken = "mock_jwt_token";

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser@mail.com");
        loginRequest.setPassword("password123");
        String requestJson = new ObjectMapper().writeValueAsString(loginRequest);

        // Mock authentication request
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn(mockJwtToken);

        // Perform the login request and validate the response
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value(SecurityConstants.TOKEN_PREFIX + mockJwtToken));
    }

    @Test
    @WithMockUser
    void testAuthenticateUser_InvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("invaliduser@@mail.com");
        loginRequest.setPassword("wrong");
        String requestJson = new ObjectMapper().writeValueAsString(loginRequest);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testAuthenticateUser_ValidationErrors() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("");
        loginRequest.setPassword("");
        String requestJson = new ObjectMapper().writeValueAsString(loginRequest);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("loginRequest", "Validation error")));
        when(responseErrorValidation.mapValidationService(bindingResult)).thenReturn(ResponseEntity.badRequest().body("Validation error"));

        // Ensure the mock is used by the controller
        doReturn(ResponseEntity.badRequest().body("Validation error")).when(responseErrorValidation).mapValidationService(any(BindingResult.class));

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Validation error"));
    }

    @Test
    @WithMockUser
    void testRegisterUser_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("newuser@mail.com");
        signupRequest.setFirstname("firstname");
        signupRequest.setLastname("lastname");
        signupRequest.setUsername("username");
        signupRequest.setPassword("password123");
        signupRequest.setConfirmPassword("password123");
        String requestJson = new ObjectMapper().writeValueAsString(signupRequest);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully."));
    }

    @Test
    @WithMockUser
    void testRegisterUser_ValidationErrors() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("user@mail.com");
        signupRequest.setFirstname("firstname");
        signupRequest.setLastname("lastname");
        signupRequest.setUsername("username");
        signupRequest.setPassword("password123");
        signupRequest.setConfirmPassword("password122");
        String requestJson = new ObjectMapper().writeValueAsString(signupRequest);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new ObjectError("signupRequest", "Validation error")));
        when(responseErrorValidation.mapValidationService(bindingResult)).thenReturn(ResponseEntity.badRequest().body("Validation error"));

        doReturn(ResponseEntity.badRequest().body("Validation error")).when(responseErrorValidation).mapValidationService(any(BindingResult.class));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Validation error"));
    }
}

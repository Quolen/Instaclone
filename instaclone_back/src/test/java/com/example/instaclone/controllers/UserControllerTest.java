package com.example.instaclone.controllers;

import com.example.instaclone.dto.UserDTO;
import com.example.instaclone.entity.User;
import com.example.instaclone.mapper.UserMapper;
import com.example.instaclone.security.JWTTokenProvider;
import com.example.instaclone.services.CustomUserDetailService;
import com.example.instaclone.services.UserService;
import com.example.instaclone.validations.ResponseErrorValidation;
import com.example.instaclone.web.UserController;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private ResponseErrorValidation responseErrorValidation;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @Autowired
    public UserControllerTest(MockMvc mockMvc) {
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
    public void testGetCurrentUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");

        given(userService.getCurrentUser(any(Principal.class))).willReturn(user);
        given(userMapper.userToUserDTO(any(User.class))).willReturn(userDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    public void testGetUserProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");

        given(userService.getUserById(1L)).willReturn(user);
        given(userMapper.userToUserDTO(any(User.class))).willReturn(userDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/1")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");

        given(userService.getUserByUsername("testUser")).willReturn(user);
        given(userMapper.userToUserDTO(any(User.class))).willReturn(userDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/username/testUser")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("updatedUser");

        User user = new User();
        user.setId(1L);
        user.setUsername("updatedUser");

        given(responseErrorValidation.mapValidationService(any())).willReturn(null);
        given(userService.updateUser(any(UserDTO.class), any(Principal.class))).willReturn(user);
        given(userMapper.userToUserDTO(any(User.class))).willReturn(userDTO);

        String userJson = "{\"id\":1,\"username\":\"updatedUser\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("updatedUser"));
    }

    @Test
    public void testGetCurrentUserWithInvalidToken() throws Exception {

        SecurityContextHolder.clearContext();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/")
                        .header("Authorization", "Bearer invalid-jwt-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetCurrentUserWithMissingToken() throws Exception {
        SecurityContextHolder.clearContext();

        given(authenticationManager.authenticate(any(Authentication.class))).willThrow(new BadCredentialsException("No Authentication provided"));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserProfileWithInvalidUserId() throws Exception {
        given(userService.getUserById(eq(999L))).willThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/user/999")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateUserWithValidationErrors() throws Exception {
        UserDTO userDTO = new UserDTO();

        given(responseErrorValidation.mapValidationService(any())).willReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        String userJson = "{}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson)
                        .header("Authorization", "Bearer " + jwtToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest());
    }
}

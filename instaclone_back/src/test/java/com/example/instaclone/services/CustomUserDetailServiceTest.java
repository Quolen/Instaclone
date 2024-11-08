package com.example.instaclone.services;

import com.example.instaclone.entity.User;
import com.example.instaclone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("password");
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
        when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("testUser", userDetails.getUsername());
        verify(userRepository).findUserByEmail("test@example.com");
    }

    @Test
    void loadUserById_shouldReturnUserWhenUserExists() {
        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));

        User foundUser = customUserDetailService.loadUserById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
        verify(userRepository).findUserById(1L);
    }
}
package com.example.instaclone.services;

import com.example.instaclone.dto.UserDTO;
import com.example.instaclone.entity.User;
import com.example.instaclone.entity.enums.ERole;
import com.example.instaclone.exceptions.UserExistException;
import com.example.instaclone.payload.request.SignupRequest;
import com.example.instaclone.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_shouldSaveNewUser() {
        //Given
        SignupRequest signupRequest = new SignupRequest(
                "john@example.com",
                "John",
                "Doe",
                "johndoe",
                "password",
                "password"
        );
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        //When
        userService.createUser(signupRequest);

        //Then
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(capturedUser.getName()).isEqualTo("John");
        assertThat(capturedUser.getLastname()).isEqualTo("Doe");
        assertThat(capturedUser.getUsername()).isEqualTo("johndoe");
        assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(capturedUser.getRole()).contains(ERole.ROLE_USER);
    }

    @Test
    void createUser_shouldThrowException_whenUserAlreadyExists() {
        //Given
        SignupRequest signupRequest = new SignupRequest(
                "john@example.com",
                "John",
                "Doe",
                "johndoe",
                "password",
                "password"
        );
        when(userRepository.save(any(User.class))).thenThrow(new UserExistException("The user with such email \"" + signupRequest.getEmail() + "\" already exists. Please check your credentials."));

        //When & Then
        assertThatThrownBy(() -> userService.createUser(signupRequest))
                .isInstanceOf(UserExistException.class)
                .hasMessageContaining("The user with such email \"" + signupRequest.getEmail() + "\" already exists. Please check your credentials.");
    }

    @Test
    void updateUser_shouldUpdateUserDetails() {
        //Given
        UserDTO userDTO = new UserDTO(1L,"NewFirstName", "NewLastName", "NewUsername", "NewBio");
        User existingUser = new User(1L, "John", "Doe", "johndoe", "john@example.com", "password");

        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("johndoe");
        when(userRepository.findUserByUsername("johndoe")).thenReturn(Optional.of(existingUser));

        //When
        userService.updateUser(userDTO, principal);

        //Then
        verify(userRepository).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser.getName()).isEqualTo("NewFirstName");
        assertThat(capturedUser.getLastname()).isEqualTo("NewLastName");
        assertThat(capturedUser.getBio()).isEqualTo("NewBio");

    }

    @Test
    void getCurrentUser_shouldReturnUser() {
        //Given
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("johndoe");

        User expectedUser = new User(1L, "John", "Doe", "johndoe", "john@example.com", "password");
        when(userRepository.findUserByUsername("johndoe")).thenReturn(Optional.of(expectedUser));

        //When & Then
        User actualUser = userService.getCurrentUser(principal);
        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void getUserById_shouldReturnUserWhenFound() {
        //Given
        User expectedUser = new User(1L, "John", "Doe", "johndoe", "john@example.com", "password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        //When & Then
        User actualUser = userService.getUserById(1L);

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void getUserById_shouldThrowExceptionWhenNotFound() {
        //Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserByUsername_shouldReturnUserWhenFound() {
        //Given
        User expectedUser = new User(1L, "John", "Doe", "johndoe", "john@example.com", "password");
        when(userRepository.findUserByUsername("johndoe")).thenReturn(Optional.of(expectedUser));

        //When & Then
        User actualUser = userService.getUserByUsername("johndoe");

        assertThat(actualUser).isEqualTo(expectedUser);
    }

    @Test
    void getUserByUsername_shouldThrowExceptionWhenNotFound() {
        when(userRepository.findUserByUsername("johndoe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("johndoe"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
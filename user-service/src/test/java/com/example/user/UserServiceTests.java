package com.example.user;

import com.example.user.dto.UserRegisterDto;
import com.example.user.dto.UserResponseDto;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.example.user.security.JwtTokenProvider;
import com.example.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class UserServiceTests {

    @Test
    void testRegisterUser_Success() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        JwtTokenProvider tokenProvider = Mockito.mock(JwtTokenProvider.class);

        UserService userService = new UserService(repo, encoder, tokenProvider);

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("testuser");
        dto.setPassword("password");
        dto.setEmail("test@example.com");
        dto.setRole("ROLE_USER");

        Mockito.when(repo.existsByUsername("testuser")).thenReturn(false);
        Mockito.when(repo.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(encoder.encode("password")).thenReturn("encodedPassword");
        
        User savedUser = new User("testuser", "encodedPassword", "test@example.com", "ROLE_USER");
        savedUser.setId(1L);
        Mockito.when(repo.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto result = userService.registerUser(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }
}

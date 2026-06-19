package com.example.user.service;

import com.example.user.dto.JwtResponseDto;
import com.example.user.dto.UserLoginDto;
import com.example.user.dto.UserRegisterDto;
import com.example.user.dto.UserResponseDto;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import com.example.user.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public UserResponseDto registerUser(UserRegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        String encodedPassword = passwordEncoder.encode(registerDto.getPassword());
        String role = registerDto.getRole() != null ? registerDto.getRole() : "ROLE_USER";

        User user = new User(registerDto.getUsername(), encodedPassword, registerDto.getEmail(), role);
        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
    }

    public JwtResponseDto loginUser(UserLoginDto loginDto) {
        User user = userRepository.findByUsername(loginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getEmail(), user.getRole());
        return new JwtResponseDto(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    public UserResponseDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}

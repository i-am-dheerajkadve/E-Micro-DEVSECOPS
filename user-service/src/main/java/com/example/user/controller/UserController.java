package com.example.user.controller;

import com.example.user.dto.JwtResponseDto;
import com.example.user.dto.UserLoginDto;
import com.example.user.dto.UserRegisterDto;
import com.example.user.dto.UserResponseDto;
import com.example.user.security.JwtTokenProvider;
import com.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @Autowired
    public UserController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterDto registerDto) {
        try {
            UserResponseDto registeredUser = userService.registerUser(registerDto);
            return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserLoginDto loginDto) {
        try {
            JwtResponseDto jwtResponse = userService.loginUser(loginDto);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return new ResponseEntity<>("Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = tokenHeader.substring(7);
        if (!tokenProvider.validateToken(token)) {
            return new ResponseEntity<>("Token is expired or invalid", HttpStatus.UNAUTHORIZED);
        }

        try {
            String username = tokenProvider.getUsernameFromToken(token);
            UserResponseDto userProfile = userService.getUserProfile(username);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

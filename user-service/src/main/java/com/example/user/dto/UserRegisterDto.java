package com.example.user.dto;

public class UserRegisterDto {
    private String username;
    private String password;
    private String email;
    private String role; // Optional, defaults to ROLE_USER

    public UserRegisterDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

package com.example.notification.model;

import java.time.LocalDateTime;

public class NotificationRequest {
    private String email;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public NotificationRequest() {}

    public NotificationRequest(String email, String message) {
        this.email = email;
        this.message = message;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

package com.example.notification.controller;

import com.example.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private final List<NotificationRequest> notificationHistory = new CopyOnWriteArrayList<>();

    @PostMapping
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
        logger.info("[NOTIFICATION SERVICE] Sending notification to: {} | Content: {}", request.getEmail(), request.getMessage());
        
        // Mock actual sending by logging it clearly to stdout
        System.out.println("======================================================================");
        System.out.println("MOCK EMAIL SENT TO: " + request.getEmail());
        System.out.println("TIMESTAMP: " + request.getTimestamp());
        System.out.println("MESSAGE: " + request.getMessage());
        System.out.println("======================================================================");

        notificationHistory.add(request);
        return ResponseEntity.ok("Notification sent successfully");
    }

    @GetMapping
    public ResponseEntity<List<NotificationRequest>> getNotificationHistory() {
        return ResponseEntity.ok(notificationHistory);
    }
}

package com.example.notification;

import com.example.notification.controller.NotificationController;
import com.example.notification.model.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationControllerTests {

    @Test
    void testSendAndGetHistory() {
        NotificationController controller = new NotificationController();
        NotificationRequest request = new NotificationRequest("test@test.com", "Hello World");

        ResponseEntity<String> response = controller.sendNotification(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Notification sent successfully", response.getBody());

        ResponseEntity<List<NotificationRequest>> historyResponse = controller.getNotificationHistory();
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertNotNull(historyResponse.getBody());
        assertEquals(1, historyResponse.getBody().size());
        assertEquals("test@test.com", historyResponse.getBody().get(0).getEmail());
        assertEquals("Hello World", historyResponse.getBody().get(0).getMessage());
    }
}

package com.example.payment;

import com.example.payment.controller.PaymentController;
import com.example.payment.dto.PaymentRequestDto;
import com.example.payment.dto.PaymentResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentControllerTests {

    @Test
    void testProcessPayment_Success() {
        PaymentController controller = new PaymentController();
        PaymentRequestDto request = new PaymentRequestDto();
        request.setCardNumber("1234567812345678");
        request.setCvv("123");
        request.setAmount(new BigDecimal("150.00"));

        ResponseEntity<PaymentResponseDto> response = controller.processPayment(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getTransactionId());
        assertTrue(response.getBody().getMessage().contains("150.00"));
    }

    @Test
    void testProcessPayment_Failure() {
        PaymentController controller = new PaymentController();
        PaymentRequestDto request = new PaymentRequestDto();
        request.setCardNumber("12340000"); // ends with 0000
        request.setCvv("123");
        request.setAmount(new BigDecimal("150.00"));

        ResponseEntity<PaymentResponseDto> response = controller.processPayment(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertNull(response.getBody().getTransactionId());
        assertTrue(response.getBody().getMessage().contains("Payment Declined"));
    }
}

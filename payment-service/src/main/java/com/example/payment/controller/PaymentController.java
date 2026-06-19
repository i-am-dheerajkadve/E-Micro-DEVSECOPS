package com.example.payment.controller;

import com.example.payment.dto.PaymentRequestDto;
import com.example.payment.dto.PaymentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(@RequestBody PaymentRequestDto requestDto) {
        // Simple mock validation rule: card number ending in 0000 triggers mock payment failure
        if (requestDto.getCardNumber() != null && requestDto.getCardNumber().endsWith("0000")) {
            return new ResponseEntity<>(
                    new PaymentResponseDto(false, null, "Payment Declined: Insufficient Funds or Invalid Card"),
                    HttpStatus.BAD_REQUEST
            );
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return ResponseEntity.ok(new PaymentResponseDto(
                true,
                transactionId,
                "Payment of $" + requestDto.getAmount() + " processed successfully."
        ));
    }
}

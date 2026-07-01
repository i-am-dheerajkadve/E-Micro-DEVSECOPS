package com.example.payment.dto;

public class PaymentResponseDto {
    private boolean success;
    private String transactionId;
    private String message;

    public PaymentResponseDto() {}

    public PaymentResponseDto(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

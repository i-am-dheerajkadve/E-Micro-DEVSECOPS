package com.example.payment.dto;

import java.math.BigDecimal;

public class PaymentRequestDto {
    private String cardNumber;
    private String cvv;
    private BigDecimal amount;

    public PaymentRequestDto() {}

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

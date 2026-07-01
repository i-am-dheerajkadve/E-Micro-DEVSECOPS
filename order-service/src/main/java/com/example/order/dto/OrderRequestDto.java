package com.example.order.dto;

public class OrderRequestDto {
    private String username;
    private String shippingAddress;
    private String cardNumber;
    private String cvv;

    public OrderRequestDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}

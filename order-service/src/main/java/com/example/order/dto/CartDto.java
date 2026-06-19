package com.example.order.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private String username;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;

    public CartDto() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}

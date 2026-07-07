package com.example.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private String username;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;

    public CartDto() {}

    public CartDto(String username, List<CartItemDto> items, BigDecimal totalAmount) {
        this.username = username;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}

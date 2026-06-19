package com.example.cart.controller;

import com.example.cart.dto.CartDto;
import com.example.cart.dto.CartItemDto;
import com.example.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@RequestParam String username) {
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @PostMapping
    public ResponseEntity<CartDto> addItemToCart(@RequestParam String username, @RequestBody CartItemDto itemDto) {
        return ResponseEntity.ok(cartService.addItemToCart(username, itemDto));
    }

    @PutMapping("/quantity")
    public ResponseEntity<CartDto> updateItemQuantity(
            @RequestParam String username,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(username, productId, quantity));
    }

    @DeleteMapping
    public ResponseEntity<CartDto> removeItemFromCart(
            @RequestParam String username,
            @RequestParam Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(username, productId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestParam String username) {
        cartService.clearCart(username);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}

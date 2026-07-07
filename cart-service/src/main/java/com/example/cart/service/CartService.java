package com.example.cart.service;

import com.example.cart.dto.CartDto;
import com.example.cart.dto.CartItemDto;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public CartDto getCart(String username) {
        List<CartItem> items = cartItemRepository.findByUsername(username);
        return convertToCartDto(username, items);
    }

    public CartDto addItemToCart(String username, CartItemDto itemDto) {
        Optional<CartItem> existingItemOpt = cartItemRepository.findByUsernameAndProductId(username, itemDto.getProductId());

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + itemDto.getQuantity());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem(
                    username,
                    itemDto.getProductId(),
                    itemDto.getProductName(),
                    itemDto.getPrice(),
                    itemDto.getQuantity(),
                    itemDto.getImageUrl()
            );
            cartItemRepository.save(newItem);
        }

        return getCart(username);
    }

    public CartDto updateItemQuantity(String username, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByUsernameAndProductId(username, productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCart(username);
    }

    public CartDto removeItemFromCart(String username, Long productId) {
        CartItem item = cartItemRepository.findByUsernameAndProductId(username, productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cartItemRepository.delete(item);
        return getCart(username);
    }

    @Transactional
    public void clearCart(String username) {
        cartItemRepository.deleteByUsername(username);
    }

    private CartDto convertToCartDto(String username, List<CartItem> items) {
        List<CartItemDto> itemDtos = items.stream()
                .map(item -> new CartItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getImageUrl()
                ))
                .collect(Collectors.toList());

        BigDecimal total = itemDtos.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(username, itemDtos, total);
    }
}

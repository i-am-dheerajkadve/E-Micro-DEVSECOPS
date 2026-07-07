package com.example.cart;

import com.example.cart.dto.CartDto;
import com.example.cart.dto.CartItemDto;
import com.example.cart.model.CartItem;
import com.example.cart.repository.CartItemRepository;
import com.example.cart.service.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class CartServiceTests {

    @Test
    void testGetCart_Empty() {
        CartItemRepository repo = Mockito.mock(CartItemRepository.class);
        CartService service = new CartService(repo);

        Mockito.when(repo.findByUsername("user1")).thenReturn(Collections.emptyList());

        CartDto cart = service.getCart("user1");

        assertNotNull(cart);
        assertEquals("user1", cart.getUsername());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotalAmount());
    }

    @Test
    void testAddItemToCart_NewItem() {
        CartItemRepository repo = Mockito.mock(CartItemRepository.class);
        CartService service = new CartService(repo);

        String username = "user1";
        CartItemDto inputDto = new CartItemDto(null, 5L, "Phone", new BigDecimal("500.00"), 2, "url");

        Mockito.when(repo.findByUsernameAndProductId(username, 5L)).thenReturn(java.util.Optional.empty());
        
        CartItem savedItem = new CartItem(username, 5L, "Phone", new BigDecimal("500.00"), 2, "url");
        savedItem.setId(1L);

        List<CartItem> items = new ArrayList<>();
        items.add(savedItem);
        Mockito.when(repo.findByUsername(username)).thenReturn(items);

        CartDto cart = service.addItemToCart(username, inputDto);

        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(new BigDecimal("1000.00"), cart.getTotalAmount());
    }
}

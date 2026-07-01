package com.example.order;

import com.example.order.dto.*;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import com.example.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class OrderServiceTests {

    @Test
    void testPlaceOrder_Success() {
        OrderRepository repo = Mockito.mock(OrderRepository.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        OrderService service = new OrderService(repo, restTemplate);

        OrderRequestDto request = new OrderRequestDto();
        request.setUsername("buyer");
        request.setShippingAddress("123 Street");
        request.setCardNumber("12345678");
        request.setCvv("123");

        // Mock cart retrieval
        CartDto cart = new CartDto();
        cart.setUsername("buyer");
        cart.setTotalAmount(new BigDecimal("100.00"));
        
        CartItemDto item = new CartItemDto();
        item.setProductId(1L);
        item.setProductName("Item A");
        item.setPrice(new BigDecimal("50.00"));
        item.setQuantity(2);
        cart.setItems(List.of(item));

        Mockito.when(restTemplate.getForObject(anyString(), eq(CartDto.class))).thenReturn(cart);

        // Mock inventory reduction
        ResponseEntity<String> inventoryResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        Mockito.when(restTemplate.postForEntity(contains("/api/inventory/reduce"), any(), eq(String.class))).thenReturn(inventoryResponse);

        // Mock payment processing
        Map<String, Object> paymentResponseBody = new HashMap<>();
        paymentResponseBody.put("success", true);
        paymentResponseBody.put("transactionId", "TXN999");
        ResponseEntity<Map> paymentResponse = new ResponseEntity<>(paymentResponseBody, HttpStatus.OK);
        Mockito.when(restTemplate.postForEntity(contains("/api/payments/process"), any(), eq(Map.class))).thenReturn(paymentResponse);

        // Mock order save
        Mockito.when(repo.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponseDto result = service.placeOrder(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("buyer", result.getUsername());
        assertEquals("PAID", result.getStatus());
        assertEquals("TXN999", result.getPaymentTransactionId());
    }
}

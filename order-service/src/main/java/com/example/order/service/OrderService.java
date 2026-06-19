package com.example.order.service;

import com.example.order.dto.CartDto;
import com.example.order.dto.CartItemDto;
import com.example.order.dto.OrderItemDto;
import com.example.order.dto.OrderRequestDto;
import com.example.order.dto.OrderResponseDto;
import com.example.order.model.Order;
import com.example.order.model.OrderItem;
import com.example.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${services.cart.url}")
    private String cartServiceUrl;

    @Value("${services.inventory.url}")
    private String inventoryServiceUrl;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    @Autowired
    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto requestDto) {
        // 1. Fetch Cart details
        String cartUrl = cartServiceUrl + "/api/cart?username=" + requestDto.getUsername();
        CartDto cart = restTemplate.getForObject(cartUrl, CartDto.class);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty or not found!");
        }

        // 2. Verify and Reduce Inventory for each item
        for (CartItemDto item : cart.getItems()) {
            try {
                String reduceInventoryUrl = inventoryServiceUrl + "/api/inventory/reduce?productId=" + item.getProductId() + "&quantity=" + item.getQuantity();
                ResponseEntity<String> response = restTemplate.postForEntity(reduceInventoryUrl, null, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
                }
            } catch (Exception e) {
                throw new RuntimeException("Inventory check failed: " + e.getMessage());
            }
        }

        // 3. Process Payment
        String paymentUrl = paymentServiceUrl + "/api/payments/process";
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("cardNumber", requestDto.getCardNumber());
        paymentRequest.put("cvv", requestDto.getCvv());
        paymentRequest.put("amount", cart.getTotalAmount());

        String transactionId;
        try {
            ResponseEntity<Map> paymentResponse = restTemplate.postForEntity(paymentUrl, paymentRequest, Map.class);
            Map<String, Object> body = paymentResponse.getBody();
            if (body == null || !(Boolean) body.get("success")) {
                throw new RuntimeException("Payment processing failed!");
            }
            transactionId = (String) body.get("transactionId");
        } catch (Exception e) {
            throw new RuntimeException("Payment service down or failed: " + e.getMessage());
        }

        // 4. Create and Save Order
        Order order = new Order(
                requestDto.getUsername(),
                requestDto.getShippingAddress(),
                cart.getTotalAmount(),
                "PAID"
        );
        order.setPaymentTransactionId(transactionId);

        for (CartItemDto item : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                    item.getProductId(),
                    item.getProductName(),
                    item.getPrice(),
                    item.getQuantity()
            );
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // 5. Clear Cart
        try {
            String clearCartUrl = cartServiceUrl + "/api/cart/clear?username=" + requestDto.getUsername();
            restTemplate.delete(clearCartUrl);
        } catch (Exception e) {
            // Log it but don't fail the order if cart clearing has an issue
        }

        // 6. Send Notification log
        try {
            Map<String, Object> notificationRequest = new HashMap<>();
            notificationRequest.put("email", requestDto.getUsername() + "@example.com");
            notificationRequest.put("message", "Thank you! Your order #" + savedOrder.getId() + " has been placed successfully. Total: $" + savedOrder.getTotalAmount());
            restTemplate.postForEntity(notificationServiceUrl + "/api/notifications", notificationRequest, String.class);
        } catch (Exception e) {
            // Log notification failure but keep going
        }

        return convertToResponseDto(savedOrder);
    }

    public List<OrderResponseDto> getOrderHistory(String username) {
        return orderRepository.findByUsernameOrderByOrderDateDesc(username).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private OrderResponseDto convertToResponseDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new OrderResponseDto(
                order.getId(),
                order.getUsername(),
                order.getShippingAddress(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getPaymentTransactionId(),
                order.getOrderDate(),
                itemDtos
        );
    }
}

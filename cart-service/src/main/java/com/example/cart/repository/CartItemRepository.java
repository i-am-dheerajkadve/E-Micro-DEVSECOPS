package com.example.cart.repository;

import com.example.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUsername(String username);
    Optional<CartItem> findByUsernameAndProductId(String username, Long productId);
    void deleteByUsername(String username);
}

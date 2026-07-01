package com.example.inventory.service;

import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @PostConstruct
    public void seedInventory() {
        // Initialize stock for the default 5 products
        for (long i = 1; i <= 5; i++) {
            if (inventoryRepository.findByProductId(i).isEmpty()) {
                inventoryRepository.save(new Inventory(i, 50));
            }
        }
    }

    public Integer getStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getStock)
                .orElse(0);
    }

    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found in inventory: " + productId));

        if (inventory.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product id " + productId + ". Available: " + inventory.getStock());
        }

        inventory.setStock(inventory.getStock() - quantity);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public Integer updateStock(Long productId, Integer stock) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(productId, 0));

        inventory.setStock(stock);
        Inventory saved = inventoryRepository.save(inventory);
        return saved.getStock();
    }
}

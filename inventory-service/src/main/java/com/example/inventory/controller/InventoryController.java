package com.example.inventory.controller;

import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }

    @PostMapping("/reduce")
    public ResponseEntity<?> reduceStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        try {
            inventoryService.reduceStock(productId, quantity);
            return ResponseEntity.ok("Stock reduced successfully");
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Integer> updateStock(@RequestParam Long productId, @RequestParam Integer stock) {
        Integer updatedStock = inventoryService.updateStock(productId, stock);
        return ResponseEntity.ok(updatedStock);
    }
}

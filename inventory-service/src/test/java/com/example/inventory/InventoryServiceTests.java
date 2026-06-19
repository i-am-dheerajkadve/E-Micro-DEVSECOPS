package com.example.inventory;

import com.example.inventory.model.Inventory;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class InventoryServiceTests {

    @Test
    void testGetStock_Exists() {
        InventoryRepository repo = Mockito.mock(InventoryRepository.class);
        InventoryService service = new InventoryService(repo);

        Mockito.when(repo.findByProductId(1L)).thenReturn(Optional.of(new Inventory(1L, 20)));

        Integer stock = service.getStock(1L);

        assertEquals(20, stock);
    }

    @Test
    void testReduceStock_Success() {
        InventoryRepository repo = Mockito.mock(InventoryRepository.class);
        InventoryService service = new InventoryService(repo);

        Inventory item = new Inventory(1L, 10);
        Mockito.when(repo.findByProductId(1L)).thenReturn(Optional.of(item));

        service.reduceStock(1L, 4);

        assertEquals(6, item.getStock());
        Mockito.verify(repo).save(item);
    }

    @Test
    void testReduceStock_Insufficient() {
        InventoryRepository repo = Mockito.mock(InventoryRepository.class);
        InventoryService service = new InventoryService(repo);

        Inventory item = new Inventory(1L, 3);
        Mockito.when(repo.findByProductId(1L)).thenReturn(Optional.of(item));

        assertThrows(RuntimeException.class, () -> service.reduceStock(1L, 5));
    }
}

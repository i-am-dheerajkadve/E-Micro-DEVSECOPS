package com.example.product;

import com.example.product.dto.ProductDto;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTests {

    @Test
    void testGetProductById_Success() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductService service = new ProductService(repo);

        Product dummyProduct = new Product("Laptop", "Developer laptop", new BigDecimal("1200.00"), "Electronics", "http://example.com");
        dummyProduct.setId(10L);

        Mockito.when(repo.findById(10L)).thenReturn(Optional.of(dummyProduct));

        ProductDto result = service.getProductById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(new BigDecimal("1200.00"), result.getPrice());
    }

    @Test
    void testGetProductById_NotFound() {
        ProductRepository repo = Mockito.mock(ProductRepository.class);
        ProductService service = new ProductService(repo);

        Mockito.when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getProductById(99L));
    }
}

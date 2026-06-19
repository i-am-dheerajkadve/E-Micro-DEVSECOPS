package com.example.product.service;

import com.example.product.dto.ProductDto;
import com.example.product.model.Product;
import com.example.product.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void initSampleData() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Modern Wireless Headphones", "Noise cancelling over-ear bluetooth headphones.", new BigDecimal("199.99"), "Electronics", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500"));
            productRepository.save(new Product("Minimalist Leather Watch", "Classic design with genuine brown leather strap.", new BigDecimal("129.50"), "Accessories", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500"));
            productRepository.save(new Product("Ergonomic Mechanical Keyboard", "Tactile switches with customizable RGB lighting.", new BigDecimal("89.99"), "Electronics", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500"));
            productRepository.save(new Product("Organic Cotton Hoodie", "Eco-friendly, ultra-soft unisex pullover hoodie.", new BigDecimal("59.95"), "Apparel", "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500"));
            productRepository.save(new Product("Stainless Steel Water Bottle", "Double-walled vacuum insulated, keeps drinks cold for 24h.", new BigDecimal("24.99"), "Accessories", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500"));
        }
    }

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDto(product);
    }

    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategory(category).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ProductDto> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProductDto createProduct(ProductDto productDto) {
        Product product = new Product(
                productDto.getName(),
                productDto.getDescription(),
                productDto.getPrice(),
                productDto.getCategory(),
                productDto.getImageUrl()
        );
        Product savedProduct = productRepository.save(product);
        return convertToDto(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setImageUrl(productDto.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        return convertToDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl()
        );
    }
}

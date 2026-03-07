package com.ratnakar.code.service;


import com.ratnakar.code.dto.ProductRequest;
import com.ratnakar.code.dto.ProductResponse;
import com.ratnakar.code.entity.Product;
import com.ratnakar.code.exception.ResourceNotFoundException;
import com.ratnakar.code.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    // ─────────────────────────────────────────────
    //  CREATE
    // ─────────────────────────────────────────────
    public ProductResponse addProduct(ProductRequest request) {
        Product product = Product.builder()
                .productName(request.getProductName())
                .productDetails(request.getProductDetails())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created with id={}", saved.getProductId());
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    //  READ ALL
    // ─────────────────────────────────────────────
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    //  READ ONE
    // ─────────────────────────────────────────────
    public ProductResponse getProductById(Long id) {
        Product product = findOrThrow(id);
        return toResponse(product);
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findOrThrow(id);

        product.setProductName(request.getProductName());
        product.setProductDetails(request.getProductDetails());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUpdatedAt(LocalDateTime.now());

        Product updated = productRepository.save(product);
        log.info("Product updated with id={}", updated.getProductId());
        return toResponse(updated);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────
    public void deleteProduct(Long id) {
        Product product = findOrThrow(id);
        productRepository.delete(product);
        log.info("Product deleted with id={}", id);
    }

    // ─────────────────────────────────────────────
    //  HELPER: find entity or throw 404
    // ─────────────────────────────────────────────
    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
    }

    // ─────────────────────────────────────────────
    //  HELPER: entity → DTO
    // ─────────────────────────────────────────────
    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .productId(p.getProductId())
                .productName(p.getProductName())
                .productDetails(p.getProductDetails())
                .price(p.getPrice())
                .quantity(p.getQuantity())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}

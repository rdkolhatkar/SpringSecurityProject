package com.ratnakar.code.controller;


import com.ratnakar.code.dto.ApiResponse;
import com.ratnakar.code.dto.ProductRequest;
import com.ratnakar.code.dto.ProductResponse;
import com.ratnakar.code.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Products REST Controller
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  Method  │  URL                  │  Auth Required │  Action     │
 * ├──────────┼───────────────────────┼────────────────┼─────────────┤
 * │  GET     │  /api/products        │  No (public)   │  List all   │
 * │  GET     │  /api/products/{id}   │  No (public)   │  Get one    │
 * │  POST    │  /api/products        │  YES (Basic)   │  Add        │
 * │  PUT     │  /api/products/{id}   │  YES (Basic)   │  Update     │
 * │  DELETE  │  /api/products/{id}   │  YES (Basic)   │  Delete     │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * For protected endpoints, send credentials as:
 *   Authorization: Basic base64(username:password)
 *
 * If credentials are wrong or missing → 401 Unauthorized
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    // ─────────────────────────────────────────────
    //  GET /api/products  — PUBLIC
    //  Returns the full list of products
    // ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(
                ApiResponse.ok("Products fetched successfully", products));
    }

    // ─────────────────────────────────────────────
    //  GET /api/products/{id}  — PUBLIC
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {

        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Product fetched successfully", product));
    }

    // ─────────────────────────────────────────────
    //  POST /api/products  — PROTECTED (Basic Auth)
    //  Adds a new product to the DB
    // ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request) {

        ProductResponse created = productService.addProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product added successfully", created));
    }

    // ─────────────────────────────────────────────
    //  PUT /api/products/{id}  — PROTECTED (Basic Auth)
    //  Updates an existing product
    // ─────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        ProductResponse updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.ok("Product updated successfully", updated));
    }

    // ─────────────────────────────────────────────
    //  DELETE /api/products/{id}  — PROTECTED (Basic Auth)
    //  Removes a product by id
    // ─────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Product deleted successfully"));
    }
}

package com.infologic.pos.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infologic.pos.dto.ProductDTO;
import com.infologic.pos.model.Product;
import com.infologic.pos.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product Management API")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves a paginated list of all products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "401", description = "Not authenticated"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<Product>> getAllProducts(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all Products");
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> getProduct(
            @Parameter(description = "ID of the product to retrieve") @PathVariable Long id) {
        log.debug("REST request to get Product : {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Create a product", description = "Creates a new product with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.debug("REST request to save Product : {}", productDTO);
        Product result = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Update product", description = "Updates an existing product with new information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.debug("REST request to update Product : {}, {}", id, productDTO);
        Product result = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete product", description = "Deletes a product by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID of the product to delete") @PathVariable Long id) {
        log.debug("REST request to delete Product : {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Retrieves products filtered by the specified category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    })
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "Category name to filter by") @PathVariable String category,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get Products by category : {}", category);
        Page<Product> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches for products based on the provided query string")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results")
    })
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "Search query string") @RequestParam String query,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to search Products with query : {}", query);
        Page<Product> products = productService.searchProductsByName(query, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Get low stock products", description = "Retrieves products with stock quantity below the specified threshold")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<java.util.List<Product>> getLowStockProducts(
            @Parameter(description = "Stock threshold (default: 10)") @RequestParam(defaultValue = "10") Integer threshold) {
        log.debug("REST request to get low stock Products with threshold : {}", threshold);
        java.util.List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }
    
    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Update product stock", description = "Updates the stock quantity of a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Product> updateStock(
            @Parameter(description = "ID of the product to update") @PathVariable Long id,
            @Parameter(description = "Quantity to add (positive) or remove (negative)") @RequestParam Integer quantity) {
        log.debug("REST request to update Product stock : {}, {}", id, quantity);
        Product result = productService.updateStock(id, quantity);
        return ResponseEntity.ok(result);
    }
} 
package com.infologic.pos.controller;

import com.infologic.pos.model.dto.ProductDTO;
import com.infologic.pos.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin
public class ProductController {
    
    private final ProductService productService;
    
    // For simplicity, we're using a header for tenant identification
    // In a real app, this would come from authentication or a more secure mechanism
    private static final String TENANT_HEADER = "X-Tenant-ID";
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestHeader(TENANT_HEADER) String tenantId) {
        return ResponseEntity.ok(productService.getAllProducts(tenantId));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(tenantId, id));
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(tenantId, category));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestParam String query) {
        return ResponseEntity.ok(productService.searchProducts(tenantId, query));
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(tenantId, threshold));
    }
    
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestBody ProductDTO productDTO) {
        return new ResponseEntity<>(productService.createProduct(tenantId, productDTO), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable Long id,
            @RequestBody ProductDTO productDTO) {
        return ResponseEntity.ok(productService.updateProduct(tenantId, id, productDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable Long id) {
        productService.deleteProduct(tenantId, id);
        return ResponseEntity.noContent().build();
    }
} 
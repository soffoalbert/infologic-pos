package com.infologic.pos.service;

import com.infologic.pos.model.Product;
import com.infologic.pos.model.dto.ProductDTO;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    
    // Get all products for the current tenant
    List<ProductDTO> getAllProducts(String tenantId);
    
    // Get product by ID for the current tenant
    ProductDTO getProductById(String tenantId, Long id);
    
    // Get products by category for the current tenant
    List<ProductDTO> getProductsByCategory(String tenantId, String category);
    
    // Search products by name or SKU for the current tenant
    List<ProductDTO> searchProducts(String tenantId, String query);
    
    // Create a new product for the current tenant
    ProductDTO createProduct(String tenantId, ProductDTO productDTO);
    
    // Update an existing product for the current tenant
    ProductDTO updateProduct(String tenantId, Long id, ProductDTO productDTO);
    
    // Delete a product for the current tenant
    void deleteProduct(String tenantId, Long id);
    
    // Get products with low stock for the current tenant
    List<ProductDTO> getLowStockProducts(String tenantId, Integer threshold);
    
    // Update product stock (used by sales process)
    void updateProductStock(String tenantId, Long productId, Integer quantitySold);
} 
package com.infologic.pos.repository;

import com.infologic.pos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find all products for a specific tenant
    List<Product> findByTenantId(String tenantId);
    
    // Find products by category for a specific tenant
    List<Product> findByTenantIdAndCategory(String tenantId, String category);
    
    // Find a product by SKU for a specific tenant
    Optional<Product> findByTenantIdAndSku(String tenantId, String sku);
    
    // Find products with stock below a given threshold for a specific tenant
    List<Product> findByTenantIdAndStockLessThan(String tenantId, Integer threshold);
    
    // Search products by name or SKU for a specific tenant
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("tenantId") String tenantId, @Param("query") String query);
} 
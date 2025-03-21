package com.infologic.pos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.infologic.pos.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findByIdAndTenantId(Long id, String tenantId);
    
    Page<Product> findByTenantId(String tenantId, Pageable pageable);
    
    List<Product> findByTenantIdOrderByNameAsc(String tenantId);
    
    Page<Product> findByNameContainingAndTenantId(String name, String tenantId, Pageable pageable);
    
    Page<Product> findByCategoryAndTenantId(String category, String tenantId, Pageable pageable);
    
    List<Product> findByCategoryAndTenantIdOrderByNameAsc(String category, String tenantId);
    
    Optional<Product> findBySkuAndTenantId(String sku, String tenantId);
    
    Optional<Product> findByBarcodeAndTenantId(String barcode, String tenantId);
    
    List<Product> findByStockQuantityLessThanAndTenantId(Integer threshold, String tenantId);
    
    boolean existsBySkuAndTenantId(String sku, String tenantId);
    
    boolean existsByBarcodeAndTenantId(String barcode, String tenantId);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "(p.name LIKE %:query% OR p.sku LIKE %:query% OR p.barcode LIKE %:query% OR p.category LIKE %:query%)")
    List<Product> searchProducts(@Param("tenantId") String tenantId, @Param("query") String query);
    
    @Query("SELECT p FROM Product p WHERE p.tenantId = :tenantId AND " +
           "p.stockQuantity <= p.alertThreshold AND p.stockQuantity > 0")
    List<Product> findProductsBelowAlertThreshold(@Param("tenantId") String tenantId);
} 
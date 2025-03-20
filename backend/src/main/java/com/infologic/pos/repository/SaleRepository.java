package com.infologic.pos.repository;

import com.infologic.pos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    // Find all sales for a specific tenant
    List<Sale> findByTenantId(String tenantId);
    
    // Find sales by status for a specific tenant
    List<Sale> findByTenantIdAndStatus(String tenantId, Sale.SaleStatus status);
    
    // Find sales within a date range for a specific tenant
    List<Sale> findByTenantIdAndTimestampBetween(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find sales by user for a specific tenant
    List<Sale> findByTenantIdAndUserId(String tenantId, Long userId);
    
    // Find a sale by client reference ID for a specific tenant (for offline sync)
    Optional<Sale> findByTenantIdAndClientReferenceId(String tenantId, String clientReferenceId);
    
    // Get sales report with daily totals for a specific tenant
    @Query("SELECT DATE(s.timestamp) as date, COUNT(s) as count, SUM(s.total) as total " +
           "FROM Sale s WHERE s.tenantId = :tenantId AND s.timestamp BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(s.timestamp) ORDER BY DATE(s.timestamp)")
    List<Object[]> getSalesDailyReport(@Param("tenantId") String tenantId, 
                                      @Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
} 
package com.infologic.pos.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.infologic.pos.model.Sale;
import com.infologic.pos.model.Sale.SaleStatus;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    Optional<Sale> findByIdAndTenantId(Long id, String tenantId);
    
    Optional<Sale> findByInvoiceNumberAndTenantId(String invoiceNumber, String tenantId);
    
    Optional<Sale> findByTenantIdAndClientReferenceId(String tenantId, String clientReferenceId);
    
    Page<Sale> findByTenantId(String tenantId, Pageable pageable);
    
    List<Sale> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    
    Page<Sale> findByStatusAndTenantId(SaleStatus status, String tenantId, Pageable pageable);
    
    List<Sale> findByStatusAndTenantIdOrderByCreatedAtDesc(SaleStatus status, String tenantId);
    
    Page<Sale> findByCashierIdAndTenantId(Long cashierId, String tenantId, Pageable pageable);
    
    Page<Sale> findByCreatedAtBetweenAndTenantId(LocalDateTime startDate, LocalDateTime endDate, String tenantId, Pageable pageable);
    
    List<Sale> findByCreatedAtBetweenAndTenantIdOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate, String tenantId);
    
    @Query("SELECT s FROM Sale s WHERE s.customerName LIKE %:search% OR s.customerPhone LIKE %:search% OR s.invoiceNumber LIKE %:search% AND s.tenantId = :tenantId")
    Page<Sale> searchSales(@Param("search") String search, @Param("tenantId") String tenantId, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate AND s.tenantId = :tenantId")
    Long countSalesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);
    
    @Query("SELECT CAST(s.createdAt AS DATE) as date, COUNT(s) as count, SUM(s.totalAmount) as total " +
           "FROM Sale s WHERE s.tenantId = :tenantId AND s.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(s.createdAt AS DATE) ORDER BY date")
    List<Object[]> getSalesDailyReport(@Param("tenantId") String tenantId, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate AND s.tenantId = :tenantId AND s.status = 'COMPLETED'")
    Double getTotalSalesRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("tenantId") String tenantId);
} 
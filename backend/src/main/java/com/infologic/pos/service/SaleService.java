package com.infologic.pos.service;

import com.infologic.pos.model.dto.SaleDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SaleService {
    
    // Create a new sale
    SaleDTO createSale(String tenantId, Long userId, SaleDTO saleDTO);
    
    // Get a sale by ID
    SaleDTO getSaleById(String tenantId, Long id);
    
    // Get all sales for the current tenant
    List<SaleDTO> getAllSales(String tenantId);
    
    // Get sales for a date range
    List<SaleDTO> getSalesByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Get sales by status
    List<SaleDTO> getSalesByStatus(String tenantId, String status);
    
    // Update sale status (for refunds, voids, etc.)
    SaleDTO updateSaleStatus(String tenantId, Long id, String status);
    
    // Sync offline sales
    List<SaleDTO> syncOfflineSales(String tenantId, Long userId, List<SaleDTO> offlineSales);
    
    // Get sales report with daily totals
    List<Map<String, Object>> getSalesDailyReport(String tenantId, LocalDateTime startDate, LocalDateTime endDate);
} 
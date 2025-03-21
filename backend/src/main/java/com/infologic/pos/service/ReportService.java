package com.infologic.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.model.Product;
import com.infologic.pos.model.Sale;
import com.infologic.pos.repository.ProductRepository;
import com.infologic.pos.repository.SaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    
    /**
     * Get a sales summary report for a date range
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the sales summary
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesSummary(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Generating sales summary for tenant {} between {} and {}", tenantId, startDate, endDate);
        
        Long saleCount = saleRepository.countSalesByDateRange(startDate, endDate, tenantId);
        Double totalRevenue = saleRepository.getTotalSalesRevenue(startDate, endDate, tenantId);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        summary.put("saleCount", saleCount);
        summary.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        
        return summary;
    }
    
    /**
     * Get inventory status report
     *
     * @param lowStockThreshold the threshold for low stock
     * @return the inventory status
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getInventoryStatus(Integer lowStockThreshold) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Generating inventory status for tenant {} with low stock threshold {}", tenantId, lowStockThreshold);
        
        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThanAndTenantId(lowStockThreshold, tenantId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("timestamp", LocalDateTime.now());
        status.put("lowStockThreshold", lowStockThreshold);
        status.put("lowStockCount", lowStockProducts.size());
        status.put("lowStockProducts", lowStockProducts);
        
        return status;
    }
    
    /**
     * Calculate sales statistics by payment method
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the payment method statistics
     */
    @Transactional(readOnly = true)
    public Map<Sale.PaymentMethod, BigDecimal> getSalesByPaymentMethod(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Calculating sales by payment method for tenant {} between {} and {}", tenantId, startDate, endDate);
        
        Map<Sale.PaymentMethod, BigDecimal> paymentMethodStats = new HashMap<>();
        
        // Get all sales in the date range
        List<Sale> sales = saleRepository.findByCreatedAtBetweenAndTenantId(startDate, endDate, tenantId, null).getContent();
        
        // Calculate totals by payment method
        for (Sale sale : sales) {
            if (sale.getStatus() == Sale.SaleStatus.COMPLETED) {
                Sale.PaymentMethod paymentMethod = sale.getPaymentMethod();
                BigDecimal currentAmount = paymentMethodStats.getOrDefault(paymentMethod, BigDecimal.ZERO);
                paymentMethodStats.put(paymentMethod, currentAmount.add(sale.getTotalAmount()));
            }
        }
        
        return paymentMethodStats;
    }
} 
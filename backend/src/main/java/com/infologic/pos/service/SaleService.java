package com.infologic.pos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.dto.SaleDTO;
import com.infologic.pos.dto.SaleItemDTO;
import com.infologic.pos.event.SaleEvent;
import com.infologic.pos.event.SaleEvent.SaleEventType;
import com.infologic.pos.exception.ResourceNotFoundException;
import com.infologic.pos.model.Product;
import com.infologic.pos.model.Sale;
import com.infologic.pos.model.SaleItem;
import com.infologic.pos.repository.ProductRepository;
import com.infologic.pos.repository.SaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final EventPublisherService eventPublisher;

    /**
     * Create a new sale
     *
     * @param saleDTO the sale DTO
     * @return the created sale
     */
    @Transactional
    public Sale createSale(SaleDTO saleDTO) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating new sale for tenant {}", tenantId);

        // Generate a unique invoice number
        String invoiceNumber = generateInvoiceNumber();

        // Create the sale
        Sale sale = Sale.builder()
                .invoiceNumber(invoiceNumber)
                .customerName(saleDTO.getCustomerName())
                .customerPhone(saleDTO.getCustomerPhone())
                .customerEmail(saleDTO.getCustomerEmail())
                .totalAmount(saleDTO.getTotalAmount())
                .taxAmount(saleDTO.getTaxAmount())
                .discountAmount(saleDTO.getDiscountAmount())
                .paymentMethod(saleDTO.getPaymentMethod())
                .paymentReference(saleDTO.getPaymentReference())
                .status(saleDTO.getStatus())
                .notes(saleDTO.getNotes())
                .cashierId(saleDTO.getCashierId())
                .tenantId(tenantId)
                .items(new HashSet<>())
                .build();

        // Add sale items
        if (saleDTO.getItems() != null && !saleDTO.getItems().isEmpty()) {
            for (SaleItemDTO itemDTO : saleDTO.getItems()) {
                Product product = productRepository.findByIdAndTenantId(itemDTO.getProductId(), tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDTO.getProductId()));

                // Create the sale item
                SaleItem saleItem = SaleItem.builder()
                        .sale(sale)
                        .product(product)
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .discountAmount(itemDTO.getDiscountAmount())
                        .taxAmount(itemDTO.getTaxAmount())
                        .subtotal(itemDTO.getSubtotal())
                        .notes(itemDTO.getNotes())
                        .tenantId(tenantId)
                        .build();

                sale.getItems().add(saleItem);

                // Update product stock
                productService.updateStock(product.getId(), -itemDTO.getQuantity());
            }
        }

        Sale savedSale = saleRepository.save(sale);
        
        // Publish sale created event
        eventPublisher.publishSaleEvent(new SaleEvent(
            tenantId,
            "system",
            savedSale,
            SaleEventType.CREATED
        ));
        
        return savedSale;
    }
    
    /**
     * Create a sale with specific user ID (used for offline sync)
     * 
     * @param tenantId tenant ID
     * @param userId user/cashier ID
     * @param saleDTO sale data
     * @return the created sale
     */
    @Transactional
    public Sale createSale(String tenantId, Long userId, SaleDTO saleDTO) {
        log.info("Creating sale for tenant {} by user {}", tenantId, userId);
        
        // Set the tenant context
        TenantContext.setCurrentTenant(tenantId);
        
        try {
            // Set default values if not provided
            if (saleDTO.getTimestamp() == null) {
                saleDTO.setTimestamp(LocalDateTime.now());
            }
            
            if (saleDTO.getStatus() == null) {
                saleDTO.setStatus(Sale.SaleStatus.COMPLETED);
            }
            
            // Override the cashier ID with the provided user ID
            saleDTO.setCashierId(userId);
            
            return createSale(saleDTO);
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }

    /**
     * Get a sale by ID
     *
     * @param id the sale ID
     * @return the sale
     */
    @Transactional(readOnly = true)
    public Sale getSaleById(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sale with ID {} for tenant {}", id, tenantId);

        return saleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
    }

    /**
     * Get a sale by invoice number
     *
     * @param invoiceNumber the invoice number
     * @return the sale
     */
    @Transactional(readOnly = true)
    public Sale getSaleByInvoiceNumber(String invoiceNumber) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sale with invoice number {} for tenant {}", invoiceNumber, tenantId);

        return saleRepository.findByInvoiceNumberAndTenantId(invoiceNumber, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with invoice number: " + invoiceNumber));
    }

    /**
     * Get all sales
     *
     * @param pageable pagination information
     * @return page of sales
     */
    @Transactional(readOnly = true)
    public Page<Sale> getAllSales(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting all sales for tenant {}", tenantId);

        return saleRepository.findByTenantId(tenantId, pageable);
    }
    
    /**
     * Get all sales (without pagination)
     * 
     * @return list of all sales
     */
    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting all sales (unpaginated) for tenant {}", tenantId);
        
        return saleRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    /**
     * Get sales by status
     *
     * @param status   the status
     * @param pageable pagination information
     * @return page of sales
     */
    @Transactional(readOnly = true)
    public Page<Sale> getSalesByStatus(Sale.SaleStatus status, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sales by status {} for tenant {}", status, tenantId);

        return saleRepository.findByStatusAndTenantId(status, tenantId, pageable);
    }
    
    /**
     * Get sales by status (without pagination)
     * 
     * @param status the sale status
     * @return list of sales with the given status
     */
    @Transactional(readOnly = true)
    public List<Sale> getSalesByStatus(Sale.SaleStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sales by status {} for tenant {}", status, tenantId);
        
        return saleRepository.findByStatusAndTenantIdOrderByCreatedAtDesc(status, tenantId);
    }

    /**
     * Get sales by cashier
     *
     * @param cashierId the cashier ID
     * @param pageable  pagination information
     * @return page of sales
     */
    @Transactional(readOnly = true)
    public Page<Sale> getSalesByCashier(Long cashierId, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sales by cashier ID {} for tenant {}", cashierId, tenantId);

        return saleRepository.findByCashierIdAndTenantId(cashierId, tenantId, pageable);
    }

    /**
     * Get sales by date range
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @param pageable  pagination information
     * @return page of sales
     */
    @Transactional(readOnly = true)
    public Page<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sales between {} and {} for tenant {}", startDate, endDate, tenantId);

        return saleRepository.findByCreatedAtBetweenAndTenantId(startDate, endDate, tenantId, pageable);
    }
    
    /**
     * Get sales by date range (without pagination)
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of sales in the date range
     */
    @Transactional(readOnly = true)
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sales between {} and {} for tenant {}", startDate, endDate, tenantId);
        
        return saleRepository.findByCreatedAtBetweenAndTenantIdOrderByCreatedAtDesc(startDate, endDate, tenantId);
    }

    /**
     * Search sales
     *
     * @param search   the search term
     * @param pageable pagination information
     * @return page of sales
     */
    @Transactional(readOnly = true)
    public Page<Sale> searchSales(String search, Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Searching sales with term '{}' for tenant {}", search, tenantId);

        return saleRepository.searchSales(search, tenantId, pageable);
    }

    /**
     * Update sale status
     *
     * @param id     the sale ID
     * @param status the new status
     * @return the updated sale
     */
    @Transactional
    public Sale updateSaleStatus(Long id, Sale.SaleStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating status of sale with ID {} to {} for tenant {}", id, status, tenantId);

        Sale sale = saleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));

        Sale.SaleStatus oldStatus = sale.getStatus();
        
        // If canceling or refunding a completed sale, restore the inventory
        if ((status == Sale.SaleStatus.CANCELLED || status == Sale.SaleStatus.REFUNDED) && 
            sale.getStatus() == Sale.SaleStatus.COMPLETED) {
            for (SaleItem item : sale.getItems()) {
                productService.updateStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        sale.setStatus(status);
        Sale updatedSale = saleRepository.save(sale);
        
        // Determine the event type based on the status change
        SaleEventType eventType;
        switch (status) {
            case COMPLETED:
                eventType = SaleEventType.PAYMENT_COMPLETED;
                break;
            case PENDING:
                eventType = SaleEventType.UPDATED;
                break;
            case CANCELLED:
                eventType = SaleEventType.CANCELED;
                break;
            case REFUNDED:
                eventType = SaleEventType.PAYMENT_COMPLETED; // We'll consider this a payment operation
                break;
            default:
                eventType = SaleEventType.UPDATED;
                break;
        }
        
        // Publish sale status update event
        eventPublisher.publishSaleEvent(new SaleEvent(
            tenantId,
            "system",
            updatedSale,
            eventType
        ));
        
        return updatedSale;
    }

    /**
     * Get total sales revenue for a date range
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the total revenue
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSalesRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting total sales revenue between {} and {} for tenant {}", startDate, endDate, tenantId);

        Double revenue = saleRepository.getTotalSalesRevenue(startDate, endDate, tenantId);
        return revenue != null ? BigDecimal.valueOf(revenue) : BigDecimal.ZERO;
    }

    /**
     * Get sale count for a date range
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return the sale count
     */
    @Transactional(readOnly = true)
    public Long getSaleCount(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting sale count between {} and {} for tenant {}", startDate, endDate, tenantId);

        return saleRepository.countSalesByDateRange(startDate, endDate, tenantId);
    }
    
    /**
     * Get sales data grouped by day for reporting
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of maps with date, count, and total information
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSalesDailyReport(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Getting daily sales report between {} and {} for tenant {}", startDate, endDate, tenantId);
        
        List<Object[]> reportData = saleRepository.getSalesDailyReport(tenantId, startDate, endDate);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : reportData) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("count", row[1]);
            item.put("total", row[2]);
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * Synchronize sales data from offline devices
     * 
     * @param tenantId the tenant ID
     * @param userId the user/cashier ID
     * @param offlineSales list of offline sales to sync
     * @return list of processed sales
     */
    @Transactional
    public List<Sale> syncOfflineSales(String tenantId, Long userId, List<SaleDTO> offlineSales) {
        log.info("Syncing {} offline sales for tenant {} by user {}", offlineSales.size(), tenantId, userId);
        
        // Set the tenant context
        TenantContext.setCurrentTenant(tenantId);
        
        try {
            List<Sale> processedSales = new ArrayList<>();
            
            for (SaleDTO offlineSale : offlineSales) {
                // Check if this sale already exists (by client reference ID)
                if (offlineSale.getClientReferenceId() != null && 
                    saleRepository.findByTenantIdAndClientReferenceId(tenantId, offlineSale.getClientReferenceId()).isPresent()) {
                    // Sale already processed, skip it
                    log.debug("Sale with reference ID {} already exists, skipping", offlineSale.getClientReferenceId());
                    continue;
                }
                
                try {
                    // Create the sale
                    Sale processedSale = createSale(tenantId, userId, offlineSale);
                    processedSales.add(processedSale);
                    
                    // Publish sync event
                    eventPublisher.publishSaleEvent(new SaleEvent(
                        tenantId,
                        String.valueOf(userId),
                        processedSale,
                        SaleEventType.SYNCED
                    ));
                } catch (Exception e) {
                    // Log the error and continue with the next sale
                    log.error("Error processing offline sale: {}", e.getMessage(), e);
                }
            }
            
            return processedSales;
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }

    /**
     * Generate a unique invoice number
     *
     * @return the invoice number
     */
    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
} 
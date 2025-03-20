package com.infologic.pos.service.impl;

import com.infologic.pos.exception.ResourceNotFoundException;
import com.infologic.pos.model.Product;
import com.infologic.pos.model.Sale;
import com.infologic.pos.model.SaleItem;
import com.infologic.pos.model.dto.SaleDTO;
import com.infologic.pos.model.dto.SaleItemDTO;
import com.infologic.pos.repository.ProductRepository;
import com.infologic.pos.repository.SaleRepository;
import com.infologic.pos.service.ProductService;
import com.infologic.pos.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {
    
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    
    @Override
    @Transactional
    public SaleDTO createSale(String tenantId, Long userId, SaleDTO saleDTO) {
        // Set default values if not provided
        if (saleDTO.getTimestamp() == null) {
            saleDTO.setTimestamp(LocalDateTime.now());
        }
        
        if (saleDTO.getStatus() == null) {
            saleDTO.setStatus(Sale.SaleStatus.COMPLETED.name());
        }
        
        // Create the sale entity
        Sale sale = saleDTO.toEntity(userId, tenantId);
        
        // Process each sale item
        List<SaleItem> saleItems = new ArrayList<>();
        for (SaleItemDTO itemDTO : saleDTO.getItems()) {
            // Find the product
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));
            
            // Ensure the product belongs to the current tenant
            if (!product.getTenantId().equals(tenantId)) {
                throw new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId());
            }
            
            // Check if there's enough stock
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
            }
            
            // Create the sale item
            SaleItem saleItem = itemDTO.toEntity(sale, product);
            saleItems.add(saleItem);
            
            // Update product stock
            productService.updateProductStock(tenantId, product.getId(), itemDTO.getQuantity());
        }
        
        // Save the sale
        Sale savedSale = saleRepository.save(sale);
        
        // Set the items (bidirectional relationship)
        savedSale.setItems(saleItems);
        
        return SaleDTO.fromEntity(savedSale);
    }
    
    @Override
    public SaleDTO getSaleById(String tenantId, Long id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        
        // Ensure the sale belongs to the current tenant
        if (!sale.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Sale not found with id: " + id);
        }
        
        return SaleDTO.fromEntity(sale);
    }
    
    @Override
    public List<SaleDTO> getAllSales(String tenantId) {
        return saleRepository.findByTenantId(tenantId)
                .stream()
                .map(SaleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SaleDTO> getSalesByDateRange(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByTenantIdAndTimestampBetween(tenantId, startDate, endDate)
                .stream()
                .map(SaleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SaleDTO> getSalesByStatus(String tenantId, String status) {
        Sale.SaleStatus saleStatus = Sale.SaleStatus.valueOf(status);
        return saleRepository.findByTenantIdAndStatus(tenantId, saleStatus)
                .stream()
                .map(SaleDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public SaleDTO updateSaleStatus(String tenantId, Long id, String status) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
        
        // Ensure the sale belongs to the current tenant
        if (!sale.getTenantId().equals(tenantId)) {
            throw new ResourceNotFoundException("Sale not found with id: " + id);
        }
        
        // Validate and update the status
        Sale.SaleStatus newStatus = Sale.SaleStatus.valueOf(status);
        sale.setStatus(newStatus);
        
        // If refunding, add stock back to inventory
        if (newStatus == Sale.SaleStatus.REFUNDED || newStatus == Sale.SaleStatus.PARTIALLY_REFUNDED) {
            for (SaleItem item : sale.getItems()) {
                Product product = item.getProduct();
                // Only add stock back if the product still exists
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
        
        Sale updatedSale = saleRepository.save(sale);
        return SaleDTO.fromEntity(updatedSale);
    }
    
    @Override
    @Transactional
    public List<SaleDTO> syncOfflineSales(String tenantId, Long userId, List<SaleDTO> offlineSales) {
        List<SaleDTO> processedSales = new ArrayList<>();
        
        for (SaleDTO offlineSale : offlineSales) {
            // Check if this sale already exists (by client reference ID)
            if (offlineSale.getClientReferenceId() != null &&
                saleRepository.findByTenantIdAndClientReferenceId(tenantId, offlineSale.getClientReferenceId()).isPresent()) {
                // Sale already processed, skip it
                continue;
            }
            
            // Mark as created offline
            offlineSale.setOfflineCreated(true);
            
            try {
                // Process the sale
                SaleDTO processedSale = createSale(tenantId, userId, offlineSale);
                processedSales.add(processedSale);
            } catch (Exception e) {
                // Log the error and continue with the next sale
                System.err.println("Error processing offline sale: " + e.getMessage());
            }
        }
        
        return processedSales;
    }
    
    @Override
    public List<Map<String, Object>> getSalesDailyReport(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
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
} 
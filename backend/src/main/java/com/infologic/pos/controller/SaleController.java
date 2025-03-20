package com.infologic.pos.controller;

import com.infologic.pos.model.dto.SaleDTO;
import com.infologic.pos.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin
public class SaleController {
    
    private final SaleService saleService;
    
    // For simplicity, we're using headers for tenant and user identification
    // In a real app, these would come from authentication
    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String USER_ID_HEADER = "X-User-ID";
    
    @PostMapping
    public ResponseEntity<SaleDTO> createSale(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody SaleDTO saleDTO) {
        return new ResponseEntity<>(saleService.createSale(tenantId, userId, saleDTO), HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SaleDTO> getSaleById(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(tenantId, id));
    }
    
    @GetMapping
    public ResponseEntity<List<SaleDTO>> getAllSales(
            @RequestHeader(TENANT_HEADER) String tenantId) {
        return ResponseEntity.ok(saleService.getAllSales(tenantId));
    }
    
    @GetMapping("/by-date-range")
    public ResponseEntity<List<SaleDTO>> getSalesByDateRange(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(tenantId, startDate, endDate));
    }
    
    @GetMapping("/by-status")
    public ResponseEntity<List<SaleDTO>> getSalesByStatus(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestParam String status) {
        return ResponseEntity.ok(saleService.getSalesByStatus(tenantId, status));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<SaleDTO> updateSaleStatus(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(saleService.updateSaleStatus(tenantId, id, status));
    }
    
    @PostMapping("/sync-offline")
    public ResponseEntity<List<SaleDTO>> syncOfflineSales(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestBody List<SaleDTO> offlineSales) {
        return ResponseEntity.ok(saleService.syncOfflineSales(tenantId, userId, offlineSales));
    }
    
    @GetMapping("/reports/daily")
    public ResponseEntity<List<Map<String, Object>>> getSalesDailyReport(
            @RequestHeader(TENANT_HEADER) String tenantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(saleService.getSalesDailyReport(tenantId, startDate, endDate));
    }
} 
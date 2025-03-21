package com.infologic.pos.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infologic.pos.model.Sale;
import com.infologic.pos.service.ReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
@Tag(name = "Reports", description = "Reports and Analytics API")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {
    
    private final ReportService reportService;
    
    @GetMapping("/sales-summary")
    @Operation(summary = "Get sales summary", description = "Retrieves a summary of sales metrics for a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales summary"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("REST request to get sales summary between {} and {}", startDate, endDate);
        Map<String, Object> summary = reportService.getSalesSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/inventory-status")
    @Operation(summary = "Get inventory status", description = "Retrieves inventory status information including low stock products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory status"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Map<String, Object>> getInventoryStatus(
            @Parameter(description = "Threshold for low stock (default: 10)") 
            @RequestParam(defaultValue = "10") Integer lowStockThreshold) {
        log.debug("REST request to get inventory status with low stock threshold {}", lowStockThreshold);
        Map<String, Object> status = reportService.getInventoryStatus(lowStockThreshold);
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/payment-methods")
    @Operation(summary = "Get sales by payment method", description = "Calculates sales statistics grouped by payment method for a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved payment method statistics"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Map<Sale.PaymentMethod, BigDecimal>> getSalesByPaymentMethod(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("REST request to get sales by payment method between {} and {}", startDate, endDate);
        Map<Sale.PaymentMethod, BigDecimal> paymentMethodStats = reportService.getSalesByPaymentMethod(startDate, endDate);
        return ResponseEntity.ok(paymentMethodStats);
    }
} 
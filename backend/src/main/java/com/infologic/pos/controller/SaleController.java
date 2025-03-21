package com.infologic.pos.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.infologic.pos.dto.SaleDTO;
import com.infologic.pos.model.Sale;
import com.infologic.pos.model.Sale.SaleStatus;
import com.infologic.pos.service.SaleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales Management API")
@SecurityRequirement(name = "bearerAuth")
public class SaleController {
    
    private final SaleService saleService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Get all sales", description = "Retrieves a paginated list of all sales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales list"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<Sale>> getAllSales(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get all Sales");
        Page<Sale> sales = saleService.getAllSales(pageable);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Get sale by ID", description = "Retrieves a specific sale by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<Sale> getSale(
            @Parameter(description = "ID of the sale to retrieve") @PathVariable Long id) {
        log.debug("REST request to get Sale : {}", id);
        Sale sale = saleService.getSaleById(id);
        return ResponseEntity.ok(sale);
    }
    
    @GetMapping("/invoice/{invoiceNumber}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Get sale by invoice number", description = "Retrieves a specific sale by its invoice number")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<Sale> getSaleByInvoiceNumber(
            @Parameter(description = "Invoice number of the sale to retrieve") @PathVariable String invoiceNumber) {
        log.debug("REST request to get Sale by invoice number : {}", invoiceNumber);
        Sale sale = saleService.getSaleByInvoiceNumber(invoiceNumber);
        return ResponseEntity.ok(sale);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Create a sale", description = "Creates a new sale with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sale created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleDTO saleDTO) {
        log.debug("REST request to save Sale : {}", saleDTO);
        Sale result = saleService.createSale(saleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Update sale status", description = "Updates the status of an existing sale")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sale status updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role"),
        @ApiResponse(responseCode = "404", description = "Sale not found")
    })
    public ResponseEntity<Sale> updateSaleStatus(
            @Parameter(description = "ID of the sale to update") @PathVariable Long id,
            @Parameter(description = "New status for the sale") @RequestParam SaleStatus status) {
        log.debug("REST request to update Sale status : {}, {}", id, status);
        Sale result = saleService.updateSaleStatus(id, status);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Get sales by status", description = "Retrieves sales filtered by the specified status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<Sale>> getSalesByStatus(
            @Parameter(description = "Status to filter by") @PathVariable SaleStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get Sales by status : {}", status);
        Page<Sale> sales = saleService.getSalesByStatus(status, pageable);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/cashier/{cashierId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Get sales by cashier", description = "Retrieves sales made by a specific cashier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Page<Sale>> getSalesByCashier(
            @Parameter(description = "ID of the cashier") @PathVariable Long cashierId,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get Sales by cashier : {}", cashierId);
        Page<Sale> sales = saleService.getSalesByCashier(cashierId, pageable);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Get sales by date range", description = "Retrieves sales made within a specific date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Page<Sale>> getSalesByDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to get Sales by date range : {} to {}", startDate, endDate);
        Page<Sale> sales = saleService.getSalesByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR') or hasRole('ROLE_CASHIER')")
    @Operation(summary = "Search sales", description = "Searches for sales based on the provided query string")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Page<Sale>> searchSales(
            @Parameter(description = "Search query string") @RequestParam String query,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.debug("REST request to search Sales with query : {}", query);
        Page<Sale> sales = saleService.searchSales(query, pageable);
        return ResponseEntity.ok(sales);
    }
    
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Get total sales revenue", description = "Calculates the total revenue from sales within a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated revenue"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<BigDecimal> getTotalSalesRevenue(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("REST request to get total sales revenue by date range : {} to {}", startDate, endDate);
        BigDecimal revenue = saleService.getTotalSalesRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }
    
    @GetMapping("/count")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VENDOR')")
    @Operation(summary = "Get sale count", description = "Counts the number of sales within a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully counted sales"),
        @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN or VENDOR role")
    })
    public ResponseEntity<Long> getSaleCount(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.debug("REST request to get sale count by date range : {} to {}", startDate, endDate);
        Long count = saleService.getSaleCount(startDate, endDate);
        return ResponseEntity.ok(count);
    }
} 
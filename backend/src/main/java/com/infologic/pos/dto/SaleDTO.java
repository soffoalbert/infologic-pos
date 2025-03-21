package com.infologic.pos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.infologic.pos.model.Sale.PaymentMethod;
import com.infologic.pos.model.Sale.SaleStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for Sale information")
public class SaleDTO {
    
    @Schema(description = "Unique identifier of the sale", example = "1")
    private Long id;
    
    @Schema(description = "Unique invoice number", example = "INV-12345678")
    private String invoiceNumber;
    
    @Schema(description = "Name of the customer", example = "John Doe")
    private String customerName;
    
    @Schema(description = "Phone number of the customer", example = "+1234567890")
    private String customerPhone;
    
    @Schema(description = "Email address of the customer", example = "john.doe@example.com")
    private String customerEmail;
    
    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount must be at least 0")
    @Schema(description = "Total amount of the sale", example = "1299.99", required = true)
    private BigDecimal totalAmount;
    
    @Schema(description = "Tax amount for the sale", example = "129.99")
    private BigDecimal taxAmount;
    
    @Schema(description = "Discount amount for the sale", example = "50.00")
    private BigDecimal discountAmount;
    
    @NotNull(message = "Payment method is required")
    @Schema(description = "Method of payment", example = "CREDIT_CARD", required = true, 
            allowableValues = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "MOBILE_MONEY", "BANK_TRANSFER", "FLUTTERWAVE", "STRIPE", "MPESA"})
    private PaymentMethod paymentMethod;
    
    @Schema(description = "Reference number for the payment", example = "TXN-987654321")
    private String paymentReference;
    
    @NotNull(message = "Status is required")
    @Schema(description = "Status of the sale", example = "COMPLETED", required = true,
            allowableValues = {"PENDING", "COMPLETED", "CANCELLED", "REFUNDED"})
    private SaleStatus status;
    
    @Schema(description = "Additional notes about the sale", example = "Customer requested express delivery")
    private String notes;
    
    @NotNull(message = "Cashier ID is required")
    @Schema(description = "ID of the cashier who processed the sale", example = "5", required = true)
    private Long cashierId;
    
    @Schema(description = "Timestamp of the sale", example = "2024-04-15T10:30:00")
    private LocalDateTime timestamp;
    
    @Valid
    @NotEmpty(message = "Sale must have at least one item")
    @Schema(description = "Items included in the sale")
    private List<SaleItemDTO> items;
    
    @Schema(description = "Client reference ID", example = "CL-12345")
    private String clientReferenceId;
} 
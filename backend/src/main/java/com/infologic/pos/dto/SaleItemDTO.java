package com.infologic.pos.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for Sale Item information")
public class SaleItemDTO {
    
    @Schema(description = "Unique identifier of the sale item", example = "1")
    private Long id;
    
    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product being sold", example = "42", required = true)
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Schema(description = "Quantity of the product being sold", example = "2", required = true, minimum = "1")
    private Integer quantity;
    
    @NotNull(message = "Unit price is required")
    @Min(value = 0, message = "Unit price must be at least 0")
    @Schema(description = "Price per unit of the product", example = "599.99", required = true)
    private BigDecimal unitPrice;
    
    @Schema(description = "Discount amount for this item", example = "25.00")
    private BigDecimal discountAmount;
    
    @Schema(description = "Tax amount for this item", example = "60.00")
    private BigDecimal taxAmount;
    
    @NotNull(message = "Subtotal is required")
    @Min(value = 0, message = "Subtotal must be at least 0")
    @Schema(description = "Total price for this item (quantity × unit price - discount + tax)", example = "1234.98", required = true)
    private BigDecimal subtotal;
    
    @Schema(description = "Additional notes about this sale item", example = "Customer requested gift wrapping")
    private String notes;
} 
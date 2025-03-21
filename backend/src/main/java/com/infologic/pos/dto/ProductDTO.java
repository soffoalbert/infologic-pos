package com.infologic.pos.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for Product information")
public class ProductDTO {
    
    @Schema(description = "Unique identifier of the product", example = "1")
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name cannot exceed 100 characters")
    @Schema(description = "Name of the product", example = "Samsung Galaxy S21", required = true)
    private String name;
    
    @Size(max = 500, message = "Product description cannot exceed 500 characters")
    @Schema(description = "Detailed description of the product", example = "Latest Samsung smartphone with 5G capability")
    private String description;
    
    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    @Schema(description = "Price of the product", example = "999.99", required = true)
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    @Schema(description = "Available quantity in stock", example = "50", required = true)
    private Integer stockQuantity;
    
    @Min(value = 0, message = "Alert threshold must be greater than or equal to 0")
    @Schema(description = "Alert threshold for stock", example = "10")
    private Integer alertThreshold;
    
    @Size(max = 50, message = "SKU cannot exceed 50 characters")
    @Schema(description = "Stock Keeping Unit - unique product identifier", example = "SAMS21-5G-128")
    private String sku;
    
    @Size(max = 50, message = "Barcode cannot exceed 50 characters")
    @Schema(description = "Barcode for scanning", example = "123456789012")
    private String barcode;
    
    @Schema(description = "Product category", example = "Electronics")
    private String category;
    
    @Schema(description = "URL to product image", example = "https://example.com/images/samsung-s21.jpg")
    private String imageUrl;
    
    @Schema(description = "Whether the product is active and available for sale", example = "true")
    private boolean active = true;
} 
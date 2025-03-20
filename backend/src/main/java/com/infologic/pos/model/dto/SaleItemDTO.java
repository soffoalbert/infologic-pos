package com.infologic.pos.model.dto;

import com.infologic.pos.model.Product;
import com.infologic.pos.model.Sale;
import com.infologic.pos.model.SaleItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    
    // Convert from Entity to DTO
    public static SaleItemDTO fromEntity(SaleItem saleItem) {
        SaleItemDTO dto = new SaleItemDTO();
        dto.setId(saleItem.getId());
        dto.setProductId(saleItem.getProduct().getId());
        dto.setProductName(saleItem.getProduct().getName());
        dto.setProductSku(saleItem.getProduct().getSku());
        dto.setQuantity(saleItem.getQuantity());
        dto.setUnitPrice(saleItem.getUnitPrice());
        dto.setSubtotal(saleItem.getSubtotal());
        return dto;
    }
    
    // Convert from DTO to Entity (requires Sale and Product objects)
    public SaleItem toEntity(Sale sale, Product product) {
        SaleItem saleItem = new SaleItem();
        saleItem.setId(this.id);
        saleItem.setSale(sale);
        saleItem.setProduct(product);
        saleItem.setQuantity(this.quantity);
        saleItem.setUnitPrice(this.unitPrice);
        // Subtotal will be calculated by @PrePersist/@PreUpdate
        return saleItem;
    }
} 
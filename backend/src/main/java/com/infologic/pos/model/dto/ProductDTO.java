package com.infologic.pos.model.dto;

import com.infologic.pos.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private String category;
    
    // Convert from Entity to DTO
    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setCategory(product.getCategory());
        return dto;
    }
    
    // Convert from DTO to Entity
    public Product toEntity(String tenantId) {
        Product product = new Product();
        product.setId(this.id);
        product.setName(this.name);
        product.setSku(this.sku);
        product.setPrice(this.price);
        product.setStock(this.stock);
        product.setCategory(this.category);
        product.setTenantId(tenantId);
        return product;
    }
} 
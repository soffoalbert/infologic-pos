package com.infologic.pos.model.dto;

import com.infologic.pos.model.Sale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleDTO {
    private Long id;
    private LocalDateTime createdAt;
    private BigDecimal total;
    private Sale.PaymentMethod paymentMethod;
    private String status;
    private List<SaleItemDTO> items = new ArrayList<>();
    private String paymentReference;
    private boolean offlineCreated;
    
    // Convert from Entity to DTO
    public static SaleDTO fromEntity(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setCreatedAt(sale.getCreatedAt());
        dto.setTotal(sale.getTotalAmount());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setStatus(sale.getStatus().name());
        dto.setPaymentReference(sale.getPaymentReference());
//        dto.setOfflineCreated(sale.isOfflineCreated());
        
        dto.setItems(sale.getItems().stream()
                .map(SaleItemDTO::fromEntity)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    // Convert from DTO to Entity
    public Sale toEntity(Long userId, String tenantId) {
        Sale sale = new Sale();
        sale.setId(this.id);
        sale.setCreatedAt(this.createdAt != null ? this.createdAt : LocalDateTime.now());
        sale.setTotalAmount(this.total);
        sale.setPaymentMethod(this.paymentMethod);
        sale.setStatus(Sale.SaleStatus.valueOf(this.status));
        sale.setPaymentReference(this.paymentReference);
//        sale.setOfflineCreated(this.offlineCreated);
//        sale.setUserId(userId);
        sale.setTenantId(tenantId);
        
        return sale;
    }
} 
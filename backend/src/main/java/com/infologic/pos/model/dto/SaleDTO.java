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
    private LocalDateTime timestamp;
    private BigDecimal total;
    private String paymentMethod;
    private String status;
    private List<SaleItemDTO> items = new ArrayList<>();
    private String clientReferenceId;
    private boolean offlineCreated;
    
    // Convert from Entity to DTO
    public static SaleDTO fromEntity(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setTimestamp(sale.getTimestamp());
        dto.setTotal(sale.getTotal());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setStatus(sale.getStatus().name());
        dto.setClientReferenceId(sale.getClientReferenceId());
        dto.setOfflineCreated(sale.isOfflineCreated());
        
        dto.setItems(sale.getItems().stream()
                .map(SaleItemDTO::fromEntity)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    // Convert from DTO to Entity
    public Sale toEntity(Long userId, String tenantId) {
        Sale sale = new Sale();
        sale.setId(this.id);
        sale.setTimestamp(this.timestamp != null ? this.timestamp : LocalDateTime.now());
        sale.setTotal(this.total);
        sale.setPaymentMethod(this.paymentMethod);
        sale.setStatus(Sale.SaleStatus.valueOf(this.status));
        sale.setClientReferenceId(this.clientReferenceId);
        sale.setOfflineCreated(this.offlineCreated);
        sale.setUserId(userId);
        sale.setTenantId(tenantId);
        
        return sale;
    }
} 
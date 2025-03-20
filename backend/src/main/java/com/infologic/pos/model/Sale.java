package com.infologic.pos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(nullable = false)
    private String paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleStatus status;
    
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();
    
    // Reference to the user who processed the sale
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // Tenant identifier for multi-tenancy
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    // Flag to indicate if this sale was made offline
    @Column(nullable = false)
    private boolean offlineCreated = false;
    
    // Unique identifier generated on the client side for offline sales
    @Column(unique = true)
    private String clientReferenceId;
    
    public enum SaleStatus {
        COMPLETED, REFUNDED, PARTIALLY_REFUNDED, VOIDED
    }
}
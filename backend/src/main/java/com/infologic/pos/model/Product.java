package com.infologic.pos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "name")
    private String name;

    @Size(max = 500)
    @Column(name = "description")
    private String description;

    @NotNull
    @Min(0)
    @Column(name = "price")
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Min(0)
    @Column(name = "alert_threshold")
    private Integer alertThreshold = 5;

    @Size(max = 50)
    @Column(name = "sku")
    private String sku;

    @Size(max = 50)
    @Column(name = "barcode")
    private String barcode;

    @Column(name = "category")
    private String category;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "tenant_id")
    private String tenantId;
} 
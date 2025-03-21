package com.infologic.pos.event;

import com.infologic.pos.model.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event representing inventory-related operations like 
 * product creation, update, or stock changes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class InventoryEvent extends Event {
    
    private Product product;
    private Integer quantityChange;
    private InventoryEventType eventType;
    
    public InventoryEvent(String tenantId, String createdBy, Product product, 
                        Integer quantityChange, InventoryEventType eventType) {
        super(tenantId, createdBy);
        this.product = product;
        this.quantityChange = quantityChange;
        this.eventType = eventType;
    }
    
    public enum InventoryEventType {
        PRODUCT_CREATED,
        PRODUCT_UPDATED,
        STOCK_INCREASED,
        STOCK_DECREASED,
        STOCK_ALERT,
        OUT_OF_STOCK,
        DISCREPANCY_DETECTED,
        SYNCED
    }
} 
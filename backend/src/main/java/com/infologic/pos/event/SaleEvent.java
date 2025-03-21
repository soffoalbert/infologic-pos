package com.infologic.pos.event;

import com.infologic.pos.model.Sale;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event representing sales-related operations like creation, 
 * update, or processing of a sale.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SaleEvent extends Event {
    
    private Sale sale;
    private SaleEventType eventType;
    
    public SaleEvent(String tenantId, String createdBy, Sale sale, SaleEventType eventType) {
        super(tenantId, createdBy);
        this.sale = sale;
        this.eventType = eventType;
    }
    
    public enum SaleEventType {
        CREATED,
        UPDATED,
        PROCESSED,
        CANCELED,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        SYNCED
    }
} 
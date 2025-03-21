package com.infologic.pos.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Event representing payment-related operations like 
 * payment processing, completion, or failure.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PaymentEvent extends Event {
    
    private String saleId;
    private BigDecimal amount;
    private String paymentMethod;
    private String gatewayReference;
    private PaymentEventType eventType;
    
    public PaymentEvent(String tenantId, String createdBy, String saleId, 
                       BigDecimal amount, String paymentMethod, 
                       String gatewayReference, PaymentEventType eventType) {
        super(tenantId, createdBy);
        this.saleId = saleId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.gatewayReference = gatewayReference;
        this.eventType = eventType;
    }
    
    public enum PaymentEventType {
        PAYMENT_INITIATED,
        PAYMENT_PROCESSING,
        PAYMENT_COMPLETED,
        PAYMENT_FAILED,
        REFUND_INITIATED,
        REFUND_COMPLETED,
        REFUND_FAILED,
        SYNCED
    }
} 
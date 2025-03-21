package com.infologic.pos.examples;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Example class demonstrating Lombok features used in the InfoLogic POS project.
 * This is for reference only and not part of the production code.
 */
@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "sensitiveData")
@EqualsAndHashCode(of = {"id", "orderNumber"})
public class LombokExample {

    // Basic fields
    private Long id;
    private String orderNumber;
    
    // NonNull example - will generate null checks in setters and constructors
    @NonNull
    private String customerName;
    
    // Fields excluded from toString for security/privacy
    private String sensitiveData;
    
    // Using @Singular with collections in a builder
    @Singular
    private List<OrderItem> items;
    
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private PaymentStatus status;
    
    // Example of a method using the logger
    public void processOrder() {
        log.info("Processing order: {}", orderNumber);
        
        // Example code
        if (items.isEmpty()) {
            log.warn("Order {} has no items", orderNumber);
            return;
        }
        
        // Calculate total amount
        totalAmount = items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.debug("Order total calculated: {}", totalAmount);
        status = PaymentStatus.PROCESSED;
        
        log.info("Order {} processed successfully", orderNumber);
    }
    
    // Example of a nested class using Lombok
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private Long id;
        private String productName;
        private BigDecimal price;
        private int quantity;
        
        public BigDecimal getTotal() {
            return price.multiply(new BigDecimal(quantity));
        }
    }
    
    // Example enum
    public enum PaymentStatus {
        PENDING, PROCESSED, COMPLETED, FAILED
    }
    
    // Example of a service class using RequiredArgsConstructor
    @Slf4j
    @RequiredArgsConstructor
    public static class OrderService {
        // Final fields will be initialized through constructor
        private final OrderRepository repository;
        private final PaymentService paymentService;
        
        public void saveOrder(LombokExample order) {
            log.info("Saving order: {}", order.getOrderNumber());
            repository.save(order);
        }
        
        // Mock interfaces for the example
        interface OrderRepository {
            void save(LombokExample order);
        }
        
        interface PaymentService {
            void processPayment(LombokExample order);
        }
    }
} 
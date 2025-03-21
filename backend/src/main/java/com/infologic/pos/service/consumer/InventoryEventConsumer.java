package com.infologic.pos.service.consumer;

import com.infologic.pos.config.KafkaConfig;
import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.event.InventoryEvent;
import com.infologic.pos.model.Product;
import com.infologic.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer service for processing inventory events from Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final ProductRepository productRepository;

    /**
     * Processes inventory events from the inventory topic.
     *
     * @param event The inventory event to process
     * @param ack The acknowledgment to manually acknowledge the message
     */
    @KafkaListener(topics = KafkaConfig.INVENTORY_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeInventoryEvent(InventoryEvent event, Acknowledgment ack) {
        try {
            log.info("Consuming inventory event: {}", event);
            
            // Set the tenant context for multi-tenancy
            TenantContext.setCurrentTenant(event.getTenantId());
            
            // Process the event based on its type
            switch (event.getEventType()) {
                case PRODUCT_CREATED:
                    processProductCreated(event.getProduct());
                    break;
                case PRODUCT_UPDATED:
                    processProductUpdated(event.getProduct());
                    break;
                case STOCK_INCREASED:
                    processStockIncreased(event.getProduct(), event.getQuantityChange());
                    break;
                case STOCK_DECREASED:
                    processStockDecreased(event.getProduct(), event.getQuantityChange());
                    break;
                case STOCK_ALERT:
                    processStockAlert(event.getProduct());
                    break;
                case OUT_OF_STOCK:
                    processOutOfStock(event.getProduct());
                    break;
                case DISCREPANCY_DETECTED:
                    processDiscrepancyDetected(event.getProduct(), event.getQuantityChange());
                    break;
                case SYNCED:
                    processProductSynced(event.getProduct());
                    break;
                default:
                    log.warn("Unknown inventory event type: {}", event.getEventType());
            }
            
            // Acknowledge the message
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing inventory event: {}", event, e);
            // You could implement retry logic or dead-letter queue here
            // For now, just acknowledge to prevent redelivery
            ack.acknowledge();
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }
    
    /**
     * Processes a product creation event.
     */
    private void processProductCreated(Product product) {
        log.info("Processing product created: {}", product.getId());
        productRepository.save(product);
    }
    
    /**
     * Processes a product update event.
     */
    private void processProductUpdated(Product product) {
        log.info("Processing product updated: {}", product.getId());
        productRepository.findById(product.getId())
            .ifPresentOrElse(
                existingProduct -> {
                    // Update the existing product with the new data
                    existingProduct.setName(product.getName());
                    existingProduct.setDescription(product.getDescription());
                    existingProduct.setPrice(product.getPrice());
                    existingProduct.setBarcode(product.getBarcode());
                    existingProduct.setCategory(product.getCategory());
                    existingProduct.setUpdatedAt(product.getUpdatedAt());
                    // Don't overwrite stock level during a regular update
                    productRepository.save(existingProduct);
                },
                () -> {
                    // If the product doesn't exist, create it
                    log.warn("Product not found for update, creating: {}", product.getId());
                    productRepository.save(product);
                }
            );
    }
    
    /**
     * Processes a stock increase event.
     */
    private void processStockIncreased(Product product, Integer quantityChange) {
        log.info("Processing stock increased: {} by {}", product.getId(), quantityChange);
        productRepository.findById(product.getId())
            .ifPresentOrElse(
                existingProduct -> {
                    // Increase the stock level
                    int newStockLevel = existingProduct.getStockQuantity() + quantityChange;
                    existingProduct.setStockQuantity(newStockLevel);
                    existingProduct.setUpdatedAt(product.getUpdatedAt());
                    productRepository.save(existingProduct);
                    
                    // Check if the product is now back in stock
                    if (existingProduct.getStockQuantity() <= 0 && newStockLevel > 0) {
                        log.info("Product is back in stock: {}", product.getId());
                    }
                },
                () -> {
                    // If the product doesn't exist, create it
                    log.warn("Product not found for stock increase, creating: {}", product.getId());
                    productRepository.save(product);
                }
            );
    }
    
    /**
     * Processes a stock decrease event.
     */
    private void processStockDecreased(Product product, Integer quantityChange) {
        log.info("Processing stock decreased: {} by {}", product.getId(), quantityChange);
        productRepository.findById(product.getId())
            .ifPresentOrElse(
                existingProduct -> {
                    // Decrease the stock level
                    int newStockLevel = existingProduct.getStockQuantity() - quantityChange;
                    existingProduct.setStockQuantity(Math.max(0, newStockLevel)); // Don't go below zero
                    existingProduct.setUpdatedAt(product.getUpdatedAt());
                    productRepository.save(existingProduct);
                    
                    // Check if we need to send alerts
                    if (existingProduct.getAlertThreshold() != null && 
                        newStockLevel <= existingProduct.getAlertThreshold() && newStockLevel > 0) {
                        // Low stock alert
                        log.warn("Low stock alert for product: {}, quantity: {}", 
                                 product.getId(), newStockLevel);
                    } else if (newStockLevel <= 0) {
                        // Out of stock alert
                        log.warn("Out of stock alert for product: {}", product.getId());
                    }
                },
                () -> {
                    // If the product doesn't exist, create it
                    log.warn("Product not found for stock decrease, creating: {}", product.getId());
                    productRepository.save(product);
                }
            );
    }
    
    /**
     * Processes a stock alert event.
     */
    private void processStockAlert(Product product) {
        log.info("Processing stock alert: {}", product.getId());
        // This could send notifications, update product status, etc.
        log.warn("Low stock alert for product: {}, quantity: {}", 
                 product.getId(), product.getStockQuantity());
    }
    
    /**
     * Processes an out of stock event.
     */
    private void processOutOfStock(Product product) {
        log.info("Processing out of stock: {}", product.getId());
        // This could send notifications, update product status, etc.
        log.warn("Out of stock alert for product: {}", product.getId());
    }
    
    /**
     * Processes a discrepancy detection event.
     */
    private void processDiscrepancyDetected(Product product, Integer quantityDiff) {
        log.info("Processing discrepancy detection: {} with difference {}", 
                 product.getId(), quantityDiff);
        // This could log the discrepancy, update actual stock, notify managers, etc.
        productRepository.findById(product.getId())
            .ifPresent(existingProduct -> {
                // Record the discrepancy
                log.warn("Inventory discrepancy for product: {}, expected: {}, actual: {}, difference: {}", 
                         product.getId(), existingProduct.getStockQuantity(), 
                         product.getStockQuantity(), quantityDiff);
                
                // Update to the actual count
                existingProduct.setStockQuantity(product.getStockQuantity());
                existingProduct.setUpdatedAt(product.getUpdatedAt());
                productRepository.save(existingProduct);
            });
    }
    
    /**
     * Processes a product synced event from offline data.
     */
    private void processProductSynced(Product product) {
        log.info("Processing product synced: {}", product.getId());
        // Check if the product already exists
        productRepository.findById(product.getId())
            .ifPresentOrElse(
                existingProduct -> {
                    // For synced events, we implement last-write-wins strategy
                    // Compare the timestamps to determine which is the latest
                    if (product.getUpdatedAt().isAfter(existingProduct.getUpdatedAt())) {
                        log.info("Synced product is newer, updating: {}", product.getId());
                        // We need special handling for stock levels to prevent overwrites
                        // if multiple offline updates updated the same product
                        int stockDiff = product.getStockQuantity() - existingProduct.getStockQuantity();
                        // Update other fields
                        existingProduct.setName(product.getName());
                        existingProduct.setDescription(product.getDescription());
                        existingProduct.setPrice(product.getPrice());
                        existingProduct.setBarcode(product.getBarcode());
                        existingProduct.setCategory(product.getCategory());
                        existingProduct.setStockQuantity(product.getStockQuantity());
                        existingProduct.setAlertThreshold(product.getAlertThreshold());
                        existingProduct.setUpdatedAt(product.getUpdatedAt());
                        productRepository.save(existingProduct);
                        
                        // Log stock changes for auditing
                        if (stockDiff != 0) {
                            log.info("Stock level changed during sync for product {}: {} (diff: {})",
                                     product.getId(), product.getStockQuantity(), stockDiff);
                        }
                    } else {
                        log.info("Existing product is newer, ignoring sync: {}", product.getId());
                    }
                },
                () -> {
                    // If the product doesn't exist, create it
                    log.info("Synced product not found, creating: {}", product.getId());
                    productRepository.save(product);
                }
            );
    }
} 
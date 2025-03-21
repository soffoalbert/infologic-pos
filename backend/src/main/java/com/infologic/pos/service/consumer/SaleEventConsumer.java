package com.infologic.pos.service.consumer;

import com.infologic.pos.config.KafkaConfig;
import com.infologic.pos.config.tenant.TenantContext;
import com.infologic.pos.event.SaleEvent;
import com.infologic.pos.model.Sale;
import com.infologic.pos.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer service for processing sale events from Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SaleEventConsumer {

    private final SaleRepository saleRepository;

    /**
     * Processes sale events from the sales topic.
     *
     * @param event The sale event to process
     * @param ack The acknowledgment to manually acknowledge the message
     */
    @KafkaListener(topics = KafkaConfig.SALES_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeSaleEvent(SaleEvent event, Acknowledgment ack) {
        try {
            log.info("Consuming sale event: {}", event);
            
            // Set the tenant context for multi-tenancy
            TenantContext.setCurrentTenant(event.getTenantId());
            
            // Process the event based on its type
            switch (event.getEventType()) {
                case CREATED:
                    processSaleCreated(event.getSale());
                    break;
                case UPDATED:
                    processSaleUpdated(event.getSale());
                    break;
                case PROCESSED:
                    processSaleProcessed(event.getSale());
                    break;
                case CANCELED:
                    processSaleCanceled(event.getSale());
                    break;
                case PAYMENT_COMPLETED:
                    processSalePaymentCompleted(event.getSale());
                    break;
                case PAYMENT_FAILED:
                    processSalePaymentFailed(event.getSale());
                    break;
                case SYNCED:
                    processSaleSynced(event.getSale());
                    break;
                default:
                    log.warn("Unknown sale event type: {}", event.getEventType());
            }
            
            // Acknowledge the message
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error processing sale event: {}", event, e);
            // You could implement retry logic or dead-letter queue here
            // For now, just acknowledge to prevent redelivery
            ack.acknowledge();
        } finally {
            // Clear the tenant context
            TenantContext.clear();
        }
    }
    
    /**
     * Processes a sale creation event.
     */
    private void processSaleCreated(Sale sale) {
        log.info("Processing sale created: {}", sale.getId());
        saleRepository.save(sale);
    }
    
    /**
     * Processes a sale update event.
     */
    private void processSaleUpdated(Sale sale) {
        log.info("Processing sale updated: {}", sale.getId());
        saleRepository.findById(sale.getId())
            .ifPresentOrElse(
                existingSale -> {
                    // Update the existing sale with the new data
                    existingSale.setTotalAmount(sale.getTotalAmount());
                    existingSale.setStatus(sale.getStatus());
                    existingSale.setItems(sale.getItems());
                    existingSale.setPaymentMethod(sale.getPaymentMethod());
                    existingSale.setNotes(sale.getNotes());
                    existingSale.setUpdatedAt(sale.getUpdatedAt());
                    saleRepository.save(existingSale);
                },
                () -> {
                    // If the sale doesn't exist, create it
                    log.warn("Sale not found for update, creating: {}", sale.getId());
                    saleRepository.save(sale);
                }
            );
    }
    
    /**
     * Processes a sale processed event.
     */
    private void processSaleProcessed(Sale sale) {
        log.info("Processing sale processed: {}", sale.getId());
        // Similar logic to processSaleUpdated
        processSaleUpdated(sale);
    }
    
    /**
     * Processes a sale canceled event.
     */
    private void processSaleCanceled(Sale sale) {
        log.info("Processing sale canceled: {}", sale.getId());
        saleRepository.findById(sale.getId())
            .ifPresent(existingSale -> {
                existingSale.setStatus(Sale.SaleStatus.CANCELLED);
                existingSale.setUpdatedAt(sale.getUpdatedAt());
                saleRepository.save(existingSale);
            });
    }
    
    /**
     * Processes a sale payment completed event.
     */
    private void processSalePaymentCompleted(Sale sale) {
        log.info("Processing sale payment completed: {}", sale.getId());
        saleRepository.findById(sale.getId())
            .ifPresent(existingSale -> {
                existingSale.setStatus(Sale.SaleStatus.COMPLETED);
                existingSale.setPaymentMethod(sale.getPaymentMethod());
                existingSale.setPaymentReference(sale.getPaymentReference());
                existingSale.setUpdatedAt(sale.getUpdatedAt());
                saleRepository.save(existingSale);
            });
    }
    
    /**
     * Processes a sale payment failed event.
     */
    private void processSalePaymentFailed(Sale sale) {
        log.info("Processing sale payment failed: {}", sale.getId());
        saleRepository.findById(sale.getId())
            .ifPresent(existingSale -> {
                existingSale.setStatus(Sale.SaleStatus.PENDING);
                existingSale.setUpdatedAt(sale.getUpdatedAt());
                saleRepository.save(existingSale);
            });
    }
    
    /**
     * Processes a sale synced event from offline data.
     */
    private void processSaleSynced(Sale sale) {
        log.info("Processing sale synced: {}", sale.getId());
        // Check if the sale already exists
        saleRepository.findById(sale.getId())
            .ifPresentOrElse(
                existingSale -> {
                    // For synced events, we implement last-write-wins strategy
                    // Compare the timestamps to determine which is the latest
                    if (sale.getUpdatedAt().isAfter(existingSale.getUpdatedAt())) {
                        log.info("Synced sale is newer, updating: {}", sale.getId());
                        processSaleUpdated(sale);
                    } else {
                        log.info("Existing sale is newer, ignoring sync: {}", sale.getId());
                    }
                },
                () -> {
                    // If the sale doesn't exist, create it
                    log.info("Synced sale not found, creating: {}", sale.getId());
                    saleRepository.save(sale);
                }
            );
    }
} 
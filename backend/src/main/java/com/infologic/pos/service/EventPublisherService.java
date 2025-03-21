package com.infologic.pos.service;

import com.infologic.pos.config.KafkaConfig;
import com.infologic.pos.event.Event;
import com.infologic.pos.event.InventoryEvent;
import com.infologic.pos.event.PaymentEvent;
import com.infologic.pos.event.SaleEvent;
import com.infologic.pos.event.SyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service responsible for publishing events to Kafka topics.
 * This is a key component of the event-driven architecture.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a sale event to the sales topic.
     *
     * @param event The sale event to publish
     * @return A CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishSaleEvent(SaleEvent event) {
        log.info("Publishing sale event: {}", event);
        return kafkaTemplate.send(KafkaConfig.SALES_TOPIC, event.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sale event sent successfully: {}", event.getId());
                    } else {
                        log.error("Failed to send sale event: {}", event.getId(), ex);
                    }
                });
    }

    /**
     * Publishes an inventory event to the inventory topic.
     *
     * @param event The inventory event to publish
     * @return A CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishInventoryEvent(InventoryEvent event) {
        log.info("Publishing inventory event: {}", event);
        return kafkaTemplate.send(KafkaConfig.INVENTORY_TOPIC, event.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Inventory event sent successfully: {}", event.getId());
                    } else {
                        log.error("Failed to send inventory event: {}", event.getId(), ex);
                    }
                });
    }

    /**
     * Publishes a payment event to the payment topic.
     *
     * @param event The payment event to publish
     * @return A CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishPaymentEvent(PaymentEvent event) {
        log.info("Publishing payment event: {}", event);
        return kafkaTemplate.send(KafkaConfig.PAYMENT_TOPIC, event.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Payment event sent successfully: {}", event.getId());
                    } else {
                        log.error("Failed to send payment event: {}", event.getId(), ex);
                    }
                });
    }

    /**
     * Publishes a sync event to the sync topic.
     *
     * @param event The sync event to publish
     * @return A CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishSyncEvent(SyncEvent event) {
        log.info("Publishing sync event: {}", event);
        return kafkaTemplate.send(KafkaConfig.SYNC_TOPIC, event.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Sync event sent successfully: {}", event.getId());
                    } else {
                        log.error("Failed to send sync event: {}", event.getId(), ex);
                    }
                });
    }

    /**
     * Generic method to publish any event to a specific topic.
     *
     * @param topic The Kafka topic to publish to
     * @param event The event to publish
     * @return A CompletableFuture of the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishEvent(String topic, Event event) {
        log.info("Publishing event to topic {}: {}", topic, event);
        return kafkaTemplate.send(topic, event.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Event sent successfully to topic {}: {}", topic, event.getId());
                    } else {
                        log.error("Failed to send event to topic {}: {}", topic, event.getId(), ex);
                    }
                });
    }
} 
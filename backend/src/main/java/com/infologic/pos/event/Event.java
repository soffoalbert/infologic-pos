package com.infologic.pos.event;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all events in the system.
 * This follows the event-driven architecture pattern where events
 * are used to communicate between different parts of the application.
 */
@Data
@NoArgsConstructor
public abstract class Event {
    
    private String id = UUID.randomUUID().toString();
    private String tenantId;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String createdBy;
    
    public Event(String tenantId, String createdBy) {
        this.tenantId = tenantId;
        this.createdBy = createdBy;
    }
} 
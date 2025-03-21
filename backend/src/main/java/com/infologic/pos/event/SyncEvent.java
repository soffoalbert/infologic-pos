package com.infologic.pos.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event representing synchronization operations for offline data.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SyncEvent extends Event {
    
    private String deviceId;
    private String entityType; // e.g., "sale", "inventory", "payment"
    private String entityId;
    private LocalDateTime lastModified;
    private Map<String, Object> data;
    private SyncEventType eventType;
    private SyncStatus syncStatus;
    
    public SyncEvent(String tenantId, String createdBy, String deviceId, 
                   String entityType, String entityId, LocalDateTime lastModified,
                   Map<String, Object> data, SyncEventType eventType) {
        super(tenantId, createdBy);
        this.deviceId = deviceId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.lastModified = lastModified;
        this.data = data;
        this.eventType = eventType;
        this.syncStatus = SyncStatus.PENDING;
    }
    
    public enum SyncEventType {
        DATA_UPLOAD,
        DATA_DOWNLOAD,
        CONFLICT_DETECTED,
        CONFLICT_RESOLVED
    }
    
    public enum SyncStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CONFLICT
    }
} 
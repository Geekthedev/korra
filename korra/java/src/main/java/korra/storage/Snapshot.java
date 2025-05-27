package korra.storage;

import java.time.Instant;
import java.util.UUID;

/**
 * Snapshot of component state
 */
public class Snapshot {
    private final String snapshotId;
    private final String componentId;
    private final Instant timestamp;
    private final long size;
    
    /**
     * Create a new snapshot
     * 
     * @param componentId Component ID
     * @param timestamp Timestamp
     * @param size Size in bytes
     */
    public Snapshot(String componentId, Instant timestamp, long size) {
        this.snapshotId = UUID.randomUUID().toString();
        this.componentId = componentId;
        this.timestamp = timestamp;
        this.size = size;
    }
    
    /**
     * Get the snapshot ID
     * 
     * @return Snapshot ID
     */
    public String getSnapshotId() {
        return snapshotId;
    }
    
    /**
     * Get the component ID
     * 
     * @return Component ID
     */
    public String getComponentId() {
        return componentId;
    }
    
    /**
     * Get the timestamp
     * 
     * @return Timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the size
     * 
     * @return Size in bytes
     */
    public long getSize() {
        return size;
    }
    
    @Override
    public String toString() {
        return "Snapshot{" +
                "snapshotId='" + snapshotId + '\'' +
                ", componentId='" + componentId + '\'' +
                ", timestamp=" + timestamp +
                ", size=" + size +
                '}';
    }
}
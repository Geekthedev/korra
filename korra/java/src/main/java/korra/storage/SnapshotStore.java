package korra.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Distributed snapshot and rollback system
 */
public class SnapshotStore {
    private static final Logger LOGGER = Logger.getLogger(SnapshotStore.class.getName());
    
    private final ConcurrentHashMap<String, List<Snapshot>> snapshots;
    private String baseDirectory;
    
    /**
     * Create a new snapshot store
     */
    public SnapshotStore() {
        this.snapshots = new ConcurrentHashMap<>();
        this.baseDirectory = "snapshots";
    }
    
    /**
     * Initialize the snapshot store
     */
    public void initialize() {
        LOGGER.info("Initializing snapshot store");
        
        // Create base directory if it doesn't exist
        File dir = new File(baseDirectory);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                LOGGER.severe("Failed to create snapshot directory: " + baseDirectory);
            }
        }
    }
    
    /**
     * Set the base directory for snapshots
     * 
     * @param baseDirectory Base directory
     */
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    
    /**
     * Create a snapshot
     * 
     * @param componentId Component ID
     * @param data Snapshot data
     * @return Snapshot ID
     */
    public String createSnapshot(String componentId, byte[] data) {
        LOGGER.info("Creating snapshot for component: " + componentId);
        
        // Create snapshot
        Snapshot snapshot = new Snapshot(componentId, Instant.now(), data.length);
        
        // Add snapshot to list
        List<Snapshot> componentSnapshots = snapshots.computeIfAbsent(
            componentId, k -> new ArrayList<>()
        );
        componentSnapshots.add(snapshot);
        
        // Save snapshot data to file
        String filePath = getSnapshotFilePath(snapshot);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
            LOGGER.info("Snapshot saved to file: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("Failed to save snapshot: " + e.getMessage());
            componentSnapshots.remove(snapshot);
            return null;
        }
        
        return snapshot.getSnapshotId();
    }
    
    /**
     * Load a snapshot
     * 
     * @param snapshotId Snapshot ID
     * @return Snapshot data
     */
    public byte[] loadSnapshot(String snapshotId) {
        LOGGER.info("Loading snapshot: " + snapshotId);
        
        // Find the snapshot
        Snapshot snapshot = findSnapshot(snapshotId);
        if (snapshot == null) {
            LOGGER.warning("Snapshot not found: " + snapshotId);
            return null;
        }
        
        // Load snapshot data from file
        String filePath = getSnapshotFilePath(snapshot);
        File file = new File(filePath);
        if (!file.exists()) {
            LOGGER.warning("Snapshot file not found: " + filePath);
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            LOGGER.info("Snapshot loaded from file: " + filePath);
            return data;
        } catch (IOException e) {
            LOGGER.severe("Failed to load snapshot: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Delete a snapshot
     * 
     * @param snapshotId Snapshot ID
     * @return True if the snapshot was deleted successfully
     */
    public boolean deleteSnapshot(String snapshotId) {
        LOGGER.info("Deleting snapshot: " + snapshotId);
        
        // Find the snapshot
        Snapshot snapshot = findSnapshot(snapshotId);
        if (snapshot == null) {
            LOGGER.warning("Snapshot not found: " + snapshotId);
            return false;
        }
        
        // Remove snapshot from list
        List<Snapshot> componentSnapshots = snapshots.get(snapshot.getComponentId());
        if (componentSnapshots != null) {
            componentSnapshots.remove(snapshot);
        }
        
        // Delete snapshot file
        String filePath = getSnapshotFilePath(snapshot);
        File file = new File(filePath);
        if (file.exists() && !file.delete()) {
            LOGGER.warning("Failed to delete snapshot file: " + filePath);
        }
        
        return true;
    }
    
    /**
     * Get all snapshots for a component
     * 
     * @param componentId Component ID
     * @return List of snapshots
     */
    public List<Snapshot> getSnapshots(String componentId) {
        List<Snapshot> componentSnapshots = snapshots.get(componentId);
        return componentSnapshots != null ? 
            Collections.unmodifiableList(componentSnapshots) : Collections.emptyList();
    }
    
    /**
     * Find a snapshot by ID
     * 
     * @param snapshotId Snapshot ID
     * @return Snapshot, or null if not found
     */
    private Snapshot findSnapshot(String snapshotId) {
        for (List<Snapshot> componentSnapshots : snapshots.values()) {
            for (Snapshot snapshot : componentSnapshots) {
                if (snapshot.getSnapshotId().equals(snapshotId)) {
                    return snapshot;
                }
            }
        }
        return null;
    }
    
    /**
     * Get the file path for a snapshot
     * 
     * @param snapshot Snapshot
     * @return File path
     */
    private String getSnapshotFilePath(Snapshot snapshot) {
        return baseDirectory + File.separator + 
               snapshot.getComponentId() + File.separator + 
               snapshot.getSnapshotId() + ".snap";
    }
}
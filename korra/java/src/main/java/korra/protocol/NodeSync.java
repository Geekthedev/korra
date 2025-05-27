package korra.protocol;

import korra.core.Coordinator;
import korra.core.NodeInfo;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Handles synchronization between edge nodes
 */
public class NodeSync {
    private static final Logger LOGGER = Logger.getLogger(NodeSync.class.getName());
    
    private static final Duration HEARTBEAT_INTERVAL = Duration.ofSeconds(10);
    private static final Duration NODE_TIMEOUT = Duration.ofSeconds(30);
    
    private Coordinator coordinator;
    private ScheduledFuture<?> heartbeatTask;
    
    /**
     * Create a new node sync
     */
    public NodeSync() {
    }
    
    /**
     * Initialize the node sync
     * 
     * @param coordinator Coordinator instance
     */
    public void initialize(Coordinator coordinator) {
        LOGGER.info("Initializing node sync");
        this.coordinator = coordinator;
        
        // Start heartbeat task
        ScheduledExecutorService executor = coordinator.getExecutorService();
        heartbeatTask = executor.scheduleAtFixedRate(
            this::checkHeartbeats,
            HEARTBEAT_INTERVAL.toMillis(),
            HEARTBEAT_INTERVAL.toMillis(),
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Shutdown the node sync
     */
    public void shutdown() {
        LOGGER.info("Shutting down node sync");
        
        // Stop heartbeat task
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }
    
    /**
     * Notify that a node has joined
     * 
     * @param nodeInfo Information about the node
     */
    public void notifyNodeJoined(NodeInfo nodeInfo) {
        LOGGER.info("Node joined: " + nodeInfo.getNodeId());
        
        // In a real implementation, this would broadcast the node joined event to other nodes
    }
    
    /**
     * Notify that a node has left
     * 
     * @param nodeInfo Information about the node
     */
    public void notifyNodeLeft(NodeInfo nodeInfo) {
        LOGGER.info("Node left: " + nodeInfo.getNodeId());
        
        // In a real implementation, this would broadcast the node left event to other nodes
    }
    
    /**
     * Send a heartbeat to all nodes
     */
    private void sendHeartbeat() {
        LOGGER.fine("Sending heartbeat");
        
        // In a real implementation, this would send heartbeats to all connected nodes
    }
    
    /**
     * Check heartbeats from all nodes
     */
    private void checkHeartbeats() {
        LOGGER.fine("Checking heartbeats");
        
        // Get current time
        Instant now = Instant.now();
        
        // Check all connected nodes
        Map<String, NodeInfo> nodes = coordinator.getConnectedNodes();
        for (NodeInfo nodeInfo : nodes.values()) {
            // Skip self
            if (nodeInfo.getNodeId().equals(coordinator.getNodeId())) {
                continue;
            }
            
            // Check last heartbeat time
            Duration sinceLastHeartbeat = Duration.between(nodeInfo.getLastHeartbeat(), now);
            if (sinceLastHeartbeat.compareTo(NODE_TIMEOUT) > 0) {
                // Node timeout
                LOGGER.warning("Node timeout: " + nodeInfo.getNodeId());
                coordinator.unregisterNode(nodeInfo.getNodeId());
            }
        }
    }
    
    /**
     * Handle a heartbeat from a node
     * 
     * @param nodeId Node ID
     * @return True if the heartbeat was handled successfully
     */
    public boolean handleHeartbeat(String nodeId) {
        LOGGER.fine("Received heartbeat from node: " + nodeId);
        
        // Get node info
        Map<String, NodeInfo> nodes = coordinator.getConnectedNodes();
        NodeInfo nodeInfo = nodes.get(nodeId);
        if (nodeInfo == null) {
            LOGGER.warning("Received heartbeat from unknown node: " + nodeId);
            return false;
        }
        
        // Update last heartbeat time
        nodeInfo.updateHeartbeat();
        
        return true;
    }
}
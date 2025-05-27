package korra.core;

/**
 * Status of a KORRA node
 */
public enum NodeStatus {
    /**
     * Node is online and responding to heartbeats
     */
    ONLINE,
    
    /**
     * Node is online but not accepting new jobs
     */
    BUSY,
    
    /**
     * Node is not responding to heartbeats
     */
    UNRESPONSIVE,
    
    /**
     * Node is offline
     */
    OFFLINE,
    
    /**
     * Node is in an error state
     */
    ERROR
}
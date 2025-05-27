package korra.registry;

/**
 * Status of a KORRA agent
 */
public enum AgentStatus {
    /**
     * Agent is active and ready for execution
     */
    ACTIVE,
    
    /**
     * Agent is inactive and not available for execution
     */
    INACTIVE,
    
    /**
     * Agent is currently executing a job
     */
    EXECUTING,
    
    /**
     * Agent is in an error state
     */
    ERROR,
    
    /**
     * Agent is being updated
     */
    UPDATING
}
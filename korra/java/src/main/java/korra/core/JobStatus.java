package korra.core;

/**
 * Status of a KORRA job
 */
public enum JobStatus {
    /**
     * Job is pending execution
     */
    PENDING,
    
    /**
     * Job is currently running
     */
    RUNNING,
    
    /**
     * Job completed successfully
     */
    COMPLETED,
    
    /**
     * Job failed
     */
    FAILED,
    
    /**
     * Job was cancelled
     */
    CANCELLED,
    
    /**
     * Job timed out
     */
    TIMEOUT
}
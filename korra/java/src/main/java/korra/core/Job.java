package korra.core;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Job definition for KORRA
 */
public class Job {
    private final String jobId;
    private final String agentId;
    private final byte[] input;
    private final Map<String, String> metadata;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;
    private JobStatus status;
    private byte[] output;
    private String executedByNodeId;
    private String errorMessage;
    
    /**
     * Create a new job
     * 
     * @param agentId Agent ID to execute the job
     * @param input Input data for the job
     */
    public Job(String agentId, byte[] input) {
        this.jobId = UUID.randomUUID().toString();
        this.agentId = agentId;
        this.input = input.clone();
        this.metadata = new HashMap<>();
        this.createdAt = Instant.now();
        this.status = JobStatus.PENDING;
    }
    
    /**
     * Create a new job with a specific ID
     * 
     * @param jobId Job ID
     * @param agentId Agent ID to execute the job
     * @param input Input data for the job
     */
    public Job(String jobId, String agentId, byte[] input) {
        this.jobId = jobId;
        this.agentId = agentId;
        this.input = input.clone();
        this.metadata = new HashMap<>();
        this.createdAt = Instant.now();
        this.status = JobStatus.PENDING;
    }
    
    /**
     * Get the job ID
     * 
     * @return Job ID
     */
    public String getJobId() {
        return jobId;
    }
    
    /**
     * Get the agent ID
     * 
     * @return Agent ID
     */
    public String getAgentId() {
        return agentId;
    }
    
    /**
     * Get the input data
     * 
     * @return Input data
     */
    public byte[] getInput() {
        return input.clone();
    }
    
    /**
     * Get the job metadata
     * 
     * @return Map of metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Add metadata to the job
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    /**
     * Get the creation time
     * 
     * @return Creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Get the start time
     * 
     * @return Start time
     */
    public Instant getStartedAt() {
        return startedAt;
    }
    
    /**
     * Set the start time
     * 
     * @param startedAt Start time
     */
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
    
    /**
     * Get the completion time
     * 
     * @return Completion time
     */
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    /**
     * Set the completion time
     * 
     * @param completedAt Completion time
     */
    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
    
    /**
     * Get the job status
     * 
     * @return Job status
     */
    public JobStatus getStatus() {
        return status;
    }
    
    /**
     * Set the job status
     * 
     * @param status Job status
     */
    public void setStatus(JobStatus status) {
        this.status = status;
    }
    
    /**
     * Get the output data
     * 
     * @return Output data
     */
    public byte[] getOutput() {
        return output != null ? output.clone() : null;
    }
    
    /**
     * Set the output data
     * 
     * @param output Output data
     */
    public void setOutput(byte[] output) {
        this.output = output != null ? output.clone() : null;
    }
    
    /**
     * Get the ID of the node that executed the job
     * 
     * @return Node ID
     */
    public String getExecutedByNodeId() {
        return executedByNodeId;
    }
    
    /**
     * Set the ID of the node that executed the job
     * 
     * @param executedByNodeId Node ID
     */
    public void setExecutedByNodeId(String executedByNodeId) {
        this.executedByNodeId = executedByNodeId;
    }
    
    /**
     * Get the error message
     * 
     * @return Error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Set the error message
     * 
     * @param errorMessage Error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * Mark the job as started
     * 
     * @param nodeId ID of the node executing the job
     */
    public void markStarted(String nodeId) {
        this.status = JobStatus.RUNNING;
        this.startedAt = Instant.now();
        this.executedByNodeId = nodeId;
    }
    
    /**
     * Mark the job as completed
     * 
     * @param output Output data
     */
    public void markCompleted(byte[] output) {
        this.status = JobStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.output = output != null ? output.clone() : null;
    }
    
    /**
     * Mark the job as failed
     * 
     * @param errorMessage Error message
     */
    public void markFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "Job{" +
                "jobId='" + jobId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
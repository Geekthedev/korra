package korra.crypto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Cryptographic proof of agent execution
 */
public class ExecutionProof {
    private final String proofId;
    private final String agentId;
    private final long timestamp;
    private final String inputHash;
    private final String outputHash;
    private final String proofHash;
    
    /**
     * Create a new execution proof
     * 
     * @param agentId Agent ID
     * @param timestamp Execution timestamp
     * @param inputHash Hash of input data
     * @param outputHash Hash of output data
     * @param proofHash Hash of agent ID, timestamp, input hash, and output hash
     */
    public ExecutionProof(String agentId, long timestamp, String inputHash, 
                         String outputHash, String proofHash) {
        this.proofId = UUID.randomUUID().toString();
        this.agentId = agentId;
        this.timestamp = timestamp;
        this.inputHash = inputHash;
        this.outputHash = outputHash;
        this.proofHash = proofHash;
    }
    
    /**
     * Create a new execution proof with a specific ID
     * 
     * @param proofId Proof ID
     * @param agentId Agent ID
     * @param timestamp Execution timestamp
     * @param inputHash Hash of input data
     * @param outputHash Hash of output data
     * @param proofHash Hash of agent ID, timestamp, input hash, and output hash
     */
    public ExecutionProof(String proofId, String agentId, long timestamp, String inputHash, 
                         String outputHash, String proofHash) {
        this.proofId = proofId;
        this.agentId = agentId;
        this.timestamp = timestamp;
        this.inputHash = inputHash;
        this.outputHash = outputHash;
        this.proofHash = proofHash;
    }
    
    /**
     * Get the proof ID
     * 
     * @return Proof ID
     */
    public String getProofId() {
        return proofId;
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
     * Get the timestamp
     * 
     * @return Timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Get the input hash
     * 
     * @return Input hash
     */
    public String getInputHash() {
        return inputHash;
    }
    
    /**
     * Get the output hash
     * 
     * @return Output hash
     */
    public String getOutputHash() {
        return outputHash;
    }
    
    /**
     * Get the proof hash
     * 
     * @return Proof hash
     */
    public String getProofHash() {
        return proofHash;
    }
    
    /**
     * Get the execution time as an Instant
     * 
     * @return Execution time
     */
    public Instant getExecutionTime() {
        return Instant.ofEpochSecond(timestamp);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionProof proof = (ExecutionProof) o;
        return Objects.equals(proofId, proof.proofId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(proofId);
    }
    
    @Override
    public String toString() {
        return "ExecutionProof{" +
                "proofId='" + proofId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
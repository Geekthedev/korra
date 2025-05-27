package korra.api;

import korra.core.Job;
import korra.core.NodeInfo;
import korra.crypto.ExecutionProof;
import korra.registry.AgentDefinition;

import java.util.Map;

/**
 * Serializes objects to JSON
 */
public class JsonSerializer {
    /**
     * Serialize agents to JSON
     * 
     * @param agents Map of agent IDs to agent definitions
     * @return JSON string
     */
    public static String serializeAgents(Map<String, AgentDefinition> agents) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"agents\":[");
        
        boolean first = true;
        for (AgentDefinition agent : agents.values()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("{");
            sb.append("\"agentId\":\"").append(agent.getAgentId()).append("\",");
            sb.append("\"name\":\"").append(agent.getName()).append("\",");
            sb.append("\"type\":\"").append(agent.getType()).append("\",");
            sb.append("\"version\":\"").append(agent.getVersion()).append("\",");
            sb.append("\"status\":\"").append(agent.getStatus()).append("\"");
            sb.append("}");
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    /**
     * Deserialize agent from JSON
     * 
     * @param json JSON string
     * @return Agent definition
     */
    public static AgentDefinition deserializeAgent(String json) {
        // In a real implementation, this would parse the JSON
        // For this demo, we'll return a dummy agent
        return null;
    }
    
    /**
     * Serialize nodes to JSON
     * 
     * @param nodes Map of node IDs to node info
     * @return JSON string
     */
    public static String serializeNodes(Map<String, NodeInfo> nodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"nodes\":[");
        
        boolean first = true;
        for (NodeInfo node : nodes.values()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("{");
            sb.append("\"nodeId\":\"").append(node.getNodeId()).append("\",");
            sb.append("\"hostname\":\"").append(node.getHostname()).append("\",");
            sb.append("\"address\":\"").append(node.getAddress().getHostAddress()).append("\",");
            sb.append("\"port\":").append(node.getPort()).append(",");
            sb.append("\"status\":\"").append(node.getStatus()).append("\"");
            sb.append("}");
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    /**
     * Serialize jobs to JSON
     * 
     * @param jobs Map of job IDs to jobs
     * @return JSON string
     */
    public static String serializeJobs(Map<String, Job> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"jobs\":[");
        
        boolean first = true;
        for (Job job : jobs.values()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("{");
            sb.append("\"jobId\":\"").append(job.getJobId()).append("\",");
            sb.append("\"agentId\":\"").append(job.getAgentId()).append("\",");
            sb.append("\"status\":\"").append(job.getStatus()).append("\",");
            sb.append("\"createdAt\":\"").append(job.getCreatedAt()).append("\",");
            
            if (job.getStartedAt() != null) {
                sb.append("\"startedAt\":\"").append(job.getStartedAt()).append("\",");
            }
            
            if (job.getCompletedAt() != null) {
                sb.append("\"completedAt\":\"").append(job.getCompletedAt()).append("\",");
            }
            
            if (job.getExecutedByNodeId() != null) {
                sb.append("\"executedByNodeId\":\"").append(job.getExecutedByNodeId()).append("\",");
            }
            
            if (job.getErrorMessage() != null) {
                sb.append("\"errorMessage\":\"").append(job.getErrorMessage()).append("\",");
            }
            
            // Remove trailing comma if present
            if (sb.charAt(sb.length() - 1) == ',') {
                sb.deleteCharAt(sb.length() - 1);
            }
            
            sb.append("}");
        }
        
        sb.append("]}");
        return sb.toString();
    }
    
    /**
     * Deserialize job from JSON
     * 
     * @param json JSON string
     * @return Job
     */
    public static Job deserializeJob(String json) {
        // In a real implementation, this would parse the JSON
        // For this demo, we'll return a dummy job
        return null;
    }
    
    /**
     * Serialize proofs to JSON
     * 
     * @param proofs Map of proof IDs to execution proofs
     * @return JSON string
     */
    public static String serializeProofs(Map<String, ExecutionProof> proofs) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"proofs\":[");
        
        boolean first = true;
        for (ExecutionProof proof : proofs.values()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            
            sb.append("{");
            sb.append("\"proofId\":\"").append(proof.getProofId()).append("\",");
            sb.append("\"agentId\":\"").append(proof.getAgentId()).append("\",");
            sb.append("\"timestamp\":").append(proof.getTimestamp()).append(",");
            sb.append("\"inputHash\":\"").append(proof.getInputHash()).append("\",");
            sb.append("\"outputHash\":\"").append(proof.getOutputHash()).append("\",");
            sb.append("\"proofHash\":\"").append(proof.getProofHash()).append("\"");
            sb.append("}");
        }
        
        sb.append("]}");
        return sb.toString();
    }
}
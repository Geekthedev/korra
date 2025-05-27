package korra.router;

import korra.core.Coordinator;
import korra.core.Job;
import korra.core.NodeInfo;
import korra.core.NodeStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Routes jobs between nodes
 */
public class JobRouter {
    private static final Logger LOGGER = Logger.getLogger(JobRouter.class.getName());
    
    private Coordinator coordinator;
    private final Map<String, Job> activeJobs;
    private final Map<String, List<String>> nodeJobs;
    private boolean isInitialized;
    
    /**
     * Create a new job router
     */
    public JobRouter() {
        this.activeJobs = new ConcurrentHashMap<>();
        this.nodeJobs = new ConcurrentHashMap<>();
        this.isInitialized = false;
    }
    
    /**
     * Initialize the job router
     * 
     * @param coordinator Coordinator instance
     */
    public void initialize(Coordinator coordinator) {
        if (isInitialized) {
            LOGGER.warning("Job router is already initialized");
            return;
        }
        
        LOGGER.info("Initializing job router");
        this.coordinator = coordinator;
        this.isInitialized = true;
    }
    
    /**
     * Shutdown the job router
     */
    public void shutdown() {
        if (!isInitialized) {
            LOGGER.warning("Job router is not initialized");
            return;
        }
        
        LOGGER.info("Shutting down job router");
        this.isInitialized = false;
    }
    
    /**
     * Route a job to an appropriate node
     * 
     * @param job Job to route
     * @return Job ID if successful, null otherwise
     */
    public String routeJob(Job job) {
        if (!isInitialized) {
            LOGGER.warning("Cannot route job, job router is not initialized");
            return null;
        }
        
        LOGGER.info("Routing job: " + job.getJobId());
        
        // Find a suitable node to execute the job
        String nodeId = findSuitableNode(job);
        if (nodeId == null) {
            LOGGER.warning("No suitable node found for job: " + job.getJobId());
            return null;
        }
        
        // Assign job to node
        job.markStarted(nodeId);
        activeJobs.put(job.getJobId(), job);
        
        // Track jobs assigned to the node
        nodeJobs.computeIfAbsent(nodeId, k -> new ArrayList<>()).add(job.getJobId());
        
        LOGGER.info("Job " + job.getJobId() + " assigned to node " + nodeId);
        
        return job.getJobId();
    }
    
    /**
     * Find a suitable node to execute a job
     * 
     * @param job Job to execute
     * @return Node ID if found, null otherwise
     */
    private String findSuitableNode(Job job) {
        // Get all connected nodes
        Map<String, NodeInfo> nodes = coordinator.getConnectedNodes();
        if (nodes.isEmpty()) {
            LOGGER.warning("No nodes available");
            return null;
        }
        
        // Check if agent is registered
        if (!coordinator.getAgentRegistry().isAgentRegistered(job.getAgentId())) {
            LOGGER.warning("Agent not registered: " + job.getAgentId());
            return null;
        }
        
        // Find a node that can execute the job
        for (Map.Entry<String, NodeInfo> entry : nodes.entrySet()) {
            String nodeId = entry.getKey();
            NodeInfo nodeInfo = entry.getValue();
            
            // Check if node is online
            if (nodeInfo.getStatus() != NodeStatus.ONLINE) {
                continue;
            }
            
            // Check if node has the required capability
            if (!nodeInfo.hasCapability("agent:" + job.getAgentId())) {
                continue;
            }
            
            // Node is suitable
            return nodeId;
        }
        
        return null;
    }
    
    /**
     * Notify that a job has completed
     * 
     * @param jobId Job ID
     * @param output Job output
     * @return True if the job was found and updated
     */
    public boolean notifyJobCompleted(String jobId, byte[] output) {
        if (!isInitialized) {
            LOGGER.warning("Cannot notify job completion, job router is not initialized");
            return false;
        }
        
        LOGGER.info("Job completed: " + jobId);
        
        // Find the job
        Job job = activeJobs.get(jobId);
        if (job == null) {
            LOGGER.warning("Job not found: " + jobId);
            return false;
        }
        
        // Update job status
        job.markCompleted(output);
        
        // Remove job from active jobs
        activeJobs.remove(jobId);
        
        // Remove job from node jobs
        String nodeId = job.getExecutedByNodeId();
        if (nodeId != null && nodeJobs.containsKey(nodeId)) {
            nodeJobs.get(nodeId).remove(jobId);
        }
        
        return true;
    }
    
    /**
     * Notify that a job has failed
     * 
     * @param jobId Job ID
     * @param errorMessage Error message
     * @return True if the job was found and updated
     */
    public boolean notifyJobFailed(String jobId, String errorMessage) {
        if (!isInitialized) {
            LOGGER.warning("Cannot notify job failure, job router is not initialized");
            return false;
        }
        
        LOGGER.info("Job failed: " + jobId);
        
        // Find the job
        Job job = activeJobs.get(jobId);
        if (job == null) {
            LOGGER.warning("Job not found: " + jobId);
            return false;
        }
        
        // Update job status
        job.markFailed(errorMessage);
        
        // Remove job from active jobs
        activeJobs.remove(jobId);
        
        // Remove job from node jobs
        String nodeId = job.getExecutedByNodeId();
        if (nodeId != null && nodeJobs.containsKey(nodeId)) {
            nodeJobs.get(nodeId).remove(jobId);
        }
        
        return true;
    }
    
    /**
     * Get all active jobs
     * 
     * @return Map of job IDs to jobs
     */
    public Map<String, Job> getActiveJobs() {
        return activeJobs;
    }
    
    /**
     * Get all jobs assigned to a node
     * 
     * @param nodeId Node ID
     * @return List of job IDs
     */
    public List<String> getNodeJobs(String nodeId) {
        return nodeJobs.getOrDefault(nodeId, new ArrayList<>());
    }
    
    /**
     * Check if the job router is initialized
     * 
     * @return True if the job router is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
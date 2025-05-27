package korra.core;

import korra.registry.AgentRegistry;
import korra.router.JobRouter;
import korra.crypto.ProofValidator;
import korra.protocol.NodeSync;
import korra.storage.SnapshotStore;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Global coordinator for KORRA nodes and agents
 */
public class Coordinator {
    private static final Logger LOGGER = Logger.getLogger(Coordinator.class.getName());
    
    private final String nodeId;
    private final AgentRegistry agentRegistry;
    private final JobRouter jobRouter;
    private final ProofValidator proofValidator;
    private final NodeSync nodeSync;
    private final SnapshotStore snapshotStore;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, NodeInfo> connectedNodes;
    private boolean isRunning;
    
    /**
     * Create a new coordinator
     */
    public Coordinator() {
        this.nodeId = UUID.randomUUID().toString();
        this.agentRegistry = new AgentRegistry();
        this.jobRouter = new JobRouter();
        this.proofValidator = new ProofValidator();
        this.nodeSync = new NodeSync();
        this.snapshotStore = new SnapshotStore();
        this.executorService = Executors.newFixedThreadPool(10);
        this.connectedNodes = new ConcurrentHashMap<>();
        this.isRunning = false;
        
        LOGGER.info("Coordinator created with node ID: " + nodeId);
    }
    
    /**
     * Start the coordinator
     */
    public void start() {
        if (isRunning) {
            LOGGER.warning("Coordinator is already running");
            return;
        }
        
        LOGGER.info("Starting coordinator");
        
        // Initialize components
        agentRegistry.initialize();
        jobRouter.initialize(this);
        proofValidator.initialize();
        nodeSync.initialize(this);
        snapshotStore.initialize();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        
        isRunning = true;
        LOGGER.info("Coordinator started");
    }
    
    /**
     * Stop the coordinator
     */
    public void stop() {
        if (!isRunning) {
            LOGGER.warning("Coordinator is not running");
            return;
        }
        
        LOGGER.info("Stopping coordinator");
        
        // Shutdown components
        nodeSync.shutdown();
        jobRouter.shutdown();
        
        // Shutdown executor service
        executorService.shutdown();
        
        isRunning = false;
        LOGGER.info("Coordinator stopped");
    }
    
    /**
     * Register a node with the coordinator
     * 
     * @param nodeInfo Information about the node
     * @return True if the node was registered successfully
     */
    public boolean registerNode(NodeInfo nodeInfo) {
        if (!isRunning) {
            LOGGER.warning("Cannot register node, coordinator is not running");
            return false;
        }
        
        LOGGER.info("Registering node: " + nodeInfo.getNodeId());
        
        // Store node info
        connectedNodes.put(nodeInfo.getNodeId(), nodeInfo);
        
        // Notify other components
        nodeSync.notifyNodeJoined(nodeInfo);
        
        return true;
    }
    
    /**
     * Unregister a node from the coordinator
     * 
     * @param nodeId ID of the node to unregister
     * @return True if the node was unregistered successfully
     */
    public boolean unregisterNode(String nodeId) {
        if (!isRunning) {
            LOGGER.warning("Cannot unregister node, coordinator is not running");
            return false;
        }
        
        LOGGER.info("Unregistering node: " + nodeId);
        
        // Remove node info
        NodeInfo nodeInfo = connectedNodes.remove(nodeId);
        if (nodeInfo == null) {
            LOGGER.warning("Node not found: " + nodeId);
            return false;
        }
        
        // Notify other components
        nodeSync.notifyNodeLeft(nodeInfo);
        
        return true;
    }
    
    /**
     * Submit a job to be executed
     * 
     * @param job Job to execute
     * @return Job ID if successful, null otherwise
     */
    public String submitJob(Job job) {
        if (!isRunning) {
            LOGGER.warning("Cannot submit job, coordinator is not running");
            return null;
        }
        
        LOGGER.info("Submitting job: " + job.getJobId());
        
        // Route job to appropriate node
        return jobRouter.routeJob(job);
    }
    
    /**
     * Get the node ID
     * 
     * @return Node ID
     */
    public String getNodeId() {
        return nodeId;
    }
    
    /**
     * Get the agent registry
     * 
     * @return Agent registry
     */
    public AgentRegistry getAgentRegistry() {
        return agentRegistry;
    }
    
    /**
     * Get the job router
     * 
     * @return Job router
     */
    public JobRouter getJobRouter() {
        return jobRouter;
    }
    
    /**
     * Get the proof validator
     * 
     * @return Proof validator
     */
    public ProofValidator getProofValidator() {
        return proofValidator;
    }
    
    /**
     * Get the node sync
     * 
     * @return Node sync
     */
    public NodeSync getNodeSync() {
        return nodeSync;
    }
    
    /**
     * Get the snapshot store
     * 
     * @return Snapshot store
     */
    public SnapshotStore getSnapshotStore() {
        return snapshotStore;
    }
    
    /**
     * Get the executor service
     * 
     * @return Executor service
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }
    
    /**
     * Get all connected nodes
     * 
     * @return Map of node IDs to node info
     */
    public ConcurrentHashMap<String, NodeInfo> getConnectedNodes() {
        return connectedNodes;
    }
    
    /**
     * Check if the coordinator is running
     * 
     * @return True if the coordinator is running
     */
    public boolean isRunning() {
        return isRunning;
    }
}
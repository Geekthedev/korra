package korra.registry;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Registry for KORRA agents
 */
public class AgentRegistry {
    private static final Logger LOGGER = Logger.getLogger(AgentRegistry.class.getName());
    
    private final Map<String, AgentDefinition> agents;
    private final Map<String, AgentVersion> latestVersions;
    
    /**
     * Create a new agent registry
     */
    public AgentRegistry() {
        this.agents = new ConcurrentHashMap<>();
        this.latestVersions = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the agent registry
     */
    public void initialize() {
        LOGGER.info("Initializing agent registry");
    }
    
    /**
     * Register an agent definition
     * 
     * @param agentDefinition Agent definition
     * @return True if the agent was registered successfully
     */
    public boolean registerAgent(AgentDefinition agentDefinition) {
        LOGGER.info("Registering agent: " + agentDefinition.getAgentId());
        
        // Store agent definition
        agents.put(agentDefinition.getAgentId(), agentDefinition);
        
        // Update latest version if newer
        AgentVersion latestVersion = latestVersions.get(agentDefinition.getAgentId());
        if (latestVersion == null || agentDefinition.getVersion().compareTo(latestVersion) > 0) {
            latestVersions.put(agentDefinition.getAgentId(), agentDefinition.getVersion());
        }
        
        return true;
    }
    
    /**
     * Unregister an agent definition
     * 
     * @param agentId Agent ID
     * @return True if the agent was unregistered successfully
     */
    public boolean unregisterAgent(String agentId) {
        LOGGER.info("Unregistering agent: " + agentId);
        
        // Remove agent definition
        AgentDefinition agentDefinition = agents.remove(agentId);
        if (agentDefinition == null) {
            LOGGER.warning("Agent not found: " + agentId);
            return false;
        }
        
        // Remove latest version
        latestVersions.remove(agentId);
        
        return true;
    }
    
    /**
     * Get an agent definition
     * 
     * @param agentId Agent ID
     * @return Agent definition, or null if not found
     */
    public AgentDefinition getAgent(String agentId) {
        return agents.get(agentId);
    }
    
    /**
     * Get all agent definitions
     * 
     * @return Map of agent IDs to agent definitions
     */
    public Map<String, AgentDefinition> getAllAgents() {
        return Collections.unmodifiableMap(agents);
    }
    
    /**
     * Get the latest version of an agent
     * 
     * @param agentId Agent ID
     * @return Latest version, or null if not found
     */
    public AgentVersion getLatestVersion(String agentId) {
        return latestVersions.get(agentId);
    }
    
    /**
     * Check if an agent is registered
     * 
     * @param agentId Agent ID
     * @return True if the agent is registered
     */
    public boolean isAgentRegistered(String agentId) {
        return agents.containsKey(agentId);
    }
    
    /**
     * Update an agent definition
     * 
     * @param agentDefinition Updated agent definition
     * @return True if the agent was updated successfully
     */
    public boolean updateAgent(AgentDefinition agentDefinition) {
        LOGGER.info("Updating agent: " + agentDefinition.getAgentId());
        
        // Check if agent exists
        if (!agents.containsKey(agentDefinition.getAgentId())) {
            LOGGER.warning("Agent not found: " + agentDefinition.getAgentId());
            return false;
        }
        
        // Update agent definition
        agents.put(agentDefinition.getAgentId(), agentDefinition);
        
        // Update latest version if newer
        AgentVersion latestVersion = latestVersions.get(agentDefinition.getAgentId());
        if (latestVersion == null || agentDefinition.getVersion().compareTo(latestVersion) > 0) {
            latestVersions.put(agentDefinition.getAgentId(), agentDefinition.getVersion());
        }
        
        return true;
    }
}
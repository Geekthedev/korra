package korra.registry;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Definition of a KORRA agent
 */
public class AgentDefinition {
    private final String agentId;
    private final String name;
    private final AgentType type;
    private final AgentVersion version;
    private final String description;
    private final Map<String, String> metadata;
    private final Instant registrationTime;
    private final String wasmModulePath;
    private AgentStatus status;
    
    /**
     * Create a new agent definition
     * 
     * @param name Agent name
     * @param type Agent type
     * @param version Agent version
     * @param description Agent description
     * @param wasmModulePath Path to the WASM module
     */
    public AgentDefinition(String name, AgentType type, AgentVersion version, 
                         String description, String wasmModulePath) {
        this.agentId = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.version = version;
        this.description = description;
        this.metadata = new HashMap<>();
        this.registrationTime = Instant.now();
        this.wasmModulePath = wasmModulePath;
        this.status = AgentStatus.INACTIVE;
    }
    
    /**
     * Create a new agent definition with a specific ID
     * 
     * @param agentId Agent ID
     * @param name Agent name
     * @param type Agent type
     * @param version Agent version
     * @param description Agent description
     * @param wasmModulePath Path to the WASM module
     */
    public AgentDefinition(String agentId, String name, AgentType type, AgentVersion version, 
                         String description, String wasmModulePath) {
        this.agentId = agentId;
        this.name = name;
        this.type = type;
        this.version = version;
        this.description = description;
        this.metadata = new HashMap<>();
        this.registrationTime = Instant.now();
        this.wasmModulePath = wasmModulePath;
        this.status = AgentStatus.INACTIVE;
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
     * Get the agent name
     * 
     * @return Agent name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the agent type
     * 
     * @return Agent type
     */
    public AgentType getType() {
        return type;
    }
    
    /**
     * Get the agent version
     * 
     * @return Agent version
     */
    public AgentVersion getVersion() {
        return version;
    }
    
    /**
     * Get the agent description
     * 
     * @return Agent description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the agent metadata
     * 
     * @return Map of metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }
    
    /**
     * Add metadata to the agent
     * 
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    /**
     * Get the registration time
     * 
     * @return Registration time
     */
    public Instant getRegistrationTime() {
        return registrationTime;
    }
    
    /**
     * Get the WASM module path
     * 
     * @return WASM module path
     */
    public String getWasmModulePath() {
        return wasmModulePath;
    }
    
    /**
     * Get the agent status
     * 
     * @return Agent status
     */
    public AgentStatus getStatus() {
        return status;
    }
    
    /**
     * Set the agent status
     * 
     * @param status Agent status
     */
    public void setStatus(AgentStatus status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "AgentDefinition{" +
                "agentId='" + agentId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", version=" + version +
                ", status=" + status +
                '}';
    }
}
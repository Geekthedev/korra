package korra.registry;

/**
 * Type of a KORRA agent
 */
public enum AgentType {
    /**
     * Analyzer agent - processes and analyzes data
     */
    ANALYZER,
    
    /**
     * Transformer agent - transforms data from one format to another
     */
    TRANSFORMER,
    
    /**
     * Validator agent - validates data against rules
     */
    VALIDATOR,
    
    /**
     * Coordinator agent - coordinates other agents
     */
    COORDINATOR,
    
    /**
     * Custom agent - user-defined functionality
     */
    CUSTOM
}
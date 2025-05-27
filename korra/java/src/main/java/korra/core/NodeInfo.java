package korra.core;

import java.net.InetAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Information about a KORRA node
 */
public class NodeInfo {
    private final String nodeId;
    private final String hostname;
    private final InetAddress address;
    private final int port;
    private final Map<String, String> capabilities;
    private final Instant joinTime;
    private Instant lastHeartbeat;
    private NodeStatus status;
    
    /**
     * Create a new node info
     * 
     * @param nodeId Node ID
     * @param hostname Hostname
     * @param address IP address
     * @param port Port number
     */
    public NodeInfo(String nodeId, String hostname, InetAddress address, int port) {
        this.nodeId = nodeId;
        this.hostname = hostname;
        this.address = address;
        this.port = port;
        this.capabilities = new HashMap<>();
        this.joinTime = Instant.now();
        this.lastHeartbeat = Instant.now();
        this.status = NodeStatus.ONLINE;
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
     * Get the hostname
     * 
     * @return Hostname
     */
    public String getHostname() {
        return hostname;
    }
    
    /**
     * Get the IP address
     * 
     * @return IP address
     */
    public InetAddress getAddress() {
        return address;
    }
    
    /**
     * Get the port number
     * 
     * @return Port number
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Get the node capabilities
     * 
     * @return Map of capabilities
     */
    public Map<String, String> getCapabilities() {
        return capabilities;
    }
    
    /**
     * Add a capability to the node
     * 
     * @param key Capability key
     * @param value Capability value
     */
    public void addCapability(String key, String value) {
        capabilities.put(key, value);
    }
    
    /**
     * Check if the node has a capability
     * 
     * @param key Capability key
     * @return True if the node has the capability
     */
    public boolean hasCapability(String key) {
        return capabilities.containsKey(key);
    }
    
    /**
     * Get the join time
     * 
     * @return Join time
     */
    public Instant getJoinTime() {
        return joinTime;
    }
    
    /**
     * Get the last heartbeat time
     * 
     * @return Last heartbeat time
     */
    public Instant getLastHeartbeat() {
        return lastHeartbeat;
    }
    
    /**
     * Update the last heartbeat time
     */
    public void updateHeartbeat() {
        this.lastHeartbeat = Instant.now();
    }
    
    /**
     * Get the node status
     * 
     * @return Node status
     */
    public NodeStatus getStatus() {
        return status;
    }
    
    /**
     * Set the node status
     * 
     * @param status New status
     */
    public void setStatus(NodeStatus status) {
        this.status = status;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(nodeId, nodeInfo.nodeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }
    
    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", hostname='" + hostname + '\'' +
                ", address=" + address +
                ", port=" + port +
                ", status=" + status +
                '}';
    }
}
package korra.api;

import korra.core.Coordinator;
import korra.core.Job;
import korra.core.NodeInfo;
import korra.registry.AgentDefinition;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * JSON-RPC API for TypeScript CLI
 */
public class AdminInterface {
    private static final Logger LOGGER = Logger.getLogger(AdminInterface.class.getName());
    
    private final Coordinator coordinator;
    private HttpServer server;
    private final int port;
    
    /**
     * Create a new admin interface
     * 
     * @param coordinator Coordinator instance
     * @param port Port to listen on
     */
    public AdminInterface(Coordinator coordinator, int port) {
        this.coordinator = coordinator;
        this.port = port;
    }
    
    /**
     * Start the admin interface
     * 
     * @throws IOException If the server could not be started
     */
    public void start() throws IOException {
        LOGGER.info("Starting admin interface on port " + port);
        
        // Create HTTP server
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Register endpoints
        server.createContext("/api/agents", new AgentsHandler());
        server.createContext("/api/nodes", new NodesHandler());
        server.createContext("/api/jobs", new JobsHandler());
        server.createContext("/api/proofs", new ProofsHandler());
        
        // Start server
        server.start();
        
        LOGGER.info("Admin interface started");
    }
    
    /**
     * Stop the admin interface
     */
    public void stop() {
        LOGGER.info("Stopping admin interface");
        
        if (server != null) {
            server.stop(0);
            server = null;
        }
        
        LOGGER.info("Admin interface stopped");
    }
    
    /**
     * Handler for /api/agents endpoint
     */
    private class AgentsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            try {
                if (method.equals("GET")) {
                    // Get all agents
                    Map<String, AgentDefinition> agents = coordinator.getAgentRegistry().getAllAgents();
                    String response = JsonSerializer.serializeAgents(agents);
                    sendJsonResponse(exchange, 200, response);
                } else if (method.equals("POST")) {
                    // Register agent
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    AgentDefinition agent = JsonSerializer.deserializeAgent(requestBody);
                    boolean success = coordinator.getAgentRegistry().registerAgent(agent);
                    String response = "{\"success\":" + success + "}";
                    sendJsonResponse(exchange, success ? 200 : 400, response);
                } else {
                    sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                LOGGER.severe("Error handling agent request: " + e.getMessage());
                sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }
    }
    
    /**
     * Handler for /api/nodes endpoint
     */
    private class NodesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            try {
                if (method.equals("GET")) {
                    // Get all nodes
                    Map<String, NodeInfo> nodes = coordinator.getConnectedNodes();
                    String response = JsonSerializer.serializeNodes(nodes);
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                LOGGER.severe("Error handling node request: " + e.getMessage());
                sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }
    }
    
    /**
     * Handler for /api/jobs endpoint
     */
    private class JobsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            try {
                if (method.equals("GET")) {
                    // Get all jobs
                    Map<String, Job> jobs = coordinator.getJobRouter().getActiveJobs();
                    String response = JsonSerializer.serializeJobs(jobs);
                    sendJsonResponse(exchange, 200, response);
                } else if (method.equals("POST")) {
                    // Submit job
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    Job job = JsonSerializer.deserializeJob(requestBody);
                    String jobId = coordinator.submitJob(job);
                    String response = "{\"jobId\":\"" + jobId + "\"}";
                    sendJsonResponse(exchange, jobId != null ? 200 : 400, response);
                } else {
                    sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                LOGGER.severe("Error handling job request: " + e.getMessage());
                sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }
    }
    
    /**
     * Handler for /api/proofs endpoint
     */
    private class ProofsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            try {
                if (method.equals("GET")) {
                    // Get all proofs
                    Map<String, korra.crypto.ExecutionProof> proofs = 
                        coordinator.getProofValidator().getAllProofs();
                    String response = JsonSerializer.serializeProofs(proofs);
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            } catch (Exception e) {
                LOGGER.severe("Error handling proof request: " + e.getMessage());
                sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        }
    }
    
    /**
     * Send a JSON response
     * 
     * @param exchange HTTP exchange
     * @param statusCode HTTP status code
     * @param response Response body
     * @throws IOException If an I/O error occurs
     */
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}
package korra.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Validates cryptographic execution traces
 */
public class ProofValidator {
    private static final Logger LOGGER = Logger.getLogger(ProofValidator.class.getName());
    
    private final Map<String, ExecutionProof> proofs;
    
    /**
     * Create a new proof validator
     */
    public ProofValidator() {
        this.proofs = new ConcurrentHashMap<>();
    }
    
    /**
     * Initialize the proof validator
     */
    public void initialize() {
        LOGGER.info("Initializing proof validator");
    }
    
    /**
     * Register an execution proof
     * 
     * @param proof Execution proof
     * @return True if the proof was registered successfully
     */
    public boolean registerProof(ExecutionProof proof) {
        LOGGER.info("Registering proof: " + proof.getProofId());
        
        // Store proof
        proofs.put(proof.getProofId(), proof);
        
        return true;
    }
    
    /**
     * Validate a proof against input and output
     * 
     * @param proofId Proof ID
     * @param input Input data
     * @param output Output data
     * @return Validation result
     */
    public ValidationResult validateProof(String proofId, byte[] input, byte[] output) {
        LOGGER.info("Validating proof: " + proofId);
        
        // Find the proof
        ExecutionProof proof = proofs.get(proofId);
        if (proof == null) {
            LOGGER.warning("Proof not found: " + proofId);
            return ValidationResult.PROOF_NOT_FOUND;
        }
        
        // Calculate input hash
        String inputHash = calculateHash(input);
        if (!inputHash.equals(proof.getInputHash())) {
            LOGGER.warning("Input hash mismatch for proof: " + proofId);
            return ValidationResult.INPUT_MISMATCH;
        }
        
        // Calculate output hash
        String outputHash = calculateHash(output);
        if (!outputHash.equals(proof.getOutputHash())) {
            LOGGER.warning("Output hash mismatch for proof: " + proofId);
            return ValidationResult.OUTPUT_MISMATCH;
        }
        
        // Verify proof hash
        String proofHash = calculateProofHash(proof.getAgentId(), proof.getTimestamp(), 
                                             inputHash, outputHash);
        if (!proofHash.equals(proof.getProofHash())) {
            LOGGER.warning("Proof hash mismatch for proof: " + proofId);
            return ValidationResult.PROOF_HASH_MISMATCH;
        }
        
        LOGGER.info("Proof validation successful: " + proofId);
        return ValidationResult.VALID;
    }
    
    /**
     * Calculate hash of data
     * 
     * @param data Data to hash
     * @return Base64-encoded hash
     */
    private String calculateHash(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("SHA-256 algorithm not available: " + e.getMessage());
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Calculate proof hash
     * 
     * @param agentId Agent ID
     * @param timestamp Timestamp
     * @param inputHash Input hash
     * @param outputHash Output hash
     * @return Base64-encoded proof hash
     */
    private String calculateProofHash(String agentId, long timestamp, 
                                     String inputHash, String outputHash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(agentId.getBytes());
            digest.update(Long.toString(timestamp).getBytes());
            digest.update(inputHash.getBytes());
            digest.update(outputHash.getBytes());
            byte[] hash = digest.digest();
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.severe("SHA-256 algorithm not available: " + e.getMessage());
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * Get a proof by ID
     * 
     * @param proofId Proof ID
     * @return Execution proof, or null if not found
     */
    public ExecutionProof getProof(String proofId) {
        return proofs.get(proofId);
    }
    
    /**
     * Get all proofs
     * 
     * @return Map of proof IDs to proofs
     */
    public Map<String, ExecutionProof> getAllProofs() {
        return new HashMap<>(proofs);
    }
}
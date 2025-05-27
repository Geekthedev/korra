package korra.crypto;

/**
 * Result of proof validation
 */
public enum ValidationResult {
    /**
     * Proof is valid
     */
    VALID,
    
    /**
     * Proof not found
     */
    PROOF_NOT_FOUND,
    
    /**
     * Input hash doesn't match proof
     */
    INPUT_MISMATCH,
    
    /**
     * Output hash doesn't match proof
     */
    OUTPUT_MISMATCH,
    
    /**
     * Proof hash is invalid
     */
    PROOF_HASH_MISMATCH
}
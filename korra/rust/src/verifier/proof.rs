//! Execution proof generator and validator

use std::time::{SystemTime, UNIX_EPOCH};
use sha2::{Sha256, Digest};
use base64::{Engine as _, engine::general_purpose};

/// Execution proof for agent execution
#[derive(Debug, Clone)]
pub struct ExecutionProof {
    agent_id: String,
    timestamp: u64,
    input_hash: String,
    output_hash: String,
    proof_hash: String,
}

impl ExecutionProof {
    /// Create a new execution proof
    pub fn new(agent_id: &str, input: &[u8], output: &[u8]) -> Self {
        // Get current timestamp
        let timestamp = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
        
        // Calculate input hash
        let mut hasher = Sha256::new();
        hasher.update(input);
        let input_hash = general_purpose::STANDARD.encode(hasher.finalize());
        
        // Calculate output hash
        let mut hasher = Sha256::new();
        hasher.update(output);
        let output_hash = general_purpose::STANDARD.encode(hasher.finalize());
        
        // Calculate proof hash (hash of agent_id + timestamp + input_hash + output_hash)
        let mut hasher = Sha256::new();
        hasher.update(agent_id.as_bytes());
        hasher.update(timestamp.to_string().as_bytes());
        hasher.update(input_hash.as_bytes());
        hasher.update(output_hash.as_bytes());
        let proof_hash = general_purpose::STANDARD.encode(hasher.finalize());
        
        ExecutionProof {
            agent_id: agent_id.to_string(),
            timestamp,
            input_hash,
            output_hash,
            proof_hash,
        }
    }
    
    /// Verify the execution proof against input and output
    pub fn verify(&self, agent_id: &str, input: &[u8], output: &[u8]) -> bool {
        // Verify agent ID
        if self.agent_id != agent_id {
            return false;
        }
        
        // Calculate and verify input hash
        let mut hasher = Sha256::new();
        hasher.update(input);
        let input_hash = general_purpose::STANDARD.encode(hasher.finalize());
        if self.input_hash != input_hash {
            return false;
        }
        
        // Calculate and verify output hash
        let mut hasher = Sha256::new();
        hasher.update(output);
        let output_hash = general_purpose::STANDARD.encode(hasher.finalize());
        if self.output_hash != output_hash {
            return false;
        }
        
        // Calculate and verify proof hash
        let mut hasher = Sha256::new();
        hasher.update(agent_id.as_bytes());
        hasher.update(self.timestamp.to_string().as_bytes());
        hasher.update(self.input_hash.as_bytes());
        hasher.update(self.output_hash.as_bytes());
        let proof_hash = general_purpose::STANDARD.encode(hasher.finalize());
        
        self.proof_hash == proof_hash
    }
    
    /// Serialize the proof to JSON
    pub fn to_json(&self) -> String {
        serde_json::json!({
            "agent_id": self.agent_id,
            "timestamp": self.timestamp,
            "input_hash": self.input_hash,
            "output_hash": self.output_hash,
            "proof_hash": self.proof_hash,
        }).to_string()
    }
    
    /// Deserialize the proof from JSON
    pub fn from_json(json: &str) -> Option<Self> {
        let v: serde_json::Value = serde_json::from_str(json).ok()?;
        
        Some(ExecutionProof {
            agent_id: v["agent_id"].as_str()?.to_string(),
            timestamp: v["timestamp"].as_u64()?,
            input_hash: v["input_hash"].as_str()?.to_string(),
            output_hash: v["output_hash"].as_str()?.to_string(),
            proof_hash: v["proof_hash"].as_str()?.to_string(),
        })
    }
    
    /// Get the agent ID
    pub fn agent_id(&self) -> &str {
        &self.agent_id
    }
    
    /// Get the timestamp
    pub fn timestamp(&self) -> u64 {
        self.timestamp
    }
    
    /// Get the input hash
    pub fn input_hash(&self) -> &str {
        &self.input_hash
    }
    
    /// Get the output hash
    pub fn output_hash(&self) -> &str {
        &self.output_hash
    }
    
    /// Get the proof hash
    pub fn proof_hash(&self) -> &str {
        &self.proof_hash
    }
}
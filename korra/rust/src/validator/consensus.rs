//! Lightweight consensus validator

use std::collections::{HashMap, HashSet};
use std::time::{SystemTime, UNIX_EPOCH};

use crate::verifier::proof::ExecutionProof;

/// Consensus validation result
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ConsensusResult {
    Valid,
    Invalid,
    Uncertain,
}

/// Validator node info
#[derive(Debug, Clone)]
pub struct ValidatorNode {
    node_id: String,
    weight: u32,
    last_seen: u64,
}

impl ValidatorNode {
    /// Create a new validator node
    pub fn new(node_id: &str, weight: u32) -> Self {
        ValidatorNode {
            node_id: node_id.to_string(),
            weight,
            last_seen: SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap_or_default()
                .as_secs(),
        }
    }
    
    /// Update the last seen timestamp
    pub fn update_last_seen(&mut self) {
        self.last_seen = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
    }
    
    /// Get the node ID
    pub fn node_id(&self) -> &str {
        &self.node_id
    }
    
    /// Get the node weight
    pub fn weight(&self) -> u32 {
        self.weight
    }
    
    /// Get the last seen timestamp
    pub fn last_seen(&self) -> u64 {
        self.last_seen
    }
}

/// Lightweight consensus validator
pub struct ConsensusValidator {
    nodes: HashMap<String, ValidatorNode>,
    proofs: HashMap<String, HashMap<String, ExecutionProof>>,
    required_consensus: f32, // 0.0 to 1.0
}

impl ConsensusValidator {
    /// Create a new consensus validator
    pub fn new(required_consensus: f32) -> Self {
        ConsensusValidator {
            nodes: HashMap::new(),
            proofs: HashMap::new(),
            required_consensus: required_consensus.max(0.0).min(1.0),
        }
    }
    
    /// Add a validator node
    pub fn add_node(&mut self, node_id: &str, weight: u32) {
        self.nodes.insert(node_id.to_string(), ValidatorNode::new(node_id, weight));
    }
    
    /// Remove a validator node
    pub fn remove_node(&mut self, node_id: &str) -> bool {
        self.nodes.remove(node_id).is_some()
    }
    
    /// Add an execution proof from a node
    pub fn add_proof(&mut self, node_id: &str, proof: ExecutionProof) -> bool {
        // Check if node exists
        if !self.nodes.contains_key(node_id) {
            return false;
        }
        
        // Update node's last seen timestamp
        if let Some(node) = self.nodes.get_mut(node_id) {
            node.update_last_seen();
        }
        
        // Get or create the proof map for this agent
        let agent_proofs = self.proofs
            .entry(proof.agent_id().to_string())
            .or_insert_with(HashMap::new);
        
        // Add the proof
        agent_proofs.insert(node_id.to_string(), proof);
        
        true
    }
    
    /// Validate consensus for an agent
    pub fn validate(&self, agent_id: &str) -> ConsensusResult {
        // Get proofs for this agent
        let agent_proofs = match self.proofs.get(agent_id) {
            Some(p) => p,
            None => return ConsensusResult::Uncertain,
        };
        
        // Count the total weight of all nodes
        let total_weight: u32 = self.nodes.values().map(|n| n.weight).sum();
        if total_weight == 0 {
            return ConsensusResult::Uncertain;
        }
        
        // Group proofs by proof hash
        let mut hash_groups: HashMap<String, HashSet<String>> = HashMap::new();
        for (node_id, proof) in agent_proofs {
            hash_groups.entry(proof.proof_hash().to_string())
                .or_insert_with(HashSet::new)
                .insert(node_id.clone());
        }
        
        // Find the hash with the most weight
        let mut max_weight = 0;
        let mut max_hash = String::new();
        for (hash, node_ids) in &hash_groups {
            let weight: u32 = node_ids.iter()
                .filter_map(|id| self.nodes.get(id))
                .map(|n| n.weight)
                .sum();
            
            if weight > max_weight {
                max_weight = weight;
                max_hash = hash.clone();
            }
        }
        
        // Calculate consensus percentage
        let consensus = max_weight as f32 / total_weight as f32;
        
        // Determine result based on consensus threshold
        if consensus >= self.required_consensus {
            ConsensusResult::Valid
        } else if consensus > 0.0 {
            ConsensusResult::Uncertain
        } else {
            ConsensusResult::Invalid
        }
    }
    
    /// Get all known validator nodes
    pub fn nodes(&self) -> &HashMap<String, ValidatorNode> {
        &self.nodes
    }
    
    /// Get the required consensus threshold
    pub fn required_consensus(&self) -> f32 {
        self.required_consensus
    }
    
    /// Set the required consensus threshold
    pub fn set_required_consensus(&mut self, consensus: f32) {
        self.required_consensus = consensus.max(0.0).min(1.0);
    }
}
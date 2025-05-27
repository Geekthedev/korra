//! Agent definition, lifecycle, and logic routing

use std::collections::HashMap;
use std::error::Error;
use std::fmt;
use std::sync::{Arc, Mutex};

use crate::sandbox::wasm_host::WasmHost;
use crate::verifier::proof::ExecutionProof;
use crate::state::core::StateStore;

/// Error type for agent operations
#[derive(Debug)]
pub enum AgentError {
    InitError(String),
    ExecutionError(String),
    StateError(String),
    SandboxError(String),
    InvalidInput(String),
}

impl fmt::Display for AgentError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            AgentError::InitError(msg) => write!(f, "Agent initialization error: {}", msg),
            AgentError::ExecutionError(msg) => write!(f, "Agent execution error: {}", msg),
            AgentError::StateError(msg) => write!(f, "Agent state error: {}", msg),
            AgentError::SandboxError(msg) => write!(f, "Agent sandbox error: {}", msg),
            AgentError::InvalidInput(msg) => write!(f, "Invalid input: {}", msg),
        }
    }
}

impl Error for AgentError {}

/// Agent types supported by the system
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum AgentType {
    Analyzer,
    Transformer,
    Validator,
    Coordinator,
    Custom,
}

impl AgentType {
    pub fn from_str(s: &str) -> Option<Self> {
        match s.to_lowercase().as_str() {
            "analyzer" => Some(AgentType::Analyzer),
            "transformer" => Some(AgentType::Transformer),
            "validator" => Some(AgentType::Validator),
            "coordinator" => Some(AgentType::Coordinator),
            "custom" => Some(AgentType::Custom),
            _ => None,
        }
    }
}

/// Agent definition for KORRA
pub struct Agent {
    id: String,
    agent_type: AgentType,
    config: HashMap<String, String>,
    state: Arc<Mutex<StateStore>>,
    sandbox: WasmHost,
    last_execution: Option<ExecutionProof>,
}

impl Agent {
    /// Create a new agent instance
    pub fn new(agent_type_str: &str, config_json: &str) -> Result<Self, AgentError> {
        // Parse agent type
        let agent_type = AgentType::from_str(agent_type_str).ok_or_else(|| {
            AgentError::InitError(format!("Unsupported agent type: {}", agent_type_str))
        })?;
        
        // Parse config JSON
        let config: HashMap<String, String> = match serde_json::from_str(config_json) {
            Ok(c) => c,
            Err(e) => {
                return Err(AgentError::InitError(format!("Invalid config JSON: {}", e)));
            }
        };
        
        // Get agent ID from config
        let id = config.get("id").cloned().unwrap_or_else(|| {
            let uuid = uuid::Uuid::new_v4();
            uuid.to_string()
        });
        
        // Create state store
        let state = Arc::new(Mutex::new(StateStore::new()));
        
        // Initialize WASM sandbox
        let wasm_path = config.get("wasm_path").ok_or_else(|| {
            AgentError::InitError("Missing wasm_path in config".to_string())
        })?;
        
        let sandbox = match WasmHost::new(wasm_path) {
            Ok(s) => s,
            Err(e) => {
                return Err(AgentError::SandboxError(format!("Failed to create WASM host: {}", e)));
            }
        };
        
        Ok(Agent {
            id,
            agent_type,
            config,
            state,
            sandbox,
            last_execution: None,
        })
    }
    
    /// Execute the agent with the provided input
    pub fn execute(&mut self, input: &[u8]) -> Result<Vec<u8>, AgentError> {
        // Create execution context
        let mut context = ExecutionContext {
            agent_id: &self.id,
            agent_type: self.agent_type,
            input,
            state: self.state.clone(),
        };
        
        // Execute in sandbox
        let result = match self.sandbox.execute(&mut context) {
            Ok(r) => r,
            Err(e) => {
                return Err(AgentError::ExecutionError(format!("Sandbox execution failed: {}", e)));
            }
        };
        
        // Generate execution proof
        let proof = ExecutionProof::new(&self.id, input, &result);
        self.last_execution = Some(proof);
        
        Ok(result)
    }
    
    /// Get the last execution proof
    pub fn get_last_proof(&self) -> Option<&ExecutionProof> {
        self.last_execution.as_ref()
    }
    
    /// Get agent ID
    pub fn id(&self) -> &str {
        &self.id
    }
    
    /// Get agent type
    pub fn agent_type(&self) -> AgentType {
        self.agent_type
    }
    
    /// Get agent configuration
    pub fn config(&self) -> &HashMap<String, String> {
        &self.config
    }
}

/// Execution context for agent
pub struct ExecutionContext<'a> {
    pub agent_id: &'a str,
    pub agent_type: AgentType,
    pub input: &'a [u8],
    pub state: Arc<Mutex<StateStore>>,
}
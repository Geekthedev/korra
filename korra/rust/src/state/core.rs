//! State store and snapshot logic

use std::collections::HashMap;
use std::sync::{Arc, Mutex};
use std::time::{SystemTime, UNIX_EPOCH};

/// State store for agent state
pub struct StateStore {
    values: HashMap<String, Vec<u8>>,
    snapshots: Vec<StateSnapshot>,
    snapshot_limit: usize,
}

/// State snapshot for rollback
struct StateSnapshot {
    timestamp: u64,
    values: HashMap<String, Vec<u8>>,
}

impl StateStore {
    /// Create a new state store
    pub fn new() -> Self {
        StateStore {
            values: HashMap::new(),
            snapshots: Vec::new(),
            snapshot_limit: 10, // Keep up to 10 snapshots
        }
    }
    
    /// Set a value in the state store
    pub fn set(&mut self, key: &str, value: &[u8]) {
        self.values.insert(key.to_string(), value.to_vec());
    }
    
    /// Get a value from the state store
    pub fn get(&self, key: &str) -> Option<Vec<u8>> {
        self.values.get(key).cloned()
    }
    
    /// Delete a value from the state store
    pub fn delete(&mut self, key: &str) -> bool {
        self.values.remove(key).is_some()
    }
    
    /// Create a snapshot of the current state
    pub fn create_snapshot(&mut self) -> u64 {
        // Get current timestamp
        let timestamp = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap_or_default()
            .as_secs();
        
        // Create snapshot
        let snapshot = StateSnapshot {
            timestamp,
            values: self.values.clone(),
        };
        
        // Add snapshot to list
        self.snapshots.push(snapshot);
        
        // Trim snapshots if needed
        if self.snapshots.len() > self.snapshot_limit {
            self.snapshots.remove(0);
        }
        
        timestamp
    }
    
    /// Rollback to a previous snapshot
    pub fn rollback(&mut self, timestamp: u64) -> bool {
        // Find snapshot with timestamp
        if let Some(idx) = self.snapshots.iter().position(|s| s.timestamp == timestamp) {
            // Restore state from snapshot
            self.values = self.snapshots[idx].values.clone();
            
            // Remove all snapshots after this one
            self.snapshots.truncate(idx + 1);
            
            true
        } else {
            false
        }
    }
    
    /// Get all keys in the state store
    pub fn keys(&self) -> Vec<String> {
        self.values.keys().cloned().collect()
    }
    
    /// Get the number of entries in the state store
    pub fn size(&self) -> usize {
        self.values.len()
    }
    
    /// Clear all values in the state store
    pub fn clear(&mut self) {
        self.values.clear();
    }
    
    /// Get all available snapshot timestamps
    pub fn snapshot_timestamps(&self) -> Vec<u64> {
        self.snapshots.iter().map(|s| s.timestamp).collect()
    }
}

/// Thread-safe state store
pub struct ConcurrentStateStore {
    inner: Arc<Mutex<StateStore>>,
}

impl ConcurrentStateStore {
    /// Create a new concurrent state store
    pub fn new() -> Self {
        ConcurrentStateStore {
            inner: Arc::new(Mutex::new(StateStore::new())),
        }
    }
    
    /// Set a value in the state store
    pub fn set(&self, key: &str, value: &[u8]) -> Result<(), String> {
        let mut store = self.inner.lock().map_err(|e| e.to_string())?;
        store.set(key, value);
        Ok(())
    }
    
    /// Get a value from the state store
    pub fn get(&self, key: &str) -> Result<Option<Vec<u8>>, String> {
        let store = self.inner.lock().map_err(|e| e.to_string())?;
        Ok(store.get(key))
    }
    
    /// Create a snapshot of the current state
    pub fn create_snapshot(&self) -> Result<u64, String> {
        let mut store = self.inner.lock().map_err(|e| e.to_string())?;
        Ok(store.create_snapshot())
    }
    
    /// Rollback to a previous snapshot
    pub fn rollback(&self, timestamp: u64) -> Result<bool, String> {
        let mut store = self.inner.lock().map_err(|e| e.to_string())?;
        Ok(store.rollback(timestamp))
    }
    
    /// Get the underlying state store
    pub fn inner(&self) -> Arc<Mutex<StateStore>> {
        self.inner.clone()
    }
}
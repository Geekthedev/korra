//! KORRA Rust agent engine and execution layer
//! 
//! This crate provides the secure agent execution environment,
//! WASM sandbox, and state management for KORRA.

use std::ffi::{c_void, CStr, CString};
use std::os::raw::{c_char, c_int};
use std::slice;
use std::ptr;

pub mod engine;
pub mod sandbox;
pub mod verifier;
pub mod state;
pub mod interop;
pub mod validator;

// FFI exports for C interop
#[no_mangle]
pub extern "C" fn rust_agent_create(
    agent_type: *const c_char,
    config: *const c_char
) -> *mut c_void {
    // Safety checks
    if agent_type.is_null() || config.is_null() {
        log_error("Null pointer passed to rust_agent_create");
        return ptr::null_mut();
    }
    
    // Convert C strings to Rust strings
    let agent_type_str = unsafe { CStr::from_ptr(agent_type) }.to_str();
    let config_str = unsafe { CStr::from_ptr(config) }.to_str();
    
    if agent_type_str.is_err() || config_str.is_err() {
        log_error("Invalid UTF-8 in agent_type or config");
        return ptr::null_mut();
    }
    
    let agent_type_str = agent_type_str.unwrap();
    let config_str = config_str.unwrap();
    
    log_info(&format!("Creating agent of type '{}' with config", agent_type_str));
    
    // Create agent instance
    match engine::agent::Agent::new(agent_type_str, config_str) {
        Ok(agent) => {
            // Box the agent and return a raw pointer
            let boxed = Box::new(agent);
            Box::into_raw(boxed) as *mut c_void
        }
        Err(e) => {
            log_error(&format!("Failed to create agent: {}", e));
            ptr::null_mut()
        }
    }
}

#[no_mangle]
pub extern "C" fn rust_agent_execute(
    handle: *mut c_void,
    input: *const u8,
    input_size: usize,
    output: *mut *mut u8,
    output_size: *mut usize
) -> c_int {
    // Safety checks
    if handle.is_null() || (input.is_null() && input_size > 0) || output.is_null() || output_size.is_null() {
        log_error("Null pointer passed to rust_agent_execute");
        return -1;
    }
    
    // Get agent from handle
    let agent = unsafe { &mut *(handle as *mut engine::agent::Agent) };
    
    // Convert input to Rust slice
    let input_slice = if input.is_null() {
        &[]
    } else {
        unsafe { slice::from_raw_parts(input, input_size) }
    };
    
    log_debug(&format!("Executing agent with {} bytes of input", input_size));
    
    // Execute agent
    match agent.execute(input_slice) {
        Ok(result) => {
            // Allocate memory for output
            let result_len = result.len();
            let result_ptr = unsafe { alloc(result_len) };
            
            if result_ptr.is_null() {
                log_error("Failed to allocate memory for agent output");
                return -1;
            }
            
            // Copy result to output buffer
            unsafe {
                ptr::copy_nonoverlapping(result.as_ptr(), result_ptr, result_len);
                *output = result_ptr;
                *output_size = result_len;
            }
            
            0 // Success
        }
        Err(e) => {
            log_error(&format!("Agent execution failed: {}", e));
            -1 // Error
        }
    }
}

#[no_mangle]
pub extern "C" fn rust_agent_destroy(handle: *mut c_void) {
    if handle.is_null() {
        log_error("Null pointer passed to rust_agent_destroy");
        return;
    }
    
    log_debug("Destroying agent");
    
    // Safely drop the Box
    unsafe {
        let _ = Box::from_raw(handle as *mut engine::agent::Agent);
    }
}

// FFI functions to call C code

// Log level constants
const LOG_LEVEL_DEBUG: i32 = 0;
const LOG_LEVEL_INFO: i32 = 1;
const LOG_LEVEL_WARN: i32 = 2;
const LOG_LEVEL_ERROR: i32 = 3;
const LOG_LEVEL_FATAL: i32 = 4;

// External C functions
extern "C" {
    fn c_log_callback(level: c_int, message: *const c_char);
    fn c_alloc_callback(size: usize) -> *mut u8;
    fn c_free_callback(ptr: *mut c_void);
}

// Helper functions for logging
fn log_debug(message: &str) {
    let c_str = CString::new(message).unwrap_or_else(|_| CString::new("Invalid UTF-8 in log message").unwrap());
    unsafe { c_log_callback(LOG_LEVEL_DEBUG, c_str.as_ptr()) };
}

fn log_info(message: &str) {
    let c_str = CString::new(message).unwrap_or_else(|_| CString::new("Invalid UTF-8 in log message").unwrap());
    unsafe { c_log_callback(LOG_LEVEL_INFO, c_str.as_ptr()) };
}

fn log_warn(message: &str) {
    let c_str = CString::new(message).unwrap_or_else(|_| CString::new("Invalid UTF-8 in log message").unwrap());
    unsafe { c_log_callback(LOG_LEVEL_WARN, c_str.as_ptr()) };
}

fn log_error(message: &str) {
    let c_str = CString::new(message).unwrap_or_else(|_| CString::new("Invalid UTF-8 in log message").unwrap());
    unsafe { c_log_callback(LOG_LEVEL_ERROR, c_str.as_ptr()) };
}

// Helper functions for memory management
unsafe fn alloc(size: usize) -> *mut u8 {
    c_alloc_callback(size)
}

pub unsafe fn free(ptr: *mut c_void) {
    if !ptr.is_null() {
        c_free_callback(ptr);
    }
}
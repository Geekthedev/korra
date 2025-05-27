/**
 * @file rust_glue.c
 * @brief FFI for Rust agent engine
 * 
 * This module provides the C-side of the Foreign Function Interface
 * between C and Rust components of KORRA.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/rust_glue.h"
#include "../include/debug.h"

// FFI callback function pointers
static rust_agent_create_fn rust_agent_create = NULL;
static rust_agent_execute_fn rust_agent_execute = NULL;
static rust_agent_destroy_fn rust_agent_destroy = NULL;

// Initialize Rust FFI
int rust_ffi_init(rust_ffi_callbacks_t callbacks) {
    DEBUG_LOG("Initializing Rust FFI");
    
    // Store callback function pointers
    rust_agent_create = callbacks.agent_create;
    rust_agent_execute = callbacks.agent_execute;
    rust_agent_destroy = callbacks.agent_destroy;
    
    if (!rust_agent_create || !rust_agent_execute || !rust_agent_destroy) {
        ERROR_LOG("Invalid Rust FFI callbacks");
        return -1;
    }
    
    INFO_LOG("Rust FFI initialized successfully");
    return 0;
}

// Create a new agent instance
agent_handle_t create_agent(const char* agent_type, const char* config) {
    if (!rust_agent_create) {
        ERROR_LOG("Rust FFI not initialized");
        return NULL;
    }
    
    DEBUG_LOG("Creating agent of type '%s'", agent_type);
    return rust_agent_create(agent_type, config);
}

// Execute an agent with provided input
int execute_agent(agent_handle_t handle, const void* input, size_t input_size,
                  void** output, size_t* output_size) {
    if (!rust_agent_execute) {
        ERROR_LOG("Rust FFI not initialized");
        return -1;
    }
    
    if (!handle) {
        ERROR_LOG("Invalid agent handle");
        return -1;
    }
    
    DEBUG_LOG("Executing agent with %zu bytes of input", input_size);
    return rust_agent_execute(handle, input, input_size, output, output_size);
}

// Destroy an agent instance
void destroy_agent(agent_handle_t handle) {
    if (!rust_agent_destroy) {
        ERROR_LOG("Rust FFI not initialized");
        return;
    }
    
    if (!handle) {
        ERROR_LOG("Invalid agent handle");
        return;
    }
    
    DEBUG_LOG("Destroying agent");
    rust_agent_destroy(handle);
}

// C callback for Rust to call
void c_log_callback(int level, const char* message) {
    switch (level) {
        case 0: DEBUG_LOG("[Rust] %s", message); break;
        case 1: INFO_LOG("[Rust] %s", message); break;
        case 2: WARN_LOG("[Rust] %s", message); break;
        case 3: ERROR_LOG("[Rust] %s", message); break;
        case 4: FATAL_LOG("[Rust] %s", message); break;
        default: INFO_LOG("[Rust] %s", message); break;
    }
}

// C callback for memory allocation (used by Rust)
void* c_alloc_callback(size_t size) {
    void* ptr = malloc(size);
    if (!ptr) {
        ERROR_LOG("Failed to allocate %zu bytes", size);
        return NULL;
    }
    
    DEBUG_LOG("Allocated %zu bytes at %p", size, ptr);
    return ptr;
}

// C callback for memory deallocation (used by Rust)
void c_free_callback(void* ptr) {
    if (!ptr) {
        ERROR_LOG("Attempt to free NULL pointer");
        return;
    }
    
    DEBUG_LOG("Freeing memory at %p", ptr);
    free(ptr);
}
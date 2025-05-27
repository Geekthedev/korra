/**
 * @file rust_glue.h
 * @brief FFI interface for Rust agent engine
 */

#ifndef KORRA_RUST_GLUE_H
#define KORRA_RUST_GLUE_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// Opaque agent handle
typedef void* agent_handle_t;

// Function types for Rust callbacks
typedef agent_handle_t (*rust_agent_create_fn)(const char*, const char*);
typedef int (*rust_agent_execute_fn)(agent_handle_t, const void*, size_t, void**, size_t*);
typedef void (*rust_agent_destroy_fn)(agent_handle_t);

// Struct containing Rust callback functions
typedef struct {
    rust_agent_create_fn agent_create;
    rust_agent_execute_fn agent_execute;
    rust_agent_destroy_fn agent_destroy;
} rust_ffi_callbacks_t;

/**
 * Initialize the Rust FFI
 * 
 * @param callbacks Struct containing Rust callback functions
 * @return 0 on success, -1 on failure
 */
int rust_ffi_init(rust_ffi_callbacks_t callbacks);

/**
 * Create a new agent instance
 * 
 * @param agent_type Type of agent to create
 * @param config Agent configuration JSON string
 * @return Handle to the created agent, or NULL on failure
 */
agent_handle_t create_agent(const char* agent_type, const char* config);

/**
 * Execute an agent with provided input
 * 
 * @param handle Agent handle
 * @param input Input data
 * @param input_size Size of input data
 * @param output Pointer to store output data (caller must free)
 * @param output_size Pointer to store output data size
 * @return 0 on success, -1 on failure
 */
int execute_agent(agent_handle_t handle, const void* input, size_t input_size,
                  void** output, size_t* output_size);

/**
 * Destroy an agent instance
 * 
 * @param handle Agent handle
 */
void destroy_agent(agent_handle_t handle);

/**
 * C callback for Rust to call for logging
 * 
 * @param level Log level
 * @param message Log message
 */
void c_log_callback(int level, const char* message);

/**
 * C callback for memory allocation (used by Rust)
 * 
 * @param size Size to allocate
 * @return Pointer to allocated memory
 */
void* c_alloc_callback(size_t size);

/**
 * C callback for memory deallocation (used by Rust)
 * 
 * @param ptr Pointer to memory to free
 */
void c_free_callback(void* ptr);

#ifdef __cplusplus
}
#endif

#endif // KORRA_RUST_GLUE_H
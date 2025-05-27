/**
 * @file debug.c
 * @brief Tracing and fail-safe interrupt handling for KORRA
 */

#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <execinfo.h>
#include <unistd.h>
#include "../include/debug.h"

// Maximum stack frames to trace
#define MAX_STACK_FRAMES 64

// Signal handler for crashes
static void crash_handler(int sig) {
    void* stack_frames[MAX_STACK_FRAMES];
    int frame_count;
    
    // Get stack trace
    frame_count = backtrace(stack_frames, MAX_STACK_FRAMES);
    
    // Log crash information
    FATAL_LOG("Caught signal %d, process crashing", sig);
    
    // Print stack trace to stderr
    fprintf(stderr, "Stack trace:\n");
    backtrace_symbols_fd(stack_frames, frame_count, STDERR_FILENO);
    
    // Exit process
    exit(1);
}

// Initialize debug and crash handling
void debug_init() {
    // Register signal handlers
    signal(SIGSEGV, crash_handler);
    signal(SIGABRT, crash_handler);
    signal(SIGFPE, crash_handler);
    signal(SIGILL, crash_handler);
    signal(SIGBUS, crash_handler);
    
    INFO_LOG("Debug and crash handling initialized");
}

// Dump current process memory map
void debug_dump_memmap() {
    FILE* maps_file = fopen("/proc/self/maps", "r");
    if (!maps_file) {
        ERROR_LOG("Failed to open memory map file");
        return;
    }
    
    char line[256];
    fprintf(stderr, "--- Memory Map Start ---\n");
    while (fgets(line, sizeof(line), maps_file)) {
        fprintf(stderr, "%s", line);
    }
    fprintf(stderr, "--- Memory Map End ---\n");
    
    fclose(maps_file);
}

// Set current thread name for debugging
void debug_set_thread_name(const char* name) {
#ifdef __linux__
    pthread_setname_np(pthread_self(), name);
    DEBUG_LOG("Set thread name to '%s'", name);
#endif
}

// Dump hex representation of memory region
void debug_hexdump(const void* data, size_t size) {
    const unsigned char* buf = (const unsigned char*)data;
    size_t i, j;
    
    fprintf(stderr, "Hexdump of %zu bytes at %p:\n", size, data);
    
    for (i = 0; i < size; i += 16) {
        fprintf(stderr, "%04zx: ", i);
        
        // Print hex values
        for (j = 0; j < 16; j++) {
            if (i + j < size) {
                fprintf(stderr, "%02x ", buf[i + j]);
            } else {
                fprintf(stderr, "   ");
            }
        }
        
        fprintf(stderr, " | ");
        
        // Print ASCII representation
        for (j = 0; j < 16; j++) {
            if (i + j < size) {
                char c = buf[i + j];
                fprintf(stderr, "%c", (c >= 32 && c <= 126) ? c : '.');
            } else {
                fprintf(stderr, " ");
            }
        }
        
        fprintf(stderr, "\n");
    }
}
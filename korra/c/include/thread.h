/**
 * @file thread.h
 * @brief Thread pool and task management for KORRA
 */

#ifndef KORRA_THREAD_H
#define KORRA_THREAD_H

#ifdef __cplusplus
extern "C" {
#endif

// Task function type
typedef void (*task_function_t)(void*);

// Task structure
typedef struct {
    char name[64];
    task_function_t function;
    void* arg;
} thread_task_t;

/**
 * Initialize the thread pool
 * 
 * @return 0 on success, -1 on failure
 */
int thread_pool_init();

/**
 * Submit a task to the thread pool
 * 
 * @param task The task to submit
 * @return 0 on success, -1 on failure
 */
int thread_pool_submit(thread_task_t task);

/**
 * Shutdown the thread pool
 * 
 * @return 0 on success, -1 on failure
 */
int thread_pool_shutdown();

#ifdef __cplusplus
}
#endif

#endif // KORRA_THREAD_H
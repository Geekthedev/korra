/**
 * @file thread.c
 * @brief Event loop and thread pool engine for KORRA
 * 
 * This module handles the core threading and event loop mechanisms
 * for the KORRA system.
 */

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <stdbool.h>
#include <string.h>
#include "../include/thread.h"
#include "../include/debug.h"

// Thread pool configuration
#define MAX_THREADS 8
#define MAX_QUEUE 256

// Thread pool state
typedef struct {
    thread_task_t queue[MAX_QUEUE];
    int queue_size;
    int head;
    int tail;
    pthread_t threads[MAX_THREADS];
    pthread_mutex_t queue_mutex;
    pthread_cond_t queue_not_empty;
    pthread_cond_t queue_not_full;
    bool shutdown;
} thread_pool_t;

static thread_pool_t pool;

// Thread worker function
static void* thread_worker(void* arg) {
    while (true) {
        pthread_mutex_lock(&pool.queue_mutex);
        
        // Wait for tasks
        while (pool.queue_size == 0 && !pool.shutdown) {
            pthread_cond_wait(&pool.queue_not_empty, &pool.queue_mutex);
        }
        
        // Check for shutdown
        if (pool.shutdown && pool.queue_size == 0) {
            pthread_mutex_unlock(&pool.queue_mutex);
            pthread_exit(NULL);
        }
        
        // Get task from queue
        thread_task_t task = pool.queue[pool.head];
        pool.head = (pool.head + 1) % MAX_QUEUE;
        pool.queue_size--;
        
        // Signal that queue is not full
        pthread_cond_signal(&pool.queue_not_full);
        pthread_mutex_unlock(&pool.queue_mutex);
        
        // Execute task
        DEBUG_LOG("Executing task %s", task.name);
        task.function(task.arg);
    }
    
    return NULL;
}

int thread_pool_init() {
    DEBUG_LOG("Initializing thread pool");
    
    // Initialize pool state
    memset(&pool, 0, sizeof(thread_pool_t));
    pool.shutdown = false;
    
    // Initialize mutex and condition variables
    if (pthread_mutex_init(&pool.queue_mutex, NULL) != 0) {
        perror("Failed to initialize queue mutex");
        return -1;
    }
    
    if (pthread_cond_init(&pool.queue_not_empty, NULL) != 0) {
        perror("Failed to initialize queue not empty condition");
        return -1;
    }
    
    if (pthread_cond_init(&pool.queue_not_full, NULL) != 0) {
        perror("Failed to initialize queue not full condition");
        return -1;
    }
    
    // Create worker threads
    for (int i = 0; i < MAX_THREADS; i++) {
        if (pthread_create(&pool.threads[i], NULL, thread_worker, NULL) != 0) {
            perror("Failed to create worker thread");
            return -1;
        }
    }
    
    DEBUG_LOG("Thread pool initialized with %d threads", MAX_THREADS);
    return 0;
}

int thread_pool_submit(thread_task_t task) {
    pthread_mutex_lock(&pool.queue_mutex);
    
    // Wait if queue is full
    while (pool.queue_size == MAX_QUEUE && !pool.shutdown) {
        pthread_cond_wait(&pool.queue_not_full, &pool.queue_mutex);
    }
    
    // Don't accept new tasks if shutting down
    if (pool.shutdown) {
        pthread_mutex_unlock(&pool.queue_mutex);
        return -1;
    }
    
    // Add task to queue
    pool.queue[pool.tail] = task;
    pool.tail = (pool.tail + 1) % MAX_QUEUE;
    pool.queue_size++;
    
    DEBUG_LOG("Task %s added to queue, size: %d", task.name, pool.queue_size);
    
    // Signal that queue is not empty
    pthread_cond_signal(&pool.queue_not_empty);
    pthread_mutex_unlock(&pool.queue_mutex);
    
    return 0;
}

int thread_pool_shutdown() {
    DEBUG_LOG("Shutting down thread pool");
    
    pthread_mutex_lock(&pool.queue_mutex);
    pool.shutdown = true;
    pthread_cond_broadcast(&pool.queue_not_empty);
    pthread_cond_broadcast(&pool.queue_not_full);
    pthread_mutex_unlock(&pool.queue_mutex);
    
    // Wait for threads to finish
    for (int i = 0; i < MAX_THREADS; i++) {
        if (pthread_join(pool.threads[i], NULL) != 0) {
            perror("Failed to join worker thread");
            return -1;
        }
    }
    
    // Destroy mutex and condition variables
    pthread_mutex_destroy(&pool.queue_mutex);
    pthread_cond_destroy(&pool.queue_not_empty);
    pthread_cond_destroy(&pool.queue_not_full);
    
    DEBUG_LOG("Thread pool shutdown complete");
    return 0;
}
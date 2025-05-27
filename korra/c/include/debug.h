/**
 * @file debug.h
 * @brief Debug and logging utilities for KORRA
 */

#ifndef KORRA_DEBUG_H
#define KORRA_DEBUG_H

#include <stdio.h>
#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

// Debug log levels
typedef enum {
    LOG_LEVEL_DEBUG,
    LOG_LEVEL_INFO,
    LOG_LEVEL_WARN,
    LOG_LEVEL_ERROR,
    LOG_LEVEL_FATAL
} log_level_t;

// Current log level (default: debug in development)
#ifndef NDEBUG
#define CURRENT_LOG_LEVEL LOG_LEVEL_DEBUG
#else
#define CURRENT_LOG_LEVEL LOG_LEVEL_INFO
#endif

// Debug log macro
#define DEBUG_LOG(fmt, ...) do { \
    if (CURRENT_LOG_LEVEL <= LOG_LEVEL_DEBUG) { \
        time_t t = time(NULL); \
        struct tm* tm_info = localtime(&t); \
        char time_str[20]; \
        strftime(time_str, 20, "%Y-%m-%d %H:%M:%S", tm_info); \
        fprintf(stderr, "[%s][DEBUG][%s:%d] " fmt "\n", \
                time_str, __FILE__, __LINE__, ##__VA_ARGS__); \
    } \
} while (0)

// Info log macro
#define INFO_LOG(fmt, ...) do { \
    if (CURRENT_LOG_LEVEL <= LOG_LEVEL_INFO) { \
        time_t t = time(NULL); \
        struct tm* tm_info = localtime(&t); \
        char time_str[20]; \
        strftime(time_str, 20, "%Y-%m-%d %H:%M:%S", tm_info); \
        fprintf(stderr, "[%s][INFO] " fmt "\n", \
                time_str, ##__VA_ARGS__); \
    } \
} while (0)

// Warning log macro
#define WARN_LOG(fmt, ...) do { \
    if (CURRENT_LOG_LEVEL <= LOG_LEVEL_WARN) { \
        time_t t = time(NULL); \
        struct tm* tm_info = localtime(&t); \
        char time_str[20]; \
        strftime(time_str, 20, "%Y-%m-%d %H:%M:%S", tm_info); \
        fprintf(stderr, "[%s][WARN][%s:%d] " fmt "\n", \
                time_str, __FILE__, __LINE__, ##__VA_ARGS__); \
    } \
} while (0)

// Error log macro
#define ERROR_LOG(fmt, ...) do { \
    if (CURRENT_LOG_LEVEL <= LOG_LEVEL_ERROR) { \
        time_t t = time(NULL); \
        struct tm* tm_info = localtime(&t); \
        char time_str[20]; \
        strftime(time_str, 20, "%Y-%m-%d %H:%M:%S", tm_info); \
        fprintf(stderr, "[%s][ERROR][%s:%d] " fmt "\n", \
                time_str, __FILE__, __LINE__, ##__VA_ARGS__); \
    } \
} while (0)

// Fatal log macro
#define FATAL_LOG(fmt, ...) do { \
    if (CURRENT_LOG_LEVEL <= LOG_LEVEL_FATAL) { \
        time_t t = time(NULL); \
        struct tm* tm_info = localtime(&t); \
        char time_str[20]; \
        strftime(time_str, 20, "%Y-%m-%d %H:%M:%S", tm_info); \
        fprintf(stderr, "[%s][FATAL][%s:%d] " fmt "\n", \
                time_str, __FILE__, __LINE__, ##__VA_ARGS__); \
    } \
    exit(1); \
} while (0)

#ifdef __cplusplus
}
#endif

#endif // KORRA_DEBUG_H
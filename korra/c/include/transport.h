/**
 * @file transport.h
 * @brief Binary transport protocol for KORRA
 */

#ifndef KORRA_TRANSPORT_H
#define KORRA_TRANSPORT_H

#include <stdbool.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

// Protocol version
#define KORRA_PROTOCOL_VERSION 1

// Message types
typedef enum {
    MSG_HEARTBEAT = 0,
    MSG_AGENT_REGISTER = 1,
    MSG_AGENT_UPDATE = 2,
    MSG_JOB_SUBMIT = 3,
    MSG_JOB_RESULT = 4,
    MSG_STATE_SYNC = 5,
    MSG_NODE_INFO = 6,
    MSG_ERROR = 255
} message_type_t;

// Message header
typedef struct {
    uint32_t magic;         // Magic number to identify KORRA messages
    uint8_t version;        // Protocol version
    uint8_t msg_type;       // Message type
    uint16_t reserved;      // Reserved for future use
    uint32_t payload_size;  // Size of payload in bytes
} transport_header_t;

// Message structure
typedef struct {
    uint8_t msg_type;       // Message type
    uint32_t payload_size;  // Size of payload in bytes
    void* payload;          // Payload data
} transport_message_t;

/**
 * Initialize the transport layer
 * 
 * @param port Port number to use
 * @param is_server Whether this instance is a server
 * @return 0 on success, -1 on failure
 */
int transport_init(int port, bool is_server);

/**
 * Send a message
 * 
 * @param message Message to send
 * @return 0 on success, -1 on failure
 */
int transport_send(const transport_message_t* message);

/**
 * Receive a message
 * 
 * @param message Message structure to fill
 * @return 0 on success, -1 on failure
 */
int transport_receive(transport_message_t* message);

/**
 * Accept an incoming connection (server only)
 * 
 * @param client_fd Pointer to store the client socket file descriptor
 * @return 0 on success, -1 on failure
 */
int transport_accept(int* client_fd);

/**
 * Close the transport connection
 */
void transport_close();

#ifdef __cplusplus
}
#endif

#endif // KORRA_TRANSPORT_H
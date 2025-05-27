/**
 * @file transport.c
 * @brief High-performance binary socket protocol for KORRA
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <errno.h>
#include "../include/transport.h"
#include "../include/debug.h"

// Message header magic number
#define KORRA_MSG_MAGIC 0x4B525241 // "KRRA"

// Socket buffer sizes
#define SOCKET_BUFFER_SIZE 8192

// Socket state
typedef struct {
    int socket_fd;
    struct sockaddr_in address;
    bool is_server;
    bool is_connected;
} transport_socket_t;

static transport_socket_t transport_socket;

int transport_init(int port, bool is_server) {
    DEBUG_LOG("Initializing transport on port %d, is_server: %d", port, is_server);
    
    // Initialize socket state
    memset(&transport_socket, 0, sizeof(transport_socket_t));
    transport_socket.is_server = is_server;
    transport_socket.is_connected = false;
    
    // Create socket
    transport_socket.socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (transport_socket.socket_fd < 0) {
        perror("Failed to create socket");
        return -1;
    }
    
    // Set socket options
    int opt = 1;
    if (setsockopt(transport_socket.socket_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("Failed to set socket options");
        close(transport_socket.socket_fd);
        return -1;
    }
    
    // Set up address
    transport_socket.address.sin_family = AF_INET;
    transport_socket.address.sin_addr.s_addr = INADDR_ANY;
    transport_socket.address.sin_port = htons(port);
    
    if (is_server) {
        // Bind socket
        if (bind(transport_socket.socket_fd, (struct sockaddr*)&transport_socket.address, sizeof(transport_socket.address)) < 0) {
            perror("Failed to bind socket");
            close(transport_socket.socket_fd);
            return -1;
        }
        
        // Listen for connections
        if (listen(transport_socket.socket_fd, 10) < 0) {
            perror("Failed to listen on socket");
            close(transport_socket.socket_fd);
            return -1;
        }
        
        DEBUG_LOG("Transport initialized as server on port %d", port);
    } else {
        // Connect to server
        if (connect(transport_socket.socket_fd, (struct sockaddr*)&transport_socket.address, sizeof(transport_socket.address)) < 0) {
            perror("Failed to connect to server");
            close(transport_socket.socket_fd);
            return -1;
        }
        
        transport_socket.is_connected = true;
        DEBUG_LOG("Transport initialized as client, connected to port %d", port);
    }
    
    return 0;
}

int transport_send(const transport_message_t* message) {
    if (!transport_socket.is_connected) {
        DEBUG_LOG("Cannot send message, not connected");
        return -1;
    }
    
    // Prepare header
    transport_header_t header;
    header.magic = KORRA_MSG_MAGIC;
    header.version = KORRA_PROTOCOL_VERSION;
    header.msg_type = message->msg_type;
    header.payload_size = message->payload_size;
    
    // Send header
    if (send(transport_socket.socket_fd, &header, sizeof(header), 0) != sizeof(header)) {
        perror("Failed to send message header");
        return -1;
    }
    
    // Send payload
    if (message->payload_size > 0) {
        if (send(transport_socket.socket_fd, message->payload, message->payload_size, 0) != message->payload_size) {
            perror("Failed to send message payload");
            return -1;
        }
    }
    
    DEBUG_LOG("Sent message type %d, size %d", message->msg_type, message->payload_size);
    return 0;
}

int transport_receive(transport_message_t* message) {
    if (!transport_socket.is_connected) {
        DEBUG_LOG("Cannot receive message, not connected");
        return -1;
    }
    
    // Receive header
    transport_header_t header;
    ssize_t bytes_received = recv(transport_socket.socket_fd, &header, sizeof(header), 0);
    
    if (bytes_received != sizeof(header)) {
        if (bytes_received == 0) {
            DEBUG_LOG("Connection closed by peer");
            transport_socket.is_connected = false;
        } else {
            perror("Failed to receive message header");
        }
        return -1;
    }
    
    // Validate header
    if (header.magic != KORRA_MSG_MAGIC) {
        DEBUG_LOG("Invalid message magic: %08x", header.magic);
        return -1;
    }
    
    if (header.version != KORRA_PROTOCOL_VERSION) {
        DEBUG_LOG("Unsupported protocol version: %d", header.version);
        return -1;
    }
    
    // Copy header fields to message
    message->msg_type = header.msg_type;
    message->payload_size = header.payload_size;
    
    // Allocate and receive payload if present
    if (header.payload_size > 0) {
        message->payload = malloc(header.payload_size);
        if (!message->payload) {
            DEBUG_LOG("Failed to allocate %d bytes for payload", header.payload_size);
            return -1;
        }
        
        bytes_received = recv(transport_socket.socket_fd, message->payload, header.payload_size, 0);
        if (bytes_received != header.payload_size) {
            if (bytes_received == 0) {
                DEBUG_LOG("Connection closed by peer during payload receive");
                transport_socket.is_connected = false;
            } else {
                perror("Failed to receive message payload");
            }
            free(message->payload);
            message->payload = NULL;
            return -1;
        }
    } else {
        message->payload = NULL;
    }
    
    DEBUG_LOG("Received message type %d, size %d", message->msg_type, message->payload_size);
    return 0;
}

int transport_accept(int* client_fd) {
    if (!transport_socket.is_server) {
        DEBUG_LOG("Cannot accept connections, not a server");
        return -1;
    }
    
    // Accept incoming connection
    struct sockaddr_in client_addr;
    socklen_t client_addr_len = sizeof(client_addr);
    *client_fd = accept(transport_socket.socket_fd, (struct sockaddr*)&client_addr, &client_addr_len);
    
    if (*client_fd < 0) {
        perror("Failed to accept connection");
        return -1;
    }
    
    DEBUG_LOG("Accepted connection from %s:%d", 
              inet_ntoa(client_addr.sin_addr), ntohs(client_addr.sin_port));
    return 0;
}

void transport_close() {
    DEBUG_LOG("Closing transport");
    
    if (transport_socket.socket_fd > 0) {
        close(transport_socket.socket_fd);
        transport_socket.socket_fd = -1;
    }
    
    transport_socket.is_connected = false;
}
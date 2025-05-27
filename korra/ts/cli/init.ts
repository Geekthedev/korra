/**
 * Environment and node boot setup
 */

import fs from 'fs';
import path from 'path';
import { RpcClient } from '../rpc/client';
import os from 'os';

// Configuration interface
interface InitConfig {
  coordinatorHost: string;
  coordinatorPort: number;
  nodePort: number;
  capabilities: string[];
}

/**
 * Initialize KORRA environment
 * 
 * @param configPath Path to configuration file
 * @returns Result of initialization
 */
export async function initEnvironment(configPath?: string): Promise<any> {
  console.log('Initializing KORRA environment');
  
  // Load configuration
  const config = loadConfig(configPath);
  
  console.log('Configuration loaded:');
  console.log(`  Coordinator: ${config.coordinatorHost}:${config.coordinatorPort}`);
  console.log(`  Node port: ${config.nodePort}`);
  console.log(`  Capabilities: ${config.capabilities.join(', ')}`);
  
  // Create directories
  createDirectories();
  
  // Initialize node
  const nodeInfo = await initNode(config);
  
  console.log('Node initialized:');
  console.log(`  Node ID: ${nodeInfo.nodeId}`);
  console.log(`  Hostname: ${nodeInfo.hostname}`);
  console.log(`  Address: ${nodeInfo.address}:${nodeInfo.port}`);
  
  return {
    success: true,
    nodeInfo,
  };
}

/**
 * Load configuration
 * 
 * @param configPath Path to configuration file
 * @returns Configuration
 */
function loadConfig(configPath?: string): InitConfig {
  // Default configuration
  const defaultConfig: InitConfig = {
    coordinatorHost: 'localhost',
    coordinatorPort: 8080,
    nodePort: 8081,
    capabilities: ['agent:analyzer', 'agent:transformer'],
  };
  
  if (!configPath) {
    return defaultConfig;
  }
  
  try {
    const configData = fs.readFileSync(configPath, 'utf8');
    const config = JSON.parse(configData);
    
    return {
      ...defaultConfig,
      ...config,
    };
  } catch (error) {
    console.warn(`Failed to load configuration from ${configPath}: ${error}`);
    return defaultConfig;
  }
}

/**
 * Create required directories
 */
function createDirectories(): void {
  const dirs = [
    'agents',
    'data',
    'logs',
    'snapshots',
  ];
  
  for (const dir of dirs) {
    const dirPath = path.join(process.cwd(), dir);
    if (!fs.existsSync(dirPath)) {
      console.log(`Creating directory: ${dirPath}`);
      fs.mkdirSync(dirPath, { recursive: true });
    }
  }
}

/**
 * Initialize node
 * 
 * @param config Configuration
 * @returns Node info
 */
async function initNode(config: InitConfig): Promise<any> {
  // Generate node ID
  const nodeId = generateNodeId();
  
  // Get hostname
  const hostname = os.hostname();
  
  // Get IP address
  const address = getLocalAddress();
  
  // Create node info
  const nodeInfo = {
    nodeId,
    hostname,
    address,
    port: config.nodePort,
    capabilities: config.capabilities,
  };
  
  // In a real implementation, this would register the node with the coordinator
  console.log('Node registration simulated');
  
  return nodeInfo;
}

/**
 * Generate a node ID
 * 
 * @returns Node ID
 */
function generateNodeId(): string {
  // Generate a random ID
  const id = Math.random().toString(36).substring(2, 15) + 
             Math.random().toString(36).substring(2, 15);
  
  return id;
}

/**
 * Get local IP address
 * 
 * @returns IP address
 */
function getLocalAddress(): string {
  const interfaces = os.networkInterfaces();
  
  for (const [name, netInterface] of Object.entries(interfaces)) {
    // Skip loopback interfaces
    if (name.includes('lo')) {
      continue;
    }
    
    // Find IPv4 address
    for (const iface of netInterface || []) {
      if (iface.family === 'IPv4' && !iface.internal) {
        return iface.address;
      }
    }
  }
  
  // Fallback to localhost
  return '127.0.0.1';
}
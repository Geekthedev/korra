/**
 * Inspect node state via RPC
 */

import { RpcClient } from '../rpc/client';

/**
 * Inspect node state
 * 
 * @param nodeId Node ID
 * @param host Host to connect to
 * @param port Port to connect to
 * @returns Node state
 */
export async function inspect(nodeId: string, host: string, port: number): Promise<any> {
  console.log(`Inspecting node ${nodeId} at ${host}:${port}`);
  
  // Create RPC client
  const client = new RpcClient(host, port);
  
  try {
    // Get node info
    const nodes = await client.getNodes();
    const node = nodes.find(n => n.nodeId === nodeId);
    
    if (!node) {
      throw new Error(`Node not found: ${nodeId}`);
    }
    
    // Get jobs assigned to the node
    const jobs = await client.getJobs();
    const nodeJobs = jobs.filter(job => job.executedByNodeId === nodeId);
    
    // Get agents on the node
    const agents = await client.getAgents();
    
    // Get proofs from the node
    const proofs = await client.getProofs();
    const nodeProofs = proofs.filter(proof => {
      // In a real implementation, we would filter proofs by node
      // For this demo, we'll just return all proofs
      return true;
    });
    
    // Build node state
    const state = {
      node,
      jobs: nodeJobs,
      agents,
      proofs: nodeProofs,
    };
    
    return state;
  } catch (error) {
    console.error('Failed to inspect node:', error);
    throw error;
  }
}
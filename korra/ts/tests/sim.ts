/**
 * Batch simulation and testing runner
 */

import { RpcClient } from '../rpc/client';

// Simulation configuration
interface SimulationConfig {
  nodes: number;
  agents: number;
  jobs: number;
  host: string;
  port: number;
}

/**
 * Run a simulation
 * 
 * @param config Simulation configuration
 * @returns Simulation results
 */
export async function runSimulation(config: SimulationConfig): Promise<any> {
  console.log('Starting simulation with configuration:');
  console.log(`  Nodes: ${config.nodes}`);
  console.log(`  Agents: ${config.agents}`);
  console.log(`  Jobs: ${config.jobs}`);
  console.log(`  Host: ${config.host}`);
  console.log(`  Port: ${config.port}`);
  
  // Create RPC client
  const client = new RpcClient(config.host, config.port);
  
  // Track timing statistics
  const startTime = Date.now();
  let jobSubmitTimes: number[] = [];
  let jobCompletionTimes: number[] = [];
  
  try {
    // Register agents
    console.log('Registering agents...');
    for (let i = 0; i < config.agents; i++) {
      const agentName = `SimAgent${i}`;
      const agentType = i % 2 === 0 ? 'analyzer' : 'transformer';
      const agentVersion = '1.0.0';
      
      await client.registerAgent({
        name: agentName,
        type: agentType,
        version: agentVersion,
      });
    }
    
    // Get registered agents
    const agents = await client.getAgents();
    console.log(`Registered ${agents.length} agents`);
    
    // Submit jobs
    console.log('Submitting jobs...');
    const jobIds: string[] = [];
    
    for (let i = 0; i < config.jobs; i++) {
      // Select an agent
      const agent = agents[i % agents.length];
      
      // Create job input
      const input = Buffer.from(`Job ${i} input data`);
      
      // Track submit time
      const submitStart = Date.now();
      
      // Submit job
      const jobId = await client.submitJob(agent.agentId, input);
      jobIds.push(jobId);
      
      // Track submit time
      const submitEnd = Date.now();
      jobSubmitTimes.push(submitEnd - submitStart);
    }
    
    console.log(`Submitted ${jobIds.length} jobs`);
    
    // Wait for all jobs to complete
    console.log('Waiting for jobs to complete...');
    
    for (const jobId of jobIds) {
      const waitStart = Date.now();
      
      try {
        await client.waitForJob(jobId, 500, 30000);
      } catch (error) {
        console.warn(`Job ${jobId} did not complete within timeout`);
      }
      
      const waitEnd = Date.now();
      jobCompletionTimes.push(waitEnd - waitStart);
    }
    
    // Calculate statistics
    const endTime = Date.now();
    const totalTime = endTime - startTime;
    
    const avgSubmitTime = jobSubmitTimes.length > 0 ? 
      jobSubmitTimes.reduce((a, b) => a + b, 0) / jobSubmitTimes.length : 0;
    
    const avgCompletionTime = jobCompletionTimes.length > 0 ? 
      jobCompletionTimes.reduce((a, b) => a + b, 0) / jobCompletionTimes.length : 0;
    
    // Build results
    const results = {
      totalTime,
      avgSubmitTime,
      avgCompletionTime,
      jobsPerSecond: (config.jobs / totalTime) * 1000,
    };
    
    console.log('Simulation complete');
    console.log(`Total time: ${totalTime}ms`);
    console.log(`Average submit time: ${avgSubmitTime.toFixed(2)}ms`);
    console.log(`Average completion time: ${avgCompletionTime.toFixed(2)}ms`);
    console.log(`Jobs per second: ${results.jobsPerSecond.toFixed(2)}`);
    
    return results;
  } catch (error) {
    console.error('Simulation failed:', error);
    throw error;
  }
}
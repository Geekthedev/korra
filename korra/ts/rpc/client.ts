/**
 * JSON-RPC 2.0 client for KORRA Java API
 */

import fetch from 'node-fetch';

export interface Agent {
  agentId: string;
  name: string;
  type: string;
  version: string;
  status: string;
}

export interface Node {
  nodeId: string;
  hostname: string;
  address: string;
  port: number;
  status: string;
}

export interface Job {
  jobId: string;
  agentId: string;
  status: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
  executedByNodeId?: string;
  errorMessage?: string;
  output?: string;
}

export interface Proof {
  proofId: string;
  agentId: string;
  timestamp: number;
  inputHash: string;
  outputHash: string;
  proofHash: string;
}

export class RpcClient {
  private readonly host: string;
  private readonly port: number;
  private readonly baseUrl: string;

  /**
   * Create a new RPC client
   * 
   * @param host Host to connect to
   * @param port Port to connect to
   */
  constructor(host: string, port: number) {
    this.host = host;
    this.port = port;
    this.baseUrl = `http://${host}:${port}/api`;
  }

  /**
   * Get all registered agents
   * 
   * @returns Array of agents
   */
  async getAgents(): Promise<Agent[]> {
    const response = await fetch(`${this.baseUrl}/agents`);
    const data = await response.json();
    return data.agents || [];
  }

  /**
   * Get all connected nodes
   * 
   * @returns Array of nodes
   */
  async getNodes(): Promise<Node[]> {
    const response = await fetch(`${this.baseUrl}/nodes`);
    const data = await response.json();
    return data.nodes || [];
  }

  /**
   * Get all active jobs
   * 
   * @returns Array of jobs
   */
  async getJobs(): Promise<Job[]> {
    const response = await fetch(`${this.baseUrl}/jobs`);
    const data = await response.json();
    return data.jobs || [];
  }

  /**
   * Get all execution proofs
   * 
   * @returns Array of proofs
   */
  async getProofs(): Promise<Proof[]> {
    const response = await fetch(`${this.baseUrl}/proofs`);
    const data = await response.json();
    return data.proofs || [];
  }

  /**
   * Submit a job to an agent
   * 
   * @param agentId Agent ID
   * @param input Input data
   * @returns Job ID
   */
  async submitJob(agentId: string, input: Buffer): Promise<string> {
    const response = await fetch(`${this.baseUrl}/jobs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        agentId,
        input: input.toString('base64'),
      }),
    });

    const data = await response.json();
    
    if (!data.jobId) {
      throw new Error('Failed to submit job');
    }
    
    return data.jobId;
  }

  /**
   * Wait for a job to complete
   * 
   * @param jobId Job ID
   * @param pollInterval Poll interval in milliseconds
   * @param timeout Timeout in milliseconds
   * @returns Completed job
   */
  async waitForJob(jobId: string, pollInterval = 1000, timeout = 60000): Promise<Job> {
    const startTime = Date.now();
    
    while (Date.now() - startTime < timeout) {
      // Get all jobs
      const jobs = await this.getJobs();
      
      // Find the job
      const job = jobs.find(j => j.jobId === jobId);
      
      // If job not found, it may have completed
      if (!job) {
        break;
      }
      
      // Check if job is completed or failed
      if (job.status === 'COMPLETED' || job.status === 'FAILED') {
        return job;
      }
      
      // Wait before polling again
      await new Promise(resolve => setTimeout(resolve, pollInterval));
    }
    
    throw new Error(`Job ${jobId} did not complete within timeout`);
  }

  /**
   * Register an agent
   * 
   * @param agent Agent definition
   * @returns True if registration was successful
   */
  async registerAgent(agent: Partial<Agent>): Promise<boolean> {
    const response = await fetch(`${this.baseUrl}/agents`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(agent),
    });

    const data = await response.json();
    return data.success === true;
  }
}
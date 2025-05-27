/**
 * Main CLI for KORRA operations
 */

import { Command } from 'commander';
import { inspect } from '../tools/inspect';
import { agentDeploy } from '../scripts/agentDeploy';
import { RpcClient } from '../rpc/client';
import fs from 'fs';
import path from 'path';

// Create command line interface
const program = new Command();

// Set program information
program
  .name('korra')
  .description('KORRA Command Line Interface')
  .version('0.1.0');

// Add commands
program
  .command('agent:deploy <path>')
  .description('Deploy a Rust agent')
  .option('-n, --name <name>', 'Agent name')
  .option('-t, --type <type>', 'Agent type', 'custom')
  .option('-v, --version <version>', 'Agent version', '1.0.0')
  .action(async (agentPath, options) => {
    try {
      const result = await agentDeploy(agentPath, options);
      console.log('Agent deployment result:', result);
    } catch (error) {
      console.error('Failed to deploy agent:', error);
      process.exit(1);
    }
  });

program
  .command('agent:list')
  .description('List all registered agents')
  .option('-h, --host <host>', 'Host to connect to', 'localhost')
  .option('-p, --port <port>', 'Port to connect to', '8080')
  .action(async (options) => {
    try {
      const client = new RpcClient(options.host, parseInt(options.port, 10));
      const agents = await client.getAgents();
      console.log('Registered Agents:');
      agents.forEach(agent => {
        console.log(`- ${agent.name} (${agent.agentId})`);
        console.log(`  Type: ${agent.type}`);
        console.log(`  Version: ${agent.version}`);
        console.log(`  Status: ${agent.status}`);
        console.log();
      });
    } catch (error) {
      console.error('Failed to list agents:', error);
      process.exit(1);
    }
  });

program
  .command('node:list')
  .description('List all connected nodes')
  .option('-h, --host <host>', 'Host to connect to', 'localhost')
  .option('-p, --port <port>', 'Port to connect to', '8080')
  .action(async (options) => {
    try {
      const client = new RpcClient(options.host, parseInt(options.port, 10));
      const nodes = await client.getNodes();
      console.log('Connected Nodes:');
      nodes.forEach(node => {
        console.log(`- ${node.hostname} (${node.nodeId})`);
        console.log(`  Address: ${node.address}:${node.port}`);
        console.log(`  Status: ${node.status}`);
        console.log();
      });
    } catch (error) {
      console.error('Failed to list nodes:', error);
      process.exit(1);
    }
  });

program
  .command('job:submit <agentId> <inputFile>')
  .description('Submit a job to an agent')
  .option('-h, --host <host>', 'Host to connect to', 'localhost')
  .option('-p, --port <port>', 'Port to connect to', '8080')
  .action(async (agentId, inputFile, options) => {
    try {
      // Read input file
      const input = fs.readFileSync(path.resolve(inputFile));
      
      // Create RPC client
      const client = new RpcClient(options.host, parseInt(options.port, 10));
      
      // Submit job
      const jobId = await client.submitJob(agentId, input);
      console.log(`Job submitted successfully with ID: ${jobId}`);
      
      // Wait for job completion
      console.log('Waiting for job completion...');
      const job = await client.waitForJob(jobId);
      
      console.log('Job completed:');
      console.log(`  Status: ${job.status}`);
      console.log(`  Started: ${job.startedAt}`);
      console.log(`  Completed: ${job.completedAt}`);
      
      if (job.status === 'COMPLETED') {
        console.log('Job output:');
        console.log(job.output);
      } else if (job.status === 'FAILED') {
        console.error('Job failed:', job.errorMessage);
      }
    } catch (error) {
      console.error('Failed to submit job:', error);
      process.exit(1);
    }
  });

program
  .command('job:list')
  .description('List all active jobs')
  .option('-h, --host <host>', 'Host to connect to', 'localhost')
  .option('-p, --port <port>', 'Port to connect to', '8080')
  .action(async (options) => {
    try {
      const client = new RpcClient(options.host, parseInt(options.port, 10));
      const jobs = await client.getJobs();
      console.log('Active Jobs:');
      jobs.forEach(job => {
        console.log(`- ${job.jobId} (Agent: ${job.agentId})`);
        console.log(`  Status: ${job.status}`);
        console.log(`  Created: ${job.createdAt}`);
        if (job.startedAt) {
          console.log(`  Started: ${job.startedAt}`);
        }
        if (job.completedAt) {
          console.log(`  Completed: ${job.completedAt}`);
        }
        console.log();
      });
    } catch (error) {
      console.error('Failed to list jobs:', error);
      process.exit(1);
    }
  });

program
  .command('inspect <nodeId>')
  .description('Inspect node state')
  .option('-h, --host <host>', 'Host to connect to', 'localhost')
  .option('-p, --port <port>', 'Port to connect to', '8080')
  .action(async (nodeId, options) => {
    try {
      const state = await inspect(nodeId, options.host, parseInt(options.port, 10));
      console.log('Node State:');
      console.log(JSON.stringify(state, null, 2));
    } catch (error) {
      console.error('Failed to inspect node:', error);
      process.exit(1);
    }
  });

// Parse command line arguments
program.parse(process.argv);

// If no command is provided, show help
if (process.argv.length <= 2) {
  program.help();
}
import { buildQueryParams } from './utils';

export class WorkflowClient {
  constructor(atlasClient) {
    this.client = atlasClient;
  }

  async execute(request) {
    return this.client.request('/api/v1/atlas/workflows/execute', {
      method: 'POST',
      body: JSON.stringify(request),
    });
  }

  async getStatus(executionId) {
    return this.client.request(`/api/v1/atlas/workflows/executions/${executionId}`, {
      method: 'GET',
    });
  }

  async getHistory() {
    return this.client.request('/api/v1/atlas/workflows/history', {
      method: 'GET',
    });
  }

  async cancel(executionId, reason = 'User requested cancellation') {
    const query = buildQueryParams({ reason });
    return this.client.request(`/api/v1/atlas/workflows/executions/${executionId}/cancel${query}`, {
      method: 'POST',
    });
  }
}

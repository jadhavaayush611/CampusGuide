import { ConversationClient } from './ConversationClient';
import { WorkflowClient } from './WorkflowClient';
import { StreamingClient } from './StreamingClient';
import { AtlasError } from './utils';

export class AtlasClient {
  constructor(config = {}) {
    this.baseUrl = config.baseUrl || '';
    this.getToken = config.getToken || (() => null);
    this.maxRetries = config.maxRetries ?? 2;
    this.timeoutMs = config.timeoutMs ?? 30000;

    this.conversations = new ConversationClient(this);
    this.workflows = new WorkflowClient(this);
    this.streaming = new StreamingClient(this);
  }

  async getHeaders(customHeaders = {}) {
    const headers = {
      'Content-Type': 'application/json',
      ...customHeaders,
    };
    const token = await Promise.resolve(this.getToken());
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseUrl}${endpoint}`;
    const headers = await this.getHeaders(options.headers || {});
    const method = options.method || 'GET';

    let lastError = null;
    for (let attempt = 0; attempt <= this.maxRetries; attempt++) {
      try {
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeoutMs);

        const fetchOptions = {
          ...options,
          method,
          headers,
          signal: controller.signal,
        };

        const response = await fetch(url, fetchOptions);
        clearTimeout(timeoutId);

        if (!response.ok) {
          const errJson = await response.json().catch(() => ({}));
          throw AtlasError.fromResponse(response.status, errJson);
        }

        if (response.status === 204) {
          return null;
        }

        return await response.json();
      } catch (err) {
        lastError = err;
        if (err instanceof AtlasError && (err.status === 400 || err.status === 401 || err.status === 403 || err.status === 404)) {
          // Do not retry client validation or auth errors
          throw err;
        }

        if (attempt < this.maxRetries) {
          const backoff = Math.pow(2, attempt) * 300;
          await new Promise((res) => setTimeout(res, backoff));
        }
      }
    }
    throw lastError || new AtlasError('Request failed after retries', 500);
  }

  async chat(chatRequest) {
    return this.request('/api/v1/atlas/chat', {
      method: 'POST',
      body: JSON.stringify(chatRequest),
    });
  }

  async getCapabilities() {
    return this.request('/api/v1/atlas/capabilities', {
      method: 'GET',
    });
  }

  async getOperationalInfo() {
    return this.request('/api/v1/atlas/info', {
      method: 'GET',
    });
  }
}

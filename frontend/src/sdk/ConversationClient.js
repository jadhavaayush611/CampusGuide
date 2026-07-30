import { AtlasError, buildQueryParams, paginate, filterItems, sortArray } from './utils';

export class ConversationClient {
  constructor(atlasClient) {
    this.client = atlasClient;
  }

  async create(payload) {
    return this.client.request('/api/v1/atlas/conversations', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async list(options = {}) {
    const { page, limit, status, sortBy = 'updatedAt', sortOrder = 'desc' } = options;
    const query = buildQueryParams({ page, limit, status });
    const conversations = await this.client.request(`/api/v1/atlas/conversations${query}`, {
      method: 'GET',
    });

    let result = Array.isArray(conversations) ? conversations : [];
    if (status) {
      result = filterItems(result, (c) => c.status === status);
    }
    result = sortArray(result, sortBy, sortOrder);

    if (page || limit) {
      return paginate(result, page || 1, limit || 10);
    }
    return result;
  }

  async get(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}`, {
      method: 'GET',
    });
  }

  async update(id, payload) {
    return this.client.request(`/api/v1/atlas/conversations/${id}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  }

  async rename(id, title) {
    const query = buildQueryParams({ title });
    return this.client.request(`/api/v1/atlas/conversations/${id}/rename${query}`, {
      method: 'POST',
    });
  }

  async archive(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}/archive`, {
      method: 'POST',
    });
  }

  async restore(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}/restore`, {
      method: 'POST',
    });
  }

  async delete(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}`, {
      method: 'DELETE',
    });
  }

  async getHistory(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}/history`, {
      method: 'GET',
    });
  }

  async getSummary(id) {
    return this.client.request(`/api/v1/atlas/conversations/${id}/summary`, {
      method: 'GET',
    });
  }

  async continue(id, chatRequest) {
    return this.client.request(`/api/v1/atlas/conversations/${id}/continue`, {
      method: 'POST',
      body: JSON.stringify(chatRequest),
    });
  }
}

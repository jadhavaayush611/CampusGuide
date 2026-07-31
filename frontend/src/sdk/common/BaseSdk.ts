import { ApiClient, apiClient as defaultApiClient } from '../../core/api/ApiClient';
import { ApiRequestConfig } from '../../core/api/ApiConfig';
import { SdkError } from './SdkError';

/**
 * Base Abstract SDK encapsulating request execution, response parsing, and error mapping.
 */
export abstract class BaseSdk {
  protected client: ApiClient;

  constructor(client: ApiClient = defaultApiClient) {
    this.client = client;
  }

  /**
   * Protected wrapper executing HTTP requests with error boundary mapping.
   */
  protected async executeRequest<T>(config: ApiRequestConfig): Promise<T> {
    try {
      const response = await this.client.request<T>(config);
      return response.data;
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }

  protected async get<T>(url: string, params?: Record<string, any>, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'params'>): Promise<T> {
    try {
      return await this.client.get<T>(url, { ...config, params });
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }

  protected async post<T>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    try {
      return await this.client.post<T>(url, body, config);
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }

  protected async put<T>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    try {
      return await this.client.put<T>(url, body, config);
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }

  protected async patch<T>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    try {
      return await this.client.patch<T>(url, body, config);
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }

  protected async delete<T>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>): Promise<T> {
    try {
      return await this.client.delete<T>(url, config);
    } catch (error) {
      throw SdkError.fromApiError(error);
    }
  }
}

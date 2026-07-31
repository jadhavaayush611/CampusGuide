import { HttpMethod } from './ApiConstants';

export interface ApiRequestConfig {
  url: string;
  method?: HttpMethod;
  headers?: Record<string, string>;
  params?: Record<string, string | number | boolean | undefined | null>;
  body?: unknown;
  timeoutMs?: number;
  retryCount?: number;
  skipAuth?: boolean;
  skipCorrelationId?: boolean;
  signal?: AbortSignal;
}

export interface ApiResponse<T = unknown> {
  data: T;
  status: number;
  statusText: string;
  headers: Headers;
  config: ApiRequestConfig;
}

export interface ApiClientConfig {
  baseUrl: string;
  timeoutMs: number;
  defaultHeaders: Record<string, string>;
  retryCount: number;
}

import { config } from '../config';
import { ApiClientConfig, ApiRequestConfig, ApiResponse } from './ApiConfig';
import { HTTP_HEADER, HTTP_METHOD, DEFAULT_API_TIMEOUT_MS, DEFAULT_MAX_RETRIES, HTTP_STATUS, RETRY_BACKOFF_FACTOR_MS } from './ApiConstants';
import { ApiError, NetworkError, TimeoutError } from '../errors/AppError';
import { generateCorrelationId } from '../utils/uuid';
import { logger } from '../utils/logger';

export type RequestInterceptor = (requestConfig: ApiRequestConfig) => Promise<ApiRequestConfig> | ApiRequestConfig;
export type ResponseInterceptor = (response: ApiResponse<any>) => Promise<ApiResponse<any>> | ApiResponse<any>;
export type ErrorInterceptor = (error: ApiError) => Promise<any>;

export class ApiClient {
  private clientConfig: ApiClientConfig;
  private requestInterceptors: RequestInterceptor[] = [];
  private responseInterceptors: ResponseInterceptor[] = [];
  private errorInterceptors: ErrorInterceptor[] = [];

  constructor(customConfig?: Partial<ApiClientConfig>) {
    this.clientConfig = {
      baseUrl: customConfig?.baseUrl ?? config.apiBaseUrl,
      timeoutMs: customConfig?.timeoutMs ?? DEFAULT_API_TIMEOUT_MS,
      defaultHeaders: {
        [HTTP_HEADER.CONTENT_TYPE]: 'application/json',
        [HTTP_HEADER.ACCEPT]: 'application/json',
        ...(customConfig?.defaultHeaders ?? {}),
      },
      retryCount: customConfig?.retryCount ?? DEFAULT_MAX_RETRIES,
    };

    // Register standard default interceptors
    this.setupDefaultInterceptors();
  }

  /**
   * Register request interceptor
   */
  public addRequestInterceptor(interceptor: RequestInterceptor): () => void {
    this.requestInterceptors.push(interceptor);
    return () => {
      this.requestInterceptors = this.requestInterceptors.filter((i) => i !== interceptor);
    };
  }

  /**
   * Register response interceptor
   */
  public addResponseInterceptor(interceptor: ResponseInterceptor): () => void {
    this.responseInterceptors.push(interceptor);
    return () => {
      this.responseInterceptors = this.responseInterceptors.filter((i) => i !== interceptor);
    };
  }

  /**
   * Register error interceptor
   */
  public addErrorInterceptor(interceptor: ErrorInterceptor): () => void {
    this.errorInterceptors.push(interceptor);
    return () => {
      this.errorInterceptors = this.errorInterceptors.filter((i) => i !== interceptor);
    };
  }

  private setupDefaultInterceptors(): void {
    // Request Interceptor: Attach Correlation ID & default headers
    this.addRequestInterceptor((reqConfig) => {
      const headers = { ...this.clientConfig.defaultHeaders, ...reqConfig.headers };

      if (!reqConfig.skipCorrelationId && !headers[HTTP_HEADER.X_CORRELATION_ID]) {
        headers[HTTP_HEADER.X_CORRELATION_ID] = generateCorrelationId();
      }

      return {
        ...reqConfig,
        headers,
      };
    });
  }

  private buildUrl(url: string, params?: ApiRequestConfig['params']): string {
    const isAbsolute = /^https?:\/\//i.test(url);
    if (isAbsolute) {
      let fullUrl = url;
      if (params) {
        const searchParams = new URLSearchParams();
        Object.entries(params).forEach(([key, val]) => {
          if (val !== undefined && val !== null) {
            searchParams.append(key, String(val));
          }
        });
        const queryString = searchParams.toString();
        if (queryString) {
          fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString;
        }
      }
      return fullUrl;
    }

    const baseUrl = this.clientConfig.baseUrl.replace(/\/+$/, '');
    let cleanPath = url.startsWith('/') ? url : `/${url}`;

    // Normalize duplicate /api/v1 or /api prefixes if baseUrl already ends with /api/v1
    if (baseUrl.endsWith('/api/v1')) {
      if (cleanPath.startsWith('/api/v1/')) {
        cleanPath = cleanPath.replace(/^\/api\/v1/, '');
      } else if (cleanPath.startsWith('/api/')) {
        cleanPath = cleanPath.replace(/^\/api/, '');
      }
    } else if (baseUrl.endsWith('/api')) {
      if (cleanPath.startsWith('/api/')) {
        cleanPath = cleanPath.replace(/^\/api/, '');
      }
    }

    let fullUrl = `${baseUrl}${cleanPath.startsWith('/') ? '' : '/'}${cleanPath}`;

    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, val]) => {
        if (val !== undefined && val !== null) {
          searchParams.append(key, String(val));
        }
      });
      const queryString = searchParams.toString();
      if (queryString) {
        fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString;
      }
    }

    return fullUrl;
  }

  private async executeFetch<T>(
    reqConfig: ApiRequestConfig,
    attempt = 0
  ): Promise<ApiResponse<T>> {
    const method = reqConfig.method ?? HTTP_METHOD.GET;
    
    // Offline detection: block destructive mutations immediately
    if (typeof navigator !== 'undefined' && !navigator.onLine && method !== HTTP_METHOD.GET) {
      throw new NetworkError('Destructive actions are disabled while offline. Please reconnect and try again.');
    }

    const fullUrl = this.buildUrl(reqConfig.url, reqConfig.params);
    const timeoutMs = reqConfig.timeoutMs ?? this.clientConfig.timeoutMs;
    const maxRetries = reqConfig.retryCount ?? this.clientConfig.retryCount;

    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

    // Merge signals if an external AbortSignal was provided
    if (reqConfig.signal) {
      reqConfig.signal.addEventListener('abort', () => controller.abort());
    }

    const correlationId = reqConfig.headers?.[HTTP_HEADER.X_CORRELATION_ID];

    try {
      logger.debug(`[ApiClient] ${method} -> ${fullUrl}`, { correlationId });

      const requestInit: RequestInit = {
        method,
        headers: reqConfig.headers,
        signal: controller.signal,
      };

      if (reqConfig.body !== undefined && reqConfig.body !== null) {
        if (typeof reqConfig.body === 'string') {
          requestInit.body = reqConfig.body;
        } else if (reqConfig.body instanceof FormData || reqConfig.body instanceof Blob) {
          requestInit.body = reqConfig.body;
        } else {
          requestInit.body = JSON.stringify(reqConfig.body);
        }
      }

      const response = await fetch(fullUrl, requestInit);
      clearTimeout(timeoutId);

      let responseData: any = null;
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const text = await response.text();
        responseData = text ? JSON.parse(text) : null;
      } else {
        responseData = await response.text();
      }

      if (!response.ok) {
        const errorMessage =
          (typeof responseData === 'object' && responseData?.message) ||
          `HTTP ${response.status}: ${response.statusText}`;

        const apiError = new ApiError(
          errorMessage,
          response.status,
          responseData,
          correlationId,
          `HTTP_${response.status}`
        );
        (apiError as any).config = reqConfig; // Attach request config for interceptor recovery
        throw apiError;
      }

      return {
        data: responseData as T,
        status: response.status,
        statusText: response.statusText,
        headers: response.headers,
        config: reqConfig,
      };
    } catch (error: any) {
      clearTimeout(timeoutId);

      let apiError: ApiError;

      if (error instanceof ApiError) {
        apiError = error;
      } else if (error.name === 'AbortError') {
        apiError = new TimeoutError(`Request timeout after ${timeoutMs}ms`, { url: fullUrl });
      } else if (error instanceof TypeError && error.message.includes('fetch')) {
        apiError = new NetworkError('Failed to connect to backend server', { url: fullUrl, originalError: error.message });
      } else {
        apiError = new ApiError(error.message || 'Network error', 0, null, correlationId, 'NETWORK_ERROR');
      }

      // Ensure config is attached
      if (!(apiError as any).config) {
        (apiError as any).config = reqConfig;
      }

      // Check if transient error suitable for automatic retry
      const isTransient =
        apiError.statusCode >= 502 ||
        apiError instanceof NetworkError ||
        apiError instanceof TimeoutError;

      if (isTransient && attempt < maxRetries && method === HTTP_METHOD.GET) {
        const backoffMs = Math.pow(2, attempt) * RETRY_BACKOFF_FACTOR_MS;
        logger.warn(`[ApiClient] Transient error (${apiError.message}). Retrying attempt ${attempt + 1}/${maxRetries} in ${backoffMs}ms`);
        await new Promise((res) => setTimeout(res, backoffMs));
        return this.executeFetch<T>(reqConfig, attempt + 1);
      }

      // Run error interceptors
      for (const errorInterceptor of this.errorInterceptors) {
        try {
          const recoveryResult = await errorInterceptor(apiError);
          if (recoveryResult !== undefined) {
            return recoveryResult; // Recovered from error (e.g. silent retry on token refresh)
          }
        } catch (interceptErr) {
          logger.error('[ApiClient] Error in error interceptor:', interceptErr);
          throw interceptErr;
        }
      }

      throw apiError;
    }
  }

  /**
   * Main request method with interceptor pipeline
   */
  public async request<T = unknown>(reqConfig: ApiRequestConfig): Promise<ApiResponse<T>> {
    let currentConfig = { ...reqConfig };

    // Execute Request Interceptors
    for (const interceptor of this.requestInterceptors) {
      currentConfig = await interceptor(currentConfig);
    }

    // Execute Request
    let response = await this.executeFetch<T>(currentConfig);

    // Execute Response Interceptors
    for (const interceptor of this.responseInterceptors) {
      response = await interceptor(response);
    }

    return response;
  }

  public async get<T = unknown>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>): Promise<T> {
    const res = await this.request<T>({ ...config, url, method: HTTP_METHOD.GET });
    return res.data;
  }

  public async post<T = unknown>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    const res = await this.request<T>({ ...config, url, method: HTTP_METHOD.POST, body });
    return res.data;
  }

  public async put<T = unknown>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    const res = await this.request<T>({ ...config, url, method: HTTP_METHOD.PUT, body });
    return res.data;
  }

  public async patch<T = unknown>(url: string, body?: unknown, config?: Omit<ApiRequestConfig, 'url' | 'method' | 'body'>): Promise<T> {
    const res = await this.request<T>({ ...config, url, method: HTTP_METHOD.PATCH, body });
    return res.data;
  }

  public async delete<T = unknown>(url: string, config?: Omit<ApiRequestConfig, 'url' | 'method'>): Promise<T> {
    const res = await this.request<T>({ ...config, url, method: HTTP_METHOD.DELETE });
    return res.data;
  }
}

/** Global Centralized ApiClient Singleton Instance */
export const apiClient = new ApiClient();

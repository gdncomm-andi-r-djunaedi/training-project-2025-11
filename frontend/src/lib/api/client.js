import { API_BASE_URL, REQUEST_TIMEOUT, DEFAULT_HEADERS } from './config.js';
import { authStore } from '../stores/auth.js';
import { get } from 'svelte/store';

/**
 * Lightweight API client for REST communication
 */
class ApiClient {
  constructor() {
    this.baseUrl = API_BASE_URL;
    this.timeout = REQUEST_TIMEOUT;
  }

  /**
   * Get authorization headers if user is logged in
   */
  getAuthHeaders() {
    const auth = get(authStore);
    if (auth.token) {
      return { 'Authorization': `Bearer ${auth.token}` };
    }
    return {};
  }

  /**
   * Build query string from params object
   */
  buildQueryString(params) {
    const filtered = Object.entries(params || {})
      .filter(([_, v]) => v !== undefined && v !== null && v !== '');
    
    if (filtered.length === 0) return '';
    
    return '?' + filtered
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
      .join('&');
  }

  /**
   * Make HTTP request with timeout
   */
  async request(method, endpoint, options = {}) {
    const { body, params, requiresAuth = false, headers = {} } = options;
    
    const url = `${this.baseUrl}${endpoint}${this.buildQueryString(params)}`;
    
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), this.timeout);

    try {
      const requestHeaders = {
        ...DEFAULT_HEADERS,
        ...headers,
        ...(requiresAuth ? this.getAuthHeaders() : {})
      };

      const response = await fetch(url, {
        method,
        headers: requestHeaders,
        body: body ? JSON.stringify(body) : undefined,
        signal: controller.signal
      });

      clearTimeout(timeoutId);

      // Parse response
      const contentType = response.headers.get('content-type');
      let data = null;
      
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      }

      if (!response.ok) {
        throw new ApiError(
          data?.message || data?.details || `Request failed with status ${response.status}`,
          response.status,
          data
        );
      }

      return data;
    } catch (error) {
      clearTimeout(timeoutId);
      
      if (error.name === 'AbortError') {
        throw new ApiError('Request timeout', 504);
      }
      
      if (error instanceof ApiError) {
        throw error;
      }
      
      throw new ApiError(error.message || 'Network error', 0);
    }
  }

  // HTTP method shortcuts
  get(endpoint, options) {
    return this.request('GET', endpoint, options);
  }

  post(endpoint, body, options = {}) {
    return this.request('POST', endpoint, { ...options, body });
  }

  put(endpoint, body, options = {}) {
    return this.request('PUT', endpoint, { ...options, body });
  }

  delete(endpoint, options) {
    return this.request('DELETE', endpoint, options);
  }
}

/**
 * Custom API Error class
 */
export class ApiError extends Error {
  constructor(message, status, data = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

// Export singleton instance
export const api = new ApiClient();


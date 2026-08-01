import { AtlasClient } from './AtlasClient';

/**
 * Singleton instance of production AtlasClient configured with standard JWT authentication supplier.
 */
export const atlasClient = new AtlasClient({
  baseUrl: (import.meta as any).env?.VITE_API_BASE_URL || '',
  getToken: () => {
    try {
      return localStorage.getItem('token') || sessionStorage.getItem('token') || null;
    } catch {
      return null;
    }
  },
});

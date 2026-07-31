import { IStorage } from './StorageInterface';
import { MemoryStorageAdapter } from './MemoryStorageAdapter';

/**
 * SessionStorage Adapter with fallback to MemoryStorage if Web Storage is unavailable.
 */
export class SessionStorageAdapter implements IStorage {
  private fallback = new MemoryStorageAdapter();

  private isAvailable(): boolean {
    try {
      if (typeof window === 'undefined' || !window.sessionStorage) return false;
      const testKey = '__cg_test__';
      window.sessionStorage.setItem(testKey, '1');
      window.sessionStorage.removeItem(testKey);
      return true;
    } catch {
      return false;
    }
  }

  getItem(key: string): string | null {
    if (!this.isAvailable()) return this.fallback.getItem(key);
    try {
      return window.sessionStorage.getItem(key);
    } catch {
      return this.fallback.getItem(key);
    }
  }

  setItem(key: string, value: string): void {
    if (!this.isAvailable()) {
      this.fallback.setItem(key, value);
      return;
    }
    try {
      window.sessionStorage.setItem(key, value);
    } catch {
      this.fallback.setItem(key, value);
    }
  }

  removeItem(key: string): void {
    if (!this.isAvailable()) {
      this.fallback.removeItem(key);
      return;
    }
    try {
      window.sessionStorage.removeItem(key);
    } catch {
      this.fallback.removeItem(key);
    }
  }

  clear(): void {
    if (!this.isAvailable()) {
      this.fallback.clear();
      return;
    }
    try {
      window.sessionStorage.clear();
    } catch {
      this.fallback.clear();
    }
  }
}

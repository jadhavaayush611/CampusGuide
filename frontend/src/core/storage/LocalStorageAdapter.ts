import { IStorage } from './StorageInterface';
import { MemoryStorageAdapter } from './MemoryStorageAdapter';

/**
 * LocalStorage Adapter with fallback to MemoryStorage if Web Storage is blocked or restricted.
 */
export class LocalStorageAdapter implements IStorage {
  private fallback = new MemoryStorageAdapter();

  private isAvailable(): boolean {
    try {
      if (typeof window === 'undefined' || !window.localStorage) return false;
      const testKey = '__cg_test__';
      window.localStorage.setItem(testKey, '1');
      window.localStorage.removeItem(testKey);
      return true;
    } catch {
      return false;
    }
  }

  getItem(key: string): string | null {
    if (!this.isAvailable()) return this.fallback.getItem(key);
    try {
      return window.localStorage.getItem(key);
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
      window.localStorage.setItem(key, value);
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
      window.localStorage.removeItem(key);
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
      window.localStorage.clear();
    } catch {
      this.fallback.clear();
    }
  }
}

import { IStorage } from './StorageInterface';
import { LocalStorageAdapter } from './LocalStorageAdapter';
import { SessionStorageAdapter } from './SessionStorageAdapter';
import { MemoryStorageAdapter } from './MemoryStorageAdapter';

export interface StorageOptions {
  prefix?: string;
  driver?: 'local' | 'session' | 'memory' | IStorage;
}

/**
 * Higher-level Storage Manager providing key prefixing and JSON serialization.
 */
export class StorageManager {
  private storage: IStorage;
  private prefix: string;

  constructor(options: StorageOptions = {}) {
    const prefix = options.prefix ?? 'campusguide_';
    this.prefix = prefix.endsWith('_') ? prefix : `${prefix}_`;

    if (typeof options.driver === 'object' && options.driver !== null) {
      this.storage = options.driver;
    } else {
      switch (options.driver) {
        case 'session':
          this.storage = new SessionStorageAdapter();
          break;
        case 'memory':
          this.storage = new MemoryStorageAdapter();
          break;
        case 'local':
        default:
          this.storage = new LocalStorageAdapter();
          break;
      }
    }
  }

  private getKey(key: string): string {
    return `${this.prefix}${key}`;
  }

  get<T>(key: string, defaultValue: T | null = null): T | null {
    const raw = this.storage.getItem(this.getKey(key));
    if (raw === null) return defaultValue;
    try {
      return JSON.parse(raw) as T;
    } catch {
      return (raw as unknown) as T;
    }
  }

  set<T>(key: string, value: T): void {
    const serialized = typeof value === 'string' ? value : JSON.stringify(value);
    this.storage.setItem(this.getKey(key), serialized);
  }

  remove(key: string): void {
    this.storage.removeItem(this.getKey(key));
  }

  clear(): void {
    this.storage.clear();
  }
}

/** Default singleton storage instance */
export const appStorage = new StorageManager({ driver: 'local', prefix: 'campusguide_' });

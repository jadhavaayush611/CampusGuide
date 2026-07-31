/**
 * Storage Abstraction Interface
 * 
 * Provides dependency inversion for local state persistence.
 */
export interface IStorage {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
  removeItem(key: string): void;
  clear(): void;
}

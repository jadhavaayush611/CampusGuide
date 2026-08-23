/**
 * Environment Configuration Layer
 * 
 * CRITICAL ARCHITECTURAL DIRECTIVE:
 * Only this file may access `import.meta.env`.
 * All other modules MUST import runtime configuration from `@/core/config`.
 */

export interface AppConfig {
  /** Base URL for backend REST API endpoints */
  readonly apiBaseUrl: string;

  /** Human-readable application name */
  readonly appName: string;

  /** Semantic version string */
  readonly appVersion: string;

  /** Flag to enable verbose debug logging and diagnostics */
  readonly enableDebug: boolean;

  /** Flag to enable telemetry and analytics collection */
  readonly enableAnalytics: boolean;

  /** Flag indicating production build environment */
  readonly isProduction: boolean;

  /** Flag indicating development build environment */
  readonly isDevelopment: boolean;

  /** Flag indicating whether Atlas AI is available for MVP release */
  readonly isAtlasMvpAvailable: boolean;
}

const parseBoolean = (value: string | undefined, defaultValue: boolean): boolean => {
  if (value === undefined || value === "") return defaultValue;
  return value.toLowerCase() === "true" || value === "1";
};

const resolveEnvValue = (viteKey: string, fallbackKey: string, defaultValue: string): string => {
  const env = import.meta.env as Record<string, string | undefined>;
  return env[viteKey] || env[fallbackKey] || defaultValue;
};

/**
 * Frozen Application Configuration Instance.
 * Immutable at runtime.
 */
export const config: Readonly<AppConfig> = Object.freeze({
  apiBaseUrl: resolveEnvValue("VITE_API_BASE_URL", "API_BASE_URL", "http://localhost:8080/api/v1"),
  appName: resolveEnvValue("VITE_APP_NAME", "APP_NAME", "CampusGuide"),
  appVersion: resolveEnvValue("VITE_APP_VERSION", "APP_VERSION", "1.0.0"),
  enableDebug: parseBoolean(
    (import.meta.env as Record<string, string | undefined>)["VITE_ENABLE_DEBUG"] ||
      (import.meta.env as Record<string, string | undefined>)["ENABLE_DEBUG"],
    import.meta.env.DEV ?? false
  ),
  enableAnalytics: parseBoolean(
    (import.meta.env as Record<string, string | undefined>)["VITE_ENABLE_ANALYTICS"] ||
      (import.meta.env as Record<string, string | undefined>)["ENABLE_ANALYTICS"],
    false
  ),
  isProduction: import.meta.env.PROD ?? false,
  isDevelopment: import.meta.env.DEV ?? true,
  isAtlasMvpAvailable: parseBoolean(
    (import.meta.env as Record<string, string | undefined>)["VITE_ATLAS_MVP_AVAILABLE"] ||
      (import.meta.env as Record<string, string | undefined>)["ATLAS_MVP_AVAILABLE"],
    false
  ),
});

/**
 * Accessor function for retrieving typed app configuration.
 */
export const getConfig = (): Readonly<AppConfig> => config;

/**
 * Lightweight JWT utilities without external dependencies.
 */

export interface JwtPayload {
  sub?: string;
  exp?: number;
  iat?: number;
  roles?: string[];
  [key: string]: unknown;
}

/**
 * Safely parse a JWT token payload.
 */
export function parseJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payloadBase64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(payloadBase64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

/**
 * Check if a JWT token is expired, considering an optional buffer in seconds.
 */
export function isTokenExpired(token: string, bufferSeconds = 10): boolean {
  const payload = parseJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') return true;
  const currentTime = Math.floor(Date.now() / 1000);
  return payload.exp - bufferSeconds <= currentTime;
}

/**
 * Get token expiration timestamp in milliseconds.
 */
export function getTokenExpirationTime(token: string): number | null {
  const payload = parseJwtPayload(token);
  if (!payload || typeof payload.exp !== 'number') return null;
  return payload.exp * 1000;
}

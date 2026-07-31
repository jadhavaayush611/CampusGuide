import { config } from '../config';

type LogLevel = 'debug' | 'info' | 'warn' | 'error';

class Logger {
  private formatPrefix(level: LogLevel): string {
    const timestamp = new Date().toISOString();
    return `[${timestamp}] [${config.appName}] [${level.toUpperCase()}]:`;
  }

  debug(...args: unknown[]): void {
    if (config.enableDebug) {
      console.debug(this.formatPrefix('debug'), ...args);
    }
  }

  info(...args: unknown[]): void {
    console.info(this.formatPrefix('info'), ...args);
  }

  warn(...args: unknown[]): void {
    console.warn(this.formatPrefix('warn'), ...args);
  }

  error(...args: unknown[]): void {
    console.error(this.formatPrefix('error'), ...args);
  }
}

export const logger = new Logger();

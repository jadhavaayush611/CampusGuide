import { toast as sonnerToast, ExternalToast } from 'sonner';

export interface ToastOptions extends ExternalToast {
  description?: string;
  duration?: number;
}

export interface ToastWrapper {
  success: (message: string, options?: ToastOptions) => string | number;
  error: (message: string, options?: ToastOptions) => string | number;
  info: (message: string, options?: ToastOptions) => string | number;
  warning: (message: string, options?: ToastOptions) => string | number;
  dismiss: (toastId?: string | number) => void;
  promise: <T>(
    promise: Promise<T>,
    data: {
      loading: string;
      success: string | ((data: T) => string);
      error: string | ((error: any) => string);
    }
  ) => void;
}

/**
 * Custom hook providing a decoupled wrapper around the underlying Sonner toast library.
 */
export function useToast(): ToastWrapper {
  return {
    success: (message, options) => sonnerToast.success(message, options),
    error: (message, options) => sonnerToast.error(message, options),
    info: (message, options) => sonnerToast.info(message, options),
    warning: (message, options) => sonnerToast.warning(message, options),
    dismiss: (toastId) => sonnerToast.dismiss(toastId),
    promise: (promise, data) => sonnerToast.promise(promise, data),
  };
}

/** Direct export for non-React context usages */
export const toast = {
  success: (message: string, options?: ToastOptions) => sonnerToast.success(message, options),
  error: (message: string, options?: ToastOptions) => sonnerToast.error(message, options),
  info: (message: string, options?: ToastOptions) => sonnerToast.info(message, options),
  warning: (message: string, options?: ToastOptions) => sonnerToast.warning(message, options),
  dismiss: (toastId?: string | number) => sonnerToast.dismiss(toastId),
};

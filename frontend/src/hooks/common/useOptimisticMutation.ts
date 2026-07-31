import { useMutation, useQueryClient, QueryKey, UseMutationOptions, UseMutationResult } from '@tanstack/react-query';
import { toast } from '../../core/toast/useToast';
import { SdkError } from '../../sdk/common/SdkError';

export interface OptimisticMutationOptions<TData, TVariables, TContext = { previousData?: unknown }> {
  /** The mutation function invoking the SDK */
  mutationFn: (variables: TVariables) => Promise<TData>;
  /** Query keys to invalidate upon successful mutation */
  invalidateQueryKeys?: QueryKey[];
  /** Target query key to optimistically update */
  targetQueryKey?: QueryKey;
  /** Function producing updated cache state optimistically */
  updateCacheOptimistically?: (oldData: any, variables: TVariables) => any;
  /** Success message for toast notification */
  successMessage?: string | ((data: TData, variables: TVariables) => string);
  /** Error message prefix for toast notification */
  errorMessage?: string | ((error: SdkError, variables: TVariables) => string);
  /** Additional React Query mutation options */
  options?: Omit<UseMutationOptions<TData, Error, TVariables, TContext>, 'mutationFn'>;
}

/**
 * Reusable mutation helper supporting optimistic updates, automatic rollback on error,
 * query cache invalidation, and standardized toast notifications.
 */
export function useOptimisticMutation<TData, TVariables, TContext = { previousData?: unknown }>({
  mutationFn,
  invalidateQueryKeys = [],
  targetQueryKey,
  updateCacheOptimistically,
  successMessage,
  errorMessage,
  options,
}: OptimisticMutationOptions<TData, TVariables, TContext>): UseMutationResult<TData, Error, TVariables, TContext> {
  const queryClient = useQueryClient();

  return useMutation<TData, Error, TVariables, TContext>({
    mutationFn,
    onMutate: async (variables: TVariables) => {
      let previousData: unknown;

      if (targetQueryKey && updateCacheOptimistically) {
        // Cancel ongoing refetches so they don't overwrite optimistic update
        await queryClient.cancelQueries({ queryKey: targetQueryKey });
        // Snapshot previous cache value
        previousData = queryClient.getQueryData(targetQueryKey);
        // Optimistically update cache
        queryClient.setQueryData(targetQueryKey, (old: any) => updateCacheOptimistically(old, variables));
      }

      // Call user's custom onMutate if provided
      const customContext = options?.onMutate ? await (options.onMutate as any)(variables, {} as any) : undefined;
      return ({ previousData, ...customContext } as unknown) as TContext;
    },
    onError: (error: Error, variables: TVariables, context: TContext | undefined) => {
      // Rollback cache if mutation fails
      if (targetQueryKey && (context as any)?.previousData !== undefined) {
        queryClient.setQueryData(targetQueryKey, (context as any).previousData);
      }

      // Display standardized toast error notification
      const sdkError = error instanceof SdkError ? error : SdkError.fromApiError(error);
      const msg = typeof errorMessage === 'function' ? errorMessage(sdkError, variables) : errorMessage || sdkError.message;
      toast.error(msg);

      if (options?.onError) {
        (options.onError as any)(error, variables, context);
      }
    },
    onSuccess: (data: TData, variables: TVariables, context: TContext | undefined) => {
      // Invalidate associated query keys
      invalidateQueryKeys.forEach((key) => {
        queryClient.invalidateQueries({ queryKey: key });
      });

      // Display standardized toast success notification
      if (successMessage) {
        const msg = typeof successMessage === 'function' ? successMessage(data, variables) : successMessage;
        toast.success(msg);
      }

      if (options?.onSuccess) {
        (options.onSuccess as any)(data, variables, context);
      }
    },
    onSettled: (data, error, variables, context) => {
      if (targetQueryKey) {
        queryClient.invalidateQueries({ queryKey: targetQueryKey });
      }
      if (options?.onSettled) {
        (options.onSettled as any)(data, error, variables, context);
      }
    },
    ...options,
  });
}

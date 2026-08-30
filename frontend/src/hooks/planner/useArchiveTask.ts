/**
 * @deprecated Task archiving is not supported in MVP Planner specifications.
 */
export function useArchiveTask() {
  return {
    mutate: () => {
      throw new Error('Task archiving is not supported in MVP');
    },
    isPending: false,
  };
}

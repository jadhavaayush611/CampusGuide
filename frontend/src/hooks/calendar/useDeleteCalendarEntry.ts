import { useMutation, useQueryClient } from '@tanstack/react-query';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';
import { queryKeys } from '../../sdk/queryKeys';

export function useDeleteCalendarEntry() {
  const queryClient = useQueryClient();

  return useMutation<void, Error, string>({
    mutationFn: (id) => calendarSdk.deleteEntry(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.calendar.all });
    },
  });
}

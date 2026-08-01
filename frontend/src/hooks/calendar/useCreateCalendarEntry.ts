import { useMutation, useQueryClient } from '@tanstack/react-query';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CreateCalendarEntryPayload, CalendarEntry } from '../../models/calendar.model';

export function useCreateCalendarEntry() {
  const queryClient = useQueryClient();

  return useMutation<CalendarEntry, Error, CreateCalendarEntryPayload>({
    mutationFn: (payload) => calendarSdk.createEntry(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.calendar.all });
    },
  });
}

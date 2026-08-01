import { useMutation, useQueryClient } from '@tanstack/react-query';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { UpdateCalendarEntryPayload, CalendarEntry } from '../../models/calendar.model';

interface UpdateCalendarEntryVariables {
  id: string;
  payload: UpdateCalendarEntryPayload;
}

export function useUpdateCalendarEntry() {
  const queryClient = useQueryClient();

  return useMutation<CalendarEntry, Error, UpdateCalendarEntryVariables>({
    mutationFn: ({ id, payload }) => calendarSdk.updateEntry(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.calendar.all });
    },
  });
}

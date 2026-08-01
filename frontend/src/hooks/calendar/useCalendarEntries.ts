import { useQuery } from '@tanstack/react-query';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CalendarEntry } from '../../models/calendar.model';

export function useCalendarEntries() {
  return useQuery<CalendarEntry[], Error>({
    queryKey: queryKeys.calendar.entries(),
    queryFn: () => calendarSdk.getEntries(),
    staleTime: 2 * 60 * 1000,
  });
}

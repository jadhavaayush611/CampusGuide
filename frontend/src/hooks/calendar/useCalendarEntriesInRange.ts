import { useQuery } from '@tanstack/react-query';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CalendarEntry } from '../../models/calendar.model';

export function useCalendarEntriesInRange(from?: string, to?: string) {
  return useQuery<CalendarEntry[], Error>({
    queryKey: queryKeys.calendar.range(from, to),
    queryFn: () => calendarSdk.getEntriesInRange(from!, to!),
    enabled: Boolean(from && to),
    staleTime: 2 * 60 * 1000,
  });
}

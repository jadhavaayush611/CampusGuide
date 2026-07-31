import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { TimetableSlot } from '../../models/planner.model';

export function useTimetable(scheduleId?: string): UseQueryResult<TimetableSlot[], Error> {
  return useQuery<TimetableSlot[], Error>({
    queryKey: queryKeys.planner.timetable(scheduleId),
    queryFn: () => plannerSdk.getTimetable(scheduleId),
  });
}

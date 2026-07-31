import { UseMutationResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Schedule } from '../../models/planner.model';
import { CreateScheduleDto } from '../../sdk/planner/planner.dto';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useCreateSchedule(): UseMutationResult<Schedule, Error, CreateScheduleDto> {
  return useOptimisticMutation<Schedule, CreateScheduleDto>({
    mutationFn: (payload) => plannerSdk.createSchedule(payload),
    invalidateQueryKeys: [queryKeys.planner.schedules()],
    successMessage: 'Schedule created successfully!',
    errorMessage: 'Failed to create schedule.',
  });
}

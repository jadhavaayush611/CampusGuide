import { UseMutationResult } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Schedule } from '../../models/planner.model';
import { UpdateScheduleDto } from '../../sdk/planner/planner.dto';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export interface UpdateScheduleVariables {
  id: string;
  payload: UpdateScheduleDto;
}

export function useUpdateSchedule(): UseMutationResult<Schedule, Error, UpdateScheduleVariables> {
  return useOptimisticMutation<Schedule, UpdateScheduleVariables>({
    mutationFn: ({ id, payload }) => plannerSdk.updateSchedule(id, payload),
    invalidateQueryKeys: [queryKeys.planner.schedules()],
    successMessage: 'Schedule updated successfully',
    errorMessage: 'Failed to update schedule',
  });
}

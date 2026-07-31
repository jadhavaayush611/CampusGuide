import { UseMutationResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';
import { CreateEventDto } from '../../sdk/campus/campus.dto';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export function useCreateEvent(): UseMutationResult<CampusEvent, Error, CreateEventDto> {
  return useOptimisticMutation<CampusEvent, CreateEventDto>({
    mutationFn: (payload) => campusSdk.createEvent(payload),
    invalidateQueryKeys: [queryKeys.campus.events()],
    successMessage: 'Campus event created successfully!',
    errorMessage: 'Failed to create campus event',
  });
}

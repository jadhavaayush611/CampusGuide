import { UseMutationResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { CampusEvent } from '../../models/campus.model';
import { useOptimisticMutation } from '../common/useOptimisticMutation';

export interface RegisterEventVariables {
  eventId: string;
  register: boolean; // true to register, false to cancel
}

export function useRegisterForEvent(): UseMutationResult<CampusEvent, Error, RegisterEventVariables> {
  return useOptimisticMutation<CampusEvent, RegisterEventVariables>({
    mutationFn: ({ eventId, register }) =>
      register ? campusSdk.registerForEvent(eventId) : campusSdk.cancelEventRegistration(eventId),
    invalidateQueryKeys: [queryKeys.campus.events()],
    targetQueryKey: queryKeys.campus.events(),
    updateCacheOptimistically: (oldEvents: CampusEvent[], { eventId, register }) => {
      if (!Array.isArray(oldEvents)) return oldEvents;
      return oldEvents.map((evt) => {
        if (evt.id === eventId) {
          return {
            ...evt,
            isRegistered: register,
            attendeeCount: register ? evt.attendeeCount + 1 : Math.max(0, evt.attendeeCount - 1),
          };
        }
        return evt;
      });
    },
    successMessage: (_, { register }) =>
      register ? 'Registered for event successfully!' : 'Registration cancelled.',
    errorMessage: (_, { register }) =>
      register ? 'Failed to register for event.' : 'Failed to cancel registration.',
  });
}

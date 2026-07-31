import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Council } from '../../models/campus.model';

export function useCouncils(): UseQueryResult<Council[], Error> {
  return useQuery<Council[], Error>({
    queryKey: queryKeys.campus.councils(),
    queryFn: () => campusSdk.getCouncils(),
    staleTime: 30 * 60 * 1000, // 30 minutes
  });
}

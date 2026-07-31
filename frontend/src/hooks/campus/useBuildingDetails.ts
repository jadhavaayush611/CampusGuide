import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { campusSdk } from '../../sdk/campus/CampusSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { Building, FloorPlan, Location } from '../../models/campus.model';

export interface BuildingDetailsData {
  building: Building;
  floorPlans: FloorPlan[];
  locations: Location[];
}

export function useBuildingDetails(buildingId: string): UseQueryResult<BuildingDetailsData, Error> {
  return useQuery<BuildingDetailsData, Error>({
    queryKey: queryKeys.campus.building(buildingId),
    queryFn: async () => {
      const [building, floorPlans, locations] = await Promise.all([
        campusSdk.getBuildingById(buildingId),
        campusSdk.getFloorPlans(buildingId),
        campusSdk.getLocations(buildingId),
      ]);
      return { building, floorPlans, locations };
    },
    enabled: Boolean(buildingId),
  });
}

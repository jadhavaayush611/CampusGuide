/**
 * Atlas GIS & Maps Backend DTO Schemas
 */

export interface SpatialSearchResultDto {
  id: string;
  title: string;
  subtitle?: string | null;
  category: 'BUILDING' | 'ROOM' | 'FACILITY' | 'EVENT' | 'LANDMARK';
  latitude: number;
  longitude: number;
  buildingId?: string | null;
  floor?: number | null;
  distanceMeters?: number | null;
}

export interface RouteRequestDto {
  originLatitude: number;
  originLongitude: number;
  destinationLatitude: number;
  destinationLongitude: number;
  isAccessible?: boolean;
  travelMode?: 'WALKING' | 'WHEELCHAIR' | 'SHUTTLE';
}

export interface WayfindingInstructionDto {
  stepNumber: number;
  instruction: string;
  distanceMeters: number;
  durationSeconds: number;
  startLatitude: number;
  startLongitude: number;
  endLatitude: number;
  endLongitude: number;
  floorChange?: {
    fromFloor: number;
    toFloor: number;
    type: 'ELEVATOR' | 'STAIRS';
  } | null;
}

export interface RouteResponseDto {
  id: string;
  origin: {
    name: string;
    latitude: number;
    longitude: number;
  };
  destination: {
    name: string;
    latitude: number;
    longitude: number;
  };
  totalDistanceMeters: number;
  totalDurationSeconds: number;
  pathCoordinates: Array<[number, number]>;
  steps: WayfindingInstructionDto[];
  isAccessibleRoute: boolean;
}

export interface LandmarkDto {
  id: string;
  name: string;
  category: string;
  description: string;
  latitude: number;
  longitude: number;
  imageUrl?: string | null;
  isPopular?: boolean | null;
}

export interface MapLayerDto {
  id: string;
  name: string;
  type: 'TILE' | 'GEOJSON' | 'OVERLAY';
  url: string;
  isVisible: boolean;
  opacity: number;
  zIndex: number;
}

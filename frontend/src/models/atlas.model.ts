/**
 * Frontend UI Domain Models for Atlas Maps & Wayfinding
 */

export interface SpatialSearchResult {
  id: string;
  title: string;
  subtitle?: string;
  category: 'BUILDING' | 'ROOM' | 'FACILITY' | 'EVENT' | 'LANDMARK';
  latitude: number;
  longitude: number;
  buildingId?: string;
  floor?: number;
  distanceMeters?: number;
}

export interface WayfindingStep {
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
  };
}

export interface CalculatedRoute {
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
  pathCoordinates: Array<[number, number]>; // [lat, lng]
  steps: WayfindingStep[];
  isAccessibleRoute: boolean;
}

export interface Landmark {
  id: string;
  name: string;
  category: string;
  description: string;
  latitude: number;
  longitude: number;
  imageUrl?: string;
  isPopular?: boolean;
}

export interface MapLayer {
  id: string;
  name: string;
  type: 'TILE' | 'GEOJSON' | 'OVERLAY';
  url: string;
  isVisible: boolean;
  opacity: number;
  zIndex: number;
}

import {
  SpatialSearchResultDto,
  WayfindingInstructionDto,
  RouteResponseDto,
  LandmarkDto,
  MapLayerDto,
} from './atlas.dto';
import {
  SpatialSearchResult,
  WayfindingStep,
  CalculatedRoute,
  Landmark,
  MapLayer,
} from '../../models/atlas.model';

export function mapSearchResultDtoToModel(dto: SpatialSearchResultDto): SpatialSearchResult {
  return {
    id: dto.id,
    title: dto.title,
    subtitle: dto.subtitle ?? undefined,
    category: dto.category,
    latitude: dto.latitude,
    longitude: dto.longitude,
    buildingId: dto.buildingId ?? undefined,
    floor: dto.floor ?? undefined,
    distanceMeters: dto.distanceMeters ?? undefined,
  };
}

export function mapWayfindingStepDtoToModel(dto: WayfindingInstructionDto): WayfindingStep {
  return {
    stepNumber: dto.stepNumber,
    instruction: dto.instruction,
    distanceMeters: dto.distanceMeters,
    durationSeconds: dto.durationSeconds,
    startLatitude: dto.startLatitude,
    startLongitude: dto.startLongitude,
    endLatitude: dto.endLatitude,
    endLongitude: dto.endLongitude,
    floorChange: dto.floorChange ? {
      fromFloor: dto.floorChange.fromFloor,
      toFloor: dto.floorChange.toFloor,
      type: dto.floorChange.type,
    } : undefined,
  };
}

export function mapRouteDtoToModel(dto: RouteResponseDto): CalculatedRoute {
  return {
    id: dto.id,
    origin: dto.origin,
    destination: dto.destination,
    totalDistanceMeters: dto.totalDistanceMeters,
    totalDurationSeconds: dto.totalDurationSeconds,
    pathCoordinates: dto.pathCoordinates,
    steps: (dto.steps || []).map(mapWayfindingStepDtoToModel),
    isAccessibleRoute: dto.isAccessibleRoute,
  };
}

export function mapLandmarkDtoToModel(dto: LandmarkDto): Landmark {
  return {
    id: dto.id,
    name: dto.name,
    category: dto.category,
    description: dto.description,
    latitude: dto.latitude,
    longitude: dto.longitude,
    imageUrl: dto.imageUrl ?? undefined,
    isPopular: dto.isPopular ?? undefined,
  };
}

export function mapMapLayerDtoToModel(dto: MapLayerDto): MapLayer {
  return {
    id: dto.id,
    name: dto.name,
    type: dto.type,
    url: dto.url,
    isVisible: dto.isVisible,
    opacity: dto.opacity,
    zIndex: dto.zIndex,
  };
}

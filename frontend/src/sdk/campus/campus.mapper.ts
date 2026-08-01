import {
  BuildingDto,
  LocationDto,
  EventDto,
  FloorPlanDto,
  CampusCouncilDto,
} from './campus.dto';
import { ResourceDto } from '../resources/resource.dto';
import { mapResourceDtoToModel } from '../resources/resource.mapper';
import {
  Building,
  Location,
  CampusEvent,
  FloorPlan,
  Council,
  CouncilCategory,
  Resource,
} from '../../models/campus.model';

export function mapBuildingDtoToModel(dto: BuildingDto): Building {
  return {
    id: dto.id,
    code: dto.code,
    name: dto.name,
    category: dto.category ?? undefined,
    description: dto.description ?? undefined,
    latitude: dto.latitude,
    longitude: dto.longitude ?? undefined,
    totalFloors: dto.totalFloors ?? undefined,
    imageUrl: dto.imageUrl ?? undefined,
    openingHours: dto.openingHours ?? undefined,
    isAccessible: dto.isAccessible ?? undefined,
  };
}

export function mapLocationDtoToModel(dto: LocationDto): Location {
  return {
    id: dto.id,
    name: dto.name,
    code: dto.code ?? undefined,
    buildingId: dto.buildingId,
    floor: dto.floor,
    roomNumber: dto.roomNumber ?? undefined,
    category: dto.category,
    latitude: dto.latitude ?? undefined,
    longitude: dto.longitude ?? undefined,
    isAccessible: dto.isAccessible ?? undefined,
  };
}

import { mapCouncilDtoToModel } from '../council/council.mapper';

export function mapEventDtoToModel(dto: EventDto, isRegistered = false): CampusEvent {
  return {
    id: dto.id,
    title: dto.title,
    description: dto.description,
    councilId: dto.councilId ?? undefined,
    organizerId: dto.organizerId ?? undefined,
    organizerName: dto.organizerName ?? undefined,
    location: dto.location,
    startTime: dto.startTime,
    endTime: dto.endTime,
    registrationDeadline: dto.registrationDeadline,
    maxParticipants: dto.maxParticipants ?? undefined,
    attendeeCount: dto.attendeeCount,
    imageUrl: dto.imageUrl ?? undefined,
    isCancelled: dto.isCancelled,
    isRegistered,
    createdAt: dto.createdAt ?? undefined,
    updatedAt: dto.updatedAt ?? undefined,
  };
}

export function mapFloorPlanDtoToModel(dto: FloorPlanDto): FloorPlan {
  return {
    id: dto.id,
    buildingId: dto.buildingId,
    floorNumber: dto.floorNumber,
    imageUrl: dto.imageUrl,
    width: dto.width,
    height: dto.height,
  };
}

export function mapCampusCouncilDtoToModel(dto: CampusCouncilDto): Council {
  return mapCouncilDtoToModel({
    id: dto.id,
    name: dto.name,
    category: dto.category,
    description: dto.description,
    logoUrl: dto.logoUrl ?? undefined,
    memberCount: dto.memberCount ?? undefined,
  });
}

export function mapCampusResourceDtoToModel(dto: ResourceDto): Resource {
  return mapResourceDtoToModel(dto);
}


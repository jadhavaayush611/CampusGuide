import {
  BuildingDto,
  LocationDto,
  EventDto,
  FloorPlanDto,
  CouncilDto,
  CommunityDto,
  ResourceDto,
} from './campus.dto';
import {
  Building,
  Location,
  CampusEvent,
  FloorPlan,
  Council,
  Community,
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
    longitude: dto.longitude,
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
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
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

export function mapCouncilDtoToModel(dto: CouncilDto): Council {
  return {
    id: dto.id,
    name: dto.name,
    category: dto.category,
    description: dto.description,
    leadName: dto.leadName ?? undefined,
    contactEmail: dto.contactEmail ?? undefined,
    logoUrl: dto.logoUrl ?? undefined,
    memberCount: dto.memberCount ?? undefined,
  };
}

export { mapCommunityDtoToModel } from '../community/community.mapper';


export function mapResourceDtoToModel(dto: ResourceDto): Resource {
  return {
    id: dto.id,
    title: dto.title,
    description: dto.description ?? undefined,
    uploaderId: dto.uploaderId,
    uploaderName: dto.uploaderName ?? undefined,
    councilId: dto.councilId ?? undefined,
    communityId: dto.communityId ?? undefined,
    tags: dto.tags || [],
    fileName: dto.fileName,
    originalFileName: dto.originalFileName,
    fileType: dto.fileType,
    fileSize: dto.fileSize,
    downloadUrl: dto.downloadUrl,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt ?? undefined,
  };
}

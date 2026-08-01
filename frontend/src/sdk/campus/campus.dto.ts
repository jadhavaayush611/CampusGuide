/**
 * Campus Backend DTO Schemas
 */

export interface BuildingDto {
  id: string;
  code: string;
  name: string;
  category?: string | null;
  description?: string | null;
  latitude: number;
  longitude: number;
  totalFloors?: number | null;
  imageUrl?: string | null;
  openingHours?: string | null;
  isAccessible?: boolean | null;
}

export interface LocationDto {
  id: string;
  name: string;
  code?: string | null;
  buildingId: string;
  floor: number;
  roomNumber?: string | null;
  category: string;
  latitude?: number | null;
  longitude?: number | null;
  isAccessible?: boolean | null;
}

export interface EventDto {
  id: string;
  title: string;
  description: string;
  councilId?: string | null;
  organizerId?: string | null;
  organizerName?: string | null;
  location: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  maxParticipants?: number | null;
  attendeeCount: number;
  imageUrl?: string | null;
  isCancelled: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateEventDto {
  title: string;
  description: string;
  councilId: string;
  location: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  maxParticipants?: number;
  imageUrl?: string;
}

export interface UpdateEventDto {
  title?: string;
  description?: string;
  location?: string;
  startTime?: string;
  endTime?: string;
  registrationDeadline?: string;
  maxParticipants?: number;
  imageUrl?: string;
}

export interface FloorPlanDto {
  id: string;
  buildingId: string;
  floorNumber: number;
  imageUrl: string;
  width: number;
  height: number;
}

export interface CampusCouncilDto {
  id: string;
  name: string;
  category: string;
  description: string;
  leadName?: string | null;
  contactEmail?: string | null;
  logoUrl?: string | null;
  memberCount?: number | null;
}

import type { ResourceDto } from '../resources/resource.dto';
import type { CommunityDto } from '../community/community.dto';

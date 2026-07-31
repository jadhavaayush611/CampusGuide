/**
 * Frontend UI Domain Models for Campus & Facilities
 */

export interface Building {
  id: string;
  code: string;
  name: string;
  category?: string;
  description?: string;
  latitude: number;
  longitude: number;
  totalFloors?: number;
  imageUrl?: string;
  openingHours?: string;
  isAccessible?: boolean;
}

export interface Location {
  id: string;
  name: string;
  code?: string;
  buildingId: string;
  floor: number;
  roomNumber?: string;
  category: string;
  latitude?: number;
  longitude?: number;
  isAccessible?: boolean;
}

export interface CampusEvent {
  id: string;
  title: string;
  description: string;
  councilId?: string;
  organizerId?: string;
  organizerName?: string;
  location: string;
  startTime: string;
  endTime: string;
  registrationDeadline: string;
  maxParticipants?: number;
  attendeeCount: number;
  imageUrl?: string;
  isCancelled: boolean;
  isRegistered?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface FloorPlan {
  id: string;
  buildingId: string;
  floorNumber: number;
  imageUrl: string;
  width: number;
  height: number;
}

export interface Council {
  id: string;
  name: string;
  category: string;
  description: string;
  leadName?: string;
  contactEmail?: string;
  logoUrl?: string;
  memberCount?: number;
}

export interface Community {
  id: string;
  name: string;
  description: string;
  councilId?: string;
  memberCount: number;
  isPrivate?: boolean;
}

export interface Resource {
  id: string;
  title: string;
  description?: string;
  uploaderId: string;
  uploaderName?: string;
  councilId?: string;
  communityId?: string;
  tags: string[];
  fileName: string;
  originalFileName: string;
  fileType: string;
  fileSize: number;
  downloadUrl: string;
  createdAt: string;
  updatedAt?: string;
}

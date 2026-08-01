import { BaseSdk } from '../common/BaseSdk';
import {
  BuildingDto,
  LocationDto,
  EventDto,
  CreateEventDto,
  UpdateEventDto,
  FloorPlanDto,
  CampusCouncilDto,
} from './campus.dto';
import { ResourceDto } from '../resources/resource.dto';
import {
  mapBuildingDtoToModel,
  mapLocationDtoToModel,
  mapEventDtoToModel,
  mapFloorPlanDtoToModel,
  mapCampusCouncilDtoToModel,
  mapCampusResourceDtoToModel,
} from './campus.mapper';
import {
  Building,
  Location,
  CampusEvent,
  FloorPlan,
  Council,
  Resource,
} from '../../models/campus.model';

/**
 * Production Campus SDK encapsulating buildings, locations, events, councils, and resource endpoints.
 */
export class CampusSdk extends BaseSdk {
  private readonly eventsUrl = '/api/events';
  private readonly councilsUrl = '/api/councils';
  private readonly resourcesUrl = '/api/resources';
  private readonly buildingsUrl = '/api/buildings';

  // --- Buildings & Locations ---

  public async getBuildings(): Promise<Building[]> {
    const dtos = await this.get<BuildingDto[]>(this.buildingsUrl);
    return dtos.map(mapBuildingDtoToModel);
  }

  public async getBuildingById(id: string): Promise<Building> {
    const dto = await this.get<BuildingDto>(`${this.buildingsUrl}/${id}`);
    return mapBuildingDtoToModel(dto);
  }

  public async getLocations(buildingId?: string): Promise<Location[]> {
    const params = buildingId ? { buildingId } : undefined;
    const dtos = await this.get<LocationDto[]>('/api/locations', params);
    return dtos.map(mapLocationDtoToModel);
  }

  public async getFloorPlans(buildingId: string): Promise<FloorPlan[]> {
    const dtos = await this.get<FloorPlanDto[]>(`${this.buildingsUrl}/${buildingId}/floor-plans`);
    return dtos.map(mapFloorPlanDtoToModel);
  }

  // --- Events ---

  public async getEvents(): Promise<CampusEvent[]> {
    const dtos = await this.get<EventDto[]>(this.eventsUrl);
    return dtos.map((dto) => mapEventDtoToModel(dto));
  }

  public async getUpcomingEvents(): Promise<CampusEvent[]> {
    const dtos = await this.get<EventDto[]>(`${this.eventsUrl}/upcoming`);
    return dtos.map((dto) => mapEventDtoToModel(dto));
  }

  public async getEventById(eventId: string): Promise<CampusEvent> {
    const dto = await this.get<EventDto>(`${this.eventsUrl}/${eventId}`);
    const status = await this.getEventRegistrationStatus(eventId);
    return mapEventDtoToModel(dto, status);
  }

  public async createEvent(payload: CreateEventDto): Promise<CampusEvent> {
    const dto = await this.post<EventDto>(this.eventsUrl, payload);
    return mapEventDtoToModel(dto);
  }

  public async updateEvent(eventId: string, payload: UpdateEventDto): Promise<CampusEvent> {
    const dto = await this.put<EventDto>(`${this.eventsUrl}/${eventId}`, payload);
    return mapEventDtoToModel(dto);
  }

  public async deleteEvent(eventId: string): Promise<void> {
    await this.delete<void>(`${this.eventsUrl}/${eventId}`);
  }

  public async registerForEvent(eventId: string): Promise<CampusEvent> {
    const dto = await this.post<EventDto>(`${this.eventsUrl}/${eventId}/register`);
    return mapEventDtoToModel(dto, true);
  }

  public async cancelEventRegistration(eventId: string): Promise<CampusEvent> {
    const dto = await this.delete<EventDto>(`${this.eventsUrl}/${eventId}/register`);
    return mapEventDtoToModel(dto, false);
  }

  public async getEventRegistrationStatus(eventId: string): Promise<boolean> {
    try {
      const res = await this.get<{ registered: boolean }>(`${this.eventsUrl}/${eventId}/registration-status`);
      return res.registered;
    } catch {
      return false;
    }
  }

  // --- Councils ---

  public async getCouncils(): Promise<Council[]> {
    const dtos = await this.get<CampusCouncilDto[]>(this.councilsUrl);
    return dtos.map(mapCampusCouncilDtoToModel);
  }

  public async getCouncilById(councilId: string): Promise<Council> {
    const dto = await this.get<CampusCouncilDto>(`${this.councilsUrl}/${councilId}`);
    return mapCampusCouncilDtoToModel(dto);
  }

  // --- Resources ---

  public async getResources(): Promise<Resource[]> {
    const dtos = await this.get<ResourceDto[]>(this.resourcesUrl);
    return dtos.map(mapCampusResourceDtoToModel);
  }

  public async searchResources(query: string): Promise<Resource[]> {
    const dtos = await this.get<ResourceDto[]>(`${this.resourcesUrl}/search`, { query });
    return dtos.map(mapCampusResourceDtoToModel);
  }
}

export const campusSdk = new CampusSdk();

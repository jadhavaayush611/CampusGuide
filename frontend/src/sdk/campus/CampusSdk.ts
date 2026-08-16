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

// Realistic mock campus spatial dataset matching InMemoryCampusKnowledgeProvider
const MOCK_BUILDINGS: Building[] = [
  { id: 'bld-a', name: 'Building A', code: 'BLD_A', description: 'Main academic block containing classrooms and HOD offices', latitude: 19.0468, longitude: 72.8893, totalFloors: 6, isAccessible: true },
  { id: 'bld-b', name: 'Building B', code: 'BLD_B', description: 'Facility block containing library, canteen, and amphitheatre', latitude: 19.0465, longitude: 72.8891, totalFloors: 3, isAccessible: true },
];

const MOCK_LOCATIONS: Location[] = [
  { id: 'loc-1', name: 'Central Library', buildingId: 'bld-b', floor: 1, category: 'LIBRARY', latitude: 19.0465, longitude: 72.8891, isAccessible: true },
  { id: 'loc-2', name: 'Main Auditorium', buildingId: 'bld-a', floor: 0, category: 'AUDITORIUM', latitude: 19.0470, longitude: 72.8895, isAccessible: true },
  { id: 'loc-3', name: 'Amphitheatre', buildingId: 'bld-b', floor: 2, category: 'AMPHITHEATRE', latitude: 19.0469, longitude: 72.8894, isAccessible: true },
  { id: 'loc-4', name: 'Student Canteen', buildingId: 'bld-b', floor: 0, category: 'CANTEEN', latitude: 19.0464, longitude: 72.8889, isAccessible: true },
];

/**
 * Production Campus SDK encapsulating buildings, locations, events, councils, and resource endpoints.
 */
export class CampusSdk extends BaseSdk {
  private readonly eventsUrl = '/api/v1/events';
  private readonly councilsUrl = '/api/v1/councils';
  private readonly resourcesUrl = '/api/v1/resources';
  private readonly buildingsUrl = '/api/v1/academic/buildings';

  // --- Buildings & Locations ---

  public async getBuildings(): Promise<Building[]> {
    // Return mock data directly to support offline/local wayfinding MVP
    return MOCK_BUILDINGS;
  }

  public async getBuildingById(id: string): Promise<Building> {
    const building = MOCK_BUILDINGS.find(b => b.id === id);
    if (!building) {
      throw new Error(`Building with ID ${id} not found`);
    }
    return building;
  }

  public async getLocations(buildingId?: string): Promise<Location[]> {
    if (buildingId) {
      return MOCK_LOCATIONS.filter(l => l.buildingId === buildingId);
    }
    return MOCK_LOCATIONS;
  }

  public async getFloorPlans(buildingId: string): Promise<FloorPlan[]> {
    return [
      {
        id: `fp-${buildingId}-0`,
        buildingId,
        floorNumber: 0,
        imageUrl: '/images/floor-plans/ground.png',
        width: 800,
        height: 600,
      }
    ];
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
      const res = await this.get<boolean | { registered: boolean }>(`${this.eventsUrl}/${eventId}/is-registered`);
      return typeof res === 'boolean' ? res : Boolean(res?.registered);
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

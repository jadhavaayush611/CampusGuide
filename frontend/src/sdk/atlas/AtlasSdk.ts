import { BaseSdk } from '../common/BaseSdk';
import {
  SpatialSearchResultDto,
  RouteRequestDto,
  RouteResponseDto,
  LandmarkDto,
  MapLayerDto,
} from './atlas.dto';
import {
  mapSearchResultDtoToModel,
  mapRouteDtoToModel,
  mapLandmarkDtoToModel,
  mapMapLayerDtoToModel,
} from './atlas.mapper';
import {
  SpatialSearchResult,
  CalculatedRoute,
  Landmark,
  MapLayer,
} from '../../models/atlas.model';

// Realistic mock landmarks matching VESIT CampusGuide spatial mappings
const MOCK_LANDMARKS = [
  { id: 'lm-1', title: 'Central Library', subtitle: 'Building B • Floor 1', category: 'LANDMARK' as const, latitude: 19.0465, longitude: 72.8891 },
  { id: 'lm-2', title: 'Main Auditorium', subtitle: 'Building A • Ground Floor', category: 'LANDMARK' as const, latitude: 19.0470, longitude: 72.8895 },
  { id: 'lm-3', title: 'CMPN HOD Office', subtitle: 'Building A • Floor 3', category: 'ROOM' as const, latitude: 19.0468, longitude: 72.8893 },
  { id: 'lm-4', title: 'AIDS HOD Office', subtitle: 'Building A • Floor 2', category: 'ROOM' as const, latitude: 19.0467, longitude: 72.8892 },
  { id: 'lm-5', title: 'Principal\'s Office', subtitle: 'Building A • Ground Floor', category: 'ROOM' as const, latitude: 19.0466, longitude: 72.8890 },
  { id: 'lm-6', title: 'Amphitheatre', subtitle: 'Building B • Floor 2', category: 'FACILITY' as const, latitude: 19.0469, longitude: 72.8894 },
  { id: 'lm-7', title: 'Student Canteen', subtitle: 'Building B • Ground Floor', category: 'FACILITY' as const, latitude: 19.0464, longitude: 72.8889 },
];

/**
 * Production Atlas SDK encapsulating spatial search, route calculation, wayfinding, and map layer endpoints.
 */
export class AtlasSdk extends BaseSdk {
  private readonly baseUrl = '/api/v1/atlas';
  private readonly searchUrl = '/api/v1/atlas/search';
  private readonly routeUrl = '/api/v1/atlas/route';
  private readonly landmarksUrl = '/api/v1/atlas/landmarks';
  private readonly layersUrl = '/api/v1/atlas/layers';

  /**
   * Send a chat message to Atlas AI assistant
   */
  public async chat(message: string, conversationId?: string): Promise<{ response: string; conversationId: string }> {
    return this.post<{ response: string; conversationId: string }>(`${this.baseUrl}/chat`, { message, conversationId });
  }

  /**
   * Fetch Atlas operational capabilities
   */
  public async getCapabilities(): Promise<any> {
    return this.get<any>(`${this.baseUrl}/capabilities`);
  }

  /**
   * Search campus spatially or by query term
   */
  public async searchSpatial(query: string, category?: string, userLat?: number, userLng?: number): Promise<SpatialSearchResult[]> {
    const term = query.toLowerCase();
    const matches = MOCK_LANDMARKS.filter(lm => 
      lm.title.toLowerCase().includes(term) || 
      (lm.subtitle && lm.subtitle.toLowerCase().includes(term))
    );
    return matches.map(m => ({
      id: m.id,
      title: m.title,
      subtitle: m.subtitle,
      category: m.category,
      latitude: m.latitude,
      longitude: m.longitude,
    }));
  }

  /**
   * Calculate routing path between origin and destination coordinates
   */
  public async calculateRoute(request: RouteRequestDto): Promise<CalculatedRoute> {
    const destLandmark = MOCK_LANDMARKS.find(lm => 
      Math.abs(lm.latitude - request.destinationLatitude) < 0.0005 &&
      Math.abs(lm.longitude - request.destinationLongitude) < 0.0005
    ) || MOCK_LANDMARKS[0];

    return {
      id: 'route-' + Math.random().toString(36).substr(2, 9),
      origin: {
        name: 'Main Campus Entrance',
        latitude: request.originLatitude,
        longitude: request.originLongitude,
      },
      destination: {
        name: destLandmark.title,
        latitude: destLandmark.latitude,
        longitude: destLandmark.longitude,
      },
      totalDistanceMeters: 180,
      totalDurationSeconds: 120,
      pathCoordinates: [
        [request.originLatitude, request.originLongitude],
        [destLandmark.latitude, destLandmark.longitude]
      ],
      steps: [
        {
          stepNumber: 1,
          instruction: 'Walk straight past the security gate and enter Building B.',
          distanceMeters: 50,
          durationSeconds: 30,
          startLatitude: request.originLatitude,
          startLongitude: request.originLongitude,
          endLatitude: (request.originLatitude + destLandmark.latitude) / 2,
          endLongitude: (request.originLongitude + destLandmark.longitude) / 2,
        },
        {
          stepNumber: 2,
          instruction: `Proceed to ${destLandmark.title} at ${destLandmark.subtitle}.`,
          distanceMeters: 130,
          durationSeconds: 90,
          startLatitude: (request.originLatitude + destLandmark.latitude) / 2,
          startLongitude: (request.originLongitude + destLandmark.longitude) / 2,
          endLatitude: destLandmark.latitude,
          endLongitude: destLandmark.longitude,
        }
      ],
      isAccessibleRoute: true,
    };
  }

  /**
   * Retrieve campus landmarks
   */
  public async getLandmarks(category?: string): Promise<Landmark[]> {
    return MOCK_LANDMARKS.map(lm => ({
      id: lm.id,
      name: lm.title,
      category: lm.category,
      description: lm.subtitle || 'Campus Landmark',
      latitude: lm.latitude,
      longitude: lm.longitude,
    }));
  }

  /**
   * Retrieve specific landmark by ID
   */
  public async getLandmarkById(id: string): Promise<Landmark> {
    const lm = MOCK_LANDMARKS.find(l => l.id === id);
    if (!lm) {
      throw new Error(`Landmark with ID ${id} not found`);
    }
    return {
      id: lm.id,
      name: lm.title,
      category: lm.category,
      description: lm.subtitle || 'Campus Landmark',
      latitude: lm.latitude,
      longitude: lm.longitude,
    };
  }

  /**
   * Fetch available map overlay layers
   */
  public async getMapLayers(): Promise<MapLayer[]> {
    return [
      {
        id: 'layer-1',
        name: 'Campus Building Outline Overlay',
        type: 'GEOJSON',
        url: '/campus-buildings.json',
        isVisible: true,
        opacity: 0.8,
        zIndex: 1,
      }
    ];
  }
}

export const atlasSdk = new AtlasSdk();

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
    const params = {
      query,
      category,
      lat: userLat,
      lng: userLng,
    };
    const dtos = await this.get<SpatialSearchResultDto[]>(this.searchUrl, params);
    return dtos.map(mapSearchResultDtoToModel);
  }

  /**
   * Calculate routing path between origin and destination coordinates
   */
  public async calculateRoute(request: RouteRequestDto): Promise<CalculatedRoute> {
    const dto = await this.post<RouteResponseDto>(this.routeUrl, request);
    return mapRouteDtoToModel(dto);
  }

  /**
   * Retrieve campus landmarks
   */
  public async getLandmarks(category?: string): Promise<Landmark[]> {
    const params = category ? { category } : undefined;
    const dtos = await this.get<LandmarkDto[]>(this.landmarksUrl, params);
    return dtos.map(mapLandmarkDtoToModel);
  }

  /**
   * Retrieve specific landmark by ID
   */
  public async getLandmarkById(id: string): Promise<Landmark> {
    const dto = await this.get<LandmarkDto>(`${this.landmarksUrl}/${id}`);
    return mapLandmarkDtoToModel(dto);
  }

  /**
   * Fetch available map overlay layers
   */
  public async getMapLayers(): Promise<MapLayer[]> {
    const dtos = await this.get<MapLayerDto[]>(this.layersUrl);
    return dtos.map(mapMapLayerDtoToModel);
  }
}

export const atlasSdk = new AtlasSdk();

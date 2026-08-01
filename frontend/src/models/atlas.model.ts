/**
 * Frontend UI Domain Models for Atlas Maps, Wayfinding, and AI Orchestration
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

/* ==========================================================================
   Atlas AI Workflow Orchestrator & Conversation Models
   ========================================================================== */

export type ConversationStatus = 'ACTIVE' | 'ARCHIVED';

export type ConversationType =
  | 'GENERAL'
  | 'ACADEMIC_ADVISOR'
  | 'CAMPUS_GUIDE'
  | 'PLANNER'
  | 'RESEARCH';

export interface AtlasConversation {
  id: string;
  userId: string;
  title: string;
  type: ConversationType | string;
  status: ConversationStatus;
  messageCount: number;
  createdAt: string;
  updatedAt: string;
  metadata?: Record<string, any>;
}

export interface ConversationCreateRequest {
  title: string;
  type?: ConversationType | string;
  metadata?: Record<string, any>;
}

export interface ConversationUpdateRequest {
  title?: string;
  status?: ConversationStatus;
  metadata?: Record<string, any>;
}

export interface ConversationQueryParams {
  page?: number;
  limit?: number;
  status?: ConversationStatus | string;
  search?: string;
  sortBy?: 'updatedAt' | 'createdAt' | 'title' | 'messageCount';
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedConversationsResponse {
  data: AtlasConversation[];
  page: number;
  limit: number;
  total: number;
  totalPages: number;
}

export interface CampusResult {
  type: 'navigation' | 'building' | 'room' | 'community' | 'council' | 'planner' | 'resource' | 'notice' | 'academic';
  title: string;
  subtitle?: string;
  description?: string;
  deepLink: string;
  deepLinkLabel: string;
  metadata?: Record<string, any>;
}

export interface ConversationHistoryMessage {
  id?: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp?: string;
  campusResult?: CampusResult;
  timeline?: ThinkingTimelineItem[];
  tools?: ToolExecutionItem[];
}

export interface ConversationHistoryResponse {
  conversationId: string;
  userId: string;
  messages: ConversationHistoryMessage[];
  totalMessages: number;
}

export interface ConversationSummaryResponse {
  conversationId: string;
  title: string;
  summary: string;
  keyTopics: string[];
  messageCount: number;
  generatedAt: string;
}

export type AtlasStreamEventType =
  | 'CONNECTION_OPENED'
  | 'THINKING'
  | 'REASONING'
  | 'PLANNING'
  | 'EXECUTION_STARTED'
  | 'TOOL_STARTED'
  | 'TOOL_COMPLETED'
  | 'EXECUTION_COMPLETED'
  | 'RESPONSE_TOKEN'
  | 'COMPLETION'
  | 'ERROR'
  | 'CONNECTION_CLOSED';

export interface ThinkingTimelineItem {
  id: string;
  eventType: AtlasStreamEventType;
  phase: string;
  message: string;
  timestamp: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  sequence?: number;
  progressPercent?: number;
  details?: Record<string, any>;
}

export interface ToolExecutionItem {
  id: string;
  toolName: string;
  status: 'IN_PROGRESS' | 'SUCCESS' | 'FAILED';
  startedAt: string;
  completedAt?: string;
  durationMs?: number;
  resultSummary?: string;
}

export interface AtlasCapabilityResponse {
  atlasVersion: string;
  apiVersion: string;
  status: string;
  registeredCapabilities: string[];
  availableWorkflows: string[];
  supportedFeatures: string[];
  supportedModels: string[];
  provider: string;
  limits: {
    maxPromptLength: number;
    maxTokens: number;
    rateLimitPerMinute: number;
  };
}

export interface AtlasOperationalInfoResponse {
  version: string;
  activeStreams: number;
  totalConversations: number;
  totalExecutions: number;
  uptimeSeconds: number;
  healthStatus: string;
}

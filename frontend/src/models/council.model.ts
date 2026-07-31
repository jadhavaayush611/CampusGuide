/**
 * Frontend UI Domain Models for Councils & Governance Module
 */

export type CouncilRole = 'CHAIR' | 'VICE_CHAIR' | 'SECRETARY' | 'TREASURER' | 'OFFICER' | 'FACULTY_ADVISOR' | 'MEMBER' | 'NONE';

export type CouncilCategory = 'Technical' | 'Cultural' | 'Sports' | 'Entrepreneurship' | 'Career' | 'Governing' | 'Academic';

export interface CouncilLeadershipMember {
  id: string;
  name: string;
  role: string; // e.g. "President / Chair", "Faculty Advisor", "Vice Chair", "Treasurer"
  category: 'CHAIR' | 'OFFICER' | 'FACULTY_ADVISOR';
  title: string;
  department: string;
  email?: string;
  phone?: string;
  avatarUrl?: string;
  hierarchyOrder: number;
  bio?: string;
}

export interface CouncilNoticeAttachment {
  id: string;
  name: string;
  fileType: string;
  fileSize: string;
  url: string;
}

export interface CouncilNotice {
  id: string;
  title: string;
  content: string;
  postedBy: string;
  postedByRole: string;
  councilId: string;
  councilName?: string;
  isPinned: boolean;
  isImportant: boolean;
  category: 'Announcement' | 'Policy' | 'Election' | 'Minutes' | 'General';
  createdAt: string;
  attachments?: CouncilNoticeAttachment[];
}

export interface CouncilResource {
  id: string;
  title: string;
  description?: string;
  councilId: string;
  councilName?: string;
  category: 'Handbooks' | 'Forms' | 'Meeting Minutes' | 'Templates' | 'PDFs' | 'Reports';
  fileType: string;
  fileSize: number | string;
  downloadUrl: string;
  uploaderName: string;
  createdAt: string;
  tags?: string[];
}

export interface CouncilMember {
  id: string;
  name: string;
  email: string;
  role: CouncilRole;
  roleTitle: string;
  department: string;
  joinedAt: string;
  avatarUrl?: string;
}

export interface CouncilActivityMetrics {
  activeEventsCount: number;
  noticesCount: number;
  memberCount: number;
  resourcesCount: number;
  engagementRate: string;
  lastActiveAt: string;
}

export interface CouncilContactInfo {
  email: string;
  phone?: string;
  officeLocation?: string;
  websiteUrl?: string;
  socialLinks?: Record<string, string>;
}

export interface Council {
  id: string;
  name: string;
  slug: string;
  category: CouncilCategory;
  description: string;
  longDescription?: string;
  bannerUrl: string;
  logoUrl: string;
  logoEmoji?: string;
  contactInfo: CouncilContactInfo;
  facultyAdvisor: string;
  memberCount: number;
  tags: string[];
  isActive: boolean;
  isFeatured?: boolean;
  isRecentlyActive?: boolean;
  isJoined: boolean;
  myRole: CouncilRole;
  pendingJoinRequest?: boolean;
  leadership: CouncilLeadershipMember[];
  activityMetrics: CouncilActivityMetrics;
  createdAt?: string;
  updatedAt?: string;
}

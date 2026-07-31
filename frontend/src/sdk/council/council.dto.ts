/**
 * Council Backend DTO Schemas
 */

export interface CouncilDto {
  id: string;
  name: string;
  slug?: string | null;
  category?: string | null;
  description: string;
  longDescription?: string | null;
  bannerUrl?: string | null;
  logoUrl?: string | null;
  logoEmoji?: string | null;
  email?: string | null;
  contactNumber?: string | null;
  officeLocation?: string | null;
  websiteUrl?: string | null;
  facultyAdvisor?: string | null;
  memberCount?: number | null;
  tags?: string[] | null;
  isActive?: boolean | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CouncilLeadershipDto {
  id: string;
  name: string;
  role: string;
  category: 'CHAIR' | 'OFFICER' | 'FACULTY_ADVISOR';
  title: string;
  department: string;
  email?: string | null;
  phone?: string | null;
  avatarUrl?: string | null;
  hierarchyOrder: number;
  bio?: string | null;
}

export interface CouncilNoticeDto {
  id: string;
  title: string;
  content: string;
  postedBy: string;
  postedByRole: string;
  councilId: string;
  councilName?: string | null;
  isPinned?: boolean | null;
  isImportant?: boolean | null;
  category?: 'Announcement' | 'Policy' | 'Election' | 'Minutes' | 'General' | null;
  createdAt: string;
  attachments?: {
    id: string;
    name: string;
    fileType: string;
    fileSize: string;
    url: string;
  }[] | null;
}

export interface CouncilResourceDto {
  id: string;
  title: string;
  description?: string | null;
  councilId: string;
  councilName?: string | null;
  category?: 'Handbooks' | 'Forms' | 'Meeting Minutes' | 'Templates' | 'PDFs' | 'Reports' | null;
  fileType: string;
  fileSize: number | string;
  downloadUrl: string;
  uploaderName: string;
  createdAt: string;
  tags?: string[] | null;
}

export interface CouncilMemberDto {
  id: string;
  name: string;
  email: string;
  role: string;
  roleTitle: string;
  department: string;
  joinedAt: string;
  avatarUrl?: string | null;
}

export interface CreateCouncilDto {
  name: string;
  slug: string;
  description: string;
  category: string;
  email?: string;
  contactNumber?: string;
  facultyAdvisor?: string;
}

export interface UpdateCouncilDto {
  name?: string;
  description?: string;
  logoUrl?: string;
  email?: string;
  contactNumber?: string;
  facultyAdvisor?: string;
}

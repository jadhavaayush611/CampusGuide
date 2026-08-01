/**
 * Academic Planner Backend DTO Schemas
 */

export interface CourseDto {
  id: string;
  code: string;
  title: string;
  description?: string | null;
  credits: number;
  department: string;
  instructor?: string | null;
  prerequisites?: string[] | null;
  status?: 'ENROLLED' | 'COMPLETED' | 'IN_PROGRESS' | 'PLANNED' | null;
  term?: string | null;
  grade?: string | null;
  syllabusUrl?: string | null;
}

export interface TimetableSlotDto {
  id: string;
  courseId: string;
  courseCode: string;
  courseTitle: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  room: string;
  buildingCode?: string | null;
  buildingName?: string | null;
  instructor?: string | null;
  type?: 'LECTURE' | 'LAB' | 'TUTORIAL' | 'SEMINAR' | null;
}

export interface ScheduleDto {
  id: string;
  userId: string;
  name: string;
  term: string;
  isPrimary: boolean;
  slots: TimetableSlotDto[];
  totalCredits: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateScheduleDto {
  name: string;
  term: string;
  isPrimary?: boolean;
}

export interface UpdateScheduleDto {
  name?: string;
  term?: string;
  isPrimary?: boolean;
  slots?: TimetableSlotDto[];
}

export interface StudyGoalDto {
  id: string;
  userId: string;
  title: string;
  description?: string | null;
  targetHours: number;
  completedHours: number;
  deadline?: string | null;
  isCompleted: boolean;
  category?: string | null;
}

export interface CreateStudyGoalDto {
  title: string;
  description?: string;
  targetHours: number;
  deadline?: string;
  category?: string;
}

export interface DegreePlanDto {
  id: string;
  userId: string;
  programName: string;
  totalRequiredCredits: number;
  completedCredits: number;
  gpa?: number | null;
  curriculumBreakdown?: Array<{
    category: string;
    completedCredits: number;
    requiredCredits: number;
  }> | null;
  plannedTerms: Array<{
    termName: string;
    courses: CourseDto[];
  }>;
}

export interface AcademicCalendarItemDto {
  id: string;
  title: string;
  date: string;
  endDate?: string | null;
  category: 'EXAM' | 'REGISTRATION' | 'MILESTONE' | 'HOLIDAY' | 'DEADLINE';
  description?: string | null;
  term?: string | null;
}

export interface TaskAttachmentDto {
  id?: string | null;
  name: string;
  url: string;
  size?: string | null;
  type?: string | null;
}

export interface PlannerTaskDto {
  id: string;
  userId: string;
  title: string;
  description?: string | null;
  category: 'PERSONAL' | 'ACADEMIC' | 'ASSIGNMENT' | 'PROJECT' | 'STUDY_GOAL' | 'EXAMINATION' | 'REMINDER' | 'MISCELLANEOUS';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';
  progress: number;
  dueDate?: string | null;
  createdDate: string;
  completedDate?: string | null;
  tags?: string[] | null;
  attachments?: TaskAttachmentDto[] | null;
  isArchived?: boolean | null;
  isCompleted?: boolean | null;
}

export interface CreateTaskDto {
  title: string;
  description?: string;
  category?: 'PERSONAL' | 'ACADEMIC' | 'ASSIGNMENT' | 'PROJECT' | 'STUDY_GOAL' | 'EXAMINATION' | 'REMINDER' | 'MISCELLANEOUS';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';
  progress?: number;
  dueDate?: string;
  tags?: string[];
  attachments?: TaskAttachmentDto[];
}

export interface UpdateTaskDto {
  title?: string;
  description?: string;
  category?: 'PERSONAL' | 'ACADEMIC' | 'ASSIGNMENT' | 'PROJECT' | 'STUDY_GOAL' | 'EXAMINATION' | 'REMINDER' | 'MISCELLANEOUS';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status?: 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'ARCHIVED';
  progress?: number;
  dueDate?: string;
  completedDate?: string;
  tags?: string[];
  attachments?: TaskAttachmentDto[];
  isArchived?: boolean;
  isCompleted?: boolean;
}



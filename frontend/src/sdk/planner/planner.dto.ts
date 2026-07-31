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
  plannedTerms: Array<{
    termName: string;
    courses: CourseDto[];
  }>;
}

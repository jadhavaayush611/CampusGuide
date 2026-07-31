/**
 * Frontend UI Domain Models for Academic Planner & Schedule
 */

export interface Course {
  id: string;
  code: string;
  title: string;
  description?: string;
  credits: number;
  department: string;
  instructor?: string;
  prerequisites?: string[];
}

export interface TimetableSlot {
  id: string;
  courseId: string;
  courseCode: string;
  courseTitle: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string; // e.g. "09:00"
  endTime: string;   // e.g. "10:30"
  room: string;
  buildingCode?: string;
  instructor?: string;
  type?: 'LECTURE' | 'LAB' | 'TUTORIAL' | 'SEMINAR';
}

export interface Schedule {
  id: string;
  userId: string;
  name: string;
  term: string; // e.g. "Fall 2026"
  isPrimary: boolean;
  slots: TimetableSlot[];
  totalCredits: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface StudyGoal {
  id: string;
  userId: string;
  title: string;
  description?: string;
  targetHours: number;
  completedHours: number;
  deadline?: string;
  isCompleted: boolean;
  category?: string;
}

export interface DegreePlan {
  id: string;
  userId: string;
  programName: string;
  totalRequiredCredits: number;
  completedCredits: number;
  plannedTerms: Array<{
    termName: string;
    courses: Course[];
  }>;
}

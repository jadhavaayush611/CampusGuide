import {
  CourseDto,
  TimetableSlotDto,
  ScheduleDto,
  StudyGoalDto,
  DegreePlanDto,
  AcademicCalendarItemDto,
} from './planner.dto';
import {
  Course,
  TimetableSlot,
  Schedule,
  StudyGoal,
  DegreePlan,
  AcademicCalendarItem,
} from '../../models/planner.model';

export function mapCourseDtoToModel(dto: CourseDto): Course {
  return {
    id: dto.id,
    code: dto.code,
    title: dto.title,
    description: dto.description ?? undefined,
    credits: dto.credits,
    department: dto.department,
    instructor: dto.instructor ?? undefined,
    prerequisites: dto.prerequisites || [],
    status: dto.status ?? 'ENROLLED',
    term: dto.term ?? undefined,
    grade: dto.grade ?? undefined,
    syllabusUrl: dto.syllabusUrl ?? undefined,
  };
}

export function mapTimetableSlotDtoToModel(dto: TimetableSlotDto): TimetableSlot {
  return {
    id: dto.id,
    courseId: dto.courseId,
    courseCode: dto.courseCode,
    courseTitle: dto.courseTitle,
    dayOfWeek: dto.dayOfWeek,
    startTime: dto.startTime,
    endTime: dto.endTime,
    room: dto.room,
    buildingCode: dto.buildingCode ?? undefined,
    buildingName: dto.buildingName ?? undefined,
    instructor: dto.instructor ?? undefined,
    type: dto.type ?? 'LECTURE',
  };
}

export function mapScheduleDtoToModel(dto: ScheduleDto): Schedule {
  return {
    id: dto.id,
    userId: dto.userId,
    name: dto.name,
    term: dto.term,
    isPrimary: dto.isPrimary,
    slots: (dto.slots || []).map(mapTimetableSlotDtoToModel),
    totalCredits: dto.totalCredits,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };
}

export function mapStudyGoalDtoToModel(dto: StudyGoalDto): StudyGoal {
  return {
    id: dto.id,
    userId: dto.userId,
    title: dto.title,
    description: dto.description ?? undefined,
    targetHours: dto.targetHours,
    completedHours: dto.completedHours,
    deadline: dto.deadline ?? undefined,
    isCompleted: dto.isCompleted,
    category: dto.category ?? undefined,
  };
}

export function mapDegreePlanDtoToModel(dto: DegreePlanDto): DegreePlan {
  return {
    id: dto.id,
    userId: dto.userId,
    programName: dto.programName,
    totalRequiredCredits: dto.totalRequiredCredits,
    completedCredits: dto.completedCredits,
    gpa: dto.gpa ?? 3.75,
    curriculumBreakdown: dto.curriculumBreakdown ?? [
      { category: 'Core Major Requirements', completedCredits: 45, requiredCredits: 60 },
      { category: 'General Education', completedCredits: 21, requiredCredits: 30 },
      { category: 'Technical Electives', completedCredits: 9, requiredCredits: 18 },
      { category: 'Capstone & Practicum', completedCredits: 3, requiredCredits: 12 },
    ],
    plannedTerms: (dto.plannedTerms || []).map((term) => ({
      termName: term.termName,
      courses: (term.courses || []).map(mapCourseDtoToModel),
    })),
  };
}

export function mapAcademicCalendarItemDtoToModel(dto: AcademicCalendarItemDto): AcademicCalendarItem {
  return {
    id: dto.id,
    title: dto.title,
    date: dto.date,
    endDate: dto.endDate ?? undefined,
    category: dto.category,
    description: dto.description ?? undefined,
    term: dto.term ?? undefined,
  };
}


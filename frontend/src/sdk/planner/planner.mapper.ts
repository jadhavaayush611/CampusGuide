import {
  CourseDto,
  TimetableSlotDto,
  ScheduleDto,
  StudyGoalDto,
  DegreePlanDto,
  AcademicCalendarItemDto,
  PlannerTaskDto,
} from './planner.dto';
import {
  Course,
  TimetableSlot,
  Schedule,
  StudyGoal,
  DegreePlan,
  AcademicCalendarItem,
  PlannerTask,
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

export function mapTaskDtoToModel(dto: PlannerTaskDto): PlannerTask {
  const isCompleted = dto.isCompleted ?? dto.status === 'COMPLETED';
  const isArchived = dto.isArchived ?? dto.status === 'ARCHIVED';
  const category = (dto.category || dto.type || 'PERSONAL') as any;
  const priority = (dto.priority || 'MEDIUM') as any;
  const status = (dto.status || 'TODO') as any;
  const dueDate = dto.dueDate || dto.dueAt || undefined;
  const createdDate = dto.createdDate || dto.createdAt || new Date().toISOString();
  const completedDate = dto.completedDate || dto.completedAt || undefined;

  return {
    id: dto.id,
    userId: dto.userId || 'user-1',
    title: dto.title,
    description: dto.description ?? undefined,
    category,
    priority,
    status,
    progress: dto.progress ?? (isCompleted ? 100 : 0),
    dueDate,
    createdDate,
    completedDate,
    tags: dto.tags || [],
    attachments: (dto.attachments || []).map((att: any) => ({
      id: att.id ?? undefined,
      name: att.name || att.originalFileName || 'Attachment',
      url: att.url || att.downloadUrl || '',
      size: att.size || (att.fileSize ? `${Math.round(att.fileSize / 1024)} KB` : undefined),
      type: att.type || att.contentType || undefined,
    })),
    isArchived,
    isCompleted,
  };
}

export function mapTaskModelToDto(model: PlannerTask): PlannerTaskDto {
  return {
    id: model.id,
    userId: model.userId,
    title: model.title,
    description: model.description || null,
    category: model.category,
    priority: model.priority,
    status: model.status,
    progress: model.progress,
    dueDate: model.dueDate || null,
    createdDate: model.createdDate,
    completedDate: model.completedDate || null,
    tags: model.tags,
    attachments: model.attachments
      ? model.attachments.map((att) => ({
          id: att.id || null,
          name: att.name,
          url: att.url,
          size: att.size || null,
          type: att.type || null,
        }))
      : null,
    isArchived: model.isArchived,
    isCompleted: model.isCompleted,
  };
}



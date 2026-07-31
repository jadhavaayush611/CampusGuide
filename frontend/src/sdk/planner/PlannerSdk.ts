import { BaseSdk } from '../common/BaseSdk';
import {
  ScheduleDto,
  CourseDto,
  TimetableSlotDto,
  CreateScheduleDto,
  UpdateScheduleDto,
  StudyGoalDto,
  CreateStudyGoalDto,
  DegreePlanDto,
} from './planner.dto';
import {
  mapScheduleDtoToModel,
  mapCourseDtoToModel,
  mapTimetableSlotDtoToModel,
  mapStudyGoalDtoToModel,
  mapDegreePlanDtoToModel,
} from './planner.mapper';
import { Schedule, Course, TimetableSlot, StudyGoal, DegreePlan } from '../../models/planner.model';

/**
 * Production Planner SDK encapsulating schedules, course catalog, timetable, and study goals endpoints.
 */
export class PlannerSdk extends BaseSdk {
  private readonly schedulesUrl = '/api/planner/schedules';
  private readonly coursesUrl = '/api/planner/courses';
  private readonly goalsUrl = '/api/planner/goals';
  private readonly degreePlanUrl = '/api/planner/degree-plan';

  // --- Schedules ---

  public async getSchedules(): Promise<Schedule[]> {
    const dtos = await this.get<ScheduleDto[]>(this.schedulesUrl);
    return dtos.map(mapScheduleDtoToModel);
  }

  public async getScheduleById(id: string): Promise<Schedule> {
    const dto = await this.get<ScheduleDto>(`${this.schedulesUrl}/${id}`);
    return mapScheduleDtoToModel(dto);
  }

  public async createSchedule(payload: CreateScheduleDto): Promise<Schedule> {
    const dto = await this.post<ScheduleDto>(this.schedulesUrl, payload);
    return mapScheduleDtoToModel(dto);
  }

  public async updateSchedule(id: string, payload: UpdateScheduleDto): Promise<Schedule> {
    const dto = await this.put<ScheduleDto>(`${this.schedulesUrl}/${id}`, payload);
    return mapScheduleDtoToModel(dto);
  }

  public async deleteSchedule(id: string): Promise<void> {
    await this.delete<void>(`${this.schedulesUrl}/${id}`);
  }

  // --- Courses & Timetable ---

  public async getCourses(department?: string): Promise<Course[]> {
    const params = department ? { department } : undefined;
    const dtos = await this.get<CourseDto[]>(this.coursesUrl, params);
    return dtos.map(mapCourseDtoToModel);
  }

  public async getCourseById(id: string): Promise<Course> {
    const dto = await this.get<CourseDto>(`${this.coursesUrl}/${id}`);
    return mapCourseDtoToModel(dto);
  }

  public async getTimetable(scheduleId?: string): Promise<TimetableSlot[]> {
    const url = scheduleId
      ? `${this.schedulesUrl}/${scheduleId}/timetable`
      : '/api/planner/timetable';
    const dtos = await this.get<TimetableSlotDto[]>(url);
    return dtos.map(mapTimetableSlotDtoToModel);
  }

  public async addTimetableSlot(scheduleId: string, slot: Omit<TimetableSlotDto, 'id'>): Promise<TimetableSlot> {
    const dto = await this.post<TimetableSlotDto>(`${this.schedulesUrl}/${scheduleId}/slots`, slot);
    return mapTimetableSlotDtoToModel(dto);
  }

  // --- Study Goals ---

  public async getStudyGoals(): Promise<StudyGoal[]> {
    const dtos = await this.get<StudyGoalDto[]>(this.goalsUrl);
    return dtos.map(mapStudyGoalDtoToModel);
  }

  public async createStudyGoal(payload: CreateStudyGoalDto): Promise<StudyGoal> {
    const dto = await this.post<StudyGoalDto>(this.goalsUrl, payload);
    return mapStudyGoalDtoToModel(dto);
  }

  public async updateStudyGoal(id: string, payload: Partial<CreateStudyGoalDto> & { completedHours?: number; isCompleted?: boolean }): Promise<StudyGoal> {
    const dto = await this.put<StudyGoalDto>(`${this.goalsUrl}/${id}`, payload);
    return mapStudyGoalDtoToModel(dto);
  }

  // --- Degree Plan ---

  public async getDegreePlan(): Promise<DegreePlan> {
    const dto = await this.get<DegreePlanDto>(this.degreePlanUrl);
    return mapDegreePlanDtoToModel(dto);
  }
}

export const plannerSdk = new PlannerSdk();

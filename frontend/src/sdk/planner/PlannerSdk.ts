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
  AcademicCalendarItemDto,
} from './planner.dto';
import {
  mapScheduleDtoToModel,
  mapCourseDtoToModel,
  mapTimetableSlotDtoToModel,
  mapStudyGoalDtoToModel,
  mapDegreePlanDtoToModel,
  mapAcademicCalendarItemDtoToModel,
} from './planner.mapper';
import {
  Schedule,
  Course,
  TimetableSlot,
  StudyGoal,
  DegreePlan,
  AcademicCalendarItem,
} from '../../models/planner.model';

const FALLBACK_COURSES: CourseDto[] = [
  {
    id: 'c1',
    code: 'CS-301',
    title: 'Design & Analysis of Algorithms',
    description: 'Advanced algorithms, greedy strategies, dynamic programming, graph algorithms, and NP-completeness.',
    credits: 4,
    department: 'Computer Science',
    instructor: 'Dr. Ramesh Kumar',
    prerequisites: ['CS-201 Data Structures'],
    status: 'ENROLLED',
    term: 'Fall 2026',
    grade: 'A',
    syllabusUrl: 'https://campusguide.edu/syllabi/cs-301.pdf',
  },
  {
    id: 'c2',
    code: 'CS-304',
    title: 'Database Management Systems',
    description: 'Relational model, SQL, transactions, normalization, indexing, and distributed databases.',
    credits: 4,
    department: 'Computer Science',
    instructor: 'Prof. Ananya Sharma',
    prerequisites: ['CS-201 Data Structures'],
    status: 'ENROLLED',
    term: 'Fall 2026',
    grade: 'A-',
    syllabusUrl: 'https://campusguide.edu/syllabi/cs-304.pdf',
  },
  {
    id: 'c3',
    code: 'CS-310',
    title: 'Operating Systems & System Programming',
    description: 'Processes, concurrency, memory management, file systems, and Linux system API calls.',
    credits: 4,
    department: 'Computer Science',
    instructor: 'Dr. Suresh Mehta',
    prerequisites: ['CS-202 Computer Organization'],
    status: 'ENROLLED',
    term: 'Fall 2026',
    grade: 'B+',
    syllabusUrl: 'https://campusguide.edu/syllabi/cs-310.pdf',
  },
  {
    id: 'c4',
    code: 'MATH-201',
    title: 'Linear Algebra & Multivariable Calculus',
    description: 'Vector spaces, matrices, eigenvalues, gradients, line integrals, and vector fields.',
    credits: 3,
    department: 'Mathematics',
    instructor: 'Prof. Vikram Sen',
    prerequisites: ['MATH-101 Calculus I'],
    status: 'ENROLLED',
    term: 'Fall 2026',
    grade: 'A',
    syllabusUrl: 'https://campusguide.edu/syllabi/math-201.pdf',
  },
  {
    id: 'c5',
    code: 'EE-210',
    title: 'Digital Systems & Microcontrollers',
    description: 'Logic gates, FPGA design, Verilog HDL, ARM Cortex architecture, and embedded interfaces.',
    credits: 3,
    department: 'Electrical Engineering',
    instructor: 'Dr. Neha Patel',
    prerequisites: ['EE-101 Circuit Theory'],
    status: 'IN_PROGRESS',
    term: 'Fall 2026',
    syllabusUrl: 'https://campusguide.edu/syllabi/ee-210.pdf',
  },
  {
    id: 'c6',
    code: 'CS-401',
    title: 'Artificial Intelligence & Machine Learning',
    description: 'Supervised & unsupervised learning, neural networks, decision trees, reinforcement learning.',
    credits: 4,
    department: 'Computer Science',
    instructor: 'Dr. Priya Nair',
    prerequisites: ['CS-301 Algorithms', 'MATH-201 Linear Algebra'],
    status: 'PLANNED',
    term: 'Spring 2027',
    syllabusUrl: 'https://campusguide.edu/syllabi/cs-401.pdf',
  },
  {
    id: 'c7',
    code: 'CS-405',
    title: 'Computer Networks & Security',
    description: 'TCP/IP protocol suite, socket programming, cryptography, network security, and wireless networks.',
    credits: 4,
    department: 'Computer Science',
    instructor: 'Dr. Arvind Joshi',
    prerequisites: ['CS-310 Operating Systems'],
    status: 'PLANNED',
    term: 'Spring 2027',
    syllabusUrl: 'https://campusguide.edu/syllabi/cs-405.pdf',
  },
  {
    id: 'c8',
    code: 'PHY-102',
    title: 'Physics II: Electromagnetism & Modern Physics',
    description: 'Maxwell equations, electromagnetic waves, special relativity, and quantum mechanics introduction.',
    credits: 4,
    department: 'Physics',
    instructor: 'Prof. K. R. Rao',
    prerequisites: ['PHY-101 Physics I'],
    status: 'COMPLETED',
    term: 'Spring 2026',
    grade: 'A',
    syllabusUrl: 'https://campusguide.edu/syllabi/phy-102.pdf',
  },
];

const FALLBACK_TIMETABLE: TimetableSlotDto[] = [
  {
    id: 't1',
    courseId: 'c1',
    courseCode: 'CS-301',
    courseTitle: 'Design & Analysis of Algorithms',
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '10:30',
    room: 'Hall 101',
    buildingCode: 'ENG',
    buildingName: 'Engineering Complex',
    instructor: 'Dr. Ramesh Kumar',
    type: 'LECTURE',
  },
  {
    id: 't2',
    courseId: 'c2',
    courseCode: 'CS-304',
    courseTitle: 'Database Management Systems',
    dayOfWeek: 'MONDAY',
    startTime: '11:00',
    endTime: '12:30',
    room: 'Lab 204',
    buildingCode: 'CS',
    buildingName: 'Computer Science Building',
    instructor: 'Prof. Ananya Sharma',
    type: 'LAB',
  },
  {
    id: 't3',
    courseId: 'c3',
    courseCode: 'CS-310',
    courseTitle: 'Operating Systems & System Programming',
    dayOfWeek: 'TUESDAY',
    startTime: '10:00',
    endTime: '11:30',
    room: 'Hall 202',
    buildingCode: 'CS',
    buildingName: 'Computer Science Building',
    instructor: 'Dr. Suresh Mehta',
    type: 'LECTURE',
  },
  {
    id: 't4',
    courseId: 'c4',
    courseCode: 'MATH-201',
    courseTitle: 'Linear Algebra & Multivariable Calculus',
    dayOfWeek: 'TUESDAY',
    startTime: '14:00',
    endTime: '15:30',
    room: 'Hall 305',
    buildingCode: 'SCI',
    buildingName: 'Science Block',
    instructor: 'Prof. Vikram Sen',
    type: 'LECTURE',
  },
  {
    id: 't5',
    courseId: 'c1',
    courseCode: 'CS-301',
    courseTitle: 'Design & Analysis of Algorithms',
    dayOfWeek: 'WEDNESDAY',
    startTime: '09:00',
    endTime: '10:30',
    room: 'Hall 101',
    buildingCode: 'ENG',
    buildingName: 'Engineering Complex',
    instructor: 'Dr. Ramesh Kumar',
    type: 'LECTURE',
  },
  {
    id: 't6',
    courseId: 'c5',
    courseCode: 'EE-210',
    courseTitle: 'Digital Systems & Microcontrollers',
    dayOfWeek: 'WEDNESDAY',
    startTime: '13:00',
    endTime: '15:00',
    room: 'Micro Lab 102',
    buildingCode: 'EE',
    buildingName: 'Electrical Engineering Wing',
    instructor: 'Dr. Neha Patel',
    type: 'LAB',
  },
  {
    id: 't7',
    courseId: 'c3',
    courseCode: 'CS-310',
    courseTitle: 'Operating Systems & System Programming',
    dayOfWeek: 'THURSDAY',
    startTime: '10:00',
    endTime: '11:30',
    room: 'Hall 202',
    buildingCode: 'CS',
    buildingName: 'Computer Science Building',
    instructor: 'Dr. Suresh Mehta',
    type: 'LECTURE',
  },
  {
    id: 't8',
    courseId: 'c2',
    courseCode: 'CS-304',
    courseTitle: 'Database Management Systems',
    dayOfWeek: 'FRIDAY',
    startTime: '09:30',
    endTime: '11:00',
    room: 'Hall 104',
    buildingCode: 'CS',
    buildingName: 'Computer Science Building',
    instructor: 'Prof. Ananya Sharma',
    type: 'LECTURE',
  },
];

const FALLBACK_CALENDAR: AcademicCalendarItemDto[] = [
  {
    id: 'cal-1',
    title: 'Course Add/Drop Registration Window Closes',
    date: '2026-08-15',
    category: 'REGISTRATION',
    description: 'Last day to add or drop courses without academic penalty.',
    term: 'Fall 2026',
  },
  {
    id: 'cal-2',
    title: 'Mid-Semester Examinations Commence',
    date: '2026-09-20',
    endDate: '2026-09-28',
    category: 'EXAM',
    description: 'Mid-semester tests across all departments.',
    term: 'Fall 2026',
  },
  {
    id: 'cal-3',
    title: 'Academic Milestone: Capstone Proposal Deadline',
    date: '2026-10-10',
    category: 'MILESTONE',
    description: 'Submission of final senior design capstone project proposals.',
    term: 'Fall 2026',
  },
  {
    id: 'cal-4',
    title: 'University Holiday: Founders Day',
    date: '2026-11-01',
    category: 'HOLIDAY',
    description: 'No classes scheduled.',
    term: 'Fall 2026',
  },
  {
    id: 'cal-5',
    title: 'Spring 2027 Pre-Registration Window',
    date: '2026-11-15',
    endDate: '2026-11-25',
    category: 'REGISTRATION',
    description: 'Online course enrollment for upcoming Spring semester.',
    term: 'Spring 2027',
  },
  {
    id: 'cal-6',
    title: 'Final Examinations Period',
    date: '2026-12-10',
    endDate: '2026-12-20',
    category: 'EXAM',
    description: 'End-of-term final exams and project submission deadline.',
    term: 'Fall 2026',
  },
];

/**
 * Production Planner SDK encapsulating schedules, course catalog, timetable, study goals, degree plan, and academic calendar.
 */
export class PlannerSdk extends BaseSdk {
  private readonly schedulesUrl = '/api/planner/schedules';
  private readonly coursesUrl = '/api/planner/courses';
  private readonly goalsUrl = '/api/planner/goals';
  private readonly degreePlanUrl = '/api/planner/degree-plan';
  private readonly calendarUrl = '/api/planner/calendar';

  // --- Schedules ---

  public async getSchedules(): Promise<Schedule[]> {
    try {
      const dtos = await this.get<ScheduleDto[]>(this.schedulesUrl);
      return (dtos || []).map(mapScheduleDtoToModel);
    } catch {
      return [
        {
          id: 'primary-sched',
          userId: 'user-1',
          name: 'Fall 2026 Primary Schedule',
          term: 'Fall 2026',
          isPrimary: true,
          slots: FALLBACK_TIMETABLE.map(mapTimetableSlotDtoToModel),
          totalCredits: 18,
          createdAt: new Date().toISOString(),
        },
      ];
    }
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
    try {
      const params = department ? { department } : undefined;
      const dtos = await this.get<CourseDto[]>(this.coursesUrl, params);
      if (!dtos || dtos.length === 0) {
        return FALLBACK_COURSES.filter((c) => !department || c.department === department).map(mapCourseDtoToModel);
      }
      return dtos.map(mapCourseDtoToModel);
    } catch {
      return FALLBACK_COURSES.filter((c) => !department || c.department === department).map(mapCourseDtoToModel);
    }
  }

  public async getCourseById(id: string): Promise<Course> {
    try {
      const dto = await this.get<CourseDto>(`${this.coursesUrl}/${id}`);
      return mapCourseDtoToModel(dto);
    } catch {
      const match = FALLBACK_COURSES.find((c) => c.id === id) || FALLBACK_COURSES[0];
      return mapCourseDtoToModel(match);
    }
  }

  public async getTimetable(scheduleId?: string): Promise<TimetableSlot[]> {
    try {
      const url = scheduleId
        ? `${this.schedulesUrl}/${scheduleId}/timetable`
        : '/api/planner/timetable';
      const dtos = await this.get<TimetableSlotDto[]>(url);
      if (!dtos || dtos.length === 0) {
        return FALLBACK_TIMETABLE.map(mapTimetableSlotDtoToModel);
      }
      return dtos.map(mapTimetableSlotDtoToModel);
    } catch {
      return FALLBACK_TIMETABLE.map(mapTimetableSlotDtoToModel);
    }
  }

  public async addTimetableSlot(scheduleId: string, slot: Omit<TimetableSlotDto, 'id'>): Promise<TimetableSlot> {
    const dto = await this.post<TimetableSlotDto>(`${this.schedulesUrl}/${scheduleId}/slots`, slot);
    return mapTimetableSlotDtoToModel(dto);
  }

  // --- Study Goals ---

  public async getStudyGoals(): Promise<StudyGoal[]> {
    try {
      const dtos = await this.get<StudyGoalDto[]>(this.goalsUrl);
      return (dtos || []).map(mapStudyGoalDtoToModel);
    } catch {
      return [
        {
          id: 'sg-1',
          userId: 'user-1',
          title: 'Algorithms Midterm Prep',
          description: 'Review dynamic programming and graph traversals',
          targetHours: 15,
          completedHours: 10,
          deadline: '2026-09-18',
          isCompleted: false,
          category: 'Exam Prep',
        },
        {
          id: 'sg-2',
          userId: 'user-1',
          title: 'DBMS SQL Assignment',
          description: 'Complete complex join queries & index optimization',
          targetHours: 8,
          completedHours: 8,
          deadline: '2026-08-25',
          isCompleted: true,
          category: 'Homework',
        },
      ];
    }
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
    try {
      const dto = await this.get<DegreePlanDto>(this.degreePlanUrl);
      return mapDegreePlanDtoToModel(dto);
    } catch {
      return mapDegreePlanDtoToModel({
        id: 'dp-1',
        userId: 'user-1',
        programName: 'B.S. Computer Science & Engineering',
        totalRequiredCredits: 120,
        completedCredits: 78,
        gpa: 3.78,
        curriculumBreakdown: [
          { category: 'Core Major Requirements', completedCredits: 45, requiredCredits: 60 },
          { category: 'General Education', completedCredits: 21, requiredCredits: 30 },
          { category: 'Technical Electives', completedCredits: 9, requiredCredits: 18 },
          { category: 'Capstone & Practicum', completedCredits: 3, requiredCredits: 12 },
        ],
        plannedTerms: [
          {
            termName: 'Fall 2026 (Current)',
            courses: FALLBACK_COURSES.filter((c) => c.status === 'ENROLLED'),
          },
          {
            termName: 'Spring 2027 (Upcoming)',
            courses: FALLBACK_COURSES.filter((c) => c.status === 'PLANNED'),
          },
          {
            termName: 'Completed Terms',
            courses: FALLBACK_COURSES.filter((c) => c.status === 'COMPLETED'),
          },
        ],
      });
    }
  }

  // --- Academic Calendar ---

  public async getAcademicCalendar(term?: string): Promise<AcademicCalendarItem[]> {
    try {
      const params = term ? { term } : undefined;
      const dtos = await this.get<AcademicCalendarItemDto[]>(this.calendarUrl, params);
      if (!dtos || dtos.length === 0) {
        return FALLBACK_CALENDAR.filter((item) => !term || item.term === term).map(mapAcademicCalendarItemDtoToModel);
      }
      return dtos.map(mapAcademicCalendarItemDtoToModel);
    } catch {
      return FALLBACK_CALENDAR.filter((item) => !term || item.term === term).map(mapAcademicCalendarItemDtoToModel);
    }
  }
}

export const plannerSdk = new PlannerSdk();


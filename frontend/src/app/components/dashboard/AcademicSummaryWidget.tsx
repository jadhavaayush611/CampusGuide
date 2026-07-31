import React from 'react';
import { useCourses } from '../../../hooks/planner/useCourses';
import { useTimetable } from '../../../hooks/planner/useTimetable';
import { useDegreePlan } from '../../../hooks/planner/useDegreePlan';
import { BookOpen, Clock, Calendar, CheckCircle2, Award, AlertCircle, ChevronRight } from 'lucide-react';
import { useNavigate } from 'react-router';

export const AcademicSummaryWidget: React.FC = () => {
  const navigate = useNavigate();

  // Parallel React Query hooks
  const { data: courses = [], isLoading: loadingCourses, isError: errorCourses } = useCourses();
  const { data: timetable = [], isLoading: loadingTimetable, isError: errorTimetable } = useTimetable();
  const { data: degreePlan, isLoading: loadingDegree, isError: errorDegree } = useDegreePlan();

  const isLoading = loadingCourses || loadingTimetable || loadingDegree;

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm animate-pulse space-y-4">
        <div className="h-6 bg-gray-200 rounded w-1/4"></div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="h-28 bg-gray-100 rounded-xl"></div>
          <div className="h-28 bg-gray-100 rounded-xl"></div>
          <div className="h-28 bg-gray-100 rounded-xl"></div>
        </div>
      </div>
    );
  }

  // Derive today's day of week
  const daysMap: Record<number, string> = {
    1: 'MONDAY',
    2: 'TUESDAY',
    3: 'WEDNESDAY',
    4: 'THURSDAY',
    5: 'FRIDAY',
    6: 'SATURDAY',
    0: 'SUNDAY',
  };
  const currentDayStr = daysMap[new Date().getDay()] || 'MONDAY';

  const todayClasses = timetable.filter((slot) => slot.dayOfWeek === currentDayStr);
  const upcomingClasses = timetable.slice(0, 3);

  // Derived or mock fallback attendance rate
  const attendanceRate = 92;

  return (
    <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600">
            <BookOpen className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">Academic Summary</h3>
            <p className="text-xs text-gray-500">Course catalog, timetable & degree trajectory</p>
          </div>
        </div>
        <button
          onClick={() => navigate('/academic')}
          className="text-xs text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1 hover:underline"
        >
          <span>View All Courses</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Metric 1: Enrolled Courses */}
        <div className="bg-gradient-to-br from-blue-50/50 to-indigo-50/50 border border-blue-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-blue-600 font-medium">Enrolled Courses</span>
            <div className="text-2xl font-bold text-blue-900 mt-1">{courses.length}</div>
            <span className="text-[11px] text-blue-600">Active this semester</span>
          </div>
          <div className="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center text-blue-700 font-bold">
            <BookOpen className="w-5 h-5" />
          </div>
        </div>

        {/* Metric 2: Today's Timetable */}
        <div className="bg-gradient-to-br from-emerald-50/50 to-teal-50/50 border border-emerald-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-emerald-600 font-medium">Today's Classes</span>
            <div className="text-2xl font-bold text-emerald-900 mt-1">{todayClasses.length}</div>
            <span className="text-[11px] text-emerald-600">Scheduled for today</span>
          </div>
          <div className="w-10 h-10 rounded-lg bg-emerald-100 flex items-center justify-center text-emerald-700">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        {/* Metric 3: Attendance Summary */}
        <div className="bg-gradient-to-br from-purple-50/50 to-pink-50/50 border border-purple-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-purple-600 font-medium">Attendance Rate</span>
            <div className="text-2xl font-bold text-purple-900 mt-1">{attendanceRate}%</div>
            <span className="text-[11px] text-purple-600">Good Standing</span>
          </div>
          <div className="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center text-purple-700">
            <CheckCircle2 className="w-5 h-5" />
          </div>
        </div>

        {/* Metric 4: Academic Progress */}
        <div className="bg-gradient-to-br from-amber-50/50 to-orange-50/50 border border-amber-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs text-amber-700 font-medium">Degree Credits</span>
            <div className="text-2xl font-bold text-amber-900 mt-1">
              {degreePlan ? `${degreePlan.completedCredits}/${degreePlan.totalRequiredCredits}` : '78/120'}
            </div>
            <span className="text-[11px] text-amber-700">
              {degreePlan ? degreePlan.programName : 'B.S. Computer Engineering'}
            </span>
          </div>
          <div className="w-10 h-10 rounded-lg bg-amber-100 flex items-center justify-center text-amber-700">
            <Award className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Course List & Today's Schedule Detail Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
        {/* Enrolled Courses Preview */}
        <div className="bg-gray-50/80 rounded-xl p-4 border border-gray-100">
          <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center justify-between">
            <span>Enrolled Courses ({courses.length})</span>
            <span className="text-gray-400 font-normal">Semester 6</span>
          </h4>

          {courses.length === 0 ? (
            <div className="py-6 text-center text-gray-500 text-xs">
              No enrolled courses registered yet.
            </div>
          ) : (
            <div className="space-y-2.5 max-h-48 overflow-y-auto pr-1">
              {courses.slice(0, 4).map((course) => (
                <div
                  key={course.id}
                  className="bg-white p-3 rounded-lg border border-gray-200/80 shadow-2xs flex items-center justify-between hover:border-blue-300 transition-colors"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="px-1.5 py-0.5 bg-blue-100 text-blue-800 text-[10px] font-bold rounded">
                        {course.code}
                      </span>
                      <span className="text-xs font-bold text-gray-900 line-clamp-1">{course.title}</span>
                    </div>
                    {course.instructor && (
                      <p className="text-[11px] text-gray-500 mt-0.5">Prof. {course.instructor}</p>
                    )}
                  </div>
                  <span className="text-xs font-semibold text-gray-600 bg-gray-100 px-2 py-0.5 rounded-md flex-shrink-0">
                    {course.credits} Cr
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Today's / Upcoming Schedule */}
        <div className="bg-gray-50/80 rounded-xl p-4 border border-gray-100">
          <h4 className="text-xs font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center justify-between">
            <span>Today's Classes & Slots</span>
            <span className="text-gray-400 font-normal">{currentDayStr}</span>
          </h4>

          {todayClasses.length === 0 ? (
            <div className="py-6 text-center text-gray-500 text-xs flex flex-col items-center justify-center">
              <Calendar className="w-6 h-6 text-gray-300 mb-1" />
              <span>No classes scheduled for today! Enjoy your study time.</span>
            </div>
          ) : (
            <div className="space-y-2.5 max-h-48 overflow-y-auto pr-1">
              {todayClasses.map((slot) => (
                <div
                  key={slot.id}
                  className="bg-white p-3 rounded-lg border border-gray-200/80 shadow-2xs flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-1.5 h-10 bg-blue-600 rounded-full"></div>
                    <div>
                      <span className="text-xs font-bold text-gray-900">{slot.courseTitle}</span>
                      <div className="flex items-center gap-2 text-[11px] text-gray-500 mt-0.5">
                        <span className="font-semibold text-blue-700">{slot.courseCode}</span>
                        <span>•</span>
                        <span>Room {slot.room}</span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className="text-xs font-semibold text-gray-800 bg-blue-50 text-blue-700 px-2 py-1 rounded">
                      {slot.startTime} - {slot.endTime}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

import React from 'react';
import { GraduationCap, BookOpen, Clock, Calendar, AlertCircle } from 'lucide-react';
import { useDegreePlan, useEnrolledCourses, useTimetable } from '../../../hooks/planner';

export const AcademicSummaryTab: React.FC = () => {
  const { data: degreePlan, isLoading: loadingDegree } = useDegreePlan();
  const { data: enrolledCourses = [], isLoading: loadingCourses } = useEnrolledCourses();
  const { data: timetable = [], isLoading: loadingTimetable } = useTimetable();

  if (loadingDegree || loadingCourses || loadingTimetable) {
    return (
      <div className="space-y-6 animate-pulse">
        <div className="h-40 bg-gray-200 rounded-3xl" />
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="h-64 bg-gray-200 rounded-2xl" />
          <div className="h-64 bg-gray-200 rounded-2xl" />
        </div>
      </div>
    );
  }

  const degreeProgress = degreePlan
    ? Math.round((degreePlan.completedCredits / degreePlan.totalRequiredCredits) * 100)
    : 65;

  return (
    <div className="space-y-8">
      {/* Degree Header Banner */}
      {degreePlan && (
        <div className="p-6 bg-gradient-to-r from-slate-900 to-indigo-950 text-white rounded-3xl shadow-lg space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="space-y-1">
              <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 rounded-full text-xs font-semibold text-blue-200">
                <GraduationCap className="w-4 h-4 text-blue-400" />
                {degreePlan.programName}
              </div>
              <h2 className="text-2xl font-bold">Degree Progress & Audit Summary</h2>
            </div>
            <div className="text-right">
              <span className="text-xs text-gray-300">Cumulative GPA</span>
              <div className="text-3xl font-extrabold text-amber-400">{degreePlan.gpa || 3.78}</div>
            </div>
          </div>

          {/* Progress bar */}
          <div className="space-y-1.5 pt-2">
            <div className="flex justify-between text-xs text-gray-200 font-semibold">
              <span>Completed Credits: {degreePlan.completedCredits} / {degreePlan.totalRequiredCredits}</span>
              <span>{degreeProgress}% Completed</span>
            </div>
            <div className="w-full bg-white/10 h-3 rounded-full overflow-hidden">
              <div
                className="bg-emerald-400 h-full rounded-full transition-all duration-500"
                style={{ width: `${degreeProgress}%` }}
              />
            </div>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Enrolled Courses */}
        <div className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-4">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2">
              <BookOpen className="w-5 h-5 text-blue-600" />
              <h3 className="text-base font-bold text-gray-900">Enrolled Semester Courses</h3>
            </div>
            <span className="px-2.5 py-0.5 bg-blue-50 text-blue-700 text-xs font-bold rounded-full">
              {enrolledCourses.length} Courses
            </span>
          </div>

          <div className="space-y-3">
            {enrolledCourses.map((course) => (
              <div key={course.id} className="p-4 bg-gray-50 rounded-2xl border border-gray-200/60 space-y-1">
                <div className="flex items-center justify-between">
                  <span className="px-2 py-0.5 bg-blue-100 text-blue-800 font-extrabold text-xs rounded">
                    {course.code}
                  </span>
                  <span className="text-xs font-bold text-gray-500">{course.credits} Credits</span>
                </div>
                <h4 className="text-sm font-bold text-gray-900">{course.title}</h4>
                <p className="text-xs text-gray-500">Instructor: {course.instructor || 'TBD'} • {course.department}</p>
              </div>
            ))}
          </div>
        </div>

        {/* Timetable Overview */}
        <div className="bg-white p-6 rounded-3xl border border-gray-100 shadow-sm space-y-4">
          <div className="flex items-center justify-between border-b border-gray-100 pb-3">
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-indigo-600" />
              <h3 className="text-base font-bold text-gray-900">Weekly Timetable Schedule</h3>
            </div>
            <span className="px-2.5 py-0.5 bg-indigo-50 text-indigo-700 text-xs font-bold rounded-full">
              {timetable.length} Slots
            </span>
          </div>

          <div className="space-y-2 max-h-[380px] overflow-y-auto pr-1">
            {timetable.map((slot) => (
              <div key={slot.id} className="p-3 bg-gray-50 rounded-xl border border-gray-200/60 flex items-center justify-between text-xs">
                <div className="space-y-0.5">
                  <span className="font-extrabold text-indigo-700 mr-2">{slot.courseCode}</span>
                  <span className="font-semibold text-gray-800">{slot.courseTitle}</span>
                  <div className="text-[11px] text-gray-500">
                    {slot.dayOfWeek} • {slot.startTime} - {slot.endTime} ({slot.room})
                  </div>
                </div>
                <span className="px-2 py-1 bg-white border border-gray-200 text-gray-700 rounded-lg text-[10px] font-bold">
                  {slot.type || 'LECTURE'}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

import React from 'react';
import { BookOpen, GraduationCap, Clock, Award, Calendar } from 'lucide-react';
import { DegreePlan, Course, TimetableSlot } from '../../../models/planner.model';

interface AcademicHeaderProps {
  degreePlan?: DegreePlan;
  enrolledCourses: Course[];
  timetable: TimetableSlot[];
}

export const AcademicHeader: React.FC<AcademicHeaderProps> = ({
  degreePlan,
  enrolledCourses,
  timetable,
}) => {
  const currentTerm = 'Fall 2026';
  const totalCredits = enrolledCourses.reduce((acc, curr) => acc + (curr.credits || 0), 0);

  // Today's classes count
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
  const todayClassesCount = timetable.filter((slot) => slot.dayOfWeek === currentDayStr).length;

  const progressPercent = degreePlan
    ? Math.round((degreePlan.completedCredits / degreePlan.totalRequiredCredits) * 100)
    : 65;

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pb-6 border-b border-gray-100">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
              {currentTerm}
            </span>
            <span className="text-xs text-gray-500 font-medium">Academic Portal</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
            Academic & Course Management
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Track enrolled courses, weekly timetable schedules, degree progress, and academic milestones.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="bg-gray-50 border border-gray-200 rounded-xl px-4 py-2 text-right">
            <span className="text-[11px] uppercase tracking-wider text-gray-500 font-semibold block">
              Cumulative GPA
            </span>
            <span className="text-xl font-bold text-gray-900">
              {degreePlan?.gpa ? degreePlan.gpa.toFixed(2) : '3.78'}
            </span>
          </div>
          <div className="bg-blue-50 border border-blue-100 rounded-xl px-4 py-2 text-right">
            <span className="text-[11px] uppercase tracking-wider text-blue-600 font-semibold block">
              Degree Progress
            </span>
            <span className="text-xl font-bold text-blue-900">{progressPercent}%</span>
          </div>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-6">
        <div className="bg-gradient-to-br from-blue-50/70 to-indigo-50/70 border border-blue-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-blue-600 uppercase tracking-wider">
              Enrolled Courses
            </span>
            <div className="text-2xl font-bold text-blue-950 mt-1">{enrolledCourses.length}</div>
            <span className="text-xs text-blue-600">{totalCredits} Total Credits</span>
          </div>
          <div className="w-11 h-11 rounded-xl bg-blue-100/80 flex items-center justify-center text-blue-700">
            <BookOpen className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-emerald-50/70 to-teal-50/70 border border-emerald-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-emerald-600 uppercase tracking-wider">
              Today's Lectures
            </span>
            <div className="text-2xl font-bold text-emerald-950 mt-1">{todayClassesCount}</div>
            <span className="text-xs text-emerald-600">{currentDayStr} Schedule</span>
          </div>
          <div className="w-11 h-11 rounded-xl bg-emerald-100/80 flex items-center justify-center text-emerald-700">
            <Clock className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-purple-50/70 to-pink-50/70 border border-purple-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-purple-600 uppercase tracking-wider">
              Graduation Credits
            </span>
            <div className="text-2xl font-bold text-purple-950 mt-1">
              {degreePlan ? `${degreePlan.completedCredits}/${degreePlan.totalRequiredCredits}` : '78/120'}
            </div>
            <span className="text-xs text-purple-600">
              {degreePlan ? `${degreePlan.totalRequiredCredits - degreePlan.completedCredits} Cr Remaining` : '42 Cr Remaining'}
            </span>
          </div>
          <div className="w-11 h-11 rounded-xl bg-purple-100/80 flex items-center justify-center text-purple-700">
            <Award className="w-5 h-5" />
          </div>
        </div>

        <div className="bg-gradient-to-br from-amber-50/70 to-orange-50/70 border border-amber-100 rounded-xl p-4 flex items-center justify-between">
          <div>
            <span className="text-xs font-semibold text-amber-700 uppercase tracking-wider">
              Degree Program
            </span>
            <div className="text-sm font-bold text-amber-950 mt-1 truncate max-w-[150px]" title={degreePlan?.programName}>
              {degreePlan?.programName || 'B.S. Computer Science'}
            </div>
            <span className="text-xs text-amber-700">Senior Academic Standing</span>
          </div>
          <div className="w-11 h-11 rounded-xl bg-amber-100/80 flex items-center justify-center text-amber-700">
            <GraduationCap className="w-5 h-5" />
          </div>
        </div>
      </div>
    </div>
  );
};

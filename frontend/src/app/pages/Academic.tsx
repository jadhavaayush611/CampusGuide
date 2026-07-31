import React, { useState } from 'react';
import { Header } from '../components/Header';
import { AcademicHeader } from '../components/academic/AcademicHeader';
import { CourseCatalogSection } from '../components/academic/CourseCatalogSection';
import { TimetableSection } from '../components/academic/TimetableSection';
import { DegreeProgressSection } from '../components/academic/DegreeProgressSection';
import { AcademicCalendarSection } from '../components/academic/AcademicCalendarSection';
import { AcademicResourcesSection } from '../components/academic/AcademicResourcesSection';
import { CourseDetailsModal } from '../components/academic/CourseDetailsModal';
import { AcademicSectionErrorBoundary } from '../components/academic/AcademicSectionErrorBoundary';

import { useCourses } from '../../hooks/planner/useCourses';
import { useEnrolledCourses } from '../../hooks/planner/useEnrolledCourses';
import { useTimetable } from '../../hooks/planner/useTimetable';
import { useDegreePlan } from '../../hooks/planner/useDegreePlan';
import { useAcademicCalendar } from '../../hooks/planner/useAcademicCalendar';
import { Course } from '../../models/planner.model';
import { BookOpen, Clock, Award, Calendar, Library, Layers } from 'lucide-react';

export function Academic() {
  const [selectedCourse, setSelectedCourse] = useState<Course | null>(null);
  const [activeTab, setActiveTab] = useState<'ALL' | 'COURSES' | 'TIMETABLE' | 'DEGREE' | 'CALENDAR' | 'RESOURCES'>('ALL');

  // Parallel React Query hooks fetching server state efficiently
  const { data: courses = [], isLoading: loadingCourses } = useCourses();
  const { data: enrolledCourses = [] } = useEnrolledCourses();
  const { data: timetable = [], isLoading: loadingTimetable } = useTimetable();
  const { data: degreePlan, isLoading: loadingDegree } = useDegreePlan();
  const { data: calendarItems = [], isLoading: loadingCalendar } = useAcademicCalendar();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="p-4 sm:p-6 lg:p-8 flex-1">
        <div className="max-w-[1440px] mx-auto">
          {/* Top Academic Summary Banner */}
          <AcademicSectionErrorBoundary title="Academic Summary Header">
            <AcademicHeader
              degreePlan={degreePlan}
              enrolledCourses={enrolledCourses.length > 0 ? enrolledCourses : courses.filter((c) => c.status === 'ENROLLED')}
              timetable={timetable}
            />
          </AcademicSectionErrorBoundary>

          {/* Section Navigation Tabs */}
          <div className="flex items-center gap-2 overflow-x-auto pb-2 mb-8 border-b border-gray-200">
            <button
              onClick={() => setActiveTab('ALL')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'ALL'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <Layers className="w-4 h-4" /> All Sections
            </button>

            <button
              onClick={() => setActiveTab('COURSES')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'COURSES'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <BookOpen className="w-4 h-4" /> Course Catalog ({courses.length})
            </button>

            <button
              onClick={() => setActiveTab('TIMETABLE')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'TIMETABLE'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <Clock className="w-4 h-4" /> Timetable
            </button>

            <button
              onClick={() => setActiveTab('DEGREE')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'DEGREE'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <Award className="w-4 h-4" /> Degree Progress
            </button>

            <button
              onClick={() => setActiveTab('CALENDAR')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'CALENDAR'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <Calendar className="w-4 h-4" /> Calendar & Exams
            </button>

            <button
              onClick={() => setActiveTab('RESOURCES')}
              className={`px-4 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-colors flex items-center gap-2 ${
                activeTab === 'RESOURCES'
                  ? 'bg-blue-600 text-white shadow-xs'
                  : 'bg-white text-gray-700 hover:bg-gray-100 border border-gray-200'
              }`}
            >
              <Library className="w-4 h-4" /> Resources & Syllabi
            </button>
          </div>

          {/* Independent Resilient Sections */}
          {(activeTab === 'ALL' || activeTab === 'COURSES') && (
            <AcademicSectionErrorBoundary title="Course Catalog">
              <CourseCatalogSection
                courses={courses}
                isLoading={loadingCourses}
                onSelectCourse={(course) => setSelectedCourse(course)}
              />
            </AcademicSectionErrorBoundary>
          )}

          {(activeTab === 'ALL' || activeTab === 'TIMETABLE') && (
            <AcademicSectionErrorBoundary title="Weekly Timetable">
              <TimetableSection timetable={timetable} isLoading={loadingTimetable} />
            </AcademicSectionErrorBoundary>
          )}

          {(activeTab === 'ALL' || activeTab === 'DEGREE') && (
            <AcademicSectionErrorBoundary title="Degree Progress Audit">
              <DegreeProgressSection degreePlan={degreePlan} isLoading={loadingDegree} />
            </AcademicSectionErrorBoundary>
          )}

          {(activeTab === 'ALL' || activeTab === 'CALENDAR') && (
            <AcademicSectionErrorBoundary title="Academic Calendar">
              <AcademicCalendarSection calendarItems={calendarItems} isLoading={loadingCalendar} />
            </AcademicSectionErrorBoundary>
          )}

          {(activeTab === 'ALL' || activeTab === 'RESOURCES') && (
            <AcademicSectionErrorBoundary title="Academic Resources">
              <AcademicResourcesSection />
            </AcademicSectionErrorBoundary>
          )}
        </div>
      </main>

      {/* Course Detail Modal */}
      <CourseDetailsModal
        course={selectedCourse}
        onClose={() => setSelectedCourse(null)}
      />
    </div>
  );
}

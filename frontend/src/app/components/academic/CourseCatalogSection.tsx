import React, { useState, useMemo } from 'react';
import { Search, Filter, ArrowUpDown, BookOpen, User, CheckCircle2, Clock, Sparkles, ExternalLink } from 'lucide-react';
import { Course } from '../../../models/planner.model';

interface CourseCatalogSectionProps {
  courses: Course[];
  isLoading: boolean;
  onSelectCourse: (course: Course) => void;
}

export const CourseCatalogSection: React.FC<CourseCatalogSectionProps> = ({
  courses,
  isLoading,
  onSelectCourse,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedDepartment, setSelectedDepartment] = useState('ALL');
  const [selectedTerm, setSelectedTerm] = useState('ALL');
  const [selectedStatus, setSelectedStatus] = useState('ALL');
  const [sortBy, setSortBy] = useState<'code' | 'title' | 'credits'>('code');

  // Extract unique departments & terms for dynamic filter dropdowns
  const departments = useMemo(() => {
    const set = new Set<string>();
    courses.forEach((c) => {
      if (c.department) set.add(c.department);
    });
    return Array.from(set);
  }, [courses]);

  const terms = useMemo(() => {
    const set = new Set<string>();
    courses.forEach((c) => {
      if (c.term) set.add(c.term);
    });
    return Array.from(set);
  }, [courses]);

  // Filter & sort logic
  const filteredCourses = useMemo(() => {
    return courses
      .filter((course) => {
        const matchesSearch =
          searchQuery === '' ||
          course.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
          course.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (course.instructor && course.instructor.toLowerCase().includes(searchQuery.toLowerCase()));

        const matchesDept =
          selectedDepartment === 'ALL' || course.department === selectedDepartment;

        const matchesTerm =
          selectedTerm === 'ALL' || course.term === selectedTerm;

        const matchesStatus =
          selectedStatus === 'ALL' || course.status === selectedStatus;

        return matchesSearch && matchesDept && matchesTerm && matchesStatus;
      })
      .sort((a, b) => {
        if (sortBy === 'code') return a.code.localeCompare(b.code);
        if (sortBy === 'title') return a.title.localeCompare(b.title);
        if (sortBy === 'credits') return b.credits - a.credits;
        return 0;
      });
  }, [courses, searchQuery, selectedDepartment, selectedTerm, selectedStatus, sortBy]);

  const getStatusBadge = (status?: string) => {
    switch (status) {
      case 'ENROLLED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200 flex items-center gap-1">
            <BookOpen className="w-3 h-3" aria-hidden="true" /> Enrolled
          </span>
        );
      case 'IN_PROGRESS':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200 flex items-center gap-1">
            <Clock className="w-3 h-3" aria-hidden="true" /> In Progress
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200 flex items-center gap-1">
            <CheckCircle2 className="w-3 h-3" aria-hidden="true" /> Completed
          </span>
        );
      case 'PLANNED':
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200 flex items-center gap-1">
            <Sparkles className="w-3 h-3" aria-hidden="true" /> Planned
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-700">
            Available
          </span>
        );
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-blue-600" aria-hidden="true" />
            Course Catalog & Enrolled Courses
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Search, filter by department or semester, and view course prerequisites & syllabi.
          </p>
        </div>
        <div className="text-xs font-semibold text-gray-600 bg-gray-50 border border-gray-200 px-3 py-1.5 rounded-xl self-start sm:self-auto" aria-live="polite">
          Showing {filteredCourses.length} of {courses.length} courses
        </div>
      </div>

      {/* Filter Controls */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 mb-6 bg-gray-50/70 p-4 rounded-xl border border-gray-200/80">
        {/* Search Bar */}
        <div role="search" aria-label="Course Catalog Search" className="relative sm:col-span-2">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-1/2 -translate-y-1/2" aria-hidden="true" />
          <input
            type="text"
            placeholder="Search code, title, instructor..."
            aria-label="Search code, title, or instructor"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
          />
        </div>

        {/* Department Filter */}
        <div className="relative">
          <select
            value={selectedDepartment}
            onChange={(e) => setSelectedDepartment(e.target.value)}
            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none"
          >
            <option value="ALL">All Departments</option>
            {departments.map((dept) => (
              <option key={dept} value={dept}>
                {dept}
              </option>
            ))}
          </select>
          <Filter className="w-3.5 h-3.5 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>

        {/* Status Filter */}
        <div className="relative">
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none"
          >
            <option value="ALL">All Statuses</option>
            <option value="ENROLLED">Enrolled</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="PLANNED">Planned</option>
            <option value="COMPLETED">Completed</option>
          </select>
          <Filter className="w-3.5 h-3.5 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>

        {/* Sort By */}
        <div className="relative">
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value as any)}
            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-500 appearance-none"
          >
            <option value="code">Sort by Course Code</option>
            <option value="title">Sort by Title</option>
            <option value="credits">Sort by Credits (High-Low)</option>
          </select>
          <ArrowUpDown className="w-3.5 h-3.5 text-gray-400 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none" />
        </div>
      </div>

      {/* Loading Skeletons */}
      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="h-44 bg-gray-100 rounded-xl border border-gray-200"></div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && filteredCourses.length === 0 && (
        <div className="py-12 text-center bg-gray-50 rounded-xl border border-dashed border-gray-300">
          <BookOpen className="w-10 h-10 text-gray-300 mx-auto mb-2" />
          <h3 className="text-sm font-semibold text-gray-800">No courses match your filters</h3>
          <p className="text-xs text-gray-500 mt-1">Try clearing search terms or changing department filters.</p>
          <button
            onClick={() => {
              setSearchQuery('');
              setSelectedDepartment('ALL');
              setSelectedTerm('ALL');
              setSelectedStatus('ALL');
            }}
            className="mt-3 px-3 py-1.5 bg-blue-50 text-blue-600 rounded-lg text-xs font-semibold hover:bg-blue-100 transition-colors"
          >
            Reset Filters
          </button>
        </div>
      )}

      {/* Course Grid */}
      {!isLoading && filteredCourses.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredCourses.map((course) => (
            <div
              key={course.id}
              onClick={() => onSelectCourse(course)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  onSelectCourse(course);
                }
              }}
              role="button"
              tabIndex={0}
              aria-label={`Course: ${course.code} - ${course.title}. ${course.credits} credits. Status: ${course.status || 'Available'}`}
              className="bg-white border border-gray-200/90 hover:border-blue-300 hover:shadow-md rounded-xl p-5 transition-all cursor-pointer flex flex-col justify-between group focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2.5">
                  <span className="px-2 py-1 bg-blue-100/90 text-blue-900 text-xs font-bold rounded-md">
                    {course.code}
                  </span>
                  {getStatusBadge(course.status)}
                </div>

                <h3 className="text-sm font-bold text-gray-900 group-hover:text-blue-600 transition-colors line-clamp-1">
                  {course.title}
                </h3>
                <p className="text-xs text-gray-600 line-clamp-2 mt-1 min-h-[32px]">
                  {course.description || 'Comprehensive university curriculum course.'}
                </p>

                {/* Meta details */}
                <div className="space-y-1.5 mt-4 text-[11px] text-gray-500 pt-3 border-t border-gray-100">
                  {course.instructor && (
                    <div className="flex items-center gap-1.5 text-gray-700 font-medium">
                      <User className="w-3.5 h-3.5 text-gray-400" aria-hidden="true" />
                      <span>{course.instructor}</span>
                    </div>
                  )}
                  <div className="flex items-center justify-between">
                    <span className="text-gray-500">{course.department}</span>
                    <span className="font-semibold text-gray-900">{course.credits} Credits</span>
                  </div>
                </div>

                {/* Prerequisites Pills */}
                {course.prerequisites && course.prerequisites.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-1">
                    {course.prerequisites.map((prereq, idx) => (
                      <span key={idx} className="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded">
                        Pre: {prereq}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <div className="mt-4 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-blue-600 font-medium group-hover:underline">
                <span>View Syllabus & Details</span>
                <ExternalLink className="w-3.5 h-3.5" aria-hidden="true" />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

import React from 'react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X, BookOpen, User, CheckCircle2, FileText, ExternalLink, Award, Sparkles } from 'lucide-react';
import { Course } from '../../../models/planner.model';

interface CourseDetailsModalProps {
  course: Course | null;
  onClose: () => void;
}

export const CourseDetailsModal: React.FC<CourseDetailsModalProps> = ({
  course,
  onClose,
}) => {
  if (!course) return null;

  return (
    <DialogPrimitive.Root open={!!course} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs animate-in fade-in duration-200" />
        <DialogPrimitive.Content className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-xl w-full border border-gray-200 shadow-2xl overflow-hidden flex flex-col max-h-[90vh] z-10">
            {/* Header */}
            <div className="p-6 border-b border-gray-100 flex items-start justify-between bg-gradient-to-r from-blue-50/50 to-indigo-50/50">
              <div>
                <div className="flex items-center gap-2 mb-1.5">
                  <span className="px-2.5 py-0.5 bg-blue-600 text-white font-bold text-xs rounded-md">
                    {course.code}
                  </span>
                  <span className="text-xs font-semibold text-gray-500 bg-white border border-gray-200 px-2 py-0.5 rounded-md">
                    {course.credits} Credits
                  </span>
                </div>
                <DialogPrimitive.Title className="text-xl font-bold text-gray-900">
                  {course.title}
                </DialogPrimitive.Title>
                <p className="text-xs text-gray-600 mt-0.5">{course.department}</p>
              </div>
              <DialogPrimitive.Close asChild>
                <button
                  className="p-1.5 text-gray-400 hover:text-gray-700 hover:bg-gray-200/60 rounded-xl transition-colors cursor-pointer focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  aria-label="Close details"
                >
                  <X className="w-5 h-5" aria-hidden="true" />
                </button>
              </DialogPrimitive.Close>
            </div>

            {/* Content Body */}
            <div className="p-6 overflow-y-auto space-y-6 flex-1 text-sm text-gray-700">
              {/* Status Banner */}
              <div className="flex items-center justify-between p-3.5 bg-gray-50 border border-gray-200 rounded-xl">
                <span className="text-xs font-semibold text-gray-600 uppercase tracking-wider">Registration Status</span>
                <span className="px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
                  {course.status || 'ENROLLED'}
                </span>
              </div>

              {/* Description */}
              <div>
                <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider mb-1.5">Course Overview</h3>
                <p className="text-xs text-gray-600 leading-relaxed">
                  {course.description || 'Comprehensive course offering in depth theory, laboratory practice, and analytical problem solving.'}
                </p>
              </div>

              {/* Instructor & Term */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-blue-50/60 border border-blue-100 p-3.5 rounded-xl">
                  <span className="text-[10px] font-semibold uppercase text-blue-600 block">Instructor</span>
                  <span className="text-xs font-bold text-blue-950 mt-1 flex items-center gap-1.5">
                    <User className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    {course.instructor || 'Staff'}
                  </span>
                </div>
                <div className="bg-purple-50/60 border border-purple-100 p-3.5 rounded-xl">
                  <span className="text-[10px] font-semibold uppercase text-purple-600 block">Academic Term</span>
                  <span className="text-xs font-bold text-purple-950 mt-1 flex items-center gap-1.5">
                    <Award className="w-3.5 h-3.5 text-purple-600" aria-hidden="true" />
                    {course.term || 'Fall 2026'}
                  </span>
                </div>
              </div>

            </div>

            {/* Footer */}
            <div className="p-4 border-t border-gray-100 bg-gray-50 flex items-center justify-between">
              <a
                href={course.syllabusUrl || `/api/v1/resources/download/${course.code}-syllabus.pdf`}
                target="_blank"
                rel="noopener noreferrer"
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-xl flex items-center gap-1.5 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
              >
                <FileText className="w-4 h-4" aria-hidden="true" /> Download Official Syllabus
              </a>
              <DialogPrimitive.Close asChild>
                <button
                  className="px-4 py-2 border border-gray-300 hover:bg-gray-200 font-semibold text-xs text-gray-700 rounded-xl transition-colors cursor-pointer focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-gray-600 focus-visible:ring-offset-1"
                >
                  Close
                </button>
              </DialogPrimitive.Close>
            </div>
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
};

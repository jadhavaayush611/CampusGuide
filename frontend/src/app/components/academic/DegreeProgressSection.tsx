import React, { useState } from 'react';
import { Award, CheckCircle2, ChevronDown, ChevronUp } from 'lucide-react';
import { DegreePlan } from '../../../models/planner.model';

interface DegreeProgressSectionProps {
  degreePlan?: DegreePlan;
  isLoading: boolean;
}

export const DegreeProgressSection: React.FC<DegreeProgressSectionProps> = ({
  degreePlan,
  isLoading,
}) => {
  const [expandedTerm, setExpandedTerm] = useState<string | null>(
    degreePlan?.plannedTerms[0]?.termName || null
  );

  if (isLoading) {
    return (
      <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8 animate-pulse space-y-4">
        <div className="h-6 bg-gray-200 rounded w-1/3"></div>
        <div className="h-24 bg-gray-100 rounded-xl"></div>
        <div className="grid grid-cols-2 gap-4">
          <div className="h-32 bg-gray-100 rounded-xl"></div>
          <div className="h-32 bg-gray-100 rounded-xl"></div>
        </div>
      </div>
    );
  }

  const completed = degreePlan?.completedCredits || 78;
  const total = degreePlan?.totalRequiredCredits || 120;
  const remaining = total - completed;
  const percentage = Math.round((completed / total) * 100);

  const breakdown = degreePlan?.curriculumBreakdown || [
    { category: 'Core Major Requirements', completedCredits: 45, requiredCredits: 60 },
    { category: 'General Education', completedCredits: 21, requiredCredits: 30 },
    { category: 'Technical Electives', completedCredits: 9, requiredCredits: 18 },
    { category: 'Capstone & Practicum', completedCredits: 3, requiredCredits: 12 },
  ];

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <Award className="w-5 h-5 text-purple-600" />
            Degree Progress & Graduation Audit
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Degree requirements, total credit trajectory, and curriculum breakdown.
          </p>
        </div>
        <div className="bg-purple-50 border border-purple-200 px-3.5 py-1.5 rounded-xl text-right self-start sm:self-auto">
          <span className="text-[11px] text-purple-600 font-medium block uppercase tracking-wider">Program</span>
          <span className="text-xs font-extrabold text-purple-950">
            {degreePlan?.programName || 'B.S. Computer Science'}
          </span>
        </div>
      </div>

      {/* Graduation Progress Bar Card */}
      <div className="bg-gradient-to-r from-purple-900 to-indigo-900 text-white rounded-2xl p-6 mb-8 shadow-md">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
          <div>
            <span className="text-xs uppercase tracking-wider text-purple-200 font-semibold">
              Graduation Progress
            </span>
            <h3 className="text-2xl font-black mt-1">{percentage}% Complete</h3>
            <p className="text-xs text-purple-200 mt-0.5">
              {completed} of {total} required credits completed • {remaining} credits remaining
            </p>
          </div>
          <div className="flex items-center gap-4 bg-white/10 backdrop-blur-md px-4 py-2.5 rounded-xl border border-white/20">
            <div>
              <span className="text-[10px] text-purple-200 font-medium block">Current GPA</span>
              <span className="text-lg font-extrabold">{degreePlan?.gpa ? degreePlan.gpa.toFixed(2) : '3.78'}</span>
            </div>
            <div className="w-px h-8 bg-white/20"></div>
            <div>
              <span className="text-[10px] text-purple-200 font-medium block">Est. Graduation</span>
              <span className="text-sm font-bold">Spring 2027</span>
            </div>
          </div>
        </div>

        {/* Progress Bar Track */}
        <div className="w-full bg-white/20 rounded-full h-3.5 overflow-hidden p-0.5">
          <div
            className="bg-gradient-to-r from-emerald-400 to-teal-300 h-full rounded-full transition-all duration-500"
            style={{ width: `${percentage}%` }}
          ></div>
        </div>
      </div>

      {/* Curriculum Category Breakdown */}
      <div className="mb-8">
        <h3 className="text-sm font-bold text-gray-900 mb-4 uppercase tracking-wider">
          Curriculum Category Breakdown
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {breakdown.map((item, idx) => {
            const catPercent = Math.min(100, Math.round((item.completedCredits / item.requiredCredits) * 100));
            return (
              <div key={idx} className="bg-gray-50 border border-gray-200/80 rounded-xl p-4">
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="text-xs font-bold text-gray-900">{item.category}</span>
                  <span className="text-xs font-bold text-purple-700">
                    {item.completedCredits}/{item.requiredCredits} Cr ({catPercent}%)
                  </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-purple-600 h-full rounded-full transition-all"
                    style={{ width: `${catPercent}%` }}
                  ></div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Terms Curriculum Roadmap Accordion */}
      {degreePlan?.plannedTerms && degreePlan.plannedTerms.length > 0 && (
        <div>
          <h3 className="text-sm font-bold text-gray-900 mb-4 uppercase tracking-wider">
            Planned & Completed Semester Roadmap
          </h3>
          <div className="space-y-3">
            {degreePlan.plannedTerms.map((term, idx) => {
              const isExpanded = expandedTerm === term.termName || (expandedTerm === null && idx === 0);
              return (
                <div key={idx} className="border border-gray-200 rounded-xl overflow-hidden bg-white">
                  <button
                    onClick={() => setExpandedTerm(isExpanded ? null : term.termName)}
                    className="w-full px-5 py-4 flex items-center justify-between bg-gray-50/80 hover:bg-gray-100/80 transition-colors text-left"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-purple-100 text-purple-700 flex items-center justify-center font-bold text-xs">
                        T{idx + 1}
                      </div>
                      <div>
                        <h4 className="text-sm font-bold text-gray-900">{term.termName}</h4>
                        <p className="text-xs text-gray-500">{term.courses.length} Courses Registered</p>
                      </div>
                    </div>
                    {isExpanded ? (
                      <ChevronUp className="w-4 h-4 text-gray-500" />
                    ) : (
                      <ChevronDown className="w-4 h-4 text-gray-500" />
                    )}
                  </button>

                  {isExpanded && (
                    <div className="p-4 border-t border-gray-200 divide-y divide-gray-100">
                      {term.courses.map((course) => (
                        <div key={course.id} className="py-2.5 flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <span className="px-2 py-0.5 bg-blue-100 text-blue-800 font-bold text-[10px] rounded">
                              {course.code}
                            </span>
                            <div>
                              <span className="text-xs font-bold text-gray-900 block">{course.title}</span>
                              <span className="text-[11px] text-gray-500">{course.department}</span>
                            </div>
                          </div>
                          <div className="flex items-center gap-3">
                            <span className="text-xs text-gray-600 font-semibold">{course.credits} Cr</span>
                            {course.status === 'COMPLETED' && (
                              <span className="text-xs font-bold text-emerald-600 flex items-center gap-1">
                                <CheckCircle2 className="w-3.5 h-3.5" /> Grade {course.grade || 'A'}
                              </span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

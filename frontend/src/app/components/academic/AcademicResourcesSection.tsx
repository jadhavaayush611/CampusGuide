import React, { useState } from 'react';
import { FileText, Download, Search, BookOpen, ExternalLink, Library, Users, Folder } from 'lucide-react';
import { useResources } from '../../../hooks/campus/useResources';

export const AcademicResourcesSection: React.FC = () => {
  const [query, setQuery] = useState('');
  const { data: resources = [], isLoading } = useResources(query);

  const fallbackAcademicResources = [
    {
      id: 'res-1',
      title: 'CS-301 Algorithms Complete Lecture Notes & Problem Sets',
      category: 'Course Materials',
      author: 'Dr. Ramesh Kumar',
      downloads: 142,
      type: 'PDF',
      url: 'https://campusguide.edu/resources/cs301-notes.pdf',
    },
    {
      id: 'res-2',
      title: 'Database Systems Midterm Sample Papers (2023 - 2025)',
      category: 'Previous Papers',
      author: 'Prof. Ananya Sharma',
      downloads: 215,
      type: 'PDF',
      url: 'https://campusguide.edu/resources/dbms-past-papers.pdf',
    },
    {
      id: 'res-3',
      title: 'Operating Systems Linux Kernel Lab Instructions & Manual',
      category: 'Lab Manuals',
      author: 'Dr. Suresh Mehta',
      downloads: 98,
      type: 'PDF',
      url: 'https://campusguide.edu/resources/os-lab-manual.pdf',
    },
    {
      id: 'res-4',
      title: 'Department of Computer Science & Engineering Syllabi Handbook',
      category: 'Department Syllabi',
      author: 'Dean of Academic Affairs',
      downloads: 380,
      type: 'PDF',
      url: 'https://campusguide.edu/resources/cse-syllabus.pdf',
    },
  ];

  const displayResources = resources.length > 0 ? resources : fallbackAcademicResources;

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm mb-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <Library className="w-5 h-5 text-blue-600" />
            Academic Resources, Syllabi & Materials
          </h2>
          <p className="text-xs text-gray-500 mt-0.5">
            Quick access to course lecture notes, syllabi, past papers, and department repositories.
          </p>
        </div>

        {/* Quick links header */}
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <a
            href="/resources?category=Academic"
            className="px-3.5 py-1.5 bg-blue-600 text-white hover:bg-blue-700 rounded-xl text-xs font-bold flex items-center gap-1.5 transition-colors shadow-xs"
          >
            <Library className="w-3.5 h-3.5" /> Open in Resource Center
          </a>
          <a
            href="https://campusguide.edu/faculty"
            target="_blank"
            rel="noopener noreferrer"
            className="px-3 py-1.5 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-colors"
          >
            <Users className="w-3.5 h-3.5" /> Faculty Directory
          </a>
        </div>
      </div>

      {/* Search Input */}
      <div className="relative mb-6">
        <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          placeholder="Search academic notes, syllabi, past papers..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-300 rounded-xl text-xs text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
      </div>

      {/* Resource Quick Categories */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
        <div className="bg-blue-50/60 border border-blue-100 p-3.5 rounded-xl flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-blue-100 text-blue-700 flex items-center justify-center font-bold">
            <BookOpen className="w-4 h-4" />
          </div>
          <div>
            <span className="text-xs font-bold text-blue-950 block">Lecture Notes</span>
            <span className="text-[10px] text-blue-600">Updated weekly</span>
          </div>
        </div>

        <div className="bg-purple-50/60 border border-purple-100 p-3.5 rounded-xl flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-purple-100 text-purple-700 flex items-center justify-center font-bold">
            <FileText className="w-4 h-4" />
          </div>
          <div>
            <span className="text-xs font-bold text-purple-950 block">Past Papers</span>
            <span className="text-[10px] text-purple-600">Exams 2021-2025</span>
          </div>
        </div>

        <div className="bg-emerald-50/60 border border-emerald-100 p-3.5 rounded-xl flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold">
            <Folder className="w-4 h-4" />
          </div>
          <div>
            <span className="text-xs font-bold text-emerald-950 block">Syllabi</span>
            <span className="text-[10px] text-emerald-600">All Departments</span>
          </div>
        </div>

        <div className="bg-amber-50/60 border border-amber-100 p-3.5 rounded-xl flex items-center gap-3">
          <div className="w-9 h-9 rounded-lg bg-amber-100 text-amber-700 flex items-center justify-center font-bold">
            <Users className="w-4 h-4" />
          </div>
          <div>
            <span className="text-xs font-bold text-amber-950 block">Faculty Info</span>
            <span className="text-[10px] text-amber-700">Office hours & contacts</span>
          </div>
        </div>
      </div>

      {/* Resources Table List */}
      {isLoading ? (
        <div className="space-y-3 animate-pulse">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-16 bg-gray-100 rounded-xl border border-gray-200"></div>
          ))}
        </div>
      ) : (
        <div className="space-y-2.5">
          {displayResources.map((item: any) => (
            <div
              key={item.id}
              className="bg-white border border-gray-200 rounded-xl p-4 flex items-center justify-between gap-4 hover:border-blue-300 transition-colors"
            >
              <div className="flex items-center gap-3.5">
                <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0" aria-hidden="true">
                  <FileText className="w-5 h-5" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-gray-900">{item.title || item.name}</h4>
                  <p className="text-[11px] text-gray-500 mt-0.5">
                    {item.category || 'Academic Resource'} • {item.author || item.uploadedBy || 'Faculty'}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-3 flex-shrink-0">
                {item.downloads && (
                  <span className="text-[11px] text-gray-500 font-medium hidden sm:inline">
                    {item.downloads} downloads
                  </span>
                )}
                <a
                  href={item.url || '#'}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  title="Download / View Resource"
                  aria-label={`Download or view resource: ${item.title || item.name}`}
                >
                  <Download className="w-4 h-4" aria-hidden="true" />
                </a>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

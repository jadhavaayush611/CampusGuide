import React from 'react';
import { ResourceCategory } from '../../../models/resource.model';
import { Search, Filter, ArrowUpDown, LayoutGrid, List, X } from 'lucide-react';

interface ResourceFilterBarProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  selectedCategory: string;
  onCategoryChange: (category: string) => void;
  selectedFileType: string;
  onFileTypeChange: (fileType: string) => void;
  sortBy: 'newest' | 'popular' | 'title' | 'size';
  onSortChange: (sort: 'newest' | 'popular' | 'title' | 'size') => void;
  viewMode: 'grid' | 'list';
  onViewModeChange: (mode: 'grid' | 'list') => void;
}

const CATEGORIES: Array<{ id: string; label: string }> = [
  { id: 'All', label: 'All Categories' },
  { id: 'Lecture Notes', label: 'Lecture Notes' },
  { id: 'Lab Manuals', label: 'Lab Manuals' },
  { id: 'Past Papers', label: 'Past Papers' },
  { id: 'Syllabi', label: 'Syllabi' },
  { id: 'Forms', label: 'Forms' },
  { id: 'Templates', label: 'Templates' },
  { id: 'Handbooks', label: 'Handbooks' },
  { id: 'Policies', label: 'Policies' },
  { id: 'Miscellaneous', label: 'Miscellaneous' },
];

const FILE_TYPES = [
  { id: 'All', label: 'All File Formats' },
  { id: 'pdf', label: 'PDF Documents' },
  { id: 'docx', label: 'Word Documents' },
  { id: 'zip', label: 'Archives (ZIP/RAR)' },
  { id: 'png', label: 'Images' },
  { id: 'pptx', label: 'Presentations' },
  { id: 'mp4', label: 'Videos' },
];

export function ResourceFilterBar({
  searchQuery,
  onSearchChange,
  selectedCategory,
  onCategoryChange,
  selectedFileType,
  onFileTypeChange,
  sortBy,
  onSortChange,
  viewMode,
  onViewModeChange,
}: ResourceFilterBarProps) {
  return (
    <div className="space-y-4 mb-6">
      {/* Category Pills horizontal scroll */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        {CATEGORIES.map((cat) => {
          const isActive = selectedCategory === cat.id;
          return (
            <button
              key={cat.id}
              onClick={() => onCategoryChange(cat.id)}
              className={`px-4 py-2 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                isActive
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50 hover:text-gray-900'
              }`}
            >
              {cat.label}
            </button>
          );
        })}
      </div>

      {/* Control bar: Search, File Type, Sort, View mode */}
      <div className="bg-white rounded-2xl border border-gray-200 p-4 shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Search input */}
        <div className="relative flex-1 w-full">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search by title, topic, tag, uploader, or filename..."
            className="w-full pl-10 pr-9 py-2 rounded-xl border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600"
          />
          {searchQuery && (
            <button
              onClick={() => onSearchChange('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 p-1"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Dropdowns & View toggles */}
        <div className="flex items-center gap-3 w-full md:w-auto justify-between md:justify-end">
          {/* File Type Filter */}
          <div className="relative">
            <select
              value={selectedFileType}
              onChange={(e) => onFileTypeChange(e.target.value)}
              className="pl-3 pr-8 py-2 rounded-xl border border-gray-200 text-xs font-medium text-gray-700 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 cursor-pointer"
            >
              {FILE_TYPES.map((ft) => (
                <option key={ft.id} value={ft.id}>
                  {ft.label}
                </option>
              ))}
            </select>
          </div>

          {/* Sort By */}
          <div className="relative">
            <select
              value={sortBy}
              onChange={(e) => onSortChange(e.target.value as any)}
              className="pl-3 pr-8 py-2 rounded-xl border border-gray-200 text-xs font-medium text-gray-700 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 cursor-pointer"
            >
              <option value="newest">Sort: Newest First</option>
              <option value="popular">Sort: Most Popular</option>
              <option value="title">Sort: Title (A-Z)</option>
              <option value="size">Sort: File Size</option>
            </select>
          </div>

          {/* View mode toggle */}
          <div className="flex items-center border border-gray-200 rounded-xl p-1 bg-gray-50">
            <button
              onClick={() => onViewModeChange('grid')}
              className={`p-1.5 rounded-lg transition-colors ${
                viewMode === 'grid' ? 'bg-white text-blue-600 shadow-xs font-bold' : 'text-gray-400 hover:text-gray-600'
              }`}
              title="Grid View"
            >
              <LayoutGrid className="w-4 h-4" />
            </button>
            <button
              onClick={() => onViewModeChange('list')}
              className={`p-1.5 rounded-lg transition-colors ${
                viewMode === 'list' ? 'bg-white text-blue-600 shadow-xs font-bold' : 'text-gray-400 hover:text-gray-600'
              }`}
              title="List View"
            >
              <List className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

import { Search, Filter, X, LayoutGrid, List, Plus, Pin, AlertCircle, Archive, CheckCircle2 } from 'lucide-react';
import { NoticeCategory, NoticePriority } from '../../../models/notice.model';

export type NoticeTab = 'ALL' | 'PINNED' | 'IMPORTANT' | 'UNREAD' | 'ARCHIVED';

interface NoticeFiltersProps {
  searchQuery: string;
  onSearchChange: (q: string) => void;
  selectedCategory: NoticeCategory | 'ALL';
  onCategoryChange: (cat: NoticeCategory | 'ALL') => void;
  selectedPriority: NoticePriority | 'ALL';
  onPriorityChange: (p: NoticePriority | 'ALL') => void;
  activeTab: NoticeTab;
  onTabChange: (tab: NoticeTab) => void;
  sortBy: 'publishedAt' | 'priority' | 'title';
  onSortByChange: (sort: 'publishedAt' | 'priority' | 'title') => void;
  viewMode: 'grid' | 'list';
  onViewModeChange: (mode: 'grid' | 'list') => void;
  unreadCount?: number;
  onCreateNotice?: () => void;
}

const CATEGORIES: (NoticeCategory | 'ALL')[] = [
  'ALL',
  'Academic',
  'Administrative',
  'Examination',
  'Events',
  'Councils',
  'Placements',
  'Scholarships',
  'General',
];

export function NoticeFilters({
  searchQuery,
  onSearchChange,
  selectedCategory,
  onCategoryChange,
  selectedPriority,
  onPriorityChange,
  activeTab,
  onTabChange,
  sortBy,
  onSortByChange,
  viewMode,
  onViewModeChange,
  unreadCount = 0,
  onCreateNotice,
}: NoticeFiltersProps) {
  return (
    <div className="space-y-6 mb-8">
      {/* Navigation Tabs Bar */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-gray-200 pb-3">
        <div className="flex items-center gap-1 sm:gap-2 overflow-x-auto no-scrollbar">
          <button
            onClick={() => onTabChange('ALL')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-2 whitespace-nowrap ${
              activeTab === 'ALL'
                ? 'bg-[#2563EB] text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            All Notices
          </button>

          <button
            onClick={() => onTabChange('PINNED')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 whitespace-nowrap ${
              activeTab === 'PINNED'
                ? 'bg-amber-500 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            <Pin className="w-3.5 h-3.5 fill-current" />
            Pinned
          </button>

          <button
            onClick={() => onTabChange('IMPORTANT')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 whitespace-nowrap ${
              activeTab === 'IMPORTANT'
                ? 'bg-red-600 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            <AlertCircle className="w-3.5 h-3.5" />
            Important
          </button>

          <button
            onClick={() => onTabChange('UNREAD')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 whitespace-nowrap ${
              activeTab === 'UNREAD'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            <CheckCircle2 className="w-3.5 h-3.5" />
            Unread
            {unreadCount > 0 && (
              <span className="ml-1 px-1.5 py-0.5 bg-white text-blue-700 rounded-full text-[10px] font-bold">
                {unreadCount}
              </span>
            )}
          </button>

          <button
            onClick={() => onTabChange('ARCHIVED')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 whitespace-nowrap ${
              activeTab === 'ARCHIVED'
                ? 'bg-gray-800 text-white shadow-sm'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            }`}
          >
            <Archive className="w-3.5 h-3.5" />
            Archived / Expired
          </button>
        </div>

        {/* Create Notice Trigger & View Controls */}
        <div className="flex items-center gap-3 ml-auto">
          {onCreateNotice && (
            <button
              onClick={onCreateNotice}
              className="inline-flex items-center gap-2 px-4 py-2 bg-[#2563EB] hover:bg-blue-700 text-white font-semibold text-xs rounded-xl transition-all shadow-sm active:scale-95 whitespace-nowrap"
            >
              <Plus className="w-4 h-4" />
              Publish Notice
            </button>
          )}

          {/* View Mode Toggle */}
          <div className="flex items-center bg-gray-100 p-1 rounded-xl border border-gray-200">
            <button
              onClick={() => onViewModeChange('grid')}
              title="Grid View"
              className={`p-1.5 rounded-lg transition-all ${
                viewMode === 'grid' ? 'bg-white text-[#2563EB] shadow-2xs' : 'text-gray-500 hover:text-gray-800'
              }`}
            >
              <LayoutGrid className="w-4 h-4" />
            </button>
            <button
              onClick={() => onViewModeChange('list')}
              title="List View"
              className={`p-1.5 rounded-lg transition-all ${
                viewMode === 'list' ? 'bg-white text-[#2563EB] shadow-2xs' : 'text-gray-500 hover:text-gray-800'
              }`}
            >
              <List className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Filter Controls Bar */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-center">
        {/* Search Input */}
        <div className="md:col-span-5 relative">
          <Search className="w-4 h-4 text-gray-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Search notices by title, content, publisher, tags..."
            className="w-full pl-10 pr-9 py-2.5 bg-white border border-gray-200 rounded-xl text-xs text-gray-800 focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all shadow-2xs"
          />
          {searchQuery && (
            <button
              onClick={() => onSearchChange('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Category Pills Selector */}
        <div className="md:col-span-5 flex items-center gap-2 overflow-x-auto no-scrollbar py-1">
          <Filter className="w-4 h-4 text-gray-400 shrink-0" />
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => onCategoryChange(cat)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition-all border ${
                selectedCategory === cat
                  ? 'bg-blue-50 text-[#2563EB] border-blue-200 shadow-2xs'
                  : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'
              }`}
            >
              {cat === 'ALL' ? 'All Categories' : cat}
            </button>
          ))}
        </div>

        {/* Priority & Sorting */}
        <div className="md:col-span-2 flex items-center justify-end gap-2">
          <select
            value={sortBy}
            onChange={(e) => onSortByChange(e.target.value as any)}
            className="w-full px-3 py-2 bg-white border border-gray-200 rounded-xl text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#2563EB] shadow-2xs"
          >
            <option value="publishedAt">Sort: Newest First</option>
            <option value="priority">Sort: Highest Priority</option>
            <option value="title">Sort: Title A-Z</option>
          </select>
        </div>
      </div>
    </div>
  );
}

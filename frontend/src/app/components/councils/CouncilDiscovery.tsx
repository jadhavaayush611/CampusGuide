import { useState } from 'react';
import { Search, Filter, ArrowUpDown, ShieldAlert, Sparkles, Activity, CheckCircle2 } from 'lucide-react';
import { Council, CouncilCategory } from '../../../models/council.model';
import { CouncilCard } from './CouncilCard';
import { CouncilCardSkeleton } from './CouncilSkeletons';
import {
  useCouncils,
  useFeaturedCouncils,
  useRecentlyActiveCouncils,
  useJoinedCouncils,
} from '../../../hooks';

type ViewMode = 'all' | 'featured' | 'recentlyActive' | 'joined';

const CATEGORIES: (CouncilCategory | 'All')[] = [
  'All',
  'Technical',
  'Cultural',
  'Sports',
  'Entrepreneurship',
  'Career',
  'Governing',
];

export function CouncilDiscovery() {
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState<CouncilCategory | 'All'>('All');
  const [sort, setSort] = useState<'members' | 'activity' | 'name' | 'newest'>('members');
  const [viewMode, setViewMode] = useState<ViewMode>('all');
  const [page, setPage] = useState(1);

  // Queries
  const councilsQuery = useCouncils({ search, category, sort, page, limit: 8 });
  const featuredQuery = useFeaturedCouncils();
  const recentlyActiveQuery = useRecentlyActiveCouncils();
  const joinedQuery = useJoinedCouncils();

  const getActiveList = (): { councils: Council[]; total: number; totalPages: number; isLoading: boolean; isError: boolean } => {
    if (viewMode === 'featured') {
      return {
        councils: featuredQuery.data || [],
        total: featuredQuery.data?.length || 0,
        totalPages: 1,
        isLoading: featuredQuery.isLoading,
        isError: featuredQuery.isError,
      };
    }
    if (viewMode === 'recentlyActive') {
      return {
        councils: recentlyActiveQuery.data || [],
        total: recentlyActiveQuery.data?.length || 0,
        totalPages: 1,
        isLoading: recentlyActiveQuery.isLoading,
        isError: recentlyActiveQuery.isError,
      };
    }
    if (viewMode === 'joined') {
      return {
        councils: joinedQuery.data || [],
        total: joinedQuery.data?.length || 0,
        totalPages: 1,
        isLoading: joinedQuery.isLoading,
        isError: joinedQuery.isError,
      };
    }

    return {
      councils: councilsQuery.data?.councils || [],
      total: councilsQuery.data?.total || 0,
      totalPages: councilsQuery.data?.totalPages || 1,
      isLoading: councilsQuery.isLoading,
      isError: councilsQuery.isError,
    };
  };

  const { councils, totalPages, isLoading, isError } = getActiveList();

  return (
    <div className="space-y-8">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-semibold text-gray-900 mb-1">Campus Councils & Governing Bodies</h1>
          <p className="text-gray-600">
            Explore official student councils, technical societies, and constitutional governing boards.
          </p>
        </div>
      </div>

      {/* Search & Sort Controls */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
        {/* Search Input */}
        <div className="relative flex-1 max-w-xl">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            placeholder="Search councils by name, category, tags..."
            className="w-full pl-12 pr-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all shadow-xs"
          />
        </div>

        {/* Sort Select */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-white border border-gray-200 rounded-xl px-4 py-3 shadow-xs">
            <ArrowUpDown className="w-4 h-4 text-gray-400" />
            <span className="text-xs font-medium text-gray-500 uppercase">Sort by:</span>
            <select
              value={sort}
              onChange={(e) => setSort(e.target.value as any)}
              className="bg-transparent text-sm font-medium text-gray-800 focus:outline-none cursor-pointer"
            >
              <option value="members">Most Members</option>
              <option value="activity">Active Events</option>
              <option value="name">Alphabetical</option>
              <option value="newest">Recently Formed</option>
            </select>
          </div>
        </div>
      </div>

      {/* Category Pills */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        <span className="text-xs font-semibold text-gray-400 uppercase tracking-wider mr-1 flex items-center gap-1">
          <Filter className="w-3.5 h-3.5" />
          Category:
        </span>
        {CATEGORIES.map((cat) => (
          <button
            key={cat}
            onClick={() => {
              setCategory(cat);
              setPage(1);
            }}
            className={`px-4 py-2 text-xs font-medium rounded-xl transition-all whitespace-nowrap ${
              category === cat
                ? 'bg-[#2563EB] text-white shadow-xs'
                : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* View Mode Tabs */}
      <div className="flex items-center gap-6 border-b border-gray-200">
        <button
          onClick={() => {
            setViewMode('all');
            setPage(1);
          }}
          className={`pb-3 px-1 text-sm font-medium transition-colors relative ${
            viewMode === 'all' ? 'text-[#2563EB]' : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          All Councils
          {viewMode === 'all' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]" />}
        </button>

        <button
          onClick={() => setViewMode('featured')}
          className={`pb-3 px-1 text-sm font-medium transition-colors relative flex items-center gap-1.5 ${
            viewMode === 'featured' ? 'text-[#2563EB]' : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <Sparkles className="w-4 h-4 text-amber-500" />
          Featured
          {viewMode === 'featured' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]" />}
        </button>

        <button
          onClick={() => setViewMode('recentlyActive')}
          className={`pb-3 px-1 text-sm font-medium transition-colors relative flex items-center gap-1.5 ${
            viewMode === 'recentlyActive' ? 'text-[#2563EB]' : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <Activity className="w-4 h-4 text-emerald-500" />
          Recently Active
          {viewMode === 'recentlyActive' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]" />}
        </button>

        <button
          onClick={() => setViewMode('joined')}
          className={`pb-3 px-1 text-sm font-medium transition-colors relative flex items-center gap-1.5 ${
            viewMode === 'joined' ? 'text-[#2563EB]' : 'text-gray-600 hover:text-gray-900'
          }`}
        >
          <CheckCircle2 className="w-4 h-4 text-blue-500" />
          My Councils ({joinedQuery.data?.length || 0})
          {viewMode === 'joined' && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]" />}
        </button>
      </div>

      {/* Grid Content */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[1, 2, 3, 4].map((n) => (
            <CouncilCardSkeleton key={n} />
          ))}
        </div>
      ) : isError ? (
        <div className="bg-red-50 border border-red-200 rounded-xl p-8 text-center max-w-md mx-auto">
          <ShieldAlert className="w-10 h-10 text-red-500 mx-auto mb-3" />
          <h3 className="text-lg font-semibold text-red-900 mb-1">Failed to load councils</h3>
          <p className="text-sm text-red-600 mb-4">An error occurred while communicating with backend services.</p>
          <button
            onClick={() => councilsQuery.refetch()}
            className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700 transition-colors"
          >
            Retry Fetching
          </button>
        </div>
      ) : councils.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-lg mx-auto">
          <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">
            🏛️
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-1">No councils found</h3>
          <p className="text-sm text-gray-600 mb-4">
            {search || category !== 'All'
              ? 'Try adjusting your search keywords or category filters.'
              : 'You have not joined any councils yet.'}
          </p>
          {(search || category !== 'All') && (
            <button
              onClick={() => {
                setSearch('');
                setCategory('All');
              }}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm hover:bg-gray-200 transition-colors"
            >
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {councils.map((council) => (
            <CouncilCard key={council.id} council={council} />
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {viewMode === 'all' && totalPages > 1 && (
        <div className="flex items-center justify-between pt-6 border-t border-gray-200">
          <button
            disabled={page === 1}
            onClick={() => setPage((p) => Math.max(1, p - 1))}
            className="px-4 py-2 text-sm border border-gray-200 bg-white rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Previous
          </button>

          <div className="flex items-center gap-2">
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
              <button
                key={p}
                onClick={() => setPage(p)}
                className={`w-9 h-9 text-sm font-medium rounded-lg transition-colors ${
                  page === p ? 'bg-[#2563EB] text-white' : 'bg-white border border-gray-200 text-gray-700 hover:bg-gray-50'
                }`}
              >
                {p}
              </button>
            ))}
          </div>

          <button
            disabled={page === totalPages}
            onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
            className="px-4 py-2 text-sm border border-gray-200 bg-white rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}

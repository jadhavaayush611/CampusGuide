import React, { useState } from 'react';
import { Search, Filter, SlidersHorizontal, Sparkles, TrendingUp, Compass, Users } from 'lucide-react';
import { useCommunities } from '../../../hooks/community/useCommunities';
import { useFeaturedCommunities, useTrendingCommunities } from '../../../hooks/community/useFeaturedCommunities';
import { useJoinedCommunities } from '../../../hooks/community/useJoinedCommunities';
import { Community } from '../../../models/community.model';
import { CommunityCard } from './CommunityCard';
import { CommunityCardSkeleton } from './CommunitySkeletons';

interface CommunityDiscoveryProps {
  onSelectCommunity: (community: Community) => void;
  onCreateCommunity?: () => void;
}

const CATEGORIES = [
  'All',
  'Academic',
  'Technology',
  'Creative',
  'Cultural',
  'Sports',
  'Professional',
  'Social',
];

export const CommunityDiscovery: React.FC<CommunityDiscoveryProps> = ({
  onSelectCommunity,
  onCreateCommunity,
}) => {
  const [activeTab, setActiveTab] = useState<'all' | 'featured' | 'trending' | 'joined'>('all');
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [sortBy, setSortBy] = useState<'members' | 'activity' | 'name' | 'newest'>('members');
  const [page, setPage] = useState<number>(1);

  // Queries
  const allCommunitiesQuery = useCommunities({
    search: searchQuery,
    category: selectedCategory === 'All' ? undefined : selectedCategory,
    sort: sortBy,
    page,
    limit: 12,
  });

  const featuredQuery = useFeaturedCommunities();
  const trendingQuery = useTrendingCommunities();
  const joinedQuery = useJoinedCommunities();

  const getDisplayedCommunities = (): { communities: Community[]; isLoading: boolean; totalPages?: number } => {
    if (activeTab === 'featured') {
      return { communities: featuredQuery.data || [], isLoading: featuredQuery.isLoading };
    }
    if (activeTab === 'trending') {
      return { communities: trendingQuery.data || [], isLoading: trendingQuery.isLoading };
    }
    if (activeTab === 'joined') {
      return { communities: joinedQuery.data || [], isLoading: joinedQuery.isLoading };
    }
    return {
      communities: allCommunitiesQuery.data?.communities || [],
      isLoading: allCommunitiesQuery.isLoading,
      totalPages: allCommunitiesQuery.data?.totalPages || 1,
    };
  };

  const { communities, isLoading, totalPages } = getDisplayedCommunities();

  return (
    <div className="space-y-8">
      {/* Top Search & Filter Bar */}
      <div className="bg-white rounded-3xl p-6 border border-gray-200 shadow-sm space-y-6">
        <div className="flex flex-col md:flex-row items-center justify-between gap-4">
          {/* Search bar */}
          <div className="relative flex-1 w-full">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search by community name, description, tags, or topic..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(1);
              }}
              className="w-full pl-12 pr-4 py-3 bg-gray-50 border border-gray-200 rounded-2xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600 focus:bg-white transition-all"
            />
          </div>

          {/* Sort dropdown & Create CTA */}
          <div className="flex items-center gap-3 w-full md:w-auto">
            <div className="flex items-center gap-2 bg-gray-50 border border-gray-200 px-3.5 py-2.5 rounded-2xl text-xs font-semibold text-gray-700 w-full md:w-auto">
              <SlidersHorizontal className="w-4 h-4 text-gray-400" />
              <span>Sort:</span>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value as any)}
                className="bg-transparent font-bold text-gray-900 focus:outline-none cursor-pointer"
              >
                <option value="members">Most Members</option>
                <option value="activity">Most Active</option>
                <option value="name">Alphabetical</option>
                <option value="newest">Newest</option>
              </select>
            </div>

            {onCreateCommunity && (
              <button
                onClick={onCreateCommunity}
                className="px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-2xl text-xs font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-1.5 whitespace-nowrap"
              >
                Create Community
              </button>
            )}
          </div>
        </div>

        {/* Discovery View Tabs (All, Featured, Trending, Joined) */}
        <div className="flex items-center border-b border-gray-100 gap-6 overflow-x-auto">
          <button
            onClick={() => {
              setActiveTab('all');
              setPage(1);
            }}
            className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'all'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Compass className="w-4 h-4" /> All Communities
          </button>

          <button
            onClick={() => setActiveTab('featured')}
            className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'featured'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Sparkles className="w-4 h-4 text-amber-500" /> Featured
          </button>

          <button
            onClick={() => setActiveTab('trending')}
            className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'trending'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <TrendingUp className="w-4 h-4 text-emerald-500" /> Trending
          </button>

          <button
            onClick={() => setActiveTab('joined')}
            className={`pb-3 text-sm font-bold flex items-center gap-2 border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'joined'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Users className="w-4 h-4 text-indigo-500" /> My Joined Communities
          </button>
        </div>

        {/* Category Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          <Filter className="w-4 h-4 text-gray-400 mr-1 flex-shrink-0" />
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => {
                setSelectedCategory(cat);
                setPage(1);
              }}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid Content */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <CommunityCardSkeleton key={n} />
          ))}
        </div>
      ) : communities.length === 0 ? (
        <div className="bg-white rounded-3xl border border-gray-200 p-12 text-center space-y-4 shadow-sm">
          <Compass className="w-16 h-16 text-gray-300 mx-auto" />
          <h3 className="text-xl font-bold text-gray-900">No communities found</h3>
          <p className="text-sm text-gray-500 max-w-md mx-auto">
            We couldn't find any communities matching your search criteria or category filter.
          </p>
          <button
            onClick={() => {
              setSearchQuery('');
              setSelectedCategory('All');
              setActiveTab('all');
            }}
            className="px-5 py-2.5 bg-blue-600 text-white rounded-xl text-xs font-bold hover:bg-blue-700 transition-colors shadow-sm"
          >
            Reset Filters
          </button>
        </div>
      ) : (
        <div className="space-y-8">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {communities.map((community) => (
              <CommunityCard
                key={community.id}
                community={community}
                onSelect={onSelectCommunity}
              />
            ))}
          </div>

          {/* Pagination Controls */}
          {activeTab === 'all' && (totalPages || 1) > 1 && (
            <div className="flex items-center justify-center gap-3 pt-4">
              <button
                disabled={page <= 1}
                onClick={() => setPage((p) => Math.max(1, p - 1))}
                className="px-4 py-2 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-700 disabled:opacity-50 hover:bg-gray-50"
              >
                Previous
              </button>
              <span className="text-xs font-semibold text-gray-600">
                Page {page} of {totalPages}
              </span>
              <button
                disabled={page >= (totalPages || 1)}
                onClick={() => setPage((p) => Math.min(totalPages || 1, p + 1))}
                className="px-4 py-2 bg-white border border-gray-200 rounded-xl text-xs font-bold text-gray-700 disabled:opacity-50 hover:bg-gray-50"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

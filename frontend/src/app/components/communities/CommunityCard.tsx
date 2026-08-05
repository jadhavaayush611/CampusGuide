import React, { memo, useCallback, useMemo } from 'react';
import { Users, TrendingUp, Check, Plus, Loader2 } from 'lucide-react';
import { Community } from '../../../models/community.model';
import { useCommunityMembership } from '../../../hooks/community/useCommunityMembership';

interface CommunityCardProps {
  community: Community;
  onSelect?: (community: Community) => void;
}

const CATEGORY_COLORS: Record<string, string> = {
  Academic: 'bg-blue-50 text-blue-700 border-blue-200',
  Technology: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  Creative: 'bg-purple-50 text-purple-700 border-purple-200',
  Cultural: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  Sports: 'bg-amber-50 text-amber-700 border-amber-200',
  Professional: 'bg-sky-50 text-sky-700 border-sky-200',
  Social: 'bg-rose-50 text-rose-700 border-rose-200',
};

export const CommunityCard: React.FC<CommunityCardProps> = memo(function CommunityCard({ community, onSelect }) {
  const membershipMutation = useCommunityMembership();

  const isJoined = community.isJoined;
  const isPending = membershipMutation.isPending;

  const handleCardClick = useCallback(() => {
    onSelect?.(community);
  }, [onSelect, community]);

  const handleToggleJoin = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      membershipMutation.mutate({
        communityId: community.id,
        action: isJoined ? 'leave' : 'join',
      });
    },
    [membershipMutation, community.id, isJoined]
  );

  const badgeStyle = useMemo(
    () => CATEGORY_COLORS[community.category] || 'bg-gray-50 text-gray-700 border-gray-200',
    [community.category]
  );

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      if (e.target === e.currentTarget) {
        e.preventDefault();
        handleCardClick();
      }
    }
  }, [handleCardClick]);

  return (
    <div
      onClick={handleCardClick}
      onKeyDown={handleKeyDown}
      role="button"
      tabIndex={0}
      aria-label={`Community: ${community.name}, Category: ${community.category}, ${community.memberCount} members. ${community.description}`}
      className="group relative bg-white rounded-2xl border border-gray-200/80 shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden flex flex-col justify-between cursor-pointer hover:-translate-y-1 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2"
    >
      {/* Banner / Visual accent */}
      <div className="relative h-24 w-full bg-gradient-to-r from-blue-600 to-indigo-700 overflow-hidden">
        {community.bannerUrl ? (
          <img
            src={community.bannerUrl}
            alt=""
            loading="lazy"
            decoding="async"
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
        ) : (
          <div className="absolute inset-0 bg-gradient-to-tr from-blue-600 via-indigo-600 to-purple-600 opacity-90" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent" />

        {/* Category badge */}
        <span
          className={`absolute top-3 right-3 px-2.5 py-1 rounded-full text-xs font-semibold border backdrop-blur-md shadow-sm ${badgeStyle}`}
        >
          {community.category}
        </span>
      </div>

      {/* Body content */}
      <div className="p-5 flex-1 flex flex-col justify-between space-y-3">
        <div>
          <div className="flex items-start gap-3">
            <div className="w-12 h-12 rounded-xl bg-white border border-gray-200 shadow-md p-0.5 flex-shrink-0 -mt-8 relative z-10 overflow-hidden">
              {community.logoUrl ? (
                <img
                  src={community.logoUrl}
                  alt=""
                  loading="lazy"
                  decoding="async"
                  className="w-full h-full object-cover rounded-lg"
                />
              ) : (
                <div className="w-full h-full bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center text-white font-bold text-lg">
                  {community.name.charAt(0)}
                </div>
              )}
            </div>

            <div className="min-w-0 flex-1">
              <h3 className="font-bold text-gray-900 text-base leading-snug group-hover:text-blue-600 transition-colors line-clamp-1">
                {community.name}
              </h3>
              <div className="flex items-center gap-2 text-xs text-gray-500 mt-0.5">
                <span className="flex items-center gap-1 font-medium">
                  <Users className="w-3.5 h-3.5 text-blue-500" aria-hidden="true" />
                  {community.memberCount} members
                </span>
                {community.isTrending && (
                  <span className="flex items-center gap-1 text-emerald-600 font-semibold bg-emerald-50 px-1.5 py-0.5 rounded">
                    <TrendingUp className="w-3 h-3" aria-hidden="true" /> Trending
                  </span>
                )}
              </div>
            </div>
          </div>

          <p className="text-xs text-gray-600 mt-3 line-clamp-2 leading-relaxed">
            {community.description}
          </p>

          {/* Tags */}
          {community.tags && community.tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mt-3">
              {community.tags.slice(0, 3).map((tag, idx) => (
                <span
                  key={idx}
                  className="px-2 py-0.5 bg-gray-100 text-gray-600 rounded-md text-[11px] font-medium"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>

        {/* Footer actions */}
        <div className="pt-3 border-t border-gray-100 flex items-center justify-between">
          <span className="text-xs text-gray-500 font-medium">
            {community.activityMetrics
              ? `${community.activityMetrics.postsThisWeek} posts/wk`
              : 'Active campus group'}
          </span>

          <button
            onClick={handleToggleJoin}
            disabled={isPending}
            className={`inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all duration-200 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2 ${
              isJoined
                ? 'bg-gray-100 hover:bg-red-50 text-gray-700 hover:text-red-600 border border-gray-200'
                : 'bg-blue-600 hover:bg-blue-700 text-white shadow-sm hover:shadow'
            }`}
          >
            {isPending ? (
              <Loader2 className="w-3.5 h-3.5 animate-spin" aria-hidden="true" />
            ) : isJoined ? (
              <>
                <Check className="w-3.5 h-3.5 text-emerald-500" aria-hidden="true" />
                Joined
              </>
            ) : (
              <>
                <Plus className="w-3.5 h-3.5" aria-hidden="true" />
                Join
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
});

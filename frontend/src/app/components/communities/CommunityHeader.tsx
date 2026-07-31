import React from 'react';
import { Users, Shield, Award, Activity, Plus, Check, Loader2, Share2, Edit3 } from 'lucide-react';
import { Community } from '../../../models/community.model';
import { useCommunityMembership } from '../../../hooks/community/useCommunityMembership';
import { toast } from '../../../core/toast/useToast';

interface CommunityHeaderProps {
  community: Community;
  onEdit?: () => void;
}

export const CommunityHeader: React.FC<CommunityHeaderProps> = ({ community, onEdit }) => {
  const membershipMutation = useCommunityMembership();
  const isJoined = community.isJoined;
  const isPending = membershipMutation.isPending;

  const handleToggleJoin = () => {
    membershipMutation.mutate({
      communityId: community.id,
      action: isJoined ? 'leave' : 'join',
    });
  };

  const handleShare = () => {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(window.location.href);
      toast.success('Community link copied to clipboard!');
    }
  };

  return (
    <div className="bg-white rounded-3xl border border-gray-200 shadow-sm overflow-hidden mb-8">
      {/* Banner */}
      <div className="relative h-48 sm:h-64 w-full bg-gradient-to-r from-blue-700 via-indigo-700 to-purple-800 overflow-hidden">
        {community.bannerUrl ? (
          <img
            src={community.bannerUrl}
            alt={community.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="absolute inset-0 bg-gradient-to-tr from-blue-600 via-indigo-600 to-purple-700 opacity-90" />
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-black/20 to-transparent" />

        {/* Category & Status Overlay */}
        <div className="absolute top-4 right-4 flex items-center gap-2">
          <span className="px-3 py-1 rounded-full text-xs font-semibold bg-white/90 backdrop-blur-md text-gray-900 shadow">
            {community.category}
          </span>
          {community.isPrivate && (
            <span className="px-3 py-1 rounded-full text-xs font-semibold bg-amber-500/90 text-white shadow">
              Private
            </span>
          )}
        </div>
      </div>

      {/* Detail Content */}
      <div className="p-6 sm:p-8 pt-0 relative">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 -mt-14 mb-6">
          {/* Logo & Info */}
          <div className="flex flex-col sm:flex-row items-start sm:items-end gap-5">
            <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-2xl bg-white border-4 border-white shadow-xl p-1 relative z-10 overflow-hidden flex-shrink-0">
              {community.logoUrl ? (
                <img
                  src={community.logoUrl}
                  alt={community.name}
                  className="w-full h-full object-cover rounded-xl"
                />
              ) : (
                <div className="w-full h-full bg-gradient-to-br from-blue-600 to-indigo-700 rounded-xl flex items-center justify-center text-white font-extrabold text-3xl">
                  {community.name.charAt(0)}
                </div>
              )}
            </div>

            <div className="space-y-1">
              <div className="flex items-center gap-3 flex-wrap">
                <h1 className="text-2xl sm:text-3xl font-extrabold text-gray-900 tracking-tight">
                  {community.name}
                </h1>
                {community.myRole && community.myRole !== 'NONE' && (
                  <span className="px-2.5 py-0.5 rounded-md text-xs font-bold bg-blue-100 text-blue-700 uppercase">
                    {community.myRole}
                  </span>
                )}
              </div>
              {community.councilName && (
                <p className="text-sm font-medium text-blue-600 flex items-center gap-1">
                  <Award className="w-4 h-4" /> Part of {community.councilName}
                </p>
              )}
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex items-center gap-3">
            <button
              onClick={handleShare}
              className="p-2.5 rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
              title="Share community"
            >
              <Share2 className="w-4 h-4" />
            </button>

            {onEdit && (
              <button
                onClick={onEdit}
                className="p-2.5 rounded-xl border border-gray-200 text-gray-700 hover:bg-gray-50 transition-colors"
                title="Edit Community"
              >
                <Edit3 className="w-4 h-4" />
              </button>
            )}

            <button
              onClick={handleToggleJoin}
              disabled={isPending}
              className={`px-6 py-2.5 rounded-xl text-sm font-semibold flex items-center gap-2 transition-all shadow-md ${
                isJoined
                  ? 'bg-gray-100 hover:bg-red-50 text-gray-800 hover:text-red-600 border border-gray-200'
                  : 'bg-blue-600 hover:bg-blue-700 text-white hover:shadow-lg'
              }`}
            >
              {isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : isJoined ? (
                <>
                  <Check className="w-4 h-4 text-emerald-500" />
                  Joined
                </>
              ) : (
                <>
                  <Plus className="w-4 h-4" />
                  Join Community
                </>
              )}
            </button>
          </div>
        </div>

        {/* Description & Tags */}
        <p className="text-gray-700 text-base leading-relaxed max-w-4xl mb-6">
          {community.description}
        </p>

        {community.tags && community.tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-6">
            {community.tags.map((tag, idx) => (
              <span
                key={idx}
                className="px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-xs font-semibold"
              >
                #{tag}
              </span>
            ))}
          </div>
        )}

        {/* Activity Metrics & Administrators Bar */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-2xl bg-gray-50 border border-gray-100">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-blue-100 text-blue-700">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Members</p>
              <p className="text-base font-bold text-gray-900">{community.memberCount}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-emerald-100 text-emerald-700">
              <Activity className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Weekly Posts</p>
              <p className="text-base font-bold text-gray-900">
                {community.activityMetrics?.postsThisWeek || 12}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-purple-100 text-purple-700">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Admins</p>
              <p className="text-base font-bold text-gray-900">
                {community.administrators?.length || 1}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-amber-100 text-amber-700">
              <Award className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Engagement</p>
              <p className="text-base font-bold text-gray-900">
                {community.activityMetrics?.engagementRate || '94%'}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

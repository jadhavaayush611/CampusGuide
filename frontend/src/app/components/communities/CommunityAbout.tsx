import React from 'react';
import { Info, Tag, Calendar, Shield, Globe, Lock, UserCheck, Users, Award } from 'lucide-react';
import { Community } from '../../../models/community.model';

interface CommunityAboutProps {
  community: Community;
}

export const CommunityAbout: React.FC<CommunityAboutProps> = ({ community }) => {
  const formattedDate = community.createdAt
    ? new Date(community.createdAt).toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      })
    : 'September 2025';

  return (
    <div className="space-y-8">
      {/* Community Overview & Info */}
      <div className="bg-white rounded-3xl border border-gray-200 p-6 sm:p-8 shadow-sm space-y-6">
        <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
          <Info className="w-5 h-5 text-blue-600" />
          About {community.name}
        </h3>

        <p className="text-gray-700 leading-relaxed text-sm sm:text-base">
          {community.description}
        </p>

        {/* Info Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 pt-4 border-t border-gray-100">
          <div className="p-4 rounded-2xl bg-gray-50/80 border border-gray-100 space-y-1">
            <span className="text-xs font-semibold text-gray-500 uppercase flex items-center gap-1.5">
              <Tag className="w-3.5 h-3.5 text-blue-500" /> Category
            </span>
            <p className="text-sm font-bold text-gray-900">{community.category}</p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50/80 border border-gray-100 space-y-1">
            <span className="text-xs font-semibold text-gray-500 uppercase flex items-center gap-1.5">
              {community.isPrivate ? (
                <Lock className="w-3.5 h-3.5 text-amber-500" />
              ) : (
                <Globe className="w-3.5 h-3.5 text-emerald-500" />
              )}
              Privacy Status
            </span>
            <p className="text-sm font-bold text-gray-900">
              {community.isPrivate ? 'Private Community' : 'Public Community'}
            </p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50/80 border border-gray-100 space-y-1">
            <span className="text-xs font-semibold text-gray-500 uppercase flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-indigo-500" /> Created
            </span>
            <p className="text-sm font-bold text-gray-900">{formattedDate}</p>
          </div>

          <div className="p-4 rounded-2xl bg-gray-50/80 border border-gray-100 space-y-1">
            <span className="text-xs font-semibold text-gray-500 uppercase flex items-center gap-1.5">
              <Users className="w-3.5 h-3.5 text-purple-500" /> Members
            </span>
            <p className="text-sm font-bold text-gray-900">{community.memberCount} joined</p>
          </div>
        </div>

        {/* Tags Section */}
        {community.tags && community.tags.length > 0 && (
          <div className="space-y-2 pt-2">
            <h4 className="text-xs font-bold text-gray-500 uppercase">Tags & Interests</h4>
            <div className="flex flex-wrap gap-2">
              {community.tags.map((tag, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-semibold transition-colors"
                >
                  #{tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {community.councilName && (
          <div className="p-4 rounded-2xl bg-blue-50/60 border border-blue-100 flex items-center gap-3 text-xs text-blue-900 font-medium">
            <Award className="w-5 h-5 text-blue-600 flex-shrink-0" />
            <span>
              Affiliated with <strong>{community.councilName}</strong> council.
            </span>
          </div>
        )}
      </div>

      {/* Community Ownership / Management Section */}
      <div className="bg-white rounded-3xl border border-gray-200 p-6 sm:p-8 shadow-sm space-y-6">
        <div>
          <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
            <Shield className="w-5 h-5 text-purple-600" />
            Community Managers
          </h3>
          <p className="text-xs text-gray-500 mt-1">
            Organizers and moderators maintaining community guidelines and discussion safety.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
          {/* Administrators / Owners */}
          <div>
            <h4 className="font-bold text-xs text-gray-500 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <Shield className="w-3.5 h-3.5 text-purple-600" /> Community Administrators
            </h4>
            <div className="space-y-3">
              {(community.administrators || []).length === 0 ? (
                <p className="text-xs text-gray-400 italic">No administrators listed.</p>
              ) : (
                community.administrators?.map((admin, index) => (
                  <div
                    key={admin.id}
                    className="flex items-center space-x-3 p-3.5 bg-gray-50/80 rounded-2xl border border-gray-100"
                  >
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      {admin.name.charAt(0)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="font-bold text-sm text-gray-900 truncate">{admin.name}</p>
                      <p className="text-xs text-gray-500">
                        {index === 0 ? 'Community Owner & Admin' : 'Community Administrator'}
                        {admin.department ? ` • ${admin.department}` : ''}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Moderators */}
          <div>
            <h4 className="font-bold text-xs text-gray-500 uppercase tracking-wider mb-3 flex items-center gap-1.5">
              <UserCheck className="w-3.5 h-3.5 text-blue-600" /> Community Moderators
            </h4>
            <div className="space-y-3">
              {(community.moderators || []).length === 0 ? (
                <p className="text-xs text-gray-400 italic">No moderators listed.</p>
              ) : (
                community.moderators?.map((mod) => (
                  <div
                    key={mod.id}
                    className="flex items-center space-x-3 p-3.5 bg-gray-50/80 rounded-2xl border border-gray-100"
                  >
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-cyan-600 text-white font-bold flex items-center justify-center text-sm shadow-sm">
                      {mod.name.charAt(0)}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="font-bold text-sm text-gray-900 truncate">{mod.name}</p>
                      <p className="text-xs text-gray-500">
                        Community Moderator
                        {mod.department ? ` • ${mod.department}` : ''}
                      </p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

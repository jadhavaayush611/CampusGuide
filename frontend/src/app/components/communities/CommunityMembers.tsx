import React, { useState } from 'react';
import { Search, Shield, UserCheck, Mail, Building, Users } from 'lucide-react';
import { useCommunityMembers } from '../../../hooks/community/useCommunityMembers';
import { CommunityRole } from '../../../models/community.model';
import { CommunityMembersSkeleton } from './CommunitySkeletons';

interface CommunityMembersProps {
  communityId: string;
}

export const CommunityMembers: React.FC<CommunityMembersProps> = ({ communityId }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [roleFilter, setRoleFilter] = useState<CommunityRole | 'ALL'>('ALL');

  const { data: membersData, isLoading, isError } = useCommunityMembers(communityId, {
    query: searchQuery,
    role: roleFilter === 'ALL' ? undefined : roleFilter,
  });

  return (
    <div className="space-y-6">
      {/* Controls */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-gray-200 shadow-sm">
        {/* Search */}
        <div className="relative w-full sm:w-72">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            id="members-search"
            placeholder="Search members..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs focus:outline-none focus:ring-2 focus:ring-blue-600"
            aria-label="Search members"
          />
        </div>

        {/* Role Filters */}
        <div className="flex items-center gap-2 overflow-x-auto w-full sm:w-auto">
          {(['ALL', 'ADMIN', 'MODERATOR', 'MEMBER'] as const).map((role) => (
            <button
              key={role}
              onClick={() => setRoleFilter(role)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                roleFilter === role
                  ? 'bg-blue-600 text-white shadow-sm'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {role === 'ALL' ? 'All Roles' : role}
            </button>
          ))}
        </div>
      </div>

      {/* Members Grid */}
      {isLoading ? (
        <CommunityMembersSkeleton />
      ) : isError ? (
        <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-center text-red-700">
          <p className="font-semibold">Unable to load community members.</p>
        </div>
      ) : !membersData?.members || membersData.members.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center space-y-3">
          <Users className="w-12 h-12 text-gray-300 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900">No members found</h3>
          <p className="text-sm text-gray-500 max-w-sm mx-auto">
            Try refining your search query or filter.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {membersData.members.map((member) => (
            <div
              key={member.id}
              className="bg-white rounded-2xl border border-gray-200/80 p-5 shadow-sm hover:shadow-md transition-shadow flex items-start space-x-4"
            >
              <div className="w-12 h-12 rounded-full bg-gradient-to-tr from-blue-500 via-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-base shadow-sm flex-shrink-0">
                {member.name.charAt(0)}
              </div>

              <div className="flex-1 min-w-0 space-y-1">
                <div className="flex items-center justify-between">
                  <h4 className="font-bold text-gray-900 text-sm truncate">{member.name}</h4>
                  {member.role === 'ADMIN' && (
                    <span
                      title="Administrator"
                      className="px-2 py-0.5 rounded text-[10px] font-extrabold bg-purple-100 text-purple-700 flex items-center gap-1"
                    >
                      <Shield className="w-3 h-3" /> ADMIN
                    </span>
                  )}
                  {member.role === 'MODERATOR' && (
                    <span
                      title="Moderator"
                      className="px-2 py-0.5 rounded text-[10px] font-extrabold bg-blue-100 text-blue-700 flex items-center gap-1"
                    >
                      <UserCheck className="w-3 h-3" /> MOD
                    </span>
                  )}
                </div>

                {member.department && (
                  <p className="text-xs text-gray-500 flex items-center gap-1">
                    <Building className="w-3 h-3 text-gray-400" />
                    <span className="truncate">{member.department}</span>
                  </p>
                )}

                {member.email && (
                  <p className="text-xs text-gray-400 flex items-center gap-1 truncate">
                    <Mail className="w-3 h-3 text-gray-400" />
                    <span className="truncate">{member.email}</span>
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

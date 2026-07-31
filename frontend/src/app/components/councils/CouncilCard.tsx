import React from 'react';
import { useNavigate } from 'react-router';
import { Users, ChevronRight, Check, Plus, Clock } from 'lucide-react';
import { Council } from '../../../models/council.model';
import { useCouncilMembership } from '../../../hooks/council/useCouncilMembership';

interface CouncilCardProps {
  council: Council;
}

export function CouncilCard({ council }: CouncilCardProps) {
  const navigate = useNavigate();
  const { join, leave, isJoining, isLeaving } = useCouncilMembership(council.id);

  const handleToggleJoin = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (council.isJoined) {
      leave();
    } else {
      join();
    }
  };

  return (
    <div
      onClick={() => navigate(`/councils/${council.id}`)}
      className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-all duration-200 cursor-pointer group flex flex-col justify-between"
    >
      <div>
        <div className="flex items-start gap-4">
          <div className="w-16 h-16 bg-gradient-to-br from-blue-50 to-purple-50 rounded-xl border border-gray-100 flex items-center justify-center text-3xl flex-shrink-0 shadow-xs">
            {council.logoEmoji || '🏛️'}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <h3 className="font-semibold text-gray-900 text-lg group-hover:text-[#2563EB] transition-colors truncate">
                {council.name}
              </h3>
            </div>
            <p className="text-sm text-gray-600 mb-3 line-clamp-2 leading-relaxed">
              {council.description}
            </p>
          </div>
        </div>

        {/* Tags */}
        {council.tags && council.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mb-4">
            {council.tags.slice(0, 3).map((tag, idx) => (
              <span key={idx} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-md font-medium">
                #{tag}
              </span>
            ))}
          </div>
        )}
      </div>

      <div className="flex items-center justify-between pt-4 border-t border-gray-100 mt-2">
        <div className="flex items-center gap-4 text-sm text-gray-600">
          <div className="flex items-center gap-1.5">
            <Users className="w-4 h-4 text-gray-400" />
            <span className="font-medium text-gray-700">{council.memberCount} members</span>
          </div>
          <span className="text-xs bg-blue-50 text-[#2563EB] px-2.5 py-0.5 rounded-full font-semibold">
            {council.category}
          </span>
        </div>

        <div className="flex items-center gap-2">
          {council.pendingJoinRequest ? (
            <span className="px-3 py-1.5 bg-amber-50 text-amber-700 text-xs rounded-lg flex items-center gap-1 border border-amber-200 font-medium">
              <Clock className="w-3.5 h-3.5" />
              Pending
            </span>
          ) : (
            <button
              onClick={handleToggleJoin}
              disabled={isJoining || isLeaving}
              className={`px-3.5 py-1.5 text-xs font-medium rounded-lg transition-all flex items-center gap-1.5 ${
                council.isJoined
                  ? 'bg-gray-100 text-gray-700 hover:bg-gray-200 border border-gray-300'
                  : 'bg-[#2563EB] text-white hover:bg-blue-600 shadow-xs'
              }`}
            >
              {council.isJoined ? (
                <>
                  <Check className="w-3.5 h-3.5" />
                  Joined
                </>
              ) : (
                <>
                  <Plus className="w-3.5 h-3.5" />
                  Join
                </>
              )}
            </button>
          )}

          <button
            className="p-1.5 text-gray-400 group-hover:text-[#2563EB] group-hover:translate-x-0.5 transition-all"
            title="View Council"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>
    </div>
  );
}

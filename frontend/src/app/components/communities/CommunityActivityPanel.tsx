import React from 'react';
import { MessageSquare, Clock, Sparkles, Trophy, Calendar } from 'lucide-react';
import { useRecentlyActiveCommunities } from '../../../hooks/community/useFeaturedCommunities';
import { Community } from '../../../models/community.model';

interface CommunityActivityPanelProps {
  onSelectCommunity?: (community: Community) => void;
}

const RECENT_ACTIVITIES = [
  {
    id: 'act-1',
    community: 'Computer Engineering Club',
    message: 'Dr. Alan Turing posted: Annual Campus HackFest 2026 Announced!',
    time: '2 hours ago',
    type: 'announcement',
  },
  {
    id: 'act-2',
    community: 'Photography Society',
    message: 'Golden Hour Photo Walk scheduled for tomorrow at 6:30 AM',
    time: '4 hours ago',
    type: 'event',
  },
  {
    id: 'act-3',
    community: 'Robotics Club',
    message: 'Autonomous drone flight test schedule updated',
    time: '12 hours ago',
    type: 'discussion',
  },
  {
    id: 'act-4',
    community: 'Entrepreneurship Cell',
    message: 'New venture incubator application link live now',
    time: '1 day ago',
    type: 'announcement',
  },
];

export const CommunityActivityPanel: React.FC<CommunityActivityPanelProps> = ({
  onSelectCommunity,
}) => {
  const { data: activeCommunities } = useRecentlyActiveCommunities();

  return (
    <div className="space-y-6">
      {/* Recent Activity Card */}
      <div className="bg-white rounded-3xl border border-gray-200 shadow-sm p-6 space-y-4">
        <h3 className="text-base font-bold text-gray-900 flex items-center gap-2">
          <Clock className="w-4 h-4 text-blue-600" /> Recent Campus Activity
        </h3>

        <div className="space-y-3.5">
          {RECENT_ACTIVITIES.map((activity) => (
            <div
              key={activity.id}
              className="p-3 rounded-2xl bg-gray-50/80 border border-gray-100 hover:bg-blue-50/50 transition-colors space-y-1"
            >
              <div className="flex items-center justify-between">
                <span className="text-[11px] font-bold text-blue-600">{activity.community}</span>
                <span className="text-[10px] text-gray-400 font-medium">{activity.time}</span>
              </div>
              <p className="text-xs text-gray-800 font-medium line-clamp-2 leading-relaxed">
                {activity.message}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Top Active Communities */}
      <div className="bg-white rounded-3xl border border-gray-200 shadow-sm p-6 space-y-4">
        <h3 className="text-base font-bold text-gray-900 flex items-center gap-2">
          <Trophy className="w-4 h-4 text-amber-500" /> Most Active Societies
        </h3>

        <div className="space-y-3">
          {(activeCommunities || []).slice(0, 4).map((comm) => (
            <div
              key={comm.id}
              onClick={() => onSelectCommunity?.(comm)}
              className="flex items-center justify-between p-2.5 rounded-xl hover:bg-gray-50 transition-colors cursor-pointer"
            >
              <div className="flex items-center space-x-3">
                <div className="w-9 h-9 rounded-xl bg-blue-600 text-white font-bold text-sm flex items-center justify-center shadow-sm">
                  {comm.name.charAt(0)}
                </div>
                <div>
                  <h4 className="text-xs font-bold text-gray-900 line-clamp-1">{comm.name}</h4>
                  <p className="text-[11px] text-gray-500">{comm.memberCount} members</p>
                </div>
              </div>

              <span className="text-[10px] font-bold px-2 py-0.5 bg-emerald-50 text-emerald-700 rounded-md">
                High Activity
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

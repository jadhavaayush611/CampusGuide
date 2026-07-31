import React from 'react';
import { useNotifications } from '../../../hooks/notifications/useNotifications';
import { useUnreadNotificationCount } from '../../../hooks/notifications/useUnreadNotificationCount';
import { Bell, Clock, Calendar, MessageSquare, ChevronRight, Check } from 'lucide-react';
import { useNavigate } from 'react-router';

export const NotificationsWidget: React.FC = () => {
  const navigate = useNavigate();

  const { data: notifications = [], isLoading: loadingNotifs } = useNotifications();
  const { data: unreadCount = 0, isLoading: loadingUnread } = useUnreadNotificationCount();

  const isLoading = loadingNotifs || loadingUnread;

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm animate-pulse space-y-3">
        <div className="h-5 bg-gray-200 rounded w-1/3"></div>
        <div className="h-16 bg-gray-100 rounded-xl"></div>
        <div className="h-16 bg-gray-100 rounded-xl"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-red-50 flex items-center justify-center text-red-600 relative">
            <Bell className="w-5 h-5" />
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-600 text-white rounded-full text-[10px] font-extrabold flex items-center justify-center ring-2 ring-white">
                {unreadCount}
              </span>
            )}
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">Notifications</h3>
            <p className="text-xs text-gray-500">{unreadCount} unread alert{unreadCount !== 1 ? 's' : ''}</p>
          </div>
        </div>

        <button
          onClick={() => navigate('/notices')}
          className="text-xs text-blue-600 hover:text-blue-800 font-semibold flex items-center gap-1 hover:underline"
        >
          <span>View All</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>

      <div className="space-y-2.5">
        {notifications.slice(0, 3).map((item) => (
          <div
            key={item.id}
            onClick={() => navigate(item.linkUrl || '/notices')}
            className={`p-3 rounded-xl border transition-all cursor-pointer flex items-start gap-3 ${
              !item.isRead
                ? 'bg-blue-50/40 border-blue-200/80 hover:bg-blue-50/80'
                : 'bg-gray-50/70 border-gray-200/70 hover:bg-gray-100/70'
            }`}
          >
            <div
              className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 mt-0.5 ${
                item.type === 'reminder'
                  ? 'bg-blue-100 text-blue-700'
                  : item.type === 'event'
                  ? 'bg-purple-100 text-purple-700'
                  : 'bg-amber-100 text-amber-700'
              }`}
            >
              {item.type === 'reminder' ? (
                <Clock className="w-4 h-4" />
              ) : item.type === 'event' ? (
                <Calendar className="w-4 h-4" />
              ) : (
                <MessageSquare className="w-4 h-4" />
              )}
            </div>

            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2">
                <p className="text-xs font-bold text-gray-900 truncate">{item.title}</p>
                {!item.isRead && (
                  <span className="w-2 h-2 bg-blue-600 rounded-full flex-shrink-0"></span>
                )}
              </div>
              <p className="text-[11px] text-gray-600 mt-0.5 line-clamp-1">{item.description}</p>
              <span className="text-[10px] text-gray-400 mt-1 block">{item.time}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

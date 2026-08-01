import React from 'react';
import { NotificationItem } from '../../../models/notification.model';
import {
  GraduationCap,
  CalendarCheck,
  Calendar,
  Users,
  Shield,
  BookOpen,
  ClipboardList,
  Compass,
  KeyRound,
  Server,
  ExternalLink,
  CheckCircle,
  Circle,
  Archive,
  RotateCcw,
  Trash2,
  Clock,
  AlertOctagon,
  AlertTriangle,
  Info,
} from 'lucide-react';
import { useNavigate } from 'react-router';

interface NotificationItemCardProps {
  item: NotificationItem;
  onSelect: (item: NotificationItem) => void;
  onToggleRead: (id: string, currentlyRead: boolean) => void;
  onToggleArchive: (id: string, currentlyArchived: boolean) => void;
  onDelete: (id: string) => void;
}

const CATEGORY_ICONS: Record<string, React.FC<{ className?: string }>> = {
  Academic: GraduationCap,
  Planner: CalendarCheck,
  Calendar: Calendar,
  Communities: Users,
  Councils: Shield,
  Resources: BookOpen,
  Notices: ClipboardList,
  Atlas: Compass,
  Authentication: KeyRound,
  System: Server,
};

const CATEGORY_COLORS: Record<string, string> = {
  Academic: 'bg-blue-50 text-blue-700 border-blue-200',
  Planner: 'bg-indigo-50 text-indigo-700 border-indigo-200',
  Calendar: 'bg-purple-50 text-purple-700 border-purple-200',
  Communities: 'bg-teal-50 text-teal-700 border-teal-200',
  Councils: 'bg-amber-50 text-amber-700 border-amber-200',
  Resources: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  Notices: 'bg-rose-50 text-rose-700 border-rose-200',
  Atlas: 'bg-violet-50 text-violet-700 border-violet-200',
  Authentication: 'bg-sky-50 text-sky-700 border-sky-200',
  System: 'bg-gray-100 text-gray-700 border-gray-200',
};

const PRIORITY_BADGES: Record<string, { label: string; class: string; icon: React.FC<{ className?: string }> }> = {
  URGENT: { label: 'URGENT', class: 'bg-red-100 text-red-700 border-red-200 font-bold', icon: AlertOctagon },
  HIGH: { label: 'HIGH', class: 'bg-amber-100 text-amber-700 border-amber-200 font-semibold', icon: AlertTriangle },
  NORMAL: { label: 'NORMAL', class: 'bg-blue-50 text-blue-600 border-blue-200', icon: Info },
  LOW: { label: 'LOW', class: 'bg-gray-100 text-gray-600 border-gray-200', icon: Clock },
};

export const NotificationItemCard: React.FC<NotificationItemCardProps> = ({
  item,
  onSelect,
  onToggleRead,
  onToggleArchive,
  onDelete,
}) => {
  const navigate = useNavigate();
  const IconComponent = CATEGORY_ICONS[item.category] || Server;
  const categoryColorClass = CATEGORY_COLORS[item.category] || CATEGORY_COLORS.System;
  const priorityInfo = PRIORITY_BADGES[item.priority] || PRIORITY_BADGES.NORMAL;
  const PriorityIcon = priorityInfo.icon;

  const handleActionClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (item.actionLink) {
      navigate(item.actionLink);
    }
  };

  return (
    <div
      onClick={() => onSelect(item)}
      className={`group relative p-5 rounded-2xl border transition-all duration-200 cursor-pointer ${
        !item.isRead
          ? 'bg-blue-50/30 border-blue-200/80 shadow-sm hover:bg-blue-50/60'
          : 'bg-white border-gray-200 hover:border-gray-300 hover:shadow-sm'
      }`}
    >
      <div className="flex items-start gap-4">
        {/* Category Icon */}
        <div className={`w-11 h-11 rounded-2xl flex items-center justify-center border flex-shrink-0 mt-0.5 ${categoryColorClass}`}>
          <IconComponent className="w-5.5 h-5.5" />
        </div>

        {/* Content Details */}
        <div className="flex-1 min-w-0">
          <div className="flex flex-wrap items-center justify-between gap-2 mb-1.5">
            <div className="flex items-center gap-2 flex-wrap">
              {/* Unread indicator */}
              {!item.isRead && (
                <span className="w-2.5 h-2.5 bg-blue-600 rounded-full flex-shrink-0 animate-pulse" title="Unread" />
              )}

              {/* Title */}
              <h3 className={`text-sm font-bold ${item.isRead ? 'text-gray-800' : 'text-gray-900'} line-clamp-1`}>
                {item.title}
              </h3>
            </div>

            {/* Badges: Category, Priority, Delivery Status */}
            <div className="flex items-center gap-1.5 flex-shrink-0">
              {/* Category Badge */}
              <span className={`px-2 py-0.5 rounded-lg text-[10px] font-bold border ${categoryColorClass}`}>
                {item.category}
              </span>

              {/* Priority Badge */}
              <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-lg text-[10px] border ${priorityInfo.class}`}>
                <PriorityIcon className="w-3 h-3" />
                <span>{priorityInfo.label}</span>
              </span>

              {/* Delivery Status Badge (if not standard DELIVERED) */}
              {item.deliveryStatus === 'SCHEDULED' && (
                <span className="px-2 py-0.5 rounded-lg text-[10px] font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200 flex items-center gap-1">
                  <Clock className="w-3 h-3" />
                  <span>Scheduled</span>
                </span>
              )}
              {item.deliveryStatus === 'FAILED' && (
                <span className="px-2 py-0.5 rounded-lg text-[10px] font-bold bg-red-100 text-red-700 border border-red-200 flex items-center gap-1">
                  <AlertOctagon className="w-3 h-3 text-red-600" />
                  <span>Delivery Failed</span>
                </span>
              )}
            </div>
          </div>

          {/* Notification Message */}
          <p className="text-xs text-gray-600 leading-relaxed line-clamp-2 mb-3">
            {item.message}
          </p>

          {/* Metadata Bar & Actions */}
          <div className="flex flex-wrap items-center justify-between gap-3 text-xs text-gray-500 pt-2 border-t border-gray-100">
            <div className="flex items-center gap-3 flex-wrap text-[11px]">
              <span className="font-semibold text-gray-700">{item.sourceModule}</span>
              <span>•</span>
              <span className="text-gray-400">{item.time || item.createdAt}</span>

              {item.relatedEntity && (
                <>
                  <span>•</span>
                  <span className="bg-gray-100 text-gray-700 px-2 py-0.5 rounded text-[10px] font-medium truncate max-w-[160px]">
                    {item.relatedEntity.name || item.relatedEntity.type}
                  </span>
                </>
              )}
            </div>

            <div className="flex items-center gap-2">
              {/* Deep Link Button */}
              {item.actionLink && (
                <button
                  onClick={handleActionClick}
                  className="inline-flex items-center gap-1 px-2.5 py-1 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg text-[11px] font-semibold transition-colors"
                >
                  <span>Open</span>
                  <ExternalLink className="w-3 h-3" />
                </button>
              )}

              {/* Quick Actions (Read/Unread, Archive/Restore, Delete) */}
              <div className="flex items-center gap-1 opacity-90 sm:opacity-0 group-hover:opacity-100 transition-opacity">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onToggleRead(item.id, item.isRead);
                  }}
                  title={item.isRead ? 'Mark as Unread' : 'Mark as Read'}
                  className="p-1.5 hover:bg-gray-100 rounded-lg text-gray-500 hover:text-gray-800 transition-colors"
                >
                  {item.isRead ? <Circle className="w-3.5 h-3.5" /> : <CheckCircle className="w-3.5 h-3.5 text-blue-600" />}
                </button>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onToggleArchive(item.id, item.isArchived);
                  }}
                  title={item.isArchived ? 'Restore' : 'Archive'}
                  className="p-1.5 hover:bg-gray-100 rounded-lg text-gray-500 hover:text-gray-800 transition-colors"
                >
                  {item.isArchived ? <RotateCcw className="w-3.5 h-3.5 text-purple-600" /> : <Archive className="w-3.5 h-3.5" />}
                </button>

                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(item.id);
                  }}
                  title="Delete"
                  className="p-1.5 hover:bg-red-50 rounded-lg text-gray-400 hover:text-red-600 transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

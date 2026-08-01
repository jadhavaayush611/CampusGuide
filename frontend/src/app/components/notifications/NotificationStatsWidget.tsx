import React from 'react';
import { NotificationStats } from '../../../models/notification.model';
import { Bell, MailOpen, Archive, CheckCircle2, Clock, AlertTriangle, Inbox } from 'lucide-react';

interface NotificationStatsWidgetProps {
  stats?: NotificationStats;
  currentStatus: 'ALL' | 'UNREAD' | 'READ' | 'ARCHIVED';
  onStatusChange: (status: 'ALL' | 'UNREAD' | 'READ' | 'ARCHIVED') => void;
}

export const NotificationStatsWidget: React.FC<NotificationStatsWidgetProps> = ({
  stats,
  currentStatus,
  onStatusChange,
}) => {
  const cards = [
    {
      id: 'ALL',
      label: 'Active Inbox',
      value: stats?.total ?? 0,
      icon: Inbox,
      color: 'bg-blue-50 text-blue-600 border-blue-200',
      activeColor: 'ring-2 ring-blue-500 bg-blue-50/90',
    },
    {
      id: 'UNREAD',
      label: 'Unread Alerts',
      value: stats?.unread ?? 0,
      icon: Bell,
      color: 'bg-amber-50 text-amber-600 border-amber-200',
      activeColor: 'ring-2 ring-amber-500 bg-amber-50/90',
    },
    {
      id: 'READ',
      label: 'Read History',
      value: stats?.read ?? 0,
      icon: MailOpen,
      color: 'bg-emerald-50 text-emerald-600 border-emerald-200',
      activeColor: 'ring-2 ring-emerald-500 bg-emerald-50/90',
    },
    {
      id: 'ARCHIVED',
      label: 'Archived',
      value: stats?.archived ?? 0,
      icon: Archive,
      color: 'bg-purple-50 text-purple-600 border-purple-200',
      activeColor: 'ring-2 ring-purple-500 bg-purple-50/90',
    },
  ];

  return (
    <div className="space-y-4">
      {/* Primary Status Selector Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        {cards.map((c) => {
          const Icon = c.icon;
          const isSelected = currentStatus === c.id;

          return (
            <button
              key={c.id}
              onClick={() => onStatusChange(c.id as any)}
              className={`p-4 rounded-2xl border bg-white transition-all text-left flex items-center justify-between ${
                isSelected ? c.activeColor : 'border-gray-200 hover:border-gray-300 hover:shadow-sm'
              }`}
            >
              <div>
                <p className="text-xs font-medium text-gray-500">{c.label}</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{c.value}</p>
              </div>
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center border ${c.color}`}>
                <Icon className="w-5 h-5" />
              </div>
            </button>
          );
        })}
      </div>

      {/* Secondary Delivery Status Pills */}
      {stats && (
        <div className="bg-gray-50 border border-gray-200/80 rounded-xl p-3 flex flex-wrap items-center justify-between gap-3 text-xs">
          <span className="font-semibold text-gray-700">Delivery Infrastructure Status:</span>
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1.5 text-emerald-700 font-medium">
              <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
              <span>Delivered: {stats.delivered}</span>
            </span>
            <span className="flex items-center gap-1.5 text-blue-700 font-medium">
              <Clock className="w-3.5 h-3.5 text-blue-600" />
              <span>Scheduled: {stats.scheduled}</span>
            </span>
            {stats.failed > 0 && (
              <span className="flex items-center gap-1.5 text-red-700 font-medium">
                <AlertTriangle className="w-3.5 h-3.5 text-red-600" />
                <span>Failed: {stats.failed}</span>
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

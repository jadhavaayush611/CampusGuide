import React from 'react';
import { NotificationItem } from '../../../models/notification.model';
import {
  X,
  ExternalLink,
  CheckCircle,
  Circle,
  Archive,
  RotateCcw,
  Trash2,
  Calendar,
  Tag,
  Shield,
  Layers,
  Clock,
  AlertTriangle,
} from 'lucide-react';
import { useNavigate } from 'react-router';

interface NotificationDetailModalProps {
  item: NotificationItem | null;
  onClose: () => void;
  onToggleRead: (id: string, currentlyRead: boolean) => void;
  onToggleArchive: (id: string, currentlyArchived: boolean) => void;
  onDelete: (id: string) => void;
}

export const NotificationDetailModal: React.FC<NotificationDetailModalProps> = ({
  item,
  onClose,
  onToggleRead,
  onToggleArchive,
  onDelete,
}) => {
  const navigate = useNavigate();

  if (!item) return null;

  const handleActionClick = () => {
    if (item.actionLink) {
      navigate(item.actionLink);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-sm animate-fadeIn">
      <div className="bg-white rounded-3xl max-w-xl w-full border border-gray-200 shadow-2xl overflow-hidden animate-scaleIn">
        {/* Header */}
        <div className="p-6 border-b border-gray-100 flex items-start justify-between gap-4 bg-gradient-to-r from-gray-50 to-white">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="px-2.5 py-0.5 rounded-lg text-xs font-bold bg-blue-50 text-blue-700 border border-blue-200">
                {item.category}
              </span>
              <span className="px-2 py-0.5 rounded-lg text-xs font-bold bg-gray-100 text-gray-700">
                {item.priority} Priority
              </span>
              <span
                className={`px-2 py-0.5 rounded-lg text-xs font-semibold ${
                  item.isRead ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'
                }`}
              >
                {item.isRead ? 'Read' : 'Unread'}
              </span>
            </div>
            <h2 className="text-xl font-bold text-gray-900 leading-snug">{item.title}</h2>
          </div>

          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-xl text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body Content */}
        <div className="p-6 space-y-6 max-h-[70vh] overflow-y-auto">
          {/* Main Message */}
          <div>
            <h4 className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Notification Details</h4>
            <p className="text-sm text-gray-700 leading-relaxed bg-gray-50/70 p-4 rounded-2xl border border-gray-200/70">
              {item.message}
            </p>
          </div>

          {/* Metadata Grid */}
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 flex items-center gap-2.5">
              <Layers className="w-4 h-4 text-blue-600" />
              <div>
                <p className="text-gray-400 font-medium">Source Module</p>
                <p className="font-semibold text-gray-800">{item.sourceModule}</p>
              </div>
            </div>

            <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 flex items-center gap-2.5">
              <Clock className="w-4 h-4 text-indigo-600" />
              <div>
                <p className="text-gray-400 font-medium">Timestamp</p>
                <p className="font-semibold text-gray-800">{item.time || item.createdAt}</p>
              </div>
            </div>

            <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 flex items-center gap-2.5">
              <Shield className="w-4 h-4 text-emerald-600" />
              <div>
                <p className="text-gray-400 font-medium">Delivery Status</p>
                <p className="font-semibold text-gray-800">{item.deliveryStatus}</p>
              </div>
            </div>

            {item.relatedEntity && (
              <div className="p-3 bg-gray-50 rounded-xl border border-gray-100 flex items-center gap-2.5">
                <Tag className="w-4 h-4 text-purple-600" />
                <div>
                  <p className="text-gray-400 font-medium">Related Entity</p>
                  <p className="font-semibold text-gray-800 truncate">
                    {item.relatedEntity.name || item.relatedEntity.type}
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Footer Actions */}
        <div className="p-4 border-t border-gray-100 bg-gray-50 flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <button
              onClick={() => onToggleRead(item.id, item.isRead)}
              className="inline-flex items-center gap-1.5 px-3 py-2 bg-white hover:bg-gray-100 border border-gray-200 rounded-xl text-xs font-semibold text-gray-700 transition-colors"
            >
              {item.isRead ? <Circle className="w-3.5 h-3.5" /> : <CheckCircle className="w-3.5 h-3.5 text-blue-600" />}
              <span>{item.isRead ? 'Mark Unread' : 'Mark Read'}</span>
            </button>

            <button
              onClick={() => onToggleArchive(item.id, item.isArchived)}
              className="inline-flex items-center gap-1.5 px-3 py-2 bg-white hover:bg-gray-100 border border-gray-200 rounded-xl text-xs font-semibold text-gray-700 transition-colors"
            >
              {item.isArchived ? <RotateCcw className="w-3.5 h-3.5 text-purple-600" /> : <Archive className="w-3.5 h-3.5" />}
              <span>{item.isArchived ? 'Restore' : 'Archive'}</span>
            </button>

            <button
              onClick={() => {
                onDelete(item.id);
                onClose();
              }}
              className="p-2 bg-white hover:bg-red-50 border border-gray-200 rounded-xl text-gray-400 hover:text-red-600 transition-colors"
              title="Delete Notification"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>

          {item.actionLink && (
            <button
              onClick={handleActionClick}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white hover:bg-blue-700 rounded-xl text-xs font-bold shadow-md shadow-blue-200 transition-all"
            >
              <span>Go to Source</span>
              <ExternalLink className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

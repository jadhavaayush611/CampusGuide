import { lazy, Suspense } from 'react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { X, Calendar, User, Building, Pin, Eye, EyeOff, Shield, Tag, Edit2, Trash2, Clock } from 'lucide-react';
import { Notice } from '../../../models/notice.model';
import { useToggleNoticeRead } from '../../../hooks/notices/useToggleNoticeRead';
import { usePinNotice } from '../../../hooks/notices/usePinNotice';
import { useDeleteNotice } from '../../../hooks/notices/useDeleteNotice';

const NoticeAttachmentViewer = lazy(() =>
  import('./NoticeAttachmentViewer').then((m) => ({ default: m.NoticeAttachmentViewer }))
);

interface NoticeDetailsModalProps {
  notice: Notice | null;
  onClose: () => void;
  onEdit?: (notice: Notice) => void;
}

export function NoticeDetailsModal({ notice, onClose, onEdit }: NoticeDetailsModalProps) {
  const toggleReadMutation = useToggleNoticeRead();
  const pinMutation = usePinNotice();
  const deleteMutation = useDeleteNotice();

  if (!notice) return null;

  const formatDate = (dateStr?: string) => {
    if (!dateStr) return 'N/A';
    try {
      return new Date(dateStr).toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateStr;
    }
  };

  const handleReadToggle = () => {
    toggleReadMutation.mutate({ id: notice.id, isRead: !notice.isRead });
  };

  const handlePinToggle = () => {
    pinMutation.mutate({ id: notice.id, isPinned: !notice.isPinned });
  };

  const handleDelete = () => {
    if (window.confirm(`Are you sure you want to delete notice "${notice.title}"?`)) {
      deleteMutation.mutate(notice.id);
      onClose();
    }
  };

  return (
    <DialogPrimitive.Root open={!!notice} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-gray-900/60 backdrop-blur-xs animate-in fade-in duration-200" />
        <DialogPrimitive.Content className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-3xl w-full max-h-[90vh] flex flex-col shadow-2xl overflow-hidden border border-gray-100 z-10">
            {/* Header */}
            <div className="p-6 pb-4 border-b border-gray-100 bg-gray-50/50 flex items-start justify-between gap-4">
              <div className="space-y-3 max-w-2xl">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="px-3 py-1 bg-blue-50 text-blue-700 font-semibold text-xs rounded-full border border-blue-200">
                    {notice.category}
                  </span>
                  <span className="px-3 py-1 bg-red-50 text-red-700 font-bold text-xs rounded-full border border-red-200">
                    {notice.priority} Priority
                  </span>
                  {notice.isPinned && (
                    <span className="inline-flex items-center gap-1 px-3 py-1 bg-amber-100 text-amber-800 font-semibold text-xs rounded-full border border-amber-200">
                      <Pin className="w-3 h-3 fill-amber-700" aria-hidden="true" />
                      Pinned
                    </span>
                  )}
                  <span className="inline-flex items-center gap-1 px-2.5 py-1 bg-gray-100 text-gray-600 text-xs font-medium rounded-md">
                    <Shield className="w-3 h-3 text-gray-500" aria-hidden="true" />
                    {notice.visibility}
                  </span>
                </div>

                <DialogPrimitive.Title className="text-2xl font-bold text-gray-900 leading-snug">
                  {notice.title}
                </DialogPrimitive.Title>
              </div>

              <DialogPrimitive.Close asChild>
                <button
                  className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200/60 rounded-xl transition-all shrink-0 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-1"
                  aria-label="Close details"
                >
                  <X className="w-5 h-5" aria-hidden="true" />
                </button>
              </DialogPrimitive.Close>
            </div>

            {/* Content Body */}
            <div className="p-6 overflow-y-auto space-y-6 flex-1">
              {/* Notice Metadata Banner */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 p-4 bg-blue-50/40 border border-blue-100 rounded-2xl text-xs text-gray-700">
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <User className="w-4 h-4 text-blue-600 shrink-0" aria-hidden="true" />
                    <span>
                      <strong>Issued By:</strong> {notice.postedBy} ({notice.postedByRole || 'Admin'})
                    </span>
                  </div>
                  {notice.councilName && (
                    <div className="flex items-center gap-2">
                      <Building className="w-4 h-4 text-blue-600 shrink-0" aria-hidden="true" />
                      <span>
                        <strong>Council:</strong> {notice.councilName}
                      </span>
                    </div>
                  )}
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-blue-600 shrink-0" aria-hidden="true" />
                    <span>
                      <strong>Published:</strong> {formatDate(notice.publishedAt)}
                    </span>
                  </div>
                  {notice.expiresAt && (
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-amber-600 shrink-0" aria-hidden="true" />
                      <span>
                        <strong>Expires:</strong> {formatDate(notice.expiresAt)}
                      </span>
                    </div>
                  )}
                </div>
              </div>

              {/* Notice Summary Box */}
              {notice.summary && (
                <div className="p-4 bg-amber-50/50 border border-amber-200/60 rounded-2xl">
                  <h4 className="text-xs font-bold text-amber-900 uppercase tracking-wider mb-1">
                    Executive Summary
                  </h4>
                  <p className="text-sm text-amber-950 font-medium leading-relaxed">{notice.summary}</p>
                </div>
              )}

              {/* Main Notice Content */}
              <div className="space-y-3">
                <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">
                  Notice Content & Directives
                </h4>
                <div className="prose prose-blue max-w-none text-gray-800 text-sm leading-relaxed whitespace-pre-line bg-gray-50/30 p-5 rounded-2xl border border-gray-100">
                  {notice.content}
                </div>
              </div>

              {/* Tags */}
              {notice.tags && notice.tags.length > 0 && (
                <div className="space-y-2">
                  <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Keywords & Tags</h4>
                  <div className="flex flex-wrap gap-2">
                    {notice.tags.map((tag) => (
                      <span
                        key={tag}
                        className="inline-flex items-center gap-1.5 px-3 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-lg border border-gray-200"
                      >
                        <Tag className="w-3 h-3 text-gray-400" aria-hidden="true" />
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Attachments Section */}
              {notice.attachments && notice.attachments.length > 0 && (
                <Suspense fallback={<div className="h-24 bg-gray-50 animate-pulse rounded" />}>
                  <NoticeAttachmentViewer attachments={notice.attachments} />
                </Suspense>
              )}
            </div>

            {/* Modal Footer Controls */}
            <div className="px-6 py-4 border-t border-gray-100 bg-gray-50/50 flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <button
                  onClick={handleReadToggle}
                  className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold transition-all focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 ${
                    notice.isRead
                      ? 'bg-gray-200 hover:bg-gray-300 text-gray-700'
                      : 'bg-[#2563EB] hover:bg-blue-700 text-white shadow-sm'
                  }`}
                >
                  {notice.isRead ? <EyeOff className="w-4 h-4" aria-hidden="true" /> : <Eye className="w-4 h-4" aria-hidden="true" />}
                  {notice.isRead ? 'Mark as Unread' : 'Mark as Read'}
                </button>

                <button
                  onClick={handlePinToggle}
                  className={`inline-flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-semibold border transition-all focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-amber-500 focus-visible:ring-offset-1 ${
                    notice.isPinned
                      ? 'bg-amber-100 text-amber-900 border-amber-300 hover:bg-amber-200'
                      : 'bg-white text-gray-700 border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  <Pin className="w-3.5 h-3.5" aria-hidden="true" />
                  {notice.isPinned ? 'Unpin' : 'Pin to Top'}
                </button>
              </div>

              <div className="flex items-center gap-2">
                {onEdit && (
                  <button
                    onClick={() => {
                      onClose();
                      onEdit(notice);
                    }}
                    className="inline-flex items-center gap-1.5 px-4 py-2 bg-white border border-gray-200 hover:bg-gray-50 text-gray-700 font-semibold text-xs rounded-xl transition-all focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  >
                    <Edit2 className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    Edit Notice
                  </button>
                )}

                <button
                  onClick={handleDelete}
                  className="inline-flex items-center gap-1.5 px-4 py-2 bg-red-50 hover:bg-red-100 text-red-700 font-semibold text-xs rounded-xl border border-red-200 transition-all focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-1"
                >
                  <Trash2 className="w-3.5 h-3.5" aria-hidden="true" />
                  Delete
                </button>
              </div>
            </div>
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}

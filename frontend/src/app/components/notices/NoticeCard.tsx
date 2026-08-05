import React, { useState, memo, useCallback, useMemo } from 'react';
import { Pin, Calendar, Eye, EyeOff, Paperclip, MoreVertical, Edit2, Trash2, Globe, Shield, Tag } from 'lucide-react';
import { Notice, NoticeCategory, NoticePriority } from '../../../models/notice.model';
import { useToggleNoticeRead } from '../../../hooks/notices/useToggleNoticeRead';
import { usePinNotice } from '../../../hooks/notices/usePinNotice';
import { useDeleteNotice } from '../../../hooks/notices/useDeleteNotice';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';

interface NoticeCardProps {
  notice: Notice;
  onSelect: (notice: Notice) => void;
  onEdit?: (notice: Notice) => void;
}

export const NoticeCard = memo(function NoticeCard({ notice, onSelect, onEdit }: NoticeCardProps) {
  const toggleReadMutation = useToggleNoticeRead();
  const pinMutation = usePinNotice();
  const deleteMutation = useDeleteNotice();

  const categoryStyle = useMemo(() => {
    switch (notice.category) {
      case 'Academic':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'Administrative':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'Examination':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'Events':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'Placements':
        return 'bg-amber-50 text-amber-800 border-amber-200';
      case 'Scholarships':
        return 'bg-teal-50 text-teal-700 border-teal-200';
      case 'Councils':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      case 'General':
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  }, [notice.category]);

  const priorityStyle = useMemo(() => {
    switch (notice.priority) {
      case 'URGENT':
        return 'bg-red-600 text-white border-red-700';
      case 'HIGH':
        return 'bg-red-50 text-red-700 border-red-200';
      case 'MEDIUM':
        return 'bg-amber-50 text-amber-800 border-amber-200';
      case 'LOW':
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  }, [notice.priority]);

  const formattedDate = useMemo(() => {
    try {
      return new Date(notice.publishedAt).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return notice.publishedAt;
    }
  }, [notice.publishedAt]);

  const handleSelect = useCallback(() => {
    onSelect(notice);
  }, [onSelect, notice]);

  const handleReadToggle = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      toggleReadMutation.mutate({ id: notice.id, isRead: !notice.isRead });
    },
    [toggleReadMutation, notice.id, notice.isRead]
  );

  const handlePinToggle = useCallback(
    (e?: React.MouseEvent | Event) => {
      e?.stopPropagation();
      pinMutation.mutate({ id: notice.id, isPinned: !notice.isPinned });
    },
    [pinMutation, notice.id, notice.isPinned]
  );

  const handleDelete = useCallback(
    (e?: React.MouseEvent | Event) => {
      e?.stopPropagation();
      if (window.confirm(`Are you sure you want to delete notice "${notice.title}"?`)) {
        deleteMutation.mutate(notice.id);
      }
    },
    [deleteMutation, notice.id, notice.title]
  );

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      if (e.target === e.currentTarget) {
        e.preventDefault();
        handleSelect();
      }
    }
  }, [handleSelect]);

  return (
    <div
      onClick={handleSelect}
      onKeyDown={handleKeyDown}
      role="button"
      tabIndex={0}
      aria-label={`Notice: ${notice.title}, Category: ${notice.category}, Priority: ${notice.priority}. Issued by ${notice.postedBy} on ${formattedDate}`}
      className={`relative bg-white rounded-2xl border transition-all duration-200 p-6 shadow-2xs hover:shadow-md cursor-pointer group focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2 ${
        notice.isPinned ? 'border-amber-300 ring-2 ring-amber-400/20 bg-amber-50/10' : 'border-gray-100 hover:border-blue-200'
      } ${!notice.isRead ? 'border-l-4 border-l-[#2563EB]' : ''}`}
    >
      {/* Card Header & Badges */}
      <div className="flex items-start justify-between gap-4 mb-3">
        <div className="flex flex-wrap items-center gap-2">
          {/* Category */}
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${categoryStyle}`}>
            {notice.category}
          </span>

          {/* Priority */}
          <span className={`px-2.5 py-0.5 rounded-full text-xs font-bold border ${priorityStyle}`}>
            {notice.priority}
          </span>

          {/* Pinned */}
          {notice.isPinned && (
            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 border border-amber-200">
              <Pin className="w-3 h-3 fill-amber-700" aria-hidden="true" />
              Pinned
            </span>
          )}

          {/* Visibility Scope */}
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[11px] font-medium text-gray-500 bg-gray-100">
            {notice.visibility === 'PUBLIC' ? <Globe className="w-3 h-3" aria-hidden="true" /> : <Shield className="w-3 h-3" aria-hidden="true" />}
            {notice.visibility}
          </span>
        </div>

        {/* Action controls */}
        <div className="flex items-center gap-1 shrink-0" onClick={(e) => e.stopPropagation()}>
          <button
            onClick={handleReadToggle}
            title={notice.isRead ? 'Mark as unread' : 'Mark as read'}
            aria-label={notice.isRead ? 'Mark as unread' : 'Mark as read'}
            className={`p-1.5 rounded-lg transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 ${
              notice.isRead ? 'text-gray-400 hover:bg-gray-100' : 'text-blue-600 bg-blue-50 hover:bg-blue-100'
            }`}
          >
            {notice.isRead ? <EyeOff className="w-4 h-4" aria-hidden="true" /> : <Eye className="w-4 h-4" aria-hidden="true" />}
          </button>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="p-1.5 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-lg transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                aria-label="More options"
                title="More options"
              >
                <MoreVertical className="w-4 h-4" aria-hidden="true" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-44">
              <DropdownMenuItem onClick={handlePinToggle}>
                <Pin className="w-3.5 h-3.5 mr-2" aria-hidden="true" />
                {notice.isPinned ? 'Unpin Notice' : 'Pin to Top'}
              </DropdownMenuItem>
              {onEdit && (
                <DropdownMenuItem onClick={() => onEdit(notice)}>
                  <Edit2 className="w-3.5 h-3.5 mr-2 text-blue-600" aria-hidden="true" />
                  Edit Notice
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onClick={handleDelete} className="text-red-600 focus:text-red-600">
                <Trash2 className="w-3.5 h-3.5 mr-2" aria-hidden="true" />
                Delete Notice
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Notice Title */}
      <h3 className="text-lg font-bold text-gray-900 mb-2 group-hover:text-[#2563EB] transition-colors leading-snug">
        {notice.title}
      </h3>

      {/* Summary / Content Preview */}
      <p className="text-gray-600 text-sm mb-4 line-clamp-2 leading-relaxed">
        {notice.summary || notice.content}
      </p>

      {/* Tags */}
      {notice.tags && notice.tags.length > 0 && (
        <div className="flex flex-wrap gap-1.5 mb-4">
          {notice.tags.slice(0, 4).map((tag) => (
            <span key={tag} className="inline-flex items-center gap-1 px-2 py-0.5 bg-gray-50 text-gray-600 rounded-md text-[11px] font-medium border border-gray-100">
              <Tag className="w-2.5 h-2.5 text-gray-400" aria-hidden="true" />
              {tag}
            </span>
          ))}
        </div>
      )}

      {/* Footer Info */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-gray-100 text-xs text-gray-500">
        <div className="flex items-center gap-3">
          <span className="font-semibold text-gray-700">Issued by: {notice.postedBy}</span>
          <span>•</span>
          <span className="inline-flex items-center gap-1">
            <Calendar className="w-3.5 h-3.5 text-gray-400" aria-hidden="true" />
            {formattedDate}
          </span>
        </div>

        <div className="flex items-center gap-3">
          {notice.attachments && notice.attachments.length > 0 && (
            <span className="inline-flex items-center gap-1 font-semibold text-blue-700 bg-blue-50 px-2 py-0.5 rounded-md">
              <Paperclip className="w-3.5 h-3.5" aria-hidden="true" />
              {notice.attachments.length} {notice.attachments.length === 1 ? 'Attachment' : 'Attachments'}
            </span>
          )}
          <span className="text-[#2563EB] font-bold group-hover:translate-x-1 transition-transform inline-flex items-center" aria-hidden="true">
            Read More →
          </span>
        </div>
      </div>
    </div>
  );
});

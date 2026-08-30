import { useState, useEffect } from 'react';
import { X, Plus, Trash2, Calendar, FileText, Shield, Tag, Send } from 'lucide-react';
import { Notice, NoticeCategory, NoticePriority, NoticeVisibility, CreateNoticePayload, UpdateNoticePayload, NoticeAttachment } from '../../../models/notice.model';
import { useCreateNotice } from '../../../hooks/notices/useCreateNotice';
import { useUpdateNotice } from '../../../hooks/notices/useUpdateNotice';
import { AttachmentManager } from '../common/AttachmentManager';
import { attachmentSdk } from '../../../sdk/attachments/AttachmentSdk';
import { useQueryClient } from '@tanstack/react-query';
import { queryKeys } from '../../../sdk/queryKeys';
import { toast } from 'sonner';

interface NoticeFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  noticeToEdit?: Notice | null;
}

const CATEGORIES: NoticeCategory[] = [
  'Academic',
  'Administrative',
  'Examination',
  'Events',
  'Councils',
  'Placements',
  'Scholarships',
  'General',
];

const PRIORITIES: NoticePriority[] = ['URGENT', 'HIGH', 'MEDIUM', 'LOW'];

const VISIBILITIES: NoticeVisibility[] = ['PUBLIC', 'STUDENTS', 'FACULTY', 'COUNCIL_MEMBERS', 'INTERNAL'];

export function NoticeFormModal({ isOpen, onClose, noticeToEdit }: NoticeFormModalProps) {
  const queryClient = useQueryClient();
  const createMutation = useCreateNotice();
  const updateMutation = useUpdateNotice();

  const [title, setTitle] = useState('');
  const [slug, setSlug] = useState('');
  const [category, setCategory] = useState<NoticeCategory>('General');
  const [priority, setPriority] = useState<NoticePriority>('MEDIUM');
  const [visibility, setVisibility] = useState<NoticeVisibility>('PUBLIC');
  const [summary, setSummary] = useState('');
  const [content, setContent] = useState('');
  const [tagsInput, setTagsInput] = useState('');
  const [publishedAt, setPublishedAt] = useState('');
  const [expiresAt, setExpiresAt] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [isPublished, setIsPublished] = useState(true);

  // Attachments form state
  const [attachments, setAttachments] = useState<NoticeAttachment[]>([]);
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);

  useEffect(() => {
    if (noticeToEdit) {
      setTitle(noticeToEdit.title);
      setSlug(noticeToEdit.slug);
      setCategory(noticeToEdit.category);
      setPriority(noticeToEdit.priority);
      setVisibility(noticeToEdit.visibility);
      setSummary(noticeToEdit.summary || '');
      setContent(noticeToEdit.content);
      setTagsInput(noticeToEdit.tags ? noticeToEdit.tags.join(', ') : '');
      setPublishedAt(noticeToEdit.publishedAt ? noticeToEdit.publishedAt.slice(0, 16) : '');
      setExpiresAt(noticeToEdit.expiresAt ? noticeToEdit.expiresAt.slice(0, 16) : '');
      setIsPinned(noticeToEdit.isPinned);
      setIsPublished(noticeToEdit.isPublished);
      setAttachments(noticeToEdit.attachments || []);
    } else {
      setTitle('');
      setSlug('');
      setCategory('General');
      setPriority('MEDIUM');
      setVisibility('PUBLIC');
      setSummary('');
      setContent('');
      setTagsInput('');
      setPublishedAt(new Date().toISOString().slice(0, 16));
      setExpiresAt('');
      setIsPinned(false);
      setIsPublished(true);
      setAttachments([]);
    }
  }, [noticeToEdit, isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  const handleTitleChange = (val: string) => {
    setTitle(val);
    if (!noticeToEdit) {
      setSlug(
        val
          .toLowerCase()
          .replace(/[^a-z0-9]+/g, '-')
          .replace(/(^-|-$)/g, '')
      );
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;

    const parsedTags = tagsInput
      .split(',')
      .map((t) => t.trim())
      .filter((t) => t.length > 0);

    if (noticeToEdit) {
      const payload: UpdateNoticePayload = {
        title,
        slug,
        category,
        priority,
        visibility,
        summary: summary || undefined,
        content,
        publishedAt: publishedAt ? new Date(publishedAt).toISOString() : undefined,
        expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined,
        isPinned,
        isPublished,
        tags: parsedTags,
        attachments,
      };
      updateMutation.mutate(
        { id: noticeToEdit.id, payload },
        {
          onSuccess: () => {
            onClose();
          },
        }
      );
    } else {
      const payload: CreateNoticePayload = {
        title,
        slug,
        category,
        priority,
        visibility,
        summary: summary || undefined,
        content,
        publishedAt: publishedAt ? new Date(publishedAt).toISOString() : new Date().toISOString(),
        expiresAt: expiresAt ? new Date(expiresAt).toISOString() : undefined,
        isPinned,
        isPublished,
        tags: parsedTags,
        attachments,
      };
      createMutation.mutate(payload, {
        onSuccess: async (createdNotice) => {
          if (pendingFiles.length > 0) {
            for (const file of pendingFiles) {
              try {
                await attachmentSdk.uploadAttachment(file, 'NOTICE', createdNotice.id);
              } catch (err: any) {
                toast.error(`Failed to upload ${file.name}: ${err.message || 'Upload failed'}`);
              }
            }
            queryClient.invalidateQueries({ queryKey: queryKeys.notices.all });
          }
          onClose();
        },
      });
    }
  };

  if (!isOpen) return null;

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200" role="dialog" aria-modal="true" aria-labelledby="notice-modal-title">
      <div className="bg-white rounded-2xl shadow-2xl border border-gray-200 w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden animate-in zoom-in-95 duration-200 z-10">
        {/* Header */}
        <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-50 text-[#2563EB] rounded-2xl flex items-center justify-center font-bold">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <h2 id="notice-modal-title" className="text-xl font-bold text-gray-900">
                {noticeToEdit ? 'Edit Notice' : 'Publish New Notice'}
              </h2>
              <p className="text-xs text-gray-500">
                {noticeToEdit ? 'Update existing campus notice details' : 'Broadcast an official campus announcement'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200/60 rounded-xl transition-all"
            aria-label="Close dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-6 flex-1 text-sm">
          {/* Title & Slug */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label htmlFor="notice-title" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Notice Title <span className="text-red-500">*</span>
              </label>
              <input
                id="notice-title"
                type="text"
                required
                value={title}
                onChange={(e) => handleTitleChange(e.target.value)}
                placeholder="e.g. Mid-Semester Exam Regulations"
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <label htmlFor="notice-slug" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                URL Slug <span className="text-red-500">*</span>
              </label>
              <input
                id="notice-slug"
                type="text"
                required
                value={slug}
                onChange={(e) => setSlug(e.target.value)}
                placeholder="mid-semester-exam-regulations"
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all"
              />
            </div>
          </div>

          {/* Category, Priority, Visibility */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="space-y-1.5">
              <label htmlFor="notice-category" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">Category</label>
              <select
                id="notice-category"
                value={category}
                onChange={(e) => setCategory(e.target.value as NoticeCategory)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="notice-priority" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">Priority</label>
              <select
                id="notice-priority"
                value={priority}
                onChange={(e) => setPriority(e.target.value as NoticePriority)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
              >
                {PRIORITIES.map((p) => (
                  <option key={p} value={p}>
                    {p}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label htmlFor="notice-visibility" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">Visibility Scope</label>
              <select
                id="notice-visibility"
                value={visibility}
                onChange={(e) => setVisibility(e.target.value as NoticeVisibility)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
              >
                {VISIBILITIES.map((v) => (
                  <option key={v} value={v}>
                    {v}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Dates */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label htmlFor="notice-publish-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-gray-500" />
                Publish Date & Time
              </label>
              <input
                id="notice-publish-date"
                type="datetime-local"
                value={publishedAt}
                onChange={(e) => setPublishedAt(e.target.value)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <label htmlFor="notice-expiry-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-gray-500" />
                Expiry Date & Time (Optional)
              </label>
              <input
                id="notice-expiry-date"
                type="datetime-local"
                value={expiresAt}
                onChange={(e) => setExpiresAt(e.target.value)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
              />
            </div>
          </div>

          {/* Summary */}
          <div className="space-y-1.5">
            <label htmlFor="notice-summary" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Executive Summary (Brief overview)
            </label>
            <input
              id="notice-summary"
              type="text"
              value={summary}
              onChange={(e) => setSummary(e.target.value)}
              placeholder="Short 1-2 sentence preview summary"
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
            />
          </div>

          {/* Content */}
          <div className="space-y-1.5">
            <label htmlFor="notice-content" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Detailed Content <span className="text-red-500">*</span>
            </label>
            <textarea
              id="notice-content"
              required
              rows={5}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Write complete notice directives, instructions, rules, and details..."
              className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
            />
          </div>

          {/* Tags */}
          <div className="space-y-1.5">
            <label htmlFor="notice-tags" className="block text-xs font-bold text-gray-700 uppercase tracking-wider flex items-center gap-1">
              <Tag className="w-3.5 h-3.5 text-gray-500" />
              Tags (Comma separated)
            </label>
            <input
              id="notice-tags"
              type="text"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              placeholder="e.g. examination, timetable, spring-2026"
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl focus:bg-white focus:outline-none focus:ring-2 focus:ring-[#2563EB] transition-all"
            />
          </div>

          {/* Attachments Section */}
          <div className="space-y-3 pt-2 border-t border-gray-100">
            <span className="block text-xs font-bold text-gray-700 uppercase tracking-wider">Attachments</span>
            <AttachmentManager
              ownerType="NOTICE"
              ownerId={noticeToEdit?.id}
              attachments={attachments}
              pendingFiles={pendingFiles}
              onPendingFileAdded={(file) => setPendingFiles((prev) => [...prev, file])}
              onPendingFileRemoved={(idx) => setPendingFiles((prev) => prev.filter((_, i) => i !== idx))}
              onAttachmentUploaded={(att) => {
                setAttachments((prev) => [
                  ...prev,
                  {
                    id: att.id,
                    name: att.originalFileName,
                    url: att.downloadUrl,
                    fileSize: `${Math.round(att.fileSize / 1024)} KB`,
                    fileType: att.contentType,
                    isPreviewable: Boolean(
                      att.contentType?.includes('image') ||
                      att.contentType?.includes('pdf') ||
                      att.originalFileName?.endsWith('.pdf') ||
                      att.originalFileName?.endsWith('.png') ||
                      att.originalFileName?.endsWith('.jpg')
                    ),
                  },
                ]);
              }}
              onAttachmentDeleted={(attId) => {
                setAttachments((prev) => prev.filter((a) => a.id !== attId));
              }}
            />
          </div>

          {/* Toggles */}
          <div className="flex items-center gap-6 pt-2">
            <label htmlFor="notice-pinned" className="flex items-center gap-2 cursor-pointer font-medium text-xs text-gray-700">
              <input
                type="checkbox"
                id="notice-pinned"
                checked={isPinned}
                onChange={(e) => setIsPinned(e.target.checked)}
                className="w-4 h-4 text-[#2563EB] rounded border-gray-300 focus:ring-blue-500"
              />
              Pin to Top of Notice Board
            </label>

            <label htmlFor="notice-published" className="flex items-center gap-2 cursor-pointer font-medium text-xs text-gray-700">
              <input
                type="checkbox"
                id="notice-published"
                checked={isPublished}
                onChange={(e) => setIsPublished(e.target.checked)}
                className="w-4 h-4 text-[#2563EB] rounded border-gray-300 focus:ring-blue-500"
              />
              Publish Immediately
            </label>
          </div>

          {/* Form Actions */}
          <div className="pt-4 border-t border-gray-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-xl text-xs transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              aria-busy={isSubmitting}
              className="inline-flex items-center gap-2 px-6 py-2.5 bg-[#2563EB] hover:bg-blue-700 text-white font-semibold rounded-xl text-xs transition-all shadow-sm active:scale-95 disabled:opacity-50"
            >
              <Send className="w-4 h-4" />
              {noticeToEdit ? 'Save Changes' : 'Publish Notice'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

import { useState, useEffect } from 'react';
import { X, Plus, Trash2, Calendar, FileText, Shield, Tag, Send } from 'lucide-react';
import { Notice, NoticeCategory, NoticePriority, NoticeVisibility, CreateNoticePayload, UpdateNoticePayload, NoticeAttachment } from '../../../models/notice.model';
import { useCreateNotice } from '../../../hooks/notices/useCreateNotice';
import { useUpdateNotice } from '../../../hooks/notices/useUpdateNotice';

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
  const [attName, setAttName] = useState('');
  const [attUrl, setAttUrl] = useState('');
  const [attType, setAttType] = useState('pdf');

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

  const handleAddAttachment = () => {
    if (!attName.trim() || !attUrl.trim()) return;
    const newAtt: NoticeAttachment = {
      id: `att-${Date.now()}`,
      name: attName.trim(),
      fileType: attType,
      fileSize: '1.5 MB',
      url: attUrl.trim(),
      isPreviewable: attType === 'pdf' || attType === 'image',
    };
    setAttachments([...attachments, newAtt]);
    setAttName('');
    setAttUrl('');
  };

  const handleRemoveAttachment = (id: string) => {
    setAttachments(attachments.filter((a) => a.id !== id));
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
        onSuccess: () => {
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
            {attachments.length > 0 && (
              <div className="space-y-2 mb-3">
                {attachments.map((att) => (
                  <div
                    key={att.id}
                    className="flex items-center justify-between p-2.5 bg-blue-50/50 border border-blue-100 rounded-xl text-xs"
                  >
                    <span className="font-semibold text-gray-800">{att.name}</span>
                    <button
                      type="button"
                      onClick={() => handleRemoveAttachment(att.id)}
                      className="text-red-500 hover:text-red-700 p-1"
                      aria-label={`Remove attachment ${att.name}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                ))}
              </div>
            )}

            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Attachment Name (e.g. Timetable.pdf)"
                value={attName}
                onChange={(e) => setAttName(e.target.value)}
                className="flex-1 px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-xs"
                aria-label="Attachment Title"
              />
              <input
                type="text"
                placeholder="File URL or Link"
                value={attUrl}
                onChange={(e) => setAttUrl(e.target.value)}
                className="flex-1 px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-xs"
                aria-label="Attachment URL"
              />
              <select
                value={attType}
                onChange={(e) => setAttType(e.target.value)}
                className="px-3 py-2 bg-gray-50 border border-gray-200 rounded-lg text-xs"
                aria-label="Attachment Type"
              >
                <option value="pdf">PDF</option>
                <option value="image">Image</option>
                <option value="doc">Document</option>
              </select>
              <button
                type="button"
                onClick={handleAddAttachment}
                className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold rounded-lg text-xs flex items-center gap-1"
              >
                <Plus className="w-3.5 h-3.5" /> Add
              </button>
            </div>
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

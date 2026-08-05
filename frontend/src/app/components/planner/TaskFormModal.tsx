import React, { useState, useEffect } from 'react';
import { X, Plus, Trash2, Paperclip, Tag, Calendar, AlertCircle } from 'lucide-react';
import { PlannerTask, TaskCategory, TaskPriority, TaskStatus, TaskAttachment } from '../../../models/planner.model';
import { CreateTaskDto, UpdateTaskDto } from '../../../sdk/planner/planner.dto';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';

interface TaskFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  taskToEdit?: PlannerTask | null;
  onSubmitCreate: (payload: CreateTaskDto) => void;
  onSubmitUpdate: (id: string, payload: UpdateTaskDto) => void;
  isSubmitting?: boolean;
}

const CATEGORY_OPTIONS: { value: TaskCategory; label: string }[] = [
  { value: 'ACADEMIC', label: 'Academic' },
  { value: 'ASSIGNMENT', label: 'Assignment' },
  { value: 'PROJECT', label: 'Project' },
  { value: 'STUDY_GOAL', label: 'Study Goal' },
  { value: 'EXAMINATION', label: 'Examination' },
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'REMINDER', label: 'Reminder' },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' },
];

const PRIORITY_OPTIONS: { value: TaskPriority; label: string }[] = [
  { value: 'URGENT', label: 'Urgent' },
  { value: 'HIGH', label: 'High' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'LOW', label: 'Low' },
];

const STATUS_OPTIONS: { value: TaskStatus; label: string }[] = [
  { value: 'TODO', label: 'To Do' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'ARCHIVED', label: 'Archived' },
];

export const TaskFormModal: React.FC<TaskFormModalProps> = ({
  isOpen,
  onClose,
  taskToEdit,
  onSubmitCreate,
  onSubmitUpdate,
  isSubmitting = false,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<TaskCategory>('PERSONAL');
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [dueDate, setDueDate] = useState('');
  const [progress, setProgress] = useState(0);
  const [tagsInput, setTagsInput] = useState('');
  const [attachments, setAttachments] = useState<TaskAttachment[]>([]);

  // New Attachment inputs
  const [newAttName, setNewAttName] = useState('');
  const [newAttUrl, setNewAttUrl] = useState('');

  useEffect(() => {
    if (taskToEdit) {
      setTitle(taskToEdit.title);
      setDescription(taskToEdit.description || '');
      setCategory(taskToEdit.category);
      setPriority(taskToEdit.priority);
      setStatus(taskToEdit.status);
      setDueDate(taskToEdit.dueDate ? taskToEdit.dueDate.split('T')[0] : '');
      setProgress(taskToEdit.progress);
      setTagsInput(taskToEdit.tags.join(', '));
      setAttachments(taskToEdit.attachments || []);
    } else {
      setTitle('');
      setDescription('');
      setCategory('PERSONAL');
      setPriority('MEDIUM');
      setStatus('TODO');
      setDueDate('');
      setProgress(0);
      setTagsInput('');
      setAttachments([]);
    }
  }, [taskToEdit, isOpen]);

  const handleAddAttachment = () => {
    if (!newAttName.trim() || !newAttUrl.trim()) return;
    setAttachments([
      ...attachments,
      {
        id: `att-${Date.now()}`,
        name: newAttName.trim(),
        url: newAttUrl.trim(),
        size: '1.0 MB',
      },
    ]);
    setNewAttName('');
    setNewAttUrl('');
  };

  const handleRemoveAttachment = (index: number) => {
    setAttachments(attachments.filter((_, i) => i !== index));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) return;

    const tags = tagsInput
      .split(',')
      .map((t) => t.trim().toLowerCase())
      .filter((t) => t.length > 0);

    if (taskToEdit) {
      onSubmitUpdate(taskToEdit.id, {
        title: title.trim(),
        description: description.trim() || undefined,
        category,
        priority,
        status,
        dueDate: dueDate || undefined,
        progress,
        tags,
        attachments,
        isCompleted: status === 'COMPLETED' || progress === 100,
        isArchived: status === 'ARCHIVED',
      });
    } else {
      onSubmitCreate({
        title: title.trim(),
        description: description.trim() || undefined,
        category,
        priority,
        status,
        dueDate: dueDate || undefined,
        progress,
        tags,
        attachments,
      });
    }
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl bg-white p-6 rounded-3xl shadow-2xl overflow-y-auto max-h-[90vh]">
        <DialogHeader className="flex items-center justify-between border-b border-gray-100 pb-4">
          <DialogTitle className="text-xl font-bold text-gray-900">
            {taskToEdit ? 'Edit Task' : 'Create New Task'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-5 pt-2">
          {/* Title */}
          <div className="space-y-1.5">
            <label htmlFor="task-title" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Task Title <span className="text-red-500">*</span>
            </label>
            <input
              id="task-title"
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Complete CS-301 Algorithm Analysis Report"
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label htmlFor="task-description" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Description
            </label>
            <textarea
              id="task-description"
              rows={3}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Add key details, links, or guidelines for this task..."
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all"
            />
          </div>

          {/* Category, Priority, Status Row */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {/* Category */}
            <div className="space-y-1.5">
              <label htmlFor="task-category" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Category
              </label>
              <select
                id="task-category"
                value={category}
                onChange={(e) => setCategory(e.target.value as TaskCategory)}
                className="w-full px-3 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              >
                {CATEGORY_OPTIONS.map((c) => (
                  <option key={c.value} value={c.value}>
                    {c.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Priority */}
            <div className="space-y-1.5">
              <label htmlFor="task-priority" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Priority
              </label>
              <select
                id="task-priority"
                value={priority}
                onChange={(e) => setPriority(e.target.value as TaskPriority)}
                className="w-full px-3 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              >
                {PRIORITY_OPTIONS.map((p) => (
                  <option key={p.value} value={p.value}>
                    {p.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Status */}
            <div className="space-y-1.5">
              <label htmlFor="task-status" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Status
              </label>
              <select
                id="task-status"
                value={status}
                onChange={(e) => {
                  const newStatus = e.target.value as TaskStatus;
                  setStatus(newStatus);
                  if (newStatus === 'COMPLETED') setProgress(100);
                }}
                className="w-full px-3 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500/20"
              >
                {STATUS_OPTIONS.map((s) => (
                  <option key={s.value} value={s.value}>
                    {s.label}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Due Date & Progress Slider */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label htmlFor="task-due-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Due Date
              </label>
              <div className="relative">
                <input
                  id="task-due-date"
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs font-bold text-gray-700 uppercase tracking-wider">
                <label htmlFor="task-progress" className="cursor-pointer">Completion Progress</label>
                <span className="text-blue-600 font-bold">{progress}%</span>
              </div>
              <input
                id="task-progress"
                type="range"
                min="0"
                max="100"
                step="5"
                value={progress}
                onChange={(e) => {
                  const val = Number(e.target.value);
                  setProgress(val);
                  if (val === 100) setStatus('COMPLETED');
                  else if (val > 0 && status === 'TODO') setStatus('IN_PROGRESS');
                }}
                className="w-full accent-blue-600 h-2 bg-gray-200 rounded-lg cursor-pointer mt-3"
              />
            </div>
          </div>

          {/* Tags */}
          <div className="space-y-1.5">
            <label htmlFor="task-tags" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Tags (Comma separated)
            </label>
            <input
              id="task-tags"
              type="text"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              placeholder="e.g. cs301, homework, urgent"
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            />
          </div>

          {/* Attachments Section */}
          <div className="space-y-2 pt-2 border-t border-gray-100">
            <span className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Attachments (Optional)
            </span>

            {attachments.length > 0 && (
              <div className="space-y-2 max-h-36 overflow-y-auto pr-1">
                {attachments.map((att, index) => (
                  <div key={index} className="flex items-center justify-between p-2.5 bg-gray-50 rounded-xl border border-gray-200 text-xs">
                    <div className="flex items-center gap-2 truncate">
                      <Paperclip className="w-3.5 h-3.5 text-gray-400 shrink-0" />
                      <span className="font-semibold text-gray-800 truncate">{att.name}</span>
                      <span className="text-gray-400 text-[10px]">({att.size || '1 MB'})</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveAttachment(index)}
                      className="p-1 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                      aria-label={`Remove attachment ${att.name}`}
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                ))}
              </div>
            )}

            <div className="flex items-center gap-2">
              <input
                type="text"
                placeholder="File Title (e.g. Draft Report)"
                value={newAttName}
                onChange={(e) => setNewAttName(e.target.value)}
                className="flex-1 px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs"
                aria-label="Attachment Title"
              />
              <input
                type="url"
                placeholder="File URL (https://...)"
                value={newAttUrl}
                onChange={(e) => setNewAttUrl(e.target.value)}
                className="flex-1 px-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs"
                aria-label="Attachment URL"
              />
              <button
                type="button"
                onClick={handleAddAttachment}
                className="px-3 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-semibold shrink-0"
              >
                + Add
              </button>
            </div>
          </div>

          {/* Form Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-semibold text-sm transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting || !title.trim()}
              aria-busy={isSubmitting}
              className="px-6 py-2.5 bg-[#2563EB] hover:bg-blue-700 text-white rounded-xl font-semibold text-sm transition-all shadow-md hover:shadow-blue-200 disabled:opacity-50"
            >
              {isSubmitting ? 'Saving...' : taskToEdit ? 'Save Changes' : 'Create Task'}
            </button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

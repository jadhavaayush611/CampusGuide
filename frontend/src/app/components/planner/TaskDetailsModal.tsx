import React from 'react';
import {
  Calendar,
  CheckCircle2,
  Clock,
  ExternalLink,
  Paperclip,
  Tag,
  AlertCircle,
  Archive,
  RotateCcw,
  Trash2,
  Edit,
} from 'lucide-react';
import { PlannerTask } from '../../../models/planner.model';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';

interface TaskDetailsModalProps {
  isOpen: boolean;
  task: PlannerTask | null;
  onClose: () => void;
  onEdit: (task: PlannerTask) => void;
  onMarkComplete: (id: string, completed: boolean) => void;
  onUpdateProgress: (id: string, progress: number) => void;
  onArchive: (id: string) => void;
  onRestore: (id: string) => void;
  onDelete: (id: string) => void;
}

export const TaskDetailsModal: React.FC<TaskDetailsModalProps> = ({
  isOpen,
  task,
  onClose,
  onEdit,
  onMarkComplete,
  onUpdateProgress,
  onArchive,
  onRestore,
  onDelete,
}) => {
  if (!task) return null;

  const todayStr = new Date().toISOString().split('T')[0];
  const isOverdue = task.dueDate && task.dueDate.split('T')[0] < todayStr && !task.isCompleted && !task.isArchived;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-2xl bg-white p-6 rounded-3xl shadow-2xl overflow-y-auto max-h-[90vh] space-y-6">
        <DialogHeader className="flex items-center justify-between border-b border-gray-100 pb-4">
          <div className="flex flex-wrap items-center gap-2">
            <span className="px-3 py-1 bg-blue-50 text-blue-700 font-bold rounded-full text-xs border border-blue-200">
              {task.category.replace('_', ' ')}
            </span>
            <span className="px-2.5 py-0.5 bg-gray-900 text-white font-extrabold rounded-full text-[10px] tracking-wider">
              {task.priority} PRIORITY
            </span>
            {isOverdue && (
              <span className="inline-flex items-center gap-1 px-2.5 py-0.5 bg-red-100 text-red-700 font-bold rounded-full text-xs">
                <AlertCircle className="w-3.5 h-3.5" aria-hidden="true" /> Overdue
              </span>
            )}
          </div>
        </DialogHeader>

        {/* Title & Complete Toggle */}
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-1">
            <DialogTitle className={`text-2xl font-extrabold text-gray-900 ${task.isCompleted ? 'line-through text-gray-500' : ''}`}>
              {task.title}
            </DialogTitle>
            <div className="flex items-center gap-4 text-xs text-gray-500">
              {task.dueDate && (
                <div className="flex items-center gap-1.5 font-medium">
                  <Calendar className="w-4 h-4 text-blue-600" aria-hidden="true" />
                  <span>Due: {task.dueDate.split('T')[0]}</span>
                </div>
              )}
              <div className="flex items-center gap-1.5">
                <Clock className="w-4 h-4 text-gray-400" aria-hidden="true" />
                <span>Created: {new Date(task.createdDate).toLocaleDateString()}</span>
              </div>
            </div>
          </div>

          <button
            onClick={() => onMarkComplete(task.id, !task.isCompleted)}
            className={`px-4 py-2 rounded-xl font-semibold text-xs transition-all flex items-center gap-2 shadow-xs shrink-0 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 ${
              task.isCompleted
                ? 'bg-emerald-100 text-emerald-800 hover:bg-emerald-200'
                : 'bg-blue-50 text-[#2563EB] hover:bg-blue-100'
            }`}
          >
            <CheckCircle2 className="w-4 h-4" aria-hidden="true" />
            {task.isCompleted ? 'Completed' : 'Mark Complete'}
          </button>
        </div>

        {/* Description */}
        <div className="space-y-2 bg-gray-50 p-4 rounded-2xl border border-gray-100">
          <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Description</h4>
          <p className="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">
            {task.description || 'No detailed description provided for this task.'}
          </p>
        </div>

        {/* Progress Adjuster */}
        <div className="space-y-2 bg-gray-50 p-4 rounded-2xl border border-gray-100">
          <div className="flex items-center justify-between text-xs font-bold text-gray-700">
            <span>Completion Progress</span>
            <span className="text-blue-600 font-extrabold text-sm">{task.progress}%</span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            step="5"
            aria-label="Completion Progress"
            value={task.progress}
            onChange={(e) => onUpdateProgress(task.id, Number(e.target.value))}
            className="w-full accent-blue-600 h-2.5 bg-gray-200 rounded-lg cursor-pointer focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
          />
          <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden mt-1">
            <div
              className={`h-full rounded-full transition-all duration-300 ${
                task.isCompleted ? 'bg-emerald-500' : 'bg-[#2563EB]'
              }`}
              style={{ width: `${task.progress}%` }}
            />
          </div>
        </div>

        {/* Tags */}
        {task.tags && task.tags.length > 0 && (
          <div className="space-y-2">
            <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Tags</h4>
            <div className="flex flex-wrap gap-2">
              {task.tags.map((tag) => (
                <span key={tag} className="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 rounded-lg text-xs font-semibold">
                  <Tag className="w-3 h-3 text-gray-400" aria-hidden="true" />
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Attachments */}
        {task.attachments && task.attachments.length > 0 && (
          <div className="space-y-2">
            <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">Attachments</h4>
            <div className="space-y-2">
              {task.attachments.map((att, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded-2xl border border-gray-200 text-xs">
                  <div className="flex items-center gap-2 truncate">
                    <Paperclip className="w-4 h-4 text-blue-500 shrink-0" aria-hidden="true" />
                    <span className="font-bold text-gray-800 truncate">{att.name}</span>
                    <span className="text-gray-400">({att.size || '1 MB'})</span>
                  </div>
                  <a
                    href={att.url}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex items-center gap-1 px-3 py-1.5 bg-blue-50 text-blue-600 hover:bg-blue-100 rounded-lg font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  >
                    <span>View File</span>
                    <ExternalLink className="w-3 h-3" aria-hidden="true" />
                  </a>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Modal Actions Footer */}
        <div className="flex flex-wrap items-center justify-between gap-3 pt-4 border-t border-gray-100">
          <div className="flex items-center gap-2">
            {task.isArchived ? (
              <button
                onClick={() => {
                  onRestore(task.id);
                  onClose();
                }}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-xl text-xs font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
              >
                <RotateCcw className="w-4 h-4" aria-hidden="true" /> Restore
              </button>
            ) : (
              <button
                onClick={() => {
                  onArchive(task.id);
                  onClose();
                }}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-amber-50 text-amber-700 hover:bg-amber-100 rounded-xl text-xs font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-amber-600 focus-visible:ring-offset-1"
              >
                <Archive className="w-4 h-4" aria-hidden="true" /> Archive
              </button>
            )}
            <button
              onClick={() => {
                onDelete(task.id);
                onClose();
              }}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 bg-red-50 text-red-600 hover:bg-red-100 rounded-xl text-xs font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-red-600 focus-visible:ring-offset-1"
            >
              <Trash2 className="w-4 h-4" aria-hidden="true" /> Delete
            </button>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                onEdit(task);
                onClose();
              }}
              className="inline-flex items-center gap-1.5 px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded-xl text-xs font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-gray-600 focus-visible:ring-offset-1"
            >
              <Edit className="w-4 h-4" aria-hidden="true" /> Edit Task
            </button>
            <button
              onClick={onClose}
              className="px-5 py-2 bg-gray-900 hover:bg-gray-800 text-white rounded-xl text-xs font-semibold transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-gray-600 focus-visible:ring-offset-1"
            >
              Close
            </button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
};

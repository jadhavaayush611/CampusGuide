import React, { memo, useCallback, useMemo } from 'react';
import {
  Calendar,
  CheckCircle2,
  Clock,
  MoreVertical,
  Paperclip,
  Tag,
  AlertCircle,
  Archive,
  RotateCcw,
  Trash2,
  Edit,
  Minus,
  Plus,
} from 'lucide-react';
import { PlannerTask, TaskCategory, TaskPriority } from '../../../models/planner.model';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  DropdownMenuSeparator,
} from '../ui/dropdown-menu';

interface TaskCardProps {
  task: PlannerTask;
  viewMode?: 'grid' | 'list';
  onSelect: (task: PlannerTask) => void;
  onEdit: (task: PlannerTask) => void;
  onMarkComplete: (id: string, completed: boolean) => void;
  onUpdateProgress: (id: string, progress: number) => void;
  onArchive: (id: string) => void;
  onRestore: (id: string) => void;
  onDelete: (id: string) => void;
}

const CATEGORY_STYLES: Record<TaskCategory, { bg: string; text: string; border: string }> = {
  ACADEMIC: { bg: 'bg-blue-50', text: 'text-blue-700', border: 'border-blue-200' },
  ASSIGNMENT: { bg: 'bg-purple-50', text: 'text-purple-700', border: 'border-purple-200' },
  PROJECT: { bg: 'bg-indigo-50', text: 'text-indigo-700', border: 'border-indigo-200' },
  STUDY_GOAL: { bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200' },
  EXAMINATION: { bg: 'bg-rose-50', text: 'text-rose-700', border: 'border-rose-200' },
  PERSONAL: { bg: 'bg-amber-50', text: 'text-amber-700', border: 'border-amber-200' },
  REMINDER: { bg: 'bg-cyan-50', text: 'text-cyan-700', border: 'border-cyan-200' },
  MISCELLANEOUS: { bg: 'bg-gray-100', text: 'text-gray-700', border: 'border-gray-200' },
};

const PRIORITY_STYLES: Record<TaskPriority, { bg: string; text: string }> = {
  URGENT: { bg: 'bg-red-500', text: 'text-white' },
  HIGH: { bg: 'bg-amber-500', text: 'text-white' },
  MEDIUM: { bg: 'bg-blue-500', text: 'text-white' },
  LOW: { bg: 'bg-gray-400', text: 'text-white' },
};

export const TaskCard: React.FC<TaskCardProps> = memo(function TaskCard({
  task,
  viewMode = 'grid',
  onSelect,
  onEdit,
  onMarkComplete,
  onUpdateProgress,
  onArchive,
  onRestore,
  onDelete,
}) {
  const { isOverdue, isDueToday } = useMemo(() => {
    const todayStr = new Date().toISOString().split('T')[0];
    const overdue = task.dueDate && task.dueDate.split('T')[0] < todayStr && !task.isCompleted && !task.isArchived;
    const dueToday = task.dueDate && task.dueDate.split('T')[0] === todayStr && !task.isCompleted;
    return { isOverdue: Boolean(overdue), isDueToday: Boolean(dueToday) };
  }, [task.dueDate, task.isCompleted, task.isArchived]);

  const categoryStyle = CATEGORY_STYLES[task.category] || CATEGORY_STYLES.MISCELLANEOUS;
  const priorityStyle = PRIORITY_STYLES[task.priority] || PRIORITY_STYLES.MEDIUM;

  const handleCardClick = useCallback(() => {
    onSelect(task);
  }, [onSelect, task]);

  const handleCheckboxClick = useCallback(
    (e: React.MouseEvent) => {
      e.stopPropagation();
      onMarkComplete(task.id, !task.isCompleted);
    },
    [onMarkComplete, task.id, task.isCompleted]
  );

  const handleStepProgress = useCallback(
    (e: React.MouseEvent, delta: number) => {
      e.stopPropagation();
      const newProgress = Math.min(100, Math.max(0, task.progress + delta));
      onUpdateProgress(task.id, newProgress);
    },
    [onUpdateProgress, task.id, task.progress]
  );

  const handleEdit = useCallback(() => {
    onEdit(task);
  }, [onEdit, task]);

  const handleToggleComplete = useCallback(() => {
    onMarkComplete(task.id, !task.isCompleted);
  }, [onMarkComplete, task.id, task.isCompleted]);

  const handleRestore = useCallback(() => {
    onRestore(task.id);
  }, [onRestore, task.id]);

  const handleArchive = useCallback(() => {
    onArchive(task.id);
  }, [onArchive, task.id]);

  const handleDelete = useCallback(() => {
    onDelete(task.id);
  }, [onDelete, task.id]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      if (e.target === e.currentTarget) {
        e.preventDefault();
        handleCardClick();
      }
    }
  }, [handleCardClick]);

  if (viewMode === 'list') {
    return (
      <div
        onClick={handleCardClick}
        onKeyDown={handleKeyDown}
        role="button"
        tabIndex={0}
        aria-label={`Task: ${task.title}, Category: ${task.category.replace('_', ' ')}, Priority: ${task.priority}, Progress: ${task.progress}%${task.dueDate ? `, Due: ${task.dueDate.split('T')[0]}` : ''}`}
        className={`group p-4 bg-white hover:bg-gray-50/80 rounded-2xl border transition-all duration-200 cursor-pointer flex flex-col sm:flex-row sm:items-center gap-4 shadow-xs hover:shadow-md focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2 ${
          task.isCompleted ? 'border-gray-200 opacity-75' : isOverdue ? 'border-red-300 bg-red-50/20' : 'border-gray-100'
        }`}
      >
        {/* Quick Complete Checkbox */}
        <button
          onClick={handleCheckboxClick}
          className="p-1 text-gray-400 hover:text-blue-600 transition-colors self-start sm:self-center focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 rounded-full"
          title={task.isCompleted ? 'Mark as Incomplete' : 'Mark as Complete'}
          aria-label={task.isCompleted ? 'Mark as Incomplete' : 'Mark as Complete'}
        >
          {task.isCompleted ? (
            <CheckCircle2 className="w-5 h-5 text-emerald-500 fill-emerald-50" aria-hidden="true" />
          ) : (
            <div className="w-5 h-5 rounded-full border-2 border-gray-300 group-hover:border-blue-500 transition-colors" />
          )}
        </button>

        {/* Content */}
        <div className="flex-1 min-w-0 space-y-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${categoryStyle.bg} ${categoryStyle.text} ${categoryStyle.border}`}>
              {task.category.replace('_', ' ')}
            </span>
            <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold tracking-wider ${priorityStyle.bg} ${priorityStyle.text}`}>
              {task.priority}
            </span>
            {isOverdue && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700">
                <AlertCircle className="w-3 h-3" aria-hidden="true" /> Overdue
              </span>
            )}
            {isDueToday && (
              <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                <Clock className="w-3 h-3" aria-hidden="true" /> Due Today
              </span>
            )}
          </div>

          <h4 className={`text-base font-bold text-gray-900 truncate ${task.isCompleted ? 'line-through text-gray-500' : ''}`}>
            {task.title}
          </h4>

          {task.description && (
            <p className="text-xs text-gray-500 line-clamp-1">{task.description}</p>
          )}
        </div>

        {/* Progress & Due Date */}
        <div className="flex items-center justify-between sm:justify-end gap-6 text-xs text-gray-500">
          <div className="flex items-center gap-2 w-32">
            <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all duration-300 ${
                  task.isCompleted ? 'bg-emerald-500' : 'bg-blue-600'
                }`}
                style={{ width: `${task.progress}%` }}
              />
            </div>
            <span className="font-semibold text-gray-700">{task.progress}%</span>
          </div>

          {task.dueDate && (
            <div className="flex items-center gap-1 min-w-[100px]">
              <Calendar className="w-3.5 h-3.5 text-gray-400" aria-hidden="true" />
              <span>{task.dueDate.split('T')[0]}</span>
            </div>
          )}

          {/* Action Menu */}
          <div onClick={(e) => e.stopPropagation()}>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <button
                  className="p-1 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  aria-label="More actions"
                  title="More actions"
                >
                  <MoreVertical className="w-4 h-4" aria-hidden="true" />
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-44">
                <DropdownMenuItem onClick={handleEdit}>
                  <Edit className="w-4 h-4 mr-2" aria-hidden="true" /> Edit Task
                </DropdownMenuItem>
                <DropdownMenuItem onClick={handleToggleComplete}>
                  <CheckCircle2 className="w-4 h-4 mr-2 text-emerald-500" aria-hidden="true" />
                  {task.isCompleted ? 'Mark Pending' : 'Mark Complete'}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {task.isArchived ? (
                  <DropdownMenuItem onClick={handleRestore}>
                    <RotateCcw className="w-4 h-4 mr-2 text-blue-500" aria-hidden="true" /> Restore Task
                  </DropdownMenuItem>
                ) : (
                  <DropdownMenuItem onClick={handleArchive}>
                    <Archive className="w-4 h-4 mr-2 text-amber-500" aria-hidden="true" /> Archive Task
                  </DropdownMenuItem>
                )}
                <DropdownMenuItem onClick={handleDelete} className="text-red-600 focus:text-red-600">
                  <Trash2 className="w-4 h-4 mr-2" aria-hidden="true" /> Delete Task
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div
      onClick={handleCardClick}
      onKeyDown={handleKeyDown}
      role="button"
      tabIndex={0}
      aria-label={`Task: ${task.title}, Category: ${task.category.replace('_', ' ')}, Priority: ${task.priority}, Progress: ${task.progress}%${task.dueDate ? `, Due: ${task.dueDate.split('T')[0]}` : ''}`}
      className={`group relative p-5 bg-white hover:bg-gray-50/50 rounded-2xl border transition-all duration-200 cursor-pointer flex flex-col justify-between space-y-4 shadow-sm hover:shadow-md focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2 ${
        task.isCompleted ? 'border-gray-200 opacity-80' : isOverdue ? 'border-red-300 bg-red-50/10' : 'border-gray-100'
      }`}
    >
      {/* Card Header: Category Badge, Priority Badge & Action Menu */}
      <div className="flex items-center justify-between gap-2">
        <div className="flex flex-wrap items-center gap-1.5">
          <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${categoryStyle.bg} ${categoryStyle.text} ${categoryStyle.border}`}>
            {task.category.replace('_', ' ')}
          </span>
          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold tracking-wider ${priorityStyle.bg} ${priorityStyle.text}`}>
            {task.priority}
          </span>
          {isOverdue && (
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-100 text-red-700">
              <AlertCircle className="w-3 h-3" aria-hidden="true" /> Overdue
            </span>
          )}
        </div>

        <div onClick={(e) => e.stopPropagation()}>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="p-1.5 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                aria-label="More options"
                title="More options"
              >
                <MoreVertical className="w-4 h-4" aria-hidden="true" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-44">
              <DropdownMenuItem onClick={handleEdit}>
                <Edit className="w-4 h-4 mr-2" aria-hidden="true" /> Edit Task
              </DropdownMenuItem>
              <DropdownMenuItem onClick={handleToggleComplete}>
                <CheckCircle2 className="w-4 h-4 mr-2 text-emerald-500" aria-hidden="true" />
                {task.isCompleted ? 'Mark Pending' : 'Mark Complete'}
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              {task.isArchived ? (
                <DropdownMenuItem onClick={handleRestore}>
                  <RotateCcw className="w-4 h-4 mr-2 text-blue-500" aria-hidden="true" /> Restore Task
                </DropdownMenuItem>
              ) : (
                <DropdownMenuItem onClick={handleArchive}>
                  <Archive className="w-4 h-4 mr-2 text-amber-500" aria-hidden="true" /> Archive Task
                </DropdownMenuItem>
              )}
              <DropdownMenuItem onClick={handleDelete} className="text-red-600 focus:text-red-600">
                <Trash2 className="w-4 h-4 mr-2" aria-hidden="true" /> Delete Task
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Main Title & Description */}
      <div className="space-y-2 flex-1">
        <div className="flex items-start gap-2.5">
          <button
            onClick={handleCheckboxClick}
            className="mt-0.5 text-gray-400 hover:text-blue-600 transition-colors shrink-0 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1 rounded-full"
            title={task.isCompleted ? 'Mark Incomplete' : 'Mark Complete'}
            aria-label={task.isCompleted ? 'Mark Incomplete' : 'Mark Complete'}
          >
            {task.isCompleted ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-500 fill-emerald-50" aria-hidden="true" />
            ) : (
              <div className="w-5 h-5 rounded-full border-2 border-gray-300 group-hover:border-blue-500 transition-colors" />
            )}
          </button>
          <h3 className={`text-base font-bold text-gray-900 leading-snug ${task.isCompleted ? 'line-through text-gray-500' : ''}`}>
            {task.title}
          </h3>
        </div>

        {task.description && (
          <p className="text-xs text-gray-500 line-clamp-2 pl-7 leading-relaxed">
            {task.description}
          </p>
        )}
      </div>

      {/* Tags & Attachments Counter */}
      {(task.tags.length > 0 || (task.attachments && task.attachments.length > 0)) && (
        <div className="flex flex-wrap items-center gap-2 pt-1 pl-7">
          {task.tags.map((tag) => (
            <span key={tag} className="inline-flex items-center gap-1 px-2 py-0.5 bg-gray-100 text-gray-600 rounded-md text-[10px] font-medium">
              <Tag className="w-2.5 h-2.5 text-gray-400" aria-hidden="true" />
              {tag}
            </span>
          ))}
          {task.attachments && task.attachments.length > 0 && (
            <span className="inline-flex items-center gap-1 text-[10px] text-gray-500 font-medium">
              <Paperclip className="w-3 h-3 text-gray-400" aria-hidden="true" />
              {task.attachments.length} {task.attachments.length === 1 ? 'file' : 'files'}
            </span>
          )}
        </div>
      )}

      {/* Progress Bar & Interactive Controls */}
      <div className="space-y-1.5 pt-2 border-t border-gray-100">
        <div className="flex items-center justify-between text-xs">
          <span className="font-medium text-gray-500">Progress</span>
          <div className="flex items-center gap-1.5" onClick={(e) => e.stopPropagation()}>
            <button
              onClick={(e) => handleStepProgress(e, -10)}
              disabled={task.progress <= 0}
              className="p-0.5 text-gray-400 hover:text-gray-700 disabled:opacity-30 rounded focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
              title="Decrease Progress"
              aria-label="Decrease Progress"
            >
              <Minus className="w-3 h-3" aria-hidden="true" />
            </button>
            <span className="font-bold text-gray-800 w-8 text-right">{task.progress}%</span>
            <button
              onClick={(e) => handleStepProgress(e, 10)}
              disabled={task.progress >= 100}
              className="p-0.5 text-gray-400 hover:text-gray-700 disabled:opacity-30 rounded focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
              title="Increase Progress"
              aria-label="Increase Progress"
            >
              <Plus className="w-3 h-3" aria-hidden="true" />
            </button>
          </div>
        </div>
        <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-300 ${
              task.isCompleted ? 'bg-emerald-500' : 'bg-[#2563EB]'
            }`}
            style={{ width: `${task.progress}%` }}
          />
        </div>
      </div>

      {/* Footer: Due Date */}
      {task.dueDate && (
        <div className="flex items-center justify-between pt-2 text-xs text-gray-500">
          <div className={`flex items-center gap-1.5 ${isOverdue ? 'text-red-600 font-semibold' : ''}`}>
            <Calendar className="w-3.5 h-3.5 text-gray-400" aria-hidden="true" />
            <span>Due {task.dueDate.split('T')[0]}</span>
          </div>
          {task.completedDate && (
            <span className="text-[10px] text-emerald-600 font-medium">
              Done {task.completedDate.split('T')[0]}
            </span>
          )}
        </div>
      )}
    </div>
  );
});

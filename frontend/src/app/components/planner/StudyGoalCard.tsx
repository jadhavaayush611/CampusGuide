import React, { memo, useCallback, useMemo } from 'react';
import { Target, Calendar, Edit, Trash2 } from 'lucide-react';
import { StudyGoal } from '../../../models/planner.model';

interface StudyGoalCardProps {
  goal: StudyGoal;
  onLogHours: (goal: StudyGoal, hours: number) => void;
  onEdit: (goal: StudyGoal) => void;
  onDelete: (id: string) => void;
}

export const StudyGoalCard: React.FC<StudyGoalCardProps> = memo(function StudyGoalCard({
  goal,
  onLogHours,
  onEdit,
  onDelete,
}) {
  const { percent, isCompleted } = useMemo(() => {
    const p = goal.targetHours > 0 ? Math.min(100, Math.round((goal.completedHours / goal.targetHours) * 100)) : 0;
    const completed = goal.isCompleted || p >= 100;
    return { percent: p, isCompleted: completed };
  }, [goal.targetHours, goal.completedHours, goal.isCompleted]);

  const handleEdit = useCallback(() => {
    onEdit(goal);
  }, [onEdit, goal]);

  const handleDelete = useCallback(() => {
    onDelete(goal.id);
  }, [onDelete, goal.id]);

  const handleLogOneHour = useCallback(() => {
    onLogHours(goal, 1);
  }, [onLogHours, goal]);

  const handleLogTwoHours = useCallback(() => {
    onLogHours(goal, 2);
  }, [onLogHours, goal]);

  return (
    <div className={`p-5 bg-white rounded-2xl border transition-all duration-200 space-y-4 shadow-sm hover:shadow-md ${
      isCompleted ? 'border-emerald-200 bg-emerald-50/10' : 'border-gray-100'
    }`}>
      {/* Header */}
      <div className="flex items-center justify-between gap-2">
        <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
          {goal.category || 'Study Goal'}
        </span>
        <div className="flex items-center gap-1">
          <button
            onClick={handleEdit}
            className="p-1 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-emerald-600 focus-visible:ring-offset-1"
            title="Edit Goal"
            aria-label="Edit Goal"
          >
            <Edit className="w-3.5 h-3.5" aria-hidden="true" />
          </button>
          <button
            onClick={handleDelete}
            className="p-1 text-gray-400 hover:text-red-600 rounded-lg hover:bg-red-50 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-red-500 focus-visible:ring-offset-1"
            title="Delete Goal"
            aria-label="Delete Goal"
          >
            <Trash2 className="w-3.5 h-3.5" aria-hidden="true" />
          </button>
        </div>
      </div>

      {/* Goal Title */}
      <div className="space-y-1">
        <div className="flex items-center gap-2">
          <Target className="w-5 h-5 text-emerald-600 shrink-0" aria-hidden="true" />
          <h3 className={`text-base font-bold text-gray-900 ${isCompleted ? 'line-through text-gray-500' : ''}`}>
            {goal.title}
          </h3>
        </div>
        {goal.description && (
          <p className="text-xs text-gray-500 line-clamp-2 pl-7">{goal.description}</p>
        )}
      </div>

      {/* Hours Progress Bar */}
      <div className="space-y-1.5 pt-1">
        <div className="flex items-center justify-between text-xs">
          <span className="font-semibold text-gray-600">Hours Completed</span>
          <span className="font-extrabold text-emerald-700">
            {goal.completedHours} / {goal.targetHours} hrs ({percent}%)
          </span>
        </div>
        <div className="w-full bg-gray-100 h-2.5 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all duration-300 ${
              isCompleted ? 'bg-emerald-500' : 'bg-emerald-600'
            }`}
            style={{ width: `${percent}%` }}
          />
        </div>
      </div>

      {/* Footer & Log Hours Quick Buttons */}
      <div className="flex flex-wrap items-center justify-between gap-2 pt-2 border-t border-gray-100 text-xs">
        {goal.deadline ? (
          <div className="flex items-center gap-1 text-gray-500">
            <Calendar className="w-3.5 h-3.5 text-gray-400" aria-hidden="true" />
            <span>Target: {goal.deadline}</span>
          </div>
        ) : (
          <span />
        )}

        <div className="flex items-center gap-1.5">
          <span className="text-[11px] text-gray-400 font-medium">Log:</span>
          <button
            onClick={handleLogOneHour}
            disabled={isCompleted}
            aria-label="Log 1 hour completed"
            className="px-2 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg text-xs font-bold transition-colors disabled:opacity-50 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-emerald-600 focus-visible:ring-offset-1"
          >
            +1h
          </button>
          <button
            onClick={handleLogTwoHours}
            disabled={isCompleted}
            aria-label="Log 2 hours completed"
            className="px-2 py-1 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 rounded-lg text-xs font-bold transition-colors disabled:opacity-50 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-emerald-600 focus-visible:ring-offset-1"
          >
            +2h
          </button>
        </div>
      </div>
    </div>
  );
});

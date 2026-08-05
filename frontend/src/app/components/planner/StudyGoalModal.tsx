import React, { useState, useEffect } from 'react';
import { Target } from 'lucide-react';
import { StudyGoal } from '../../../models/planner.model';
import { CreateStudyGoalDto } from '../../../sdk/planner/planner.dto';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../ui/dialog';

interface StudyGoalModalProps {
  isOpen: boolean;
  onClose: () => void;
  goalToEdit?: StudyGoal | null;
  onSubmitCreate: (payload: CreateStudyGoalDto) => void;
  onSubmitUpdate: (id: string, payload: Partial<CreateStudyGoalDto> & { completedHours?: number; isCompleted?: boolean }) => void;
  isSubmitting?: boolean;
}

export const StudyGoalModal: React.FC<StudyGoalModalProps> = ({
  isOpen,
  onClose,
  goalToEdit,
  onSubmitCreate,
  onSubmitUpdate,
  isSubmitting = false,
}) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [targetHours, setTargetHours] = useState(10);
  const [completedHours, setCompletedHours] = useState(0);
  const [category, setCategory] = useState('Exam Prep');
  const [deadline, setDeadline] = useState('');

  useEffect(() => {
    if (goalToEdit) {
      setTitle(goalToEdit.title);
      setDescription(goalToEdit.description || '');
      setTargetHours(goalToEdit.targetHours);
      setCompletedHours(goalToEdit.completedHours);
      setCategory(goalToEdit.category || 'Exam Prep');
      setDeadline(goalToEdit.deadline || '');
    } else {
      setTitle('');
      setDescription('');
      setTargetHours(10);
      setCompletedHours(0);
      setCategory('Exam Prep');
      setDeadline('');
    }
  }, [goalToEdit, isOpen]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || targetHours <= 0) return;

    if (goalToEdit) {
      onSubmitUpdate(goalToEdit.id, {
        title: title.trim(),
        description: description.trim() || undefined,
        targetHours,
        completedHours,
        category: category.trim() || 'General',
        deadline: deadline || undefined,
        isCompleted: completedHours >= targetHours,
      });
    } else {
      onSubmitCreate({
        title: title.trim(),
        description: description.trim() || undefined,
        targetHours,
        category: category.trim() || 'General',
        deadline: deadline || undefined,
      });
    }
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-lg bg-white p-6 rounded-3xl shadow-2xl space-y-4">
        <DialogHeader className="flex items-center justify-between border-b border-gray-100 pb-3">
          <DialogTitle className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <Target className="w-5 h-5 text-emerald-600" />
            {goalToEdit ? 'Edit Study Goal' : 'Create New Study Goal'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 pt-1">
          <div className="space-y-1">
            <label htmlFor="goal-title" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Goal Title <span className="text-red-500">*</span>
            </label>
            <input
              id="goal-title"
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Algorithms Midterm Prep"
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="space-y-1">
            <label htmlFor="goal-description" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
              Description
            </label>
            <textarea
              id="goal-description"
              rows={2}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Topics, chapters, or lab exercises to master..."
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500/20 focus:border-emerald-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label htmlFor="goal-target-hours" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Target Hours <span className="text-red-500">*</span>
              </label>
              <input
                id="goal-target-hours"
                type="number"
                min="1"
                required
                value={targetHours}
                onChange={(e) => setTargetHours(Number(e.target.value))}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium"
              />
            </div>

            {goalToEdit && (
              <div className="space-y-1">
                <label htmlFor="goal-completed-hours" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                  Completed Hours
                </label>
                <input
                  id="goal-completed-hours"
                  type="number"
                  min="0"
                  max={targetHours}
                  value={completedHours}
                  onChange={(e) => setCompletedHours(Number(e.target.value))}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium"
                />
              </div>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label htmlFor="goal-category" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Category
              </label>
              <input
                id="goal-category"
                type="text"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                placeholder="e.g. Exam Prep, Homework"
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium"
              />
            </div>

            <div className="space-y-1">
              <label htmlFor="goal-target-date" className="block text-xs font-bold text-gray-700 uppercase tracking-wider">
                Target Date
              </label>
              <input
                id="goal-target-date"
                type="date"
                value={deadline}
                onChange={(e) => setDeadline(e.target.value)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-medium"
              />
            </div>
          </div>

          <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
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
              className="px-6 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl font-semibold text-sm transition-all shadow-md hover:shadow-emerald-200 disabled:opacity-50"
            >
              {isSubmitting ? 'Saving...' : goalToEdit ? 'Save Changes' : 'Create Goal'}
            </button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
};

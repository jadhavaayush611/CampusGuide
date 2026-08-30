import React from 'react';
import { CalendarCheck, Plus, CheckCircle2, Clock, Target, AlertCircle } from 'lucide-react';
import { PlannerTask, StudyGoal } from '../../../models/planner.model';

interface PlannerHeaderProps {
  tasks: PlannerTask[];
  studyGoals: StudyGoal[];
  onOpenCreateTask: () => void;
  onOpenCreateGoal: () => void;
}

export const PlannerHeader: React.FC<PlannerHeaderProps> = ({
  tasks,
  studyGoals,
  onOpenCreateTask,
  onOpenCreateGoal,
}) => {
  const todayStr = new Date().toISOString().split('T')[0];
  const activeTasks = tasks.filter((t) => !t.isCompleted);
  const completedTasks = tasks.filter((t) => t.isCompleted);
  const dueTodayTasks = activeTasks.filter((t) => t.dueDate && t.dueDate.split('T')[0] === todayStr);
  const overdueTasks = activeTasks.filter((t) => t.dueDate && t.dueDate.split('T')[0] < todayStr);

  const totalGoalHours = studyGoals.reduce((acc, g) => acc + g.targetHours, 0);
  const completedGoalHours = studyGoals.reduce((acc, g) => acc + g.completedHours, 0);
  const goalProgressPercent = totalGoalHours > 0 ? Math.round((completedGoalHours / totalGoalHours) * 100) : 0;

  return (
    <div className="space-y-6">
      {/* Hero Banner */}
      <div className="relative overflow-hidden bg-gradient-to-r from-slate-950 via-indigo-950 to-blue-900 text-white rounded-3xl p-6 sm:p-8 shadow-xl">
        <div className="absolute right-0 top-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-semibold text-blue-200 border border-white/10">
              <CalendarCheck className="w-4 h-4 text-blue-400" />
              Academic & Personal Task Management
            </div>
            <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
              Academic Planner
            </h1>
            <p className="text-blue-100/80 text-sm max-w-2xl leading-relaxed">
              Track assignments, capstone projects, midterm revisions, personal tasks, study goals, and upcoming deadlines in one unified productivity workspace.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <a
              href="/calendar?filter=planner"
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-white/10 hover:bg-white/20 text-white border border-white/20 rounded-xl font-semibold text-sm backdrop-blur-md transition-all shadow-sm"
            >
              <CalendarCheck className="w-4 h-4 text-amber-400" />
              Open in Calendar
            </a>
            <button
              onClick={onOpenCreateGoal}
              className="inline-flex items-center gap-2 px-4 py-2.5 bg-white/10 hover:bg-white/20 text-white border border-white/20 rounded-xl font-semibold text-sm backdrop-blur-md transition-all shadow-sm"
            >
              <Target className="w-4 h-4 text-emerald-400" />
              + New Study Goal
            </button>
            <button
              onClick={onOpenCreateTask}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-[#2563EB] hover:bg-blue-600 text-white rounded-xl font-semibold text-sm transition-all shadow-lg hover:shadow-blue-500/30"
            >
              <Plus className="w-4 h-4" />
              New Task
            </button>
          </div>

        </div>
      </div>

      {/* Quick Statistics Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {/* Active Tasks */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-gray-500 text-xs font-semibold uppercase tracking-wider">
            <span>Pending Tasks</span>
            <Clock className="w-4 h-4 text-blue-500" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900">{activeTasks.length}</span>
            <span className="text-xs text-gray-500">active</span>
          </div>
          <p className="text-xs text-gray-400">{completedTasks.length} completed</p>
        </div>

        {/* Due Today */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-gray-500 text-xs font-semibold uppercase tracking-wider">
            <span>Due Today</span>
            <CalendarCheck className="w-4 h-4 text-amber-500" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900">{dueTodayTasks.length}</span>
            <span className="text-xs text-gray-500">tasks</span>
          </div>
          <p className="text-xs text-gray-400">Requires attention</p>
        </div>

        {/* Overdue */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-gray-500 text-xs font-semibold uppercase tracking-wider">
            <span>Overdue Items</span>
            <AlertCircle className={`w-4 h-4 ${overdueTasks.length > 0 ? 'text-red-500' : 'text-gray-400'}`} />
          </div>
          <div className="flex items-baseline gap-2">
            <span className={`text-2xl font-bold ${overdueTasks.length > 0 ? 'text-red-600' : 'text-gray-900'}`}>
              {overdueTasks.length}
            </span>
            <span className="text-xs text-gray-500">past due</span>
          </div>
          <p className="text-xs text-gray-400">{overdueTasks.length > 0 ? 'Immediate action required' : 'All up to date!'}</p>
        </div>

        {/* Study Goal Progress */}
        <div className="bg-white p-5 rounded-2xl border border-gray-100 shadow-sm space-y-1">
          <div className="flex items-center justify-between text-gray-500 text-xs font-semibold uppercase tracking-wider">
            <span>Study Goal Hours</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-500" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900">{completedGoalHours}/{totalGoalHours}</span>
            <span className="text-xs text-gray-500">hrs ({goalProgressPercent}%)</span>
          </div>
          <div className="w-full bg-gray-100 h-1.5 rounded-full overflow-hidden mt-1">
            <div
              className="bg-emerald-500 h-full rounded-full transition-all duration-300"
              style={{ width: `${goalProgressPercent}%` }}
            />
          </div>
        </div>
      </div>
    </div>
  );
};

import React, { useState, memo, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router';
import { useTimetable } from '../../../hooks/planner/useTimetable';
import { useStudyGoals } from '../../../hooks/planner/useStudyGoals';
import { useDegreePlan } from '../../../hooks/planner/useDegreePlan';
import { useCreateStudyGoal } from '../../../hooks/planner/useCreateStudyGoal';
import { useTasks } from '../../../hooks/planner/useTasks';
import { Calendar, Target, Award, Plus, CheckCircle2, Clock, Sparkles, ArrowRight } from 'lucide-react';

export const PlannerWidget: React.FC = memo(function PlannerWidget() {
  const navigate = useNavigate();
  const { data: timetable = [], isLoading: loadingTimetable } = useTimetable();
  const { data: studyGoals = [], isLoading: loadingGoals } = useStudyGoals();
  const { data: degreePlan, isLoading: loadingDegree } = useDegreePlan();
  const { data: tasksData, isLoading: loadingTasks } = useTasks({ pageSize: 100 });
  const createGoalMutation = useCreateStudyGoal();

  const tasks = useMemo(() => tasksData?.tasks || [], [tasksData]);
  const activeTasks = useMemo(() => tasks.filter((t) => !t.isCompleted), [tasks]);
  const completedTasks = useMemo(() => tasks.filter((t) => t.isCompleted), [tasks]);
  const todayStr = useMemo(() => new Date().toISOString().split('T')[0], []);
  const overdueTasks = useMemo(
    () => activeTasks.filter((t) => t.dueDate && t.dueDate.split('T')[0] < todayStr),
    [activeTasks, todayStr]
  );
  const upcomingTasks = useMemo(
    () =>
      activeTasks
        .filter((t) => t.dueDate && t.dueDate.split('T')[0] >= todayStr)
        .sort((a, b) => (a.dueDate || '').localeCompare(b.dueDate || ''))
        .slice(0, 3),
    [activeTasks, todayStr]
  );

  const [showAddGoalModal, setShowAddGoalModal] = useState(false);
  const [goalTitle, setGoalTitle] = useState('');
  const [targetHours, setTargetHours] = useState('5');
  const [goalCategory, setGoalCategory] = useState('Study');

  const isLoading = loadingTimetable || loadingGoals || loadingDegree || loadingTasks;

  const handleCreateGoal = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    if (!goalTitle.trim()) return;

    createGoalMutation.mutate(
      {
        title: goalTitle,
        targetHours: Number(targetHours) || 5,
        category: goalCategory,
      },
      {
        onSuccess: () => {
          setGoalTitle('');
          setShowAddGoalModal(false);
        },
      }
    );
  }, [goalTitle, targetHours, goalCategory, createGoalMutation]);

  const handleOpenModal = useCallback(() => {
    setShowAddGoalModal(true);
  }, []);

  const handleCloseModal = useCallback(() => {
    setShowAddGoalModal(false);
  }, []);

  // Derive today's slots
  const currentDayStr = useMemo(() => {
    const daysMap: Record<number, string> = {
      1: 'MONDAY',
      2: 'TUESDAY',
      3: 'WEDNESDAY',
      4: 'THURSDAY',
      5: 'FRIDAY',
      6: 'SATURDAY',
      0: 'SUNDAY',
    };
    return daysMap[new Date().getDay()] || 'MONDAY';
  }, []);

  const todaySchedule = useMemo(
    () => timetable.filter((slot) => slot.dayOfWeek === currentDayStr),
    [timetable, currentDayStr]
  );

  const completedGoalsCount = useMemo(
    () => studyGoals.filter((g) => g.isCompleted).length,
    [studyGoals]
  );

  const degreeProgressPercentage = useMemo(() => {
    const completed = degreePlan?.completedCredits || 78;
    const total = degreePlan?.totalRequiredCredits || 120;
    return Math.round((completed / total) * 100);
  }, [degreePlan]);

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm animate-pulse space-y-4">
        <div className="h-6 bg-gray-200 rounded w-1/4"></div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="h-40 bg-gray-100 rounded-xl"></div>
          <div className="h-40 bg-gray-100 rounded-xl"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-teal-50 flex items-center justify-center text-teal-600">
            <Target className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">Academic Planner</h3>
            <p className="text-xs text-gray-500">Schedule, tasks, study goals & degree progress</p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate('/planner')}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-600 text-white rounded-lg text-xs font-semibold hover:bg-blue-700 transition-colors shadow-xs cursor-pointer"
          >
            <span>Open Planner</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={handleOpenModal}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-teal-600 text-white rounded-lg text-xs font-semibold hover:bg-teal-700 transition-colors shadow-xs cursor-pointer"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Study Goal</span>
          </button>
        </div>
      </div>

      {/* Task Summary Banner */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-blue-50/60 border border-blue-100 rounded-xl p-3">
          <span className="text-[11px] font-semibold text-blue-600">Active Tasks</span>
          <div className="text-xl font-bold text-blue-950 mt-0.5">{activeTasks.length}</div>
        </div>
        <div className="bg-emerald-50/60 border border-emerald-100 rounded-xl p-3">
          <span className="text-[11px] font-semibold text-emerald-600">Completed Tasks</span>
          <div className="text-xl font-bold text-emerald-950 mt-0.5">{completedTasks.length}</div>
        </div>
        <div className="bg-red-50/60 border border-red-100 rounded-xl p-3">
          <span className="text-[11px] font-semibold text-red-600">Overdue Tasks</span>
          <div className="text-xl font-bold text-red-950 mt-0.5">{overdueTasks.length}</div>
        </div>
        <div className="bg-purple-50/60 border border-purple-100 rounded-xl p-3">
          <span className="text-[11px] font-semibold text-purple-600">Study Goals</span>
          <div className="text-xl font-bold text-purple-950 mt-0.5">
            {completedGoalsCount}/{studyGoals.length}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

        {/* 1. Today's Schedule Column */}
        <div className="bg-slate-50/70 border border-slate-200/80 rounded-xl p-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-gray-800 flex items-center gap-1.5">
                <Clock className="w-4 h-4 text-teal-600" />
                Today's Schedule
              </span>
              <span className="text-[11px] font-semibold text-gray-500 bg-white px-2 py-0.5 rounded border border-gray-200">
                {currentDayStr}
              </span>
            </div>

            {todaySchedule.length === 0 ? (
              <div className="py-8 text-center bg-white rounded-lg border border-dashed border-gray-300 p-4">
                <Calendar className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                <p className="text-xs font-semibold text-gray-700">No classes scheduled today</p>
                <p className="text-[11px] text-gray-500 mt-1">Your schedule is open for independent study or rest.</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                {todaySchedule.map((slot) => (
                  <div key={slot.id} className="bg-white p-3 rounded-lg border border-gray-200 text-xs shadow-2xs">
                    <div className="flex items-center justify-between font-bold text-gray-900">
                      <span>{slot.courseTitle}</span>
                      <span className="text-[10px] text-teal-700 bg-teal-50 px-1.5 py-0.5 rounded">{slot.type}</span>
                    </div>
                    <div className="flex items-center justify-between text-[11px] text-gray-500 mt-1">
                      <span>Room {slot.room}</span>
                      <span className="font-semibold text-gray-700">{slot.startTime} - {slot.endTime}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* 2. Active Study Goals Column */}
        <div className="bg-slate-50/70 border border-slate-200/80 rounded-xl p-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-gray-800 flex items-center gap-1.5">
                <Target className="w-4 h-4 text-indigo-600" />
                Active Study Goals
              </span>
              <span className="text-[11px] font-semibold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100">
                {completedGoalsCount}/{studyGoals.length} Done
              </span>
            </div>

            {studyGoals.length === 0 ? (
              <div className="py-8 text-center bg-white rounded-lg border border-dashed border-gray-300 p-4">
                <Sparkles className="w-8 h-8 text-indigo-300 mx-auto mb-2" />
                <p className="text-xs font-semibold text-gray-700">No study goals set yet</p>
                <p className="text-[11px] text-gray-500 mt-1">Add your first goal to track target hours and completion.</p>
                <button
                  onClick={handleOpenModal}
                  className="mt-3 text-xs text-indigo-600 font-bold hover:underline"
                >
                  + Add Study Goal
                </button>
              </div>
            ) : (
              <div className="space-y-2.5 max-h-56 overflow-y-auto pr-1">
                {studyGoals.map((goal) => {
                  const pct = Math.min(100, Math.round((goal.completedHours / goal.targetHours) * 100));
                  return (
                    <div key={goal.id} className="bg-white p-3 rounded-lg border border-gray-200 text-xs shadow-2xs">
                      <div className="flex items-center justify-between font-bold text-gray-900 mb-1">
                        <span className="line-clamp-1">{goal.title}</span>
                        {goal.isCompleted && (
                          <CheckCircle2 className="w-4 h-4 text-emerald-500 flex-shrink-0" />
                        )}
                      </div>
                      <div className="w-full bg-gray-100 rounded-full h-1.5 mb-1.5">
                        <div
                          className="bg-indigo-600 h-1.5 rounded-full transition-all duration-300"
                          style={{ width: `${pct}%` }}
                        ></div>
                      </div>
                      <div className="flex items-center justify-between text-[10px] text-gray-500">
                        <span>{goal.completedHours} / {goal.targetHours} hours ({pct}%)</span>
                        {goal.deadline && <span>Due: {goal.deadline}</span>}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* 3. Degree Progress & Deadlines */}
        <div className="bg-slate-50/70 border border-slate-200/80 rounded-xl p-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-gray-800 flex items-center gap-1.5">
                <Award className="w-4 h-4 text-amber-600" />
                Degree Progress
              </span>
              <span className="text-[11px] font-semibold text-amber-700 bg-amber-50 px-2 py-0.5 rounded border border-amber-100">
                {degreePlan ? degreePlan.programName : 'Computer Engineering'}
              </span>
            </div>

            <div className="bg-white p-3.5 rounded-lg border border-gray-200 shadow-2xs mb-3">
              <div className="flex items-center justify-between text-xs font-bold text-gray-900 mb-1">
                <span>Completed Credits</span>
                <span className="text-amber-700">
                  {degreePlan ? `${degreePlan.completedCredits} / ${degreePlan.totalRequiredCredits} Cr` : '78 / 120 Cr'}
                </span>
              </div>
              <div className="w-full bg-gray-100 rounded-full h-2 mb-2">
                <div
                  className="bg-gradient-to-r from-amber-500 to-orange-500 h-2 rounded-full transition-all duration-300"
                  style={{
                    width: `${degreeProgressPercentage}%`,
                  }}
                ></div>
              </div>
              <p className="text-[11px] text-gray-500">
                On track for graduation in Spring 2027.
              </p>
            </div>

            <div className="flex items-center justify-between mb-2">
              <h5 className="text-[11px] font-bold text-gray-700 uppercase tracking-wider">
                Upcoming Deadlines
              </h5>
              {overdueTasks.length > 0 && (
                <span className="text-[10px] font-bold text-red-600 bg-red-50 px-1.5 py-0.5 rounded">
                  {overdueTasks.length} Overdue
                </span>
              )}
            </div>

            {upcomingTasks.length === 0 && overdueTasks.length === 0 ? (
              <p className="text-xs text-gray-500 py-2">No pending task deadlines.</p>
            ) : (
              <div className="space-y-2 text-xs">
                {overdueTasks.slice(0, 2).map((t) => (
                  <div
                    key={t.id}
                    className="bg-red-50/70 p-2.5 rounded-lg border border-red-200 flex items-center justify-between"
                  >
                    <div className="min-w-0 pr-2">
                      <p className="font-bold text-red-950 truncate">{t.title}</p>
                      <p className="text-[10px] text-red-700">{t.category} • Overdue</p>
                    </div>
                    <span className="text-[10px] bg-red-100 text-red-800 font-bold px-2 py-0.5 rounded shrink-0">
                      {t.dueDate ? t.dueDate.split('T')[0] : 'Past'}
                    </span>
                  </div>
                ))}
                {upcomingTasks.map((t) => (
                  <div
                    key={t.id}
                    className="bg-white p-2.5 rounded-lg border border-gray-200 flex items-center justify-between"
                  >
                    <div className="min-w-0 pr-2">
                      <p className="font-bold text-gray-900 truncate">{t.title}</p>
                      <p className="text-[10px] text-gray-500">{t.category}</p>
                    </div>
                    <span className="text-[10px] bg-blue-50 text-blue-700 font-bold px-2 py-0.5 rounded shrink-0">
                      {t.dueDate ? t.dueDate.split('T')[0] : ''}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>

      {/* Add Goal Modal */}
      {showAddGoalModal && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl border border-gray-200">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Create Study Goal</h3>
            <form onSubmit={handleCreateGoal} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Goal Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Master React Query hooks"
                  value={goalTitle}
                  onChange={(e) => setGoalTitle(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-teal-500 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Target Hours</label>
                  <input
                    type="number"
                    min="1"
                    max="100"
                    required
                    value={targetHours}
                    onChange={(e) => setTargetHours(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-teal-500 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Category</label>
                  <select
                    value={goalCategory}
                    onChange={(e) => setGoalCategory(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-teal-500 focus:outline-none"
                  >
                    <option value="Study">Study</option>
                    <option value="Assignment">Assignment</option>
                    <option value="Exam Prep">Exam Prep</option>
                    <option value="Project">Project</option>
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-xs font-semibold hover:bg-gray-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createGoalMutation.isPending}
                  className="px-4 py-2 bg-teal-600 text-white rounded-lg text-xs font-semibold hover:bg-teal-700 disabled:opacity-50"
                >
                  {createGoalMutation.isPending ? 'Saving...' : 'Save Goal'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
});

import React, { useState, lazy, Suspense } from 'react';
import { Header } from '../components/Header';
import { PlannerHeader } from '../components/planner/PlannerHeader';
import { PlannerTabs, PlannerTab } from '../components/planner/PlannerTabs';
import { TaskFilters } from '../components/planner/TaskFilters';
import { TaskCard } from '../components/planner/TaskCard';
import { StudyGoalCard } from '../components/planner/StudyGoalCard';
import { DeadlinesView } from '../components/planner/DeadlinesView';
import { AcademicSummaryTab } from '../components/planner/AcademicSummaryTab';
import { TaskSkeleton, TaskEmptyState } from '../components/planner/TaskSkeleton';
import { PlannerErrorBoundary } from '../components/planner/PlannerErrorBoundary';

const TaskFormModal = lazy(() =>
  import('../components/planner/TaskFormModal').then((m) => ({ default: m.TaskFormModal }))
);
const TaskDetailsModal = lazy(() =>
  import('../components/planner/TaskDetailsModal').then((m) => ({ default: m.TaskDetailsModal }))
);
const StudyGoalModal = lazy(() =>
  import('../components/planner/StudyGoalModal').then((m) => ({ default: m.StudyGoalModal }))
);

import {
  useTasks,
  useCreateTask,
  useUpdateTask,
  useDeleteTask,
  useArchiveTask,
  useRestoreTask,
  useMarkTaskComplete,
  useUpdateTaskProgress,
  useStudyGoals,
  useCreateStudyGoal,
  useUpdateStudyGoal,
  useDeleteStudyGoal,
} from '../../hooks/planner';
import { PlannerTask, StudyGoal, TaskQueryParams } from '../../models/planner.model';
import { toast } from 'sonner';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export function PlannerPage() {
  // Navigation Tab state
  const [activeTab, setActiveTab] = useState<PlannerTab>('TASKS');

  // Filter & Query States for Tasks
  const [filters, setFilters] = useState<TaskQueryParams>({
    search: '',
    category: 'ALL',
    priority: 'ALL',
    status: 'ALL',
    dueDateFilter: 'ALL',
    sortBy: 'dueDate',
    sortOrder: 'asc',
    page: 1,
    pageSize: 9,
  });

  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  // Modals state
  const [selectedTask, setSelectedTask] = useState<PlannerTask | null>(null);
  const [taskToEdit, setTaskToEdit] = useState<PlannerTask | null>(null);
  const [isTaskFormOpen, setIsTaskFormOpen] = useState(false);

  const [selectedGoalToEdit, setSelectedGoalToEdit] = useState<StudyGoal | null>(null);
  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);

  // Compute effective query filters based on active tab
  const computedTaskFilters: TaskQueryParams = {
    ...filters,
    isArchived: activeTab === 'ARCHIVED' ? true : false,
    status: activeTab === 'ARCHIVED' ? 'ARCHIVED' : filters.status,
  };

  // Queries
  const { data: taskResponse, isLoading: loadingTasks, isError: errorTasks } = useTasks(computedTaskFilters);
  const { data: allTasksResponse } = useTasks({ pageSize: 100 });
  const { data: studyGoals = [], isLoading: loadingGoals } = useStudyGoals();

  // Task Mutations
  const createTaskMutation = useCreateTask();
  const updateTaskMutation = useUpdateTask();
  const deleteTaskMutation = useDeleteTask();
  const archiveTaskMutation = useArchiveTask();
  const restoreTaskMutation = useRestoreTask();
  const markCompleteMutation = useMarkTaskComplete();
  const updateProgressMutation = useUpdateTaskProgress();

  // Goal Mutations
  const createGoalMutation = useCreateStudyGoal();
  const updateGoalMutation = useUpdateStudyGoal();
  const deleteGoalMutation = useDeleteStudyGoal();

  // Handlers
  const handleFilterChange = (newFilters: Partial<TaskQueryParams>) => {
    setFilters((prev) => ({ ...prev, ...newFilters }));
  };

  const handleResetFilters = () => {
    setFilters({
      search: '',
      category: 'ALL',
      priority: 'ALL',
      status: 'ALL',
      dueDateFilter: 'ALL',
      sortBy: 'dueDate',
      sortOrder: 'asc',
      page: 1,
      pageSize: 9,
    });
  };

  // Task Handlers
  const handleOpenCreateTask = () => {
    setTaskToEdit(null);
    setIsTaskFormOpen(true);
  };

  const handleOpenEditTask = (task: PlannerTask) => {
    setTaskToEdit(task);
    setIsTaskFormOpen(true);
  };

  const handleMarkTaskComplete = (id: string, completed: boolean) => {
    markCompleteMutation.mutate(
      { id, completed },
      {
        onSuccess: () => {
          toast.success(completed ? 'Task marked complete!' : 'Task marked pending.');
        },
        onError: (err) => {
          toast.error(`Failed to update task: ${err.message}`);
        },
      }
    );
  };

  const handleUpdateTaskProgress = (id: string, progress: number) => {
    updateProgressMutation.mutate(
      { id, progress },
      {
        onSuccess: () => {
          toast.success(`Progress updated to ${progress}%`);
        },
      }
    );
  };

  const handleArchiveTask = (id: string) => {
    archiveTaskMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Task moved to Archived.');
      },
    });
  };

  const handleRestoreTask = (id: string) => {
    restoreTaskMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Task restored to active planner.');
      },
    });
  };

  const handleDeleteTask = (id: string) => {
    deleteTaskMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Task permanently deleted.');
      },
    });
  };

  // Goal Handlers
  const handleOpenCreateGoal = () => {
    setSelectedGoalToEdit(null);
    setIsGoalModalOpen(true);
  };

  const handleOpenEditGoal = (goal: StudyGoal) => {
    setSelectedGoalToEdit(goal);
    setIsGoalModalOpen(true);
  };

  const handleLogGoalHours = (goal: StudyGoal, hoursToAdd: number) => {
    const newCompleted = goal.completedHours + hoursToAdd;
    updateGoalMutation.mutate(
      {
        id: goal.id,
        payload: {
          completedHours: newCompleted,
          isCompleted: newCompleted >= goal.targetHours,
        },
      },
      {
        onSuccess: () => {
          toast.success(`Logged +${hoursToAdd} hrs to "${goal.title}"!`);
        },
      }
    );
  };

  const handleDeleteGoal = (id: string) => {
    deleteGoalMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Study goal deleted.');
      },
    });
  };

  const tasksList = taskResponse?.tasks || [];
  const allTasksList = allTasksResponse?.tasks || [];
  const todayStr = new Date().toISOString().split('T')[0];
  const overdueCount = allTasksList.filter((t) => !t.isArchived && t.dueDate && t.dueDate.split('T')[0] < todayStr && !t.isCompleted).length;
  const archivedCount = allTasksList.filter((t) => t.isArchived).length;

  return (
    <div className="min-h-screen bg-gray-50/50">
      <Header />

      <main className="p-4 sm:p-8">
        <div className="max-w-[1440px] mx-auto space-y-8">
          {/* Top Header Banner */}
          <PlannerHeader
            tasks={allTasksList}
            studyGoals={studyGoals}
            onOpenCreateTask={handleOpenCreateTask}
            onOpenCreateGoal={handleOpenCreateGoal}
          />

          {/* Tab Bar */}
          <PlannerTabs
            activeTab={activeTab}
            onTabChange={setActiveTab}
            tasksCount={allTasksList.filter((t) => !t.isArchived && !t.isCompleted).length}
            goalsCount={studyGoals.filter((g) => !g.isCompleted).length}
            overdueCount={overdueCount}
            archivedCount={archivedCount}
          />

          {/* Section Content */}
          <PlannerErrorBoundary fallbackTitle="Error loading Planner tab content">
            {/* TAB 1 & TAB 5: TASKS & ARCHIVED */}
            {(activeTab === 'TASKS' || activeTab === 'ARCHIVED') && (
              <div className="space-y-6">
                <TaskFilters
                  filters={filters}
                  onFilterChange={handleFilterChange}
                  onResetFilters={handleResetFilters}
                  viewMode={viewMode}
                  onViewModeChange={setViewMode}
                  totalResults={taskResponse?.total}
                />

                {loadingTasks ? (
                  <TaskSkeleton count={6} viewMode={viewMode} />
                ) : errorTasks ? (
                  <div className="p-8 bg-red-50 text-red-700 rounded-3xl text-center space-y-2 border border-red-200">
                    <h3 className="text-base font-bold">Failed to load tasks</h3>
                    <p className="text-xs">There was an error communicating with the planner service.</p>
                  </div>
                ) : tasksList.length === 0 ? (
                  <TaskEmptyState
                    title={activeTab === 'ARCHIVED' ? 'No archived tasks' : 'No tasks found'}
                    description={
                      activeTab === 'ARCHIVED'
                        ? 'You have not archived any tasks yet.'
                        : 'No tasks match your filter criteria or search query.'
                    }
                    onAction={activeTab === 'TASKS' ? handleOpenCreateTask : undefined}
                    actionLabel="Create Task"
                  />
                ) : (
                  <>
                    <div
                      className={
                        viewMode === 'grid'
                          ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5'
                          : 'space-y-3'
                      }
                    >
                      {tasksList.map((task) => (
                        <TaskCard
                          key={task.id}
                          task={task}
                          viewMode={viewMode}
                          onSelect={setSelectedTask}
                          onEdit={handleOpenEditTask}
                          onMarkComplete={handleMarkTaskComplete}
                          onUpdateProgress={handleUpdateTaskProgress}
                          onArchive={handleArchiveTask}
                          onRestore={handleRestoreTask}
                          onDelete={handleDeleteTask}
                        />
                      ))}
                    </div>

                    {/* Pagination Bar */}
                    {taskResponse && taskResponse.totalPages > 1 && (
                      <div className="flex items-center justify-between pt-4 border-t border-gray-200 text-xs font-semibold text-gray-600">
                        <span>
                          Page {taskResponse.page} of {taskResponse.totalPages} ({taskResponse.total} items)
                        </span>

                        <div className="flex items-center gap-2">
                          <button
                            disabled={taskResponse.page <= 1}
                            onClick={() => handleFilterChange({ page: taskResponse.page - 1 })}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-40 transition-colors shadow-2xs"
                          >
                            <ChevronLeft className="w-4 h-4" /> Previous
                          </button>
                          <button
                            disabled={taskResponse.page >= taskResponse.totalPages}
                            onClick={() => handleFilterChange({ page: taskResponse.page + 1 })}
                            className="inline-flex items-center gap-1 px-3 py-1.5 bg-white border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-40 transition-colors shadow-2xs"
                          >
                            Next <ChevronRight className="w-4 h-4" />
                          </button>
                        </div>
                      </div>
                    )}
                  </>
                )}
              </div>
            )}

            {/* TAB 2: STUDY GOALS */}
            {activeTab === 'GOALS' && (
              <div className="space-y-6">
                <div className="flex items-center justify-between">
                  <div>
                    <h2 className="text-xl font-extrabold text-gray-900">Study Goals & Target Hours</h2>
                    <p className="text-xs text-gray-500">Track target hours versus completed study hours for midterms and lab assignments.</p>
                  </div>
                  <button
                    onClick={handleOpenCreateGoal}
                    className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl transition-all shadow-sm"
                  >
                    + Add Study Goal
                  </button>
                </div>

                {loadingGoals ? (
                  <TaskSkeleton count={3} />
                ) : studyGoals.length === 0 ? (
                  <TaskEmptyState
                    title="No study goals set"
                    description="Create a study goal to track study hours and prep progress."
                    onAction={handleOpenCreateGoal}
                    actionLabel="Create Study Goal"
                  />
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                    {studyGoals.map((goal) => (
                      <StudyGoalCard
                        key={goal.id}
                        goal={goal}
                        onLogHours={handleLogGoalHours}
                        onEdit={handleOpenEditGoal}
                        onDelete={handleDeleteGoal}
                      />
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* TAB 3: DEADLINES & MILESTONES */}
            {activeTab === 'DEADLINES' && <DeadlinesView />}

            {/* TAB 4: ACADEMIC OVERVIEW */}
            {activeTab === 'ACADEMIC' && <AcademicSummaryTab />}
          </PlannerErrorBoundary>
        </div>
      </main>

      {/* Modals */}
      <Suspense fallback={null}>
        <TaskFormModal
          isOpen={isTaskFormOpen}
          onClose={() => setIsTaskFormOpen(false)}
          taskToEdit={taskToEdit}
          onSubmitCreate={(payload) => {
            createTaskMutation.mutate(payload, {
              onSuccess: () => {
                toast.success('New task created successfully!');
              },
              onError: (err) => {
                toast.error(`Error creating task: ${err.message}`);
              },
            });
          }}
          onSubmitUpdate={(id, payload) => {
            updateTaskMutation.mutate(
              { id, payload },
              {
                onSuccess: () => {
                  toast.success('Task updated.');
                },
                onError: (err) => {
                  toast.error(`Error updating task: ${err.message}`);
                },
              }
            );
          }}
          isSubmitting={createTaskMutation.isPending || updateTaskMutation.isPending}
        />

        <TaskDetailsModal
          isOpen={!!selectedTask}
          task={selectedTask}
          onClose={() => setSelectedTask(null)}
          onEdit={(task) => {
            handleOpenEditTask(task);
          }}
          onMarkComplete={handleMarkTaskComplete}
          onUpdateProgress={handleUpdateTaskProgress}
          onArchive={handleArchiveTask}
          onRestore={handleRestoreTask}
          onDelete={handleDeleteTask}
        />

        <StudyGoalModal
          isOpen={isGoalModalOpen}
          onClose={() => setIsGoalModalOpen(false)}
          goalToEdit={selectedGoalToEdit}
          onSubmitCreate={(payload) => {
            createGoalMutation.mutate(payload, {
              onSuccess: () => {
                toast.success('Study goal created!');
              },
            });
          }}
          onSubmitUpdate={(id, payload) => {
            updateGoalMutation.mutate(
              { id, payload },
              {
                onSuccess: () => {
                  toast.success('Study goal updated.');
                },
              }
            );
          }}
          isSubmitting={createGoalMutation.isPending || updateGoalMutation.isPending}
        />
      </Suspense>
    </div>
  );
}

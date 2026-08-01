import { useQuery } from '@tanstack/react-query';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { queryKeys } from '../../sdk/queryKeys';
import { PlannerTask, AcademicCalendarItem, StudyGoal } from '../../models/planner.model';

export interface UpcomingDeadlineItem {
  id: string;
  title: string;
  type: 'TASK' | 'CALENDAR' | 'STUDY_GOAL';
  dueDate: string;
  category?: string;
  priority?: string;
  isOverdue: boolean;
  isCompleted: boolean;
  rawItem: PlannerTask | AcademicCalendarItem | StudyGoal;
}

export function useUpcomingDeadlines() {
  return useQuery({
    queryKey: queryKeys.planner.upcomingDeadlines(),
    queryFn: async (): Promise<{
      upcoming: UpcomingDeadlineItem[];
      overdue: UpcomingDeadlineItem[];
      recentlyCompleted: UpcomingDeadlineItem[];
    }> => {
      const todayStr = new Date().toISOString().split('T')[0];
      const taskRes = await plannerSdk.getTasks({ pageSize: 100 });
      const calendarItems = await plannerSdk.getAcademicCalendar();
      const studyGoals = await plannerSdk.getStudyGoals();

      const items: UpcomingDeadlineItem[] = [];

      // Add tasks with due date
      taskRes.tasks.forEach((task) => {
        if (task.dueDate && !task.isArchived) {
          const taskDate = task.dueDate.split('T')[0];
          items.push({
            id: `task-${task.id}`,
            title: task.title,
            type: 'TASK',
            dueDate: taskDate,
            category: task.category,
            priority: task.priority,
            isOverdue: taskDate < todayStr && !task.isCompleted,
            isCompleted: task.isCompleted,
            rawItem: task,
          });
        }
      });

      // Add calendar items
      calendarItems.forEach((cal) => {
        if (cal.date) {
          items.push({
            id: `cal-${cal.id}`,
            title: cal.title,
            type: 'CALENDAR',
            dueDate: cal.date,
            category: cal.category,
            isOverdue: cal.date < todayStr,
            isCompleted: false,
            rawItem: cal,
          });
        }
      });

      // Add study goals with deadline
      studyGoals.forEach((goal) => {
        if (goal.deadline) {
          const goalDate = goal.deadline.split('T')[0];
          items.push({
            id: `goal-${goal.id}`,
            title: goal.title,
            type: 'STUDY_GOAL',
            dueDate: goalDate,
            category: goal.category || 'Study Goal',
            isOverdue: goalDate < todayStr && !goal.isCompleted,
            isCompleted: goal.isCompleted,
            rawItem: goal,
          });
        }
      });

      const upcoming = items
        .filter((item) => !item.isOverdue && !item.isCompleted)
        .sort((a, b) => a.dueDate.localeCompare(b.dueDate));

      const overdue = items
        .filter((item) => item.isOverdue)
        .sort((a, b) => a.dueDate.localeCompare(b.dueDate));

      const recentlyCompleted = items
        .filter((item) => item.isCompleted)
        .sort((a, b) => b.dueDate.localeCompare(a.dueDate));

      return { upcoming, overdue, recentlyCompleted };
    },
    staleTime: 1000 * 60 * 5,
  });
}

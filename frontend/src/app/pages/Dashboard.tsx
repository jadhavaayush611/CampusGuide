import { memo, useCallback } from 'react';
import { Header } from '../components/Header';
import { WidgetErrorBoundary } from '../components/dashboard/WidgetErrorBoundary';
import { UserOverviewWidget } from '../components/dashboard/UserOverviewWidget';
import { AcademicSummaryWidget } from '../components/dashboard/AcademicSummaryWidget';
import { CampusActivityWidget } from '../components/dashboard/CampusActivityWidget';
import { PlannerWidget } from '../components/dashboard/PlannerWidget';
import { AtlasWidget } from '../components/dashboard/AtlasWidget';
import { NotificationsWidget } from '../components/dashboard/NotificationsWidget';
import { Calendar, LayoutDashboard, Search } from 'lucide-react';
import { useNavigate } from 'react-router';
import { prefetchRoute } from '../../core/routing/routePrefetch';

export const Dashboard = memo(function Dashboard() {
  const navigate = useNavigate();

  const handleNavigateCalendar = useCallback(() => {
    navigate('/calendar');
  }, [navigate]);

  const handlePrefetchCalendar = useCallback(() => {
    prefetchRoute('/calendar');
  }, []);

  const handleNavigateResources = useCallback(() => {
    navigate('/resources');
  }, [navigate]);

  const handlePrefetchResources = useCallback(() => {
    prefetchRoute('/resources');
  }, []);

  return (
    <div className="min-h-screen bg-slate-50/50">
      <Header />

      <main className="p-4 sm:p-6 lg:p-8 max-w-[1500px] mx-auto space-y-8">
        {/* Navigation Bar */}
        <div className="flex items-center justify-between border-b border-gray-200/80 pb-3">
          <div className="flex gap-6">
            <div className="flex items-center gap-2 pb-2 text-sm font-bold border-b-2 border-blue-600 text-blue-600">
              <LayoutDashboard className="w-4 h-4" />
              <span>Production Dashboard</span>
            </div>
            <button
              onClick={handleNavigateCalendar}
              onMouseEnter={handlePrefetchCalendar}
              onFocus={handlePrefetchCalendar}
              className="flex items-center gap-2 pb-2 text-sm font-bold border-b-2 border-transparent text-gray-500 hover:text-gray-800 transition-all"
            >
              <Calendar className="w-4 h-4 text-blue-600" />
              <span>Full Campus Calendar</span>
            </button>
          </div>

          <div className="hidden sm:flex items-center gap-2 text-xs text-gray-500 bg-white border border-gray-200 px-3 py-1.5 rounded-lg shadow-2xs">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>Live SDK Integration</span>
          </div>
        </div>

        {/* Overview Tab Layout (Aggregator & Orchestrator) */}
        <div className="space-y-8">
          {/* 1. User Overview Widget */}
          <WidgetErrorBoundary title="User Overview">
            <UserOverviewWidget />
          </WidgetErrorBoundary>

          {/* 2. Top Grid: Academic Summary & Notifications */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2">
              <WidgetErrorBoundary title="Academic Summary">
                <AcademicSummaryWidget />
              </WidgetErrorBoundary>
            </div>

            <div>
              <WidgetErrorBoundary title="Notifications">
                <NotificationsWidget />
              </WidgetErrorBoundary>
            </div>
          </div>

          {/* 3. Middle Section: Academic Planner */}
          <WidgetErrorBoundary title="Academic Planner">
            <PlannerWidget />
          </WidgetErrorBoundary>

          {/* 4. Bottom Grid: Campus Activity & Atlas Wayfinding Quick Actions */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2">
              <WidgetErrorBoundary title="Campus Activity">
                <CampusActivityWidget />
              </WidgetErrorBoundary>
            </div>

            <div>
              <WidgetErrorBoundary title="Atlas Quick Actions">
                <AtlasWidget />
              </WidgetErrorBoundary>
            </div>
          </div>
        </div>
      </main>

      {/* Quick Search Floating Action Button */}
      <button
        onClick={handleNavigateResources}
        onMouseEnter={handlePrefetchResources}
        onFocus={handlePrefetchResources}
        className="fixed bottom-8 right-8 w-14 h-14 bg-blue-600 text-white rounded-2xl shadow-xl hover:bg-blue-700 hover:scale-105 transition-all flex items-center justify-center ring-4 ring-blue-100 z-40"
        title="Quick Search Campus Resources"
      >
        <Search className="w-6 h-6" />
      </button>
    </div>
  );
});

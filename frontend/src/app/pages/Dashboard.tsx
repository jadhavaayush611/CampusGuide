import React, { useState } from 'react';
import { Header } from '../components/Header';
import { WidgetErrorBoundary } from '../components/dashboard/WidgetErrorBoundary';
import { UserOverviewWidget } from '../components/dashboard/UserOverviewWidget';
import { AcademicSummaryWidget } from '../components/dashboard/AcademicSummaryWidget';
import { CampusActivityWidget } from '../components/dashboard/CampusActivityWidget';
import { PlannerWidget } from '../components/dashboard/PlannerWidget';
import { AtlasWidget } from '../components/dashboard/AtlasWidget';
import { NotificationsWidget } from '../components/dashboard/NotificationsWidget';
import { useCampusEvents } from '../../hooks/campus/useCampusEvents';
import { Calendar, ChevronLeft, ChevronRight, LayoutDashboard, Search, MapPin } from 'lucide-react';
import { useNavigate } from 'react-router';

type Tab = 'overview' | 'calendar';

export function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<Tab>('overview');
  const [calendarView, setCalendarView] = useState<'month' | 'week'>('month');
  const [selectedDate, setSelectedDate] = useState<number | null>(null);
  const [currentWeekOffset, setCurrentWeekOffset] = useState<number>(0);
  const [currentMonthOffset, setCurrentMonthOffset] = useState<number>(0);

  // Fetch campus events for calendar view
  const { data: events = [] } = useCampusEvents();

  const navigateWeek = (direction: 'prev' | 'next') => {
    setCurrentWeekOffset((prev) => (direction === 'prev' ? prev - 1 : prev + 1));
  };

  const navigateMonth = (direction: 'prev' | 'next') => {
    setCurrentMonthOffset((prev) => (direction === 'prev' ? prev - 1 : prev + 1));
  };

  const renderCalendar = () => {
    const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    const renderCalendarHeader = (title: string, subtitle: string) => (
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between mb-6 gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center shadow-sm">
            <Calendar className="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 className="text-xl font-bold text-gray-900 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              {title}
            </h3>
            <p className="text-xs text-gray-500">{subtitle}</p>
          </div>
          <div className="flex gap-1.5 ml-2">
            <button
              onClick={() => (calendarView === 'week' ? navigateWeek('prev') : navigateMonth('prev'))}
              className="p-1.5 hover:bg-white rounded-lg transition-colors border border-gray-200"
            >
              <ChevronLeft className="w-4 h-4 text-gray-600" />
            </button>
            <button
              onClick={() => (calendarView === 'week' ? navigateWeek('next') : navigateMonth('next'))}
              className="p-1.5 hover:bg-white rounded-lg transition-colors border border-gray-200"
            >
              <ChevronRight className="w-4 h-4 text-gray-600" />
            </button>
          </div>
        </div>
        <div className="flex gap-1.5 bg-gray-100 p-1 rounded-xl">
          <button
            onClick={() => setCalendarView('month')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              calendarView === 'month' ? 'bg-white text-blue-700 shadow-xs' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Month
          </button>
          <button
            onClick={() => setCalendarView('week')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              calendarView === 'week' ? 'bg-white text-blue-700 shadow-xs' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Week
          </button>
        </div>
      </div>
    );

    const baseDate = new Date(2026, 3, 1);
    const targetMonthDate = new Date(baseDate.getFullYear(), baseDate.getMonth() + currentMonthOffset, 1);
    const currentMonthDisplay = targetMonthDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    const daysInMonth = new Date(targetMonthDate.getFullYear(), targetMonthDate.getMonth() + 1, 0).getDate();
    const firstDayOfWeek = targetMonthDate.getDay();

    const days = [];
    for (let i = 0; i < firstDayOfWeek; i++) {
      days.push(<div key={`empty-${i}`} className="aspect-square"></div>);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const isToday = day === 7 && currentMonthOffset === 0;
      days.push(
        <div
          key={day}
          onClick={() => setSelectedDate(day)}
          className={`aspect-square p-2 border rounded-xl hover:shadow-sm cursor-pointer transition-all relative overflow-hidden flex flex-col justify-between ${
            isToday
              ? 'bg-blue-600 border-blue-600 text-white shadow-md'
              : selectedDate === day
              ? 'bg-indigo-50 border-indigo-300'
              : 'bg-white border-gray-200 hover:border-blue-300'
          }`}
        >
          <div className={`text-xs font-bold ${isToday ? 'text-white' : 'text-gray-800'}`}>{day}</div>
          <div className="flex gap-1 flex-wrap">
            {events.slice(0, 2).map((_, idx) => (
              <div
                key={idx}
                className={`w-1.5 h-1.5 rounded-full ${isToday ? 'bg-white' : 'bg-blue-500'}`}
              ></div>
            ))}
          </div>
        </div>
      );
    }

    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm">
        {renderCalendarHeader(currentMonthDisplay, 'Academic & Campus Events Schedule')}
        <div className="grid grid-cols-7 gap-2 mb-2 text-center text-xs font-bold text-gray-500">
          {weekDays.map((d) => (
            <div key={d} className="py-1">
              {d}
            </div>
          ))}
        </div>
        <div className="grid grid-cols-7 gap-2">{days}</div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-slate-50/50">
      <Header />

      <main className="p-4 sm:p-6 lg:p-8 max-w-[1500px] mx-auto space-y-8">
        {/* Navigation Tabs */}
        <div className="flex items-center justify-between border-b border-gray-200/80 pb-3">
          <div className="flex gap-6">
            <button
              onClick={() => setActiveTab('overview')}
              className={`flex items-center gap-2 pb-2 text-sm font-bold border-b-2 transition-all ${
                activeTab === 'overview'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Production Dashboard</span>
            </button>
            <button
              onClick={() => setActiveTab('calendar')}
              className={`flex items-center gap-2 pb-2 text-sm font-bold border-b-2 transition-all ${
                activeTab === 'calendar'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-800'
              }`}
            >
              <Calendar className="w-4 h-4" />
              <span>Event Calendar</span>
            </button>
          </div>

          <div className="hidden sm:flex items-center gap-2 text-xs text-gray-500 bg-white border border-gray-200 px-3 py-1.5 rounded-lg shadow-2xs">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            <span>Live SDK Integration</span>
          </div>
        </div>

        {/* Overview Tab Layout */}
        {activeTab === 'overview' && (
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
        )}

        {/* Event Calendar Tab Layout */}
        {activeTab === 'calendar' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2">{renderCalendar()}</div>
            <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-4">
              <h3 className="text-lg font-bold text-gray-900">Upcoming Events</h3>
              <div className="space-y-3">
                {events.slice(0, 5).map((evt) => (
                  <div key={evt.id} className="p-3 bg-gray-50 rounded-xl border border-gray-200 text-xs space-y-1">
                    <p className="font-bold text-gray-900">{evt.title}</p>
                    <p className="text-gray-600">{evt.description}</p>
                    <div className="flex items-center gap-2 text-gray-500 pt-1">
                      <MapPin className="w-3 h-3 text-gray-400" />
                      <span>{evt.location}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Quick Search Floating Action Button */}
      <button
        onClick={() => navigate('/resources')}
        className="fixed bottom-8 right-8 w-14 h-14 bg-blue-600 text-white rounded-2xl shadow-xl hover:bg-blue-700 hover:scale-105 transition-all flex items-center justify-center ring-4 ring-blue-100 z-40"
        title="Quick Search Campus Resources"
      >
        <Search className="w-6 h-6" />
      </button>
    </div>
  );
}

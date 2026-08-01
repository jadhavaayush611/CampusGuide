import React, { useState, useEffect, useMemo, lazy, Suspense } from 'react';
import { useSearchParams } from 'react-router';
import { Header } from '../components/Header';
import { CalendarHeader } from '../components/calendar/CalendarHeader';
import { CalendarSidebarFilter } from '../components/calendar/CalendarSidebarFilter';
import { MonthView } from '../components/calendar/MonthView';
import { WeekView } from '../components/calendar/WeekView';
import { DayView } from '../components/calendar/DayView';
import { AgendaView } from '../components/calendar/AgendaView';
import { CalendarSkeleton } from '../components/calendar/CalendarSkeleton';
import { CalendarErrorBoundary } from '../components/calendar/CalendarErrorBoundary';

const EventDetailsModal = lazy(() =>
  import('../components/calendar/EventDetailsModal').then((m) => ({ default: m.EventDetailsModal }))
);
const EventFormModal = lazy(() =>
  import('../components/calendar/EventFormModal').then((m) => ({ default: m.EventFormModal }))
);

import {
  useAggregatedCalendarEvents,
  useCreateCalendarEntry,
  useUpdateCalendarEntry,
  useDeleteCalendarEntry,
} from '../../hooks/calendar';
import {
  AggregatedCalendarEvent,
  CalendarFilterState,
  CalendarViewMode,
  CalendarEventSourceModule,
} from '../../models/calendar.model';
import { toast } from 'sonner';

export function CalendarPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Parse deep-link query parameters
  const initialViewMode = (searchParams.get('view') as CalendarViewMode) || 'month';
  const initialDateStr = searchParams.get('date');
  const initialEventId = searchParams.get('eventId');
  const initialModuleFilter = searchParams.get('filter') as CalendarEventSourceModule | null;

  const [filterState, setFilterState] = useState<CalendarFilterState>({
    search: '',
    selectedModules: initialModuleFilter ? [initialModuleFilter] : [],
    selectedCategories: [],
    showCompleted: true,
    showSharedOnly: false,
    viewMode: initialViewMode,
    currentDate: initialDateStr ? new Date(initialDateStr) : new Date(),
  });

  // Modals state
  const [selectedEvent, setSelectedEvent] = useState<AggregatedCalendarEvent | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const [eventToEdit, setEventToEdit] = useState<AggregatedCalendarEvent | null>(null);
  const [presetDate, setPresetDate] = useState<Date | null>(null);
  const [presetHour, setPresetHour] = useState<number | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  // Aggregated Server State Query
  const { events, allEvents, isLoading, isError } = useAggregatedCalendarEvents(filterState);

  // Personal Event Mutations
  const createMutation = useCreateCalendarEntry();
  const updateMutation = useUpdateCalendarEntry();
  const deleteMutation = useDeleteCalendarEntry();

  // Handle deep-linked event auto-opening
  useEffect(() => {
    if (initialEventId && allEvents.length > 0) {
      const found = allEvents.find((e) => e.originalId === initialEventId || e.id === initialEventId);
      if (found) {
        setSelectedEvent(found);
        setIsDetailsOpen(true);
      }
    }
  }, [initialEventId, allEvents]);

  // Handlers
  const handleFilterChange = (updated: Partial<CalendarFilterState>) => {
    setFilterState((prev) => ({ ...prev, ...updated }));
  };

  const handleResetFilters = () => {
    setFilterState({
      search: '',
      selectedModules: [],
      selectedCategories: [],
      showCompleted: true,
      showSharedOnly: false,
      viewMode: 'month',
      currentDate: new Date(),
    });
  };

  // Date Navigation Handlers
  const handleNavigatePrev = () => {
    setFilterState((prev) => {
      const curr = new Date(prev.currentDate);
      if (prev.viewMode === 'month') {
        curr.setMonth(curr.getMonth() - 1);
      } else if (prev.viewMode === 'week') {
        curr.setDate(curr.getDate() - 7);
      } else if (prev.viewMode === 'day') {
        curr.setDate(curr.getDate() - 1);
      }
      return { ...prev, currentDate: curr };
    });
  };

  const handleNavigateNext = () => {
    setFilterState((prev) => {
      const curr = new Date(prev.currentDate);
      if (prev.viewMode === 'month') {
        curr.setMonth(curr.getMonth() + 1);
      } else if (prev.viewMode === 'week') {
        curr.setDate(curr.getDate() + 7);
      } else if (prev.viewMode === 'day') {
        curr.setDate(curr.getDate() + 1);
      }
      return { ...prev, currentDate: curr };
    });
  };

  const handleToday = () => {
    setFilterState((prev) => ({ ...prev, currentDate: new Date() }));
  };

  const handleSelectDate = (date: Date) => {
    setFilterState((prev) => ({ ...prev, currentDate: date }));
  };

  // Event Action Handlers
  const handleSelectEvent = (event: AggregatedCalendarEvent) => {
    setSelectedEvent(event);
    setIsDetailsOpen(true);
  };

  const handleOpenCreateModal = () => {
    setEventToEdit(null);
    setPresetDate(filterState.currentDate);
    setPresetHour(9);
    setIsFormOpen(true);
  };

  const handleOpenCreateForDate = (date: Date) => {
    setEventToEdit(null);
    setPresetDate(date);
    setPresetHour(9);
    setIsFormOpen(true);
  };

  const handleOpenCreateForTime = (date: Date, hour: number) => {
    setEventToEdit(null);
    setPresetDate(date);
    setPresetHour(hour);
    setIsFormOpen(true);
  };

  const handleEditPersonalEvent = (event: AggregatedCalendarEvent) => {
    setEventToEdit(event);
    setIsFormOpen(true);
  };

  const handleDeletePersonalEvent = (id: string) => {
    deleteMutation.mutate(id, {
      onSuccess: () => {
        toast.success('Personal event deleted.');
        setIsDetailsOpen(false);
      },
      onError: (err) => {
        toast.error(`Error deleting event: ${err.message}`);
      },
    });
  };

  const handleUpdatePersonalEventTime = (id: string, payload: { startTime: string; endTime: string }) => {
    updateMutation.mutate(
      { id, payload },
      {
        onSuccess: () => {
          toast.success('Event rescheduled successfully.');
        },
        onError: (err) => {
          toast.error(`Rescheduling failed: ${err.message}`);
        },
      }
    );
  };

  // Count conflict events
  const conflictEventsCount = useMemo(() => {
    return events.filter((e) => e.hasConflict).length;
  }, [events]);

  return (
    <div className="min-h-screen bg-gray-50/50">
      <Header />

      <main className="p-4 sm:p-8">
        <div className="max-w-[1440px] mx-auto space-y-8">
          {/* Top Header Banner & Navigation */}
          <CalendarHeader
            currentDate={filterState.currentDate}
            viewMode={filterState.viewMode}
            searchQuery={filterState.search}
            onViewModeChange={(viewMode) => handleFilterChange({ viewMode })}
            onNavigatePrev={handleNavigatePrev}
            onNavigateNext={handleNavigateNext}
            onToday={handleToday}
            onDateSelect={handleSelectDate}
            onSearchChange={(search) => handleFilterChange({ search })}
            onOpenCreateModal={handleOpenCreateModal}
            totalEventsCount={events.length}
            conflictEventsCount={conflictEventsCount}
          />

          {/* Main Content Area: Sidebar Filter + Calendar View */}
          <div className="flex flex-col lg:flex-row gap-8 items-start">
            {/* Sidebar Filter */}
            <CalendarSidebarFilter
              filterState={filterState}
              onFilterChange={handleFilterChange}
              onResetFilters={handleResetFilters}
              conflictEventsCount={conflictEventsCount}
              totalEventsCount={events.length}
            />

            {/* Calendar Main View Area */}
            <div className="flex-1 w-full min-w-0">
              <CalendarErrorBoundary fallbackTitle="Error loading Calendar view">
                {isLoading ? (
                  <CalendarSkeleton />
                ) : isError ? (
                  <div className="p-8 bg-red-50 text-red-700 rounded-3xl text-center border border-red-200">
                    <h3 className="font-bold text-base">Failed to fetch calendar data</h3>
                    <p className="text-xs mt-1">There was an issue retrieving aggregated calendar entries.</p>
                  </div>
                ) : (
                  <>
                    {filterState.viewMode === 'month' && (
                      <MonthView
                        currentDate={filterState.currentDate}
                        events={events}
                        onSelectEvent={handleSelectEvent}
                        onSelectDate={handleSelectDate}
                        onOpenCreateEventForDate={handleOpenCreateForDate}
                      />
                    )}

                    {filterState.viewMode === 'week' && (
                      <WeekView
                        currentDate={filterState.currentDate}
                        events={events}
                        onSelectEvent={handleSelectEvent}
                        onOpenCreateEventForTime={handleOpenCreateForTime}
                        onUpdatePersonalEvent={handleUpdatePersonalEventTime}
                      />
                    )}

                    {filterState.viewMode === 'day' && (
                      <DayView
                        currentDate={filterState.currentDate}
                        events={events}
                        onSelectEvent={handleSelectEvent}
                        onOpenCreateEventForTime={handleOpenCreateForTime}
                      />
                    )}

                    {filterState.viewMode === 'agenda' && (
                      <AgendaView
                        events={events}
                        onSelectEvent={handleSelectEvent}
                      />
                    )}
                  </>
                )}
              </CalendarErrorBoundary>
            </div>
          </div>
        </div>
      </main>

      {/* Modals */}
      <Suspense fallback={null}>
        <EventDetailsModal
          isOpen={isDetailsOpen}
          event={selectedEvent}
          onClose={() => setIsDetailsOpen(false)}
          onEditPersonalEvent={handleEditPersonalEvent}
          onDeletePersonalEvent={handleDeletePersonalEvent}
        />

        <EventFormModal
          isOpen={isFormOpen}
          onClose={() => setIsFormOpen(false)}
          eventToEdit={eventToEdit}
          presetDate={presetDate}
          presetHour={presetHour}
          onSubmitCreate={(payload) => {
            createMutation.mutate(payload, {
              onSuccess: () => {
                toast.success('Personal event created!');
              },
              onError: (err) => {
                toast.error(`Error creating event: ${err.message}`);
              },
            });
          }}
          onSubmitUpdate={(id, payload) => {
            updateMutation.mutate(
              { id, payload },
              {
                onSuccess: () => {
                  toast.success('Personal event updated!');
                },
                onError: (err) => {
                  toast.error(`Error updating event: ${err.message}`);
                },
              }
            );
          }}
          isSubmitting={createMutation.isPending || updateMutation.isPending}
        />
      </Suspense>
    </div>
  );
}

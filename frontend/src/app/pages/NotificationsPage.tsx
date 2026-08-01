import React, { useState, useMemo, lazy, Suspense } from 'react';
import {
  NotificationCategory,
  NotificationPriority,
  NotificationDeliveryStatus,
  NotificationItem,
} from '../../models/notification.model';
import {
  useNotifications,
  useNotificationStats,
  useMarkAsRead,
  useMarkAsUnread,
  useMarkAllAsRead,
  useArchiveNotification,
  useRestoreNotification,
  useDeleteNotification,
} from '../../hooks/notifications';
import { NotificationHeader } from '../components/notifications/NotificationHeader';
import { NotificationStatsWidget } from '../components/notifications/NotificationStatsWidget';
import { NotificationCategoryTabs } from '../components/notifications/NotificationCategoryTabs';
import { NotificationItemCard } from '../components/notifications/NotificationItemCard';
import { NotificationSkeleton } from '../components/notifications/NotificationSkeleton';
import { NotificationEmptyState } from '../components/notifications/NotificationEmptyState';
import { NotificationErrorBoundary } from '../components/notifications/NotificationErrorBoundary';

const NotificationDetailModal = lazy(() =>
  import('../components/notifications/NotificationDetailModal').then((m) => ({ default: m.NotificationDetailModal }))
);
import { ChevronLeft, ChevronRight } from 'lucide-react';

const PAGE_SIZE = 8;

export function NotificationsPage() {
  const [selectedStatus, setSelectedStatus] = useState<'ALL' | 'UNREAD' | 'READ' | 'ARCHIVED'>('ALL');
  const [selectedCategory, setSelectedCategory] = useState<NotificationCategory | 'ALL'>('ALL');
  const [selectedPriority, setSelectedPriority] = useState<NotificationPriority | 'ALL'>('ALL');
  const [selectedDeliveryStatus, setSelectedDeliveryStatus] = useState<NotificationDeliveryStatus | 'ALL'>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState<'createdAt' | 'priority' | 'title'>('createdAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [page, setPage] = useState(1);
  const [selectedItem, setSelectedItem] = useState<NotificationItem | null>(null);

  // Queries
  const { data: stats } = useNotificationStats();
  const { data: notifications = [], isLoading, isError, error } = useNotifications({
    status: selectedStatus,
    category: selectedCategory,
    priority: selectedPriority,
    deliveryStatus: selectedDeliveryStatus,
    search: searchQuery,
    sortBy,
    sortOrder,
  });

  // Mutations
  const markAsReadMutation = useMarkAsRead();
  const markAsUnreadMutation = useMarkAsUnread();
  const markAllAsReadMutation = useMarkAllAsRead();
  const archiveMutation = useArchiveNotification();
  const restoreMutation = useRestoreNotification();
  const deleteMutation = useDeleteNotification();

  // Pagination Math
  const totalItems = notifications.length;
  const totalPages = Math.ceil(totalItems / PAGE_SIZE) || 1;
  const paginatedNotifications = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return notifications.slice(start, start + PAGE_SIZE);
  }, [notifications, page]);

  // Handlers
  const handleToggleRead = (id: string, currentlyRead: boolean) => {
    if (currentlyRead) {
      markAsUnreadMutation.mutate(id);
    } else {
      markAsReadMutation.mutate(id);
    }
  };

  const handleToggleArchive = (id: string, currentlyArchived: boolean) => {
    if (currentlyArchived) {
      restoreMutation.mutate(id);
    } else {
      archiveMutation.mutate(id);
    }
  };

  const handleDelete = (id: string) => {
    deleteMutation.mutate(id);
  };

  const handleResetFilters = () => {
    setSearchQuery('');
    setSelectedCategory('ALL');
    setSelectedPriority('ALL');
    setSelectedDeliveryStatus('ALL');
    setSelectedStatus('ALL');
    setPage(1);
  };

  const hasActiveFilters =
    searchQuery.trim() !== '' ||
    selectedCategory !== 'ALL' ||
    selectedPriority !== 'ALL' ||
    selectedDeliveryStatus !== 'ALL';

  return (
    <div className="p-6 md:p-8 space-y-6 max-w-7xl mx-auto">
      {/* Page Header with Controls */}
      <NotificationHeader
        searchQuery={searchQuery}
        onSearchChange={(q) => {
          setSearchQuery(q);
          setPage(1);
        }}
        selectedPriority={selectedPriority}
        onPriorityChange={(p) => {
          setSelectedPriority(p);
          setPage(1);
        }}
        selectedDeliveryStatus={selectedDeliveryStatus}
        onDeliveryStatusChange={(d) => {
          setSelectedDeliveryStatus(d);
          setPage(1);
        }}
        sortBy={sortBy}
        onSortByChange={setSortBy}
        sortOrder={sortOrder}
        onToggleSortOrder={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
        onMarkAllAsRead={() => markAllAsReadMutation.mutate(undefined)}
        isMarkingAllRead={markAllAsReadMutation.isPending}
      />

      {/* Summary Statistics Cards */}
      <NotificationStatsWidget
        stats={stats}
        currentStatus={selectedStatus}
        onStatusChange={(status) => {
          setSelectedStatus(status);
          setPage(1);
        }}
      />

      {/* Category Tabs */}
      <NotificationCategoryTabs
        selectedCategory={selectedCategory}
        onSelectCategory={(cat) => {
          setSelectedCategory(cat);
          setPage(1);
        }}
      />

      {/* Main List Area wrapped in Section Error Boundary */}
      <NotificationErrorBoundary fallbackTitle="Notification List Error">
        {isLoading ? (
          <NotificationSkeleton />
        ) : isError ? (
          <div className="p-8 bg-red-50 text-red-700 rounded-3xl border border-red-200 text-center text-xs">
            Failed to load notifications: {error?.message || 'Unknown network error.'}
          </div>
        ) : paginatedNotifications.length === 0 ? (
          <NotificationEmptyState
            hasFilters={hasActiveFilters}
            status={selectedStatus}
            onResetFilters={handleResetFilters}
          />
        ) : (
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-3">
              {paginatedNotifications.map((item) => (
                <NotificationItemCard
                  key={item.id}
                  item={item}
                  onSelect={setSelectedItem}
                  onToggleRead={handleToggleRead}
                  onToggleArchive={handleToggleArchive}
                  onDelete={handleDelete}
                />
              ))}
            </div>

            {/* Pagination Bar */}
            {totalPages > 1 && (
              <div className="bg-white rounded-2xl p-4 border border-gray-200 flex items-center justify-between gap-4 text-xs">
                <span className="text-gray-500 font-medium">
                  Showing {(page - 1) * PAGE_SIZE + 1} to {Math.min(page * PAGE_SIZE, totalItems)} of {totalItems} notifications
                </span>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => setPage((p) => Math.max(1, p - 1))}
                    disabled={page === 1}
                    className="p-2 border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-40 transition-colors"
                  >
                    <ChevronLeft className="w-4 h-4 text-gray-600" />
                  </button>

                  <span className="font-semibold text-gray-800 px-2">
                    Page {page} of {totalPages}
                  </span>

                  <button
                    onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
                    disabled={page === totalPages}
                    className="p-2 border border-gray-200 rounded-xl hover:bg-gray-50 disabled:opacity-40 transition-colors"
                  >
                    <ChevronRight className="w-4 h-4 text-gray-600" />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </NotificationErrorBoundary>

      {/* Item Detail Modal */}
      <Suspense fallback={null}>
        <NotificationDetailModal
          item={selectedItem}
          onClose={() => setSelectedItem(null)}
          onToggleRead={handleToggleRead}
          onToggleArchive={handleToggleArchive}
          onDelete={handleDelete}
        />
      </Suspense>
    </div>
  );
}

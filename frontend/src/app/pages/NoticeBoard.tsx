import { useState, lazy, Suspense } from 'react';
import { Header } from '../components/Header';
import { useNotices } from '../../hooks/notices/useNotices';
import { useUnreadNoticesCount } from '../../hooks/notices/useUnreadNoticesCount';
import { Notice, NoticeCategory, NoticePriority } from '../../models/notice.model';
import { NoticeCard } from '../components/notices/NoticeCard';
import { NoticeFilters, NoticeTab } from '../components/notices/NoticeFilters';
import { NoticeSkeleton, NoticeEmptyState } from '../components/notices/NoticeSkeleton';
import { NoticeErrorBoundary } from '../components/notices/NoticeErrorBoundary';
import { Bell, Sparkles, Plus, AlertCircle, Pin } from 'lucide-react';

const NoticeDetailsModal = lazy(() =>
  import('../components/notices/NoticeDetailsModal').then((m) => ({ default: m.NoticeDetailsModal }))
);
const NoticeFormModal = lazy(() =>
  import('../components/notices/NoticeFormModal').then((m) => ({ default: m.NoticeFormModal }))
);

export function NoticeBoard() {
  // Filter & Query States
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<NoticeCategory | 'ALL'>('ALL');
  const [selectedPriority, setSelectedPriority] = useState<NoticePriority | 'ALL'>('ALL');
  const [activeTab, setActiveTab] = useState<NoticeTab>('ALL');
  const [sortBy, setSortBy] = useState<'publishedAt' | 'priority' | 'title'>('publishedAt');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');

  // Modals state
  const [selectedNotice, setSelectedNotice] = useState<Notice | null>(null);
  const [noticeToEdit, setNoticeToEdit] = useState<Notice | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  // Compute status parameter based on activeTab
  const getStatusParam = () => {
    switch (activeTab) {
      case 'PINNED':
        return undefined; // Handled via isPinned param
      case 'UNREAD':
        return 'UNREAD';
      case 'ARCHIVED':
        return 'ARCHIVED';
      default:
        return 'ACTIVE';
    }
  };

  // Fetch notices via React Query SDK Hook
  const { data: notices = [], isLoading, isError, refetch } = useNotices({
    search: searchQuery,
    category: selectedCategory,
    priority: selectedPriority,
    status: getStatusParam(),
    isPinned: activeTab === 'PINNED' ? true : undefined,
    sortBy,
  });

  // Additional filter for IMPORTANT tab if active
  const displayedNotices = notices.filter((notice) => {
    if (activeTab === 'IMPORTANT') {
      return notice.isImportant;
    }
    return true;
  });

  // Unread Count Hook
  const unreadCountQuery = useUnreadNoticesCount();
  const unreadCount = unreadCountQuery.data || 0;

  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedCategory('ALL');
    setSelectedPriority('ALL');
    setActiveTab('ALL');
  };

  const handleOpenCreate = () => {
    setNoticeToEdit(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (notice: Notice) => {
    setNoticeToEdit(notice);
    setIsFormOpen(true);
  };

  return (
    <div className="min-h-screen bg-gray-50/50 dark:bg-background text-foreground transition-colors duration-150">
      <Header />
      <main className="p-4 sm:p-8">
        <div className="max-w-[1440px] mx-auto space-y-8">
          {/* Top Hero Banner */}
          <div className="relative overflow-hidden bg-gradient-to-r from-slate-900 via-blue-950 to-indigo-900 text-white rounded-3xl p-8 shadow-xl">
            <div className="absolute right-0 top-0 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />
            <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
              <div className="space-y-2">
                <div className="inline-flex items-center gap-2 px-3 py-1 bg-white/10 backdrop-blur-md rounded-full text-xs font-semibold text-blue-200 border border-white/10">
                  <Sparkles className="w-3.5 h-3.5 text-amber-400" />
                  Official Campus Bulletin & Directives
                </div>
                <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                  Notice Board
                </h1>
                <p className="text-blue-100/80 text-sm max-w-xl">
                  Stay updated with official university announcements, examination timetables, scholarship deadlines, council circulars, and placement drives.
                </p>
              </div>

              <div className="flex items-center gap-4 shrink-0">
                <div className="bg-white/10 backdrop-blur-md p-4 rounded-2xl border border-white/10 text-center min-w-[120px]">
                  <div className="flex items-center justify-center gap-1.5 text-amber-400 font-bold text-xl mb-0.5">
                    <Bell className="w-5 h-5" />
                    {unreadCount}
                  </div>
                  <span className="text-[11px] text-blue-200 uppercase font-semibold tracking-wider">
                    Unread Notices
                  </span>
                </div>

                <button
                  onClick={handleOpenCreate}
                  className="inline-flex items-center gap-2 px-5 py-3.5 bg-[#2563EB] hover:bg-blue-600 text-white font-bold text-xs rounded-2xl transition-all shadow-lg hover:shadow-blue-500/25 active:scale-95"
                >
                  <Plus className="w-4 h-4" />
                  Create Notice
                </button>
              </div>
            </div>
          </div>

          {/* Section Level Error Boundary wrapping filters & notice list */}
          <NoticeErrorBoundary fallbackTitle="Failed to load Notice Board">
            {/* Filters Bar */}
            <NoticeFilters
              searchQuery={searchQuery}
              onSearchChange={setSearchQuery}
              selectedCategory={selectedCategory}
              onCategoryChange={setSelectedCategory}
              selectedPriority={selectedPriority}
              onPriorityChange={setSelectedPriority}
              activeTab={activeTab}
              onTabChange={setActiveTab}
              sortBy={sortBy}
              onSortByChange={setSortBy}
              viewMode={viewMode}
              onViewModeChange={setViewMode}
              unreadCount={unreadCount}
              onCreateNotice={handleOpenCreate}
            />

            {/* Error state */}
            {isError && (
              <div className="p-6 bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 rounded-2xl text-center space-y-4 shadow-xs">
                <AlertCircle className="w-8 h-8 text-destructive mx-auto" />
                <h3 className="text-base font-bold text-foreground">Failed to fetch notices</h3>
                <p className="text-xs text-muted-foreground max-w-md mx-auto leading-relaxed">Check your network connection and try again.</p>
                <button
                  onClick={() => refetch()}
                  className="inline-flex items-center gap-2 px-4 py-2 bg-destructive hover:bg-destructive/90 text-white rounded-lg text-xs font-semibold transition-all active:scale-[0.98] cursor-pointer shadow-xs"
                >
                  Retry Loading
                </button>
              </div>
            )}

            {/* Loading Skeletons */}
            {isLoading && <NoticeSkeleton count={6} />}

            {/* Empty State */}
            {!isLoading && !isError && displayedNotices.length === 0 && (
              <NoticeEmptyState
                onClearFilters={handleClearFilters}
                onCreateNew={handleOpenCreate}
              />
            )}

            {/* Notice Grid / List Rendering */}
            {!isLoading && !isError && displayedNotices.length > 0 && (
              <div
                className={
                  viewMode === 'grid'
                    ? 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6'
                    : 'space-y-4'
                }
              >
                {displayedNotices.map((notice) => (
                  <NoticeCard
                    key={notice.id}
                    notice={notice}
                    onSelect={setSelectedNotice}
                    onEdit={handleOpenEdit}
                  />
                ))}
              </div>
            )}
          </NoticeErrorBoundary>
        </div>
      </main>

      {/* Notice Detail & Form Modals */}
      <Suspense fallback={null}>
        <NoticeDetailsModal
          notice={selectedNotice}
          onClose={() => setSelectedNotice(null)}
          onEdit={handleOpenEdit}
        />

        <NoticeFormModal
          isOpen={isFormOpen}
          onClose={() => setIsFormOpen(false)}
          noticeToEdit={noticeToEdit}
        />
      </Suspense>
    </div>
  );
}

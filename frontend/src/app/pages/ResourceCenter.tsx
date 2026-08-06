import React, { useState, useMemo, lazy, Suspense } from 'react';
import { Header } from '../components/Header';
import {
  useResources,
  useFeaturedResources,
  useRecentResources,
  usePopularResources,
  useBookmarkedResources,
  useCreateResource,
  useUpdateResource,
  useDeleteResource,
  useBookmarkResource,
  useDownloadResource,
} from '../../hooks';
import { Resource, CreateResourcePayload, UpdateResourcePayload } from '../../models/resource.model';
import { ResourceCard } from '../components/resources/ResourceCard';
import { ResourceFilterBar } from '../components/resources/ResourceFilterBar';
import { ResourceSkeleton } from '../components/resources/ResourceSkeleton';
import { ResourceErrorBoundary } from '../components/resources/ResourceErrorBoundary';

const ResourceDetailsModal = lazy(() =>
  import('../components/resources/ResourceDetailsModal').then((m) => ({ default: m.ResourceDetailsModal }))
);
const ResourceUploadModal = lazy(() =>
  import('../components/resources/ResourceUploadModal').then((m) => ({ default: m.ResourceUploadModal }))
);
import {
  BookOpen,
  FileText,
  Upload,
  Sparkles,
  Bookmark,
  TrendingUp,
  Clock,
  FolderOpen,
  Plus,
  Search,
  ChevronLeft,
  ChevronRight,
  Layers,
} from 'lucide-react';

export function ResourceCenter() {
  const [activeTab, setActiveTab] = useState<'directory' | 'featured' | 'recent' | 'popular' | 'bookmarked'>('directory');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [selectedFileType, setSelectedFileType] = useState('All');
  const [sortBy, setSortBy] = useState<'newest' | 'popular' | 'title' | 'size'>('newest');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [currentPage, setCurrentPage] = useState(1);

  // Modals state
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [selectedResourceDetails, setSelectedResourceDetails] = useState<Resource | null>(null);
  const [editingResource, setEditingResource] = useState<Resource | null>(null);

  // React Query Hooks (Parallel Queries)
  const mainQueryParams = useMemo(
    () => ({
      search: searchQuery,
      category: selectedCategory,
      fileType: selectedFileType,
      sort: sortBy,
      page: currentPage,
      limit: 12,
    }),
    [searchQuery, selectedCategory, selectedFileType, sortBy, currentPage]
  );

  const { data: mainData, isLoading: isMainLoading, isError: isMainError, refetch: refetchMain } = useResources(mainQueryParams);
  const { data: featuredResources, isLoading: isFeaturedLoading } = useFeaturedResources();
  const { data: recentResources } = useRecentResources();
  const { data: popularResources } = usePopularResources();
  const { data: bookmarkedResources, isLoading: isBookmarkedLoading } = useBookmarkedResources();

  // Mutation Hooks
  const createMutation = useCreateResource();
  const updateMutation = useUpdateResource();
  const deleteMutation = useDeleteResource();
  const bookmarkMutation = useBookmarkResource();
  const downloadMutation = useDownloadResource();

  const handleDownload = (resource: Resource) => {
    downloadMutation.mutate({ id: resource.id, fileName: resource.originalFileName || resource.fileName });
  };

  const handleToggleBookmark = (resource: Resource) => {
    bookmarkMutation.mutate({ id: resource.id, isBookmarked: Boolean(resource.isBookmarked) });
  };

  const handleCreateResource = async (payload: CreateResourcePayload) => {
    await createMutation.mutateAsync(payload);
  };

  const handleUpdateResource = async (id: string, payload: UpdateResourcePayload) => {
    await updateMutation.mutateAsync({ id, payload });
  };

  const handleDeleteResource = async (resource: Resource) => {
    if (window.confirm(`Are you sure you want to delete "${resource.title}"?`)) {
      await deleteMutation.mutateAsync(resource.id);
    }
  };

  // Determine list items based on active tab
  const displayResources = useMemo(() => {
    if (activeTab === 'featured') return featuredResources || [];
    if (activeTab === 'recent') return recentResources || [];
    if (activeTab === 'popular') return popularResources || [];
    if (activeTab === 'bookmarked') return bookmarkedResources || [];
    return mainData?.resources || [];
  }, [activeTab, mainData, featuredResources, recentResources, popularResources, bookmarkedResources]);

  const isCurrentLoading =
    activeTab === 'directory'
      ? isMainLoading
      : activeTab === 'featured'
      ? isFeaturedLoading
      : activeTab === 'bookmarked'
      ? isBookmarkedLoading
      : false;

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-background flex flex-col font-sans text-foreground transition-colors duration-150">
      <Header />

      <main className="p-4 sm:p-6 lg:p-8 flex-1 max-w-[1440px] w-full mx-auto">
        {/* Hero Section */}
        <div className="bg-gradient-to-r from-blue-900 via-indigo-900 to-slate-900 rounded-3xl p-8 sm:p-10 text-white mb-8 shadow-xl relative overflow-hidden">
          <div className="absolute right-0 top-0 translate-x-8 -translate-y-8 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

          <div className="relative z-10 max-w-3xl">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-200 text-xs font-semibold mb-4 backdrop-blur-md">
              <Sparkles className="w-4 h-4 text-blue-400" />
              Academic Knowledge Base & Resource Hub
            </div>
            <h1 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight mb-3">
              Resource Center
            </h1>
            <p className="text-blue-100/90 text-base sm:text-lg mb-8 leading-relaxed">
              Discover, preview, download, and share lecture notes, lab manuals, past exam papers, syllabi, and administrative templates.
            </p>

            <div className="flex flex-wrap items-center gap-4">
              <button
                onClick={() => {
                  setEditingResource(null);
                  setIsUploadModalOpen(true);
                }}
                className="inline-flex items-center gap-2 px-6 py-3.5 bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm rounded-xl transition-all shadow-lg hover:shadow-blue-500/30 active:scale-95"
              >
                <Plus className="w-5 h-5" />
                Upload New Resource
              </button>

              <button
                onClick={() => setActiveTab('bookmarked')}
                className="inline-flex items-center gap-2 px-5 py-3.5 bg-white/10 hover:bg-white/20 border border-white/20 text-white font-medium text-sm rounded-xl backdrop-blur-md transition-all"
              >
                <Bookmark className="w-4 h-4 text-amber-400" />
                My Bookmarks ({bookmarkedResources?.length || 0})
              </button>
            </div>
          </div>
        </div>

        {/* Section Navigation Tabs */}
        <div className="flex items-center gap-3 border-b border-gray-200 mb-6 overflow-x-auto scrollbar-none pb-1">
          <button
            onClick={() => setActiveTab('directory')}
            className={`flex items-center gap-2 px-4 py-3 font-semibold text-sm border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'directory'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <FolderOpen className="w-4 h-4" />
            All Directory
          </button>

          <button
            onClick={() => setActiveTab('featured')}
            className={`flex items-center gap-2 px-4 py-3 font-semibold text-sm border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'featured'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Sparkles className="w-4 h-4 text-amber-500" />
            Featured ({featuredResources?.length || 0})
          </button>

          <button
            onClick={() => setActiveTab('recent')}
            className={`flex items-center gap-2 px-4 py-3 font-semibold text-sm border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'recent'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Clock className="w-4 h-4" />
            Recently Uploaded
          </button>

          <button
            onClick={() => setActiveTab('popular')}
            className={`flex items-center gap-2 px-4 py-3 font-semibold text-sm border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'popular'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <TrendingUp className="w-4 h-4 text-emerald-600" />
            Popular Downloads
          </button>

          <button
            onClick={() => setActiveTab('bookmarked')}
            className={`flex items-center gap-2 px-4 py-3 font-semibold text-sm border-b-2 transition-all whitespace-nowrap ${
              activeTab === 'bookmarked'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-900'
            }`}
          >
            <Bookmark className="w-4 h-4 text-amber-500" />
            Bookmarked ({bookmarkedResources?.length || 0})
          </button>
        </div>

        {/* Section-level Error Boundary wrapper for Filters & Resource Discovery */}
        <ResourceErrorBoundary onReset={refetchMain}>
          {/* Filter Bar (Only active for directory tab or searching) */}
          {activeTab === 'directory' && (
            <ResourceFilterBar
              searchQuery={searchQuery}
              onSearchChange={(q) => {
                setSearchQuery(q);
                setCurrentPage(1);
              }}
              selectedCategory={selectedCategory}
              onCategoryChange={(c) => {
                setSelectedCategory(c);
                setCurrentPage(1);
              }}
              selectedFileType={selectedFileType}
              onFileTypeChange={(ft) => {
                setSelectedFileType(ft);
                setCurrentPage(1);
              }}
              sortBy={sortBy}
              onSortChange={(s) => setSortBy(s)}
              viewMode={viewMode}
              onViewModeChange={setViewMode}
            />
          )}

          {/* Content Listing Grid / List */}
          {isCurrentLoading ? (
            <ResourceSkeleton viewMode={viewMode} count={6} />
          ) : isMainError ? (
            <div className="bg-destructive/5 dark:bg-destructive/10 border border-destructive/20 rounded-2xl p-8 text-center my-6 space-y-4 shadow-xs">
              <h3 className="font-bold text-foreground text-lg mb-2">Error Loading Resource Catalog</h3>
              <p className="text-xs text-muted-foreground max-w-md mx-auto leading-relaxed">Could not connect to resources service.</p>
              <button
                onClick={() => refetchMain()}
                className="inline-flex items-center gap-2 px-4 py-2 bg-destructive hover:bg-destructive/90 text-white rounded-lg text-xs font-semibold transition-all active:scale-[0.98] cursor-pointer shadow-xs"
              >
                Try Again
              </button>
            </div>
          ) : displayResources.length === 0 ? (
            /* Empty state */
            <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center my-6">
              <div className="w-16 h-16 bg-blue-50 text-blue-500 rounded-full flex items-center justify-center mx-auto mb-4">
                <FolderOpen className="w-8 h-8" />
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">No Resources Found</h3>
              <p className="text-sm text-gray-500 max-w-md mx-auto mb-6">
                {searchQuery || selectedCategory !== 'All' || selectedFileType !== 'All'
                  ? 'No documents matched your current filter criteria. Try resetting filters or search term.'
                  : activeTab === 'bookmarked'
                  ? 'You have not bookmarked any resources yet. Click the bookmark icon on any document card to save it.'
                  : 'Be the first to upload and share study materials with your classmates!'}
              </p>
              {(searchQuery || selectedCategory !== 'All' || selectedFileType !== 'All') && (
                <button
                  onClick={() => {
                    setSearchQuery('');
                    setSelectedCategory('All');
                    setSelectedFileType('All');
                  }}
                  className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold text-sm rounded-xl transition-colors"
                >
                  Clear Filters
                </button>
              )}
            </div>
          ) : (
            <>
              {/* Resource Cards Container */}
              {viewMode === 'grid' ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                  {displayResources.map((resource) => (
                    <ResourceCard
                      key={resource.id}
                      resource={resource}
                      viewMode="grid"
                      onViewDetails={(res) => setSelectedResourceDetails(res)}
                      onDownload={handleDownload}
                      onToggleBookmark={handleToggleBookmark}
                      onEdit={(res) => {
                        setEditingResource(res);
                        setIsUploadModalOpen(true);
                      }}
                      onDelete={handleDeleteResource}
                    />
                  ))}
                </div>
              ) : (
                <div className="space-y-3 mb-8">
                  {displayResources.map((resource) => (
                    <ResourceCard
                      key={resource.id}
                      resource={resource}
                      viewMode="list"
                      onViewDetails={(res) => setSelectedResourceDetails(res)}
                      onDownload={handleDownload}
                      onToggleBookmark={handleToggleBookmark}
                      onEdit={(res) => {
                        setEditingResource(res);
                        setIsUploadModalOpen(true);
                      }}
                      onDelete={handleDeleteResource}
                    />
                  ))}
                </div>
              )}

              {/* Pagination controls for main directory tab */}
              {activeTab === 'directory' && mainData && mainData.totalPages > 1 && (
                <div className="flex items-center justify-between border-t border-gray-200 pt-6">
                  <p className="text-xs text-gray-500 font-medium">
                    Showing <span className="font-semibold text-gray-800">{(currentPage - 1) * 12 + 1}</span> to{' '}
                    <span className="font-semibold text-gray-800">
                      {Math.min(currentPage * 12, mainData.total)}
                    </span>{' '}
                    of <span className="font-semibold text-gray-800">{mainData.total}</span> resources
                  </p>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setCurrentPage((p) => Math.max(p - 1, 1))}
                      disabled={currentPage === 1}
                      className="p-2 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-100 disabled:opacity-40 transition-colors"
                    >
                      <ChevronLeft className="w-4 h-4" />
                    </button>
                    <span className="text-xs font-semibold px-3 py-1 bg-gray-100 rounded-lg text-gray-700">
                      Page {currentPage} of {mainData.totalPages}
                    </span>
                    <button
                      onClick={() => setCurrentPage((p) => Math.min(p + 1, mainData.totalPages))}
                      disabled={currentPage === mainData.totalPages}
                      className="p-2 border border-gray-200 rounded-lg text-gray-600 hover:bg-gray-100 disabled:opacity-40 transition-colors"
                    >
                      <ChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </ResourceErrorBoundary>

        {/* Modals */}
        <Suspense fallback={null}>
          <ResourceDetailsModal
            resource={selectedResourceDetails}
            isOpen={Boolean(selectedResourceDetails)}
            onClose={() => setSelectedResourceDetails(null)}
            onDownload={handleDownload}
            onToggleBookmark={handleToggleBookmark}
          />

          <ResourceUploadModal
            isOpen={isUploadModalOpen}
            onClose={() => {
              setIsUploadModalOpen(false);
              setEditingResource(null);
            }}
            onSubmit={handleCreateResource}
            onUpdate={handleUpdateResource}
            editingResource={editingResource}
          />
        </Suspense>
      </main>
    </div>
  );
}

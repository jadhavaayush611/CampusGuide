import React, { lazy, Suspense } from 'react';
import * as DialogPrimitive from '@radix-ui/react-dialog';
import { Resource } from '../../../models/resource.model';
import { X, Download, Bookmark, BookmarkCheck, Calendar, User, FileText, Tag, Hash, Eye } from 'lucide-react';

const ResourcePreview = lazy(() =>
  import('./ResourcePreview').then((m) => ({ default: m.ResourcePreview }))
);

interface ResourceDetailsModalProps {
  resource: Resource | null;
  isOpen: boolean;
  onClose: () => void;
  onDownload: (resource: Resource) => void;
  onToggleBookmark: (resource: Resource) => void;
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(dateStr: string): string {
  if (!dateStr) return 'N/A';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
  } catch {
    return dateStr;
  }
}

export function ResourceDetailsModal({
  resource,
  isOpen,
  onClose,
  onDownload,
  onToggleBookmark,
}: ResourceDetailsModalProps) {
  if (!isOpen || !resource) return null;

  return (
    <DialogPrimitive.Root open={isOpen} onOpenChange={(open) => { if (!open) onClose(); }}>
      <DialogPrimitive.Portal>
        <DialogPrimitive.Overlay className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200" />
        <DialogPrimitive.Content className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl border border-gray-200 w-full max-w-4xl max-h-[90vh] flex flex-col overflow-hidden z-10">
            {/* Modal Header */}
            <div className="p-6 border-b border-gray-200 flex items-start justify-between gap-4 bg-gray-50/50">
              <div>
                <div className="flex items-center gap-2 flex-wrap mb-2">
                  <span className="text-xs font-semibold px-3 py-1 rounded-full bg-blue-100 text-blue-700 border border-blue-200">
                    {resource.category}
                  </span>
                  <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-gray-100 text-gray-700 border border-gray-200 uppercase">
                    {resource.fileType}
                  </span>
                  {resource.isFeatured && (
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-amber-100 text-amber-800 border border-amber-200">
                      ★ Featured Resource
                    </span>
                  )}
                </div>
                <DialogPrimitive.Title className="text-2xl font-bold text-gray-900 leading-snug">
                  {resource.title}
                </DialogPrimitive.Title>
              </div>

              <DialogPrimitive.Close asChild>
                <button
                  className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200/60 rounded-full transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                  aria-label="Close details"
                >
                  <X className="w-6 h-6" aria-hidden="true" />
                </button>
              </DialogPrimitive.Close>
            </div>

            {/* Modal Body */}
            <div className="p-6 overflow-y-auto space-y-6 flex-1">
              {/* Preview Container */}
              <div>
                <h3 className="text-sm font-semibold text-gray-700 uppercase tracking-wider mb-3 flex items-center gap-2">
                  <Eye className="w-4 h-4 text-blue-600" aria-hidden="true" />
                  Document Preview
                </h3>
                <Suspense fallback={<div className="h-48 bg-gray-50 animate-pulse rounded-xl flex items-center justify-center text-sm text-gray-400">Loading Preview...</div>}>
                  <ResourcePreview resource={resource} onDownload={() => onDownload(resource)} />
                </Suspense>
              </div>

              {/* Description */}
              {resource.description && (
                <div>
                  <h3 className="text-sm font-semibold text-gray-700 uppercase tracking-wider mb-2">Description</h3>
                  <p className="text-gray-600 leading-relaxed text-sm bg-gray-50 p-4 rounded-xl border border-gray-200/80">
                    {resource.description}
                  </p>
                </div>
              )}

              {/* Metadata Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 bg-blue-50/40 p-4 rounded-xl border border-blue-100 text-sm">
                <div>
                  <span className="text-xs text-gray-500 flex items-center gap-1 mb-1">
                    <User className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    Uploader
                  </span>
                  <p className="font-semibold text-gray-900 truncate">
                    {resource.uploaderName || resource.uploaderId}
                  </p>
                </div>

                <div>
                  <span className="text-xs text-gray-500 flex items-center gap-1 mb-1">
                    <Calendar className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    Upload Date
                  </span>
                  <p className="font-semibold text-gray-900">{formatDate(resource.createdAt)}</p>
                </div>

                <div>
                  <span className="text-xs text-gray-500 flex items-center gap-1 mb-1">
                    <FileText className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    File Size
                  </span>
                  <p className="font-semibold text-gray-900">{formatFileSize(resource.fileSize)}</p>
                </div>

                <div>
                  <span className="text-xs text-gray-500 flex items-center gap-1 mb-1">
                    <Download className="w-3.5 h-3.5 text-blue-600" aria-hidden="true" />
                    Downloads
                  </span>
                  <p className="font-semibold text-gray-900">{resource.downloadCount || 0} times</p>
                </div>
              </div>

              {/* Tags */}
              {resource.tags && resource.tags.length > 0 && (
                <div>
                  <h3 className="text-sm font-semibold text-gray-700 uppercase tracking-wider mb-2 flex items-center gap-1.5">
                    <Tag className="w-4 h-4 text-blue-600" aria-hidden="true" />
                    Tags
                  </h3>
                  <div className="flex flex-wrap gap-2">
                    {resource.tags.map((tag, idx) => (
                      <span
                        key={idx}
                        className="text-xs bg-gray-100 text-gray-700 border border-gray-200 px-3 py-1 rounded-lg font-medium"
                      >
                        #{tag}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div className="p-4 border-t border-gray-200 bg-gray-50 flex items-center justify-between gap-4">
              <button
                onClick={() => onToggleBookmark(resource)}
                className={`inline-flex items-center gap-2 px-4 py-2.5 rounded-xl border text-sm font-medium transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-amber-500 focus-visible:ring-offset-1 ${
                  resource.isBookmarked
                    ? 'bg-amber-50 text-amber-700 border-amber-200 hover:bg-amber-100'
                    : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-100'
                }`}
              >
                {resource.isBookmarked ? (
                  <>
                    <BookmarkCheck className="w-4 h-4 text-amber-600" aria-hidden="true" />
                    Bookmarked
                  </>
                ) : (
                  <>
                    <Bookmark className="w-4 h-4 text-gray-500" aria-hidden="true" />
                    Bookmark Resource
                  </>
                )}
              </button>

              <div className="flex items-center gap-3">
                <DialogPrimitive.Close asChild>
                  <button
                    className="px-4 py-2.5 bg-white text-gray-700 border border-gray-300 font-medium text-sm rounded-xl hover:bg-gray-100 transition-colors focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-gray-600 focus-visible:ring-offset-1"
                  >
                    Close
                  </button>
                </DialogPrimitive.Close>

                <button
                  onClick={() => onDownload(resource)}
                  className="inline-flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
                >
                  <Download className="w-5 h-5" aria-hidden="true" />
                  Download File
                </button>
              </div>
            </div>
          </div>
        </DialogPrimitive.Content>
      </DialogPrimitive.Portal>
    </DialogPrimitive.Root>
  );
}

import { FileText } from 'lucide-react';

export function NoticeSkeleton({ count = 3 }: { count?: number }) {
  return (
    <div className="space-y-4">
      {Array.from({ length: count }).map((_, index) => (
        <div
          key={index}
          className="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm animate-pulse space-y-4"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="h-6 w-24 bg-gray-200 rounded-full" />
              <div className="h-6 w-20 bg-gray-200 rounded-full" />
            </div>
            <div className="h-6 w-16 bg-gray-200 rounded-full" />
          </div>

          <div className="space-y-2">
            <div className="h-6 bg-gray-200 rounded-md w-3/4" />
            <div className="h-4 bg-gray-100 rounded-md w-full" />
            <div className="h-4 bg-gray-100 rounded-md w-5/6" />
          </div>

          <div className="pt-2 flex items-center justify-between border-t border-gray-50">
            <div className="h-4 bg-gray-200 rounded w-48" />
            <div className="h-8 bg-gray-200 rounded-lg w-28" />
          </div>
        </div>
      ))}
    </div>
  );
}

interface NoticeEmptyStateProps {
  title?: string;
  message?: string;
  onClearFilters?: () => void;
  onCreateNew?: () => void;
}

export function NoticeEmptyState({
  title = 'No notices found',
  message = 'We could not find any notices matching your current search or filter criteria.',
  onClearFilters,
  onCreateNew,
}: NoticeEmptyStateProps) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 p-12 text-center shadow-sm my-4">
      <div className="w-16 h-16 bg-blue-50 rounded-2xl flex items-center justify-center mx-auto mb-4 text-[#2563EB]">
        <FileText className="w-8 h-8" />
      </div>
      <h3 className="text-xl font-bold text-gray-900 mb-2">{title}</h3>
      <p className="text-gray-500 max-w-md mx-auto text-sm mb-6 leading-relaxed">{message}</p>

      <div className="flex items-center justify-center gap-3">
        {onClearFilters && (
          <button
            onClick={onClearFilters}
            className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 font-medium text-sm rounded-xl transition-all"
          >
            Clear Filters
          </button>
        )}
        {onCreateNew && (
          <button
            onClick={onCreateNew}
            className="px-5 py-2 bg-[#2563EB] hover:bg-blue-700 text-white font-medium text-sm rounded-xl transition-all shadow-sm"
          >
            Create Notice
          </button>
        )}
      </div>
    </div>
  );
}

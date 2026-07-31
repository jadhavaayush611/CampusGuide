export function CouncilCardSkeleton() {
  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 animate-pulse">
      <div className="flex items-start gap-4">
        <div className="w-16 h-16 bg-gray-200 rounded-xl flex-shrink-0"></div>
        <div className="flex-1 space-y-3">
          <div className="h-5 bg-gray-200 rounded w-2/3"></div>
          <div className="h-4 bg-gray-200 rounded w-full"></div>
          <div className="h-4 bg-gray-200 rounded w-4/5"></div>
          <div className="flex items-center gap-3 pt-2">
            <div className="h-4 bg-gray-200 rounded w-24"></div>
            <div className="h-4 bg-gray-200 rounded w-16"></div>
          </div>
        </div>
        <div className="h-9 w-20 bg-gray-200 rounded-lg"></div>
      </div>
    </div>
  );
}

export function CouncilHeaderSkeleton() {
  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden mb-8 animate-pulse">
      <div className="h-40 bg-gray-300"></div>
      <div className="px-8 pb-6 -mt-12 flex items-end justify-between">
        <div className="flex items-end gap-6">
          <div className="w-24 h-24 bg-gray-200 rounded-xl border-4 border-white shadow-lg"></div>
          <div className="space-y-2 mb-2">
            <div className="h-7 bg-gray-200 rounded w-64"></div>
            <div className="h-4 bg-gray-200 rounded w-96"></div>
            <div className="h-4 bg-gray-200 rounded w-48"></div>
          </div>
        </div>
        <div className="h-10 w-28 bg-gray-200 rounded-lg mb-2"></div>
      </div>
    </div>
  );
}

export function CouncilSectionSkeleton() {
  return (
    <div className="space-y-4 animate-pulse">
      <div className="h-24 bg-white rounded-xl border border-gray-200 p-6 space-y-3">
        <div className="h-5 bg-gray-200 rounded w-1/3"></div>
        <div className="h-4 bg-gray-200 rounded w-3/4"></div>
      </div>
      <div className="h-24 bg-white rounded-xl border border-gray-200 p-6 space-y-3">
        <div className="h-5 bg-gray-200 rounded w-1/2"></div>
        <div className="h-4 bg-gray-200 rounded w-2/3"></div>
      </div>
    </div>
  );
}

import { useState } from 'react';
import { Pin, AlertCircle, FileText, Download, Calendar, UserCheck } from 'lucide-react';
import { CouncilNotice } from '../../../models/council.model';
import { toast } from '../../../core/toast/useToast';

interface CouncilNoticesProps {
  notices: CouncilNotice[];
}

export function CouncilNotices({ notices }: CouncilNoticesProps) {
  const [filter, setFilter] = useState<'all' | 'pinned' | 'important'>('all');

  const filteredNotices = notices.filter((n) => {
    if (filter === 'pinned') return n.isPinned;
    if (filter === 'important') return n.isImportant;
    return true;
  });

  const handleDownloadAttachment = (name: string) => {
    toast.success(`Downloading attachment: ${name}`);
  };

  if (!notices || notices.length === 0) {
    return (
      <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-lg mx-auto">
        <div className="w-16 h-16 bg-purple-50 text-purple-600 rounded-full flex items-center justify-center mx-auto mb-4 text-2xl">
          📢
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-1">No notices published</h3>
        <p className="text-sm text-gray-600">This council has not posted any official notices or announcements yet.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Filter Tabs */}
      <div className="flex items-center gap-3 border-b border-gray-200 pb-3">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
            filter === 'all' ? 'bg-[#2563EB] text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          All Notices ({notices.length})
        </button>

        <button
          onClick={() => setFilter('pinned')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
            filter === 'pinned' ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <Pin className="w-4 h-4 text-purple-400" />
          Pinned Updates ({notices.filter((n) => n.isPinned).length})
        </button>

        <button
          onClick={() => setFilter('important')}
          className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-1.5 ${
            filter === 'important' ? 'bg-amber-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          }`}
        >
          <AlertCircle className="w-4 h-4 text-amber-300" />
          Important Only ({notices.filter((n) => n.isImportant).length})
        </button>
      </div>

      {/* Notices Feed */}
      <div className="space-y-4">
        {filteredNotices.map((notice) => {
          const dateStr = new Date(notice.createdAt).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric',
          });

          return (
            <div
              key={notice.id}
              className={`bg-white rounded-xl border shadow-sm p-6 transition-all ${
                notice.isPinned
                  ? 'border-l-4 border-l-purple-600 bg-purple-50/20'
                  : notice.isImportant
                  ? 'border-l-4 border-l-amber-500 bg-amber-50/20'
                  : 'border-gray-200'
              }`}
            >
              <div className="flex items-start justify-between gap-4 mb-3">
                <div className="flex items-center gap-2 flex-wrap">
                  {notice.isPinned && (
                    <span className="bg-purple-100 text-purple-800 text-xs px-2.5 py-0.5 rounded-full font-bold flex items-center gap-1">
                      <Pin className="w-3 h-3" />
                      Pinned Update
                    </span>
                  )}
                  {notice.isImportant && (
                    <span className="bg-amber-100 text-amber-800 text-xs px-2.5 py-0.5 rounded-full font-bold flex items-center gap-1">
                      <AlertCircle className="w-3 h-3" />
                      High Priority
                    </span>
                  )}
                  <span className="bg-gray-100 text-gray-700 text-xs px-2.5 py-0.5 rounded-full font-medium">
                    {notice.category}
                  </span>
                </div>
                <div className="flex items-center gap-1.5 text-xs text-gray-500">
                  <Calendar className="w-3.5 h-3.5" />
                  {dateStr}
                </div>
              </div>

              <h4 className="text-lg font-bold text-gray-900 mb-2 leading-snug">{notice.title}</h4>
              <p className="text-gray-700 text-sm leading-relaxed mb-4">{notice.content}</p>

              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-3 border-t border-gray-100 text-xs text-gray-500">
                <div className="flex items-center gap-2">
                  <UserCheck className="w-3.5 h-3.5 text-[#2563EB]" />
                  <span>
                    Posted by <strong className="text-gray-800">{notice.postedBy}</strong> ({notice.postedByRole})
                  </span>
                </div>

                {notice.attachments && notice.attachments.length > 0 && (
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-gray-600">Attachments:</span>
                    {notice.attachments.map((att) => (
                      <button
                        key={att.id}
                        onClick={() => handleDownloadAttachment(att.name)}
                        className="px-2.5 py-1 bg-gray-100 hover:bg-gray-200 text-gray-800 rounded font-medium flex items-center gap-1 transition-colors"
                      >
                        <FileText className="w-3 h-3 text-[#2563EB]" />
                        {att.name} ({att.fileSize})
                        <Download className="w-3 h-3 text-gray-400" />
                      </button>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

import { useState } from 'react';
import { FileText, Download, ExternalLink, Eye, X, Image as ImageIcon, File } from 'lucide-react';
import { NoticeAttachment } from '../../../models/notice.model';
import { noticeSdk } from '../../../sdk/notices/NoticeSdk';

interface NoticeAttachmentViewerProps {
  attachments: NoticeAttachment[];
}

export function NoticeAttachmentViewer({ attachments }: NoticeAttachmentViewerProps) {
  const [activePreview, setActivePreview] = useState<NoticeAttachment | null>(null);

  if (!attachments || attachments.length === 0) {
    return null;
  }

  const getFileIcon = (fileType: string) => {
    if (fileType.includes('image') || fileType.includes('png') || fileType.includes('jpg')) {
      return <ImageIcon className="w-4 h-4 text-emerald-600" />;
    }
    if (fileType.includes('pdf')) {
      return <FileText className="w-4 h-4 text-red-600" />;
    }
    return <File className="w-4 h-4 text-blue-600" />;
  };

  const handleDownload = (att: NoticeAttachment) => {
    noticeSdk.downloadAttachment(att.id, att.url);
  };

  return (
    <div className="space-y-3">
      <h4 className="text-xs font-bold text-gray-500 uppercase tracking-wider">
        Attachments & Resources ({attachments.length})
      </h4>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        {attachments.map((att) => (
          <div
            key={att.id}
            className="flex items-center justify-between p-3 bg-gray-50 hover:bg-blue-50/50 border border-gray-200 hover:border-blue-200 rounded-xl transition-all duration-200 group"
          >
            <div className="flex items-center gap-3 min-w-0 pr-2">
              <div className="p-2 bg-white rounded-lg border border-gray-100 shadow-2xs group-hover:scale-105 transition-transform">
                {getFileIcon(att.fileType)}
              </div>
              <div className="min-w-0">
                <p className="text-sm font-semibold text-gray-800 truncate group-hover:text-blue-900">
                  {att.name}
                </p>
                <p className="text-xs text-gray-500">{att.fileSize}</p>
              </div>
            </div>

            <div className="flex items-center gap-1.5 shrink-0">
              <button
                onClick={() => setActivePreview(att)}
                title="Preview attachment"
                className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-white rounded-lg transition-colors"
              >
                <Eye className="w-4 h-4" />
              </button>
              {att.externalUrl && (
                <a
                  href={att.externalUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  title="Open external link"
                  className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-white rounded-lg transition-colors"
                >
                  <ExternalLink className="w-4 h-4" />
                </a>
              )}
              <button
                onClick={() => handleDownload(att)}
                title="Download file"
                className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-white rounded-lg transition-colors"
              >
                <Download className="w-4 h-4" />
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Attachment Preview Modal */}
      {activePreview && (
        <div className="fixed inset-0 z-50 bg-gray-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-3xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200">
            {/* Modal Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 bg-gray-50/50">
              <div className="flex items-center gap-3 min-w-0">
                <div className="p-2 bg-white rounded-lg border border-gray-200">
                  {getFileIcon(activePreview.fileType)}
                </div>
                <div className="min-w-0">
                  <h3 className="text-base font-bold text-gray-900 truncate">
                    {activePreview.name}
                  </h3>
                  <p className="text-xs text-gray-500">{activePreview.fileSize}</p>
                </div>
              </div>
              <button
                onClick={() => setActivePreview(null)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200/60 rounded-xl transition-all"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 overflow-y-auto flex-1 min-h-[300px] flex flex-col items-center justify-center bg-gray-50/30">
              {activePreview.fileType.includes('image') ? (
                <img
                  src={activePreview.url}
                  alt={activePreview.name}
                  loading="lazy"
                  decoding="async"
                  className="max-h-[60vh] rounded-lg object-contain border border-gray-200 shadow-sm"
                />
              ) : activePreview.fileType.includes('pdf') ? (
                <iframe
                  src={activePreview.url}
                  title={activePreview.name}
                  className="w-full h-[50vh] rounded-xl border border-gray-200"
                />
              ) : (
                /* Fallback when inline preview is unsupported */
                <div className="text-center py-8">
                  <div className="w-16 h-16 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center mx-auto mb-4">
                    <FileText className="w-8 h-8" />
                  </div>
                  <h4 className="text-base font-bold text-gray-900 mb-1">Preview Unavailable</h4>
                  <p className="text-sm text-gray-500 max-w-sm mx-auto mb-6">
                    Direct inline preview for <strong>{activePreview.fileType}</strong> is not supported by your browser. You can download the file to inspect.
                  </p>
                  <button
                    onClick={() => handleDownload(activePreview)}
                    className="inline-flex items-center gap-2 px-5 py-2.5 bg-[#2563EB] hover:bg-blue-700 text-white font-medium text-sm rounded-xl transition-all shadow-sm"
                  >
                    <Download className="w-4 h-4" />
                    Download File
                  </button>
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div className="px-6 py-3 border-t border-gray-100 bg-white flex items-center justify-between text-xs text-gray-500">
              <span>Attachment ID: {activePreview.id}</span>
              <button
                onClick={() => handleDownload(activePreview)}
                className="text-[#2563EB] font-semibold hover:underline inline-flex items-center gap-1"
              >
                <Download className="w-3.5 h-3.5" />
                Download Attachment
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

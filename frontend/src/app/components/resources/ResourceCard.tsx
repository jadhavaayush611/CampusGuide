import React, { memo, useMemo } from 'react';
import { Resource } from '../../../models/resource.model';
import {
  FileText,
  Download,
  Bookmark,
  BookmarkCheck,
  Eye,
  Edit2,
  Trash2,
  FileArchive,
  Video,
  Image as ImageIcon,
  User,
  Calendar,
  Tag,
} from 'lucide-react';

interface ResourceCardProps {
  resource: Resource;
  viewMode?: 'grid' | 'list';
  onViewDetails: (resource: Resource) => void;
  onDownload: (resource: Resource) => void;
  onToggleBookmark: (resource: Resource) => void;
  onEdit?: (resource: Resource) => void;
  onDelete?: (resource: Resource) => void;
  isOwnerOrAdmin?: boolean;
}

function getFileTypeBadge(fileType: string) {
  const t = fileType.toLowerCase();
  if (t.includes('pdf')) return { label: 'PDF', bg: 'bg-red-50 text-red-600 border-red-200', icon: FileText };
  if (t.includes('doc') || t.includes('word')) return { label: 'DOCX', bg: 'bg-blue-50 text-blue-600 border-blue-200', icon: FileText };
  if (t.includes('zip') || t.includes('rar') || t.includes('7z')) return { label: 'ZIP', bg: 'bg-amber-50 text-amber-600 border-amber-200', icon: FileArchive };
  if (t.includes('png') || t.includes('jpg') || t.includes('jpeg') || t.includes('img')) return { label: 'IMG', bg: 'bg-emerald-50 text-emerald-600 border-emerald-200', icon: ImageIcon };
  if (t.includes('mp4') || t.includes('video') || t.includes('avi')) return { label: 'VIDEO', bg: 'bg-purple-50 text-purple-600 border-purple-200', icon: Video };
  if (t.includes('ppt') || t.includes('slides')) return { label: 'PPT', bg: 'bg-orange-50 text-orange-600 border-orange-200', icon: FileText };
  return { label: fileType.toUpperCase(), bg: 'bg-gray-100 text-gray-600 border-gray-200', icon: FileText };
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '0 B';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '';
  try {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  } catch {
    return dateStr;
  }
}

export const ResourceCard = memo(function ResourceCard({
  resource,
  viewMode = 'grid',
  onViewDetails,
  onDownload,
  onToggleBookmark,
  onEdit,
  onDelete,
  isOwnerOrAdmin = true,
}: ResourceCardProps) {
  const badge = useMemo(() => getFileTypeBadge(resource.fileType), [resource.fileType]);
  const BadgeIcon = badge.icon;

  if (viewMode === 'list') {
    return (
      <div className="bg-white rounded-xl border border-gray-200 hover:border-blue-300 hover:shadow-md transition-all p-4 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-4 flex-1 min-w-0">
          <div className={`w-11 h-11 rounded-lg flex items-center justify-center border flex-shrink-0 ${badge.bg}`}>
            <BadgeIcon className="w-5 h-5" />
          </div>
          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2 flex-wrap mb-1">
              <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-100">
                {resource.category}
              </span>
              {resource.isFeatured && (
                <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
                  ★ Featured
                </span>
              )}
            </div>
            <h3
              onClick={() => onViewDetails(resource)}
              className="font-semibold text-gray-900 truncate hover:text-blue-600 transition-colors cursor-pointer"
            >
              {resource.title}
            </h3>
            <p className="text-xs text-gray-500 flex items-center gap-3 mt-1 flex-wrap">
              <span className="flex items-center gap-1">
                <User className="w-3.5 h-3.5 text-gray-400" />
                {resource.uploaderName || resource.uploaderId}
              </span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-gray-400" />
                {formatDate(resource.createdAt)}
              </span>
              <span>•</span>
              <span>{formatFileSize(resource.fileSize)}</span>
              <span>•</span>
              <span>{resource.downloadCount || 0} downloads</span>
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 self-end md:self-center flex-shrink-0">
          <button
            onClick={() => onToggleBookmark(resource)}
            className={`p-2 rounded-lg border transition-colors ${
              resource.isBookmarked
                ? 'bg-amber-50 text-amber-600 border-amber-200 hover:bg-amber-100'
                : 'text-gray-400 border-gray-200 hover:bg-gray-50 hover:text-gray-600'
            }`}
            title={resource.isBookmarked ? 'Remove Bookmark' : 'Bookmark Resource'}
          >
            {resource.isBookmarked ? <BookmarkCheck className="w-4 h-4" /> : <Bookmark className="w-4 h-4" />}
          </button>

          <button
            onClick={() => onViewDetails(resource)}
            className="p-2 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            title="Preview Details"
          >
            <Eye className="w-4 h-4" />
          </button>

          {isOwnerOrAdmin && onEdit && (
            <button
              onClick={() => onEdit(resource)}
              className="p-2 text-gray-600 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              title="Edit Resource"
            >
              <Edit2 className="w-4 h-4" />
            </button>
          )}

          {isOwnerOrAdmin && onDelete && (
            <button
              onClick={() => onDelete(resource)}
              className="p-2 text-red-600 border border-red-100 rounded-lg hover:bg-red-50 transition-colors"
              title="Delete Resource"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={() => onDownload(resource)}
            className="flex items-center gap-1.5 px-3 py-2 bg-blue-600 text-white text-xs font-semibold rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
          >
            <Download className="w-4 h-4" />
            Download
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 hover:border-blue-300 hover:shadow-md transition-all p-5 flex flex-col justify-between h-full group">
      <div>
        {/* Top bar with category & file badge */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-blue-50 text-blue-700 border border-blue-100 truncate max-w-[170px]">
            {resource.category}
          </span>

          <div className="flex items-center gap-2">
            <span className={`text-xs font-bold px-2 py-0.5 rounded border ${badge.bg}`}>
              {badge.label}
            </span>
            <button
              onClick={() => onToggleBookmark(resource)}
              className={`p-1.5 rounded-lg transition-colors ${
                resource.isBookmarked
                  ? 'text-amber-500 bg-amber-50 hover:bg-amber-100'
                  : 'text-gray-400 hover:bg-gray-100 hover:text-gray-600'
              }`}
              title={resource.isBookmarked ? 'Remove Bookmark' : 'Bookmark Resource'}
            >
              {resource.isBookmarked ? <BookmarkCheck className="w-4 h-4" /> : <Bookmark className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Title & Description */}
        <h3
          onClick={() => onViewDetails(resource)}
          className="font-semibold text-gray-900 text-base mb-2 group-hover:text-blue-600 transition-colors line-clamp-2 cursor-pointer"
        >
          {resource.title}
        </h3>
        {resource.description && (
          <p className="text-xs text-gray-600 line-clamp-2 mb-4">
            {resource.description}
          </p>
        )}

        {/* Tags */}
        {resource.tags && resource.tags.length > 0 && (
          <div className="flex flex-wrap gap-1.5 mb-4">
            {resource.tags.slice(0, 3).map((tag, idx) => (
              <span key={idx} className="text-[11px] bg-gray-100 text-gray-600 px-2 py-0.5 rounded flex items-center gap-1">
                <Tag className="w-3 h-3 text-gray-400" />
                {tag}
              </span>
            ))}
            {resource.tags.length > 3 && (
              <span className="text-[11px] bg-gray-50 text-gray-400 px-1.5 py-0.5 rounded">
                +{resource.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Footer Info & Actions */}
      <div className="pt-4 border-t border-gray-100 flex items-center justify-between text-xs text-gray-500">
        <div>
          <p className="font-medium text-gray-700 truncate max-w-[140px]">
            {resource.uploaderName || resource.uploaderId}
          </p>
          <p className="text-[11px] text-gray-400 mt-0.5">
            {formatFileSize(resource.fileSize)} • {resource.downloadCount || 0} dl
          </p>
        </div>

        <div className="flex items-center gap-1">
          <button
            onClick={() => onViewDetails(resource)}
            className="p-2 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
            title="View Details & Preview"
          >
            <Eye className="w-4 h-4" />
          </button>

          {isOwnerOrAdmin && onEdit && (
            <button
              onClick={() => onEdit(resource)}
              className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
              title="Edit"
            >
              <Edit2 className="w-4 h-4" />
            </button>
          )}

          {isOwnerOrAdmin && onDelete && (
            <button
              onClick={() => onDelete(resource)}
              className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
              title="Delete"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}

          <button
            onClick={() => onDownload(resource)}
            className="p-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors shadow-sm ml-1"
            title="Download Resource"
          >
            <Download className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
});

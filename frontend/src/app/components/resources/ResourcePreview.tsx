import React from 'react';
import { Resource } from '../../../models/resource.model';
import { FileText, Download, ExternalLink, AlertCircle, FileArchive, Video, Image as ImageIcon, Music } from 'lucide-react';

interface ResourcePreviewProps {
  resource: Resource;
  onDownload?: () => void;
}

export function ResourcePreview({ resource, onDownload }: ResourcePreviewProps) {
  const type = (resource.fileType || '').toLowerCase();
  const url = resource.previewUrl || resource.downloadUrl;

  const isImage = type.includes('png') || type.includes('jpg') || type.includes('jpeg') || type.includes('svg') || type.includes('webp');
  const isPdf = type.includes('pdf');
  const isVideo = type.includes('mp4') || type.includes('webm') || type.includes('mov');
  const isAudio = type.includes('mp3') || type.includes('wav') || type.includes('ogg');
  const isLink = Boolean(resource.externalUrl);

  if (isLink && resource.externalUrl) {
    return (
      <div className="bg-blue-50/60 border border-blue-200 rounded-xl p-6 text-center">
        <div className="w-12 h-12 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-3">
          <ExternalLink className="w-6 h-6" />
        </div>
        <h4 className="font-medium text-gray-900 mb-1">External Resource Link</h4>
        <p className="text-sm text-gray-600 mb-4">This resource points to an external link or web tool.</p>
        <a
          href={resource.externalUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white font-medium text-sm rounded-lg hover:bg-blue-700 transition-colors"
        >
          Open Link
          <ExternalLink className="w-4 h-4" />
        </a>
      </div>
    );
  }

  if (isImage && url) {
    return (
      <div className="bg-gray-900 rounded-xl overflow-hidden flex items-center justify-center p-4 min-h-[300px] max-h-[500px]">
        <img
          src={url}
          alt={resource.title}
          loading="lazy"
          decoding="async"
          className="max-h-[460px] object-contain rounded shadow-lg"
          onError={(e) => {
            (e.target as HTMLElement).style.display = 'none';
          }}
        />
      </div>
    );
  }

  if (isPdf && url) {
    return (
      <div className="border border-gray-200 rounded-xl overflow-hidden bg-gray-50 flex flex-col h-[480px]">
        <div className="bg-gray-100 px-4 py-2 text-xs font-medium text-gray-600 border-b border-gray-200 flex items-center justify-between">
          <span className="flex items-center gap-2">
            <FileText className="w-4 h-4 text-red-500" />
            PDF Viewer: {resource.originalFileName || resource.fileName}
          </span>
          <span className="text-gray-400">PDF Document</span>
        </div>
        <iframe
          src={url}
          title={resource.title}
          className="w-full flex-1 border-0"
        />
      </div>
    );
  }

  if (isVideo && url) {
    return (
      <div className="bg-black rounded-xl overflow-hidden flex items-center justify-center min-h-[280px]">
        <video controls className="w-full max-h-[450px]">
          <source src={url} type={`video/${type}`} />
          Your browser does not support video playback.
        </video>
      </div>
    );
  }

  if (isAudio && url) {
    return (
      <div className="bg-gray-50 border border-gray-200 rounded-xl p-6 text-center">
        <div className="w-12 h-12 bg-purple-100 text-purple-600 rounded-full flex items-center justify-center mx-auto mb-4">
          <Music className="w-6 h-6" />
        </div>
        <h4 className="font-medium text-gray-900 mb-3">{resource.title}</h4>
        <audio controls className="w-full max-w-md mx-auto">
          <source src={url} type={`audio/${type}`} />
          Your browser does not support audio playback.
        </audio>
      </div>
    );
  }

  // Graceful Fallback when preview is unavailable
  return (
    <div className="bg-gray-50 border border-gray-200 border-dashed rounded-xl p-8 text-center flex flex-col items-center justify-center min-h-[220px]">
      <div className="w-12 h-12 bg-gray-200 text-gray-500 rounded-full flex items-center justify-center mb-3">
        {type.includes('zip') || type.includes('rar') ? (
          <FileArchive className="w-6 h-6 text-amber-600" />
        ) : (
          <AlertCircle className="w-6 h-6 text-gray-500" />
        )}
      </div>
      <h4 className="font-semibold text-gray-800 mb-1">Preview Unavailable</h4>
      <p className="text-sm text-gray-500 mb-4 max-w-md">
        In-browser preview is unavailable for <span className="font-semibold text-gray-700">.{type || 'binary'}</span> files. Download the file to view its full contents.
      </p>
      {onDownload && (
        <button
          onClick={onDownload}
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm"
        >
          <Download className="w-4 h-4" />
          Download File ({(resource.fileSize / 1024 / 1024).toFixed(1)} MB)
        </button>
      )}
    </div>
  );
}

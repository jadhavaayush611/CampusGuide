import React, { useState, useRef, useCallback } from 'react';
import {
  Paperclip,
  UploadCloud,
  FileText,
  FileImage,
  FileArchive,
  FileSpreadsheet,
  Download,
  Trash2,
  AlertCircle,
  Loader2,
  CheckCircle2,
  ExternalLink,
} from 'lucide-react';
import { AttachmentItem, attachmentSdk } from '../../../sdk/attachments/AttachmentSdk';

interface AttachmentManagerProps {
  ownerType: 'PLANNER_TASK' | 'NOTICE';
  ownerId?: string;
  attachments?: Array<{
    id?: string;
    name: string;
    url?: string;
    size?: string;
    type?: string;
  }>;
  canUpload?: boolean;
  canDelete?: boolean;
  onAttachmentUploaded?: (attachment: AttachmentItem) => void;
  onAttachmentDeleted?: (attachmentId: string) => void;
  onPendingFileAdded?: (file: File) => void;
  onPendingFileRemoved?: (index: number) => void;
  pendingFiles?: File[];
}

const MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

const ALLOWED_EXTENSIONS = new Set([
  '.pdf',
  '.jpg',
  '.jpeg',
  '.png',
  '.webp',
  '.gif',
  '.doc',
  '.docx',
  '.xls',
  '.xlsx',
  '.ppt',
  '.pptx',
  '.txt',
  '.csv',
  '.md',
  '.zip',
]);

export const AttachmentManager: React.FC<AttachmentManagerProps> = ({
  ownerType,
  ownerId,
  attachments = [],
  canUpload = true,
  canDelete = true,
  onAttachmentUploaded,
  onAttachmentDeleted,
  onPendingFileAdded,
  onPendingFileRemoved,
  pendingFiles = [],
}) => {
  const [isUploading, setIsUploading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const getFileIcon = (fileName: string, fileType?: string) => {
    const ext = fileName.slice(fileName.lastIndexOf('.')).toLowerCase();
    if (['.jpg', '.jpeg', '.png', '.webp', '.gif'].includes(ext) || fileType?.includes('image')) {
      return <FileImage className="w-4 h-4 text-emerald-500 shrink-0" />;
    }
    if (['.zip', '.tar', '.gz'].includes(ext) || fileType?.includes('zip')) {
      return <FileArchive className="w-4 h-4 text-amber-500 shrink-0" />;
    }
    if (['.xls', '.xlsx', '.csv'].includes(ext) || fileType?.includes('sheet') || fileType?.includes('excel')) {
      return <FileSpreadsheet className="w-4 h-4 text-teal-500 shrink-0" />;
    }
    return <FileText className="w-4 h-4 text-blue-500 shrink-0" />;
  };

  const validateFile = (file: File): string | null => {
    if (!file || file.size === 0) {
      return 'Selected file is empty. Please choose a valid file.';
    }
    if (file.size > MAX_FILE_SIZE_BYTES) {
      const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
      return `File size (${sizeMb} MB) exceeds maximum allowed limit of 20 MB.`;
    }
    const extIndex = file.name.lastIndexOf('.');
    if (extIndex === -1) {
      return 'File must have a valid extension (e.g. .pdf, .png, .docx).';
    }
    const ext = file.name.slice(extIndex).toLowerCase();
    if (!ALLOWED_EXTENSIONS.has(ext)) {
      return `File type '${ext}' is not supported. Please upload PDF, images, documents, or ZIP files.`;
    }
    return null;
  };

  const handleFileSelected = async (e: React.ChangeEvent<HTMLInputElement>) => {
    setErrorMessage(null);
    setSuccessMessage(null);
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const file = files[0];
    const validationError = validateFile(file);
    if (validationError) {
      setErrorMessage(validationError);
      if (fileInputRef.current) fileInputRef.current.value = '';
      return;
    }

    // If ownerId exists, upload immediately to backend
    if (ownerId) {
      try {
        setIsUploading(true);
        const uploaded = await attachmentSdk.uploadAttachment(file, ownerType, ownerId);
        setSuccessMessage(`"${file.name}" uploaded successfully.`);
        if (onAttachmentUploaded) {
          onAttachmentUploaded(uploaded);
        }
      } catch (err: any) {
        setErrorMessage(err.message || 'Failed to upload attachment. Please try again.');
      } finally {
        setIsUploading(false);
        if (fileInputRef.current) fileInputRef.current.value = '';
      }
    } else {
      // Pending file for uncreated owner
      if (onPendingFileAdded) {
        onPendingFileAdded(file);
        setSuccessMessage(`"${file.name}" attached. Will be saved with the record.`);
      }
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleDelete = async (attId: string, attName: string) => {
    setErrorMessage(null);
    setSuccessMessage(null);
    try {
      setDeletingId(attId);
      await attachmentSdk.deleteAttachment(attId);
      setSuccessMessage(`"${attName}" removed.`);
      setConfirmDeleteId(null);
      if (onAttachmentDeleted) {
        onAttachmentDeleted(attId);
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to delete attachment.');
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <div className="space-y-3">
      {/* Error Message */}
      {errorMessage && (
        <div className="flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-xl text-xs text-red-700 animate-in fade-in">
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <span className="flex-1">{errorMessage}</span>
          <button
            type="button"
            onClick={() => setErrorMessage(null)}
            className="text-red-500 hover:text-red-800 font-bold ml-1"
          >
            ×
          </button>
        </div>
      )}

      {/* Success Message */}
      {successMessage && (
        <div className="flex items-start gap-2 p-3 bg-emerald-50 border border-emerald-200 rounded-xl text-xs text-emerald-700 animate-in fade-in">
          <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
          <span className="flex-1">{successMessage}</span>
          <button
            type="button"
            onClick={() => setSuccessMessage(null)}
            className="text-emerald-500 hover:text-emerald-800 font-bold ml-1"
          >
            ×
          </button>
        </div>
      )}

      {/* Existing Attachments List */}
      {attachments.length > 0 && (
        <div className="space-y-2">
          {attachments.map((att, idx) => {
            const attId = att.id || `att-idx-${idx}`;
            const downloadUrl = att.id ? attachmentSdk.getDownloadUrl(att.id) : (att.url || '#');
            const viewUrl = att.id ? attachmentSdk.getViewUrl(att.id) : (att.url || '#');

            return (
              <div
                key={attId}
                className="flex items-center justify-between p-3 bg-gray-50 hover:bg-gray-100/80 border border-gray-200 rounded-xl text-xs transition-colors"
              >
                <div className="flex items-center gap-2.5 min-w-0 pr-2">
                  {getFileIcon(att.name, att.type)}
                  <div className="min-w-0">
                    <p className="font-semibold text-gray-800 truncate">{att.name}</p>
                    {att.size && <span className="text-[10px] text-gray-500">{att.size}</span>}
                  </div>
                </div>

                <div className="flex items-center gap-1.5 shrink-0">
                  {att.id && (
                    <a
                      href={viewUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-1.5 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                      title="View file inline"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                  )}

                  <a
                    href={downloadUrl}
                    download={att.name}
                    className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-blue-50 text-blue-700 hover:bg-blue-100 rounded-lg font-medium transition-colors"
                    title="Download file"
                  >
                    <Download className="w-3.5 h-3.5" />
                    <span>Download</span>
                  </a>

                  {canDelete && att.id && (
                    confirmDeleteId === att.id ? (
                      <div className="flex items-center gap-1 bg-red-50 p-1 rounded-lg border border-red-200 animate-in fade-in">
                        <span className="text-[10px] text-red-700 font-bold px-1">Delete?</span>
                        <button
                          type="button"
                          disabled={deletingId === att.id}
                          onClick={() => handleDelete(att.id!, att.name)}
                          className="px-2 py-0.5 bg-red-600 text-white rounded text-[10px] font-bold hover:bg-red-700"
                        >
                          {deletingId === att.id ? '...' : 'Yes'}
                        </button>
                        <button
                          type="button"
                          onClick={() => setConfirmDeleteId(null)}
                          className="px-1.5 py-0.5 text-gray-500 hover:text-gray-800 text-[10px]"
                        >
                          No
                        </button>
                      </div>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setConfirmDeleteId(att.id!)}
                        className="p-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors"
                        title="Delete attachment"
                        aria-label={`Delete attachment ${att.name}`}
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    )
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Pending Files (for record creation flow) */}
      {pendingFiles.length > 0 && (
        <div className="space-y-1.5">
          <span className="text-[11px] font-semibold text-gray-500 uppercase tracking-wider">
            Files to attach upon saving ({pendingFiles.length})
          </span>
          <div className="space-y-1.5">
            {pendingFiles.map((file, pIdx) => (
              <div
                key={pIdx}
                className="flex items-center justify-between p-2.5 bg-blue-50/50 border border-blue-100 rounded-xl text-xs"
              >
                <div className="flex items-center gap-2 min-w-0 pr-2">
                  <Paperclip className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                  <span className="font-semibold text-gray-800 truncate">{file.name}</span>
                  <span className="text-[10px] text-gray-500">
                    ({(file.size / 1024).toFixed(0)} KB)
                  </span>
                </div>
                {onPendingFileRemoved && (
                  <button
                    type="button"
                    onClick={() => onPendingFileRemoved(pIdx)}
                    className="text-red-500 hover:text-red-700 p-1"
                    title="Remove pending file"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Button & Area */}
      {canUpload && (
        <div>
          <input
            ref={fileInputRef}
            type="file"
            onChange={handleFileSelected}
            className="hidden"
            accept=".pdf,.jpg,.jpeg,.png,.webp,.gif,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.csv,.md,.zip"
          />

          <button
            type="button"
            disabled={isUploading}
            onClick={() => fileInputRef.current?.click()}
            className="w-full flex items-center justify-center gap-2 py-2.5 px-4 bg-gray-50 hover:bg-gray-100/90 border border-dashed border-gray-300 rounded-xl text-xs font-semibold text-gray-700 hover:text-gray-900 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isUploading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin text-blue-600" />
                <span>Uploading file...</span>
              </>
            ) : (
              <>
                <UploadCloud className="w-4 h-4 text-gray-500" />
                <span>Choose file to attach (PDF, PNG, DOCX, ZIP — Max 20MB)</span>
              </>
            )}
          </button>
        </div>
      )}
    </div>
  );
};

import React, { useState, useEffect } from 'react';
import { Resource, ResourceCategory, CreateResourcePayload, UpdateResourcePayload } from '../../../models/resource.model';
import { X, Upload, FileText, CheckCircle, AlertCircle, Loader2 } from 'lucide-react';

interface ResourceUploadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: CreateResourcePayload) => Promise<void>;
  onUpdate?: (id: string, payload: UpdateResourcePayload) => Promise<void>;
  editingResource?: Resource | null;
}

const CATEGORIES: ResourceCategory[] = [
  'Lecture Notes',
  'Lab Manuals',
  'Past Papers',
  'Syllabi',
  'Forms',
  'Templates',
  'Handbooks',
  'Policies',
  'Miscellaneous',
];

export function ResourceUploadModal({
  isOpen,
  onClose,
  onSubmit,
  onUpdate,
  editingResource,
}: ResourceUploadModalProps) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<ResourceCategory>('Lecture Notes');
  const [tagsInput, setTagsInput] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [externalUrl, setExternalUrl] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (editingResource) {
      setTitle(editingResource.title);
      setDescription(editingResource.description || '');
      setCategory((editingResource.category as ResourceCategory) || 'Lecture Notes');
      setTagsInput(editingResource.tags ? editingResource.tags.join(', ') : '');
      setExternalUrl(editingResource.externalUrl || '');
      setFile(null);
    } else {
      setTitle('');
      setDescription('');
      setCategory('Lecture Notes');
      setTagsInput('');
      setFile(null);
      setExternalUrl('');
    }
    setErrorMessage('');
  }, [editingResource, isOpen]);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim()) {
      setErrorMessage('Please enter a title for the resource.');
      return;
    }

    if (!editingResource && !file && !externalUrl.trim()) {
      setErrorMessage('Please select a file to upload or enter an external URL.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const tags = tagsInput
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean);

      if (editingResource && onUpdate) {
        await onUpdate(editingResource.id, {
          title: title.trim(),
          description: description.trim(),
          category,
          tags,
        });
      } else {
        await onSubmit({
          title: title.trim(),
          description: description.trim(),
          category,
          tags,
          file: file || undefined,
          externalUrl: externalUrl.trim() || undefined,
        });
      }
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to upload resource. Please check inputs.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200" role="dialog" aria-modal="true" aria-labelledby="resource-modal-title">
      <div className="bg-white rounded-2xl shadow-2xl border border-gray-200 w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden">
        {/* Header */}
        <div className="p-6 border-b border-gray-200 flex items-center justify-between bg-gray-50/50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center font-bold">
              <Upload className="w-5 h-5" />
            </div>
            <div>
              <h2 id="resource-modal-title" className="text-xl font-bold text-gray-900">
                {editingResource ? 'Edit Resource Details' : 'Upload Academic Resource'}
              </h2>
              <p className="text-xs text-gray-500">
                {editingResource
                  ? 'Update metadata and tags for this item'
                  : 'Share study guides, notes, lab manuals, and papers with campus'}
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-200/60 rounded-full transition-colors"
            aria-label="Close dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body Form */}
        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto space-y-5 flex-1">
          {errorMessage && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-center gap-3 text-red-700 text-sm" role="alert">
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-500" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Title */}
          <div>
            <label htmlFor="resource-title" className="block text-sm font-semibold text-gray-700 mb-1">
              Title <span className="text-red-500">*</span>
            </label>
            <input
              id="resource-title"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. CS201 Data Structures & Algorithms Notes"
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 text-sm"
              required
            />
          </div>

          {/* Category */}
          <div>
            <label htmlFor="resource-category" className="block text-sm font-semibold text-gray-700 mb-1">
              Category <span className="text-red-500">*</span>
            </label>
            <select
              id="resource-category"
              value={category}
              onChange={(e) => setCategory(e.target.value as ResourceCategory)}
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 text-sm bg-white"
            >
              {CATEGORIES.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>

          {/* Description */}
          <div>
            <label htmlFor="resource-description" className="block text-sm font-semibold text-gray-700 mb-1">Description</label>
            <textarea
              id="resource-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Provide a brief summary of what this document covers..."
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 text-sm resize-none"
            />
          </div>

          {/* Tags */}
          <div>
            <label htmlFor="resource-tags" className="block text-sm font-semibold text-gray-700 mb-1">Tags (Comma-separated)</label>
            <input
              id="resource-tags"
              type="text"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              placeholder="e.g. Data Structures, C++, Exams, Spring 2026"
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 text-sm"
            />
          </div>

          {/* File Picker / Upload zone (only if creating or replacing) */}
          {!editingResource && (
            <div>
              <span className="block text-sm font-semibold text-gray-700 mb-1">
                Upload File <span className="text-red-500">*</span>
              </span>
              <div className="border-2 border-dashed border-gray-300 hover:border-blue-500 rounded-2xl p-6 text-center transition-colors bg-gray-50/50 focus-within:ring-2 focus-within:ring-blue-600 focus-within:ring-offset-2">
                <input
                  type="file"
                  id="resource-file-input"
                  className="sr-only"
                  onChange={(e) => {
                    if (e.target.files && e.target.files[0]) {
                      setFile(e.target.files[0]);
                    }
                  }}
                />
                <label htmlFor="resource-file-input" className="cursor-pointer flex flex-col items-center outline-none">
                  <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-full flex items-center justify-center mb-3">
                    <FileText className="w-6 h-6" />
                  </div>
                  {file ? (
                    <div className="flex items-center gap-2 text-sm font-semibold text-emerald-700 bg-emerald-50 px-3 py-1.5 rounded-lg border border-emerald-200">
                      <CheckCircle className="w-4 h-4 text-emerald-600" />
                      {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)
                    </div>
                  ) : (
                    <>
                      <p className="text-sm font-semibold text-gray-800">
                        Click to upload file <span className="text-gray-400 font-normal">or drag & drop</span>
                      </p>
                      <p className="text-xs text-gray-500 mt-1">PDF, DOCX, ZIP, PNG, PPTX up to 50MB</p>
                    </>
                  )}
                </label>
              </div>
            </div>
          )}

          {/* External URL Optional */}
          <div>
            <label htmlFor="resource-url" className="block text-sm font-semibold text-gray-700 mb-1">External Resource URL (Optional)</label>
            <input
              id="resource-url"
              type="url"
              value={externalUrl}
              onChange={(e) => setExternalUrl(e.target.value)}
              placeholder="https://drive.google.com/... or https://github.com/..."
              className="w-full px-4 py-2.5 rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600 text-sm"
            />
          </div>
        </form>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 bg-gray-50 flex items-center justify-end gap-3">
          <button
            type="button"
            onClick={onClose}
            disabled={isSubmitting}
            className="px-4 py-2.5 bg-white text-gray-700 border border-gray-300 font-medium text-sm rounded-xl hover:bg-gray-100 transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="submit"
            onClick={handleSubmit}
            disabled={isSubmitting}
            aria-busy={isSubmitting}
            className="inline-flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white font-semibold text-sm rounded-xl hover:bg-blue-700 transition-colors shadow-md disabled:opacity-50"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Processing...
              </>
            ) : editingResource ? (
              'Save Changes'
            ) : (
              'Upload Resource'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

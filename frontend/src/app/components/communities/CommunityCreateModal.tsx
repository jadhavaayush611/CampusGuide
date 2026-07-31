import React, { useState } from 'react';
import { X, Plus, Sparkles, Loader2, Image, Hash } from 'lucide-react';
import { useCreateCommunity } from '../../../hooks/community/useCreateCommunity';
import { useUpdateCommunity } from '../../../hooks/community/useUpdateCommunity';
import { useCouncils } from '../../../hooks/campus/useCouncils';
import { Community } from '../../../models/community.model';

interface CommunityCreateModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialCommunity?: Community;
}

const CATEGORIES = [
  'Academic',
  'Technology',
  'Creative',
  'Cultural',
  'Sports',
  'Professional',
  'Social',
];

export const CommunityCreateModal: React.FC<CommunityCreateModalProps> = ({
  isOpen,
  onClose,
  initialCommunity,
}) => {
  const isEditing = Boolean(initialCommunity);

  const [name, setName] = useState(initialCommunity?.name || '');
  const [description, setDescription] = useState(initialCommunity?.description || '');
  const [category, setCategory] = useState(initialCommunity?.category || 'Academic');
  const [councilId, setCouncilId] = useState(initialCommunity?.councilId || '');
  const [bannerUrl, setBannerUrl] = useState(initialCommunity?.bannerUrl || '');
  const [logoUrl, setLogoUrl] = useState(initialCommunity?.logoUrl || '');
  const [tagsInput, setTagsInput] = useState(initialCommunity?.tags?.join(', ') || '');

  const createMutation = useCreateCommunity();
  const updateMutation = useUpdateCommunity();
  const { data: councils } = useCouncils();

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const tags = tagsInput
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean);

    if (isEditing && initialCommunity) {
      updateMutation.mutate(
        {
          communityId: initialCommunity.id,
          payload: {
            name,
            description,
            category,
            bannerUrl: bannerUrl || undefined,
            logoUrl: logoUrl || undefined,
            tags,
          },
        },
        {
          onSuccess: () => {
            onClose();
          },
        }
      );
    } else {
      createMutation.mutate(
        {
          name,
          description,
          category,
          councilId: councilId || (councils?.[0]?.id || 'council-1'),
          bannerUrl: bannerUrl || undefined,
          logoUrl: logoUrl || undefined,
          tags,
        },
        {
          onSuccess: () => {
            onClose();
          },
        }
      );
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl space-y-6 relative animate-in fade-in zoom-in-95 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between border-b border-gray-100 pb-4">
          <h3 className="text-lg font-extrabold text-gray-900 flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-blue-600" />
            {isEditing ? 'Edit Community Details' : 'Create Campus Community'}
          </h3>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-gray-400 hover:bg-gray-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
              Community Name *
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Robotics Club"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
              Description *
            </label>
            <textarea
              required
              rows={3}
              placeholder="Describe the mission, activities, and who should join..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              >
                {CATEGORIES.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
                Parent Council
              </label>
              <select
                value={councilId}
                onChange={(e) => setCouncilId(e.target.value)}
                className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              >
                <option value="">Select Council</option>
                {(councils || []).map((council) => (
                  <option key={council.id} value={council.id}>
                    {council.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
              Banner Image URL
            </label>
            <div className="relative">
              <Image className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="url"
                placeholder="https://images.unsplash.com/photo-..."
                value={bannerUrl}
                onChange={(e) => setBannerUrl(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
              Logo Image URL
            </label>
            <div className="relative">
              <Image className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="url"
                placeholder="https://images.unsplash.com/photo-..."
                value={logoUrl}
                onChange={(e) => setLogoUrl(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
              Tags (comma separated)
            </label>
            <div className="relative">
              <Hash className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="e.g. Coding, Robotics, Tech"
                value={tagsInput}
                onChange={(e) => setTagsInput(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
              />
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl text-xs font-semibold text-gray-600 hover:bg-gray-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isPending}
              className="px-6 py-2.5 rounded-xl text-xs font-bold bg-blue-600 text-white hover:bg-blue-700 shadow-md flex items-center gap-2"
            >
              {isPending ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Plus className="w-4 h-4" />
              )}
              {isEditing ? 'Update Community' : 'Create Community'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import { MessageSquare, ThumbsUp, Pin, Bell, Plus, Send, X, Loader2, Sparkles } from 'lucide-react';
import { useCommunityFeed } from '../../../hooks/community/useCommunityFeed';
import { useCreateCommunityPost } from '../../../hooks/community/useCreateCommunityPost';
import { CommunityFeedSkeleton } from './CommunitySkeletons';

interface CommunityFeedProps {
  communityId: string;
}

export const CommunityFeed: React.FC<CommunityFeedProps> = ({ communityId }) => {
  const [filter, setFilter] = useState<'all' | 'announcements' | 'pinned'>('all');
  const [isComposerOpen, setIsComposerOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isAnnouncement, setIsAnnouncement] = useState(false);
  const [isPinned, setIsPinned] = useState(false);

  const { data: feedData, isLoading, isError } = useCommunityFeed(communityId, filter);
  const createPostMutation = useCreateCommunityPost(communityId);

  const handleCreatePost = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;

    createPostMutation.mutate(
      {
        communityId,
        title: title.trim(),
        content: content.trim(),
        isAnnouncement,
        isPinned,
      },
      {
        onSuccess: () => {
          setTitle('');
          setContent('');
          setIsAnnouncement(false);
          setIsPinned(false);
          setIsComposerOpen(false);
        },
      }
    );
  };

  return (
    <div className="space-y-6">
      {/* Header controls & Create Post CTA */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-gray-200 shadow-sm">
        <div className="flex items-center gap-2 overflow-x-auto">
          <button
            onClick={() => setFilter('all')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all ${
              filter === 'all'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            All Discussions
          </button>
          <button
            onClick={() => setFilter('announcements')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 ${
              filter === 'announcements'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Bell className="w-3.5 h-3.5" />
            Announcements
          </button>
          <button
            onClick={() => setFilter('pinned')}
            className={`px-4 py-2 rounded-xl text-xs font-semibold transition-all flex items-center gap-1.5 ${
              filter === 'pinned'
                ? 'bg-blue-600 text-white shadow-sm'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            <Pin className="w-3.5 h-3.5" />
            Pinned
          </button>
        </div>

        <button
          onClick={() => setIsComposerOpen(true)}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-semibold flex items-center justify-center gap-2 shadow-sm transition-all"
        >
          <Plus className="w-4 h-4" />
          New Discussion
        </button>
      </div>

      {/* Composer Modal */}
      {isComposerOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl space-y-4 relative animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3">
              <h3 className="font-bold text-lg text-gray-900 flex items-center gap-2">
                <Sparkles className="w-5 h-5 text-blue-600" /> Start Community Discussion
              </h3>
              <button
                onClick={() => setIsComposerOpen(false)}
                className="p-1 rounded-lg text-gray-400 hover:bg-gray-100"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreatePost} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
                  Title
                </label>
                <input
                  type="text"
                  required
                  placeholder="What would you like to discuss?"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-gray-700 uppercase mb-1">
                  Content
                </label>
                <textarea
                  required
                  rows={4}
                  placeholder="Share details, questions, or ideas..."
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                />
              </div>

              <div className="flex items-center gap-4 pt-1">
                <label className="flex items-center gap-2 cursor-pointer text-xs font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={isAnnouncement}
                    onChange={(e) => setIsAnnouncement(e.target.checked)}
                    className="rounded text-blue-600 focus:ring-blue-500"
                  />
                  Mark as Announcement
                </label>
                <label className="flex items-center gap-2 cursor-pointer text-xs font-medium text-gray-700">
                  <input
                    type="checkbox"
                    checked={isPinned}
                    onChange={(e) => setIsPinned(e.target.checked)}
                    className="rounded text-blue-600 focus:ring-blue-500"
                  />
                  Pin Discussion
                </label>
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsComposerOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-gray-600 hover:bg-gray-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={createPostMutation.isPending}
                  className="px-5 py-2 rounded-xl text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 flex items-center gap-2"
                >
                  {createPostMutation.isPending ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Send className="w-4 h-4" />
                  )}
                  Post Discussion
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Feed Posts List */}
      {isLoading ? (
        <CommunityFeedSkeleton />
      ) : isError ? (
        <div className="bg-red-50 border border-red-200 rounded-2xl p-6 text-center text-red-700">
          <p className="font-semibold">Unable to load community discussions.</p>
        </div>
      ) : !feedData?.posts || feedData.posts.length === 0 ? (
        <div className="bg-white rounded-2xl border border-gray-200 p-12 text-center space-y-3">
          <MessageSquare className="w-12 h-12 text-gray-300 mx-auto" />
          <h3 className="text-lg font-bold text-gray-900">No discussions yet</h3>
          <p className="text-sm text-gray-500 max-w-sm mx-auto">
            Be the first to start a conversation in this community!
          </p>
          <button
            onClick={() => setIsComposerOpen(true)}
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-semibold shadow-sm hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" /> Start Discussion
          </button>
        </div>
      ) : (
        <div className="space-y-4">
          {feedData.posts.map((post) => (
            <div
              key={post.id}
              className="bg-white rounded-2xl border border-gray-200/80 p-6 shadow-sm hover:shadow-md transition-shadow space-y-4"
            >
              {/* Badges */}
              <div className="flex items-center gap-2">
                {post.isPinned && (
                  <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800 flex items-center gap-1">
                    <Pin className="w-3 h-3" /> Pinned
                  </span>
                )}
                {post.isAnnouncement && (
                  <span className="px-2.5 py-0.5 rounded-full text-[11px] font-bold bg-purple-100 text-purple-800 flex items-center gap-1">
                    <Bell className="w-3 h-3" /> Announcement
                  </span>
                )}
              </div>

              {/* Author & Timestamp */}
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center text-white font-bold text-sm shadow-sm">
                  {post.authorName.charAt(0)}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h4 className="text-sm font-bold text-gray-900">{post.authorName}</h4>
                    {post.authorRole && (
                      <span className="text-[10px] uppercase font-bold px-1.5 py-0.2 bg-blue-50 text-blue-700 rounded">
                        {post.authorRole}
                      </span>
                    )}
                  </div>
                  <p className="text-xs text-gray-400">
                    {new Date(post.createdAt).toLocaleDateString(undefined, {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
              </div>

              {/* Title & Body */}
              <div className="space-y-2">
                <h3 className="text-base font-bold text-gray-900">{post.title}</h3>
                <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">
                  {post.content}
                </p>
              </div>

              {/* Post Footer */}
              <div className="pt-3 border-t border-gray-100 flex items-center space-x-6 text-xs font-semibold text-gray-500">
                <button className="flex items-center space-x-1.5 hover:text-blue-600 transition-colors">
                  <ThumbsUp className="w-4 h-4" />
                  <span>{post.likeCount} Likes</span>
                </button>
                <button className="flex items-center space-x-1.5 hover:text-blue-600 transition-colors">
                  <MessageSquare className="w-4 h-4" />
                  <span>{post.commentCount} Comments</span>
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

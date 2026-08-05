import React, { useState, memo, useCallback } from 'react';
import {
  Plus,
  Search,
  MessageSquare,
  Archive,
  RotateCcw,
  Trash2,
  Edit2,
  Check,
  X,
  Filter,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  FolderArchive,
  Sparkles,
} from 'lucide-react';
import { AtlasConversation, ConversationStatus, ConversationType } from '../../../models/atlas.model';
import { useAtlasConversations } from '../../../hooks/atlas/useAtlasConversations';
import { useCreateConversation } from '../../../hooks/atlas/useCreateConversation';
import { useRenameConversation } from '../../../hooks/atlas/useRenameConversation';
import { useArchiveConversation } from '../../../hooks/atlas/useArchiveConversation';
import { useRestoreConversation } from '../../../hooks/atlas/useRestoreConversation';
import { useDeleteConversation } from '../../../hooks/atlas/useDeleteConversation';

interface AtlasSidebarProps {
  activeConversationId: string | null;
  onSelectConversation: (conversation: AtlasConversation) => void;
}

interface AtlasConversationItemProps {
  conv: AtlasConversation;
  isActive: boolean;
  isEditing: boolean;
  editingTitle: string;
  tab: ConversationStatus;
  onSelectConversation: (conversation: AtlasConversation) => void;
  onStartRename: (conv: AtlasConversation, e: React.MouseEvent) => void;
  onSaveRename: (id: string, e: React.MouseEvent) => void;
  onCancelRename: (e: React.MouseEvent) => void;
  onEditingTitleChange: (title: string) => void;
  onArchive: (id: string, e: React.MouseEvent) => void;
  onRestore: (id: string, e: React.MouseEvent) => void;
  onDelete: (id: string, e: React.MouseEvent) => void;
}

const AtlasConversationItem = memo(function AtlasConversationItem({
  conv,
  isActive,
  isEditing,
  editingTitle,
  tab,
  onSelectConversation,
  onStartRename,
  onSaveRename,
  onCancelRename,
  onEditingTitleChange,
  onArchive,
  onRestore,
  onDelete,
}: AtlasConversationItemProps) {
  return (
    <div
      onClick={() => !isEditing && onSelectConversation(conv)}
      onKeyDown={(e) => {
        if (!isEditing && (e.key === 'Enter' || e.key === ' ')) {
          e.preventDefault();
          onSelectConversation(conv);
        }
      }}
      role="button"
      tabIndex={0}
      aria-label={`Conversation: ${conv.title || 'Untitled Conversation'}. ${conv.messageCount ?? 0} messages. Type: ${conv.type || 'GENERAL'}`}
      className={`group relative p-3 rounded-xl cursor-pointer transition-all border focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#2563EB] focus-visible:ring-offset-2 ${
        isActive
          ? 'bg-blue-50/80 border-blue-200 text-[#2563EB] shadow-xs'
          : 'bg-white border-transparent hover:bg-gray-50 text-gray-700'
      }`}
    >
      {isEditing ? (
        <div className="flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
          <input
            type="text"
            value={editingTitle}
            onChange={(e) => onEditingTitleChange(e.target.value)}
            className="flex-1 px-2 py-1 bg-white border border-blue-400 rounded text-xs text-gray-900 focus:outline-none"
            autoFocus
          />
          <button
            onClick={(e) => onSaveRename(conv.id, e)}
            className="p-1 text-emerald-600 hover:bg-emerald-50 rounded"
          >
            <Check className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={onCancelRename}
            className="p-1 text-gray-400 hover:bg-gray-100 rounded"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      ) : (
        <>
          <div className="flex items-start justify-between gap-2">
            <h4 className="font-semibold text-xs truncate flex-1 text-gray-900">
              {conv.title || 'Untitled Conversation'}
            </h4>
            <span className="text-[10px] px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded font-medium uppercase tracking-wider flex-shrink-0">
              {conv.type || 'GENERAL'}
            </span>
          </div>

          <div className="flex items-center justify-between mt-2 text-[11px] text-gray-500">
            <span>{conv.messageCount ?? 0} msgs</span>
            <span>
              {conv.updatedAt
                ? new Date(conv.updatedAt).toLocaleDateString(undefined, {
                    month: 'short',
                    day: 'numeric',
                  })
                : ''}
            </span>
          </div>

          {/* Actions overlay on hover and focus */}
          <div className="absolute right-2 top-2 hidden group-hover:flex group-focus-within:flex items-center gap-1 bg-white/95 p-1 rounded-lg border border-gray-200 shadow-xs">
            <button
              onClick={(e) => onStartRename(conv, e)}
              title="Rename"
              className="p-1 text-gray-600 hover:text-blue-600 hover:bg-gray-100 rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
            >
              <Edit2 className="w-3.5 h-3.5" />
            </button>

             {tab === 'ACTIVE' ? (
              <button
                onClick={(e) => onArchive(conv.id, e)}
                title="Archive"
                className="p-1 text-gray-600 hover:text-amber-600 hover:bg-gray-100 rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                <Archive className="w-3.5 h-3.5" />
              </button>
             ) : (
              <button
                onClick={(e) => onRestore(conv.id, e)}
                title="Restore"
                className="p-1 text-gray-600 hover:text-emerald-600 hover:bg-gray-100 rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
              >
                <RotateCcw className="w-3.5 h-3.5" />
              </button>
             )}

             <button
               onClick={(e) => onDelete(conv.id, e)}
               title="Delete"
               className="p-1 text-gray-600 hover:text-red-600 hover:bg-gray-100 rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600"
             >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        </>
      )}
    </div>
  );
});

export const AtlasSidebar = memo(function AtlasSidebar({
  activeConversationId,
  onSelectConversation,
}: AtlasSidebarProps) {
  const [tab, setTab] = useState<ConversationStatus>('ACTIVE');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [sortBy] = useState<'updatedAt' | 'title' | 'messageCount'>('updatedAt');
  const [sortOrder, setSortOrder] = useState<'desc' | 'asc'>('desc');

  // Modal / inline editing state
  const [isCreating, setIsCreating] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newType, setNewType] = useState<ConversationType>('ACADEMIC_ADVISOR');
  
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState('');

  // React Query Hooks
  const { data: conversationData, isLoading } = useAtlasConversations({
    status: tab,
    search,
    page,
    limit: 10,
    sortBy,
    sortOrder,
  });

  const createMutation = useCreateConversation();
  const renameMutation = useRenameConversation();
  const archiveMutation = useArchiveConversation();
  const restoreMutation = useRestoreConversation();
  const deleteMutation = useDeleteConversation();

  const conversations = conversationData?.data || [];
  const totalPages = conversationData?.totalPages || 1;

  const handleCreate = useCallback((e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;

    createMutation.mutate(
      { title: newTitle.trim(), type: newType },
      {
        onSuccess: (newConv) => {
          setIsCreating(false);
          setNewTitle('');
          onSelectConversation(newConv);
        },
      }
    );
  }, [newTitle, newType, createMutation, onSelectConversation]);

  const handleStartRename = useCallback((conv: AtlasConversation, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingId(conv.id);
    setEditingTitle(conv.title);
  }, []);

  const handleCancelRename = useCallback((e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingId(null);
  }, []);

  const handleSaveRename = useCallback((id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!editingTitle.trim()) return;

    renameMutation.mutate(
      { id, title: editingTitle.trim() },
      {
        onSuccess: () => setEditingId(null),
      }
    );
  }, [editingTitle, renameMutation]);

  const handleArchive = useCallback((id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    archiveMutation.mutate(id);
  }, [archiveMutation]);

  const handleRestore = useCallback((id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    restoreMutation.mutate(id);
  }, [restoreMutation]);

  const handleDelete = useCallback((id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (confirm('Are you sure you want to delete this conversation?')) {
      deleteMutation.mutate(id);
    }
  }, [deleteMutation]);

  return (
    <aside className="w-80 bg-white border-r border-gray-200 flex flex-col h-full overflow-hidden">
      {/* Sidebar Header */}
      <div className="p-4 border-b border-gray-200 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-[#2563EB]" />
            <h2 className="font-semibold text-gray-900 text-base">Conversations</h2>
          </div>
          <button
            onClick={() => setIsCreating(true)}
            className="flex items-center gap-1 px-3 py-1.5 bg-[#2563EB] hover:bg-blue-700 text-white font-semibold text-xs rounded-xl transition-all shadow-xs"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Chat</span>
          </button>
        </div>

        {/* Tab switcher: Active vs Archived */}
        <div className="flex bg-gray-100 p-1 rounded-xl text-xs font-semibold text-gray-600">
          <button
            onClick={() => {
              setTab('ACTIVE');
              setPage(1);
            }}
            className={`flex-1 py-1.5 rounded-lg transition-all flex items-center justify-center gap-1.5 ${
              tab === 'ACTIVE'
                ? 'bg-white text-[#2563EB] shadow-xs'
                : 'hover:text-gray-900'
            }`}
          >
            <MessageSquare className="w-3.5 h-3.5" />
            <span>Recent</span>
          </button>
          <button
            onClick={() => {
              setTab('ARCHIVED');
              setPage(1);
            }}
            className={`flex-1 py-1.5 rounded-lg transition-all flex items-center justify-center gap-1.5 ${
              tab === 'ARCHIVED'
                ? 'bg-white text-[#2563EB] shadow-xs'
                : 'hover:text-gray-900'
            }`}
          >
            <FolderArchive className="w-3.5 h-3.5" />
            <span>Archived</span>
          </button>
        </div>

        {/* Search Bar */}
        <div className="relative">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            placeholder="Search conversations..."
            className="w-full pl-9 pr-3 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs text-gray-900 placeholder-gray-400 focus:outline-none focus:border-[#2563EB]"
          />
        </div>

        {/* Sorting option */}
        <div className="flex items-center justify-between text-[11px] text-gray-500 pt-1">
          <span className="font-medium">
            {conversationData?.total ?? 0} {tab.toLowerCase()} chats
          </span>
          <button
            onClick={() => {
              setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc');
            }}
            className="flex items-center gap-1 hover:text-gray-900 transition-colors"
          >
            <ArrowUpDown className="w-3 h-3" />
            <span>{sortOrder === 'desc' ? 'Newest' : 'Oldest'}</span>
          </button>
        </div>
      </div>

      {/* New Conversation Form (If creating) */}
      {isCreating && (
        <form onSubmit={handleCreate} className="p-4 bg-blue-50/50 border-b border-blue-100 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-blue-900 uppercase tracking-wider">
              Create New Chat
            </span>
            <button
              type="button"
              onClick={() => setIsCreating(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <input
            type="text"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            placeholder="e.g., Fall 2026 Academic Advising"
            required
            autoFocus
            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-900 focus:outline-none focus:border-[#2563EB]"
          />

          <select
            value={newType}
            onChange={(e) => setNewType(e.target.value as ConversationType)}
            className="w-full px-3 py-2 bg-white border border-gray-300 rounded-xl text-xs text-gray-900 focus:outline-none focus:border-[#2563EB]"
          >
            <option value="ACADEMIC_ADVISOR">Academic Advisor</option>
            <option value="CAMPUS_GUIDE">Campus Guide & Maps</option>
            <option value="PLANNER">Planner & Schedule</option>
            <option value="RESEARCH">Research & Resources</option>
            <option value="GENERAL">General Assistant</option>
          </select>

          <div className="flex justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={() => setIsCreating(false)}
              className="px-3 py-1.5 text-xs text-gray-600 hover:text-gray-900 font-medium"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending || !newTitle.trim()}
              className="px-3 py-1.5 bg-[#2563EB] hover:bg-blue-700 text-white font-semibold text-xs rounded-lg transition-colors shadow-xs"
            >
              {createMutation.isPending ? 'Creating...' : 'Create Chat'}
            </button>
          </div>
        </form>
      )}

      {/* Conversations List */}
      <div className="flex-1 overflow-y-auto p-3 space-y-1">
        {isLoading ? (
          <div className="space-y-2 py-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-14 bg-gray-100 animate-pulse rounded-xl" />
            ))}
          </div>
        ) : conversations.length === 0 ? (
          <div className="py-12 text-center text-gray-400 text-xs">
            <MessageSquare className="w-8 h-8 mx-auto text-gray-300 mb-2" />
            <p>No {tab.toLowerCase()} conversations found.</p>
          </div>
        ) : (
          conversations.map((conv) => (
            <AtlasConversationItem
              key={conv.id}
              conv={conv}
              isActive={activeConversationId === conv.id}
              isEditing={editingId === conv.id}
              editingTitle={editingTitle}
              tab={tab}
              onSelectConversation={onSelectConversation}
              onStartRename={handleStartRename}
              onSaveRename={handleSaveRename}
              onCancelRename={handleCancelRename}
              onEditingTitleChange={setEditingTitle}
              onArchive={handleArchive}
              onRestore={handleRestore}
              onDelete={handleDelete}
            />
          ))
        )}
      </div>

      {/* Pagination Footer */}
      {totalPages > 1 && (
        <nav
          role="navigation"
          aria-label="Pagination"
          className="p-3 border-t border-gray-200 flex items-center justify-between text-xs text-gray-600 bg-gray-50"
        >
          <button
            onClick={() => setPage((p) => Math.max(p - 1, 1))}
            disabled={page === 1}
            aria-label="Go to previous page"
            className="p-1 hover:bg-gray-200 rounded disabled:opacity-40 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
          >
            <ChevronLeft className="w-4 h-4" aria-hidden="true" />
          </button>
          <span aria-current="page">
            Page {page} of {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(p + 1, totalPages))}
            disabled={page === totalPages}
            aria-label="Go to next page"
            className="p-1 hover:bg-gray-200 rounded disabled:opacity-40 focus-visible:outline-hidden focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-1"
          >
            <ChevronRight className="w-4 h-4" aria-hidden="true" />
          </button>
        </nav>
      )}
    </aside>
  );
});

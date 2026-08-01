import React, { useState } from 'react';
import {
  Sparkles,
  Info,
  Archive,
  RotateCcw,
  Trash2,
  Edit2,
  Check,
  X,
  ShieldCheck,
  Zap,
} from 'lucide-react';
import { AtlasConversation } from '../../../models/atlas.model';
import { useRenameConversation } from '../../../hooks/atlas/useRenameConversation';
import { useArchiveConversation } from '../../../hooks/atlas/useArchiveConversation';
import { useRestoreConversation } from '../../../hooks/atlas/useRestoreConversation';
import { useDeleteConversation } from '../../../hooks/atlas/useDeleteConversation';

interface AtlasHeaderProps {
  conversation: AtlasConversation | null;
  onOpenCapabilities: () => void;
}

export function AtlasHeader({ conversation, onOpenCapabilities }: AtlasHeaderProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [title, setTitle] = useState(conversation?.title || '');

  const renameMutation = useRenameConversation();
  const archiveMutation = useArchiveConversation();
  const restoreMutation = useRestoreConversation();
  const deleteMutation = useDeleteConversation();

  const handleSaveRename = () => {
    if (!conversation || !title.trim()) return;
    renameMutation.mutate(
      { id: conversation.id, title: title.trim() },
      { onSuccess: () => setIsEditing(false) }
    );
  };

  const handleArchive = () => {
    if (conversation) archiveMutation.mutate(conversation.id);
  };

  const handleRestore = () => {
    if (conversation) restoreMutation.mutate(conversation.id);
  };

  const handleDelete = () => {
    if (conversation && confirm('Are you sure you want to delete this conversation?')) {
      deleteMutation.mutate(conversation.id);
    }
  };

  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 flex items-center justify-between gap-4">
      <div className="flex items-center gap-3 min-w-0">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#2563EB] to-indigo-600 flex items-center justify-center text-white font-bold shadow-xs flex-shrink-0">
          <Sparkles className="w-5 h-5" />
        </div>

        <div className="min-w-0 flex-1">
          {isEditing ? (
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="px-3 py-1 bg-white border border-blue-500 rounded-lg text-sm text-gray-900 focus:outline-none"
                autoFocus
              />
              <button
                onClick={handleSaveRename}
                className="p-1 text-emerald-600 hover:bg-emerald-50 rounded-lg"
              >
                <Check className="w-4 h-4" />
              </button>
              <button
                onClick={() => setIsEditing(false)}
                className="p-1 text-gray-400 hover:bg-gray-100 rounded-lg"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-base font-bold text-gray-900 truncate">
                {conversation?.title || 'Atlas AI Orchestrator'}
              </h1>
              {conversation && (
                <>
                  <button
                    onClick={() => {
                      setTitle(conversation.title);
                      setIsEditing(true);
                    }}
                    className="p-1 text-gray-400 hover:text-blue-600 rounded transition-colors"
                    title="Rename conversation"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded-full border uppercase tracking-wider ${
                      conversation.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : 'bg-amber-50 text-amber-700 border-amber-200'
                    }`}
                  >
                    {conversation.status}
                  </span>
                  <span className="text-[10px] font-semibold px-2 py-0.5 bg-blue-50 text-blue-700 border border-blue-200 rounded-full uppercase tracking-wider">
                    {conversation.type || 'GENERAL'}
                  </span>
                </>
              )}
            </div>
          )}

          <p className="text-xs text-gray-500 mt-0.5 truncate">
            Workflow Orchestration Pipeline • Autonomous Agentic Assistant
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2">
        <button
          onClick={onOpenCapabilities}
          className="flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 font-semibold text-xs rounded-xl transition-colors"
        >
          <Info className="w-3.5 h-3.5 text-blue-600" />
          <span className="hidden sm:inline">Engine Info</span>
        </button>

        {conversation && (
          <div className="flex items-center gap-1 border-l border-gray-200 pl-2">
            {conversation.status === 'ACTIVE' ? (
              <button
                onClick={handleArchive}
                title="Archive conversation"
                className="p-2 text-gray-500 hover:text-amber-600 hover:bg-amber-50 rounded-xl transition-colors"
              >
                <Archive className="w-4 h-4" />
              </button>
            ) : (
              <button
                onClick={handleRestore}
                title="Restore conversation"
                className="p-2 text-gray-500 hover:text-emerald-600 hover:bg-emerald-50 rounded-xl transition-colors"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
            )}

            <button
              onClick={handleDelete}
              title="Delete conversation"
              className="p-2 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-xl transition-colors"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

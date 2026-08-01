import React, { lazy, Suspense, memo } from 'react';
import { User, Sparkles } from 'lucide-react';
import { CampusResultCard } from './CampusResultCard';
import { ConversationHistoryMessage } from '../../../models/atlas.model';

const MarkdownRenderer = lazy(() =>
  import('./MarkdownRenderer').then((m) => ({ default: m.MarkdownRenderer }))
);

interface MessageBubbleProps {
  message: ConversationHistoryMessage;
  isStreaming?: boolean;
  onRetry?: () => void;
}

export const MessageBubble = memo(function MessageBubble({ message, isStreaming = false }: MessageBubbleProps) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-3 my-4 ${isUser ? 'justify-end' : 'justify-start'}`}>
      {!isUser && (
        <div className="w-8 h-8 rounded-xl bg-gradient-to-br from-[#2563EB] to-indigo-600 flex items-center justify-center text-white flex-shrink-0 shadow-sm">
          <Sparkles className="w-4.5 h-4.5" />
        </div>
      )}

      <div className={`max-w-[82%] ${isUser ? 'order-1' : 'order-2'}`}>
        <div className="flex items-center gap-2 mb-1">
          <span className="text-xs font-semibold text-gray-700">
            {isUser ? 'You' : 'Atlas Orchestrator'}
          </span>
          {message.timestamp && (
            <span className="text-[10px] text-gray-400 font-mono">{message.timestamp}</span>
          )}
        </div>

        <div
          className={`p-4 rounded-2xl ${
            isUser
              ? 'bg-[#2563EB] text-white rounded-tr-xs shadow-sm'
              : 'bg-white border border-gray-200/80 text-gray-900 rounded-tl-xs shadow-sm'
          }`}
        >
          {isUser ? (
            <p className="text-sm leading-relaxed whitespace-pre-wrap">{message.content}</p>
          ) : (
            <>
              <Suspense fallback={<div className="h-6 w-3/4 bg-gray-100 animate-pulse rounded" />}>
                <MarkdownRenderer content={message.content} />
              </Suspense>

              {isStreaming && (
                <div className="inline-flex items-center gap-1.5 mt-2 px-2.5 py-1 bg-blue-50 text-blue-700 font-semibold text-xs rounded-full border border-blue-200 animate-pulse">
                  <Sparkles className="w-3.5 h-3.5 animate-spin text-blue-600" />
                  <span>Streaming Atlas output...</span>
                </div>
              )}
            </>
          )}
        </div>

        {/* Structured Campus Result Card */}
        {message.campusResult && (
          <div className="mt-2">
            <CampusResultCard result={message.campusResult} />
          </div>
        )}
      </div>

      {isUser && (
        <div className="w-8 h-8 rounded-xl bg-gray-200 flex items-center justify-center text-gray-600 flex-shrink-0 order-2">
          <User className="w-4.5 h-4.5" />
        </div>
      )}
    </div>
  );
});

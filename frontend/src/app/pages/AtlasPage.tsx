import React, { lazy, Suspense, useState, useEffect, useRef } from 'react';
import {
  Sparkles,
  AlertTriangle,
  RefreshCw,
  SlidersHorizontal,
  Bot,
  Brain,
  Wrench,
  Compass,
} from 'lucide-react';
import {
  AtlasSidebar,
  AtlasHeader,
  ThinkingTimeline,
  ToolExecutionPanel,
  MessageBubble,
  MessageComposer,
  AtlasErrorBoundary,
} from '../components/atlas';
import { useAtlasConversations } from '../../hooks/atlas/useAtlasConversations';
import { useConversationHistory } from '../../hooks/atlas/useConversationHistory';
import { useAtlasStreamChat } from '../../hooks/atlas/useAtlasStreamChat';
import { AtlasConversation, ConversationHistoryMessage } from '../../models/atlas.model';

const AtlasCapabilitiesModal = lazy(() =>
  import('../components/atlas/AtlasCapabilitiesModal').then((m) => ({ default: m.AtlasCapabilitiesModal }))
);


export function AtlasPage() {
  const [selectedConversation, setSelectedConversation] = useState<AtlasConversation | null>(null);
  const [isCapabilitiesOpen, setIsCapabilitiesOpen] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Fetch initial list of conversations
  const { data: conversationData, isLoading: isLoadingList } = useAtlasConversations({
    status: 'ACTIVE',
    limit: 10,
  });

  // Auto-select first conversation on load if none selected
  useEffect(() => {
    if (conversationData?.data && conversationData.data.length > 0 && !selectedConversation) {
      setSelectedConversation(conversationData.data[0]);
    }
  }, [conversationData, selectedConversation]);

  // Fetch message history for selected conversation
  const { data: historyData, isLoading: isLoadingHistory } = useConversationHistory(
    selectedConversation?.id
  );

  // Custom streaming hook
  const {
    isStreaming,
    currentPrompt,
    streamedContent,
    timelineItems,
    toolExecutions,
    campusResult,
    error: streamingError,
    startStream,
    cancelStream,
    retryStream,
  } = useAtlasStreamChat();

  const historyMessages = historyData?.messages || [];

  // Scroll to bottom when messages or streamed content updates
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [historyMessages, streamedContent, isStreaming]);

  const handleSendPrompt = (prompt: string, model: string) => {
    startStream(prompt, {
      conversationId: selectedConversation?.id,
      model,
    });
  };

  return (
    <div className="flex h-[calc(100vh-81px)] bg-gray-100/60 overflow-hidden font-sans">
      {/* Left Column: Conversations Sidebar */}
      <AtlasErrorBoundary fallbackTitle="Sidebar Error">
        <AtlasSidebar
          activeConversationId={selectedConversation?.id || null}
          onSelectConversation={(conv) => setSelectedConversation(conv)}
        />
      </AtlasErrorBoundary>

      {/* Main Workspace */}
      <div className="flex-1 flex flex-col min-w-0 bg-white shadow-xs">
        {/* Header Bar */}
        <AtlasErrorBoundary fallbackTitle="Header Error">
          <AtlasHeader
            conversation={selectedConversation}
            onOpenCapabilities={() => setIsCapabilitiesOpen(true)}
          />
        </AtlasErrorBoundary>

        {/* Execution Pipeline Status Banner (MANDATORY Identity) */}
        <div className="bg-gradient-to-r from-blue-900 via-indigo-900 to-slate-900 px-6 py-2.5 text-white flex items-center justify-between text-xs border-b border-blue-800/50 shadow-inner">
          <div className="flex items-center gap-3 overflow-x-auto">
            <span className="font-bold text-blue-300 uppercase tracking-wider text-[10px] flex items-center gap-1 flex-shrink-0">
              <Compass className="w-3.5 h-3.5 text-blue-400 animate-spin-slow" />
              Pipeline:
            </span>
            <div className="flex items-center gap-2 text-[11px] font-mono text-gray-300">
              <span className="text-blue-200">Conversation</span>
              <span>➔</span>
              <span className={isStreaming ? 'text-emerald-400 font-bold animate-pulse' : 'text-gray-300'}>
                Streaming Response
              </span>
              <span>➔</span>
              <span className={timelineItems.length > 0 ? 'text-purple-300 font-bold' : 'text-gray-400'}>
                Thinking Timeline
              </span>
              <span>➔</span>
              <span className={toolExecutions.length > 0 ? 'text-teal-300 font-bold' : 'text-gray-400'}>
                Tool Execution
              </span>
              <span>➔</span>
              <span className={campusResult ? 'text-amber-300 font-bold' : 'text-gray-400'}>
                Campus Result
              </span>
            </div>
          </div>

          <div className="hidden md:flex items-center gap-2 text-[10px] font-semibold">
            <span className="px-2 py-0.5 bg-blue-500/20 text-blue-300 rounded border border-blue-400/30">
              ORCHESTRATOR ACTIVE
            </span>
          </div>
        </div>

        {/* Chat Messages Canvas */}
        <div className="flex-1 overflow-y-auto p-6 space-y-4">
          <AtlasErrorBoundary fallbackTitle="Message Canvas Error">
            {isLoadingHistory ? (
              <div className="space-y-4 py-8">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="h-20 bg-gray-100 rounded-2xl animate-pulse" />
                ))}
              </div>
            ) : historyMessages.length === 0 && !isStreaming ? (
              <div className="h-full flex flex-col items-center justify-center text-center p-8 max-w-lg mx-auto my-12">
                <div className="w-14 h-14 rounded-2xl bg-blue-50 text-[#2563EB] flex items-center justify-center mb-4 shadow-sm">
                  <Brain className="w-7 h-7" />
                </div>
                <h3 className="text-lg font-bold text-gray-900">
                  Welcome to Atlas AI Orchestrator
                </h3>
                <p className="text-xs text-gray-500 mt-2 leading-relaxed">
                  Atlas contextually orchestrates your degree plan, course recommendations, class timetables, campus navigation, community notices, and resource searches.
                </p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-6 w-full text-left text-xs">
                  <button
                    onClick={() => handleSendPrompt('What courses should I take next semester for AI specialization?', 'gpt-4o-mini')}
                    className="p-3 bg-gray-50 hover:bg-blue-50/60 border border-gray-200 hover:border-blue-300 rounded-xl transition-all font-medium text-gray-700"
                  >
                    💡 "Course recommendations for AI"
                  </button>
                  <button
                    onClick={() => handleSendPrompt('Find open study rooms near Computer Science building', 'gpt-4o-mini')}
                    className="p-3 bg-gray-50 hover:bg-blue-50/60 border border-gray-200 hover:border-blue-300 rounded-xl transition-all font-medium text-gray-700"
                  >
                    🗺️ "Find study rooms near CS"
                  </button>
                </div>
              </div>
            ) : (
              <>
                {historyMessages.map((msg, idx) => (
                  <MessageBubble key={msg.id || idx} message={msg} />
                ))}

                {/* Live User Prompt message while streaming */}
                {isStreaming && currentPrompt && (
                  <MessageBubble message={{ role: 'user', content: currentPrompt }} />
                )}

                {/* Live Assistant Streaming Response */}
                {isStreaming && (
                  <MessageBubble
                    message={{
                      role: 'assistant',
                      content: streamedContent || 'Atlas orchestrator initializing context...',
                      campusResult: campusResult || undefined,
                    }}
                    isStreaming={isStreaming}
                  />
                )}

                {/* Streaming Error Notification & Recovery */}
                {streamingError && (
                  <div className="p-4 bg-red-50 border border-red-200 rounded-xl flex items-center justify-between gap-3 text-xs text-red-700">
                    <div className="flex items-center gap-2">
                      <AlertTriangle className="w-4 h-4 text-red-600 flex-shrink-0" />
                      <span>{streamingError.message || 'Stream interrupted or timed out'}</span>
                    </div>
                    <button
                      onClick={() => retryStream({ conversationId: selectedConversation?.id })}
                      className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white font-semibold text-xs rounded-lg transition-colors flex items-center gap-1 shadow-2xs"
                    >
                      <RefreshCw className="w-3.5 h-3.5" />
                      <span>Retry</span>
                    </button>
                  </div>
                )}

                <div ref={messagesEndRef} />
              </>
            )}
          </AtlasErrorBoundary>
        </div>

        {/* Message Composer */}
        <AtlasErrorBoundary fallbackTitle="Composer Error">
          <MessageComposer
            onSend={handleSendPrompt}
            onCancel={cancelStream}
            isStreaming={isStreaming}
          />
        </AtlasErrorBoundary>
      </div>

      {/* Right Column: Workflow Orchestration Inspector */}
      <aside className="w-96 bg-gray-50/70 border-l border-gray-200 p-4 flex flex-col h-full overflow-y-auto space-y-4">
        <div className="flex items-center justify-between pb-2 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <SlidersHorizontal className="w-4 h-4 text-[#2563EB]" />
            <h2 className="font-bold text-gray-900 text-xs uppercase tracking-wider">
              Workflow Inspector
            </h2>
          </div>
          <span className="text-[10px] font-bold px-2 py-0.5 bg-blue-100 text-blue-800 rounded-full">
            REALTIME
          </span>
        </div>

        {/* Thinking Timeline Panel */}
        <AtlasErrorBoundary fallbackTitle="Timeline Error">
          <ThinkingTimeline items={timelineItems} isStreaming={isStreaming} />
        </AtlasErrorBoundary>

        {/* Tool Execution Panel */}
        <AtlasErrorBoundary fallbackTitle="Tools Error">
          <ToolExecutionPanel tools={toolExecutions} isStreaming={isStreaming} />
        </AtlasErrorBoundary>
      </aside>

      {/* Engine Capabilities Modal */}
      <Suspense fallback={null}>
        <AtlasCapabilitiesModal
          isOpen={isCapabilitiesOpen}
          onClose={() => setIsCapabilitiesOpen(false)}
        />
      </Suspense>
    </div>
  );
}

import { useState, useCallback, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { atlasClient } from '../../sdk/atlasClientInstance';
import { queryKeys } from '../../sdk/queryKeys';
import {
  AtlasStreamEventType,
  ThinkingTimelineItem,
  ToolExecutionItem,
  CampusResult,
  ConversationHistoryMessage,
} from '../../models/atlas.model';

interface StreamChatOptions {
  conversationId?: string;
  model?: string;
  temperature?: number;
  systemPrompt?: string;
}

export function useAtlasStreamChat() {
  const queryClient = useQueryClient();
  const [isStreaming, setIsStreaming] = useState(false);
  const [currentPrompt, setCurrentPrompt] = useState('');
  const [streamedContent, setStreamedContent] = useState('');
  const [timelineItems, setTimelineItems] = useState<ThinkingTimelineItem[]>([]);
  const [toolExecutions, setToolExecutions] = useState<ToolExecutionItem[]>([]);
  const [campusResult, setCampusResult] = useState<CampusResult | null>(null);
  const [error, setError] = useState<Error | null>(null);

  const streamControllerRef = useRef<{ cancel: () => void } | null>(null);
  const activePromptRef = useRef<string>('');

  const addTimelineEvent = useCallback(
    (type: AtlasStreamEventType, payload: any) => {
      const now = new Date().toLocaleTimeString();
      let label = type.replace('_', ' ');
      let message = typeof payload === 'string' ? payload : payload?.message || payload?.phase || '';
      let status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' = 'COMPLETED';

      switch (type) {
        case 'CONNECTION_OPENED':
          label = 'Connection Established';
          message = `Session ${payload?.sessionId || 'initialized'} (${payload?.status || 'connected'})`;
          status = 'COMPLETED';
          break;
        case 'THINKING':
          label = 'Context & Intent Analysis';
          message = payload?.message || 'Extracting query intent & evaluating context constraints';
          status = 'IN_PROGRESS';
          break;
        case 'REASONING':
          label = 'Knowledge Graph Reasoning';
          message = payload?.message || 'Navigating academic knowledge graph & evidence fusion';
          status = 'IN_PROGRESS';
          break;
        case 'PLANNING':
          label = 'Workflow Plan Generation';
          message = payload?.message || 'Formulating optimal execution plan';
          status = 'IN_PROGRESS';
          break;
        case 'EXECUTION_STARTED':
          label = 'Workflow Runtime Initiated';
          message = `Execution ID: ${payload?.executionId || 'exec-live'}`;
          status = 'IN_PROGRESS';
          break;
        case 'TOOL_STARTED':
          label = `Tool Started: ${payload?.toolName || 'Capability'}`;
          message = `Executing ${payload?.toolName || 'system tool'}...`;
          status = 'IN_PROGRESS';
          break;
        case 'TOOL_COMPLETED':
          label = `Tool Completed: ${payload?.toolName || 'Capability'}`;
          message = `Successfully executed ${payload?.toolName || 'tool'}`;
          status = 'COMPLETED';
          break;
        case 'EXECUTION_COMPLETED':
          label = 'Workflow Runtime Completed';
          message = 'All execution steps finished successfully';
          status = 'COMPLETED';
          break;
        case 'RESPONSE_TOKEN':
          label = 'Token Emission';
          message = 'Streaming answer tokens';
          status = 'IN_PROGRESS';
          break;
        case 'COMPLETION':
          label = 'Generation Complete';
          message = `Finish reason: ${payload?.finishReason || 'stop'}`;
          status = 'COMPLETED';
          break;
        case 'ERROR':
          label = 'Execution Error';
          message = payload?.error || payload?.message || 'An error occurred during streaming';
          status = 'FAILED';
          break;
        case 'CONNECTION_CLOSED':
          label = 'Stream Closed';
          message = `Session closed (${payload?.status || 'done'})`;
          status = 'COMPLETED';
          break;
      }

      setTimelineItems((prev) => {
        const id = `${type}-${Date.now()}-${prev.length}`;
        return [
          ...prev,
          {
            id,
            eventType: type,
            phase: payload?.phase || type,
            message,
            timestamp: now,
            status,
            sequence: prev.length + 1,
            details: typeof payload === 'object' ? payload : { raw: payload },
          },
        ];
      });
    },
    []
  );

  const startStream = useCallback(
    (promptText: string, options: StreamChatOptions = {}) => {
      if (!promptText.trim() || isStreaming) return;

      setIsStreaming(true);
      setError(null);
      setCurrentPrompt(promptText);
      activePromptRef.current = promptText;
      setStreamedContent('');
      setTimelineItems([]);
      setToolExecutions([]);
      setCampusResult(null);

      let tokenAccumulator = '';

      const chatRequest = {
        conversationId: options.conversationId,
        prompt: promptText,
        model: options.model || 'gpt-4o-mini',
        temperature: options.temperature ?? 0.7,
        systemPrompt: options.systemPrompt,
      };

      const controller = atlasClient.streaming.streamChat(
        chatRequest,
        {
          onConnectionOpened: (data: any) => {
            addTimelineEvent('CONNECTION_OPENED', data);
          },
          onThinking: (data: any) => {
            addTimelineEvent('THINKING', data);
          },
          onReasoning: (data: any) => {
            addTimelineEvent('REASONING', data);
          },
          onPlanning: (data: any) => {
            addTimelineEvent('PLANNING', data);
          },
          onExecutionStarted: (data: any) => {
            addTimelineEvent('EXECUTION_STARTED', data);
          },
          onToolStarted: (data: any) => {
            addTimelineEvent('TOOL_STARTED', data);
            if (data?.toolName) {
              setToolExecutions((prev) => [
                ...prev,
                {
                  id: `tool-${Date.now()}-${prev.length}`,
                  toolName: data.toolName,
                  status: 'IN_PROGRESS',
                  startedAt: new Date().toLocaleTimeString(),
                },
              ]);
            }
          },
          onToolCompleted: (data: any) => {
            addTimelineEvent('TOOL_COMPLETED', data);
            if (data?.toolName) {
              setToolExecutions((prev) =>
                prev.map((t) =>
                  t.toolName === data.toolName
                    ? {
                        ...t,
                        status: 'SUCCESS',
                        completedAt: new Date().toLocaleTimeString(),
                        durationMs: data.durationMs || 320,
                        resultSummary: data.resultSummary || 'Executed cleanly',
                      }
                    : t
                )
              );
            }
            // Check for structured campus result payload
            if (data?.campusResult) {
              setCampusResult(data.campusResult);
            }
          },
          onExecutionCompleted: (data: any) => {
            addTimelineEvent('EXECUTION_COMPLETED', data);
            if (data?.campusResult) {
              setCampusResult(data.campusResult);
            }
          },
          onResponseToken: (data: any) => {
            if (data && data.token) {
              tokenAccumulator += data.token;
              setStreamedContent(tokenAccumulator);
            }
          },
          onCompletion: (data: any) => {
            addTimelineEvent('COMPLETION', data);
            if (data?.campusResult) {
              setCampusResult(data.campusResult);
            }
            setIsStreaming(false);

            // Invalidate conversation history cache if conversationId is provided
            if (options.conversationId) {
              queryClient.invalidateQueries({
                queryKey: queryKeys.conversations.history(options.conversationId),
              });
              queryClient.invalidateQueries({
                queryKey: queryKeys.conversations.all,
              });
            }
          },
          onError: (err: any) => {
            const errObj = err instanceof Error ? err : new Error(err?.message || 'Streaming error');
            setError(errObj);
            addTimelineEvent('ERROR', { error: errObj.message });
            setIsStreaming(false);
          },
          onConnectionClosed: (data: any) => {
            addTimelineEvent('CONNECTION_CLOSED', data);
            setIsStreaming(false);
          },
        },
        {
          maxRetries: 2,
          timeoutMs: 120000,
        }
      );

      streamControllerRef.current = controller;
    },
    [isStreaming, addTimelineEvent, queryClient]
  );

  const cancelStream = useCallback(() => {
    if (streamControllerRef.current) {
      streamControllerRef.current.cancel();
      streamControllerRef.current = null;
    }
    setIsStreaming(false);
    addTimelineEvent('CONNECTION_CLOSED', { status: 'user_cancelled' });
  }, [addTimelineEvent]);

  const retryStream = useCallback(
    (options: StreamChatOptions = {}) => {
      if (activePromptRef.current) {
        startStream(activePromptRef.current, options);
      }
    },
    [startStream]
  );

  return {
    isStreaming,
    currentPrompt,
    streamedContent,
    timelineItems,
    toolExecutions,
    campusResult,
    error,
    startStream,
    cancelStream,
    retryStream,
  };
}

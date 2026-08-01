import React, { memo } from 'react';
import {
  Brain,
  Cpu,
  GitBranch,
  Play,
  Wrench,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Sparkles,
  Loader2,
  Terminal,
} from 'lucide-react';
import { ThinkingTimelineItem, AtlasStreamEventType } from '../../../models/atlas.model';

interface ThinkingTimelineProps {
  items: ThinkingTimelineItem[];
  isStreaming: boolean;
}

export const ThinkingTimeline = memo(function ThinkingTimeline({ items, isStreaming }: ThinkingTimelineProps) {
  if (items.length === 0 && !isStreaming) {
    return (
      <div className="p-6 text-center text-gray-500 bg-gray-50/50 rounded-xl border border-dashed border-gray-200">
        <Sparkles className="w-8 h-8 mx-auto text-blue-400 mb-2 opacity-80" />
        <p className="text-xs font-semibold text-gray-700">Execution Pipeline Idle</p>
        <p className="text-[11px] text-gray-500 mt-1">
          Send a prompt to visualize Atlas reasoning, planning, tool executions, and stream lifecycle in real time.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4">
      <div className="flex items-center justify-between pb-3 border-b border-gray-100 mb-4">
        <div className="flex items-center gap-2">
          <Brain className="w-4 h-4 text-[#2563EB]" />
          <h3 className="font-semibold text-gray-900 text-xs uppercase tracking-wider">
            Thinking Timeline
          </h3>
        </div>
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold ${
            isStreaming
              ? 'bg-emerald-50 text-emerald-700 border border-emerald-200 animate-pulse'
              : 'bg-gray-100 text-gray-600'
          }`}
        >
          {isStreaming ? (
            <>
              <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-ping" />
              LIVE PIPELINE
            </>
          ) : (
            'IDLE'
          )}
        </span>
      </div>

      <div className="relative pl-4 space-y-4 before:absolute before:left-2 before:top-2 before:bottom-2 before:w-0.5 before:bg-gray-200">
        {items.map((item, idx) => (
          <TimelineNode key={item.id || idx} item={item} isLast={idx === items.length - 1} />
        ))}

        {isStreaming && (
          <div className="relative flex items-start gap-3 pt-1">
            <div className="absolute -left-4 top-0.5 w-4 h-4 rounded-full bg-blue-100 border border-blue-400 flex items-center justify-center">
              <Loader2 className="w-2.5 h-2.5 text-blue-600 animate-spin" />
            </div>
            <div className="flex-1 pl-2">
              <p className="text-xs font-semibold text-blue-700 animate-pulse">
                Processing Stream Tokens...
              </p>
              <p className="text-[11px] text-gray-500">Formulating final response</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
});

const TimelineNode = memo(function TimelineNode({ item }: { item: ThinkingTimelineItem; isLast?: boolean }) {
  const getIcon = (type: AtlasStreamEventType) => {
    switch (type) {
      case 'CONNECTION_OPENED':
        return <Terminal className="w-3 h-3 text-blue-600" />;
      case 'THINKING':
        return <Brain className="w-3 h-3 text-purple-600" />;
      case 'REASONING':
        return <Cpu className="w-3 h-3 text-indigo-600" />;
      case 'PLANNING':
        return <GitBranch className="w-3 h-3 text-amber-600" />;
      case 'EXECUTION_STARTED':
        return <Play className="w-3 h-3 text-emerald-600" />;
      case 'TOOL_STARTED':
      case 'TOOL_COMPLETED':
        return <Wrench className="w-3 h-3 text-teal-600" />;
      case 'EXECUTION_COMPLETED':
      case 'COMPLETION':
        return <CheckCircle2 className="w-3 h-3 text-emerald-600" />;
      case 'ERROR':
        return <AlertTriangle className="w-3 h-3 text-red-600" />;
      default:
        return <Clock className="w-3 h-3 text-gray-600" />;
    }
  };

  return (
    <div className="relative flex items-start gap-3 group">
      {/* Node Dot */}
      <div
        className={`absolute -left-4 top-0.5 w-4 h-4 rounded-full bg-white border flex items-center justify-center shadow-xs transition-colors ${
          item.status === 'IN_PROGRESS'
            ? 'border-blue-500 ring-2 ring-blue-100'
            : item.status === 'FAILED'
            ? 'border-red-500'
            : 'border-gray-300'
        }`}
      >
        {getIcon(item.eventType)}
      </div>

      <div className="flex-1 pl-2">
        <div className="flex items-center justify-between gap-2">
          <span className="text-xs font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
            {item.phase || item.eventType}
          </span>
          <span className="text-[10px] text-gray-400 font-mono">{item.timestamp}</span>
        </div>

        <p className="text-xs text-gray-600 mt-0.5">{item.message}</p>

        {item.details && (item.details.executionId || item.details.toolName) && (
          <div className="mt-1 flex items-center gap-2 flex-wrap text-[10px]">
            {item.details.executionId && (
              <span className="px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded font-mono">
                exec: {item.details.executionId}
              </span>
            )}
            {item.details.toolName && (
              <span className="px-1.5 py-0.5 bg-teal-50 text-teal-700 rounded font-mono">
                tool: {item.details.toolName}
              </span>
            )}
          </div>
        )}
      </div>
    </div>
  );
});

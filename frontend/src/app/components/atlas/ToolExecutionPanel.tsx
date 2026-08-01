import React, { memo } from 'react';
import { Wrench, CheckCircle, XCircle, Clock, ShieldCheck, Loader2 } from 'lucide-react';
import { ToolExecutionItem } from '../../../models/atlas.model';

interface ToolExecutionPanelProps {
  tools: ToolExecutionItem[];
  isStreaming: boolean;
}

export const ToolExecutionPanel = memo(function ToolExecutionPanel({ tools }: ToolExecutionPanelProps) {
  if (tools.length === 0) {
    return null;
  }

  return (
    <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 mt-4">
      <div className="flex items-center justify-between pb-3 border-b border-gray-100 mb-3">
        <div className="flex items-center gap-2">
          <Wrench className="w-4 h-4 text-teal-600" />
          <h3 className="font-semibold text-gray-900 text-xs uppercase tracking-wider">
            Tool Executions ({tools.length})
          </h3>
        </div>
        <div className="flex items-center gap-1 text-[10px] text-gray-500 font-medium bg-gray-50 px-2 py-0.5 rounded border">
          <ShieldCheck className="w-3 h-3 text-emerald-600" />
          <span>Privacy Guaranteed</span>
        </div>
      </div>

      <div className="space-y-2">
        {tools.map((tool) => (
          <div
            key={tool.id}
            className="p-3 bg-gray-50/70 rounded-lg border border-gray-200/60 flex items-center justify-between gap-3 text-xs"
          >
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="p-1.5 bg-white rounded border border-gray-200 shadow-2xs">
                {tool.status === 'IN_PROGRESS' ? (
                  <Loader2 className="w-3.5 h-3.5 text-blue-600 animate-spin" />
                ) : tool.status === 'SUCCESS' ? (
                  <CheckCircle className="w-3.5 h-3.5 text-emerald-600" />
                ) : (
                  <XCircle className="w-3.5 h-3.5 text-red-600" />
                )}
              </div>
              <div className="truncate">
                <span className="font-semibold text-gray-900 font-mono text-xs block truncate">
                  {tool.toolName}
                </span>
                <span className="text-[10px] text-gray-500 block truncate">
                  {tool.resultSummary || (tool.status === 'IN_PROGRESS' ? 'Running capabilities...' : 'Completed')}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-2 flex-shrink-0 text-[11px]">
              {tool.durationMs !== undefined && (
                <span className="inline-flex items-center gap-1 text-gray-500 font-mono text-[10px] bg-white px-1.5 py-0.5 rounded border">
                  <Clock className="w-3 h-3" />
                  {tool.durationMs}ms
                </span>
              )}
              <span
                className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wide border ${
                  tool.status === 'IN_PROGRESS'
                    ? 'bg-blue-50 text-blue-700 border-blue-200 animate-pulse'
                    : tool.status === 'SUCCESS'
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                    : 'bg-red-50 text-red-700 border-red-200'
                }`}
              >
                {tool.status}
              </span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
});

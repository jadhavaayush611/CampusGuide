import React from 'react';
import { X, Sparkles, Shield, Cpu, Activity, CheckCircle2, Zap } from 'lucide-react';
import { useAtlasCapabilities } from '../../../hooks/atlas/useAtlasCapabilities';

interface AtlasCapabilitiesModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function AtlasCapabilitiesModal({ isOpen, onClose }: AtlasCapabilitiesModalProps) {
  const { data: capabilities, isLoading, error } = useAtlasCapabilities();

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-xs">
      <div className="bg-white rounded-2xl border border-gray-200 shadow-2xl w-full max-w-2xl overflow-hidden animate-in fade-in zoom-in duration-150">
        {/* Header */}
        <div className="p-5 border-b border-gray-200 bg-gray-50 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-blue-100 text-[#2563EB] rounded-xl">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-gray-900 text-base">Atlas Orchestrator Capabilities</h3>
              <p className="text-xs text-gray-500">
                Operational status & registered capabilities architecture
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-gray-400 hover:text-gray-700 hover:bg-gray-200/60 rounded-xl transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 max-h-[80vh] overflow-y-auto">
          {isLoading ? (
            <div className="py-8 text-center text-gray-500 text-sm">
              Loading capabilities...
            </div>
          ) : error ? (
            <div className="p-4 bg-red-50 text-red-700 rounded-xl border border-red-200 text-xs">
              Failed to load capabilities info: {error.message}
            </div>
          ) : (
            <>
              {/* Status & Version */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                <div className="p-3 bg-blue-50/50 rounded-xl border border-blue-100 text-center">
                  <span className="text-[10px] uppercase font-bold text-blue-600 tracking-wider">Status</span>
                  <p className="text-xs font-extrabold text-blue-900 mt-1 flex items-center justify-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                    {capabilities?.status || 'OPERATIONAL'}
                  </p>
                </div>
                <div className="p-3 bg-purple-50/50 rounded-xl border border-purple-100 text-center">
                  <span className="text-[10px] uppercase font-bold text-purple-600 tracking-wider">Engine Version</span>
                  <p className="text-xs font-extrabold text-purple-900 mt-1">
                    v{capabilities?.atlasVersion || '1.0.0'} ({capabilities?.apiVersion || 'v1'})
                  </p>
                </div>
                <div className="p-3 bg-emerald-50/50 rounded-xl border border-emerald-100 text-center">
                  <span className="text-[10px] uppercase font-bold text-emerald-600 tracking-wider">Provider</span>
                  <p className="text-xs font-extrabold text-emerald-900 mt-1 truncate">
                    {capabilities?.provider || 'OpenAI Resilient'}
                  </p>
                </div>
                <div className="p-3 bg-amber-50/50 rounded-xl border border-amber-100 text-center">
                  <span className="text-[10px] uppercase font-bold text-amber-600 tracking-wider">Rate Limit</span>
                  <p className="text-xs font-extrabold text-amber-900 mt-1">
                    {capabilities?.limits?.rateLimitPerMinute || 60} rpm
                  </p>
                </div>
              </div>

              {/* Registered Capabilities */}
              <div>
                <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                  <Zap className="w-4 h-4 text-blue-600" />
                  Registered Subsystem Capabilities
                </h4>
                <div className="flex flex-wrap gap-2">
                  {(capabilities?.registeredCapabilities || [
                    'PROVIDER_AGNOSTIC_CHAT',
                    'CONTEXT_INTELLIGENCE',
                    'HYBRID_RAG',
                    'KNOWLEDGE_GRAPH_REASONING',
                    'DECISION_INTELLIGENCE',
                    'WORKFLOW_ORCHESTRATION',
                  ]).map((cap) => (
                    <span
                      key={cap}
                      className="px-2.5 py-1 bg-gray-100 text-gray-800 font-mono text-[11px] rounded-lg border border-gray-200 font-medium"
                    >
                      {cap}
                    </span>
                  ))}
                </div>
              </div>

              {/* Available Workflows */}
              <div>
                <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider mb-2.5 flex items-center gap-1.5">
                  <Cpu className="w-4 h-4 text-purple-600" />
                  Available Execution Workflows
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {(capabilities?.availableWorkflows || [
                    'academic_advising_workflow',
                    'course_recommendation_workflow',
                    'campus_navigation_workflow',
                    'default_workflow',
                  ]).map((wf) => (
                    <div
                      key={wf}
                      className="p-2.5 bg-gray-50 rounded-xl border border-gray-200 text-xs font-mono text-gray-700 flex items-center gap-2"
                    >
                      <span className="w-2 h-2 rounded-full bg-purple-500" />
                      <span>{wf}</span>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 bg-gray-50 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-900 hover:bg-gray-800 text-white font-semibold text-xs rounded-xl transition-colors"
          >
            Close Window
          </button>
        </div>
      </div>
    </div>
  );
}

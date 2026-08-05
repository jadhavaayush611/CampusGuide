import React, { useState, useRef, useEffect } from 'react';
import { Send, Square, Sparkles, SlidersHorizontal, CornerDownLeft } from 'lucide-react';

interface MessageComposerProps {
  onSend: (prompt: string, model: string) => void;
  onCancel: () => void;
  isStreaming: boolean;
  disabled?: boolean;
}

export function MessageComposer({
  onSend,
  onCancel,
  isStreaming,
  disabled = false,
}: MessageComposerProps) {
  const [prompt, setPrompt] = useState('');
  const [selectedModel, setSelectedModel] = useState('gpt-4o-mini');
  const [showOptions, setShowOptions] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
      textareaRef.current.style.height = `${Math.min(textareaRef.current.scrollHeight, 180)}px`;
    }
  }, [prompt]);

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!prompt.trim() || isStreaming || disabled) return;

    onSend(prompt.trim(), selectedModel);
    setPrompt('');
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto';
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && (e.metaKey || e.ctrlKey || !e.shiftKey)) {
      if (e.shiftKey) return; // Allow Shift+Enter for newlines
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <div className="bg-white border-t border-gray-200 p-4">
      <form onSubmit={handleSubmit} className="relative bg-gray-50 rounded-2xl border border-gray-200 focus-within:border-[#2563EB] focus-within:ring-2 focus-within:ring-[#2563EB]/20 transition-all p-3">
        <textarea
          ref={textareaRef}
          id="chat-composer-prompt"
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask Atlas to orchestrate your campus schedule, degree audit, notices, or research..."
          disabled={isStreaming || disabled}
          rows={1}
          className="w-full bg-transparent text-sm text-gray-900 placeholder-gray-400 focus:outline-none resize-none min-h-[42px] max-h-[180px] leading-relaxed"
          aria-label="Atlas prompt message"
        />

        <div className="flex items-center justify-between pt-2 border-t border-gray-200/60 mt-1">
          <div className="flex items-center gap-2">
            <div className="relative">
              <button
                type="button"
                onClick={() => setShowOptions(!showOptions)}
                className="flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white rounded-lg border border-gray-200 hover:bg-gray-50 transition-colors shadow-2xs"
                aria-label="Select Atlas engine"
                aria-haspopup="listbox"
                aria-expanded={showOptions}
              >
                <Sparkles className="w-3.5 h-3.5 text-blue-600" />
                <span>{selectedModel}</span>
                <SlidersHorizontal className="w-3 h-3 text-gray-400" />
              </button>

              {showOptions && (
                <>
                  <div
                    className="fixed inset-0 z-10"
                    onClick={() => setShowOptions(false)}
                  />
                  <div className="absolute left-0 bottom-full mb-2 w-48 bg-white rounded-xl border border-gray-200 shadow-lg z-20 py-1 text-xs" role="listbox" aria-label="Atlas engines">
                    <div className="px-3 py-1.5 font-semibold text-gray-400 border-b border-gray-100 text-[10px] uppercase">
                      Select Atlas Engine
                    </div>
                    {['gpt-4o-mini', 'gpt-4o', 'mock-model'].map((model) => (
                      <button
                        key={model}
                        type="button"
                        onClick={() => {
                          setSelectedModel(model);
                          setShowOptions(false);
                        }}
                        role="option"
                        aria-selected={selectedModel === model}
                        className={`w-full text-left px-3 py-2 hover:bg-gray-50 flex items-center justify-between ${
                          selectedModel === model ? 'font-bold text-[#2563EB]' : 'text-gray-700'
                        }`}
                      >
                        <span>{model}</span>
                        {selectedModel === model && <span className="w-1.5 h-1.5 rounded-full bg-[#2563EB]" />}
                      </button>
                    ))}
                  </div>
                </>
              )}
            </div>
            <span className="text-[11px] text-gray-400 hidden sm:inline-flex items-center gap-1">
              <CornerDownLeft className="w-3 h-3" /> Press Enter to send
            </span>
          </div>

          <div className="flex items-center gap-2">
            {isStreaming ? (
              <button
                type="button"
                onClick={onCancel}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-red-50 text-red-600 hover:bg-red-100 font-semibold text-xs rounded-xl transition-colors border border-red-200 shadow-xs"
              >
                <Square className="w-3.5 h-3.5 fill-red-600" />
                <span>Stop</span>
              </button>
            ) : (
              <button
                type="submit"
                disabled={!prompt.trim() || disabled}
                className="flex items-center gap-1.5 px-4 py-2 bg-[#2563EB] hover:bg-blue-700 disabled:opacity-50 disabled:hover:bg-[#2563EB] text-white font-semibold text-xs rounded-xl transition-all shadow-sm"
              >
                <span>Send</span>
                <Send className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        </div>
      </form>
    </div>
  );
}

import React, { useState } from 'react';
import { Check, Copy } from 'lucide-react';

interface MarkdownRendererProps {
  content: string;
}

export function MarkdownRenderer({ content }: MarkdownRendererProps) {
  if (!content) return null;

  // Simple parser to separate code blocks from standard markdown
  const parts = parseMarkdownBlocks(content);

  return (
    <div className="space-y-2 text-sm leading-relaxed text-gray-800">
      {parts.map((part, idx) => {
        if (part.type === 'code') {
          return <CodeBlock key={idx} language={part.language} code={part.content} />;
        }
        return <FormattedText key={idx} text={part.content} />;
      })}
    </div>
  );
}

interface BlockPart {
  type: 'text' | 'code';
  language?: string;
  content: string;
}

function parseMarkdownBlocks(text: string): BlockPart[] {
  const parts: BlockPart[] = [];
  const codeBlockRegex = /```([a-zA-Z0-9_-]*)\n([\s\S]*?)```/g;
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = codeBlockRegex.exec(text)) !== null) {
    if (match.index > lastIndex) {
      parts.push({
        type: 'text',
        content: text.substring(lastIndex, match.index),
      });
    }
    parts.push({
      type: 'code',
      language: match[1] || 'plaintext',
      content: match[2].trim(),
    });
    lastIndex = match.index + match[0].length;
  }

  if (lastIndex < text.length) {
    parts.push({
      type: 'text',
      content: text.substring(lastIndex),
    });
  }

  return parts;
}

function CodeBlock({ language, code }: { language?: string; code: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="my-3 rounded-lg overflow-hidden border border-gray-800 bg-[#1e1e2e] text-gray-100 font-mono text-xs shadow-md">
      <div className="flex items-center justify-between px-3 py-1.5 bg-[#181825] border-b border-gray-800/80">
        <span className="text-[11px] font-semibold text-gray-400 uppercase tracking-wider">
          {language || 'code'}
        </span>
        <button
          onClick={handleCopy}
          className="flex items-center gap-1 text-[11px] text-gray-400 hover:text-white transition-colors"
          title="Copy code"
        >
          {copied ? (
            <>
              <Check className="w-3.5 h-3.5 text-emerald-400" />
              <span className="text-emerald-400">Copied</span>
            </>
          ) : (
            <>
              <Copy className="w-3.5 h-3.5" />
              <span>Copy</span>
            </>
          )}
        </button>
      </div>
      <pre className="p-3 overflow-x-auto text-xs leading-relaxed whitespace-pre font-mono text-emerald-300">
        <code>{code}</code>
      </pre>
    </div>
  );
}

function FormattedText({ text }: { text: string }) {
  const paragraphs = text.split('\n\n');

  return (
    <>
      {paragraphs.map((paragraph, pIdx) => {
        if (!paragraph.trim()) return null;

        // Check headers
        if (paragraph.startsWith('### ')) {
          return (
            <h3 key={pIdx} className="text-base font-semibold text-gray-900 mt-3 mb-1">
              {renderInline(paragraph.replace('### ', ''))}
            </h3>
          );
        }
        if (paragraph.startsWith('## ')) {
          return (
            <h2 key={pIdx} className="text-lg font-bold text-gray-900 mt-4 mb-1">
              {renderInline(paragraph.replace('## ', ''))}
            </h2>
          );
        }
        if (paragraph.startsWith('# ')) {
          return (
            <h1 key={pIdx} className="text-xl font-extrabold text-gray-900 mt-4 mb-2">
              {renderInline(paragraph.replace('# ', ''))}
            </h1>
          );
        }

        // Bullet lists
        if (paragraph.startsWith('- ') || paragraph.startsWith('* ')) {
          const items = paragraph.split('\n');
          return (
            <ul key={pIdx} className="list-disc list-inside space-y-1 my-2 text-gray-700 pl-1">
              {items.map((item, itemIdx) => (
                <li key={itemIdx}>
                  {renderInline(item.replace(/^[-*]\s+/, ''))}
                </li>
              ))}
            </ul>
          );
        }

        // Numbered lists
        if (/^\d+\.\s/.test(paragraph)) {
          const items = paragraph.split('\n');
          return (
            <ol key={pIdx} className="list-decimal list-inside space-y-1 my-2 text-gray-700 pl-1">
              {items.map((item, itemIdx) => (
                <li key={itemIdx}>
                  {renderInline(item.replace(/^\d+\.\s+/, ''))}
                </li>
              ))}
            </ol>
          );
        }

        return (
          <p key={pIdx} className="my-1.5 text-gray-700">
            {renderInline(paragraph)}
          </p>
        );
      })}
    </>
  );
}

function renderInline(text: string): React.ReactNode {
  // Bold regex `**text**`
  const parts = text.split(/(\*\*.*?\*\*|`.*?`)/g);

  return parts.map((part, idx) => {
    if (part.startsWith('**') && part.endsWith('**')) {
      return (
        <strong key={idx} className="font-semibold text-gray-900">
          {part.slice(2, -2)}
        </strong>
      );
    }
    if (part.startsWith('`') && part.endsWith('`')) {
      return (
        <code
          key={idx}
          className="px-1.5 py-0.5 bg-gray-100 border border-gray-200 text-pink-600 font-mono text-xs rounded"
        >
          {part.slice(1, -1)}
        </code>
      );
    }
    return part;
  });
}

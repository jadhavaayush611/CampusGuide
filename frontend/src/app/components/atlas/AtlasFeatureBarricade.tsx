import React from 'react';
import { Sparkles } from 'lucide-react';

interface AtlasFeatureBarricadeProps {
  children: React.ReactNode;
  /** Whether Atlas is currently disabled (showing the barricade) */
  disabled?: boolean;
  /** Optional message to display below the main "Coming Soon" text */
  customMessage?: string;
}

/**
 * AtlasFeatureBarricade
 *
 * A feature gate component that displays a "Coming Soon" overlay when Atlas is disabled.
 * When enabled, it renders its children normally.
 *
 * When disabled:
 * - Displays a visual barricade overlay
 * - Dims/blur the underlying interface
 * - Prevents interaction with all underlying content
 * - Provides accessible labeling
 * - Works responsively on desktop, tablet, and mobile
 */
export function AtlasFeatureBarricade({
  children,
  disabled = false,
  customMessage,
}: AtlasFeatureBarricadeProps) {
  if (!disabled) {
    return <>{children}</>;
  }

  return (
    <div className="relative group">
      {/* The underlying Atlas interface content */}
      <div aria-hidden="true">{children}</div>

      {/* Barricade Overlay - covers the entire component */}
      <div
        role="presentation"
        aria-label="Atlas AI feature temporarily unavailable - Coming Soon"
        className="absolute inset-0 z-50 flex flex-col items-center justify-center rounded-2xl bg-white/80 backdrop-blur-[2px] dark:bg-background/80 transition-all duration-300"
      >
        {/* Barrier visual - subtle gradient and border */}
        <div className="absolute inset-0 border-4 border-cyan-100/50 dark:border-cyan-900/30 rounded-2xl pointer-events-none" />

        {/* Centered content */}
        <div className="relative z-10 flex flex-col items-center justify-center p-8 text-center max-w-md mx-4">
          {/* Icon */}
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white mb-6 shadow-lg">
            <Sparkles className="w-8 h-8" />
          </div>

          {/* Main heading */}
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Atlas AI
          </h2>

          {/* "Coming Soon" badge */}
          <span className="inline-flex items-center px-3 py-1 rounded-full bg-cyan-100 dark:bg-cyan-900/30 text-cyan-700 dark:text-cyan-300 text-xs font-bold uppercase tracking-wider mb-4">
            <span className="w-2 h-2 rounded-full bg-cyan-500 animate-pulse mr-2" />
            Coming Soon
          </span>

          {/* Primary message */}
          <p className="text-sm text-gray-700 dark:text-gray-300 font-medium mb-3">
            Atlas is temporarily unavailable while we complete the MVP.
          </p>

          {/* Secondary message */}
          <p className="text-xs text-gray-500 dark:text-gray-400 leading-relaxed">
            This feature is currently under development and will be available in a future update.
          </p>

          {/* Custom message (if provided) */}
          {customMessage && (
            <p className="text-xs text-gray-400 dark:text-gray-500 mt-4 italic">
              {customMessage}
            </p>
          )}
        </div>

        {/* Disabled indicator - subtle bottom badge */}
        <div className="absolute bottom-3 right-3 opacity-50 pointer-events-none">
          <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">
            Feature Disabled
          </span>
        </div>
      </div>
    </div>
  );
}


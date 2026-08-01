import React from 'react';
import { Compass } from 'lucide-react';

export function PageLoadingFallback() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] w-full p-8 text-center animate-in fade-in duration-300">
      <div className="relative flex items-center justify-center mb-4">
        <div className="absolute inset-0 rounded-full bg-blue-500/20 animate-ping duration-1000" />
        <div className="relative p-4 bg-white rounded-2xl shadow-sm border border-slate-100">
          <Compass className="w-8 h-8 text-blue-600 animate-spin duration-3000" />
        </div>
      </div>
      <h3 className="text-sm font-semibold text-slate-700 tracking-wide uppercase">
        Loading CampusGuide
      </h3>
      <p className="text-xs text-slate-400 mt-1">
        Fetching view & content...
      </p>
    </div>
  );
}

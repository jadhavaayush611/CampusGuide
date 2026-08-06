import React, { useState, useEffect, useRef } from 'react';
import { WifiOff, Wifi, AlertTriangle } from 'lucide-react';
import { useOnlineStatus } from '../../hooks/common/useOnlineStatus';

export const OfflineBanner: React.FC = () => {
  const isOnline = useOnlineStatus();
  const [showBanner, setShowBanner] = useState(false);
  const [status, setStatus] = useState<'offline' | 'reconnected' | 'idle'>('idle');
  
  // Track previous online state to detect reconnection
  const prevOnlineRef = useRef(isOnline);

  useEffect(() => {
    // If transitioning from online to offline
    if (!isOnline && prevOnlineRef.current) {
      setStatus('offline');
      setShowBanner(true);
    }
    // If transitioning from offline to online
    else if (isOnline && !prevOnlineRef.current) {
      setStatus('reconnected');
      setShowBanner(true);
      const timer = setTimeout(() => {
        setShowBanner(false);
        setStatus('idle');
      }, 4000);
      return () => clearTimeout(timer);
    }

    prevOnlineRef.current = isOnline;
  }, [isOnline]);

  if (!showBanner && isOnline) {
    return null;
  }

  const isOfflineState = !isOnline || status === 'offline';

  return (
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999] w-[90%] max-w-md animate-in slide-in-from-bottom-5 duration-300">
      {isOfflineState ? (
        <div className="flex items-center gap-3 p-4 rounded-2xl border border-amber-500/30 bg-slate-900/90 text-slate-100 backdrop-blur-md shadow-xl">
          <div className="flex-shrink-0 w-8 h-8 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center animate-pulse">
            <WifiOff className="w-4 h-4" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white">Connection Interrupted</p>
            <p className="text-xs text-slate-400 mt-0.5">
              You are currently offline. Actions and modifications are restricted.
            </p>
          </div>
          <div className="flex-shrink-0 text-xs font-semibold px-2 py-1 rounded-md bg-amber-500/10 text-amber-400 border border-amber-500/20">
            Offline Mode
          </div>
        </div>
      ) : (
        <div className="flex items-center gap-3 p-4 rounded-2xl border border-emerald-500/30 bg-slate-900/90 text-slate-100 backdrop-blur-md shadow-xl animate-out fade-out duration-1000 delay-3000 fill-mode-forward">
          <div className="flex-shrink-0 w-8 h-8 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center">
            <Wifi className="w-4 h-4" />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white">Connection Restored</p>
            <p className="text-xs text-slate-400 mt-0.5">
              Back online. Synchronizing data and resuming pending requests.
            </p>
          </div>
          <div className="flex-shrink-0 text-xs font-semibold px-2 py-1 rounded-md bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            Online
          </div>
        </div>
      )}
    </div>
  );
};

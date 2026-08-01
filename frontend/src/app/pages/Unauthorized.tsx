import React from 'react';
import { useNavigate, useLocation } from 'react-router';
import { ShieldAlert, Home, ArrowLeft } from 'lucide-react';
import { useAuth } from '../../core/auth';

export function Unauthorized() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  const fromPath = (location.state as any)?.from?.pathname || 'restricted resource';

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
      <div className="bg-white rounded-3xl p-8 md:p-12 border border-gray-200 shadow-xl text-center max-w-lg w-full space-y-6 animate-fadeIn">
        {/* Shield Icon Badge */}
        <div className="w-20 h-20 rounded-3xl bg-red-50 border border-red-200 text-red-600 flex items-center justify-center mx-auto shadow-inner">
          <ShieldAlert className="w-10 h-10" />
        </div>

        {/* Messaging */}
        <div className="space-y-2">
          <span className="px-3 py-1 bg-red-100 text-red-700 rounded-full text-xs font-extrabold uppercase tracking-wider">
            403 — Access Denied
          </span>
          <h1 className="text-2xl md:text-3xl font-extrabold text-gray-900 pt-2">
            Unauthorized Access
          </h1>
          <p className="text-xs md:text-sm text-gray-600 leading-relaxed max-w-md mx-auto">
            You do not have the required permissions to access <span className="font-semibold text-gray-800">{fromPath}</span>.
            {user?.role && (
              <span className="block mt-1 text-xs text-gray-500">
                Current Role: <span className="font-semibold capitalize text-gray-700">{user.role}</span>
              </span>
            )}
          </p>
        </div>

        {/* Actions: Dashboard button & Back button */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
          <button
            onClick={() => navigate(-1)}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-bold transition-all"
          >
            <ArrowLeft className="w-4 h-4 text-gray-600" />
            <span>Go Back</span>
          </button>

          <button
            onClick={() => navigate('/')}
            className="w-full sm:w-auto inline-flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-md shadow-blue-200 transition-all"
          >
            <Home className="w-4 h-4" />
            <span>Dashboard</span>
          </button>
        </div>
      </div>
    </div>
  );
}

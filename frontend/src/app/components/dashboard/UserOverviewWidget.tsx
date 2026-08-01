import React, { memo, useCallback, useMemo } from 'react';
import { useCurrentUser } from '../../../hooks/auth/useCurrentUser';
import { useAuth } from '../../../core/auth';
import { ShieldCheck, Building2, Mail, Award, CheckCircle2, AlertCircle } from 'lucide-react';
import { useNavigate } from 'react-router';

export const UserOverviewWidget: React.FC = memo(function UserOverviewWidget() {
  const navigate = useNavigate();
  const { user: authUser } = useAuth();
  const { data: user, isLoading, isError, error, refetch } = useCurrentUser();

  // Combine query data with fallback from auth context if needed
  const currentUser = user || authUser;

  const handleNavigateProfile = useCallback(() => {
    navigate('/profile');
  }, [navigate]);

  const handleNavigateLogin = useCallback(() => {
    navigate('/login');
  }, [navigate]);

  const handleRefetch = useCallback(() => {
    refetch();
  }, [refetch]);

  // Calculate profile completion percentage based on available fields
  const completionPercentage = useMemo(() => {
    if (!currentUser) return 0;
    const fields = [
      Boolean(currentUser.name),
      Boolean(currentUser.email),
      Boolean(currentUser.role),
      Boolean(currentUser.department),
      Boolean(currentUser.studentId),
      Boolean(currentUser.phone),
      Boolean(currentUser.bio),
    ];
    const filledCount = fields.filter(Boolean).length;
    return Math.round((filledCount / fields.length) * 100);
  }, [currentUser]);

  if (isLoading && !currentUser) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm animate-pulse space-y-4">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-gray-200 rounded-full"></div>
          <div className="space-y-2 flex-1">
            <div className="h-5 bg-gray-200 rounded w-1/3"></div>
            <div className="h-4 bg-gray-200 rounded w-1/2"></div>
          </div>
        </div>
        <div className="h-3 bg-gray-200 rounded w-full"></div>
      </div>
    );
  }

  if (isError && !currentUser) {
    return (
      <div className="bg-amber-50 border border-amber-200 rounded-2xl p-5 text-amber-900 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <AlertCircle className="w-5 h-5 text-amber-600 flex-shrink-0" />
          <p className="text-sm">Could not load profile details: {error?.message}</p>
        </div>
        <button
          onClick={handleRefetch}
          className="text-xs bg-white px-3 py-1.5 rounded-lg border border-amber-300 font-medium hover:bg-amber-100"
        >
          Retry
        </button>
      </div>
    );
  }

  if (!currentUser) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm text-center">
        <p className="text-sm text-gray-600">No authenticated user found.</p>
        <button
          onClick={handleNavigateLogin}
          className="mt-3 px-4 py-2 bg-blue-600 text-white text-xs font-semibold rounded-lg hover:bg-blue-700"
        >
          Sign In
        </button>
      </div>
    );
  }

  const initial = (currentUser.name?.[0] || currentUser.email?.[0] || 'U').toUpperCase();

  return (
    <div className="bg-gradient-to-br from-white via-slate-50/50 to-blue-50/30 rounded-2xl p-6 border border-gray-200/80 shadow-md backdrop-blur-sm relative overflow-hidden transition-all hover:shadow-lg">
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
        <div className="flex items-center gap-5">
          <div className="relative">
            {currentUser.avatarUrl ? (
              <img
                src={currentUser.avatarUrl}
                alt={currentUser.name}
                className="w-16 h-16 rounded-2xl object-cover ring-4 ring-blue-100 shadow-md"
              />
            ) : (
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center text-white text-2xl font-bold ring-4 ring-blue-100 shadow-md">
                {initial}
              </div>
            )}
            <div className="absolute -bottom-1 -right-1 bg-green-500 w-4 h-4 rounded-full border-2 border-white" title="Active Session"></div>
          </div>

          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h2 className="text-xl font-bold text-gray-900">{currentUser.name || 'Campus Student'}</h2>
              <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200 flex items-center gap-1">
                <ShieldCheck className="w-3 h-3" />
                {currentUser.role || 'STUDENT'}
              </span>
            </div>

            <div className="flex flex-wrap items-center gap-4 mt-2 text-xs text-gray-600">
              <div className="flex items-center gap-1.5">
                <Mail className="w-3.5 h-3.5 text-gray-400" />
                <span>{currentUser.email}</span>
              </div>
              {currentUser.department && (
                <div className="flex items-center gap-1.5">
                  <Building2 className="w-3.5 h-3.5 text-gray-400" />
                  <span>{currentUser.department}</span>
                </div>
              )}
              {currentUser.studentId && (
                <div className="flex items-center gap-1.5">
                  <Award className="w-3.5 h-3.5 text-gray-400" />
                  <span>ID: {currentUser.studentId}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Profile Completion Indicator */}
        <div className="w-full md:w-56 bg-white/80 border border-gray-200/60 rounded-xl p-3 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-xs mb-1.5">
            <span className="font-semibold text-gray-700 flex items-center gap-1">
              <CheckCircle2 className="w-3.5 h-3.5 text-blue-600" />
              Profile Status
            </span>
            <span className="font-bold text-blue-700">{completionPercentage}%</span>
          </div>
          <div className="w-full bg-gray-100 rounded-full h-2 overflow-hidden mb-2">
            <div
              className="bg-gradient-to-r from-blue-500 to-indigo-600 h-2 rounded-full transition-all duration-500"
              style={{ width: `${completionPercentage}%` }}
            ></div>
          </div>
          <button
            onClick={handleNavigateProfile}
            className="text-[11px] text-blue-600 hover:text-blue-800 font-medium text-right hover:underline"
          >
            Edit Profile →
          </button>
        </div>
      </div>
    </div>
  );
});

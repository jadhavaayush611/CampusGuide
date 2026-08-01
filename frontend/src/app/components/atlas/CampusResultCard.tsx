import React, { memo, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router';
import {
  MapPin,
  Building,
  DoorOpen,
  Users,
  Shield,
  Calendar,
  BookOpen,
  ClipboardList,
  GraduationCap,
  ChevronRight,
} from 'lucide-react';
import { CampusResult } from '../../../models/atlas.model';

interface CampusResultCardProps {
  result: CampusResult;
}

export const CampusResultCard = memo(function CampusResultCard({ result }: CampusResultCardProps) {
  const navigate = useNavigate();

  const icon = useMemo(() => {
    switch (result.type) {
      case 'navigation':
      case 'building':
        return <Building className="w-5 h-5 text-blue-600" />;
      case 'room':
        return <DoorOpen className="w-5 h-5 text-indigo-600" />;
      case 'community':
        return <Users className="w-5 h-5 text-emerald-600" />;
      case 'council':
        return <Shield className="w-5 h-5 text-purple-600" />;
      case 'planner':
        return <Calendar className="w-5 h-5 text-amber-600" />;
      case 'resource':
        return <BookOpen className="w-5 h-5 text-teal-600" />;
      case 'notice':
        return <ClipboardList className="w-5 h-5 text-rose-600" />;
      case 'academic':
        return <GraduationCap className="w-5 h-5 text-sky-600" />;
      default:
        return <MapPin className="w-5 h-5 text-blue-600" />;
    }
  }, [result.type]);

  const badgeStyle = useMemo(() => {
    switch (result.type) {
      case 'navigation':
      case 'building':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'room':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200';
      case 'community':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'council':
        return 'bg-purple-50 text-purple-700 border-purple-200';
      case 'planner':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'resource':
        return 'bg-teal-50 text-teal-700 border-teal-200';
      case 'notice':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'academic':
        return 'bg-sky-50 text-sky-700 border-sky-200';
      default:
        return 'bg-gray-50 text-gray-700 border-gray-200';
    }
  }, [result.type]);

  const handleNavigate = useCallback(() => {
    if (result.deepLink) {
      navigate(result.deepLink);
    }
  }, [navigate, result.deepLink]);

  return (
    <div className="mt-3 p-4 bg-white rounded-xl border border-gray-200/80 shadow-sm hover:shadow-md transition-all duration-200">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-3">
          <div className="p-2.5 bg-gray-50 rounded-lg border border-gray-100 flex-shrink-0">
            {icon}
          </div>
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h4 className="font-semibold text-gray-900 text-sm">{result.title}</h4>
              <span
                className={`text-[10px] font-semibold px-2 py-0.5 rounded-full border uppercase tracking-wider ${badgeStyle}`}
              >
                {result.type}
              </span>
            </div>
            {result.subtitle && (
              <p className="text-xs font-medium text-gray-600 mt-0.5">{result.subtitle}</p>
            )}
            {result.description && (
              <p className="text-xs text-gray-500 mt-1 line-clamp-2">{result.description}</p>
            )}
          </div>
        </div>
      </div>

      <div className="mt-3 pt-3 border-t border-gray-100 flex items-center justify-end">
        <button
          onClick={handleNavigate}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-[#2563EB]/10 hover:bg-[#2563EB]/20 text-[#2563EB] font-medium text-xs rounded-lg transition-colors"
        >
          <span>{result.deepLinkLabel || 'View Module'}</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
});

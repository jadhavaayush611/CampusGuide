import { Mail, Phone, MapPin, Globe, Users, Calendar, FileText, Check, Plus, Clock, Share2 } from 'lucide-react';
import { Council } from '../../../models/council.model';
import { useCouncilMembership } from '../../../hooks/council/useCouncilMembership';
import { toast } from '../../../core/toast/useToast';

interface CouncilHeaderProps {
  council: Council;
}

export function CouncilHeader({ council }: CouncilHeaderProps) {
  const { join, leave, isJoining, isLeaving } = useCouncilMembership(council.id);

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    toast.success('Council link copied to clipboard!');
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden mb-8">
      {/* Banner */}
      <div className="h-44 relative bg-gradient-to-r from-blue-600 via-indigo-600 to-purple-700 overflow-hidden">
        {council.bannerUrl && (
          <img
            src={council.bannerUrl}
            alt={council.name}
            className="w-full h-full object-cover opacity-40 mix-blend-overlay"
          />
        )}
        <div className="absolute top-4 right-4 flex items-center gap-2">
          <button
            onClick={handleShare}
            className="p-2 bg-white/20 backdrop-blur-md text-white rounded-lg hover:bg-white/30 transition-all"
            title="Share Council"
          >
            <Share2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Main Info Section */}
      <div className="px-8 pb-6 -mt-12">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div className="flex items-end gap-6">
            <div className="w-24 h-24 bg-white rounded-2xl border-4 border-white shadow-xl flex items-center justify-center text-4xl flex-shrink-0">
              {council.logoEmoji || '🏛️'}
            </div>
            <div className="mb-1">
              <div className="flex items-center gap-3 mb-1 flex-wrap">
                <h1 className="text-2xl font-bold text-gray-900">{council.name}</h1>
                <span className="text-xs bg-blue-100 text-[#2563EB] px-3 py-1 rounded-full font-semibold">
                  {council.category}
                </span>
                <span className="text-xs bg-emerald-100 text-emerald-800 px-3 py-1 rounded-full font-medium flex items-center gap-1">
                  <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full animate-pulse"></span>
                  Official Council
                </span>
              </div>
              <p className="text-gray-600 text-sm max-w-2xl leading-relaxed">{council.description}</p>
            </div>
          </div>

          {/* Action Button */}
          <div className="flex items-center gap-3 mb-1">
            {council.pendingJoinRequest ? (
              <button
                disabled
                className="px-6 py-2.5 bg-amber-50 border border-amber-200 text-amber-700 rounded-xl font-medium text-sm flex items-center gap-2"
              >
                <Clock className="w-4 h-4" />
                Join Request Pending
              </button>
            ) : (
              <button
                onClick={() => (council.isJoined ? leave() : join())}
                disabled={isJoining || isLeaving}
                className={`px-6 py-2.5 rounded-xl font-medium text-sm transition-all shadow-xs flex items-center gap-2 ${
                  council.isJoined
                    ? 'bg-gray-100 text-gray-700 hover:bg-gray-200 border border-gray-300'
                    : 'bg-[#2563EB] text-white hover:bg-blue-600'
                }`}
              >
                {council.isJoined ? (
                  <>
                    <Check className="w-4 h-4" />
                    Joined Council
                  </>
                ) : (
                  <>
                    <Plus className="w-4 h-4" />
                    Join Council
                  </>
                )}
              </button>
            )}
          </div>
        </div>

        {/* Contact Info & Details Bar */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-gray-100 text-sm text-gray-600">
          {council.contactInfo.email && (
            <div className="flex items-center gap-2">
              <Mail className="w-4 h-4 text-gray-400" />
              <a href={`mailto:${council.contactInfo.email}`} className="hover:text-[#2563EB] truncate">
                {council.contactInfo.email}
              </a>
            </div>
          )}

          {council.contactInfo.phone && (
            <div className="flex items-center gap-2">
              <Phone className="w-4 h-4 text-gray-400" />
              <span>{council.contactInfo.phone}</span>
            </div>
          )}

          {council.contactInfo.officeLocation && (
            <div className="flex items-center gap-2">
              <MapPin className="w-4 h-4 text-gray-400" />
              <span className="truncate">{council.contactInfo.officeLocation}</span>
            </div>
          )}

          {council.contactInfo.websiteUrl && (
            <div className="flex items-center gap-2">
              <Globe className="w-4 h-4 text-gray-400" />
              <a
                href={council.contactInfo.websiteUrl}
                target="_blank"
                rel="noreferrer"
                className="hover:text-[#2563EB] truncate"
              >
                Official Portal ↗
              </a>
            </div>
          )}
        </div>

        {/* Activity Metrics Bar */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4 p-4 bg-gray-50 rounded-xl border border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center text-[#2563EB]">
              <Users className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Total Members</p>
              <p className="text-base font-bold text-gray-900">{council.memberCount}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center text-emerald-700">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Active Events</p>
              <p className="text-base font-bold text-gray-900">{council.activityMetrics.activeEventsCount}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center text-purple-700">
              <FileText className="w-5 h-5" />
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Notices & Minutes</p>
              <p className="text-base font-bold text-gray-900">{council.activityMetrics.noticesCount}</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center text-amber-700">
              <span className="text-sm font-bold">%</span>
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">Engagement Score</p>
              <p className="text-base font-bold text-gray-900">{council.activityMetrics.engagementRate}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

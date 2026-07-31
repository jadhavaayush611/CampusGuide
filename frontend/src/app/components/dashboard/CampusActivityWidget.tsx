import React from 'react';
import { useCampusEvents } from '../../../hooks/campus/useCampusEvents';
import { useCouncils } from '../../../hooks/campus/useCouncils';
import { useResources } from '../../../hooks/campus/useResources';
import { Calendar, Shield, BookOpen, Bell, ArrowRight, MapPin, Users, Download, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router';

export const CampusActivityWidget: React.FC = () => {
  const navigate = useNavigate();

  const { data: events = [], isLoading: loadingEvents } = useCampusEvents(true);
  const { data: councilsData, isLoading: loadingCouncils } = useCouncils();
  const councils = councilsData?.councils || [];
  const { data: resources = [], isLoading: loadingResources } = useResources();

  const isLoading = loadingEvents || loadingCouncils || loadingResources;

  if (isLoading) {
    return (
      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm animate-pulse space-y-4">
        <div className="h-6 bg-gray-200 rounded w-1/3"></div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="h-32 bg-gray-100 rounded-xl"></div>
          <div className="h-32 bg-gray-100 rounded-xl"></div>
          <div className="h-32 bg-gray-100 rounded-xl"></div>
        </div>
      </div>
    );
  }

  // Previews
  const topEvents = events.slice(0, 3);
  const topCouncils = councils.slice(0, 3);
  const topResources = resources.slice(0, 3);

  return (
    <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-purple-50 flex items-center justify-center text-purple-600">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">Campus Activity</h3>
            <p className="text-xs text-gray-500">Events, student councils, resources & notices</p>
          </div>
        </div>
      </div>

      {/* Tabs / 4-column Grid for Previews */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">

        {/* Card 1: Upcoming Events */}
        <div className="bg-gradient-to-b from-purple-50/40 to-white rounded-xl p-4 border border-purple-100 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2 text-purple-900 font-bold text-sm">
                <Calendar className="w-4 h-4 text-purple-600" />
                <span>Campus Events</span>
              </div>
              <span className="text-[10px] bg-purple-100 text-purple-700 font-bold px-2 py-0.5 rounded-full">
                {events.length} Upcoming
              </span>
            </div>

            {topEvents.length === 0 ? (
              <div className="py-6 text-center text-gray-400 text-xs">No upcoming events</div>
            ) : (
              <div className="space-y-2.5">
                {topEvents.map((evt) => (
                  <div key={evt.id} className="bg-white p-2.5 rounded-lg border border-gray-200/70 text-xs">
                    <p className="font-bold text-gray-900 line-clamp-1">{evt.title}</p>
                    <div className="flex items-center gap-2 text-[11px] text-gray-500 mt-1">
                      <MapPin className="w-3 h-3 text-gray-400" />
                      <span className="truncate">{evt.location}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button
            onClick={() => navigate('/councils')}
            className="mt-4 pt-2 text-xs font-semibold text-purple-600 hover:text-purple-800 flex items-center justify-center gap-1 border-t border-purple-100 hover:underline"
          >
            <span>Explore Events</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Card 2: Student Councils */}
        <div className="bg-gradient-to-b from-blue-50/40 to-white rounded-xl p-4 border border-blue-100 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2 text-blue-900 font-bold text-sm">
                <Shield className="w-4 h-4 text-blue-600" />
                <span>Councils</span>
              </div>
              <span className="text-[10px] bg-blue-100 text-blue-700 font-bold px-2 py-0.5 rounded-full">
                {councils.length} Active
              </span>
            </div>

            {topCouncils.length === 0 ? (
              <div className="py-6 text-center text-gray-400 text-xs">No councils listed</div>
            ) : (
              <div className="space-y-2.5">
                {topCouncils.map((c) => (
                  <div
                    key={c.id}
                    onClick={() => navigate(`/councils/${c.id}`)}
                    className="bg-white p-2.5 rounded-lg border border-gray-200/70 text-xs hover:border-blue-300 cursor-pointer transition-colors"
                  >
                    <p className="font-bold text-gray-900">{c.name}</p>
                    <div className="flex items-center justify-between text-[11px] text-gray-500 mt-1">
                      <span>{c.category}</span>
                      {c.memberCount && (
                        <span className="flex items-center gap-1 text-blue-600">
                          <Users className="w-3 h-3" />
                          {c.memberCount}
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button
            onClick={() => navigate('/councils')}
            className="mt-4 pt-2 text-xs font-semibold text-blue-600 hover:text-blue-800 flex items-center justify-center gap-1 border-t border-blue-100 hover:underline"
          >
            <span>View All Councils</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Card 3: Resource Center */}
        <div className="bg-gradient-to-b from-indigo-50/40 to-white rounded-xl p-4 border border-indigo-100 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2 text-indigo-900 font-bold text-sm">
                <BookOpen className="w-4 h-4 text-indigo-600" />
                <span>Resources</span>
              </div>
              <span className="text-[10px] bg-indigo-100 text-indigo-700 font-bold px-2 py-0.5 rounded-full">
                {resources.length} Docs
              </span>
            </div>

            {topResources.length === 0 ? (
              <div className="py-6 text-center text-gray-400 text-xs">No resources uploaded</div>
            ) : (
              <div className="space-y-2.5">
                {topResources.map((res) => (
                  <div key={res.id} className="bg-white p-2.5 rounded-lg border border-gray-200/70 text-xs">
                    <p className="font-bold text-gray-900 truncate">{res.title}</p>
                    <div className="flex items-center justify-between text-[11px] text-gray-500 mt-1">
                      <span className="uppercase text-[10px] font-bold text-indigo-700">{res.fileType || 'PDF'}</span>
                      <a
                        href={res.downloadUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="text-indigo-600 hover:underline flex items-center gap-0.5"
                      >
                        <Download className="w-3 h-3" />
                        Download
                      </a>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <button
            onClick={() => navigate('/resources')}
            className="mt-4 pt-2 text-xs font-semibold text-indigo-600 hover:text-indigo-800 flex items-center justify-center gap-1 border-t border-indigo-100 hover:underline"
          >
            <span>Open Resource Hub</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Card 4: Official Announcements / Notices */}
        <div className="bg-gradient-to-b from-amber-50/40 to-white rounded-xl p-4 border border-amber-100 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2 text-amber-900 font-bold text-sm">
                <Bell className="w-4 h-4 text-amber-600" />
                <span>Notice Board</span>
              </div>
              <span className="text-[10px] bg-amber-100 text-amber-800 font-bold px-2 py-0.5 rounded-full">
                Active
              </span>
            </div>

            <div className="space-y-2.5">
              <div
                onClick={() => navigate('/notices')}
                className="bg-white p-2.5 rounded-lg border border-amber-200/80 text-xs cursor-pointer hover:bg-amber-50/30 transition-colors"
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="font-bold text-gray-900 truncate">Mid-Sem Exam Schedule</span>
                  <span className="bg-red-100 text-red-700 text-[9px] font-bold px-1.5 py-0.5 rounded">HIGH</span>
                </div>
                <p className="text-[11px] text-gray-600 line-clamp-1">Issued by Dean of Academics</p>
              </div>

              <div
                onClick={() => navigate('/notices')}
                className="bg-white p-2.5 rounded-lg border border-amber-200/80 text-xs cursor-pointer hover:bg-amber-50/30 transition-colors"
              >
                <div className="flex items-center justify-between mb-1">
                  <span className="font-bold text-gray-900 truncate">Scholarship Application</span>
                  <span className="bg-amber-100 text-amber-700 text-[9px] font-bold px-1.5 py-0.5 rounded">MID</span>
                </div>
                <p className="text-[11px] text-gray-600 line-clamp-1">Computer Eng Department</p>
              </div>
            </div>
          </div>

          <button
            onClick={() => navigate('/notices')}
            className="mt-4 pt-2 text-xs font-semibold text-amber-700 hover:text-amber-900 flex items-center justify-center gap-1 border-t border-amber-100 hover:underline"
          >
            <span>View All Notices</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

      </div>
    </div>
  );
};

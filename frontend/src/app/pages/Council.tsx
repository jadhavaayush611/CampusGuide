import { useState } from 'react';
import { useParams, useNavigate } from 'react-router';
import { Header } from '../components/Header';
import { ArrowLeft, Award, FileText, Calendar, FolderOpen, Users, ShieldAlert } from 'lucide-react';
import { ErrorBoundary } from '../../core/errors/ErrorBoundary';
import {
  useCouncilDetails,
  useCouncilLeadership,
  useCouncilEvents,
  useCouncilNotices,
  useCouncilResources,
} from '../../hooks';
import { CouncilHeader } from '../components/councils/CouncilHeader';
import { CouncilLeadership } from '../components/councils/CouncilLeadership';
import { CouncilEvents } from '../components/councils/CouncilEvents';
import { CouncilNotices } from '../components/councils/CouncilNotices';
import { CouncilResources } from '../components/councils/CouncilResources';
import { CouncilMembers } from '../components/councils/CouncilMembers';
import { CouncilHeaderSkeleton, CouncilSectionSkeleton } from '../components/councils/CouncilSkeletons';

type CouncilTab = 'leadership' | 'notices' | 'events' | 'resources' | 'members';

export function Council() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<CouncilTab>('leadership');

  // Parallel Queries
  const detailsQuery = useCouncilDetails(id);
  const leadershipQuery = useCouncilLeadership(id);
  const eventsQuery = useCouncilEvents(id);
  const noticesQuery = useCouncilNotices(id);
  const resourcesQuery = useCouncilResources(id);

  const council = detailsQuery.data;

  const tabs = [
    { id: 'leadership' as CouncilTab, label: 'Leadership & Governance', icon: Award, count: undefined },
    { id: 'notices' as CouncilTab, label: 'Notices & Announcements', icon: FileText, count: noticesQuery.data?.length },
    { id: 'events' as CouncilTab, label: 'Council Events', icon: Calendar, count: eventsQuery.data?.length },
    { id: 'resources' as CouncilTab, label: 'Resources & Documents', icon: FolderOpen, count: resourcesQuery.data?.length },
    { id: 'members' as CouncilTab, label: 'Members Directory', icon: Users, count: council?.memberCount },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Back Button */}
          <button
            onClick={() => navigate('/councils')}
            className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-gray-600 hover:text-gray-900 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Councils Directory
          </button>

          {/* Header Section */}
          {detailsQuery.isLoading ? (
            <CouncilHeaderSkeleton />
          ) : detailsQuery.isError || !council ? (
            <div className="bg-white rounded-xl border border-red-200 p-12 text-center max-w-md mx-auto mb-8">
              <ShieldAlert className="w-12 h-12 text-red-500 mx-auto mb-3" />
              <h3 className="text-lg font-bold text-gray-900 mb-1">Council Not Found</h3>
              <p className="text-sm text-gray-600 mb-4">The council you requested does not exist or was removed.</p>
              <button
                onClick={() => navigate('/councils')}
                className="px-4 py-2 bg-[#2563EB] text-white rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
              >
                Return to Directory
              </button>
            </div>
          ) : (
            <ErrorBoundary>
              <CouncilHeader council={council} />
            </ErrorBoundary>
          )}

          {/* Navigation Tabs */}
          {council && (
            <>
              <div className="flex gap-4 border-b border-gray-200 mb-8 overflow-x-auto scrollbar-none">
                {tabs.map((tab) => {
                  const Icon = tab.icon;
                  return (
                    <button
                      key={tab.id}
                      onClick={() => setActiveTab(tab.id)}
                      className={`pb-3 px-2 text-sm font-medium transition-colors relative flex items-center gap-2 whitespace-nowrap ${
                        activeTab === tab.id ? 'text-[#2563EB]' : 'text-gray-600 hover:text-gray-900'
                      }`}
                    >
                      <Icon className="w-4 h-4" />
                      {tab.label}
                      {tab.count !== undefined && (
                        <span className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full font-semibold">
                          {tab.count}
                        </span>
                      )}
                      {activeTab === tab.id && <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]" />}
                    </button>
                  );
                })}
              </div>

              {/* Tab Panels with Error Boundaries */}
              <div className="min-h-[400px]">
                {activeTab === 'leadership' && (
                  <ErrorBoundary>
                    {leadershipQuery.isLoading ? (
                      <CouncilSectionSkeleton />
                    ) : (
                      <CouncilLeadership
                        leadership={leadershipQuery.data || []}
                        facultyAdvisor={council.facultyAdvisor}
                      />
                    )}
                  </ErrorBoundary>
                )}

                {activeTab === 'notices' && (
                  <ErrorBoundary>
                    {noticesQuery.isLoading ? (
                      <CouncilSectionSkeleton />
                    ) : (
                      <CouncilNotices notices={noticesQuery.data || []} />
                    )}
                  </ErrorBoundary>
                )}

                {activeTab === 'events' && (
                  <ErrorBoundary>
                    {eventsQuery.isLoading ? (
                      <CouncilSectionSkeleton />
                    ) : (
                      <CouncilEvents events={eventsQuery.data || []} />
                    )}
                  </ErrorBoundary>
                )}

                {activeTab === 'resources' && (
                  <ErrorBoundary>
                    {resourcesQuery.isLoading ? (
                      <CouncilSectionSkeleton />
                    ) : (
                      <CouncilResources resources={resourcesQuery.data || []} />
                    )}
                  </ErrorBoundary>
                )}

                {activeTab === 'members' && (
                  <ErrorBoundary>
                    <CouncilMembers councilId={council.id} />
                  </ErrorBoundary>
                )}
              </div>
            </>
          )}
        </div>
      </main>
    </div>
  );
}

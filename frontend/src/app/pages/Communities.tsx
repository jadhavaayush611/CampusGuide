import { useState, lazy, Suspense } from "react";
import { useNavigate } from "react-router";

import { Header } from "../components/Header";
import { CommunityDiscovery } from "../components/communities/CommunityDiscovery";
import { CommunityActivityPanel } from "../components/communities/CommunityActivityPanel";
import { Community } from "../../models/community.model";
import { ErrorBoundary } from "../../core/errors/ErrorBoundary";

const CommunityCreateModal = lazy(() =>
  import("../components/communities/CommunityCreateModal").then((m) => ({ default: m.CommunityCreateModal }))
);

export function Communities() {
  const navigate = useNavigate();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  const handleSelectCommunity = (community: Community) => {
    navigate(`/communities/${community.id}`);
  };

  return (
    <div className="min-h-screen bg-gray-50/50">
      <Header />
      <main className="p-4 sm:p-8">
        <div className="max-w-[1440px] mx-auto space-y-8">
          {/* Page Header */}
          <div>
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-2">
              Campus Communities
            </h1>
            <p className="text-sm font-medium text-gray-600">
              Discover, join, and collaborate in student clubs, societies, and interest groups.
            </p>
          </div>

          {/* Main Grid: Discovery & Activity Panel */}
          <div className="grid grid-cols-1 lg:grid-cols-[1fr_360px] gap-8">
            {/* Left Column: Discovery Hub */}
            <div className="space-y-8">
              <ErrorBoundary>
                <CommunityDiscovery
                  onSelectCommunity={handleSelectCommunity}
                  onCreateCommunity={() => setIsCreateModalOpen(true)}
                />
              </ErrorBoundary>
            </div>

            {/* Right Column: Activity & Recommendations Panel */}
            <div className="space-y-8">
              <ErrorBoundary>
                <CommunityActivityPanel
                  onSelectCommunity={handleSelectCommunity}
                />
              </ErrorBoundary>
            </div>
          </div>
        </div>
      </main>

      {/* Community Creation Modal */}
      <Suspense fallback={null}>
        <CommunityCreateModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
        />
      </Suspense>
    </div>
  );
}

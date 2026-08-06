import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router';

import { ArrowLeft, MessageSquare, Users, Info } from 'lucide-react';
import { useCommunityDetails } from '../../hooks/community/useCommunityDetails';
import { CommunityHeader } from '../components/communities/CommunityHeader';
import { CommunityFeed } from '../components/communities/CommunityFeed';
import { CommunityMembers } from '../components/communities/CommunityMembers';
import { CommunityCreateModal } from '../components/communities/CommunityCreateModal';
import { CommunityHeaderSkeleton } from '../components/communities/CommunitySkeletons';
import { ErrorBoundary } from '../../core/errors/ErrorBoundary';

import { CommunityAbout } from '../components/communities/CommunityAbout';

export function CommunityDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'feed' | 'members' | 'about'>('feed');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);

  const { data: community, isLoading, isError, refetch } = useCommunityDetails(id || '');

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50/50 dark:bg-background text-foreground p-6 sm:p-8 transition-colors duration-150">
        <div className="max-w-[1440px] mx-auto space-y-6">
          <CommunityHeaderSkeleton />
        </div>
      </div>
    );
  }

  if (isError || !community) {
    return (
      <div className="min-h-screen bg-gray-50/50 dark:bg-background text-foreground p-8 flex items-center justify-center transition-colors duration-150">
        <div className="bg-card text-card-foreground rounded-2xl border border-border p-12 text-center max-w-md w-full shadow-md space-y-4">
          <h2 className="text-2xl font-extrabold text-foreground">Community Not Found</h2>
          <p className="text-sm text-muted-foreground">
            The community you are looking for may have been removed or does not exist.
          </p>
          <div className="flex justify-center gap-3 pt-2">
            <button
              onClick={() => navigate('/communities')}
              className="px-5 py-2.5 bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl text-xs font-bold shadow-sm transition-all active:scale-[0.98] cursor-pointer"
            >
              Back to Communities
            </button>
            <button
              onClick={() => refetch()}
              className="px-5 py-2.5 bg-muted text-muted-foreground hover:bg-accent hover:text-accent-foreground rounded-xl text-xs font-bold transition-all active:scale-[0.98] cursor-pointer"
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50/50 dark:bg-background text-foreground p-4 sm:p-8 transition-colors duration-150">
      <div className="max-w-[1440px] mx-auto space-y-8">
        {/* Navigation header */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => navigate('/communities')}
            className="inline-flex items-center gap-2 px-4 py-2 bg-card border border-border rounded-xl text-xs font-bold text-foreground/80 hover:bg-accent hover:text-accent-foreground shadow-sm transition-all active:scale-[0.98] cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Communities
          </button>
        </div>

        {/* Section-level Error Boundary wrapping header */}
        <ErrorBoundary>
          <CommunityHeader
            community={community}
            onEdit={() => setIsEditModalOpen(true)}
          />
        </ErrorBoundary>

        {/* Detail Navigation Tabs */}
        <div className="bg-card text-card-foreground rounded-2xl border border-border p-2 flex items-center gap-2 shadow-sm max-w-fit">
          <button
            onClick={() => setActiveTab('feed')}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all cursor-pointer active:scale-[0.98] ${
              activeTab === 'feed'
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
            }`}
          >
            <MessageSquare className="w-4 h-4" />
            Discussions Feed
          </button>
          <button
            onClick={() => setActiveTab('members')}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all cursor-pointer active:scale-[0.98] ${
              activeTab === 'members'
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
            }`}
          >
            <Users className="w-4 h-4" />
            Members ({community.memberCount})
          </button>
          <button
            onClick={() => setActiveTab('about')}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2 transition-all cursor-pointer active:scale-[0.98] ${
              activeTab === 'about'
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
            }`}
          >
            <Info className="w-4 h-4" />
            About
          </button>
        </div>

        {/* Tab content with Section-Level Error Boundaries */}
        <ErrorBoundary>
          {activeTab === 'feed' && <CommunityFeed communityId={community.id} />}
          {activeTab === 'members' && <CommunityMembers communityId={community.id} />}
          {activeTab === 'about' && <CommunityAbout community={community} />}
        </ErrorBoundary>
      </div>

      {/* Edit Community Modal */}
      <CommunityCreateModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        initialCommunity={community}
      />
    </div>
  );
}

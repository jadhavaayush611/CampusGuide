import { queryClient } from '../query/queryClient';
import { queryKeys } from '../../sdk/queryKeys';
import { communitySdk } from '../../sdk/community/CommunitySdk';
import { councilSdk } from '../../sdk/council/CouncilSdk';
import { resourceSdk } from '../../sdk/resources/ResourceSdk';
import { noticeSdk } from '../../sdk/notices/NoticeSdk';
import { plannerSdk } from '../../sdk/planner/PlannerSdk';
import { atlasSdk } from '../../sdk/atlas/AtlasSdk';
import { notificationSdk } from '../../sdk/notifications/NotificationSdk';
import { calendarSdk } from '../../sdk/calendar/CalendarSdk';

/**
 * Route Lazy Component Loaders for JS chunk prefetching
 */
const routeComponentLoaders: Record<string, () => Promise<unknown>> = {
  '/': () => import('../../app/pages/Dashboard'),
  '/atlas': () => import('../../app/pages/AtlasPage'),
  '/calendar': () => import('../../app/pages/CalendarPage'),
  '/planner': () => import('../../app/pages/PlannerPage'),
  '/academic': () => import('../../app/pages/Academic'),
  '/councils': () => import('../../app/pages/Councils'),
  '/communities': () => import('../../app/pages/Communities'),
  '/resources': () => import('../../app/pages/ResourceCenter'),
  '/notices': () => import('../../app/pages/NoticeBoard'),
  '/notifications': () => import('../../app/pages/NotificationsPage'),
  '/profile': () => import('../../app/pages/Profile'),
};

const detailRouteLoaders = {
  community: () => import('../../app/pages/CommunityDetail'),
  council: () => import('../../app/pages/Council'),
};

/**
 * Lightweight Route & Data Prefetcher.
 * Initiates route bundle loading and lightweight query prefetching on hover / focus.
 */
export function prefetchRoute(path: string): void {
  // 1. Prefetch Lazy Route JS Bundle
  const matchedLoader = routeComponentLoaders[path];
  if (matchedLoader) {
    matchedLoader();
  } else if (path.startsWith('/communities/')) {
    detailRouteLoaders.community();
  } else if (path.startsWith('/councils/')) {
    detailRouteLoaders.council();
  }

  // 2. Prefetch Lightweight React Query Server State
  switch (path) {
    case '/notices':
      queryClient.prefetchQuery({
        queryKey: queryKeys.notices.recent(),
        queryFn: () => noticeSdk.getRecentNotices(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/communities':
      queryClient.prefetchQuery({
        queryKey: queryKeys.communities.featured(),
        queryFn: () => communitySdk.getFeaturedCommunities(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/councils':
      queryClient.prefetchQuery({
        queryKey: queryKeys.councils.featured(),
        queryFn: () => councilSdk.getFeaturedCouncils(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/resources':
      queryClient.prefetchQuery({
        queryKey: queryKeys.resources.featured(),
        queryFn: () => resourceSdk.getFeaturedResources(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/planner':
      queryClient.prefetchQuery({
        queryKey: queryKeys.planner.tasks({ pageSize: 10 }),
        queryFn: () => plannerSdk.getTasks({ pageSize: 10 }),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/academic':
      queryClient.prefetchQuery({
        queryKey: queryKeys.planner.courses(),
        queryFn: () => plannerSdk.getCourses(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/calendar':
      queryClient.prefetchQuery({
        queryKey: queryKeys.calendar.entries(),
        queryFn: () => calendarSdk.getEntries(),
        staleTime: 5 * 60 * 1000,
      });
      break;

    case '/notifications':
      queryClient.prefetchQuery({
        queryKey: queryKeys.notifications.list(),
        queryFn: () => notificationSdk.getNotifications(),
        staleTime: 1 * 60 * 1000,
      });
      break;

    case '/atlas':
      queryClient.prefetchQuery({
        queryKey: queryKeys.atlas.capabilities(),
        queryFn: () => atlasSdk.getCapabilities(),
        staleTime: 15 * 60 * 1000,
      });
      break;

    default:
      if (path.startsWith('/communities/')) {
        const id = path.split('/')[2];
        if (id) {
          queryClient.prefetchQuery({
            queryKey: queryKeys.communities.detail(id),
            queryFn: () => communitySdk.getCommunityById(id),
            staleTime: 5 * 60 * 1000,
          });
        }
      } else if (path.startsWith('/councils/')) {
        const id = path.split('/')[2];
        if (id) {
          queryClient.prefetchQuery({
            queryKey: queryKeys.councils.detail(id),
            queryFn: () => councilSdk.getCouncilById(id),
            staleTime: 5 * 60 * 1000,
          });
        }
      }
      break;
  }
}

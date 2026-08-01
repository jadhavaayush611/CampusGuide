import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router";
import { RootLayout } from "./components/RootLayout";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import { ProtectedRoute } from "../core/routing/ProtectedRoute";
import { PublicRoute } from "../core/routing/PublicRoute";
import { PageLoadingFallback } from "./components/PageLoadingFallback";

const Dashboard = lazy(() => import("./pages/Dashboard").then((m) => ({ default: m.Dashboard })));
const Academic = lazy(() => import("./pages/Academic").then((m) => ({ default: m.Academic })));
const Communities = lazy(() => import("./pages/Communities").then((m) => ({ default: m.Communities })));
const CommunityDetail = lazy(() => import("./pages/CommunityDetail").then((m) => ({ default: m.CommunityDetail })));
const Councils = lazy(() => import("./pages/Councils").then((m) => ({ default: m.Councils })));
const Council = lazy(() => import("./pages/Council").then((m) => ({ default: m.Council })));
const ResourceCenter = lazy(() => import("./pages/ResourceCenter").then((m) => ({ default: m.ResourceCenter })));
const NoticeBoard = lazy(() => import("./pages/NoticeBoard").then((m) => ({ default: m.NoticeBoard })));
const PlannerPage = lazy(() => import("./pages/PlannerPage").then((m) => ({ default: m.PlannerPage })));
const CalendarPage = lazy(() => import("./pages/CalendarPage").then((m) => ({ default: m.CalendarPage })));
const NotificationsPage = lazy(() => import("./pages/NotificationsPage").then((m) => ({ default: m.NotificationsPage })));
const AtlasPage = lazy(() => import("./pages/AtlasPage").then((m) => ({ default: m.AtlasPage })));
const Profile = lazy(() => import("./pages/Profile").then((m) => ({ default: m.Profile })));
const Unauthorized = lazy(() => import("./pages/Unauthorized").then((m) => ({ default: m.Unauthorized })));
const NotFound = lazy(() => import("./pages/NotFound").then((m) => ({ default: m.NotFound })));

export const router = createBrowserRouter([
  {
    path: "/login",
    element: (
      <PublicRoute restricted>
        <Login />
      </PublicRoute>
    ),
  },
  {
    path: "/register",
    element: (
      <PublicRoute restricted>
        <Register />
      </PublicRoute>
    ),
  },
  {
    path: "/unauthorized",
    element: (
      <Suspense fallback={<PageLoadingFallback />}>
        <Unauthorized />
      </Suspense>
    ),
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <RootLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, Component: Dashboard },
      { path: "atlas", Component: AtlasPage },
      { path: "calendar", Component: CalendarPage },
      { path: "planner", Component: PlannerPage },
      { path: "academic", Component: Academic },
      { path: "councils", Component: Councils },
      { path: "councils/:id", Component: Council },
      { path: "communities", Component: Communities },
      { path: "communities/:id", Component: CommunityDetail },
      { path: "resources", Component: ResourceCenter },
      { path: "notices", Component: NoticeBoard },
      { path: "notifications", Component: NotificationsPage },
      { path: "profile", Component: Profile },
      { path: "*", Component: NotFound },
    ],
  },
]);






import { createBrowserRouter } from "react-router";
import { RootLayout } from "./components/RootLayout";
import { Dashboard } from "./pages/Dashboard";
import { Communities } from "./pages/Communities";
import { CommunityDetail } from "./pages/CommunityDetail";
import { Councils } from "./pages/Councils";
import { Council } from "./pages/Council";
import { ResourceCenter } from "./pages/ResourceCenter";
import { NoticeBoard } from "./pages/NoticeBoard";
import { PlannerPage } from "./pages/PlannerPage";
import { Profile } from "./pages/Profile";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import { Academic } from "./pages/Academic";
import { CalendarPage } from "./pages/CalendarPage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { Unauthorized } from "./pages/Unauthorized";
import { NotFound } from "./pages/NotFound";
import { ProtectedRoute } from "../core/routing/ProtectedRoute";
import { PublicRoute } from "../core/routing/PublicRoute";

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
    element: <Unauthorized />,
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





import { createBrowserRouter } from "react-router";
import { RootLayout } from "./components/RootLayout";
import { Dashboard } from "./pages/Dashboard";
import { Communities } from "./pages/Communities";
import { Councils } from "./pages/Councils";
import { Council } from "./pages/Council";
import { ResourceCenter } from "./pages/ResourceCenter";
import { NoticeBoard } from "./pages/NoticeBoard";
import { Profile } from "./pages/Profile";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
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
    path: "/",
    element: (
      <ProtectedRoute>
        <RootLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, Component: Dashboard },
      { path: "councils", Component: Councils },
      { path: "councils/:id", Component: Council },
      { path: "communities", Component: Communities },
      { path: "resources", Component: ResourceCenter },
      { path: "notices", Component: NoticeBoard },
      { path: "profile", Component: Profile },
      { path: "*", Component: NotFound },
    ],
  },
]);

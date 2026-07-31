import { createBrowserRouter } from "react-router";
import { RootLayout } from "./components/RootLayout";
import { Dashboard } from "./pages/Dashboard";
import { Communities } from "./pages/Communities";
import { Councils } from "./pages/Councils";
import { Council } from "./pages/Council";
import { ResourceCenter } from "./pages/ResourceCenter";
import { NoticeBoard } from "./pages/NoticeBoard";
import { Profile } from "./pages/Profile";
import { NotFound } from "./pages/NotFound";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: RootLayout,
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

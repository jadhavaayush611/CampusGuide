import { Outlet } from "react-router";
import { Sidebar } from "./Sidebar";

export function RootLayout() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 overflow-auto">
        <Outlet />
      </div>
    </div>
  );
}

import { Suspense } from "react";
import { Outlet } from "react-router";
import { Sidebar } from "./Sidebar";
import { PageLoadingFallback } from "./PageLoadingFallback";

export function RootLayout() {
  return (
    <div className="flex h-screen bg-gray-50 dark:bg-background">
      <Sidebar />
      <div className="flex-1 overflow-auto">
        <Suspense fallback={<PageLoadingFallback />}>
          <Outlet />
        </Suspense>
      </div>
    </div>
  );
}

import { memo, useCallback } from "react";
import { NavLink, useNavigate } from "react-router";
import {
  LayoutDashboard,
  Sparkles,
  Calendar,
  CalendarCheck,
  GraduationCap,
  Users,
  Shield,
  BookOpen,
  ClipboardList,
  Bell,
  User,
  LogOut
} from "lucide-react";
import { useLogout } from "../../hooks/auth/useLogout";
import { prefetchRoute } from "../../core/routing/routePrefetch";

const navItems = [
  { path: "/", label: "Dashboard", icon: LayoutDashboard },
  { path: "/atlas", label: "Atlas AI", icon: Sparkles },
  { path: "/calendar", label: "Calendar", icon: Calendar },
  { path: "/planner", label: "Planner", icon: CalendarCheck },
  { path: "/academic", label: "Academic", icon: GraduationCap },
  { path: "/councils", label: "Councils", icon: Shield },
  { path: "/communities", label: "Communities", icon: Users },
  { path: "/resources", label: "Resource Center", icon: BookOpen },
  { path: "/notices", label: "Notice Board", icon: ClipboardList },
  { path: "/notifications", label: "Notifications", icon: Bell },
  { path: "/profile", label: "Profile", icon: User },
];

const SidebarNavItem = memo(function SidebarNavItem({
  item,
}: {
  item: typeof navItems[number];
}) {
  const Icon = item.icon;

  const handlePrefetch = useCallback(() => {
    prefetchRoute(item.path);
  }, [item.path]);

  return (
    <li>
      <NavLink
        to={item.path}
        end={item.path === "/"}
        onMouseEnter={handlePrefetch}
        onFocus={handlePrefetch}
        className={({ isActive }) =>
          `flex items-center gap-3 px-4 py-3 rounded-lg transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-sidebar-primary focus-visible:ring-offset-2 ${
            isActive
              ? "bg-sidebar-accent text-sidebar-primary border-l-4 border-sidebar-primary font-semibold"
              : "text-sidebar-foreground/80 hover:bg-sidebar-accent hover:text-sidebar-foreground"
          }`
        }
      >
        <Icon className="w-5 h-5" />
        <span>{item.label}</span>
      </NavLink>
    </li>
  );
});

export const Sidebar = memo(function Sidebar() {
  const logoutMutation = useLogout();
  const navigate = useNavigate();

  const handleLogout = useCallback(() => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/login", { replace: true });
      },
    });
  }, [logoutMutation, navigate]);

  return (
    <aside className="w-64 bg-sidebar border-r border-sidebar-border flex flex-col h-full">
      <div className="p-6 border-b border-sidebar-border">
        <h1 className="text-xl font-bold text-sidebar-foreground">CampusGuide</h1>
      </div>
      <nav className="flex-1 p-4" aria-label="Primary">
        <ul className="space-y-1">
          {navItems.map((item) => (
            <SidebarNavItem key={item.path} item={item} />
          ))}
        </ul>
      </nav>
      <div className="p-4 border-t border-sidebar-border">
        <button
          onClick={handleLogout}
          disabled={logoutMutation.isPending}
          className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-destructive hover:bg-destructive/10 transition-colors duration-150 font-medium text-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-destructive focus-visible:ring-offset-2"
          type="button"
        >
          <LogOut className="w-5 h-5 text-destructive" />
          <span>{logoutMutation.isPending ? "Signing out..." : "Sign Out"}</span>
        </button>
      </div>
    </aside>
  );
});

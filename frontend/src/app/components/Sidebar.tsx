import { NavLink, useNavigate } from "react-router";
import {
  LayoutDashboard,
  Calendar,
  CalendarCheck,
  GraduationCap,
  Users,
  Shield,
  BookOpen,
  ClipboardList,
  User,
  LogOut
} from "lucide-react";
import { useLogout } from "../../hooks/auth/useLogout";

const navItems = [
  { path: "/", label: "Dashboard", icon: LayoutDashboard },
  { path: "/calendar", label: "Calendar", icon: Calendar },
  { path: "/planner", label: "Planner", icon: CalendarCheck },
  { path: "/academic", label: "Academic", icon: GraduationCap },
  { path: "/councils", label: "Councils", icon: Shield },
  { path: "/communities", label: "Communities", icon: Users },
  { path: "/resources", label: "Resource Center", icon: BookOpen },
  { path: "/notices", label: "Notice Board", icon: ClipboardList },
  { path: "/profile", label: "Profile", icon: User },
];


export function Sidebar() {
  const logoutMutation = useLogout();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/login", { replace: true });
      },
    });
  };

  return (
    <aside className="w-64 bg-[#fafafa] border-r border-gray-200 flex flex-col h-full">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-xl font-semibold text-gray-900">CampusGuide</h1>
      </div>
      <nav className="flex-1 p-4">
        <ul className="space-y-2">
          {navItems.map((item) => (
            <li key={item.path}>
              <NavLink
                to={item.path}
                end={item.path === "/"}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-4 py-3 rounded-lg transition-all ${
                    isActive
                      ? "bg-blue-50 text-[#2563EB] border-l-4 border-[#2563EB]"
                      : "text-gray-700 hover:bg-gray-100"
                  }`
                }
              >
                <item.icon className="w-5 h-5" />
                <span>{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
      <div className="p-4 border-t border-gray-200">
        <button
          onClick={handleLogout}
          disabled={logoutMutation.isPending}
          className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-red-600 hover:bg-red-50 transition-colors font-medium text-sm"
        >
          <LogOut className="w-5 h-5 text-red-500" />
          <span>{logoutMutation.isPending ? "Signing out..." : "Sign Out"}</span>
        </button>
      </div>
    </aside>
  );
}

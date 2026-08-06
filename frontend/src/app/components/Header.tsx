import { useCallback, memo } from "react";
import { Bell, Clock, MessageSquare, Calendar, Settings, LogOut, User as UserIcon } from "lucide-react";
import { useNavigate } from "react-router";
import { useAuth } from "../../core/auth";
import { useLogout } from "../../hooks/auth/useLogout";
import { useNotifications } from "../../hooks/notifications/useNotifications";
import { useUnreadNotificationCount } from "../../hooks/notifications/useUnreadNotificationCount";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "./ui/popover";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";

export const Header = memo(function Header() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const logoutMutation = useLogout();
  const { data: notifications = [] } = useNotifications();
  const { data: unreadCount = 0 } = useUnreadNotificationCount();
  const handleLogout = useCallback(() => {
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/login", { replace: true });
      },
    });
  }, [logoutMutation, navigate]);

  const displayName = user?.name || user?.email?.split("@")[0] || "Campus User";
  const avatarInitial = (user?.name?.[0] || user?.email?.[0] || "U").toUpperCase();

  return (
    <header className="bg-card text-card-foreground border-b border-border px-8 py-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-foreground">
            Hi, {displayName} 👋
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Here's what's happening today
          </p>
        </div>
        <nav className="flex items-center gap-4" aria-label="Header actions">
          <Popover modal>
            <PopoverTrigger asChild>
              <button
                className="relative rounded-lg p-2 transition-all duration-150 hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                aria-label={
                  unreadCount > 0
                    ? `Notifications, ${unreadCount} unread`
                    : "Notifications"
                }
                type="button"
              >
                <Bell className="h-5 w-5 text-foreground/80" />
                {unreadCount > 0 && (
                  <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] font-bold text-white">
                    {unreadCount}
                    <span className="sr-only">unread notifications</span>
                  </span>
                )}
              </button>
            </PopoverTrigger>
            <PopoverContent
              className="w-96 overflow-hidden border border-border bg-card p-0"
              align="end"
              sideOffset={8}
            >
              <section aria-label="Recent notifications">
                <div className="flex items-center justify-between border-b border-border p-4">
                  <h3 className="font-semibold text-foreground">Notifications</h3>
                  <button
                    onClick={() => navigate("/profile")}
                    className="rounded-lg p-1.5 transition-all duration-150 hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                    type="button"
                    aria-label="Open profile settings"
                  >
                    <Settings className="h-4 w-4 text-muted-foreground" />
                  </button>
                </div>
                <ul className="max-h-96 overflow-y-auto" aria-label="Notifications list">
                  {notifications.map((notification) => (
                    <li key={notification.id} className="border-b border-border/50 last:border-0">
                      <article className="p-4 transition-all duration-150 hover:bg-accent/40">
                        <div className="flex gap-3">
                          <div
                            className={`h-8 w-8 flex-shrink-0 rounded-lg ${
                              notification.type === "reminder" ? "bg-blue-500/10 text-blue-500" : "bg-purple-500/10 text-purple-500"
                            } flex items-center justify-center`}
                            aria-hidden="true"
                          >
                            {notification.type === "reminder" ? (
                              <Clock className="h-4 w-4 text-[#2563EB]" />
                            ) : notification.type === "event" ? (
                              <Calendar className="h-4 w-4 text-[#7C3AED]" />
                            ) : (
                              <MessageSquare className="h-4 w-4 text-[#7C3AED]" />
                            )}
                          </div>
                          <div className="min-w-0 flex-1">
                            <div className="flex items-start justify-between gap-2">
                              <p className="text-sm font-medium text-foreground">{notification.title}</p>
                              <span
                                className="mt-1.5 h-2 w-2 flex-shrink-0 rounded-full bg-blue-500"
                                aria-hidden="true"
                              />
                            </div>
                            <p className="mt-0.5 text-sm text-muted-foreground">{notification.description}</p>
                            <p className="mt-1 text-xs text-muted-foreground/80">{notification.time}</p>
                            {notification.type === "reminder" && (
                              <span className="mt-2 inline-block rounded bg-blue-500/10 px-2 py-0.5 text-xs text-blue-500 font-medium">
                                Reminder
                              </span>
                            )}
                          </div>
                        </div>
                      </article>
                    </li>
                  ))}
                </ul>
                <div className="border-t border-border bg-muted/20 p-3">
                  <button
                    onClick={() => navigate("/notifications")}
                    className="w-full text-center text-sm font-semibold text-primary hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 rounded-md"
                    type="button"
                  >
                    View All Notifications
                  </button>
                </div>
              </section>
            </PopoverContent>
          </Popover>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] font-semibold text-white shadow-sm transition-opacity hover:opacity-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 cursor-pointer"
                aria-label={`User menu for ${displayName}`}
                type="button"
              >
                {avatarInitial}
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56 border border-border bg-card p-2" align="end" sideOffset={8}>
              <div className="border-b border-border px-2 py-2">
                <p className="truncate text-sm font-semibold text-foreground">{displayName}</p>
                {user?.email && <p className="truncate text-xs text-muted-foreground">{user.email}</p>}
              </div>
              <DropdownMenuItem
                onSelect={() => navigate("/profile")}
                className="mt-1 cursor-pointer px-2.5 py-2.5 text-sm text-foreground/80"
              >
                <UserIcon className="h-4 w-4 text-muted-foreground" />
                <span>Your Profile</span>
              </DropdownMenuItem>
              <DropdownMenuItem
                onSelect={handleLogout}
                disabled={logoutMutation.isPending}
                variant="destructive"
                className="cursor-pointer px-2.5 py-2.5 text-sm"
              >
                <LogOut className="h-4 w-4 text-destructive" />
                <span>{logoutMutation.isPending ? "Signing out..." : "Sign Out"}</span>
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </nav>
      </div>
    </header>
  );
});

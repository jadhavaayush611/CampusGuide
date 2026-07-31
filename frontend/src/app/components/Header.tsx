import { useState } from "react";
import { Bell, Clock, MessageSquare, Calendar, Settings, LogOut, User as UserIcon } from "lucide-react";
import { useNavigate } from "react-router";
import { useAuth } from "../../core/auth";
import { useLogout } from "../../hooks/auth/useLogout";
import { useNotifications } from "../../hooks/notifications/useNotifications";
import { useUnreadNotificationCount } from "../../hooks/notifications/useUnreadNotificationCount";

export function Header() {
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();
  const logoutMutation = useLogout();
  const { data: notifications = [] } = useNotifications();
  const { data: unreadCount = 0 } = useUnreadNotificationCount();


  const handleLogout = () => {
    setUserMenuOpen(false);
    logoutMutation.mutate(undefined, {
      onSuccess: () => {
        navigate("/login", { replace: true });
      },
    });
  };

  const displayName = user?.name || user?.email?.split("@")[0] || "Campus User";
  const avatarInitial = (user?.name?.[0] || user?.email?.[0] || "U").toUpperCase();

  return (
    <header className="bg-white border-b border-gray-200 px-8 py-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">
            Hi, {displayName} 👋
          </h2>
          <p className="text-sm text-gray-600 mt-1">
            Here's what's happening today
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="relative">
            <button
              onClick={() => {
                setNotificationOpen(!notificationOpen);
                setUserMenuOpen(false);
              }}
              className="p-2 hover:bg-gray-100 rounded-lg transition-all duration-150"
              aria-label="Notifications"
            >
              <Bell className="w-5 h-5 text-gray-700" />
              {unreadCount > 0 && (
                <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </button>

            {/* Notification Dropdown */}
            {notificationOpen && (
              <>
                <div
                  className="fixed inset-0 z-10"
                  onClick={() => setNotificationOpen(false)}
                ></div>
                <div className="absolute right-0 mt-2 w-96 bg-white rounded-xl border border-gray-200 shadow-lg z-20 overflow-hidden">
                  <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                    <h3 className="font-semibold text-gray-900">Notifications</h3>
                    <button
                      onClick={() => {
                        navigate("/profile");
                        setNotificationOpen(false);
                      }}
                      className="p-1.5 hover:bg-gray-100 rounded-lg transition-all duration-150"
                    >
                      <Settings className="w-4 h-4 text-gray-600" />
                    </button>
                  </div>
                  <div className="max-h-96 overflow-y-auto">
                    {notifications.map((notification) => (
                      <div
                        key={notification.id}
                        className="p-4 border-b border-gray-100 last:border-0 hover:bg-gray-50 transition-all duration-150 cursor-pointer"
                      >
                        <div className="flex gap-3">
                          <div
                            className={`w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0 ${
                              notification.type === "reminder"
                                ? "bg-blue-50"
                                : "bg-purple-50"
                            }`}
                          >
                            {notification.type === "reminder" ? (
                              <Clock className="w-4 h-4 text-[#2563EB]" />
                            ) : notification.type === "event" ? (
                              <Calendar className="w-4 h-4 text-[#7C3AED]" />
                            ) : (
                              <MessageSquare className="w-4 h-4 text-[#7C3AED]" />
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-start justify-between gap-2">
                              <p className="text-sm font-medium text-gray-900">
                                {notification.title}
                              </p>
                              <div className="w-2 h-2 bg-blue-500 rounded-full flex-shrink-0 mt-1.5"></div>
                            </div>
                            <p className="text-sm text-gray-600 mt-0.5">
                              {notification.description}
                            </p>
                            <p className="text-xs text-gray-500 mt-1">
                              {notification.time}
                            </p>
                            {notification.type === "reminder" && (
                              <span className="inline-block mt-2 text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded">
                                Reminder
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="p-3 border-t border-gray-200 bg-gray-50">
                    <button className="text-sm text-[#2563EB] hover:underline w-full text-center">
                      View All Notifications
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>

          {/* User Profile Dropdown */}
          <div className="relative">
            <button
              onClick={() => {
                setUserMenuOpen(!userMenuOpen);
                setNotificationOpen(false);
              }}
              className="w-10 h-10 rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] flex items-center justify-center text-white font-semibold shadow-sm hover:opacity-95 transition-opacity"
              aria-label="User menu"
            >
              {avatarInitial}
            </button>

            {userMenuOpen && (
              <>
                <div
                  className="fixed inset-0 z-10"
                  onClick={() => setUserMenuOpen(false)}
                ></div>
                <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl border border-gray-200 shadow-lg z-20 py-2">
                  <div className="px-4 py-2 border-b border-gray-100">
                    <p className="text-sm font-semibold text-gray-900 truncate">
                      {displayName}
                    </p>
                    {user?.email && (
                      <p className="text-xs text-gray-500 truncate">{user.email}</p>
                    )}
                  </div>

                  <button
                    onClick={() => {
                      setUserMenuOpen(false);
                      navigate("/profile");
                    }}
                    className="w-full px-4 py-2.5 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2 transition-colors"
                  >
                    <UserIcon className="w-4 h-4 text-gray-500" />
                    <span>Your Profile</span>
                  </button>

                  <button
                    onClick={handleLogout}
                    disabled={logoutMutation.isPending}
                    className="w-full px-4 py-2.5 text-left text-sm text-red-600 hover:bg-red-50 flex items-center gap-2 transition-colors border-t border-gray-100"
                  >
                    <LogOut className="w-4 h-4 text-red-500" />
                    <span>{logoutMutation.isPending ? "Signing out..." : "Sign Out"}</span>
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}

import { useState } from "react";
import { Bell, Clock, MessageSquare, Calendar, Settings } from "lucide-react";
import { useNavigate } from "react-router";

const notifications = [
  {
    id: 1,
    type: "reminder",
    title: "HackFest 2026 in 2 hours",
    description: "Event starts at 9:00 AM",
    time: "2h ago",
  },
  {
    id: 2,
    type: "mention",
    title: "New announcement from Student Council",
    description: "Mid-Semester Exam Schedule Released",
    time: "5h ago",
  },
  {
    id: 3,
    type: "reminder",
    title: "Mental Health Workshop tomorrow",
    description: "Don't forget to attend at 3:00 PM",
    time: "1d ago",
  },
  {
    id: 4,
    type: "event",
    title: "Spring Music Night this Friday",
    description: "Main Auditorium, 6:00 PM",
    time: "2d ago",
  },
];

export function Header() {
  const [notificationOpen, setNotificationOpen] = useState(false);
  const navigate = useNavigate();

  return (
    <header className="bg-white border-b border-gray-200 px-8 py-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">
            Hi, Rohan 👋
          </h2>
          <p className="text-sm text-gray-600 mt-1">
            Here's what's happening today
          </p>
        </div>
        <div className="flex items-center gap-4">
          <div className="relative">
            <button
              onClick={() => setNotificationOpen(!notificationOpen)}
              className="p-2 hover:bg-gray-100 rounded-lg transition-all duration-150"
            >
              <Bell className="w-5 h-5 text-gray-700" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full"></span>
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
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] flex items-center justify-center text-white font-semibold">
            R
          </div>
        </div>
      </div>
    </header>
  );
}

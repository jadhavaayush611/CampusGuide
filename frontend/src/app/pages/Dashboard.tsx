import { useState } from "react";
import { useNavigate } from "react-router";
import { Header } from "../components/Header";
import { Search, Calendar, MapPin, Clock, ChevronLeft, ChevronRight, Bookmark } from "lucide-react";

type Tab = "overview" | "calendar";

const officialUpdates = [
  {
    id: 1,
    title: "Exam Schedule Released",
    description: "Mid-semester examinations will be held from April 15-22",
    date: "Apr 10, 2026",
    type: "warning",
  },
  {
    id: 2,
    title: "Campus Closed - Public Holiday",
    description: "University will be closed on Apr 14 for national holiday",
    date: "Apr 8, 2026",
    type: "error",
  },
  {
    id: 3,
    title: "Library Hours Extended",
    description: "Central library now open until 11 PM on weekdays",
    date: "Apr 7, 2026",
    type: "warning",
  },
];

const events = [
  {
    id: 1,
    title: "Autominds",
    organizer: "ISA VESIT",
    tag: "Automation",
    tagColor: "bg-blue-100 text-blue-700",
    date: "Apr 12, 2026",
    time: "10:00 AM",
    location: "Seminar Hall A",
    image: "https://images.unsplash.com/photo-1485827404703-89b55fcc595e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxyb2JvdGljcyUyMGF1dG9tYXRpb24lMjB0ZWNobm9sb2d5fGVufDB8fHx8MTcwODM2NTIwMHww&ixlib=rb-4.1.0&q=80&w=1080",
  },
  {
    id: 2,
    title: "UTSAV",
    organizer: "Cultural Council",
    tag: "Cultural",
    tagColor: "bg-purple-100 text-purple-700",
    date: "Apr 18, 2026",
    time: "5:00 PM",
    location: "Main Auditorium",
    image: "https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjdWx0dXJhbCUyMGZlc3RpdmFsJTIwY2VsZWJyYXRpb258ZW58MHx8fHwxNzA4MzY1MjAwfDA&ixlib=rb-4.1.0&q=80&w=1080",
  },
  {
    id: 3,
    title: "Machine Learning Live Project",
    organizer: "ISTE VESIT",
    tag: "AI/ML",
    tagColor: "bg-green-100 text-green-700",
    date: "Apr 22, 2026",
    time: "2:00 PM",
    location: "CS Lab 401",
    image: "https://images.unsplash.com/photo-1677442136019-21780ecad995?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtYWNoaW5lJTIwbGVhcm5pbmclMjBBSSUyMHdvcmtzaG9wfGVufDB8fHx8MTcwODM2NTIwMHww&ixlib=rb-4.1.0&q=80&w=1080",
  },
  {
    id: 4,
    title: "JPMorganChase HireVue Assessment",
    organizer: "Training and Placement Cell (TPC)",
    tag: "Placement",
    tagColor: "bg-indigo-100 text-indigo-700",
    date: "Apr 25, 2026",
    time: "9:00 AM",
    location: "Online Platform",
    image: "https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxqb2IlMjBpbnRlcnZpZXclMjBhc3Nlc3NtZW50fGVufDB8fHx8MTcwODM2NTIwMHww&ixlib=rb-4.1.0&q=80&w=1080",
  },
];

// Calendar data
const getCurrentMonth = (offset: number) => {
  const baseDate = new Date(2026, 3, 1); // April 2026 (month 3)
  const targetDate = new Date(baseDate.getFullYear(), baseDate.getMonth() + offset, 1);
  return targetDate.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
};

const getDaysInMonth = (offset: number) => {
  const baseDate = new Date(2026, 3, 1); // April 2026
  const targetDate = new Date(baseDate.getFullYear(), baseDate.getMonth() + offset, 1);
  return new Date(targetDate.getFullYear(), targetDate.getMonth() + 1, 0).getDate();
};

const getFirstDayOfWeek = (offset: number) => {
  const baseDate = new Date(2026, 3, 1); // April 2026
  const targetDate = new Date(baseDate.getFullYear(), baseDate.getMonth() + offset, 1);
  return targetDate.getDay(); // 0 = Sunday, 1 = Monday, etc.
};

const calendarEvents = [
  { date: 7, type: "official", color: "bg-gradient-to-r from-amber-400 to-orange-500" },
  { date: 12, type: "official", color: "bg-gradient-to-r from-red-400 to-pink-500" },
  { date: 15, type: "community", color: "bg-gradient-to-r from-blue-400 to-cyan-500" },
  { date: 18, type: "community", color: "bg-gradient-to-r from-blue-400 to-cyan-500" },
  { date: 20, type: "community", color: "bg-gradient-to-r from-blue-400 to-cyan-500" },
  { date: 22, type: "community", color: "bg-gradient-to-r from-blue-400 to-cyan-500" },
  { date: 25, type: "council", color: "bg-gradient-to-r from-purple-400 to-violet-500" },
  { date: 28, type: "community", color: "bg-gradient-to-r from-blue-400 to-cyan-500" },
];

const upcomingEvents = [
  { title: "HackFest 2026", date: "Apr 15", time: "9:00 AM", badge: "Coding", location: "CS Block, Lab 301" },
  { title: "Spring Music Night", date: "Apr 18", time: "6:00 PM", badge: "Music", location: "Main Auditorium" },
  { title: "AI & ML Workshop", date: "Apr 20", time: "2:00 PM", badge: "Workshop", location: "Innovation Hub" },
  { title: "Sports Meet", date: "Apr 22", time: "8:00 AM", badge: "Sports", location: "Sports Complex" },
];

const yourCouncils = [
  { id: 1, name: "Student Council", logo: "🏛️", role: "Member" },
  { id: 2, name: "Technical Council", logo: "💻", role: "Volunteer" },
  { id: 3, name: "Cultural Council", logo: "🎭", role: "Member" },
];

const importantNotices = [
  {
    id: 1,
    title: "Mid-Semester Exam Schedule",
    issuedBy: "Dean of Academics",
    date: "Apr 10, 2026",
    priority: "HIGH" as const,
  },
  {
    id: 2,
    title: "Scholarship Application Deadline",
    issuedBy: "HOD - Computer Engineering",
    date: "Apr 8, 2026",
    priority: "HIGH" as const,
  },
  {
    id: 3,
    title: "Library Wing Maintenance",
    issuedBy: "Facilities Management",
    date: "Apr 9, 2026",
    priority: "MEDIUM" as const,
  },
];

export function Dashboard() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<Tab>("overview");
  const [calendarView, setCalendarView] = useState<"month" | "week">("month");
  const [selectedDate, setSelectedDate] = useState<number | null>(null);
  const [currentWeekOffset, setCurrentWeekOffset] = useState<number>(0); // 0 = current week, -1 = previous week, 1 = next week
  const [currentMonthOffset, setCurrentMonthOffset] = useState<number>(0); // 0 = current month, -1 = previous month, 1 = next month

  const navigateWeek = (direction: 'prev' | 'next') => {
    setCurrentWeekOffset(prev => direction === 'prev' ? prev - 1 : prev + 1);
  };

  const navigateMonth = (direction: 'prev' | 'next') => {
    setCurrentMonthOffset(prev => direction === 'prev' ? prev - 1 : prev + 1);
  };

  const renderCalendar = () => {
    const weekDays = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

    // Common header component for both views
    const renderCalendarHeader = (title: string, subtitle: string) => (
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg">
            <Calendar className="w-6 h-6 text-white" />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-gray-900 bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
              {title}
            </h3>
            <p className="text-sm text-gray-600">{subtitle}</p>
          </div>
          <div className="flex gap-2 ml-4">
            <button
              onClick={() => calendarView === "week" ? navigateWeek('prev') : navigateMonth('prev')}
              className="p-2 hover:bg-white/80 rounded-lg transition-all duration-200 shadow-sm"
            >
              <ChevronLeft className="w-5 h-5 text-gray-600 hover:text-blue-600" />
            </button>
            <button
              onClick={() => calendarView === "week" ? navigateWeek('next') : navigateMonth('next')}
              className="p-2 hover:bg-white/80 rounded-lg transition-all duration-200 shadow-sm"
            >
              <ChevronRight className="w-5 h-5 text-gray-600 hover:text-blue-600" />
            </button>
          </div>
        </div>
        <div className="flex gap-2 bg-white/60 rounded-xl p-1.5 shadow-sm backdrop-blur-sm">
          <button
            onClick={() => setCalendarView("month")}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
              calendarView === "month"
                ? "bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg transform scale-105"
                : "text-gray-600 hover:text-gray-900 hover:bg-white/80"
            }`}
          >
            Month
          </button>
          <button
            onClick={() => setCalendarView("week")}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
              calendarView === "week"
                ? "bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg transform scale-105"
                : "text-gray-600 hover:text-gray-900 hover:bg-white/80"
            }`}
          >
            Week
          </button>
        </div>
      </div>
    );

    if (calendarView === "week") {
      // Calculate the week start based on current week offset
      // Today is Apr 7, 2026, so current week starts on Apr 6 (Sunday)
      const baseWeekStart = 6; // Apr 6, 2026 (Sunday)
      const weekStart = baseWeekStart + (currentWeekOffset * 7);

      // Calculate week range for display
      const weekEnd = weekStart + 6;
      const weekStartDate = new Date(2026, 3, weekStart); // April is month 3
      const weekEndDate = new Date(2026, 3, weekEnd);

      const formatWeekRange = () => {
        const startMonth = weekStartDate.toLocaleDateString('en-US', { month: 'long' });
        const endMonth = weekEndDate.toLocaleDateString('en-US', { month: 'long' });
        const startDay = weekStart;
        const endDay = weekEnd;
        const year = weekStartDate.getFullYear();

        if (startMonth === endMonth) {
          return `Week of ${startMonth} ${startDay}-${endDay}, ${year}`;
        } else {
          return `Week of ${startMonth} ${startDay} - ${endMonth} ${endDay}, ${year}`;
        }
      };

      const weekDaysData = [];

      for (let i = 0; i < 7; i++) {
        const day = weekStart + i;
        const isToday = day === 7 && currentWeekOffset === 0; // Only highlight today in current week
        const dayEvents = calendarEvents.filter((e) => e.date === day);
        const dayUpcomingEvents = upcomingEvents.filter(event => {
          const eventDay = parseInt(event.date.split(' ')[1]);
          return eventDay === day;
        });

        weekDaysData.push({
          day,
          isToday,
          events: dayEvents,
          upcomingEvents: dayUpcomingEvents
        });
      }

      return (
        <div className="bg-gradient-to-br from-white via-blue-50/30 to-purple-50/30 rounded-2xl p-8 border border-gray-200/50 shadow-xl backdrop-blur-sm">
          {renderCalendarHeader(formatWeekRange(), "Weekly Event View")}

          {/* Week grid */}
          <div className="grid grid-cols-7 gap-4">
            {weekDaysData.map((dayData, index) => (
              <div
                key={dayData.day}
                onClick={() => setSelectedDate(dayData.day)}
                className={`min-h-[200px] p-4 border rounded-xl hover:shadow-md cursor-pointer transition-all duration-200 relative overflow-hidden ${
                  dayData.isToday
                    ? "bg-gradient-to-br from-blue-500 to-blue-600 border-blue-300 shadow-lg transform scale-105"
                    : selectedDate === dayData.day
                    ? "bg-gradient-to-br from-indigo-50 to-purple-50 border-indigo-200 shadow-sm"
                    : "bg-white border-gray-200 hover:border-indigo-300"
                }`}
              >
                {/* Background pattern for today */}
                {dayData.isToday && (
                  <div className="absolute inset-0 bg-gradient-to-br from-blue-400/20 to-purple-400/20 rounded-xl"></div>
                )}

                <div
                  className={`text-lg font-bold mb-3 relative z-10 ${
                    dayData.isToday
                      ? "text-white"
                      : selectedDate === dayData.day
                      ? "text-indigo-900"
                      : "text-gray-700"
                  }`}
                >
                  {dayData.day}
                </div>
                <div className="text-xs text-gray-500 mb-3 relative z-10">
                  {weekDays[index]}
                </div>

                {/* Event indicators */}
                <div className="flex gap-1 flex-wrap mb-3 relative z-10">
                  {dayData.events.map((event, idx) => (
                    <div
                      key={idx}
                      className={`w-3 h-3 rounded-full ${event.color} shadow-sm animate-pulse`}
                      title={
                        event.type === "official"
                          ? "Official Update"
                          : event.type === "council"
                          ? "Council Event"
                          : "Community Event"
                      }
                    ></div>
                  ))}
                </div>

                {/* Upcoming events for this day */}
                <div className="space-y-2 relative z-10">
                  {dayData.upcomingEvents.map((event, idx) => (
                    <div
                      key={idx}
                      className="bg-white/80 backdrop-blur-sm rounded-lg p-2 text-xs border border-white/50"
                    >
                      <div className="font-medium text-gray-900 truncate">
                        {event.title}
                      </div>
                      <div className="text-gray-600 mt-1">
                        {event.time}
                      </div>
                    </div>
                  ))}
                </div>

                {/* Hover effect overlay */}
                <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/0 to-purple-500/0 hover:from-indigo-500/5 hover:to-purple-500/5 rounded-xl transition-all duration-200"></div>
              </div>
            ))}
          </div>

          {/* Legend with enhanced styling */}
          <div className="flex flex-wrap gap-6 mt-8 pt-6 border-t border-gray-200/50">
            <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
              <div className="w-4 h-4 rounded-full bg-gradient-to-r from-blue-400 to-blue-500 shadow-sm animate-pulse"></div>
              <span className="text-sm font-medium text-gray-700">Community Events</span>
            </div>
            <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
              <div className="w-4 h-4 rounded-full bg-gradient-to-r from-purple-400 to-purple-500 shadow-sm animate-pulse"></div>
              <span className="text-sm font-medium text-gray-700">Council Events</span>
            </div>
            <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
              <div className="w-4 h-4 rounded-full bg-gradient-to-r from-amber-400 to-orange-500 shadow-sm animate-pulse"></div>
              <span className="text-sm font-medium text-gray-700">Official Updates</span>
            </div>
            <div className="flex items-center gap-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl px-4 py-2 shadow-lg">
              <div className="w-4 h-4 rounded-full bg-white shadow-sm"></div>
              <span className="text-sm font-medium text-white">Today</span>
            </div>
          </div>
        </div>
      );
    }

    // Month view (existing code)
    const days = [];
    const currentMonthDisplay = getCurrentMonth(currentMonthOffset);
    const daysInMonth = getDaysInMonth(currentMonthOffset);
    const firstDayOfWeek = getFirstDayOfWeek(currentMonthOffset);

    // Add empty cells for days before the first day of the month
    for (let i = 0; i < firstDayOfWeek; i++) {
      days.push(<div key={`empty-${i}`} className="aspect-square"></div>);
    }

    // Add days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      const isToday = day === 7;
      const dayEvents = calendarEvents.filter((e) => e.date === day);

      days.push(
        <div
          key={day}
          onClick={() => setSelectedDate(day)}
          className={`aspect-square p-2 border rounded-lg hover:shadow-md cursor-pointer transition-all duration-200 relative overflow-hidden ${
            isToday
              ? "bg-gradient-to-br from-blue-500 to-blue-600 border-blue-300 shadow-lg transform scale-105"
              : selectedDate === day
              ? "bg-gradient-to-br from-indigo-50 to-purple-50 border-indigo-200 shadow-sm"
              : "bg-white border-gray-200 hover:border-indigo-300"
          }`}
        >
          {/* Background pattern for special days */}
          {isToday && (
            <div className="absolute inset-0 bg-gradient-to-br from-blue-400/20 to-purple-400/20 rounded-lg"></div>
          )}

          <div
            className={`text-sm mb-1 relative z-10 ${
              isToday
                ? "text-white font-bold"
                : selectedDate === day
                ? "font-semibold text-indigo-900"
                : "text-gray-700 font-medium"
            }`}
          >
            {day}
          </div>
          <div className="flex gap-1 flex-wrap relative z-10">
            {dayEvents.map((event, idx) => (
              <div
                key={idx}
                className={`w-2 h-2 rounded-full ${event.color} shadow-sm animate-pulse`}
                title={
                  event.type === "official"
                    ? "Official Update"
                    : event.type === "council"
                    ? "Council Event"
                    : "Community Event"
                }
              ></div>
            ))}
          </div>

          {/* Hover effect overlay */}
          <div className="absolute inset-0 bg-gradient-to-br from-indigo-500/0 to-purple-500/0 hover:from-indigo-500/5 hover:to-purple-500/5 rounded-lg transition-all duration-200"></div>
        </div>
      );
    }

    return (
      <div className="bg-gradient-to-br from-white via-blue-50/30 to-purple-50/30 rounded-2xl p-8 border border-gray-200/50 shadow-xl backdrop-blur-sm">
        {renderCalendarHeader(currentMonthDisplay, "Event Calendar")}

        {/* Weekday headers with enhanced styling */}
        <div className="grid grid-cols-7 gap-3 mb-4">
          {weekDays.map((day, index) => (
            <div
              key={day}
              className={`text-center text-sm font-bold py-3 rounded-xl transition-all duration-200 ${
                index === 0
                  ? "text-red-600 bg-red-50/50"
                  : index === 6
                  ? "text-blue-600 bg-blue-50/50"
                  : "text-gray-700 bg-gray-50/50"
              }`}
            >
              {day}
            </div>
          ))}
        </div>

        {/* Calendar grid with enhanced spacing */}
        <div className="grid grid-cols-7 gap-3">{days}</div>

        {/* Legend with enhanced styling */}
        <div className="flex flex-wrap gap-6 mt-8 pt-6 border-t border-gray-200/50">
          <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
            <div className="w-4 h-4 rounded-full bg-gradient-to-r from-blue-400 to-blue-500 shadow-sm animate-pulse"></div>
            <span className="text-sm font-medium text-gray-700">Community Events</span>
          </div>
          <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
            <div className="w-4 h-4 rounded-full bg-gradient-to-r from-purple-400 to-purple-500 shadow-sm animate-pulse"></div>
            <span className="text-sm font-medium text-gray-700">Council Events</span>
          </div>
          <div className="flex items-center gap-3 bg-white/60 rounded-xl px-4 py-2 shadow-sm">
            <div className="w-4 h-4 rounded-full bg-gradient-to-r from-amber-400 to-orange-500 shadow-sm animate-pulse"></div>
            <span className="text-sm font-medium text-gray-700">Official Updates</span>
          </div>
          <div className="flex items-center gap-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl px-4 py-2 shadow-lg">
            <div className="w-4 h-4 rounded-full bg-white shadow-sm"></div>
            <span className="text-sm font-medium text-white">Today</span>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Tabs */}
          <div className="flex gap-8 border-b border-gray-200 mb-8">
            <button
              onClick={() => setActiveTab("overview")}
              className={`pb-3 px-1 text-sm font-medium transition-colors relative ${
                activeTab === "overview"
                  ? "text-[#2563EB]"
                  : "text-gray-600 hover:text-gray-900"
              }`}
            >
              Overview
              {activeTab === "overview" && (
                <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]"></div>
              )}
            </button>
            <button
              onClick={() => setActiveTab("calendar")}
              className={`pb-3 px-1 text-sm font-medium transition-colors relative ${
                activeTab === "calendar"
                  ? "text-[#2563EB]"
                  : "text-gray-600 hover:text-gray-900"
              }`}
            >
              Event Calendar
              {activeTab === "calendar" && (
                <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]"></div>
              )}
            </button>
          </div>

          {/* Overview Tab */}
          {activeTab === "overview" && (
            <div className="space-y-8">
              {/* Official Updates */}
              <section>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Official Updates
                </h3>
                <div className="flex gap-4 overflow-x-auto pb-2">
                  {officialUpdates.map((update) => (
                    <div
                      key={update.id}
                      className={`flex-shrink-0 w-80 p-5 rounded-xl border shadow-sm ${
                        update.type === "error"
                          ? "bg-red-50 border-red-200"
                          : "bg-amber-50 border-amber-200"
                      }`}
                    >
                      <h4 className="font-semibold text-gray-900 mb-2">
                        {update.title}
                      </h4>
                      <p className="text-sm text-gray-700 mb-3">
                        {update.description}
                      </p>
                      <div className="flex items-center justify-between">
                        <span className="text-xs text-gray-600">{update.date}</span>
                        <button className="text-sm text-[#2563EB] hover:underline">
                          View Details
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>

              {/* Your Councils */}
              <section>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Your Councils
                </h3>
                <div className="flex gap-4 overflow-x-auto pb-2">
                  {yourCouncils.map((council) => (
                    <div
                      key={council.id}
                      onClick={() => navigate(`/councils/${council.id}`)}
                      className="flex-shrink-0 w-64 bg-white p-5 rounded-xl border border-gray-200 shadow-sm hover:shadow-md transition-all duration-200 cursor-pointer"
                    >
                      <div className="flex items-center gap-3 mb-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-gray-100 to-gray-200 rounded-lg flex items-center justify-center text-2xl">
                          {council.logo}
                        </div>
                        <div className="flex-1">
                          <h4 className="font-semibold text-gray-900">
                            {council.name}
                          </h4>
                          <span className="text-xs text-gray-600">{council.role}</span>
                        </div>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/councils/${council.id}`);
                        }}
                        className="w-full py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-all duration-150 text-sm"
                      >
                        View
                      </button>
                    </div>
                  ))}
                </div>
              </section>

              {/* Important Notices */}
              <section>
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">
                    Important Notices
                  </h3>
                  <button
                    onClick={() => navigate("/notices")}
                    className="text-sm text-[#2563EB] hover:underline"
                  >
                    View All →
                  </button>
                </div>
                <div className="space-y-3">
                  {importantNotices.map((notice) => (
                    <div
                      key={notice.id}
                      onClick={() => navigate("/notices")}
                      className="bg-white rounded-xl border border-gray-200 shadow-sm p-4 hover:shadow-md transition-all duration-200 cursor-pointer"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <h4 className="font-semibold text-gray-900 mb-1">
                            {notice.title}
                          </h4>
                          <div className="flex items-center gap-3 text-sm text-gray-600">
                            <span>{notice.issuedBy}</span>
                            <span>•</span>
                            <span>{notice.date}</span>
                          </div>
                        </div>
                        <span
                          className={`px-2.5 py-1 rounded-lg text-xs font-semibold border flex-shrink-0 ${
                            notice.priority === "HIGH"
                              ? "bg-red-50 text-red-700 border-red-200"
                              : notice.priority === "MEDIUM"
                              ? "bg-amber-50 text-amber-700 border-amber-200"
                              : "bg-gray-50 text-gray-700 border-gray-200"
                          }`}
                        >
                          {notice.priority}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              </section>

              {/* Personalized Event Feed */}
              <section>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Upcoming Events
                </h3>
                <div className="space-y-4">
                  {events.map((event) => (
                    <div
                      key={event.id}
                      className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 flex gap-5 hover:shadow-md transition-all duration-200"
                    >
                      <img
                        src={event.image}
                        alt={event.title}
                        className="w-32 h-32 rounded-lg object-cover flex-shrink-0"
                      />
                      <div className="flex-1">
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <h4 className="font-semibold text-gray-900 mb-1">
                              {event.title}
                            </h4>
                            <p className="text-sm text-gray-600 mb-2">
                              Organized by {event.organizer}
                            </p>
                            <span
                              className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${event.tagColor}`}
                            >
                              {event.tag}
                            </span>
                          </div>
                          <button className="text-gray-400 hover:text-[#2563EB] transition-all duration-150">
                            <Bookmark className="w-5 h-5" />
                          </button>
                        </div>
                        <div className="space-y-1.5 text-sm text-gray-600 mt-3">
                          <div className="flex items-center gap-2">
                            <Calendar className="w-4 h-4" />
                            <span>{event.date}</span>
                            <Clock className="w-4 h-4 ml-3" />
                            <span>{event.time}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="w-4 h-4" />
                            <span>{event.location}</span>
                          </div>
                        </div>
                      </div>
                      <div className="flex items-center">
                        <button className="px-5 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-all duration-150 border border-gray-200">
                          Remind Me
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            </div>
          )}

          {/* Event Calendar Tab */}
          {activeTab === "calendar" && (
            <div className="grid grid-cols-[1fr_300px] gap-6">
              <div>{renderCalendar()}</div>
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Upcoming This Week
                </h3>
                <div className="space-y-4">
                  {upcomingEvents.map((event, idx) => (
                    <div
                      key={idx}
                      className="pb-4 border-b border-gray-100 last:border-0 last:pb-0 hover:bg-gray-50 transition-all duration-150 rounded-lg p-2 -m-2 cursor-pointer"
                    >
                      <h4 className="font-medium text-gray-900 text-sm mb-1">
                        {event.title}
                      </h4>
                      <div className="text-xs text-gray-600 mb-2">
                        {event.date} • {event.time}
                      </div>
                      <div className="text-xs text-gray-500 mb-2 flex items-center gap-1">
                        <MapPin className="w-3 h-3" />
                        {event.location}
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">
                          {event.badge}
                        </span>
                        <button className="text-xs px-3 py-1 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 transition-all duration-150">
                          Remind Me
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Floating Action Button */}
        <button className="fixed bottom-8 right-8 w-14 h-14 bg-[#2563EB] text-white rounded-full shadow-lg hover:shadow-xl hover:scale-105 transition-all flex items-center justify-center">
          <Search className="w-6 h-6" />
        </button>
      </main>
    </div>
  );
}

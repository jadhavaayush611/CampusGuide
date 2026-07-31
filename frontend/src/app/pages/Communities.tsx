import { Header } from "../components/Header";
import { Search, Users as UsersIcon, MessageSquare, Clock } from "lucide-react";

const joinedCommunities = [
  {
    id: 1,
    name: "Computer Engineering Club",
    category: "Academic",
    members: 245,
    unread: 3,
    categoryColor: "bg-blue-100 text-blue-700",
  },
  {
    id: 2,
    name: "Photography Society",
    category: "Creative",
    members: 128,
    unread: 0,
    categoryColor: "bg-purple-100 text-purple-700",
  },
  {
    id: 3,
    name: "Debate Club",
    category: "Cultural",
    members: 92,
    unread: 5,
    categoryColor: "bg-green-100 text-green-700",
  },
  {
    id: 4,
    name: "Basketball Team",
    category: "Sports",
    members: 56,
    unread: 0,
    categoryColor: "bg-orange-100 text-orange-700",
  },
];

const discoverCommunities = [
  {
    id: 5,
    name: "Robotics Club",
    description: "Build autonomous robots and compete in inter-college competitions",
    category: "Technology",
    members: 187,
    categoryColor: "bg-indigo-100 text-indigo-700",
  },
  {
    id: 6,
    name: "Music Band",
    description: "Weekly jam sessions and performances at campus events",
    category: "Creative",
    members: 76,
    categoryColor: "bg-purple-100 text-purple-700",
  },
  {
    id: 7,
    name: "Entrepreneurship Cell",
    description: "Connect with fellow entrepreneurs and learn startup fundamentals",
    category: "Professional",
    members: 312,
    categoryColor: "bg-blue-100 text-blue-700",
  },
  {
    id: 8,
    name: "Theatre Group",
    description: "Explore dramatic arts through workshops and productions",
    category: "Cultural",
    members: 145,
    categoryColor: "bg-pink-100 text-pink-700",
  },
  {
    id: 9,
    name: "Environmental Club",
    description: "Work on sustainability projects and awareness campaigns",
    category: "Social",
    members: 203,
    categoryColor: "bg-green-100 text-green-700",
  },
  {
    id: 10,
    name: "Chess Club",
    description: "Practice strategies and participate in tournaments",
    category: "Sports",
    members: 68,
    categoryColor: "bg-orange-100 text-orange-700",
  },
];

const recentActivity = [
  {
    id: 1,
    type: "mention",
    community: "CS Club",
    message: "You were mentioned in 'HackFest Planning'",
    time: "2 hours ago",
  },
  {
    id: 2,
    type: "reminder",
    community: "Photography Society",
    message: "Photo walk tomorrow at 7 AM",
    time: "5 hours ago",
  },
  {
    id: 3,
    type: "mention",
    community: "Debate Club",
    message: "New poll: Topic for next debate",
    time: "1 day ago",
  },
  {
    id: 4,
    type: "reminder",
    community: "Basketball Team",
    message: "Practice session in 30 minutes",
    time: "2 days ago",
  },
];

export function Communities() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Page Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-semibold text-gray-900 mb-2">Communities</h1>
            <p className="text-gray-600">Explore and join communities across your campus</p>
          </div>

          {/* Search Bar */}
          <div className="mb-8">
            <div className="relative max-w-xl">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search communities..."
                className="w-full pl-12 pr-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
              />
            </div>
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-[1fr_350px] gap-8">
            {/* Left Column */}
            <div className="space-y-8">
              {/* Joined Communities */}
              <section>
                <h2 className="text-xl font-semibold text-gray-900 mb-4">
                  Joined Communities
                </h2>
                <div className="grid grid-cols-2 gap-4">
                  {joinedCommunities.map((community) => (
                    <div
                      key={community.id}
                      className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 hover:shadow-md transition-shadow"
                    >
                      <div className="flex items-start justify-between mb-3">
                        <h3 className="font-semibold text-gray-900">{community.name}</h3>
                        {community.unread > 0 && (
                          <span className="bg-red-500 text-white text-xs px-2 py-0.5 rounded-full font-medium">
                            {community.unread}
                          </span>
                        )}
                      </div>
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium mb-3 ${community.categoryColor}`}
                      >
                        {community.category}
                      </span>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5 text-sm text-gray-600">
                          <UsersIcon className="w-4 h-4" />
                          <span>{community.members} members</span>
                        </div>
                        <button className="px-4 py-1.5 bg-[#2563EB] text-white rounded-lg text-sm hover:bg-blue-600 transition-colors">
                          Open
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>

              {/* Discover Communities */}
              <section>
                <h2 className="text-xl font-semibold text-gray-900 mb-4">
                  Discover Communities
                </h2>
                <div className="grid grid-cols-2 gap-4">
                  {discoverCommunities.map((community) => (
                    <div
                      key={community.id}
                      className="bg-white rounded-xl border border-gray-200 shadow-sm p-5 hover:shadow-md transition-shadow"
                    >
                      <h3 className="font-semibold text-gray-900 mb-2">
                        {community.name}
                      </h3>
                      <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                        {community.description}
                      </p>
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium mb-3 ${community.categoryColor}`}
                      >
                        {community.category}
                      </span>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-1.5 text-sm text-gray-600">
                          <UsersIcon className="w-4 h-4" />
                          <span>{community.members}</span>
                        </div>
                        <button className="px-4 py-1.5 bg-[#7C3AED] text-white rounded-lg text-sm hover:bg-purple-600 transition-colors">
                          Join
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            </div>

            {/* Right Panel - Your Activity */}
            <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 h-fit sticky top-8">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Your Activity</h3>
              <div className="space-y-4">
                {recentActivity.map((activity) => (
                  <div
                    key={activity.id}
                    className="pb-4 border-b border-gray-100 last:border-0 last:pb-0"
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                        {activity.type === "mention" ? (
                          <MessageSquare className="w-4 h-4 text-[#2563EB]" />
                        ) : (
                          <Clock className="w-4 h-4 text-[#2563EB]" />
                        )}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-xs font-medium text-gray-500 mb-1">
                          {activity.community}
                        </p>
                        <p className="text-sm text-gray-900 mb-1">{activity.message}</p>
                        <p className="text-xs text-gray-500">{activity.time}</p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

import { Header } from "../components/Header";
import { Target, TrendingUp, CheckCircle2, Circle } from "lucide-react";

const roadmaps = [
  {
    id: 1,
    title: "Full Stack Development",
    category: "Technical",
    progress: 65,
    milestones: 12,
    completed: 8,
    color: "bg-blue-500",
  },
  {
    id: 2,
    title: "Campus Placement Preparation",
    category: "Career",
    progress: 45,
    milestones: 10,
    completed: 4,
    color: "bg-purple-500",
  },
  {
    id: 3,
    title: "Research Paper Publication",
    category: "Academic",
    progress: 30,
    milestones: 8,
    completed: 2,
    color: "bg-green-500",
  },
];

const milestones = [
  { title: "Complete HTML & CSS Basics", status: "completed", date: "Jan 15, 2026" },
  { title: "Learn JavaScript Fundamentals", status: "completed", date: "Jan 28, 2026" },
  { title: "Build React Todo App", status: "completed", date: "Feb 5, 2026" },
  { title: "Master Node.js & Express", status: "in-progress", date: "Feb 20, 2026" },
  { title: "Learn Database Design (SQL)", status: "upcoming", date: "Mar 1, 2026" },
  { title: "Build Full Stack Project", status: "upcoming", date: "Mar 15, 2026" },
];

export function Roadmaps() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Page Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-semibold text-gray-900 mb-2">Roadmaps</h1>
            <p className="text-gray-600">Track your learning journey and career goals</p>
          </div>

          {/* Roadmaps Grid */}
          <div className="grid grid-cols-3 gap-6 mb-8">
            {roadmaps.map((roadmap) => (
              <div
                key={roadmap.id}
                className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-shadow"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className={`w-10 h-10 ${roadmap.color} rounded-lg flex items-center justify-center`}>
                    <Target className="w-5 h-5 text-white" />
                  </div>
                  <span className="text-xs bg-gray-100 text-gray-600 px-2.5 py-1 rounded-full">
                    {roadmap.category}
                  </span>
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{roadmap.title}</h3>
                <div className="flex items-center gap-2 text-sm text-gray-600 mb-3">
                  <span>{roadmap.completed}/{roadmap.milestones} milestones</span>
                </div>
                <div className="mb-2">
                  <div className="flex items-center justify-between text-sm mb-1">
                    <span className="text-gray-600">Progress</span>
                    <span className="font-medium text-gray-900">{roadmap.progress}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                    <div
                      className={`${roadmap.color} h-full rounded-full transition-all`}
                      style={{ width: `${roadmap.progress}%` }}
                    ></div>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Detailed Roadmap View */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-900">Full Stack Development Roadmap</h2>
              <button className="px-4 py-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors text-sm">
                View All Milestones
              </button>
            </div>
            <div className="space-y-4">
              {milestones.map((milestone, idx) => (
                <div
                  key={idx}
                  className="flex items-start gap-4 p-4 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <div className="mt-1">
                    {milestone.status === "completed" ? (
                      <div className="w-6 h-6 bg-green-100 rounded-full flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4 text-green-600" />
                      </div>
                    ) : milestone.status === "in-progress" ? (
                      <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center">
                        <TrendingUp className="w-4 h-4 text-blue-600" />
                      </div>
                    ) : (
                      <div className="w-6 h-6 bg-gray-100 rounded-full flex items-center justify-center">
                        <Circle className="w-4 h-4 text-gray-400" />
                      </div>
                    )}
                  </div>
                  <div className="flex-1">
                    <h4 className={`font-medium mb-1 ${
                      milestone.status === "completed" ? "text-gray-500 line-through" : "text-gray-900"
                    }`}>
                      {milestone.title}
                    </h4>
                    <p className="text-sm text-gray-600">Target: {milestone.date}</p>
                  </div>
                  {milestone.status === "completed" && (
                    <span className="text-xs bg-green-100 text-green-700 px-2.5 py-1 rounded-full">
                      Completed
                    </span>
                  )}
                  {milestone.status === "in-progress" && (
                    <span className="text-xs bg-blue-100 text-blue-700 px-2.5 py-1 rounded-full">
                      In Progress
                    </span>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

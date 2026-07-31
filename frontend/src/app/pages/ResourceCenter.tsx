import { Header } from "../components/Header";
import { BookOpen, FileText, Video, Link as LinkIcon, Download } from "lucide-react";

const categories = [
  {
    id: 1,
    name: "Study Materials",
    description: "Lecture notes, textbooks, and study guides",
    icon: FileText,
    color: "bg-blue-100 text-blue-600",
    itemCount: 156,
  },
  {
    id: 2,
    name: "Video Lectures",
    description: "Recorded lectures and tutorial videos",
    icon: Video,
    color: "bg-purple-100 text-purple-600",
    itemCount: 89,
  },
  {
    id: 3,
    name: "Previous Papers",
    description: "Past exam papers and sample questions",
    icon: BookOpen,
    color: "bg-green-100 text-green-600",
    itemCount: 234,
  },
  {
    id: 4,
    name: "External Resources",
    description: "Curated links to online courses and articles",
    icon: LinkIcon,
    color: "bg-orange-100 text-orange-600",
    itemCount: 78,
  },
];

const recentUploads = [
  { name: "Data Structures - Unit 3 Notes.pdf", uploadedBy: "Dr. Sharma", date: "Feb 10, 2026", downloads: 45 },
  { name: "DBMS Tutorial Series - Part 5.mp4", uploadedBy: "CS Club", date: "Feb 9, 2026", downloads: 32 },
  { name: "Calculus Practice Problems.pdf", uploadedBy: "Prof. Mehta", date: "Feb 8, 2026", downloads: 67 },
  { name: "Operating Systems Concepts.pdf", uploadedBy: "Dr. Kumar", date: "Feb 7, 2026", downloads: 54 },
];

export function ResourceCenter() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Page Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-semibold text-gray-900 mb-2">Resource Center</h1>
            <p className="text-gray-600">Access study materials, notes, and learning resources</p>
          </div>

          {/* Categories Grid */}
          <div className="grid grid-cols-4 gap-6 mb-8">
            {categories.map((category) => (
              <div
                key={category.id}
                className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-shadow cursor-pointer"
              >
                <div className={`w-12 h-12 ${category.color} rounded-lg flex items-center justify-center mb-4`}>
                  <category.icon className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-gray-900 mb-2">{category.name}</h3>
                <p className="text-sm text-gray-600 mb-3">{category.description}</p>
                <p className="text-sm text-gray-500">{category.itemCount} items</p>
              </div>
            ))}
          </div>

          {/* Recent Uploads */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Recent Uploads</h2>
            <div className="space-y-3">
              {recentUploads.map((item, idx) => (
                <div
                  key={idx}
                  className="flex items-center justify-between p-4 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                      <FileText className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <h4 className="font-medium text-gray-900">{item.name}</h4>
                      <p className="text-sm text-gray-600">
                        {item.uploadedBy} • {item.date}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-sm text-gray-600">{item.downloads} downloads</span>
                    <button className="p-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors">
                      <Download className="w-5 h-5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

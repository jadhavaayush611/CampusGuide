import { useNavigate } from "react-router";
import { Header } from "../components/Header";
import { Search, Users, ChevronRight } from "lucide-react";

const councils = [
  {
    id: 4,
    name: "Computer Society of India (CSI)",
    description: "Advancing computer engineering and IT education",
    logo: "💻",
    members: 542,
    category: "Technical",
  },
  {
    id: 5,
    name: "Institute of Electrical and Electronics Engineers (IEEE)",
    description: "Fostering technological innovation and excellence",
    logo: "⚡",
    members: 628,
    category: "Technical",
  },
  {
    id: 6,
    name: "Indian Society for Technical Education (ISTE)",
    description: "Promoting technical education and research",
    logo: "🎓",
    members: 485,
    category: "Technical",
  },
  {
    id: 7,
    name: "International Society of Automation (ISA)",
    description: "Advancing automation and control systems",
    logo: "🤖",
    members: 392,
    category: "Technical",
  },
  {
    id: 8,
    name: "CodeCell++",
    description: "Competitive programming and coding excellence",
    logo: "⌨️",
    members: 421,
    category: "Technical",
  },
  {
    id: 9,
    name: "Cultural Council",
    description: "Organizing cultural events and celebrations",
    logo: "🎭",
    members: 567,
    category: "Cultural",
  },
  {
    id: 10,
    name: "Literature Council",
    description: "Promoting literary arts and creative writing",
    logo: "📚",
    members: 298,
    category: "Cultural",
  },
  {
    id: 11,
    name: "Sports Council",
    description: "Managing sports activities and tournaments",
    logo: "⚽",
    members: 634,
    category: "Sports",
  },
  {
    id: 12,
    name: "E-Cell",
    description: "Fostering entrepreneurship and innovation",
    logo: "💡",
    members: 456,
    category: "Entrepreneurship",
  },
  {
    id: 13,
    name: "HABIT (Startup Incubation)",
    description: "Incubating student startups and ventures",
    logo: "🚀",
    members: 234,
    category: "Entrepreneurship",
  },
  {
    id: 14,
    name: "Training and Placement Cell (TPC)",
    description: "Career guidance and placement opportunities",
    logo: "💼",
    members: 892,
    category: "Career",
  },
];

export function Councils() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Page Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-semibold text-gray-900 mb-2">Councils</h1>
            <p className="text-gray-600">Official student councils and governing bodies</p>
          </div>

          {/* Search Bar */}
          <div className="mb-8">
            <div className="relative max-w-xl">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                type="text"
                placeholder="Search councils..."
                className="w-full pl-12 pr-4 py-3 bg-white border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all duration-150"
              />
            </div>
          </div>

          {/* Councils Grid */}
          <div className="grid grid-cols-2 gap-6">
            {councils.map((council) => (
              <div
                key={council.id}
                onClick={() => navigate(`/councils/${council.id}`)}
                className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-all duration-200 cursor-pointer group"
              >
                <div className="flex items-start gap-4">
                  <div className="w-16 h-16 bg-gradient-to-br from-gray-100 to-gray-200 rounded-xl flex items-center justify-center text-3xl flex-shrink-0">
                    {council.logo}
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="font-semibold text-gray-900 text-lg mb-1">
                      {council.name}
                    </h3>
                    <p className="text-sm text-gray-600 mb-3 line-clamp-2">
                      {council.description}
                    </p>
                    <div className="flex items-center gap-4">
                      <div className="flex items-center gap-1.5 text-sm text-gray-600">
                        <Users className="w-4 h-4" />
                        <span>{council.members} members</span>
                      </div>
                      <span className="text-xs bg-gray-100 text-gray-700 px-2.5 py-0.5 rounded-full">
                        {council.category}
                      </span>
                    </div>
                  </div>
                  <button
                    className="px-4 py-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-all duration-150 flex items-center gap-2 group-hover:scale-105"
                    onClick={(e) => {
                      e.stopPropagation();
                      navigate(`/councils/${council.id}`);
                    }}
                  >
                    Open
                    <ChevronRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}

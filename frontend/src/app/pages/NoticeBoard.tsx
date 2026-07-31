import { useState } from "react";
import { Header } from "../components/Header";
import { Filter } from "lucide-react";

type Priority = "HIGH" | "MEDIUM" | "LOW";

interface Notice {
  id: number;
  title: string;
  issuedBy: string;
  department: string;
  date: string;
  content: string;
  priority: Priority;
}

const notices: Notice[] = [
  {
    id: 1,
    title: "Mid-Semester Examination Schedule",
    issuedBy: "Dean of Academics",
    department: "All Departments",
    date: "Apr 10, 2026",
    content:
      "The mid-semester examinations will commence from March 15, 2026. Students are advised to check their individual timetables on the academic portal.",
    priority: "HIGH",
  },
  {
    id: 2,
    title: "Campus Maintenance - Library Wing",
    issuedBy: "Facilities Management",
    department: "All Departments",
    date: "Apr 9, 2026",
    content:
      "Library wing will undergo scheduled maintenance from Apr 20-22. Alternative study spaces will be available at the Innovation Hub.",
    priority: "MEDIUM",
  },
  {
    id: 3,
    title: "Scholarship Application Deadline",
    issuedBy: "HOD - Computer Engineering",
    department: "Computer Engineering",
    date: "Apr 8, 2026",
    content:
      "Last date to submit scholarship applications for the spring semester is April 30, 2026. Submit your documents to the department office.",
    priority: "HIGH",
  },
  {
    id: 4,
    title: "Guest Lecture Series",
    issuedBy: "HOD - Electronics and Computer Science",
    department: "Electronics and Computer Science",
    date: "Apr 7, 2026",
    content:
      "Industry experts will be conducting a series of guest lectures on emerging technologies. Registration open until Apr 15.",
    priority: "LOW",
  },
  {
    id: 5,
    title: "Laboratory Equipment Update",
    issuedBy: "HOD - Automation and Robotics",
    department: "Automation and Robotics",
    date: "Apr 6, 2026",
    content:
      "New CNC machines and 3D printers have been installed in Lab 401. Students can book slots through the online portal.",
    priority: "MEDIUM",
  },
  {
    id: 6,
    title: "Academic Calendar Revision",
    issuedBy: "Registrar Office",
    department: "All Departments",
    date: "Apr 5, 2026",
    content:
      "Minor revisions have been made to the academic calendar. Updated version available on the university website.",
    priority: "LOW",
  },
  {
    id: 7,
    title: "Internship Fair 2026",
    issuedBy: "Placement Cell",
    department: "All Departments",
    date: "Apr 4, 2026",
    content:
      "Annual internship fair scheduled for March 5, 2026. Over 50 companies will be participating. Students should prepare their resumes.",
    priority: "HIGH",
  },
  {
    id: 8,
    title: "Library New Book Arrivals",
    issuedBy: "Chief Librarian",
    department: "All Departments",
    date: "Apr 3, 2026",
    content:
      "Latest editions of popular textbooks and research journals have been added to the library collection. Check the catalog for availability.",
    priority: "LOW",
  },
];

const departments = [
  "All Departments",
  "Computer Engineering",
  "Information Technology",
  "Automation and Robotics",
  "Artificial Intelligence and Data Science",
  "Electronics and Computer Science",
];

const priorities: Priority[] = ["HIGH", "MEDIUM", "LOW"];

export function NoticeBoard() {
  const [selectedDepartment, setSelectedDepartment] = useState("All Departments");
  const [selectedPriority, setSelectedPriority] = useState<Priority | "ALL">("ALL");

  const filteredNotices = notices.filter((notice) => {
    const departmentMatch =
      selectedDepartment === "All Departments" || notice.department === selectedDepartment;
    const priorityMatch = selectedPriority === "ALL" || notice.priority === selectedPriority;
    return departmentMatch && priorityMatch;
  });

  const getPriorityStyles = (priority: Priority) => {
    switch (priority) {
      case "HIGH":
        return "bg-red-50 text-red-700 border-red-200";
      case "MEDIUM":
        return "bg-amber-50 text-amber-700 border-amber-200";
      case "LOW":
        return "bg-gray-50 text-gray-700 border-gray-200";
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Page Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-semibold text-gray-900 mb-2">Notice Board</h1>
            <p className="text-gray-600">Official notices and announcements</p>
          </div>

          {/* Filters */}
          <div className="mb-8 flex gap-4 items-center">
            <Filter className="w-5 h-5 text-gray-500" />
            <select
              value={selectedDepartment}
              onChange={(e) => setSelectedDepartment(e.target.value)}
              className="px-4 py-2 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all duration-150"
            >
              {departments.map((dept) => (
                <option key={dept} value={dept}>
                  {dept}
                </option>
              ))}
            </select>
            <div className="flex gap-2">
              <button
                onClick={() => setSelectedPriority("ALL")}
                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-150 ${
                  selectedPriority === "ALL"
                    ? "bg-[#2563EB] text-white"
                    : "bg-white border border-gray-200 text-gray-700 hover:bg-gray-50"
                }`}
              >
                All
              </button>
              {priorities.map((priority) => (
                <button
                  key={priority}
                  onClick={() => setSelectedPriority(priority)}
                  className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-150 ${
                    selectedPriority === priority
                      ? "bg-[#2563EB] text-white"
                      : "bg-white border border-gray-200 text-gray-700 hover:bg-gray-50"
                  }`}
                >
                  {priority}
                </button>
              ))}
            </div>
          </div>

          {/* Notices */}
          <div className="space-y-4">
            {filteredNotices.map((notice) => (
              <div
                key={notice.id}
                className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-all duration-200"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                      {notice.title}
                    </h3>
                    <div className="flex items-center gap-4 text-sm text-gray-600 mb-3">
                      <span>Issued by: {notice.issuedBy}</span>
                      <span>•</span>
                      <span>{notice.date}</span>
                      {notice.department !== "All Departments" && (
                        <>
                          <span>•</span>
                          <span className="text-xs bg-blue-50 text-blue-700 px-2 py-0.5 rounded">
                            {notice.department}
                          </span>
                        </>
                      )}
                    </div>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-lg text-xs font-semibold border ${getPriorityStyles(
                      notice.priority
                    )}`}
                  >
                    {notice.priority}
                  </span>
                </div>
                <p className="text-gray-700 mb-4">{notice.content}</p>
                <button className="text-[#2563EB] text-sm hover:underline">
                  Read More →
                </button>
              </div>
            ))}
          </div>

          {filteredNotices.length === 0 && (
            <div className="text-center py-12">
              <p className="text-gray-500">No notices found for the selected filters.</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

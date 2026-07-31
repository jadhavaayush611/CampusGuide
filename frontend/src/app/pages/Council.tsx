import { useEffect, useState } from "react";
import { useParams } from "react-router";
import { Header } from "../components/Header";
import { Calendar, MapPin, Clock, FileText, Folder, CheckCircle2, Send, Users } from "lucide-react";

type CouncilTab = "announcements" | "events" | "polls" | "doubts" | "resources";

type CouncilInfo = {
  name: string;
  logo: string;
  members: number;
  description: string;
  isFollowing: boolean;
  category: string;
};

const councilInfoMap: Record<number, CouncilInfo> = {
  1: {
    name: "Student Council",
    logo: "🏛️",
    members: 892,
    description: "Official student body representing all students",
    isFollowing: false,
    category: "student",
  },
  2: {
    name: "Technical Council",
    logo: "💻",
    members: 410,
    description: "Driving technical innovation and project communities",
    isFollowing: false,
    category: "technical",
  },
  3: {
    name: "Cultural Council",
    logo: "🎭",
    members: 520,
    description: "Organizing cultural festivals and student activities",
    isFollowing: false,
    category: "cultural",
  },
  4: {
    name: "Computer Society of India (CSI)",
    logo: "💻",
    members: 542,
    description: "Advancing computer engineering and IT education",
    isFollowing: false,
    category: "technical",
  },
  5: {
    name: "Institute of Electrical and Electronics Engineers (IEEE)",
    logo: "⚡",
    members: 628,
    description: "Fostering technological innovation and excellence",
    isFollowing: false,
    category: "technical",
  },
  6: {
    name: "Indian Society for Technical Education (ISTE)",
    logo: "🎓",
    members: 485,
    description: "Promoting technical education and research",
    isFollowing: false,
    category: "technical",
  },
  7: {
    name: "International Society of Automation (ISA)",
    logo: "🤖",
    members: 392,
    description: "Advancing automation and control systems",
    isFollowing: false,
    category: "technical",
  },
  8: {
    name: "CodeCell++",
    logo: "⌨️",
    members: 421,
    description: "Competitive programming and coding excellence",
    isFollowing: false,
    category: "technical",
  },
  9: {
    name: "Campus Arts Council",
    logo: "🎭",
    members: 567,
    description: "Curating art, music, and performance events across campus",
    isFollowing: false,
    category: "cultural",
  },
  10: {
    name: "Literature Council",
    logo: "📚",
    members: 298,
    description: "Promoting literary arts and creative writing",
    isFollowing: false,
    category: "cultural",
  },
  11: {
    name: "Sports Council",
    logo: "⚽",
    members: 634,
    description: "Managing sports activities and tournaments",
    isFollowing: false,
    category: "sports",
  },
  12: {
    name: "E-Cell",
    logo: "💡",
    members: 456,
    description: "Fostering entrepreneurship and innovation",
    isFollowing: false,
    category: "entrepreneurship",
  },
  13: {
    name: "HABIT (Startup Incubation)",
    logo: "🚀",
    members: 234,
    description: "Incubating student startups and ventures",
    isFollowing: false,
    category: "entrepreneurship",
  },
  14: {
    name: "Training and Placement Cell (TPC)",
    logo: "💼",
    members: 892,
    description: "Career guidance and placement opportunities",
    isFollowing: false,
    category: "career",
  },
};

type Announcement = {
  id: number;
  title: string;
  postedBy: string;
  date: string;
  description: string;
  important: boolean;
};

type CouncilEvent = {
  id: number;
  title: string;
  date: string;
  time: string;
  location: string;
  image: string;
};

type Poll = {
  id: number;
  question: string;
  options: { text: string; votes: number }[];
  closingDate: string;
  totalVotes: number;
  anonymous: boolean;
};

type ResourceItem = {
  name: string;
  fileCount: number;
  icon: string;
};

type CouncilPageContent = {
  announcements: Announcement[];
  councilEvents: CouncilEvent[];
  polls: Poll[];
  resources: ResourceItem[];
};

const councilContentTemplates: Record<
  string,
  (name: string) => CouncilPageContent
> = {
  student: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Town Hall Scheduled`,
        postedBy: name,
        date: "Mar 20, 2026",
        description: `The ${name} will discuss campus life initiatives, student feedback, and upcoming events.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Student Government Election Updates`,
        postedBy: "Elections Committee",
        date: "Mar 18, 2026",
        description: `Candidate profiles and voting instructions for the student government have been published.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Wellness Week Launch`,
        postedBy: "Campus Health",
        date: "Mar 14, 2026",
        description: `Wellness Week will include mental health workshops, sports activities, and peer mentoring sessions.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} Town Hall`,
        date: "Mar 21, 2026",
        time: "5:00 PM",
        location: "Main Auditorium",
        image: "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Campus Life Fair`,
        date: "Mar 24, 2026",
        time: "3:00 PM",
        location: "Central Plaza",
        image: "https://images.unsplash.com/photo-1523050854058-8df90110c9f1?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Volunteer Drive`,
        date: "Mar 28, 2026",
        time: "11:00 AM",
        location: "Student Centre",
        image: "https://images.unsplash.com/photo-1551434678-e076c223a692?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `Should ${name} extend library hours during exam week?`,
        options: [
          { text: "Yes, until midnight", votes: 240 },
          { text: "Yes, until 11 PM", votes: 160 },
          { text: "Keep current hours", votes: 48 },
        ],
        closingDate: "Mar 18, 2026",
        totalVotes: 448,
        anonymous: true,
      },
      {
        id: 2,
        question: `Which topic should ${name} cover in the next student forum?`,
        options: [
          { text: "Campus safety", votes: 302 },
          { text: "Mental health", votes: 260 },
          { text: "Career support", votes: 198 },
        ],
        closingDate: "Mar 22, 2026",
        totalVotes: 760,
        anonymous: false,
      },
    ],
    resources: [
      { name: "Student Handbook", fileCount: 18, icon: "📘" },
      { name: "Campus Event Guides", fileCount: 32, icon: "📅" },
      { name: "Council Meeting Minutes", fileCount: 14, icon: "📝" },
      { name: "Student Feedback Reports", fileCount: 9, icon: "📄" },
    ],
  }),
  technical: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Hackathon Registration Open`,
        postedBy: name,
        date: "Mar 22, 2026",
        description: `Register now for the ${name} hackathon focused on AI, blockchain, and cybersecurity.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Industry Mentor Sessions`,
        postedBy: "Tech Partnerships",
        date: "Mar 19, 2026",
        description: `Mentor sessions with alumni engineers are available for project guidance and career advice.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Lab Access Policy Updated`,
        postedBy: "Lab Administration",
        date: "Mar 15, 2026",
        description: `New access timings and equipment booking details for the technology labs have been released.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} AI Innovation Workshop`,
        date: "Mar 25, 2026",
        time: "4:00 PM",
        location: "Tech Lab B",
        image: "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Code Sprint`,
        date: "Mar 27, 2026",
        time: "6:00 PM",
        location: "Innovation Hub",
        image: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Robotics Showcase`,
        date: "Mar 30, 2026",
        time: "2:00 PM",
        location: "Engineering Block",
        image: "https://images.unsplash.com/photo-1518779578993-ec3579fee39f?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `Which ${name} workshop should be next?`,
        options: [
          { text: "AI & ML", votes: 320 },
          { text: "Cybersecurity", votes: 210 },
          { text: "Cloud Computing", votes: 185 },
        ],
        closingDate: "Mar 21, 2026",
        totalVotes: 715,
        anonymous: false,
      },
      {
        id: 2,
        question: `What should ${name} focus on for its next project showcase?`,
        options: [
          { text: "Smart Campus", votes: 278 },
          { text: "Automation", votes: 244 },
          { text: "Sustainable Tech", votes: 201 },
        ],
        closingDate: "Mar 26, 2026",
        totalVotes: 723,
        anonymous: true,
      },
    ],
    resources: [
      { name: "Project Templates", fileCount: 21, icon: "📁" },
      { name: "Lab Manuals", fileCount: 16, icon: "🧪" },
      { name: "Tech Learning Paths", fileCount: 9, icon: "💡" },
      { name: "Research Papers", fileCount: 27, icon: "📄" },
    ],
  }),
  cultural: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Cultural Night Announced`,
        postedBy: name,
        date: "Mar 23, 2026",
        description: `A new cultural night featuring music, dance, and drama has been scheduled by ${name}.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Poetry Open Mic`,
        postedBy: "Creative Events",
        date: "Mar 20, 2026",
        description: `Open mic submissions are open for poets, storytellers, and spoken word artists.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Art Exhibition Call for Entries`,
        postedBy: "Arts Committee",
        date: "Mar 17, 2026",
        description: `Student artists are invited to submit work for the upcoming campus exhibition.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} Music & Dance Showcase`,
        date: "Mar 26, 2026",
        time: "7:00 PM",
        location: "Auditorium",
        image: "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Art Exhibition`,
        date: "Mar 29, 2026",
        time: "4:00 PM",
        location: "Gallery Hall",
        image: "https://images.unsplash.com/photo-1494526585095-c41746248156?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Cultural Trivia Night`,
        date: "Apr 1, 2026",
        time: "6:30 PM",
        location: "Student Lounge",
        image: "https://images.unsplash.com/photo-1472653431158-6364773b2a56?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `Which theme should ${name} choose for the next festival?`,
        options: [
          { text: "Vintage Classics", votes: 180 },
          { text: "Modern Fusion", votes: 288 },
          { text: "Global Cultures", votes: 225 },
        ],
        closingDate: "Mar 25, 2026",
        totalVotes: 693,
        anonymous: false,
      },
      {
        id: 2,
        question: `What should ${name} feature more of?`,
        options: [
          { text: "Live Music", votes: 315 },
          { text: "Dance", votes: 215 },
          { text: "Drama", votes: 186 },
        ],
        closingDate: "Mar 28, 2026",
        totalVotes: 716,
        anonymous: true,
      },
    ],
    resources: [
      { name: "Performance Schedules", fileCount: 13, icon: "🎟️" },
      { name: "Event Posters", fileCount: 22, icon: "🖼️" },
      { name: "Workshop Guides", fileCount: 11, icon: "📘" },
      { name: "Contest Briefs", fileCount: 8, icon: "✍️" },
    ],
  }),
  sports: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Intercollege Match Schedule`,
        postedBy: name,
        date: "Mar 24, 2026",
        description: `Upcoming intercollege matches and tryouts have been announced by ${name}.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Fitness Camp Begins`,
        postedBy: "Athletics Department",
        date: "Mar 21, 2026",
        description: `A new fitness camp for athletes and fitness enthusiasts starts this weekend.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Gym Timings Update`,
        postedBy: "Sports Admin",
        date: "Mar 18, 2026",
        description: `Gym timings and court reservations have been updated for the new semester.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} Football Tournament`,
        date: "Mar 27, 2026",
        time: "5:00 PM",
        location: "Sports Ground",
        image: "https://images.unsplash.com/photo-1508606572321-901ea4437072?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Fitness Bootcamp`,
        date: "Mar 29, 2026",
        time: "7:00 AM",
        location: "Training Field",
        image: "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Table Tennis Finals`,
        date: "Apr 2, 2026",
        time: "3:00 PM",
        location: "Indoor Arena",
        image: "https://images.unsplash.com/photo-1521412644187-c49fa049e84d?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `Which training session should ${name} add next?`,
        options: [
          { text: "Strength Training", votes: 190 },
          { text: "Endurance", votes: 164 },
          { text: "Agility", votes: 132 },
        ],
        closingDate: "Mar 26, 2026",
        totalVotes: 486,
        anonymous: false,
      },
      {
        id: 2,
        question: `Should ${name} host a campus-wide sports festival?`,
        options: [
          { text: "Yes", votes: 341 },
          { text: "Maybe", votes: 112 },
          { text: "No", votes: 54 },
        ],
        closingDate: "Mar 30, 2026",
        totalVotes: 507,
        anonymous: true,
      },
    ],
    resources: [
      { name: "Training Schedules", fileCount: 19, icon: "📋" },
      { name: "Team Rosters", fileCount: 14, icon: "👥" },
      { name: "Field Booking", fileCount: 7, icon: "🏟️" },
      { name: "Match Archives", fileCount: 21, icon: "🏆" },
    ],
  }),
  entrepreneurship: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Startup Pitch Night`,
        postedBy: name,
        date: "Mar 26, 2026",
        description: `The ${name} is hosting a pitch night for early-stage startup ideas.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Mentor Office Hours`,
        postedBy: "Startup Mentors",
        date: "Mar 23, 2026",
        description: `Office hours with founders and investors are available to review your business ideas.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Funding Workshop`,
        postedBy: "Finance Team",
        date: "Mar 19, 2026",
        description: `Learn how to build a pitch deck and approach early-stage investors.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} Pitch Practice`,
        date: "Mar 28, 2026",
        time: "4:00 PM",
        location: "Incubation Lab",
        image: "https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Startup Community Meetup`,
        date: "Mar 31, 2026",
        time: "6:00 PM",
        location: "Innovation Hub",
        image: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Investor Q&A`,
        date: "Apr 3, 2026",
        time: "5:30 PM",
        location: "Conference Room A",
        image: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `What topic should ${name} cover next?`,
        options: [
          { text: "Funding", votes: 260 },
          { text: "Product Market Fit", votes: 224 },
          { text: "Growth Strategy", votes: 198 },
        ],
        closingDate: "Mar 29, 2026",
        totalVotes: 682,
        anonymous: false,
      },
      {
        id: 2,
        question: `Which event format do you prefer for ${name}?`,
        options: [
          { text: "Pitch Contest", votes: 312 },
          { text: "Mentor Panels", votes: 228 },
          { text: "Workshops", votes: 186 },
        ],
        closingDate: "Apr 1, 2026",
        totalVotes: 726,
        anonymous: true,
      },
    ],
    resources: [
      { name: "Pitch Deck Templates", fileCount: 14, icon: "📊" },
      { name: "Mentor Notes", fileCount: 11, icon: "🧠" },
      { name: "Funding Guides", fileCount: 9, icon: "💰" },
      { name: "Startup Case Studies", fileCount: 16, icon: "📄" },
    ],
  }),
  career: (name) => ({
    announcements: [
      {
        id: 1,
        title: `${name} Placement Drive Announced`,
        postedBy: name,
        date: "Mar 25, 2026",
        description: `Major employers will be visiting campus for placement interviews organized by ${name}.`,
        important: true,
      },
      {
        id: 2,
        title: `${name} Resume Clinic Open`,
        postedBy: "Career Services",
        date: "Mar 21, 2026",
        description: `Students can book resume reviews and interview prep sessions for upcoming campus drives.`,
        important: false,
      },
      {
        id: 3,
        title: `${name} Internship Fair Registration`,
        postedBy: "Employer Relations",
        date: "Mar 17, 2026",
        description: `Register now for the internship fair featuring tech, finance, and consulting companies.`,
        important: false,
      },
    ],
    councilEvents: [
      {
        id: 1,
        title: `${name} Resume Workshop`,
        date: "Mar 28, 2026",
        time: "3:00 PM",
        location: "Career Centre",
        image: "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?w=800&auto=format&fit=crop",
      },
      {
        id: 2,
        title: `${name} Interview Prep Session`,
        date: "Mar 30, 2026",
        time: "1:00 PM",
        location: "Training Room",
        image: "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=800&auto=format&fit=crop",
      },
      {
        id: 3,
        title: `${name} Employer Networking Meet`,
        date: "Apr 2, 2026",
        time: "5:00 PM",
        location: "Networking Hall",
        image: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=800&auto=format&fit=crop",
      },
    ],
    polls: [
      {
        id: 1,
        question: `What kind of career event should ${name} host next?`,
        options: [
          { text: "Resume Clinic", votes: 290 },
          { text: "Mock Interviews", votes: 260 },
          { text: "Company Panels", votes: 210 },
        ],
        closingDate: "Mar 27, 2026",
        totalVotes: 760,
        anonymous: false,
      },
      {
        id: 2,
        question: `Which sector should ${name} target for the next employer visit?`,
        options: [
          { text: "Tech", votes: 345 },
          { text: "Finance", votes: 210 },
          { text: "Consulting", votes: 198 },
        ],
        closingDate: "Mar 29, 2026",
        totalVotes: 753,
        anonymous: true,
      },
    ],
    resources: [
      { name: "Resume Templates", fileCount: 20, icon: "📄" },
      { name: "Interview Guides", fileCount: 15, icon: "🗂️" },
      { name: "Company Lists", fileCount: 12, icon: "🏢" },
      { name: "Placement Analytics", fileCount: 8, icon: "📈" },
    ],
  }),
};

function getCouncilPageContent(id: number, council: CouncilInfo): CouncilPageContent {
  const generator = councilContentTemplates[council.category] ?? councilContentTemplates.default;
  return generator(council.name);
}

councilContentTemplates.default = (name) => ({
  announcements: [
    {
      id: 1,
      title: `${name} Update Available`,
      postedBy: name,
      date: "Mar 25, 2026",
      description: `General updates and announcements from ${name}.`,
      important: false,
    },
  ],
  councilEvents: [
    {
      id: 1,
      title: `${name} Community Event`,
      date: "Mar 28, 2026",
      time: "4:00 PM",
      location: "Campus Hall",
      image: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=800&auto=format&fit=crop",
    },
  ],
  polls: [
    {
      id: 1,
      question: `Would you like more updates from ${name}?`,
      options: [
        { text: "Yes", votes: 220 },
        { text: "No", votes: 34 },
        { text: "Maybe", votes: 88 },
      ],
      closingDate: "Mar 30, 2026",
      totalVotes: 342,
      anonymous: true,
    },
  ],
  resources: [
    { name: "Council Overview", fileCount: 7, icon: "📄" },
    { name: "Contact Info", fileCount: 1, icon: "📇" },
  ],
});

export function Council() {
  const { id } = useParams();
  const councilId = Number(id);
  const selectedCouncil = councilInfoMap[councilId] ?? {
    name: "Council Not Found",
    logo: "❓",
    members: 0,
    description: "This council does not exist. Please go back and choose a valid council.",
    isFollowing: false,
    category: "default",
  };

  const { announcements, councilEvents, polls, resources } = getCouncilPageContent(councilId, selectedCouncil);

  const [activeTab, setActiveTab] = useState<CouncilTab>("announcements");
  const [doubtSubmitted, setDoubtSubmitted] = useState(false);
  const [doubtText, setDoubtText] = useState("");
  const [isFollowing, setIsFollowing] = useState(selectedCouncil.isFollowing);

  useEffect(() => {
    setIsFollowing(selectedCouncil.isFollowing);
  }, [selectedCouncil]);

  const councilInfo = selectedCouncil;

  const tabs = [
    { id: "announcements" as CouncilTab, label: "Announcements" },
    { id: "events" as CouncilTab, label: "Events" },
    { id: "polls" as CouncilTab, label: "Polls" },
    { id: "doubts" as CouncilTab, label: "Doubts" },
    { id: "resources" as CouncilTab, label: "Resources" },
  ];

  const handleSubmitDoubt = () => {
    if (doubtText.trim()) {
      setDoubtSubmitted(true);
      setTimeout(() => {
        setDoubtSubmitted(false);
        setDoubtText("");
      }, 3000);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Council Banner */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden mb-8">
            <div className="h-32 bg-gradient-to-r from-[#2563EB] to-[#7C3AED]"></div>
            <div className="px-8 pb-6 -mt-12">
              <div className="flex items-end gap-6">
                <div className="w-24 h-24 bg-white rounded-xl border-4 border-white shadow-lg flex items-center justify-center text-4xl">
                  {councilInfo.logo}
                </div>
                <div className="flex-1 mb-2">
                  <h1 className="text-2xl font-semibold text-gray-900 mb-1">
                    {councilInfo.name}
                  </h1>
                  <p className="text-gray-600 mb-2">{councilInfo.description}</p>
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <Users className="w-4 h-4" />
                    <span>{councilInfo.members} members</span>
                  </div>
                </div>
                <button
                  onClick={() => setIsFollowing(!isFollowing)}
                  className={`px-6 py-2 rounded-lg transition-all duration-150 mb-2 ${
                    isFollowing
                      ? "bg-gray-100 text-gray-700 border border-gray-300 hover:bg-gray-200"
                      : "bg-[#2563EB] text-white hover:bg-blue-600"
                  }`}
                >
                  {isFollowing ? "Following" : "Follow"}
                </button>
              </div>
            </div>
          </div>

          {/* Tabs */}
          <div className="flex gap-8 border-b border-gray-200 mb-8">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`pb-3 px-1 text-sm font-medium transition-colors relative ${
                  activeTab === tab.id
                    ? "text-[#2563EB]"
                    : "text-gray-600 hover:text-gray-900"
                }`}
              >
                {tab.label}
                {activeTab === tab.id && (
                  <div className="absolute bottom-0 left-0 right-0 h-0.5 bg-[#2563EB]"></div>
                )}
              </button>
            ))}
          </div>

          {/* Tab Content */}
          <div>
            {/* Announcements Tab */}
            {activeTab === "announcements" && (
              <div className="space-y-4">
                {announcements.map((announcement) => (
                  <div
                    key={announcement.id}
                    className={`bg-white rounded-xl border shadow-sm p-6 ${
                      announcement.important
                        ? "border-l-4 border-l-[#7C3AED] bg-purple-50/30"
                        : "border-gray-200"
                    }`}
                  >
                    <div className="flex items-start justify-between mb-2">
                      <h3 className="font-semibold text-gray-900 text-lg">
                        {announcement.title}
                      </h3>
                      {announcement.important && (
                        <span className="bg-purple-100 text-[#7C3AED] text-xs px-2.5 py-1 rounded-full font-medium">
                          Important
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-gray-600 mb-3">
                      <span>Posted by {announcement.postedBy}</span>
                      <span>•</span>
                      <span>{announcement.date}</span>
                    </div>
                    <p className="text-gray-700 mb-4">{announcement.description}</p>
                    <button className="text-[#2563EB] text-sm hover:underline">
                      Read More →
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* Events Tab */}
            {activeTab === "events" && (
              <div className="grid grid-cols-1 gap-6">
                {councilEvents.map((event) => (
                  <div
                    key={event.id}
                    className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden hover:shadow-md transition-shadow"
                  >
                    <div className="h-48 bg-gradient-to-br from-blue-400 to-purple-400"></div>
                    <div className="p-6">
                      <h3 className="text-xl font-semibold text-gray-900 mb-3">
                        {event.title}
                      </h3>
                      <div className="space-y-2 text-gray-600 mb-4">
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
                      <div className="flex gap-3">
                        <button className="px-5 py-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors">
                          Enroll
                        </button>
                        <button className="px-5 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
                          Remind Me
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Polls Tab */}
            {activeTab === "polls" && (
              <div className="space-y-6">
                {polls.map((poll) => (
                  <div
                    key={poll.id}
                    className="bg-white rounded-xl border border-gray-200 shadow-sm p-6"
                  >
                    <div className="flex items-start justify-between mb-4">
                      <h3 className="text-lg font-semibold text-gray-900">{poll.question}</h3>
                      {poll.anonymous && (
                        <span className="bg-gray-100 text-gray-600 text-xs px-2.5 py-1 rounded-full">
                          Anonymous
                        </span>
                      )}
                    </div>
                    <div className="space-y-3 mb-4">
                      {poll.options.map((option, idx) => {
                        const percentage = (option.votes / poll.totalVotes) * 100;
                        return (
                          <div key={idx}>
                            <div className="flex items-center justify-between mb-1 text-sm">
                              <span className="text-gray-900">{option.text}</span>
                              <span className="text-gray-600">{percentage.toFixed(1)}%</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
                              <div
                                className="bg-[#2563EB] h-full rounded-full transition-all"
                                style={{ width: `${percentage}%` }}
                              ></div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-gray-600">{poll.totalVotes} votes</span>
                      <span className="text-gray-600">Closes: {poll.closingDate}</span>
                    </div>
                    <button className="mt-4 w-full py-2 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors">
                      Vote
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* Doubts Tab */}
            {activeTab === "doubts" && (
              <div className="max-w-3xl mx-auto">
                <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-8">
                  <div className="mb-6">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">
                      Submit Your Question
                    </h3>
                    <p className="text-sm text-gray-600">
                      Only council members can see your question. You'll be notified when it's answered.
                    </p>
                  </div>
                  {!doubtSubmitted ? (
                    <>
                      <textarea
                        value={doubtText}
                        onChange={(e) => setDoubtText(e.target.value)}
                        placeholder="Type your question or doubt here..."
                        className="w-full h-40 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent resize-none mb-4"
                      />
                      <button
                        onClick={handleSubmitDoubt}
                        className="w-full py-3 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors flex items-center justify-center gap-2"
                      >
                        <Send className="w-5 h-5" />
                        Submit Question
                      </button>
                    </>
                  ) : (
                    <div className="text-center py-8">
                      <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <CheckCircle2 className="w-8 h-8 text-green-600" />
                      </div>
                      <h4 className="text-lg font-semibold text-gray-900 mb-2">
                        Question Submitted!
                      </h4>
                      <p className="text-gray-600">
                        Your question has been sent to the council. You'll be notified when it's answered.
                      </p>
                    </div>
                  )}

                  {/* Status Tracker */}
                  <div className="mt-8 pt-8 border-t border-gray-200">
                    <h4 className="font-semibold text-gray-900 mb-4">Question Status</h4>
                    <div className="space-y-3">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center">
                          <CheckCircle2 className="w-5 h-5 text-green-600" />
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">Submitted</p>
                          <p className="text-xs text-gray-500">Your question was received</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
                          <div className="w-2.5 h-2.5 bg-gray-400 rounded-full"></div>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">In Review</p>
                          <p className="text-xs text-gray-400">Council is reviewing your question</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-gray-200 rounded-full flex items-center justify-center">
                          <div className="w-2.5 h-2.5 bg-gray-400 rounded-full"></div>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-500">Answered</p>
                          <p className="text-xs text-gray-400">You'll receive a notification</p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Resources Tab */}
            {activeTab === "resources" && (
              <div className="grid grid-cols-3 gap-4">
                {resources.map((resource, idx) => (
                  <div
                    key={idx}
                    className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 hover:shadow-md transition-shadow cursor-pointer"
                  >
                    <div className="flex items-center gap-3 mb-3">
                      <div className="text-3xl">{resource.icon}</div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-gray-900">{resource.name}</h3>
                        <p className="text-sm text-gray-600">{resource.fileCount} files</p>
                      </div>
                    </div>
                    <button className="w-full py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm">
                      Open
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

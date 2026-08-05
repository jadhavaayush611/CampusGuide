import { useState } from "react";
import { Header } from "../components/Header";
import { Mail, MapPin, Calendar, Award, Users, Activity, Linkedin, Github, Globe } from "lucide-react";

const stats = [
  { label: "Communities Joined", value: "4", icon: Users, color: "bg-blue-100 text-blue-600" },
  { label: "Events Attended", value: "12", icon: Calendar, color: "bg-purple-100 text-purple-600" },
  { label: "Achievements", value: "8", icon: Award, color: "bg-green-100 text-green-600" },
  { label: "Activity Score", value: "450", icon: Activity, color: "bg-orange-100 text-orange-600" },
];

const achievements = [
  { title: "Early Adopter", description: "Joined CampusGuide in the first month", date: "Jan 2026" },
  { title: "Community Builder", description: "Active member of 5+ communities", date: "Feb 2026" },
  { title: "Event Enthusiast", description: "Attended 10+ events", date: "Feb 2026" },
  { title: "Knowledge Sharer", description: "Contributed 20+ resources", date: "Jan 2026" },
];

const interests = ["Web Development", "Machine Learning", "UI/UX Design", "Competitive Programming", "Cloud Computing"];

export function Profile() {
  const [reminderTiming, setReminderTiming] = useState("30min");

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          {/* Profile Header */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-8 mb-8">
            <div className="flex items-start gap-6">
              <div className="w-24 h-24 rounded-full bg-gradient-to-br from-[#2563EB] to-[#7C3AED] flex items-center justify-center text-white text-3xl font-semibold">
                R
              </div>
              <div className="flex-1">
                <h1 className="text-3xl font-semibold text-gray-900 mb-2">Rohan Sharma</h1>
                <p className="text-gray-600 mb-4">Computer Engineering • Second Year</p>
                <div className="flex gap-6 text-sm">
                  <div className="flex items-center gap-2 text-gray-600">
                    <Mail className="w-4 h-4" />
                    <span>rohan.sharma@ves.ac.in</span>
                  </div>
                  <div className="flex items-center gap-2 text-gray-600">
                    <MapPin className="w-4 h-4" />
                    <span>Mumbai, India</span>
                  </div>
                </div>
              </div>
              <button className="px-6 py-2.5 bg-[#2563EB] text-white rounded-lg hover:bg-blue-600 transition-colors">
                Edit Profile
              </button>
            </div>
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-4 gap-6 mb-8">
            {stats.map((stat, idx) => (
              <div
                key={idx}
                className="bg-white rounded-xl border border-gray-200 shadow-sm p-6"
              >
                <div className={`w-10 h-10 ${stat.color} rounded-lg flex items-center justify-center mb-3`}>
                  <stat.icon className="w-5 h-5" />
                </div>
                <div className="text-2xl font-semibold text-gray-900 mb-1">{stat.value}</div>
                <div className="text-sm text-gray-600">{stat.label}</div>
              </div>
            ))}
          </div>

          {/* Notification Settings */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Notification Preferences</h2>
            <div className="max-w-2xl">
              <fieldset className="mb-4">
                <legend className="block text-sm font-medium text-gray-900 mb-3">
                  Reminder Notifications
                </legend>
                <div className="space-y-2">
                  {[
                    { value: "off", label: "Off" },
                    { value: "30min", label: "30 minutes later" },
                    { value: "2hours", label: "2 hours later" },
                    { value: "6hours", label: "6 hours later" },
                  ].map((option) => (
                    <label
                      key={option.value}
                      className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 cursor-pointer transition-all duration-150"
                    >
                      <input
                        type="radio"
                        name="reminder"
                        value={option.value}
                        checked={reminderTiming === option.value}
                        onChange={(e) => setReminderTiming(e.target.value)}
                        className="w-4 h-4 text-[#2563EB] focus:ring-2 focus:ring-[#2563EB]"
                      />
                      <span className="text-sm text-gray-700">{option.label}</span>
                    </label>
                  ))}
                </div>
              </fieldset>
            </div>
          </div>

          {/* About Section */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">About</h2>
            <p className="text-gray-700 mb-4 leading-relaxed">
              Passionate computer engineering student with a keen interest in web development and machine learning.
              Actively involved in campus technical communities and always eager to learn new technologies and collaborate on innovative projects.
            </p>
            <div className="mb-4">
              <h3 className="text-sm font-semibold text-gray-900 mb-2">Academic Details</h3>
              <p className="text-sm text-gray-600">Branch: Computer Engineering</p>
              <p className="text-sm text-gray-600">Year: Second Year (2024-2028)</p>
            </div>
            <div>
              <h3 className="text-sm font-semibold text-gray-900 mb-3">Interests</h3>
              <div className="flex flex-wrap gap-2">
                {interests.map((interest, idx) => (
                  <span
                    key={idx}
                    className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-xs"
                  >
                    {interest}
                  </span>
                ))}
              </div>
            </div>
          </div>

          {/* Social Links */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 mb-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Social Links</h2>
            <div className="space-y-3">
              <a
                href="https://linkedin.com/in/darshan-kankekar"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-3 text-gray-700 hover:text-[#2563EB] transition-all duration-150"
              >
                <Linkedin className="w-5 h-5" />
                <span className="text-sm">linkedin.com/in/rohan-sharma</span>
              </a>
              <a
                href="https://github.com/rohan-sharma"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-3 text-gray-700 hover:text-[#2563EB] transition-all duration-150"
              >
                <Github className="w-5 h-5" />
                <span className="text-sm">github.com/rohan-sharma</span>
              </a>
              <a
                href="https://rohan-sharma.dev"
                target="_blank"
                rel="noopener noreferrer"
                className="flex items-center gap-3 text-gray-700 hover:text-[#2563EB] transition-all duration-150"
              >
                <Globe className="w-5 h-5" />
                <span className="text-sm">rohan-sharma.dev</span>
              </a>
            </div>
          </div>

          {/* Achievements */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">Achievements</h2>
            <div className="grid grid-cols-2 gap-4">
              {achievements.map((achievement, idx) => (
                <div
                  key={idx}
                  className="flex items-start gap-3 p-4 bg-gray-50 rounded-lg"
                >
                  <div className="w-10 h-10 bg-gradient-to-br from-yellow-400 to-orange-500 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Award className="w-5 h-5 text-white" />
                  </div>
                  <div className="flex-1">
                    <h4 className="font-semibold text-gray-900 mb-1">{achievement.title}</h4>
                    <p className="text-sm text-gray-600 mb-1">{achievement.description}</p>
                    <p className="text-xs text-gray-500">{achievement.date}</p>
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

import { Award, Mail, ShieldCheck, UserCheck } from 'lucide-react';
import { CouncilLeadershipMember } from '../../../models/council.model';

interface CouncilLeadershipProps {
  leadership: CouncilLeadershipMember[];
  facultyAdvisor: string;
}

export function CouncilLeadership({ leadership, facultyAdvisor }: CouncilLeadershipProps) {
  const facultyList = leadership.filter((m) => m.category === 'FACULTY_ADVISOR');
  const chairList = leadership.filter((m) => m.category === 'CHAIR');
  const officersList = leadership.filter((m) => m.category === 'OFFICER');

  return (
    <div className="space-y-8">
      {/* Governance Banner Notice */}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200 rounded-xl p-5 flex items-start gap-4">
        <div className="w-10 h-10 bg-[#2563EB] text-white rounded-lg flex items-center justify-center flex-shrink-0 shadow-xs">
          <ShieldCheck className="w-5 h-5" />
        </div>
        <div>
          <h3 className="font-semibold text-gray-900 text-base">Constitutional Governance Hierarchy</h3>
          <p className="text-sm text-gray-600 leading-relaxed mt-0.5">
            This council operates under the official Student Senate Constitution. Council leadership positions are election-appointed or faculty-nominated, possessing administrative oversight and budget authorization distinct from informal social clubs.
          </p>
        </div>
      </div>

      {/* Faculty Advisor Section */}
      <div>
        <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
          <UserCheck className="w-5 h-5 text-[#2563EB]" />
          Faculty Leadership & Advisor
        </h3>
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-6">
          {facultyList.length > 0 ? (
            facultyList.map((advisor) => (
              <div key={advisor.id} className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-14 h-14 bg-blue-100 text-[#2563EB] rounded-full flex items-center justify-center text-xl font-bold border-2 border-blue-200">
                    {advisor.name.charAt(0)}
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="font-bold text-gray-900 text-lg">{advisor.name}</h4>
                      <span className="text-xs bg-purple-100 text-purple-800 px-2.5 py-0.5 rounded-full font-semibold">
                        Faculty Advisor
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{advisor.title}</p>
                    <p className="text-xs text-gray-500">{advisor.department}</p>
                  </div>
                </div>

                {advisor.email && (
                  <a
                    href={`mailto:${advisor.email}`}
                    className="px-4 py-2 border border-gray-200 text-gray-700 hover:bg-gray-50 rounded-lg text-sm flex items-center gap-2 font-medium transition-colors"
                  >
                    <Mail className="w-4 h-4 text-gray-400" />
                    Contact Advisor
                  </a>
                )}
              </div>
            ))
          ) : (
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-blue-100 text-[#2563EB] rounded-full flex items-center justify-center text-lg font-bold">
                🎓
              </div>
              <div>
                <h4 className="font-bold text-gray-900">{facultyAdvisor}</h4>
                <p className="text-sm text-gray-600">Faculty Sponsor & Overseer</p>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Executive Board (Chair & President) */}
      <div>
        <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
          <Award className="w-5 h-5 text-amber-500" />
          Executive Officers & Council Chair
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {chairList.map((chair) => (
            <div key={chair.id} className="bg-white rounded-xl border border-gray-200 shadow-sm p-6 relative">
              <div className="flex items-start gap-4">
                <div className="w-14 h-14 bg-gradient-to-br from-amber-100 to-amber-200 text-amber-800 rounded-xl flex items-center justify-center text-xl font-bold border border-amber-300">
                  👑
                </div>
                <div className="flex-1">
                  <span className="text-xs bg-amber-100 text-amber-800 px-2.5 py-0.5 rounded-full font-bold inline-block mb-1">
                    {chair.role}
                  </span>
                  <h4 className="font-bold text-gray-900 text-lg">{chair.name}</h4>
                  <p className="text-sm text-gray-600 font-medium">{chair.title}</p>
                  <p className="text-xs text-gray-500 mt-1">{chair.department}</p>

                  {chair.bio && <p className="text-sm text-gray-600 mt-3 pt-3 border-t border-gray-100">{chair.bio}</p>}

                  {chair.email && (
                    <div className="mt-4 flex items-center gap-2 text-xs text-gray-500">
                      <Mail className="w-3.5 h-3.5" />
                      <a href={`mailto:${chair.email}`} className="hover:text-[#2563EB]">
                        {chair.email}
                      </a>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Council Officers List */}
      {officersList.length > 0 && (
        <div>
          <h3 className="text-lg font-bold text-gray-900 mb-4">Council Board & Committee Leads</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {officersList.map((officer) => (
              <div key={officer.id} className="bg-white rounded-xl border border-gray-200 p-5 shadow-xs">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-10 h-10 bg-gray-100 text-gray-700 rounded-lg flex items-center justify-center font-bold text-sm">
                    {officer.name.charAt(0)}
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900 text-base">{officer.name}</h4>
                    <span className="text-xs bg-gray-100 text-gray-700 px-2 py-0.5 rounded font-medium">
                      {officer.role}
                    </span>
                  </div>
                </div>
                <p className="text-xs text-gray-500">{officer.department}</p>
                {officer.bio && <p className="text-xs text-gray-600 mt-2 line-clamp-2">{officer.bio}</p>}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

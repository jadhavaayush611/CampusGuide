import { useState } from 'react';
import { Search, Filter, ShieldCheck, Mail, Calendar, User } from 'lucide-react';
import { useCouncilMembers } from '../../../hooks/council/useCouncilMembers';
import { CouncilRole } from '../../../models/council.model';

interface CouncilMembersProps {
  councilId: string;
}

export function CouncilMembers({ councilId }: CouncilMembersProps) {
  const [query, setQuery] = useState('');
  const [role, setRole] = useState<CouncilRole | 'ALL'>('ALL');

  const { data, isLoading } = useCouncilMembers(councilId, { query, role });
  const members = data?.members || [];

  return (
    <div className="space-y-6">
      {/* Search & Role Filter Bar */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search council members by name, department..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-gray-400" />
          <div className="flex bg-gray-100 p-1 rounded-xl">
            {(['ALL', 'CHAIR', 'OFFICER', 'MEMBER'] as const).map((r) => (
              <button
                key={r}
                onClick={() => setRole(r)}
                className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-all ${
                  role === r ? 'bg-white text-gray-900 shadow-xs' : 'text-gray-600 hover:text-gray-900'
                }`}
              >
                {r}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Member Cards Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 animate-pulse">
          {[1, 2, 3, 4, 5, 6].map((n) => (
            <div key={n} className="bg-white rounded-xl border border-gray-200 p-5 h-32"></div>
          ))}
        </div>
      ) : members.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-md mx-auto">
          <User className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h4 className="text-base font-semibold text-gray-900 mb-1">No members found</h4>
          <p className="text-xs text-gray-500">Try clearing search filters or selecting another role filter.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {members.map((member) => (
            <div
              key={member.id}
              className="bg-white rounded-xl border border-gray-200 p-5 shadow-xs hover:shadow-md transition-shadow flex items-start gap-4"
            >
              <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-indigo-600 text-white rounded-xl flex items-center justify-center font-bold text-lg flex-shrink-0 shadow-xs">
                {member.name.charAt(0)}
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-1.5">
                  <h4 className="font-bold text-gray-900 text-base truncate">{member.name}</h4>
                  {member.role === 'CHAIR' && <ShieldCheck className="w-4 h-4 text-amber-500 flex-shrink-0" />}
                </div>

                <span
                  className={`text-xs px-2 py-0.5 rounded font-semibold inline-block my-1 ${
                    member.role === 'CHAIR'
                      ? 'bg-amber-100 text-amber-800'
                      : member.role === 'OFFICER'
                      ? 'bg-purple-100 text-purple-800'
                      : 'bg-gray-100 text-gray-700'
                  }`}
                >
                  {member.roleTitle}
                </span>

                <p className="text-xs text-gray-500 truncate">{member.department}</p>

                <div className="mt-3 pt-2 border-t border-gray-100 flex items-center justify-between text-xs text-gray-400">
                  <div className="flex items-center gap-1">
                    <Mail className="w-3 h-3 text-gray-400" />
                    <a href={`mailto:${member.email}`} className="hover:text-[#2563EB] truncate max-w-[120px]">
                      {member.email}
                    </a>
                  </div>
                  <div className="flex items-center gap-1">
                    <Calendar className="w-3 h-3" />
                    <span>{new Date(member.joinedAt).getFullYear()}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

import { useState } from 'react';
import { FileText, Download, Search, Filter, FolderOpen, Calendar, User } from 'lucide-react';
import { CouncilResource } from '../../../models/council.model';
import { toast } from '../../../core/toast/useToast';

interface CouncilResourcesProps {
  resources: CouncilResource[];
}

const RESOURCE_CATEGORIES = ['All', 'Handbooks', 'Forms', 'Meeting Minutes', 'Templates', 'PDFs', 'Reports'];

export function CouncilResources({ resources }: CouncilResourcesProps) {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');

  const filteredResources = resources.filter((r) => {
    const matchesSearch =
      r.title.toLowerCase().includes(search.toLowerCase()) ||
      r.description?.toLowerCase().includes(search.toLowerCase()) ||
      r.tags?.some((t) => t.toLowerCase().includes(search.toLowerCase()));

    const matchesCat = selectedCategory === 'All' || r.category.toLowerCase() === selectedCategory.toLowerCase();

    return matchesSearch && matchesCat;
  });

  const handleDownload = (resource: CouncilResource) => {
    toast.success(`Downloading ${resource.title} (${resource.fileType})`);
  };

  return (
    <div className="space-y-6">
      {/* Search & Category Filter */}
      <div className="flex flex-col md:flex-row gap-4 justify-between items-stretch md:items-center">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search handbooks, forms, meeting minutes..."
            className="w-full pl-10 pr-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-[#2563EB] focus:border-transparent transition-all"
          />
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          <Filter className="w-4 h-4 text-gray-400 flex-shrink-0" />
          {RESOURCE_CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-colors whitespace-nowrap ${
                selectedCategory === cat
                  ? 'bg-[#2563EB] text-white'
                  : 'bg-white text-gray-700 border border-gray-200 hover:bg-gray-50'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid of Resource Cards */}
      {filteredResources.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center max-w-md mx-auto">
          <FolderOpen className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h4 className="text-base font-semibold text-gray-900 mb-1">No documents found</h4>
          <p className="text-xs text-gray-500">Try broadening your search query or selecting another category.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredResources.map((res) => {
            const dateStr = new Date(res.createdAt).toLocaleDateString('en-US', {
              month: 'short',
              day: 'numeric',
              year: 'numeric',
            });

            return (
              <div
                key={res.id}
                className="bg-white rounded-xl border border-gray-200 p-5 shadow-xs hover:shadow-md transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-start justify-between gap-3 mb-3">
                    <div className="w-10 h-10 bg-blue-50 text-[#2563EB] rounded-lg flex items-center justify-center font-bold text-[#2563EB] flex-shrink-0">
                      <FileText className="w-5 h-5" />
                    </div>
                    <span className="text-xs bg-gray-100 text-gray-700 px-2.5 py-0.5 rounded font-semibold">
                      {res.category}
                    </span>
                  </div>

                  <h4 className="font-bold text-gray-900 text-base mb-1 leading-snug line-clamp-2">{res.title}</h4>
                  {res.description && (
                    <p className="text-xs text-gray-600 mb-4 line-clamp-2 leading-relaxed">{res.description}</p>
                  )}
                </div>

                <div>
                  {/* File Metadata */}
                  <div className="flex items-center justify-between text-xs text-gray-500 pt-3 border-t border-gray-100 mb-3">
                    <div className="flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-gray-400" />
                      <span>{res.uploaderName}</span>
                    </div>
                    <span className="font-medium text-gray-700 uppercase bg-gray-100 px-2 py-0.5 rounded">
                      {res.fileType} • {res.fileSize}
                    </span>
                  </div>

                  <button
                    onClick={() => handleDownload(res)}
                    className="w-full py-2 bg-gray-50 hover:bg-[#2563EB] hover:text-white border border-gray-200 hover:border-transparent text-gray-700 rounded-lg text-xs font-semibold transition-all flex items-center justify-center gap-2"
                  >
                    <Download className="w-3.5 h-3.5" />
                    Download File
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}

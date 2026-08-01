import React, { useState, memo, useCallback, useMemo } from 'react';
import { useAtlasSearch } from '../../../hooks/atlas/useAtlasSearch';
import { useRouteCalculation } from '../../../hooks/atlas/useRouteCalculation';
import { useBuildings } from '../../../hooks/campus/useBuildings';
import { MapPin, Search, Navigation, Building2, ArrowRight } from 'lucide-react';

export const AtlasWidget: React.FC = memo(function AtlasWidget() {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'search' | 'route' | 'recent'>('search');

  // Quick route coordinates state
  const [routeParams, setRouteParams] = useState<{
    originLat?: number;
    originLng?: number;
    destLat?: number;
    destLng?: number;
  }>({});

  // React Query hooks
  const { data: searchResults = [], isLoading: isSearching } = useAtlasSearch({ query: searchQuery });
  const { data: buildings = [] } = useBuildings();
  const { data: calculatedRoute } = useRouteCalculation({
    originLat: routeParams.originLat || 0,
    originLng: routeParams.originLng || 0,
    destLat: routeParams.destLat || 0,
    destLng: routeParams.destLng || 0,
    enabled: Boolean(routeParams.originLat && routeParams.destLat),
  });

  const topBuildings = useMemo(() => buildings.slice(0, 3), [buildings]);

  const handleQuickRoute = useCallback((buildingLat: number, buildingLng: number) => {
    // Default origin: Main Campus Entrance
    setRouteParams({
      originLat: 19.0465,
      originLng: 72.8891,
      destLat: buildingLat,
      destLng: buildingLng,
    });
    setActiveTab('route');
  }, []);

  return (
    <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-sm space-y-5">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-cyan-50 flex items-center justify-center text-cyan-600">
            <MapPin className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900">Atlas Wayfinding</h3>
            <p className="text-xs text-gray-500">Quick campus search & route navigation</p>
          </div>
        </div>

        {/* Quick Tab Selector */}
        <div className="flex bg-gray-100 p-1 rounded-xl gap-1 text-xs">
          <button
            onClick={() => setActiveTab('search')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
              activeTab === 'search' ? 'bg-white text-cyan-700 shadow-xs' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Search Campus
          </button>
          <button
            onClick={() => setActiveTab('route')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
              activeTab === 'route' ? 'bg-white text-cyan-700 shadow-xs' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Calculate Route
          </button>
          <button
            onClick={() => setActiveTab('recent')}
            className={`px-3 py-1.5 rounded-lg font-medium transition-all ${
              activeTab === 'recent' ? 'bg-white text-cyan-700 shadow-xs' : 'text-gray-600 hover:text-gray-900'
            }`}
          >
            Recent Locations
          </button>
        </div>
      </div>

      {/* Tab 1: Search Campus */}
      {activeTab === 'search' && (
        <div className="space-y-4">
          <div className="relative">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
            <input
              type="text"
              placeholder="Search buildings, rooms, or landmarks (e.g. CS Lab 401, Library)..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-xl text-xs focus:ring-2 focus:ring-cyan-500 focus:bg-white focus:outline-none"
            />
          </div>

          {searchQuery && (
            <div className="bg-slate-50 border border-slate-200 rounded-xl p-3 max-h-48 overflow-y-auto space-y-2">
              {isSearching ? (
                <div className="py-4 text-center text-xs text-gray-500">Searching campus map...</div>
              ) : searchResults.length === 0 ? (
                <div className="py-4 text-center text-xs text-gray-500">No spatial locations found matching "{searchQuery}"</div>
              ) : (
                searchResults.map((res) => (
                  <div
                    key={res.id}
                    onClick={() => handleQuickRoute(res.latitude, res.longitude)}
                    className="bg-white p-2.5 rounded-lg border border-gray-200 flex items-center justify-between text-xs hover:border-cyan-400 cursor-pointer"
                  >
                    <div>
                      <span className="font-bold text-gray-900">{res.title}</span>
                      {res.subtitle && <p className="text-[11px] text-gray-500">{res.subtitle}</p>}
                    </div>
                    <span className="text-[10px] bg-cyan-50 text-cyan-700 px-2 py-0.5 rounded font-semibold flex items-center gap-1">
                      <Navigation className="w-3 h-3" />
                      Route
                    </span>
                  </div>
                ))
              )}
            </div>
          )}

          {!searchQuery && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              {topBuildings.map((b) => (
                <div
                  key={b.id}
                  onClick={() => handleQuickRoute(b.latitude, b.longitude)}
                  className="bg-gray-50 p-3 rounded-xl border border-gray-200/80 hover:border-cyan-300 hover:bg-cyan-50/20 cursor-pointer transition-colors text-xs"
                >
                  <div className="flex items-center gap-2 font-bold text-gray-900 mb-1">
                    <Building2 className="w-4 h-4 text-cyan-600" />
                    <span className="truncate">{b.name}</span>
                  </div>
                  <p className="text-[11px] text-gray-500 line-clamp-1">{b.description || `Building Code: ${b.code}`}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Tab 2: Calculate Route */}
      {activeTab === 'route' && (
        <div className="bg-slate-50 border border-slate-200 rounded-xl p-4 space-y-3 text-xs">
          {calculatedRoute ? (
            <div className="space-y-3">
              <div className="flex items-center justify-between font-bold text-gray-900 border-b border-slate-200 pb-2">
                <span>Route: {calculatedRoute.origin.name} → {calculatedRoute.destination.name}</span>
                <span className="text-cyan-700 bg-cyan-100 px-2 py-0.5 rounded">
                  {Math.round(calculatedRoute.totalDistanceMeters)}m ({Math.round(calculatedRoute.totalDurationSeconds / 60)} min)
                </span>
              </div>
              <div className="space-y-1.5 max-h-36 overflow-y-auto pr-1">
                {calculatedRoute.steps.map((step) => (
                  <div key={step.stepNumber} className="bg-white p-2 rounded border border-slate-200 flex items-center justify-between">
                    <span>{step.stepNumber}. {step.instruction}</span>
                    <span className="text-gray-500 font-semibold">{step.distanceMeters}m</span>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="py-6 text-center text-gray-500">
              <Navigation className="w-6 h-6 text-cyan-400 mx-auto mb-2" />
              <p className="font-semibold text-gray-700">Select a landmark or building to compute route</p>
              <p className="text-[11px] text-gray-500 mt-1">Computes step-by-step path coordinates and distance.</p>
            </div>
          )}
        </div>
      )}

      {/* Tab 3: Recent Locations */}
      {activeTab === 'recent' && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
          <div
            onClick={() => handleQuickRoute(19.0465, 72.8891)}
            className="bg-gray-50 p-3 rounded-xl border border-gray-200 flex items-center justify-between cursor-pointer hover:border-cyan-300"
          >
            <div>
              <p className="font-bold text-gray-900">Central Library</p>
              <p className="text-[11px] text-gray-500">Building B • Floor 2</p>
            </div>
            <ArrowRight className="w-4 h-4 text-cyan-600" />
          </div>

          <div
            onClick={() => handleQuickRoute(19.0470, 72.8895)}
            className="bg-gray-50 p-3 rounded-xl border border-gray-200 flex items-center justify-between cursor-pointer hover:border-cyan-300"
          >
            <div>
              <p className="font-bold text-gray-900">Main Auditorium</p>
              <p className="text-[11px] text-gray-500">Building A • Ground Floor</p>
            </div>
            <ArrowRight className="w-4 h-4 text-cyan-600" />
          </div>
        </div>
      )}
    </div>
  );
});

import { Header } from '../components/Header';
import { ErrorBoundary } from '../../core/errors/ErrorBoundary';
import { CouncilDiscovery } from '../components/councils/CouncilDiscovery';

export function Councils() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <main className="p-8">
        <div className="max-w-[1440px] mx-auto">
          <ErrorBoundary>
            <CouncilDiscovery />
          </ErrorBoundary>
        </div>
      </main>
    </div>
  );
}

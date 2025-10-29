import { Suspense } from 'react';
import { NavLink, Route, Routes } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ProjectDashboard } from './pages/ProjectDashboard';
import { BoardDesigner } from './pages/BoardDesigner';
import { CalculationsChecklist } from './pages/CalculationsChecklist';

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  `px-4 py-2 rounded-md text-sm font-medium transition-colors ${
    isActive ? 'bg-primary text-white' : 'text-slate-600 hover:bg-slate-200'
  }`;

function App() {
  const { t, i18n } = useTranslation();

  const toggleLanguage = () => {
    i18n.changeLanguage(i18n.language === 'es' ? 'en' : 'es');
  };

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="bg-white shadow-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <h1 className="text-xl font-semibold text-slate-900">Electricista Cat III</h1>
          <nav className="flex items-center gap-4">
            <NavLink to="/" className={navLinkClass} end>
              {t('dashboard')}
            </NavLink>
            <NavLink to="/designer" className={navLinkClass}>
              {t('boardDesigner')}
            </NavLink>
            <NavLink to="/calculations" className={navLinkClass}>
              {t('calculations')}
            </NavLink>
            <button
              type="button"
              onClick={toggleLanguage}
              className="rounded-md border border-slate-300 px-3 py-2 text-sm"
            >
              {i18n.language === 'es' ? 'EN' : 'ES'}
            </button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">
        <Suspense fallback={<div>Loading...</div>}>
          <Routes>
            <Route path="/" element={<ProjectDashboard />} />
            <Route path="/designer" element={<BoardDesigner />} />
            <Route path="/calculations" element={<CalculationsChecklist />} />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}

export default App;

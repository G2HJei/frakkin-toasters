import { Navigate, Outlet, Route, Routes } from 'react-router';
import { useMe } from './api/queries';
import { GamesPage } from './pages/GamesPage';
import { LoginPage } from './pages/LoginPage';
import { NewSandboxPage } from './pages/NewSandboxPage';
import { SettingsPage } from './pages/SettingsPage';
import { TablePage } from './pages/TablePage';
import { TopNav } from './pages/TopNav';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<LoggedIn />}>
        <Route element={<WithTopNav />}>
          <Route path="/" element={<GamesPage />} />
          <Route path="/games" element={<GamesPage />} />
          <Route path="/sandbox/new" element={<NewSandboxPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
        {/* a game's own tab: its room shows the top bar, the table doesn't */}
        <Route path="/games/:id" element={<TablePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

// A user who isn't logged in goes to the login page.
function LoggedIn() {
  const me = useMe();
  if (me.isPending) {
    return <p className="muted page">Loading…</p>;
  }
  if (!me.data) {
    return <Navigate to="/login" replace />;
  }
  return <Outlet />;
}

function WithTopNav() {
  return (
    <>
      <TopNav />
      <Outlet />
    </>
  );
}

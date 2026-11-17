import { Link, Navigate, Outlet, Route, Routes } from 'react-router';
import { useLogout, useMe } from './api/queries';
import { GamesPage } from './pages/GamesPage';
import { LoginPage } from './pages/LoginPage';
import { NewSandboxPage } from './pages/NewSandboxPage';
import { SettingsPage } from './pages/SettingsPage';
import { TablePage } from './pages/TablePage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<LoggedIn />}>
        <Route path="/" element={<GamesPage />} />
        <Route path="/games" element={<GamesPage />} />
        <Route path="/games/:id" element={<TablePage />} />
        <Route path="/sandbox/new" element={<NewSandboxPage />} />
        <Route path="/settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

// A user who isn't logged in goes to the login page.
function LoggedIn() {
  const me = useMe();
  const logout = useLogout();
  if (me.isPending) {
    return <p className="muted page">Loading…</p>;
  }
  if (!me.data) {
    return <Navigate to="/login" replace />;
  }
  return (
    <>
      <nav className="top">
        <Link to="/" className="brand">
          <img src="/logo.png" alt="" />
          Frakkin Toasters
        </Link>
        <span className="spacer" />
        <Link to="/settings">{me.data.name}</Link>
        <button type="button" className="link" onClick={() => logout.mutate()}>
          Log out
        </button>
      </nav>
      <Outlet />
    </>
  );
}

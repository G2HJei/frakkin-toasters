import { Navigate, Outlet, Route, Routes } from 'react-router';
import { useMe } from './api/queries';
import { AboutPage } from './pages/AboutPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { NewSandboxPage } from './pages/NewSandboxPage';
import { PlayPage } from './pages/PlayPage';
import { ProfilePage } from './pages/ProfilePage';
import { Site } from './pages/Site';
import { TablePage } from './pages/TablePage';
import { UsersPage } from './pages/UsersPage';

export function App() {
  return (
    <Routes>
      <Route element={<Site />}>
        {/* anyone may read these */}
        <Route path="/" element={<HomePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route element={<LoggedIn />}>
          <Route path="/play" element={<PlayPage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/sandbox/new" element={<NewSandboxPage />} />
        </Route>
      </Route>
      {/* a game's own tab: its room shows the site's header and footer, the table doesn't */}
      <Route element={<LoggedIn />}>
        <Route path="/games/:id" element={<TablePage />} />
      </Route>
      <Route path="/games" element={<Navigate to="/play" replace />} />
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

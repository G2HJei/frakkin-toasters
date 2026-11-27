import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { setTabUser } from '../api/devTab';
import { useDevLogin, useLogins, useMe } from '../api/queries';

const PROVIDER_NAMES: Record<string, string> = { google: 'Google', discord: 'Discord' };

/**
 * A first login registers the user, who accepts the terms with it.
 */
export function LoginPage() {
  const me = useMe();
  const logins = useLogins();
  const devLogin = useDevLogin();
  const navigate = useNavigate();
  const client = useQueryClient();
  const [name, setName] = useState('');
  // one browser, several users: this tab logs in alone, the others keep theirs
  const [tabOnly, setTabOnly] = useState(false);

  if (me.data) {
    return <Navigate to="/play" replace />;
  }
  return (
    <main className="page login">
      <section className="panel cf login-panel">
        <img className="login-logo" src="/logo.svg" alt="" />
        <h1>Log in or register</h1>
        <p className="muted">Log in to host, join and watch games, or to play a sandbox. Your first login registers you.</p>
        <div className="providers">
          {(logins.data?.providers ?? []).map((provider) => (
            <a key={provider} className="button primary" href={`/oauth2/authorization/${provider}`}>
              Log in with {PROVIDER_NAMES[provider] ?? provider}
            </a>
          ))}
        </div>
        {logins.data?.devLogin && (
          <form
            className="dev-login"
            onSubmit={(event) => {
              event.preventDefault();
              if (tabOnly) {
                setTabUser(name.trim());
                void client.invalidateQueries().then(() => navigate('/play'));
                return;
              }
              devLogin.mutate(name, { onSuccess: () => navigate('/play') });
            }}
          >
            <label className="field">
              <span className="field-name">Name</span>
              <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Your name" aria-label="name" />
            </label>
            <label className="field" title="Another tab of this browser can log in as someone else: try a game with several users">
              <input type="checkbox" checked={tabOnly} onChange={(event) => setTabOnly(event.target.checked)} aria-label="this tab only" />
              <span>This tab only</span>
            </label>
            <button type="submit" className="primary" disabled={!name.trim() || devLogin.isPending}>
              Log in (dev)
            </button>
            {devLogin.error && <p className="error">{devLogin.error.message}</p>}
          </form>
        )}
        {logins.data && logins.data.providers.length === 0 && !logins.data.devLogin && (
          <p className="error">No login is configured on this server.</p>
        )}
        <p className="consent small">
          By registering you accept the <Link to="/about#terms">Terms of Service</Link> and confirm that you own a physical copy of the
          Battlestar Galactica board game. How we handle your data: <Link to="/about#privacy">Privacy Policy</Link>.
        </p>
      </section>
    </main>
  );
}

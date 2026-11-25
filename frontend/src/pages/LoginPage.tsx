import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { setTabUser } from '../api/devTab';
import { useDevLogin, useLogins, useMe } from '../api/queries';

const PROVIDER_NAMES: Record<string, string> = { google: 'Google', discord: 'Discord' };

/** The logins: a full-page redirect to a provider, or in the dev profile a login by name. */
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
    return <Navigate to="/" replace />;
  }
  return (
    <main className="page login">
      <img className="login-logo" src="/logo.svg" alt="" />
      <h1>Frakkin Toasters</h1>
      <p className="muted">Log in to host, join and watch games, or to play a sandbox.</p>
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
              void client.invalidateQueries().then(() => navigate('/'));
              return;
            }
            devLogin.mutate(name, { onSuccess: () => navigate('/') });
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
    </main>
  );
}

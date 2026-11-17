import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router';
import { useDevLogin, useLogins, useMe } from '../api/queries';

const PROVIDER_NAMES: Record<string, string> = { google: 'Google', discord: 'Discord' };

/** The logins: a full-page redirect to a provider, or in the dev profile a login by name. */
export function LoginPage() {
  const me = useMe();
  const logins = useLogins();
  const devLogin = useDevLogin();
  const navigate = useNavigate();
  const [name, setName] = useState('');

  if (me.data) {
    return <Navigate to="/" replace />;
  }
  return (
    <main className="page login">
      <img className="login-logo" src="/logo.png" alt="" />
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
            devLogin.mutate(name, { onSuccess: () => navigate('/') });
          }}
        >
          <label className="field">
            <span className="field-name">Name</span>
            <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Your name" aria-label="name" />
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

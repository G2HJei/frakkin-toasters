import { useState } from 'react';
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { setTabUser } from '../api/devTab';
import { useDevLogin, useEmailLogin, useLogins, useMe, useRequestLoginCode } from '../api/queries';

const PROVIDER_NAMES: Record<string, string> = { google: 'Google', discord: 'Discord' };

// what went wrong in a provider's login, which comes back to /login?error=...
function loginError(error: string | null): string | null {
  if (error === null) {
    return null;
  }
  if (error === 'unverified-email') {
    return "That account's email address isn't verified: verify it with its provider, or log in with your email below.";
  }
  return "The login didn't work: try again.";
}

/**
 * A first login registers the user, who accepts the terms with it.
 */
export function LoginPage() {
  const me = useMe();
  const logins = useLogins();
  const devLogin = useDevLogin();
  const navigate = useNavigate();
  const client = useQueryClient();
  const [params] = useSearchParams();
  const [name, setName] = useState('');
  // one browser, several users: this tab logs in alone, the others keep theirs
  const [tabOnly, setTabOnly] = useState(false);

  if (me.data) {
    return <Navigate to="/play" replace />;
  }
  const providers = logins.data?.providers ?? [];
  const error = loginError(params.get('error'));
  return (
    <main className="page login">
      <section className="panel cf login-panel">
        <img className="login-logo" src="/logo.svg" alt="" />
        <h1>Log in or register</h1>
        <p className="muted">Log in to host, join and watch games, or to play a sandbox. Your first login registers you.</p>
        {error && <p className="error">{error}</p>}
        <div className="providers">
          {providers.map((provider) => (
            <a key={provider} className="button primary" href={`/oauth2/authorization/${provider}`}>
              <ProviderIcon provider={provider} />
              Log in with {PROVIDER_NAMES[provider] ?? provider}
            </a>
          ))}
        </div>
        {logins.data?.email && (
          <>
            {providers.length > 0 && <p className="login-or">or</p>}
            <EmailLogin onLoggedIn={() => navigate('/play')} />
          </>
        )}
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
        {logins.data && providers.length === 0 && !logins.data.devLogin && !logins.data.email && (
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

/**
 * The provider's own mark, from its brand kit, in public/icons/: the tile shows once the image loads, so a button whose
 * mark isn't there keeps its words alone.
 */
function ProviderIcon({ provider }: { provider: string }) {
  const [loaded, setLoaded] = useState(false);
  return (
    <span className={loaded ? 'provider-icon' : 'provider-icon pending'} data-testid={`${provider}-icon`}>
      <img src={`/icons/${provider}.svg`} alt="" onLoad={() => setLoaded(true)} />
    </span>
  );
}

/**
 * The login by email: the address, then the code sent to it. Asking for a code registers a new address.
 */
function EmailLogin({ onLoggedIn }: { onLoggedIn: () => void }) {
  const request = useRequestLoginCode();
  const login = useEmailLogin();
  const [email, setEmail] = useState('');
  const [sentTo, setSentTo] = useState<string | null>(null);
  const [code, setCode] = useState('');
  const [resent, setResent] = useState(false);

  if (sentTo === null) {
    return (
      <form
        className="email-login"
        onSubmit={(event) => {
          event.preventDefault();
          const address = email.trim();
          request.mutate(address, { onSuccess: () => setSentTo(address) });
        }}
      >
        <label className="field">
          <span className="field-name">Email</span>
          <input
            type="email"
            value={email}
            maxLength={254}
            autoComplete="email"
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            aria-label="email"
          />
        </label>
        <button type="submit" className="primary" disabled={!email.trim() || request.isPending}>
          Email me a code
        </button>
        {request.error && <p className="error">{request.error.message}</p>}
      </form>
    );
  }
  const refusal = login.error ?? request.error;
  return (
    <form
      className="email-login"
      onSubmit={(event) => {
        event.preventDefault();
        login.mutate({ email: sentTo, code }, { onSuccess: onLoggedIn });
      }}
    >
      <p className="muted small">
        We sent a code to <strong>{sentTo}</strong>. It works for 10 minutes.
      </p>
      <label className="field">
        <span className="field-name">Code</span>
        <input
          value={code}
          inputMode="numeric"
          autoComplete="one-time-code"
          autoFocus
          onChange={(event) => setCode(event.target.value.replace(/\D/g, '').slice(0, 6))}
          placeholder="123456"
          aria-label="code"
        />
      </label>
      <button type="submit" className="primary" disabled={code.length !== 6 || login.isPending}>
        Log in
      </button>
      <div className="email-login-actions">
        <button
          type="button"
          className="link"
          disabled={request.isPending}
          onClick={() => {
            login.reset();
            setResent(false);
            request.mutate(sentTo, { onSuccess: () => setResent(true) });
          }}
        >
          Send a new code
        </button>
        <button
          type="button"
          className="link"
          onClick={() => {
            request.reset();
            login.reset();
            setCode('');
            setResent(false);
            setSentTo(null);
          }}
        >
          Use another email
        </button>
      </div>
      {resent && !refusal && <p className="muted small">A new code is on its way.</p>}
      {refusal && <p className="error">{refusal.message}</p>}
    </form>
  );
}

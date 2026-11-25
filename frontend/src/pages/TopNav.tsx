import { Link } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { setTabUser, tabUser } from '../api/devTab';
import { useLogins, useLogout, useMe } from '../api/queries';

/** The bar over the lobby's pages and a game's room: the brand, the user's settings and the logout. The table has none. */
export function TopNav() {
  const me = useMe();
  const logout = useLogout();
  const client = useQueryClient();
  const logins = useLogins();
  const ownUser = tabUser() !== null;
  return (
    <nav className="top">
      <Link to="/" className="brand">
        <img src="/logo.svg" alt="" />
        Frakkin Toasters
      </Link>
      <span className="spacer" />
      <Link to="/settings">{me.data?.name}</Link>
      {ownUser && <span className="muted small">this tab</span>}
      {/* the dev profile: this tab as another user, the browser's other tabs as they are */}
      {logins.data?.devLogin && (
        <button
          type="button"
          className="link"
          onClick={() => {
            const name = window.prompt('Log this tab in as')?.trim();
            if (name) {
              setTabUser(name);
              void client.invalidateQueries();
            }
          }}
        >
          Another user in this tab
        </button>
      )}
      {/* a tab with a user of its own logs only itself out */}
      <button
        type="button"
        className="link"
        onClick={() => {
          if (ownUser) {
            setTabUser(null);
            void client.invalidateQueries();
          } else {
            logout.mutate();
          }
        }}
      >
        Log out
      </button>
    </nav>
  );
}

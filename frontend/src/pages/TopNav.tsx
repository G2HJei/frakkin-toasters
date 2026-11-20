import { Link } from 'react-router';
import { useLogout, useMe } from '../api/queries';

/** The bar over the lobby's pages and a game's room: the brand, the user's settings and the logout. The table has none. */
export function TopNav() {
  const me = useMe();
  const logout = useLogout();
  return (
    <nav className="top">
      <Link to="/" className="brand">
        <img src="/logo.png" alt="" />
        Frakkin Toasters
      </Link>
      <span className="spacer" />
      <Link to="/settings">{me.data?.name}</Link>
      <button type="button" className="link" onClick={() => logout.mutate()}>
        Log out
      </button>
    </nav>
  );
}

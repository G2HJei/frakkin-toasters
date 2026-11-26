import { useEffect, useRef, useState, type ReactNode } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { setTabUser, tabUser } from '../api/devTab';
import { useLogins, useLogout, useMe, type Me } from '../api/queries';

export const REPOSITORY_URL = 'https://github.com/G2HJei/frakkin-toasters';
// the year the site started, the footer's copyright runs from it
const FIRST_YEAR = 2025;

/** The website's pages: the header, the page and the footer. The table has none of them, a game's room has both. */
export function SiteFrame({ children }: { children: ReactNode }) {
  return (
    <div className="site">
      <SiteHeader />
      <div className="site-main">{children}</div>
      <SiteFooter />
    </div>
  );
}

/** The frame as a layout route. */
export function Site() {
  return (
    <SiteFrame>
      <Outlet />
    </SiteFrame>
  );
}

/** The brand to the home page, the site's pages, and the user's menu, or the login while nobody is logged in. */
export function SiteHeader() {
  const me = useMe();
  return (
    <header className="site-header">
      <Link to="/" className="brand">
        <img src="/logo.svg" alt="" />
        <span>Frakkin Toasters</span>
      </Link>
      <nav className="site-nav" aria-label="site">
        <NavLink to="/play">Play</NavLink>
        <NavLink to="/users">Users</NavLink>
        <NavLink to="/about">About</NavLink>
      </nav>
      <span className="spacer" />
      {me.data ? (
        <UserMenu me={me.data} />
      ) : (
        !me.isPending && (
          <Link className="button primary" to="/login">
            Log in
          </Link>
        )
      )}
    </header>
  );
}

/** The user's name, opening the profile, another user in this tab (the dev profile) and the logout. */
function UserMenu({ me }: { me: Me }) {
  const [open, setOpen] = useState(false);
  const root = useRef<HTMLDivElement>(null);
  const logout = useLogout();
  const client = useQueryClient();
  const logins = useLogins();
  const navigate = useNavigate();
  const ownUser = tabUser() !== null;

  // a click anywhere else or Escape closes it
  useEffect(() => {
    if (!open) {
      return;
    }
    const away = (event: PointerEvent) => {
      if (!root.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    const escape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    };
    document.addEventListener('pointerdown', away);
    document.addEventListener('keydown', escape);
    return () => {
      document.removeEventListener('pointerdown', away);
      document.removeEventListener('keydown', escape);
    };
  }, [open]);

  return (
    <div className="user-menu" ref={root}>
      <button type="button" className="user-menu-button" aria-expanded={open} aria-controls="user-menu" onClick={() => setOpen(!open)}>
        <span className="user-menu-name">{me.name}</span>
        {ownUser && <span className="user-menu-tab">this tab</span>}
        <svg className="chevron" viewBox="0 0 10 6" aria-hidden="true">
          <path d="M1 1l4 4 4-4" />
        </svg>
      </button>
      {open && (
        <div id="user-menu" className="user-menu-list cf">
          <Link to="/profile" onClick={() => setOpen(false)}>
            Profile
          </Link>
          {/* the dev profile: this tab as another user, the browser's other tabs as they are */}
          {logins.data?.devLogin && (
            <button
              type="button"
              onClick={() => {
                setOpen(false);
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
            onClick={() => {
              setOpen(false);
              if (ownUser) {
                setTabUser(null);
                void client.invalidateQueries();
              } else {
                // home first, which needs no login, or the page would send the logged out user to the login
                navigate('/');
                logout.mutate();
              }
            }}
          >
            Log out
          </button>
        </div>
      )}
    </div>
  );
}

export function SiteFooter() {
  const year = new Date().getFullYear();
  const items = [
    <span key="copyright">© {year > FIRST_YEAR ? `${FIRST_YEAR} – ${year}` : FIRST_YEAR}</span>,
    <Link key="about" to="/about">
      About
    </Link>,
    <a key="github" href={REPOSITORY_URL} target="_blank" rel="noreferrer">
      GitHub
    </a>,
    // no address yet
    <a key="contact" href="#">
      Contact
    </a>,
  ];
  return (
    <footer className="site-footer">
      {items.flatMap((item, index) =>
        index === 0
          ? [item]
          : [
              <span key={`separator-${index}`} className="separator" aria-hidden="true">
                –
              </span>,
              item,
            ],
      )}
    </footer>
  );
}

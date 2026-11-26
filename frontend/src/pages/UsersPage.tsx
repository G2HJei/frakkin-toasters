import { useMe, useUsers } from '../api/queries';

/** Every user of the site, by name. */
export function UsersPage() {
  const users = useUsers();
  const me = useMe();
  const list = users.data ?? [];

  return (
    <main className="page wide">
      <header className="page-head">
        <h1 className="label">Users</h1>
        {users.data && <p className="muted">{list.length === 1 ? 'One user is aboard.' : `${list.length} users are aboard.`}</p>}
      </header>
      {users.isPending && <p className="muted">Loading…</p>}
      {users.error && <p className="error">{users.error.message}</p>}
      <ul className="user-list" aria-label="users">
        {list.map((user) => (
          <li key={user.id} className={user.id === me.data?.id ? 'cf me' : 'cf'}>
            <span className="monogram" aria-hidden="true">
              {user.name.trim().charAt(0).toUpperCase()}
            </span>
            <span className="user-name">{user.name}</span>
            {user.id === me.data?.id && <span className="badge">you</span>}
          </li>
        ))}
      </ul>
    </main>
  );
}

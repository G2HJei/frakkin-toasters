import { useState } from 'react';
import { useMe, useUpdateMe } from '../api/queries';

/** The user's profile: his name, which everyone else sees, through PUT /api/me. */
export function ProfilePage() {
  const me = useMe();
  const update = useUpdateMe();
  const [name, setName] = useState<string | null>(null);
  const shown = name ?? me.data?.name ?? '';

  return (
    <main className="page narrow">
      <header className="page-head">
        <h1 className="label">Profile</h1>
        <p className="muted">Your name is what the other users see: in the user list, the lobbies, the games and their chat.</p>
      </header>
      <form
        className="panel cf form-panel"
        onSubmit={(event) => {
          event.preventDefault();
          update.mutate({ name: shown, settings: me.data?.settings ?? {} });
        }}
      >
        <label className="field">
          <span className="field-name">Name</span>
          <input value={shown} maxLength={40} onChange={(event) => setName(event.target.value)} aria-label="name" />
        </label>
        <div className="form-actions">
          <button type="submit" className="primary" disabled={update.isPending || !shown.trim()}>
            Save
          </button>
          {update.isSuccess && <span className="muted">Saved.</span>}
        </div>
        {update.error && <p className="error">{update.error.message}</p>}
      </form>
    </main>
  );
}

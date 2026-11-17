import { useState } from 'react';
import { useMe, useUpdateMe } from '../api/queries';

/** The user's name, through PUT /api/me. */
export function SettingsPage() {
  const me = useMe();
  const update = useUpdateMe();
  const [name, setName] = useState<string | null>(null);
  const shown = name ?? me.data?.name ?? '';

  return (
    <main className="page">
      <h1>Settings</h1>
      <form
        className="card"
        onSubmit={(event) => {
          event.preventDefault();
          update.mutate({ name: shown, settings: me.data?.settings ?? {} });
        }}
      >
        <label className="field">
          <span className="field-name">Name</span>
          <input value={shown} onChange={(event) => setName(event.target.value)} aria-label="name" />
        </label>
        <button type="submit" className="primary" disabled={update.isPending || !shown.trim()}>
          Save
        </button>
        {update.isSuccess && <p className="muted">Saved.</p>}
        {update.error && <p className="error">{update.error.message}</p>}
      </form>
    </main>
  );
}

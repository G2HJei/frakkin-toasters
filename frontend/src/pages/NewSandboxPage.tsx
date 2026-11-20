import { useState } from 'react';
import { useNavigate } from 'react-router';
import { useCreateSandbox, useImportSandbox } from '../api/queries';
import { openGameTab } from './gameTab';

/** A new sandbox: 3 to 7 players and an optional seed, or an exported journal to import. It opens in a tab of its own, and this one goes back to the lobby. */
export function NewSandboxPage() {
  const navigate = useNavigate();
  const create = useCreateSandbox();
  const importJournal = useImportSandbox();
  const [players, setPlayers] = useState(4);
  const [seed, setSeed] = useState('');

  return (
    <main className="page">
      <h1>New sandbox</h1>
      <p className="muted">You play every seat and see everything, and you can undo, set up scenarios and export the journal.</p>
      <form
        className="card"
        onSubmit={(event) => {
          event.preventDefault();
          const tab = openGameTab(navigate);
          create.mutate(
            { playerCount: players, seed: seed.trim() === '' ? null : Number(seed) },
            {
              onSuccess: ({ id }) => {
                navigate('/');
                tab.go(id);
              },
              onError: tab.close,
            },
          );
        }}
      >
        <label className="field">
          <span className="field-name">Players</span>
          <select value={players} onChange={(event) => setPlayers(Number(event.target.value))} aria-label="players">
            {[3, 4, 5, 6, 7].map((count) => (
              <option key={count} value={count}>
                {count}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span className="field-name">Seed</span>
          <input value={seed} onChange={(event) => setSeed(event.target.value.replace(/[^0-9]/g, ''))} placeholder="random" aria-label="seed" />
        </label>
        <button type="submit" className="primary" disabled={create.isPending}>
          Start the sandbox
        </button>
        {create.error && <p className="error">{create.error.message}</p>}
      </form>
      <div className="card">
        <label className="field">
          <span className="field-name">Import a journal</span>
          <input
            type="file"
            accept="application/json,.json"
            aria-label="journal file"
            onChange={async (event) => {
              const file = event.target.files?.[0];
              if (file) {
                // the tab opens before the file is read, while the browser still allows it
                const tab = openGameTab(navigate);
                importJournal.mutate(await file.text(), {
                  onSuccess: ({ id }) => {
                    navigate('/');
                    tab.go(id);
                  },
                  onError: tab.close,
                });
              }
            }}
          />
        </label>
        {importJournal.error && <p className="error">{importJournal.error.message}</p>}
      </div>
    </main>
  );
}

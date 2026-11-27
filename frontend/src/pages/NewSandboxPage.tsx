import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { useCreateSandbox, useImportSandbox } from '../api/queries';
import { openGameTab } from './gameTab';

export function NewSandboxPage() {
  const navigate = useNavigate();
  const create = useCreateSandbox();
  const importJournal = useImportSandbox();
  const [players, setPlayers] = useState(4);
  const [seed, setSeed] = useState('');

  return (
    <main className="page narrow">
      <Link className="back-link" to="/play">
        ← Play
      </Link>
      <header className="page-head">
        <h1 className="label">New sandbox</h1>
        <p className="muted">You play every seat and see everything, and you can undo, set up scenarios and export the journal.</p>
      </header>
      <form
        className="panel cf form-panel"
        onSubmit={(event) => {
          event.preventDefault();
          const tab = openGameTab(navigate);
          create.mutate(
            { playerCount: players, seed: seed.trim() === '' ? null : Number(seed) },
            {
              onSuccess: ({ id }) => {
                navigate('/play');
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
      <div className="panel cf form-panel">
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
                    navigate('/play');
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

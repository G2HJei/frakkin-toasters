import { useState } from 'react';
import { useNavigate } from 'react-router';
import { useCreateSandbox } from '../api/queries';
import { typeLabel } from '../labels/describe';
import { useGameStore } from '../live/gameStore';
import type { EventValue } from '../input/eventModel';
import { schema } from '../wire/schema';
import { SetupForm } from './SetupForm';

export interface SandboxToolbarProps {
  gameId: string;
  seed: number | null;
  onUndo(): void;
  onInject(event: EventValue): void;
}

export function SandboxToolbar({ gameId, seed, onUndo, onInject }: SandboxToolbarProps) {
  const game = useGameStore((state) => state.game);
  const version = useGameStore((state) => state.version);
  const navigate = useNavigate();
  const restart = useCreateSandbox();
  const [setup, setSetup] = useState<string>(schema.lists.sandboxEvents[0] ?? '');

  return (
    <section className="panel cf sandbox" aria-label="sandbox">
      <div className="toolbar">
        <button type="button" onClick={onUndo}>
          Undo
        </button>
        <a className="button" href={`/api/games/${gameId}/journal`} download={`sandbox-${gameId}.json`}>
          Export
        </a>
        <span className="muted">seed {seed ?? '—'}</span>
        <button
          type="button"
          disabled={seed === null || restart.isPending}
          onClick={() =>
            restart.mutate(
              { playerCount: game?.players?.length ?? 3, seed },
              { onSuccess: ({ id }) => navigate(`/games/${id}`) },
            )
          }
        >
          Restart with this seed
        </button>
      </div>
      <details>
        <summary>Setup</summary>
        <label className="field">
          <span className="field-name">Setup</span>
          <select value={setup} onChange={(event) => setSetup(event.target.value)} aria-label="setup">
            {schema.lists.sandboxEvents.map((name) => (
              <option key={name} value={name}>
                {typeLabel(name)}
              </option>
            ))}
          </select>
        </label>
        {setup && <SetupForm key={`${setup}-${version}`} kind={setup} onSubmit={onInject} />}
      </details>
      <details>
        <summary>Inspector</summary>
        <pre className="inspector">{JSON.stringify({ input: game?.input, game }, null, 2)}</pre>
      </details>
    </section>
  );
}

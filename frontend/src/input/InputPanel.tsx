import { optionLabel } from '../labels/describe';
import { useGameStore } from '../live/gameStore';
import { useTable } from '../table/tableContext';
import type { InputProjection } from '../wire/types';
import { EventBuilder } from './EventBuilder';
import type { EventValue } from './eventModel';

export interface InputPanelProps {
  onChoose(option: number): void;
  onSubmit(event: EventValue): void;
}

/**
 * What the game waits for. The asked player gets the input: one button per option of a choice, answered by its index,
 * or the event builder for a decision. The others see who is asked, or a plain "waiting" while it is secret.
 */
export function InputPanel({ onChoose, onSubmit }: InputPanelProps) {
  const table = useTable();
  const rejected = useGameStore((state) => state.rejected);
  const version = useGameStore((state) => state.version);
  const input = table.game?.input ?? null;
  const outcome = table.game?.outcome ?? null;

  return (
    <section className="panel input-panel" aria-label="input">
      <h2>{heading(input, table.playerName)}</h2>
      {outcome && (
        <p className="outcome">
          The {outcome.winner === 'HUMANS' ? 'humans' : 'Cylons'} win
          {outcome.winners && outcome.winners.length > 0 ? `: ${outcome.winners.map(table.playerName).join(', ')}` : ''}.
        </p>
      )}
      {input?.kind === 'Choose' && (
        <div className="options">
          {(input.options ?? []).map((option, index) => (
            <button key={index} type="button" className="option" onClick={() => onChoose(index)}>
              {optionLabel(option, table)}
            </button>
          ))}
        </div>
      )}
      {input?.kind === 'Decide' && input.action && (
        // a new game starts a new form
        <EventBuilder
          key={`${version}-${input.action}`}
          action={input.action}
          playerNumber={input.player}
          constraints={(input.constraints ?? []).filter((constraint): constraint is NonNullable<typeof constraint> => constraint !== null)}
          onSubmit={onSubmit}
        />
      )}
      {rejected && (
        <p className="rejected" role="alert">
          {rejected.reason === 'STALE' ? 'The game moved on: try again.' : rejected.message}
        </p>
      )}
    </section>
  );
}

function heading(input: InputProjection | null, playerName: (playerNumber: number) => string): string {
  switch (input?.kind) {
    case 'Choose':
      return `${playerName(input.player)} chooses`;
    case 'Decide':
      return `${playerName(input.player)} decides`;
    case 'WaitingFor':
      return `Waiting for ${playerName(input.player)}`;
    case 'Waiting':
      return 'Waiting';
    default:
      return 'The game is over';
  }
}

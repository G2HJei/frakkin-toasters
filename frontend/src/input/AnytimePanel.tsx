import { Table, useTable } from '../table/tableContext';
import { Groups } from './AnswerPanel';
import { anytimeMoves } from './answers';
import type { EventValue } from './eventModel';
import { shipsApart, shipsOf } from './shipsApart';

export interface AnytimePanelProps {
  onSubmit(event: EventValue): void;
}

export function AnytimePanel({ onSubmit }: AnytimePanelProps) {
  const table = useTable();
  const actors = (table.game?.anytime ?? [])
    .flatMap((entry) => (entry ? [{ player: entry.player, moves: anytimeMoves(entry.answers) }] : []))
    .filter((actor) => actor.moves.length > 0);
  if (actors.length === 0) {
    return null;
  }
  const sandbox = table.game?.viewer?.kind === 'Sandbox';
  return (
    <section className="panel cf anytime-panel" aria-label="at any time">
      <h2>At any time</h2>
      {actors.map(({ player, moves }) => (
        <div key={player} role="group" aria-label={table.playerName(player)}>
          {/* a sandbox acts for every seat */}
          {sandbox && <p className="pool-name">{table.playerName(player)}</p>}
          {/* the picks don't narrow these: a group opens here */}
          <Table.Provider value={shipsApart(table, shipsOf(moves), table.game?.players?.find((candidate) => candidate.number === player) ?? null)}>
            <Groups moves={moves} onMove={(move) => move.event && onSubmit(move.event)} openInPlace />
          </Table.Provider>
        </div>
      ))}
    </section>
  );
}

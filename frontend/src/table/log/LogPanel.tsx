import { useMemo } from 'react';
import { ArtStrip } from '../../art/Art';
import { factArt, optionArt, recordArt, unique } from '../../art/refs';
import { eventLabel, factLabel, optionLabel, typeLabel } from '../../labels/describe';
import { useGameStore } from '../../live/gameStore';
import type { LogEntry } from '../../wire/types';
import { useTable } from '../tableContext';

interface TurnGroup {
  turn: number;
  entries: LogEntry[];
}

// A new group starts with the scope that records the start of a turn.
export function groupByTurn(entries: LogEntry[]): TurnGroup[] {
  const groups: TurnGroup[] = [{ turn: 0, entries: [] }];
  for (const entry of entries) {
    const started = entry.kind === 'ScopeLog'
      ? (entry.facts ?? []).find((fact) => fact.kind === 'Shown' && fact.fact?.kind === 'GameFact.TurnStarted')
      : undefined;
    if (started && started.kind === 'Shown' && started.fact?.kind === 'GameFact.TurnStarted') {
      groups.push({ turn: started.fact.turn, entries: [] });
    }
    groups[groups.length - 1].entries.push(entry);
  }
  return groups.filter((group) => group.entries.length > 0);
}

/**
 * Every scope with its actor, its event and its facts, grouped by turn, the latest turn open. The cards, characters,
 * locations and ships an entry names show beside it, and the back of the cards a hidden fact counts.
 */
export function LogPanel() {
  const table = useTable();
  const log = useGameStore((state) => state.log);
  const groups = useMemo(() => groupByTurn(log), [log]);
  return (
    <section className="panel cf log" aria-label="log">
      <div className="tab">
        <span>Log</span>
      </div>
      {groups.map((group, index) => (
        <details key={`${group.turn}-${index}`} open={index === groups.length - 1}>
          <summary>{group.turn === 0 ? 'Setup' : `Turn ${group.turn}`}</summary>
          <ol className="entries">
            {group.entries.map((entry, at) => (
              <li key={at} className={entry.kind === 'ChoiceLog' ? 'choice' : 'scope'}>
                {entry.kind === 'ChoiceLog' ? (
                  <span>
                    {table.playerName(entry.chooser)} picks {entry.option ? optionLabel(entry.option, table) : 'an option'}
                    <ArtStrip refs={optionArt(entry.option)} />
                  </span>
                ) : (
                  <>
                    <span className="actor">{entry.actor > 0 ? table.playerName(entry.actor) : 'Game'}</span>{' '}
                    <span className="event">
                      {entry.details ? eventLabel(entry.details as unknown as Record<string, unknown>, table) : typeLabel(entry.event ?? '')}
                    </span>
                    <ArtStrip refs={unique([...recordArt(entry.details as unknown as Record<string, unknown> | null), ...(entry.facts ?? []).flatMap(factArt)])} />
                    {(entry.facts ?? []).length > 0 && (
                      <ul className="facts">
                        {(entry.facts ?? []).map((fact, factIndex) => (
                          <li key={factIndex}>{factLabel(fact, table)}</li>
                        ))}
                      </ul>
                    )}
                  </>
                )}
              </li>
            ))}
          </ol>
        </details>
      ))}
    </section>
  );
}

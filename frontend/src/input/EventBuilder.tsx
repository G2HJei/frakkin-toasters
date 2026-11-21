import { useMemo, useState } from 'react';
import { constantLabel, typeLabel } from '../labels/describe';
import { concreteEvents, recordNamed } from '../wire/schema';
import { useTable } from '../table/tableContext';
import { askedComponents, emptyEvent, missingFields, toWire, type EventValue } from './eventModel';
import { Field } from './widgets/Field';
import type { GameProjection } from '../wire/types';

export interface EventBuilderProps {
  // the event type the decision asks for: a record, or an interface such as ActionEvent
  action: string;
  playerNumber: number | null;
  constraints?: string[];
  submitLabel?: string;
  onSubmit(event: EventValue): void;
}

/**
 * A form made from the schema for the event a decision asks for. It builds exactly the JSON the codec reads; the
 * server fills in the player and the constraints.
 */
export function EventBuilder({ action, playerNumber, constraints = [], submitLabel = 'Send', onSubmit }: EventBuilderProps) {
  const table = useTable();
  const location = table.game?.players?.find((player) => player.number === playerNumber)?.location ?? null;
  const held = heldQuorumCards(table.game, playerNumber).join(',');
  // the action of the player's location first, and a Quorum Card's only when he holds the card
  const kinds = useMemo(() => {
    const events = concreteEvents(action).filter((event) => {
      const card = recordNamed(event)?.quorumCard;
      return !card || held.split(',').includes(card);
    });
    return [...events].sort((a, b) => Number(recordNamed(b)?.location === location) - Number(recordNamed(a)?.location === location));
  }, [action, location, held]);
  const [kind, setKind] = useState<string>(kinds[0] ?? action);
  const [value, setValue] = useState<EventValue>(() => emptyEvent(kinds[0] ?? action, playerNumber));

  const choose = (next: string) => {
    setKind(next);
    setValue(emptyEvent(next, playerNumber));
  };
  const missing = missingFields(value);
  const wanted = expectedCount(constraints);
  const picked = pickedCount(value);

  return (
    <form
      className="event-builder"
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit(toWire(value));
      }}
    >
      {kinds.length > 1 && (
        <label className="field">
          <span className="field-name">Which</span>
          <select value={kind} onChange={(event) => choose(event.target.value)} aria-label="event type">
            {kinds.map((candidate) => (
              <option key={candidate} value={candidate}>
                {typeLabel(candidate)}
                {recordNamed(candidate)?.location ? ` — ${constantLabel('Location', recordNamed(candidate)?.location)}` : ''}
              </option>
            ))}
          </select>
        </label>
      )}
      {kinds.length === 0 && <p className="muted">No event fits {typeLabel(action)}.</p>}
      {constraints.length > 0 && (
        <p className="constraint">
          {constraints.map((constraint) => constantLabel('EventConstraint', constraint)).join(', ')}
          {wanted !== null && picked !== null && <span className={picked === wanted ? 'count-ok' : 'count-off'}> · {picked} of {wanted}</span>}
        </p>
      )}
      {askedComponents(kind).map((component) => (
        <Field
          key={`${kind}.${component.name}`}
          component={component}
          value={value[component.name] ?? null}
          owner={kind}
          onChange={(next) => setValue({ ...value, [component.name]: next })}
        />
      ))}
      <div className="actions">
        <button type="submit" className="primary" disabled={kinds.length === 0}>
          {submitLabel}
        </button>
        {missing.length > 0 && <span className="muted">not set: {missing.join(', ')}</span>}
      </div>
    </form>
  );
}

/** The number of cards a constraint asks for, e.g. 3 for "Draw exactly 3". */
export function expectedCount(constraints: string[]): number | null {
  for (const constraint of constraints) {
    const exactly = /^(DRAW|DISCARD)_EXACTLY_(\d+)$/.exec(constraint);
    if (exactly) {
      return Number(exactly[2]);
    }
  }
  return null;
}

/** How many cards the form picks: the counts per color of a draw, or the cards of a discard. */
export function pickedCount(value: Record<string, unknown>): number | null {
  for (const field of Object.values(value)) {
    if (Array.isArray(field)) {
      return field.length;
    }
    if (field && typeof field === 'object' && !('kind' in field) && Object.values(field).every((count) => typeof count === 'number')) {
      return Object.values(field as Record<string, number>).reduce((sum, count) => sum + count, 0);
    }
  }
  return null;
}

// The Quorum Cards the player may play: the one "Popular Influence" left him, and the President's hand. Only a
// narrowing, the engine judges.
function heldQuorumCards(game: GameProjection | null, playerNumber: number | null): string[] {
  const player = game?.players?.find((candidate) => candidate.number === playerNumber);
  const held = (player?.quorumCards?.cards ?? []).flatMap((card) => (card ? [card] : []));
  if (!player?.character || game?.titles?.president !== player.character) {
    return held;
  }
  return [...held, ...(game.presidentHand?.cards ?? []).flatMap((card) => (card ? [card] : []))];
}

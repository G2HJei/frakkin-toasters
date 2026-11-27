// The ships of a list of answers by their names, told apart where two would read the same. A ship is its type, as its
// id means nothing on the table; two of one type in a list are told apart by where they are, then by their pilot and
// damage, and ships still alike are alike. A civilian ship the deciding player looked at shows what it holds.
import { cargoText } from '../table/board/resources';
import { knowingPlayer, shipMarks, type TableContext } from '../table/tableContext';
import { walkRecord } from '../wire/walk';
import type { Followup, PlayerProjection } from '../wire/types';
import { pickElements, type Move } from './answers';

/** Every ship the moves name, in their events, their options' events and the ships a shape picks from. */
export function shipsOf(moves: Move[]): number[] {
  const ids = new Set<number>();
  const visit = (field: unknown, type: { t: string; role?: string | null }) => {
    if ((type.t === 'int' || type.t === 'long') && type.role === 'ship' && typeof field === 'number') {
      ids.add(field);
    }
  };
  for (const move of moves) {
    for (const event of move.event ? [move.event as unknown as Record<string, unknown>] : followupEvents(move.followup)) {
      walkRecord(event, null, visit);
    }
    if (move.pick?.kind === 'ShipPick') {
      pickElements(move.pick).forEach((id) => ids.add(id as number));
    }
  }
  return [...ids];
}

function followupEvents(followup: Followup | null | undefined): Record<string, unknown>[] {
  switch (followup?.kind) {
    case 'Single':
      return followup.event ? [followup.event as unknown as Record<string, unknown>] : [];
    case 'AllOf':
      return (followup.followups ?? []).flatMap(followupEvents);
    case 'OneOf':
      return (followup.options ?? []).flatMap(followupEvents);
    default:
      return [];
  }
}

/**
 * The table with the ships `ids` named apart from each other: "Raider · Space 7 o'clock". `decider` is who answers,
 * whose looks count: the viewer, the asked player in a sandbox (whose projection holds every cargo).
 */
export function shipsApart(table: TableContext, ids: number[], decider: PlayerProjection | null = knowingPlayer(table)): TableContext {
  if (ids.length === 0) {
    return table;
  }
  const name = (id: number) => table.shipName?.(id) ?? 'a ship';
  const looked = new Set(decider?.knownCivilianShips ?? []);
  const names = new Map(
    ids.map((id) => {
      const cargo = looked.has(id) ? table.ship(id)?.cargo : null;
      return [id, cargo ? `${name(id)} · ${cargoText(cargo)}` : name(id)];
    }),
  );
  const apart = (detail: (id: number) => string | null) => {
    const byName = new Map<string, number[]>();
    for (const id of ids) {
      byName.set(names.get(id)!, [...(byName.get(names.get(id)!) ?? []), id]);
    }
    for (const alike of byName.values()) {
      if (alike.length < 2) {
        continue;
      }
      for (const id of alike) {
        const told = detail(id);
        if (told) {
          names.set(id, `${names.get(id)} · ${told}`);
        }
      }
    }
  };
  apart((id) => table.shipPlace(id));
  apart((id) => {
    const ship = table.ship(id);
    const marks = ship ? shipMarks(ship) : [];
    return marks.length > 0 ? marks.join(' · ') : null;
  });
  return { ...table, shipName: (id) => names.get(id) ?? name(id) };
}

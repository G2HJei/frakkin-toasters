// What the board and the mats offer of the pending input: the locations, ships and players the moves left name, the
// "Act" chip of each location action among them, and the asked player's cards that start a move. A click picks the
// target (selection.ts): one move left is sent, several stay in the panel and a popover next to the click.
import { createContext, useContext } from 'react';
import { recordNamed } from '../wire/schema';
import { narrow, targetKey, targetsOf, type Move, type Target } from './answers';
import type { Anchor } from './selection';

export interface Offer {
  // what lights up, by targetKey, with the moves a click on it keeps
  targets: Map<string, Move[]>;
  // the location actions, by location: the "Act" chip, which picks the action's kind
  acts: Map<string, { kind: string; moves: Move[] }>;
  // what is picked already, by targetKey
  picked: Set<string>;
}

export const NO_OFFER: Offer = { targets: new Map(), acts: new Map(), picked: new Set() };

/**
 * The offer of the moves left after the picks. The board lights the locations, ships and players of one kind of move at
 * a time: the only kind left, or the only one that names any (the Movement step's destinations beside a "Movement:"
 * card). With several, e.g. the Action step, it lights what starts a kind: a location action's "Act" chip and the held
 * cards, which always light.
 */
export function offerOf(all: Move[], picked: Target[]): Offer {
  const left = narrow(all, picked);
  const pickedKeys = new Set(picked.map(targetKey));
  const targets = new Map<string, Move[]>();
  const acts = new Map<string, { kind: string; moves: Move[] }>();
  const add = (target: Target, move: Move) => {
    const key = targetKey(target);
    if (!pickedKeys.has(key)) {
      targets.set(key, [...(targets.get(key) ?? []), move]);
    }
  };

  const byKind = new Map<string, Move[]>();
  for (const move of left) {
    const kind = move.event?.kind;
    if (kind) {
      byKind.set(kind, [...(byKind.get(kind) ?? []), move]);
    }
  }
  const onBoard = [...byKind.entries()].filter(([kind, moves]) => moves.some((move) => boardTargets(move, kind).length > 0)).map(([kind]) => kind);
  const lit = byKind.size === 1 ? [...byKind.keys()] : onBoard.length === 1 ? onBoard : [];

  for (const move of left) {
    const kind = move.event?.kind;
    const ownLocation = kind ? recordNamed(kind)?.location : null;
    if (kind && ownLocation && !pickedKeys.has(`kind:${kind}`)) {
      const act = acts.get(ownLocation);
      acts.set(ownLocation, { kind, moves: [...(act?.moves ?? []), move] });
    }
    for (const target of targetsOf(move)) {
      if (target.type === 'card') {
        add(target, move);
      }
    }
    if (kind && lit.includes(kind)) {
      for (const target of boardTargets(move, kind)) {
        add(target, move);
      }
    }
  }
  return { targets, acts, picked: pickedKeys };
}

// The board targets of a move: its locations (not the one whose action it is), ships and players.
function boardTargets(move: Move, kind: string): Target[] {
  const ownLocation = recordNamed(kind)?.location;
  return targetsOf(move).filter(
    (target) => target.type === 'ship' || target.type === 'player' || (target.type === 'location' && target.location !== ownLocation),
  );
}

/** What a click on a target does: the moves it keeps, and the one move to send when only one is left. */
export function afterPick(all: Move[], picked: Target[], target: Target): { left: Move[]; send: Move | null } {
  const left = narrow(all, [...picked, target]);
  return { left, send: left.length === 1 ? left[0] : null };
}

export interface Answering {
  offer: Offer;
  // picks a target where it was clicked; sends the move when it is the only one left
  onTarget(target: Target, anchor: Anchor | null): void;
}

export const AnsweringContext = createContext<Answering>({ offer: NO_OFFER, onTarget: () => {} });

export function useAnswering(): Answering {
  return useContext(AnsweringContext);
}

/** The anchor of a click, for the popover. */
export function anchorOf(event: { clientX: number; clientY: number }): Anchor {
  return { x: event.clientX, y: event.clientY };
}

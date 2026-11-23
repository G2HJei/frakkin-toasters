// What changed from one game message to the next, for the table's small animations: a resource, the jump track, the
// distance, the nukes, a newly damaged location, a die roll and the cards that came into a hand. Only two projections
// in a row count: after a reconnect or an undo the store keeps no previous game (gameStore.previous), so a snapshot
// animates nothing it can't attribute.
import { useMemo } from 'react';
import { enumValues } from '../wire/schema';
import { useGameStore } from '../live/gameStore';
import type { GameProjection, SkillCard } from '../wire/types';

export const RESOURCES = ['fuel', 'food', 'morale', 'population'] as const;

export interface Changes {
  resources: { resource: (typeof RESOURCES)[number]; delta: number }[];
  // steps along the jump track, null when it didn't move; a reset after a jump counts as one
  jump: { delta: number; reset: boolean } | null;
  distance: number;
  nukes: number;
  damaged: string[];
  rolled: boolean;
  // the face-up cards new in each player's hand, by player number
  gained: Map<number, SkillCard[]>;
  // the backs new in each hidden hand, by color
  gainedColors: Map<number, Record<string, number>>;
}

export const NO_CHANGES: Changes = { resources: [], jump: null, distance: 0, nukes: 0, damaged: [], rolled: false, gained: new Map(), gainedColors: new Map() };

export function changesBetween(previous: GameProjection | null, next: GameProjection | null): Changes {
  if (!previous || !next) {
    return NO_CHANGES;
  }
  const resources = RESOURCES.map((resource) => ({ resource, delta: (next.galactica?.[resource] ?? 0) - (previous.galactica?.[resource] ?? 0) })).filter(
    (change) => change.delta !== 0,
  );
  const track = enumValues('JumpPreparation');
  const from = track.indexOf(previous.galactica?.jumpPreparation ?? '');
  const to = track.indexOf(next.galactica?.jumpPreparation ?? '');
  const jump = from < 0 || to < 0 || from === to ? null : { delta: to - from, reset: to === 0 && next.distanceTraveled > previous.distanceTraveled };

  const damagedIn = (game: GameProjection) =>
    new Set([...(game.galactica?.locations ?? []), ...(game.pegasus?.locations ?? [])].filter((location) => location.damaged).map((location) => location.location as string));
  const before = damagedIn(previous);
  const damaged = [...damagedIn(next)].filter((location) => !before.has(location));

  const roll = (game: GameProjection) => game.turn?.dieRoll ?? null;
  const rolled = roll(next)?.value != null && (roll(previous)?.value == null || roll(previous)?.value !== roll(next)?.value || previous.turn?.number !== next.turn?.number);

  const gained = new Map<number, SkillCard[]>();
  const gainedColors = new Map<number, Record<string, number>>();
  for (const player of next.players ?? []) {
    const old = previous.players?.find((candidate) => candidate.number === player.number)?.skillCards;
    const hand = player.skillCards;
    if (!old || !hand) {
      continue;
    }
    if (hand.cards && old.cards) {
      const left = old.cards.map((card) => JSON.stringify(card));
      const fresh = hand.cards.filter((card) => {
        const at = left.indexOf(JSON.stringify(card));
        return at < 0 || left.splice(at, 1).length === 0;
      });
      if (fresh.length > 0) {
        gained.set(player.number, fresh);
      }
    }
    const added = Object.fromEntries(
      Object.entries(hand.colors ?? {})
        .map(([color, count]) => [color, (count ?? 0) - ((old.colors ?? {})[color as keyof typeof old.colors] ?? 0)])
        .filter(([, delta]) => (delta as number) > 0),
    ) as Record<string, number>;
    if (Object.keys(added).length > 0) {
      gainedColors.set(player.number, added);
    }
  }
  return {
    resources,
    jump,
    distance: next.distanceTraveled - previous.distanceTraveled,
    nukes: next.nukes - previous.nukes,
    damaged,
    rolled,
    gained,
    gainedColors,
  };
}

/** The changes of the latest game message, and its version, which keys the animations so each plays once. */
export function useChanges(): { changes: Changes; version: number } {
  const previous = useGameStore((state) => state.previous);
  const game = useGameStore((state) => state.game);
  const version = useGameStore((state) => state.version);
  const changes = useMemo(() => changesBetween(previous, game), [previous, game]);
  return { changes, version };
}

/** "+2", "−1": a delta as the table floats it. */
export function signed(delta: number): string {
  return delta > 0 ? `+${delta}` : `−${Math.abs(delta)}`;
}

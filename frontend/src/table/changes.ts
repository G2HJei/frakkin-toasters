// What changed from one game message to the next, for the table's small animations: a resource, a track's marker, the
// distance, the nukes, a newly damaged location, Galactica's damage turning critical, a resource's shield going up or
// taking its loss, a die roll and the cards that came into a hand. Only two projections
// in a row count: after a reconnect or an undo the store keeps no previous game (gameStore.previous), so a snapshot
// animates nothing it can't attribute.
import { useMemo } from 'react';
import { useGameStore } from '../live/gameStore';
import type { GameProjection, LogEntry, Resource, SkillCard } from '../wire/types';

export const RESOURCES = ['fuel', 'food', 'morale', 'population'] as const;

export const TRACKS = ['JumpPreparation', 'Pursuit'] as const;
export type Track = (typeof TRACKS)[number];

type ResourceKey = (typeof RESOURCES)[number];

export interface Changes {
  resources: { resource: ResourceKey; delta: number }[];
  // the space each moved marker left, a reset after a jump too: it fades out there, then in on its new space
  tracks: { track: Track; from: string }[];
  distance: number;
  nukes: number;
  damaged: string[];
  // Galactica's damage became critical: its warning plate flickers on
  critical: boolean;
  // the resources whose next loss "Preventive Policy" just made smaller: their shield fades in
  shieldsRaised: ResourceKey[];
  // the shields that took their loss, which the end of the turn may follow in the same message: they fade out
  shieldsSpent: ResourceKey[];
  rolled: boolean;
  // the face-up cards new in each player's hand, by player number
  gained: Map<number, SkillCard[]>;
  // how many more cards each hand holds, for a hand that lies face down
  gainedBacks: Map<number, number>;
}

export const NO_CHANGES: Changes = {
  resources: [],
  tracks: [],
  distance: 0,
  nukes: 0,
  damaged: [],
  critical: false,
  shieldsRaised: [],
  shieldsSpent: [],
  rolled: false,
  gained: new Map(),
  gainedBacks: new Map(),
};

// the damaged Galactica locations from which the board warns of critical damage; at 6 the Cylons win
export const CRITICAL_DAMAGE = 4;

// only Galactica's own locations can be damaged on its board, Colonial One's can't
export function isCriticallyDamaged(game: GameProjection): boolean {
  return (game.galactica?.locations ?? []).filter((location) => location.damaged).length >= CRITICAL_DAMAGE;
}

export function lossReduction(game: GameProjection, resource: ResourceKey): number {
  return game.turn?.lossReductions?.[resource.toUpperCase() as Resource] ?? 0;
}

export function changesBetween(previous: GameProjection | null, next: GameProjection | null, fresh: LogEntry[] = []): Changes {
  if (!previous || !next) {
    return NO_CHANGES;
  }
  const resources = RESOURCES.map((resource) => ({ resource, delta: (next.galactica?.[resource] ?? 0) - (previous.galactica?.[resource] ?? 0) })).filter(
    (change) => change.delta !== 0,
  );
  const tracks = TRACKS.flatMap((track) => {
    const [from, to] = [trackPosition(previous, track), trackPosition(next, track)];
    return from && to && from !== to ? [{ track, from }] : [];
  });

  const damagedIn = (game: GameProjection) =>
    new Set([...(game.galactica?.locations ?? []), ...(game.pegasus?.locations ?? [])].filter((location) => location.damaged).map((location) => location.location as string));
  const before = damagedIn(previous);
  const damaged = [...damagedIn(next)].filter((location) => !before.has(location));

  // a new turn starts without shields, so one in it is new; the facts tell a shield that took its loss from one the end
  // of the turn took down
  const sameTurn = previous.turn?.number === next.turn?.number;
  const shieldsRaised = RESOURCES.filter((resource) => lossReduction(next, resource) > 0 && (!sameTurn || lossReduction(previous, resource) === 0));
  const reduced = new Set(
    fresh
      .flatMap((entry) => (entry.kind === 'ScopeLog' ? (entry.facts ?? []) : []))
      .flatMap((fact) => (fact.kind === 'Shown' && fact.fact?.kind === 'GameFact.LossReduced' && fact.fact.resource ? [fact.fact.resource.toLowerCase()] : [])),
  );
  const shieldsSpent = RESOURCES.filter((resource) => lossReduction(previous, resource) > 0 && reduced.has(resource));

  const roll = (game: GameProjection) => game.turn?.dieRoll ?? null;
  const rolled = roll(next)?.value != null && (roll(previous)?.value == null || roll(previous)?.value !== roll(next)?.value || previous.turn?.number !== next.turn?.number);

  const gained = new Map<number, SkillCard[]>();
  const gainedBacks = new Map<number, number>();
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
    if (hand.count > old.count) {
      gainedBacks.set(player.number, hand.count - old.count);
    }
  }
  return {
    resources,
    tracks,
    distance: next.distanceTraveled - previous.distanceTraveled,
    nukes: next.nukes - previous.nukes,
    damaged,
    critical: isCriticallyDamaged(next) && !isCriticallyDamaged(previous),
    shieldsRaised,
    shieldsSpent,
    rolled,
    gained,
    gainedBacks,
  };
}

export function trackPosition(game: GameProjection, track: Track): string | null {
  return (track === 'JumpPreparation' ? game.galactica?.jumpPreparation : game.cylonFleet?.pursuit) ?? null;
}

/** The changes of the latest game message, and its version, which keys the animations so each plays once. */
export function useChanges(): { changes: Changes; version: number } {
  const previous = useGameStore((state) => state.previous);
  const game = useGameStore((state) => state.game);
  const fresh = useGameStore((state) => state.fresh);
  const version = useGameStore((state) => state.version);
  const changes = useMemo(() => changesBetween(previous, game, fresh), [previous, game, fresh]);
  return { changes, version };
}

export function signed(delta: number): string {
  return delta > 0 ? `+${delta}` : `−${Math.abs(delta)}`;
}

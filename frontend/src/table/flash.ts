// What a log entry touched, lit on the board for a moment when the entry is clicked: the locations, ships and players
// (a character stands for his player) its event, its facts or its choice name. The same names tell the log's "Mine"
// filter which entries are about a player.
import { create } from 'zustand';
import { walkRecord } from '../wire/walk';
import type { Followup, GameProjection, LogEntry } from '../wire/types';

const FLASH_MS = 1800;

interface FlashState {
  keys: Set<string>;
  timer: ReturnType<typeof setTimeout> | null;
  flash(keys: string[]): void;
}

export const useFlash = create<FlashState>((set, get) => ({
  keys: new Set(),
  timer: null,
  flash: (keys) => {
    const timer = get().timer;
    if (timer) {
      clearTimeout(timer);
    }
    set({ keys: new Set(keys), timer: setTimeout(() => set({ keys: new Set(), timer: null }), FLASH_MS) });
  },
}));

/** The keys of what an entry names, as `location:X`, `ship:7`, `player:2`, the keys the board's targets use. */
export function entryTargets(entry: LogEntry, game: GameProjection | null): string[] {
  const keys = new Set<string>();
  const playerOf = (character: string) => game?.players?.find((player) => player.character === character)?.number;
  const visit = (field: unknown, type: { t: string; name?: string; role?: string | null }) => {
    if (type.t === 'enum' && type.name === 'Location' && typeof field === 'string') {
      keys.add(`location:${field}`);
    } else if (type.t === 'enum' && type.name === 'Character' && typeof field === 'string') {
      const player = playerOf(field);
      if (player !== undefined) {
        keys.add(`player:${player}`);
      }
    } else if ((type.t === 'int' || type.t === 'long') && typeof field === 'number') {
      if (type.role === 'ship') {
        keys.add(`ship:${field}`);
      } else if (type.role === 'player') {
        keys.add(`player:${field}`);
      }
    }
  };
  if (entry.kind === 'ScopeLog') {
    walkRecord(entry.details as unknown as Record<string, unknown> | null, entry.event ?? null, visit);
    for (const fact of entry.facts ?? []) {
      if (fact.kind === 'Shown') {
        walkRecord(fact.fact as unknown as Record<string, unknown> | null, null, visit);
      } else if (fact.playerNumber !== null) {
        keys.add(`player:${fact.playerNumber}`);
      }
    }
  } else {
    for (const event of optionEvents(entry.option)) {
      walkRecord(event, null, visit);
    }
  }
  return [...keys];
}

function optionEvents(option: Followup | null | undefined): Record<string, unknown>[] {
  switch (option?.kind) {
    case 'Single':
      return option.event ? [option.event as unknown as Record<string, unknown>] : [];
    case 'AllOf':
      return (option.followups ?? []).flatMap(optionEvents);
    case 'OneOf':
      return (option.options ?? []).flatMap(optionEvents);
    default:
      return [];
  }
}

/** Whether an entry is about the player: he acts or chooses in it, or it names him. */
export function isAbout(entry: LogEntry, playerNumber: number, game: GameProjection | null): boolean {
  const actor = entry.kind === 'ScopeLog' ? entry.actor : entry.chooser;
  return actor === playerNumber || entryTargets(entry, game).includes(`player:${playerNumber}`);
}

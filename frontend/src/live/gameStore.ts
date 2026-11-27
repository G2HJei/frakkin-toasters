import { create } from 'zustand';
import type { ConnectionState } from './GameSocket';
import type { ClientMessage, Event, GameProjection, GameReport, LogEntry, RejectedMessage, ServerMessage } from '../wire/types';

export interface Intent {
  player: number;
  event: Event | Record<string, unknown>;
  // the game it was chosen in: only a later one can ask for it
  afterVersion: number;
  // submitted: it waits for the game that answers it
  sent?: boolean;
}

export interface GameState {
  connection: ConnectionState;
  attempt: number;
  token: string | null;
  game: GameProjection | null;
  // the game before the latest one, for the table's animations (table/changes.ts); none after a snapshot
  previous: GameProjection | null;
  // the next game is a snapshot (a reconnect, an undo): nothing to animate from
  snapshotNext: boolean;
  log: LogEntry[];
  // the log entries that came with the latest game, for the table's animations: an update comes before its game
  fresh: LogEntry[];
  // the entries of an update whose game hasn't come yet
  arriving: LogEntry[];
  rejected: RejectedMessage | null;
  // how many game messages came, so a view can tell a new game from the same one
  version: number;
  // an answer to send once the option just chosen asks its decision (answers.ts)
  intent: Intent | null;
  report: GameReport | null;
  receive(message: ServerMessage): void;
  setIntent(intent: Intent | null): void;
  expectSnapshot(): void;
  setConnection(state: ConnectionState, attempt: number): void;
  reset(): void;
}

const initial = {
  connection: 'connecting' as ConnectionState,
  attempt: 0,
  token: null,
  game: null,
  previous: null,
  snapshotNext: false,
  log: [] as LogEntry[],
  fresh: [] as LogEntry[],
  arriving: [] as LogEntry[],
  rejected: null,
  version: 0,
  intent: null,
  report: null as GameReport | null,
};

/**
 * The last rejection stays until the next game. Commands always carry the token of the latest game.
 */
export const useGameStore = create<GameState>((set) => ({
  ...initial,
  receive: (message) =>
    set((state) => {
      switch (message.type) {
        case 'game':
          return {
            token: message.token,
            game: message.game,
            previous: state.snapshotNext ? null : state.game,
            snapshotNext: false,
            fresh: state.arriving,
            arriving: [],
            rejected: null,
            version: state.version + 1,
            // a sandbox's undo can take the ending back
            report: message.game?.outcome ? state.report : null,
          };
        case 'update': {
          const entries = message.update?.entries ?? [];
          return { log: [...state.log, ...entries], arriving: [...state.arriving, ...entries], intent: answeredByTheServer(state.intent, entries) ? null : state.intent };
        }
        case 'log':
          return { log: [...(message.log?.entries ?? [])], arriving: [] };
        case 'rejected':
          return { rejected: message, intent: null };
        case 'report':
          return { report: message.report ?? null };
        default:
          return {};
      }
    }),
  // a connection that opens gets the game anew, as a snapshot
  setConnection: (connection, attempt) => set((state) => ({ connection, attempt, snapshotNext: connection === 'open' ? true : state.snapshotNext })),
  expectSnapshot: () => set({ snapshotNext: true }),
  setIntent: (intent) => set({ intent }),
  reset: () => set({ ...initial }),
}));

// The server takes the only answer of a decision itself, so an intent for the decision the option asked was answered
// already: it must not answer a later decision that takes the same event.
function answeredByTheServer(intent: Intent | null, entries: LogEntry[]): boolean {
  return intent !== null && entries.some((entry) => entry.kind === 'ScopeLog' && entry.automatic && entry.actor === intent.player);
}

export function submitMessage(token: string | null, event: Event | Record<string, unknown>): ClientMessage {
  return { type: 'submit', token, event: event as Event };
}

export function chooseMessage(token: string | null, option: number): ClientMessage {
  return { type: 'choose', token, option };
}

export function undoMessage(token: string | null): ClientMessage {
  return { type: 'undo', token };
}

export function injectMessage(token: string | null, event: Event | Record<string, unknown>): ClientMessage {
  return { type: 'inject', token, event: event as Event };
}

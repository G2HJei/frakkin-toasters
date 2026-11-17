import { create } from 'zustand';
import type { ConnectionState } from './GameSocket';
import type { ClientMessage, Event, GameProjection, LogEntry, RejectedMessage, ServerMessage } from '../wire/types';

export interface GameState {
  connection: ConnectionState;
  attempt: number;
  token: string | null;
  game: GameProjection | null;
  log: LogEntry[];
  rejected: RejectedMessage | null;
  // how many game messages came, so a view can tell a new game from the same one
  version: number;
  receive(message: ServerMessage): void;
  setConnection(state: ConnectionState, attempt: number): void;
  reset(): void;
}

const initial = {
  connection: 'connecting' as ConnectionState,
  attempt: 0,
  token: null,
  game: null,
  log: [] as LogEntry[],
  rejected: null,
  version: 0,
};

/**
 * The state of the open table, fed by the socket: the latest game with its token, the log, the connection and the last
 * rejection, which stays until the next game. Commands always carry the token of the latest game.
 */
export const useGameStore = create<GameState>((set) => ({
  ...initial,
  receive: (message) =>
    set((state) => {
      switch (message.type) {
        case 'game':
          return { token: message.token, game: message.game, rejected: null, version: state.version + 1 };
        case 'update':
          return { log: [...state.log, ...(message.update?.entries ?? [])] };
        case 'log':
          return { log: [...(message.log?.entries ?? [])] };
        case 'rejected':
          return { rejected: message };
        default:
          return {};
      }
    }),
  setConnection: (connection, attempt) => set({ connection, attempt }),
  reset: () => set({ ...initial }),
}));

/* the commands, each with the token of the latest game */

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

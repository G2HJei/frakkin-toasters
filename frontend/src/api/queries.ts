import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './http';

export interface Me {
  id: string;
  name: string;
  settings: Record<string, unknown>;
}

// a user as the users page lists him
export interface ListedUser {
  id: string;
  name: string;
}

export interface Logins {
  providers: string[];
  devLogin: boolean;
}

export type GameStatus = 'LOBBY' | 'RUNNING' | 'FINISHED';
export type GameMode = 'STANDARD' | 'SANDBOX';
// why a finished game ended: a team won, the players or the host abandoned it, the host cancelled the lobby
export type EndReason = 'WON' | 'ABANDONED' | 'CANCELLED';
export type VoteKind = 'VACATE' | 'ABANDON';
export type LeaveReason = 'LEFT' | 'VOTED_OUT' | 'KICKED' | 'BANNED';

export interface GameSummary {
  id: string;
  name: string;
  status: GameStatus;
  mode: GameMode;
  turn: number;
  hostName: string;
  players: number;
  winnerTeam: string | null;
  participant: boolean;
  myPlayerNumber: number | null;
  waitingForMe: boolean;
  updatedAt: string;
  openable: boolean;
  endReason: EndReason | null;
  vacantSeats: number;
}

export interface Games {
  open: GameSummary[];
  mine: GameSummary[];
  running: GameSummary[];
}

export interface Participant {
  userId: string;
  name: string;
  playerNumber: number | null;
}

export interface Room {
  id: string;
  name: string;
  status: GameStatus;
  mode: GameMode;
  hostUserId: string;
  players: Participant[];
  observers: Participant[];
  winnerTeam: string | null;
  openable: boolean;
  unavailableReason: string | null;
  seed: number | null;
  endReason: EndReason | null;
  // the seats of a running game, and those nobody holds
  playerCount: number | null;
  vacantSeats: number[];
  votes: Vote[];
  // every holder of every seat, in the order they took it
  seatHistory: SeatHolder[];
  rematchId: string | null;
  // the participants who are there: polling the room in a lobby, connected once the game runs
  online: string[];
}

// a vote to vacate a seat (its player number), or to abandon the game (0), with the seated players who voted
export interface Vote {
  kind: VoteKind;
  playerNumber: number;
  voters: string[];
  needed: number;
}

export interface SeatHolder {
  playerNumber: number;
  userId: string;
  name: string;
  since: string;
  until: string | null;
  reason: LeaveReason | null;
}

export function useMe() {
  return useQuery<Me | null>({
    queryKey: ['me'],
    queryFn: async () => {
      try {
        return await api<Me>('GET', '/api/me');
      } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
          return null;
        }
        throw error;
      }
    },
  });
}

export function useUsers() {
  return useQuery<ListedUser[]>({ queryKey: ['users'], queryFn: () => api<ListedUser[]>('GET', '/api/users') });
}

export function useLogins() {
  return useQuery<Logins>({ queryKey: ['logins'], queryFn: () => api<Logins>('GET', '/api/login/providers') });
}

// The lobby has no socket of its own, so its lists refetch every few seconds.
export function useGames() {
  return useQuery<Games>({ queryKey: ['games'], queryFn: () => api<Games>('GET', '/api/games'), refetchInterval: 4000 });
}

export function useRoom(id: string) {
  return useQuery<Room>({ queryKey: ['room', id], queryFn: () => api<Room>('GET', `/api/games/${id}`), refetchInterval: 3000 });
}

export function useDevLogin() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (name: string) => api<Me>('POST', '/api/dev/login', { name }),
    onSuccess: () => client.invalidateQueries(),
  });
}

export function useLogout() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: () => api<null>('POST', '/api/logout'),
    onSuccess: () => client.invalidateQueries(),
  });
}

export function useUpdateMe() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (update: { name: string; settings: Record<string, unknown> }) => api<Me>('PUT', '/api/me', update),
    onSuccess: () => client.invalidateQueries({ queryKey: ['me'] }),
  });
}

export function useCreateSandbox() {
  return useMutation({
    mutationFn: (sandbox: { playerCount: number; seed: number | null; name?: string }) =>
      api<{ id: string }>('POST', '/api/sandbox', sandbox),
  });
}

export function useImportSandbox() {
  return useMutation({
    mutationFn: (file: string) => api<{ id: string }>('POST', '/api/sandbox/import', file),
  });
}

export function useLobbyAction() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ path, body }: { path: string; body?: unknown }) => api<Room>('POST', path, body ?? {}),
    onSuccess: () => client.invalidateQueries(),
  });
}

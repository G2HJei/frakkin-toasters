import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from './http';

export interface Me {
  id: string;
  name: string;
  settings: Record<string, unknown>;
}

export interface Logins {
  providers: string[];
  devLogin: boolean;
}

export type GameStatus = 'LOBBY' | 'RUNNING' | 'FINISHED';
export type GameMode = 'STANDARD' | 'SANDBOX';

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

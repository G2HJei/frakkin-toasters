import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { GameSummary, Games } from '../api/queries';
import { PlayPage } from './PlayPage';

const game = (id: string, name: string, mode: 'STANDARD' | 'SANDBOX', participant: boolean): GameSummary => ({
  id,
  name,
  status: 'RUNNING',
  mode,
  turn: 3,
  hostName: 'Adama',
  players: 4,
  winnerTeam: null,
  participant,
  myPlayerNumber: participant ? 1 : null,
  waitingForMe: false,
  updatedAt: '2026-11-26T12:00:00Z',
  openable: true,
  endReason: null,
  vacantSeats: 0,
});

function stubServer(games: Games) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (path: string) => {
      if (path === '/api/games') {
        return new Response(JSON.stringify(games), { status: 200 });
      }
      if (path === '/api/me') {
        return new Response(JSON.stringify({ id: 'u1', name: 'Starbuck', settings: {} }), { status: 200 });
      }
      return new Response('[]', { status: 200 });
    }),
  );
}

afterEach(() => vi.unstubAllGlobals());

describe('the play page', () => {
  it('opens each of my games and sandboxes in a tab of its own, and offers the others to watch', async () => {
    stubServer({
      mine: [game('g1', 'Galactica', 'STANDARD', true), game('s1', 'Scenario', 'SANDBOX', true)],
      open: [],
      running: [game('g2', 'Pegasus', 'STANDARD', false)],
    });
    render(
      <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
        <MemoryRouter>
          <PlayPage />
        </MemoryRouter>
      </QueryClientProvider>,
    );

    for (const [list, id] of [
      ['My games', 'g1'],
      ['My sandboxes', 's1'],
    ]) {
      const open = await within(screen.getByRole('region', { name: list })).findByRole('link', { name: 'Open' });
      expect(open).toHaveAttribute('href', `/games/${id}`);
      expect(open).toHaveAttribute('target', '_blank');
    }
    const watch = screen.getByRole('region', { name: 'Watch' });
    expect(within(watch).queryByRole('link', { name: 'Open' })).toBeNull();
    expect(within(watch).getByRole('button', { name: 'Watch' })).toBeInTheDocument();
  });
});

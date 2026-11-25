import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { Room } from '../../api/queries';
import { Table, tableContext } from '../tableContext';
import type { GameProjection } from '../../wire/types';
import { SessionPanel } from './SessionPanel';

const game = {
  players: [
    { number: 1, character: 'WILLIAM_ADAMA' },
    { number: 2, character: 'LAURA_ROSLIN' },
    { number: 3, character: null },
  ],
} as unknown as GameProjection;

// seat 3 is vacant, and a vote to hand seat 2 over is open
const room: Room = {
  id: 'g1',
  name: 'Galactica',
  status: 'RUNNING',
  mode: 'STANDARD',
  hostUserId: 'adama',
  players: [
    { userId: 'adama', name: 'Bill', playerNumber: 1 },
    { userId: 'roslin', name: 'Laura', playerNumber: 2 },
  ],
  observers: [{ userId: 'dualla', name: 'Dee', playerNumber: null }],
  winnerTeam: null,
  openable: true,
  unavailableReason: null,
  seed: null,
  endReason: null,
  playerCount: 3,
  vacantSeats: [3],
  votes: [{ kind: 'VACATE', playerNumber: 2, voters: ['adama'], needed: 1 }],
  seatHistory: [],
  rematchId: null,
  online: ['adama'],
};

const fetchMock = vi.fn(async () => new Response(JSON.stringify(room), { status: 200 }));

function renderPanel(myId: string) {
  vi.stubGlobal('fetch', fetchMock);
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <Table.Provider value={tableContext(game, false, null)}>
          <SessionPanel room={room} myId={myId} />
        </Table.Provider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

function seat(number: number) {
  return within(document.querySelector(`[data-seat="${number}"]`) as HTMLElement);
}

afterEach(() => {
  vi.unstubAllGlobals();
  fetchMock.mockClear();
});

describe('SessionPanel', () => {
  it('opens on a vacant seat, which an observer may take', async () => {
    renderPanel('dualla');

    expect(screen.getByText('1 vacant')).toBeVisible();
    expect(seat(3).getByText('vacant')).toBeVisible();
    fireEvent.click(seat(3).getByRole('button', { name: 'Take this seat' }));

    await vi.waitFor(() => expect(fetchMock).toHaveBeenCalled());
    const [url, init] = fetchMock.mock.calls.at(-1) as unknown as [string, RequestInit];
    expect(url).toBe('/api/games/g1/seat');
    expect(JSON.parse(String(init.body))).toEqual({ playerNumber: 3 });
    // an observer has no vote, only the tally
    expect(seat(2).getByText('1 of 1')).toBeVisible();
    expect(screen.getByRole('button', { name: 'Leave the table' })).toBeVisible();
  });

  it('lets a player vote on another seat and leave their own', () => {
    renderPanel('roslin');

    expect(seat(1).getByRole('button', { name: 'Vote to hand over' })).toBeVisible();
    expect(seat(2).getByRole('button', { name: 'Leave my seat' })).toBeVisible();
    expect(seat(3).queryByRole('button', { name: 'Take this seat' })).toBeNull();
    expect(screen.getByRole('button', { name: 'Vote to abandon' })).toBeVisible();
  });

  it('gives the host the actions that pass at once', () => {
    renderPanel('adama');

    expect(seat(2).getByRole('button', { name: 'Hand over' })).toBeVisible();
    expect(seat(2).getByRole('button', { name: 'Ban' })).toBeVisible();
    expect(screen.getByRole('button', { name: 'Abandon the game' })).toBeVisible();
  });
});

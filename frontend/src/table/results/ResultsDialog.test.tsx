import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { afterEach, describe, expect, it } from 'vitest';
import type { Room } from '../../api/queries';
import { useGameStore } from '../../live/gameStore';
import type { GameProjection, GameReport } from '../../wire/types';
import { Table, tableContext } from '../tableContext';
import { ResultsDialog, useResults } from './ResultsDialog';

const stats = { skillCardsGained: 9, cardsIntoChecks: 4, strengthIntoChecks: 11, skillCardsPlayed: 1, crisisCards: 3, abilitiesUsed: 2, sentToBrig: 0 };

const report: GameReport = {
  winner: 'CYLONS',
  winners: [2],
  turns: 9,
  distance: 3,
  players: [
    { number: 1, character: 'WILLIAM_ADAMA', formerCharacters: [], loyaltyCards: ['NOT_CYLON'], motiveCards: [], cylon: false, revealedOnTurn: null, turns: 5, statistics: stats },
    { number: 2, character: 'GAIUS_BALTAR', formerCharacters: [], loyaltyCards: ['NOT_CYLON'], motiveCards: [], cylon: true, revealedOnTurn: 6, turns: 4, statistics: stats },
  ],
  statistics: { crisisCards: 8, skillChecks: { PASS: 3, FAIL: 2 }, jumps: 1, shipsLost: { VIPER: 2 }, vipersDamaged: 1, locationsDamaged: 4, nukesUsed: 0 },
  resources: [
    { turn: 1, end: false, fuel: 8, food: 8, morale: 10, population: 12, distance: 0 },
    { turn: 2, end: false, fuel: 7, food: 8, morale: 9, population: 12, distance: 0 },
    { turn: 9, end: true, fuel: 2, food: 5, morale: 0, population: 6, distance: 3 },
  ],
  story: [
    { turn: 4, kind: 'JUMPED', playerNumber: null, character: null, destination: 'RAGNAR_ANCHORAGE', distance: 3, winner: null },
    { turn: 6, kind: 'CYLON_REVEALED', playerNumber: 2, character: 'GAIUS_BALTAR', destination: null, distance: null, winner: null },
    { turn: 9, kind: 'GAME_ENDED', playerNumber: null, character: null, destination: null, distance: null, winner: 'CYLONS' },
  ],
} as unknown as GameReport;

const room = {
  id: 'g1',
  name: 'Galactica',
  status: 'FINISHED',
  mode: 'STANDARD',
  hostUserId: 'adama',
  players: [],
  observers: [],
  endReason: 'WON',
  playerCount: 2,
  vacantSeats: [],
  votes: [],
  seatHistory: [
    { playerNumber: 1, userId: 'adama', name: 'Bill', since: '', until: null, reason: null },
    { playerNumber: 2, userId: 'baltar', name: 'Gaius', since: '', until: '', reason: 'LEFT' },
    { playerNumber: 2, userId: 'dee', name: 'Dee', since: '', until: null, reason: null },
  ],
  rematchId: null,
  online: [],
} as unknown as Room;

const game = { players: [{ number: 1, character: 'WILLIAM_ADAMA' }, { number: 2, character: 'GAIUS_BALTAR' }] } as unknown as GameProjection;

function renderResults(myId: string, theRoom: Room = room) {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter>
        <Table.Provider value={tableContext(game, false, null)}>
          <ResultsDialog gameId="g1" room={theRoom} myId={myId} />
        </Table.Provider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

afterEach(() => {
  useGameStore.getState().reset();
  useResults.getState().hide();
});

describe('ResultsDialog', () => {
  it('opens once the report comes, with the winners by every user who held their seat', () => {
    useGameStore.setState({ report });
    renderResults('adama');

    const dialog = screen.getByRole('dialog', { name: 'results' });
    expect(within(dialog).getByRole('heading', { name: 'The Cylons win' })).toBeVisible();
    expect(within(dialog).getByText('9 turns · distance 3 · 1 jump')).toBeVisible();
    const winners = within(dialog).getByRole('region', { name: 'winners' });
    expect(winners).toHaveTextContent('Gaius Baltar');
    expect(winners).toHaveTextContent('Gaius, then Dee');
    expect(within(dialog).getByText('Cylon, revealed on turn 6')).toBeVisible();
    expect(within(dialog).getByText('Gaius Baltar reveals a Cylon Loyalty Card')).toBeVisible();
    expect(within(dialog).getByText('Viper lost')).toBeVisible();
    // the chart's numbers in a table too
    expect(within(dialog).getByRole('row', { name: /End 2 5 0 6 3/ })).toBeInTheDocument();
    expect(within(dialog).getByRole('button', { name: 'Rematch with the same seats' })).toBeVisible();
  });

  it('names only the seats of an abandoned game, and the rematch once the host opened it', () => {
    renderResults('dee', { ...room, endReason: 'ABANDONED', rematchId: 'g2' } as Room);

    const dialog = screen.getByRole('dialog', { name: 'results' });
    expect(within(dialog).getByRole('heading', { name: 'The game was abandoned' })).toBeVisible();
    expect(within(dialog).getByText('Gaius, then Dee')).toBeVisible();
    expect(within(dialog).getByRole('link', { name: 'Go to the rematch' })).toHaveAttribute('href', '/games/g2');
  });

  it('stays closed while the game goes on', () => {
    renderResults('adama', { ...room, status: 'RUNNING', endReason: null } as Room);

    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

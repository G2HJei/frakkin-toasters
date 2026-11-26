import { createContext, useContext } from 'react';
import { constantLabel, NO_CONTEXT, type LabelContext } from '../labels/describe';
import type { GameProjection, PlayerProjection, ShipProjection, SkillCard } from '../wire/types';

// whether the user who holds a seat is there, or nobody holds it; unknown in a sandbox
export type SeatState = 'online' | 'away' | 'vacant';

/**
 * What the widgets of the table need to know of the game: its names, the players and ships, and the Skill Cards the
 * sandbox's setup offers. The UI only narrows a picker to plausible answers; the engine judges them (frontend
 * decision 2).
 */
export interface TableContext extends LabelContext {
  game: GameProjection | null;
  sandbox: boolean;
  askedPlayer: number | null;
  // the name of the user in a seat, which only the players panel shows: everywhere else a player is his character
  userName(playerNumber: number): string | null;
  seatState(playerNumber: number): SeatState | null;
  players(): { number: number; label: string }[];
  // a ship anywhere on the boards, or out of the game
  ship(id: number): ShipProjection | null;
  // the Skill Cards of the draw piles, which a sandbox sees
  deckSkillCards(): SkillCard[];
}

export function tableContext(
  game: GameProjection | null,
  sandbox: boolean,
  askedPlayer: number | null,
  userNames: Partial<Record<number, string>> = {},
  seatStates: Partial<Record<number, SeatState>> = {},
): TableContext {
  const players = game?.players ?? [];
  // a player is his character, and his seat until he has one
  const playerName = (playerNumber: number) => {
    const player = players.find((candidate) => candidate.number === playerNumber);
    return player?.character ? constantLabel('Character', player.character) : `Player ${playerNumber}`;
  };
  const allShips = [
    ...(game?.galactica?.ships ?? []),
    ...(game?.galactica?.reserves ?? []),
    ...(game?.galactica?.damagedVipers ?? []),
    ...(game?.cylonFleet?.ships ?? []),
    ...(game?.destroyedShips ?? []),
  ];
  // a centurion on the Boarding Party track is no ShipType
  const shipType = (id: number) =>
    allShips.find((candidate) => candidate.id === id)?.type ?? ((game?.galactica?.boardingParty ?? []).some((centurion) => centurion.id === id) ? 'CENTURION' : null);
  const shipName = (id: number) => {
    const type = shipType(id);
    return type ? `${constantLabel('ShipType', type)} #${id}` : `ship #${id}`;
  };
  return {
    game,
    sandbox,
    askedPlayer,
    playerName,
    shipName,
    shipType,
    ship: (id) => allShips.find((candidate) => candidate.id === id) ?? null,
    userName: (playerNumber) => userNames[playerNumber] ?? null,
    seatState: (playerNumber) => seatStates[playerNumber] ?? null,
    players: () => players.map((player) => ({ number: player.number, label: playerName(player.number) })),
    deckSkillCards: () =>
      (game?.decks ?? [])
        .filter((deck) => ['POLITICS', 'LEADERSHIP', 'TACTICS', 'PILOTING', 'ENGINEERING', 'TREACHERY', 'DESTINY'].includes(deck.deck ?? ''))
        .flatMap((deck) => (deck.drawPile ?? []) as SkillCard[]),
  };
}

export const Table = createContext<TableContext>({
  ...NO_CONTEXT,
  game: null,
  sandbox: false,
  askedPlayer: null,
  userName: () => null,
  seatState: () => null,
  players: () => [],
  ship: () => null,
  deckSkillCards: () => [],
});

export function useTable(): TableContext {
  return useContext(Table);
}

/**
 * The player whose own knowledge the table shows: the viewer, or the asked player in a sandbox, which answers for him as
 * he would. Nobody for an observer.
 */
export function knowingPlayer(table: Pick<TableContext, 'game' | 'sandbox' | 'askedPlayer'>): PlayerProjection | null {
  const viewer = table.game?.viewer;
  const number = table.sandbox ? table.askedPlayer : viewer?.kind === 'PlayerViewer' ? viewer.playerNumber : null;
  return number === null ? null : (table.game?.players?.find((player) => player.number === number) ?? null);
}

import { createContext, useContext } from 'react';
import { constantLabel, NO_CONTEXT, shortName, type LabelContext } from '../labels/describe';
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
  ship(id: number): ShipProjection | null;
  // where a ship is, which tells it from another of its type: its space area, the Reserves, a centurion's step
  shipPlace(id: number): string | null;
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
  const centurions = game?.galactica?.boardingParty ?? [];
  // a centurion on the Boarding Party track is no ShipType
  const shipType = (id: number) => allShips.find((candidate) => candidate.id === id)?.type ?? (centurions.some((centurion) => centurion.id === id) ? 'CENTURION' : null);
  // the id means nothing on the table, so a ship is its type
  const shipName = (id: number) => {
    const type = shipType(id);
    return type ? constantLabel('ShipType', type) : 'a ship';
  };
  const shipPlace = (id: number) => {
    const has = (ships: ShipProjection[] | null | undefined) => (ships ?? []).some((candidate) => candidate.id === id);
    const located = allShips.find((candidate) => candidate.id === id)?.location;
    const centurion = centurions.find((candidate) => candidate.id === id)?.position;
    if (located) {
      return constantLabel('Location', located);
    }
    if (centurion) {
      return `Boarding Party, ${constantLabel('BoardingParty', centurion).toLowerCase()}`;
    }
    return has(game?.galactica?.reserves) ? 'Reserves' : has(game?.galactica?.damagedVipers) ? 'Damaged' : null;
  };
  return {
    game,
    sandbox,
    askedPlayer,
    playerName,
    shipName,
    shipType,
    ship: (id) => allShips.find((candidate) => candidate.id === id) ?? null,
    shipPlace,
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
  shipPlace: () => null,
  deckSkillCards: () => [],
});

export function useTable(): TableContext {
  return useContext(Table);
}

// What sets a ship apart from the others of its type: its pilot and its damage tokens.
export function shipMarks(ship: ShipProjection): string[] {
  const damage = ship.damage ?? [];
  return [
    ...(ship.pilot ? [shortName(constantLabel('Character', ship.pilot))] : []),
    ...(damage.length > 0 ? [damage.map((token) => constantLabel('BasestarDamage', token)).join(', ')] : []),
  ];
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

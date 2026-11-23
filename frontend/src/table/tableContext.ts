import { createContext, useContext } from 'react';
import { constantLabel, NO_CONTEXT, type LabelContext } from '../labels/describe';
import { sortSkillCards } from './skillCards';
import type { GameProjection, PlayedCard, PlayedInterruptCard, ShipProjection, SkillCard } from '../wire/types';

export interface ShipChoice {
  id: number;
  label: string;
}

/**
 * What the widgets of the table need to know of the game: the players, the ships and the cards a picker offers. The
 * UI only narrows a picker to plausible answers; the engine judges them (frontend decision 2).
 */
export interface TableContext extends LabelContext {
  game: GameProjection | null;
  sandbox: boolean;
  askedPlayer: number | null;
  // the name of the user in a seat, which only the players panel shows: everywhere else a player is his character
  userName(playerNumber: number): string | null;
  players(): { number: number; label: string }[];
  ships(): ShipChoice[];
  // a ship anywhere on the boards, or out of the game
  ship(id: number): ShipProjection | null;
  hand(playerNumber: number | null): SkillCard[];
  deckSkillCards(): SkillCard[];
  // the cards of the active skill check once they are divided, face up
  checkCards(): SkillCard[];
  playedBefore(): PlayedCard[];
  windowCards(): PlayedInterruptCard[];
}

export function tableContext(
  game: GameProjection | null,
  sandbox: boolean,
  askedPlayer: number | null,
  userNames: Partial<Record<number, string>> = {},
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
  const shipName = (id: number) => {
    const ship = allShips.find((candidate) => candidate.id === id);
    if (ship) {
      return `${constantLabel('ShipType', ship.type)} #${id}`;
    }
    return (game?.galactica?.boardingParty ?? []).some((centurion) => centurion.id === id) ? `Centurion #${id}` : `ship #${id}`;
  };
  return {
    game,
    sandbox,
    askedPlayer,
    playerName,
    shipName,
    ship: (id) => allShips.find((candidate) => candidate.id === id) ?? null,
    userName: (playerNumber) => userNames[playerNumber] ?? null,
    players: () => players.map((player) => ({ number: player.number, label: playerName(player.number) })),
    ships: () => {
      if (!game) {
        return [];
      }
      const ship = (where: string) => (projection: { id: number; type: string | null; location: string | null }) => ({
        id: projection.id,
        label: `${constantLabel('ShipType', projection.type)} #${projection.id} — ${projection.location ? constantLabel('Location', projection.location) : where}`,
      });
      return [
        ...(game.galactica?.ships ?? []).map(ship('space')),
        ...(game.galactica?.reserves ?? []).map(ship('reserves')),
        ...(game.galactica?.damagedVipers ?? []).map(ship('damaged')),
        ...(game.cylonFleet?.ships ?? []).map(ship('Cylon Fleet')),
        ...(game.galactica?.boardingParty ?? []).map((centurion) => ({
          id: centurion.id,
          label: `Centurion #${centurion.id} — ${constantLabel('BoardingParty', centurion.position)}`,
        })),
      ];
    },
    hand: (playerNumber) => sortSkillCards(players.find((player) => player.number === playerNumber)?.skillCards?.cards ?? []),
    deckSkillCards: () =>
      (game?.decks ?? [])
        .filter((deck) => ['POLITICS', 'LEADERSHIP', 'TACTICS', 'PILOTING', 'ENGINEERING', 'TREACHERY', 'DESTINY'].includes(deck.deck ?? ''))
        .flatMap((deck) => (deck.drawPile ?? []) as SkillCard[]),
    checkCards: () => [...(game?.skillCheck?.matchingPile ?? []), ...(game?.skillCheck?.nonMatchingPile ?? [])],
    playedBefore: () =>
      (game?.skillCheck?.playedBefore ?? []).flatMap((played) =>
        (played.cards?.cards ?? []).map((skillCard) => ({ playerNumber: played.player, skillCard })),
      ),
    windowCards: () => game?.turn?.windowCards ?? [],
  };
}

export const Table = createContext<TableContext>({
  ...NO_CONTEXT,
  game: null,
  sandbox: false,
  askedPlayer: null,
  userName: () => null,
  players: () => [],
  ships: () => [],
  ship: () => null,
  hand: () => [],
  deckSkillCards: () => [],
  checkCards: () => [],
  playedBefore: () => [],
  windowCards: () => [],
});

export function useTable(): TableContext {
  return useContext(Table);
}

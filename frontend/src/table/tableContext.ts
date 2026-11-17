import { createContext, useContext } from 'react';
import { constantLabel, NO_CONTEXT, type LabelContext } from '../labels/describe';
import type { GameProjection, PlayedCard, PlayedInterruptCard, SkillCard } from '../wire/types';

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
  players(): { number: number; label: string }[];
  ships(): ShipChoice[];
  hand(playerNumber: number | null): SkillCard[];
  deckSkillCards(): SkillCard[];
  playedBefore(): PlayedCard[];
  windowCards(): PlayedInterruptCard[];
}

export function tableContext(game: GameProjection | null, sandbox: boolean, askedPlayer: number | null): TableContext {
  const players = game?.players ?? [];
  const playerName = (playerNumber: number) => {
    const player = players.find((candidate) => candidate.number === playerNumber);
    return player?.character ? `P${playerNumber} ${constantLabel('Character', player.character)}` : `Player ${playerNumber}`;
  };
  return {
    game,
    sandbox,
    askedPlayer,
    playerName,
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
    hand: (playerNumber) => players.find((player) => player.number === playerNumber)?.skillCards?.cards ?? [],
    deckSkillCards: () =>
      (game?.decks ?? [])
        .filter((deck) => ['POLITICS', 'LEADERSHIP', 'TACTICS', 'PILOTING', 'ENGINEERING', 'TREACHERY', 'DESTINY'].includes(deck.deck ?? ''))
        .flatMap((deck) => (deck.drawPile ?? []) as SkillCard[]),
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
  players: () => [],
  ships: () => [],
  hand: () => [],
  deckSkillCards: () => [],
  playedBefore: () => [],
  windowCards: () => [],
});

export function useTable(): TableContext {
  return useContext(Table);
}

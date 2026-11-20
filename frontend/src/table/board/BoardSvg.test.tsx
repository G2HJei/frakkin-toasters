import { fireEvent, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { BoardSvg, groupShips, shortName } from './BoardSvg';
import { RevealLayer, useReveal } from '../reveal';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, ShipProjection } from '../../wire/types';
import { DECK_SLOTS, DIALS, DISTANCE, JUMP_TRACK, LAUNCH_TUBES, LAYOUT, SLOTS, TITLES } from './layout';

const location = (name: string, characters: string[] = [], damaged = false) => ({ location: name, damaged, characters });

// A projection as the backend sends it, cut down to what the board draws.
const game = {
  viewer: { kind: 'Observer' },
  objective: 'KOBOL',
  titles: { president: 'LAURA_ROSLIN', admiral: 'WILLIAM_ADAMA', cag: 'KARA_STARBUCK_THRACE' },
  turn: { currentPlayer: 1, actingPlayer: 1, crisisCards: ['WATER_SABOTAGED'], dieRoll: { value: 5, cards: [], modifier: 0, result: 5 } },
  players: [{ number: 1, character: 'WILLIAM_ADAMA' }],
  galactica: {
    fuel: 8,
    food: 7,
    morale: 9,
    population: 12,
    jumpPreparation: 'POSITION_2',
    locations: [location('RESEARCH_LAB', ['WILLIAM_ADAMA']), location('ARMORY', [], true), location('GALACTICA_SPACE_4_OCLOCK')],
    ships: [{ id: 3, type: 'VIPER', location: 'GALACTICA_SPACE_4_OCLOCK', pilot: 'KARA_STARBUCK_THRACE', damage: [], cargo: null }],
    reserves: [{ id: 4, type: 'VIPER', location: null, pilot: null, damage: [], cargo: null }],
    damagedVipers: [],
    boardingParty: [{ id: 1040, position: 'POSITION_1' }],
  },
  pegasus: { engineRoomActivated: false, locations: [location('AIRLOCK', ['LAURA_ROSLIN'])] },
  cylonFleet: {
    pursuit: 'START',
    locations: [location('CYLON_FLEET_SPACE_2')],
    ships: [{ id: 1019, type: 'RAIDER', location: 'CYLON_FLEET_SPACE_2', pilot: null, damage: [], cargo: null }],
  },
  cylonSupply: { basestars: 2, raiders: 12, heavyRaiders: 4, centurions: 3 },
  decks: [
    { deck: 'CRISIS', size: 70, discardSize: 2, discarded: ['RIOTS_JUMP', 'WATER_SABOTAGED'], drawPile: null },
    { deck: 'POLITICS', size: 12, discardSize: 1, discarded: [{ type: 'RED_TAPE', value: 0 }], drawPile: null },
  ],
  skillCheck: {
    type: 'CYLON_SCREENINGS',
    targetPlayer: null,
    difficulty: 9,
    playedBefore: [],
    playedBeforeRevealed: false,
    cards: { count: 3, colors: {}, cards: null },
    matchingPile: [],
    nonMatchingPile: [],
    resolvedAbilities: [],
    emergencyDeclared: false,
    totalStrength: null,
    result: null,
  },
  destinations: ['ALGAE_PLANET'],
  distanceTraveled: 2,
  nukes: 2,
} as unknown as GameProjection;

function renderBoard(targets?: Set<string>, projection: GameProjection = game, sandbox = false) {
  return render(
    <Table.Provider value={tableContext(projection, sandbox, null)}>
      <BoardSvg targets={targets} />
      <RevealLayer />
    </Table.Provider>,
  ).container;
}

afterEach(() => useReveal.getState().hide());

describe('the board', () => {
  it("lies on the board's image, each character's standee on the printed card of the location the projection puts him", () => {
    const board = renderBoard();

    expect(board.querySelector('.board-image')?.getAttribute('href')).toContain('board');
    expect(board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"] [data-art="Character/player-token/WILLIAM_ADAMA"]')).not.toBeNull();
    expect(board.querySelector('[data-location="AIRLOCK"] [data-character="LAURA_ROSLIN"]')).not.toBeNull();
    expect(board.querySelectorAll('[data-character="WILLIAM_ADAMA"]')).toHaveLength(1);
    // the current player's standee stands out
    expect(board.querySelector('[data-character="WILLIAM_ADAMA"]')?.classList).toContain('current');
  });

  it("draws each ship in its space area, with its pilot's token, and the reserves and the Cylon supply in their slots", () => {
    const board = renderBoard();

    const viper = board.querySelector('[data-location="GALACTICA_SPACE_4_OCLOCK"] [data-ship="3"]');
    expect(viper?.getAttribute('aria-label')).toBe('Viper #3 · Starbuck');
    expect(viper?.querySelector('[data-art="ShipType/VIPER"]')).not.toBeNull();
    expect(viper?.querySelector('[data-art="Character/piloting-token/KARA_STARBUCK_THRACE"]')).not.toBeNull();
    expect(board.querySelector('[data-location="CYLON_FLEET_SPACE_2"] [data-ship="1019"]')?.getAttribute('aria-label')).toBe('Raider #1019');
    expect(board.querySelector('[data-slot="reserves"] [data-ship="4"]')).not.toBeNull();
    expect(board.querySelector('[data-supply="RAIDER"]')?.textContent).toContain('×12');
  });

  it('piles up the ships of a type, and sets a piloted or damaged one apart', () => {
    const ship = (id: number, type: string, extra: object = {}) => ({ id, type, location: 'X', pilot: null, damage: [], cargo: null, ...extra }) as unknown as ShipProjection;

    const groups = groupShips([ship(5, 'RAIDER'), ship(1, 'VIPER', { pilot: 'LEE_ADAMA' }), ship(2, 'RAIDER'), ship(9, 'BASESTAR', { damage: ['CRITICAL_HIT'] }), ship(3, 'VIPER')]);

    expect(groups.map((group) => group.ships.map((one) => one.id))).toEqual([[9], [2, 5], [1], [3]]);
  });

  it('marks a damaged location with its damage token, and puts the markers on the tracks', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-location="ARMORY"]')?.classList).toContain('damaged');
    expect(board.querySelector('[data-location="ARMORY"] [data-art="GalacticaDamage/ARMORY"]')).not.toBeNull();
    expect(board.querySelector('[data-track="JumpPreparation"]')?.getAttribute('data-position')).toBe('POSITION_2');
    expect(board.querySelector('[data-track="Pursuit"]')?.getAttribute('data-position')).toBe('START');
    expect(board.querySelector('[data-track="BoardingParty"] [data-position="POSITION_1"]')?.getAttribute('aria-label')).toBe('1 centurion at Position 1');
  });

  it("turns each dial's printed wheel until its value sits under the pointer", () => {
    const board = renderBoard();

    expect([...board.querySelectorAll('.dial-value')].map((dial) => dial.textContent)).toEqual(['8', '7', '9', '12']);
    // food: the wheel shows 8 at the top, one step of 22.5° back gives 7
    expect(board.querySelector('[data-resource="food"] image')?.getAttribute('transform')).toBe(`rotate(22.5 ${DIALS.food.x} ${DIALS.food.y})`);
    expect(board.querySelector('[data-resource="fuel"] image')?.getAttribute('transform')).toBe(`rotate(0 ${DIALS.fuel.x} ${DIALS.fuel.y})`);
  });

  it("stands each character on the picture of his location, above its printed name", () => {
    const board = renderBoard();

    const standee = board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"] image')!;
    const bottom = Number(standee.getAttribute('y')) + Number(standee.getAttribute('height'));
    expect(Number(standee.getAttribute('y'))).toBeGreaterThanOrEqual(LAYOUT.RESEARCH_LAB.y);
    expect(bottom).toBeLessThanOrEqual(TITLES.RESEARCH_LAB!.y);
  });

  it('tucks the destinations under the objective, the later ones under the earlier, each showing a strip', () => {
    const board = renderBoard(undefined, { ...game, destinations: ['ALGAE_PLANET', 'ASTEROID_FIELD'], distanceTraveled: 3 } as GameProjection);

    const cards = [...board.querySelectorAll('[data-slot="distance"] image')].map((card) => card.getAttribute('data-art'));
    // drawn first is lowest: the second destination, then the first, then the objective on top
    expect(cards).toEqual(['DestinationCard/ASTEROID_FIELD', 'DestinationCard/ALGAE_PLANET', 'ObjectiveCard/KOBOL']);
    const bottoms = [...board.querySelectorAll('[data-slot="distance"] image')].map((card) => Number(card.getAttribute('y')) + Number(card.getAttribute('height')));
    expect(bottoms).toEqual([DISTANCE.y + 2 * DISTANCE.strip, DISTANCE.y + DISTANCE.strip, SLOTS.objective.y + SLOTS.objective.height]);
    expect(board.querySelector('[data-slot="distance"]')?.textContent).toContain('3');
  });

  it('lays every deck of the rack in its printed slot with its back, a Skill Card back where the deck has none', () => {
    const decks = ['DESTINATION', 'CRISIS', 'DESTINY', 'TACTICS'].map((deck) => ({ deck, size: 5, discardSize: 0, discarded: [], drawPile: null }));
    const board = renderBoard(undefined, { ...game, decks } as unknown as GameProjection);

    expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"] [data-art="DeckId/CRISIS"]')).not.toBeNull();
    expect(board.querySelector('[data-deck="DESTINY"][data-pile="draw"] [data-art="Back/SKILL"]')).not.toBeNull();
    const tactics = board.querySelector('[data-deck="TACTICS"][data-pile="draw"] .hotspot')!;
    expect([tactics.getAttribute('x'), tactics.getAttribute('y')]).toEqual([String(DECK_SLOTS.TACTICS.x), String(DECK_SLOTS.TACTICS.y)]);
  });

  it('keeps the ships of a space area off its printed launch tube', () => {
    const vipers = Array.from({ length: 3 }, (_, index) => ({ id: 10 + index, type: 'VIPER', location: 'GALACTICA_SPACE_4_OCLOCK', pilot: ['LEE_ADAMA', 'KARA_STARBUCK_THRACE', 'KARL_HELO_AGATHON'][index], damage: [], cargo: null }));
    const board = renderBoard(undefined, { ...game, galactica: { ...game.galactica!, ships: vipers } } as unknown as GameProjection);

    const tube = LAUNCH_TUBES.GALACTICA_SPACE_4_OCLOCK!;
    for (const viper of board.querySelectorAll('[data-location="GALACTICA_SPACE_4_OCLOCK"] [data-art="ShipType/VIPER"]')) {
      const left = Number(viper.getAttribute('x'));
      const right = left + Number(viper.getAttribute('width'));
      expect(right <= tube.x || left >= tube.x + tube.width).toBe(true);
    }
  });

  it('lays the decks in the rack with their counts and the top of the discard pile, and the die on the starfield', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"]')?.textContent).toContain('70');
    expect(board.querySelector('[data-deck="CRISIS"][data-pile="discard"] [data-art="CrisisCard/WATER_SABOTAGED"]')).not.toBeNull();
    expect(board.querySelector('[data-deck="POLITICS"][data-pile="discard"] [data-art="SkillCardType/RED_TAPE_0"]')).not.toBeNull();
    expect(board.querySelector('[data-slot="distance"] [data-art="DestinationCard/ALGAE_PLANET"]')).not.toBeNull();
    expect(board.querySelector('.die')?.getAttribute('data-die')).toBe('5');
  });

  it("leaves the card being resolved and its skill check to the turn panel, and the nukes to the Admiral's mat", () => {
    const board = renderBoard();

    // the turn's Crisis Card lies only in its discard pile, the skill check's card nowhere
    expect(board.querySelectorAll('[data-art="CrisisCard/WATER_SABOTAGED"]')).toHaveLength(1);
    expect(board.querySelector('[data-art="CrisisCard/CYLON_SCREENINGS"]')).toBeNull();
    expect(board.querySelector('[data-pile="skill-check"]')).toBeNull();
    expect(board.querySelector('[data-art="Token/NUKE"]')).toBeNull();
  });

  it('lays the Crisis Cards kept in play by the jump track, the civilian ships in the ship reserves, and no damage decks', () => {
    const decks = ['CIVILIAN_SHIPS', 'GALACTICA_DAMAGE', 'PEGASUS_DAMAGE', 'BASESTAR_DAMAGE'].map((deck) => ({ deck, size: 10, discardSize: 0, discarded: [], drawPile: null }));
    const board = renderBoard(undefined, { ...game, decks, crisisCardsInPlay: ['LEGENDARY_DISCOVERY'] } as unknown as GameProjection);

    const kept = board.querySelector('[data-slot="in-play"] image')!;
    expect(kept.getAttribute('data-art')).toBe('CrisisCard/LEGENDARY_DISCOVERY');
    expect(Number(kept.getAttribute('x'))).toBeGreaterThan(JUMP_TRACK.MINUS_1.x);
    const civilians = board.querySelector('[data-slot="reserves"] [data-deck="CIVILIAN_SHIPS"]')!;
    expect(civilians.querySelectorAll('[data-art="ShipType/CIVILIAN"]')).toHaveLength(3);
    expect(civilians.textContent).toContain('10');
    const ships = [...civilians.querySelectorAll('image')].map((ship) => Number(ship.getAttribute('x')) + Number(ship.getAttribute('width')));
    expect(Math.max(...ships)).toBeLessThanOrEqual(SLOTS.shipReserves.x + SLOTS.shipReserves.width);
    expect(board.querySelector('[data-deck$="_DAMAGE"]')).toBeNull();
  });

  it('shows a face-up discard pile on hover, top first, but never a draw pile at a live table', () => {
    const board = renderBoard();

    fireEvent.mouseEnter(board.querySelector('[data-reveal="discard-CRISIS"]')!);
    expect([...document.querySelectorAll('.reveal-pop ol.pile li')].map((card) => card.textContent)).toEqual(['Water Sabotaged', 'Riots Jump']);

    // the deck shows its discard pile too, and nothing of the draw pile
    fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CRISIS"]')!);
    expect([...document.querySelectorAll('.reveal-section-title')].map((title) => title.textContent)).toEqual(['Crisis: discard pile, top first · 2']);
    expect(document.querySelector('.reveal-pop .sandbox-only')).toBeNull();
  });

  it('shows in a sandbox what the table hides, on hover: the draw pile top first', () => {
    const sandbox = {
      ...game,
      viewer: { kind: 'Sandbox' },
      decks: [{ deck: 'CRISIS', size: 2, discardSize: 0, discarded: [], drawPile: ['RIOTS_NO_JUMP', 'FOOD_SHORTAGE'] }],
    } as unknown as GameProjection;
    const board = renderBoard(undefined, sandbox, true);

    fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CRISIS"]')!);
    expect(document.querySelector('.reveal-pop ol.pile li')?.textContent).toBe('Riots No Jump');
    expect(document.querySelector('.reveal-pop .sandbox-only')).not.toBeNull();
  });

  it('lights up the locations an input can be answered with', () => {
    const board = renderBoard(new Set(['ARMORY']));

    expect(board.querySelector('[data-location="ARMORY"]')?.getAttribute('role')).toBe('button');
    expect(board.querySelector('[data-location="RESEARCH_LAB"]')?.getAttribute('role')).toBeNull();
  });

  it('puts a nickname, or the last name, on a token', () => {
    expect(shortName('Kara "Starbuck" Thrace')).toBe('Starbuck');
    expect(shortName('William Adama')).toBe('Adama');
    expect(shortName('Gaius Baltar (alternate)')).toBe('Baltar');
  });
});

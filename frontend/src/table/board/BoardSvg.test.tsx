import { fireEvent, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { BoardSvg, groupShips, shortName } from './BoardSvg';
import { RevealLayer, useReveal } from '../reveal';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, ShipProjection } from '../../wire/types';

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
    // food: the wheel shows 8, one step of 22.5° back gives 7
    expect(board.querySelector('[data-resource="food"] image')?.getAttribute('transform')).toBe('rotate(22.5 3378 210)');
    expect(board.querySelector('[data-resource="fuel"] image')?.getAttribute('transform')).toBe('rotate(0 3158 210)');
  });

  it("lays the decks in their slots with their counts and the top of the discard pile, the skill check in its area", () => {
    const board = renderBoard();

    expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"]')?.textContent).toContain('70');
    expect(board.querySelector('[data-deck="CRISIS"][data-pile="discard"] [data-art="CrisisCard/WATER_SABOTAGED"]')).not.toBeNull();
    expect(board.querySelector('[data-deck="POLITICS"][data-pile="discard"] [data-art="SkillCardType/RED_TAPE_0"]')).not.toBeNull();
    expect(board.querySelector('[data-slot="skill-check"] [data-art="CrisisCard/CYLON_SCREENINGS"]')).not.toBeNull();
    expect(board.querySelectorAll('[data-pile="skill-check"] [data-art="Back/SKILL"]')).toHaveLength(3);
    expect(board.querySelector('[data-slot="skill-check"]')?.textContent).toContain('diff 9');
    expect(board.querySelector('[data-slot="distance"] [data-art="DestinationCard/ALGAE_PLANET"]')).not.toBeNull();
    expect(board.querySelector('.die')?.getAttribute('data-die')).toBe('5');
    expect(board.querySelectorAll('.nukes [data-art="Token/NUKE"]')).toHaveLength(2);
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

  it('shows in a sandbox what the table hides, on hover: the draw pile top first, the face-down cards of a skill check', () => {
    const sandbox = {
      ...game,
      viewer: { kind: 'Sandbox' },
      decks: [{ deck: 'CRISIS', size: 2, discardSize: 0, discarded: [], drawPile: ['RIOTS_NO_JUMP', 'FOOD_SHORTAGE'] }],
      skillCheck: { ...game.skillCheck!, cards: { count: 1, colors: {}, cards: [{ type: 'RED_TAPE', value: 0 }] } },
    } as unknown as GameProjection;
    const board = renderBoard(undefined, sandbox, true);

    fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CRISIS"]')!);
    expect(document.querySelector('.reveal-pop ol.pile li')?.textContent).toBe('Riots No Jump');
    expect(document.querySelector('.reveal-pop .sandbox-only')).not.toBeNull();

    fireEvent.mouseEnter(board.querySelector('[data-reveal="skill-check-cards"]')!);
    expect(document.querySelector('.reveal-pop ol.pile li')?.textContent).toBe('Red Tape 0');
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

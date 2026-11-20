import { fireEvent, render } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { BoardSvg, groupShips, shortName } from './BoardSvg';
import { ArtPreview, usePreview } from '../../art/Art';
import { RevealLayer, useReveal } from '../reveal';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, ShipProjection } from '../../wire/types';
import { DECK_SLOTS, DISTANCE, JUMP_TRACK, LAUNCH_TUBES, LAYOUT, RESOURCES, SLOTS, TITLES } from './layout';

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
    locations: [
      location('RESEARCH_LAB', ['WILLIAM_ADAMA']),
      location('ARMORY', [], true),
      ...['2', '4', '8', '10', '12'].map((hour) => location(`GALACTICA_SPACE_${hour}_OCLOCK`)),
    ],
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
      <ArtPreview />
    </Table.Provider>,
  ).container;
}

afterEach(() => {
  useReveal.getState().hide();
  usePreview.setState({ art: null, pointer: null, extra: null });
});

// where the images of a space area's ships lie
function shipBoxes(board: HTMLElement, location: string) {
  return [...board.querySelectorAll(`[data-location="${location}"] .ship > image`)].map((image) => ({
    art: image.getAttribute('data-art'),
    x: Number(image.getAttribute('x')),
    y: Number(image.getAttribute('y')),
    width: Number(image.getAttribute('width')),
    height: Number(image.getAttribute('height')),
  }));
}

const inSpace = (id: number, type: string, location: string, extra: object = {}) => ({ id, type, location, pilot: null, damage: [], cargo: null, ...extra });

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

  it('labels a damaged location inside it, under its printed name, and puts the markers on the tracks', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-location="ARMORY"]')?.classList).toContain('damaged');
    // a chip like the Engine Room's "activated", at the bottom of the panel the name is printed in
    const label = board.querySelector('[data-location="ARMORY"] .count.damaged-label')!;
    expect(label.textContent).toBe('damaged');
    const ys = label.querySelector('polygon')!.getAttribute('points')!.split(' ').map((point) => Number(point.split(',')[1]));
    const title = TITLES.ARMORY!;
    expect(Math.min(...ys)).toBeGreaterThan(title.y + title.height / 2 + 15);
    expect(Math.max(...ys)).toBeLessThanOrEqual(title.y + title.height);
    expect(board.querySelector('[data-location="RESEARCH_LAB"] .damaged-label')).toBeNull();
    expect(board.querySelector('[data-track="JumpPreparation"]')?.getAttribute('data-position')).toBe('POSITION_2');
    expect(board.querySelector('[data-track="Pursuit"]')?.getAttribute('data-position')).toBe('START');
    expect(board.querySelector('[data-track="BoardingParty"] [data-position="POSITION_1"]')?.getAttribute('aria-label')).toBe('1 centurion at Position 1');
  });

  it('puts each amount after its printed resource, red in the red zone', () => {
    const board = renderBoard();

    const amount = (resource: string) => board.querySelector(`.resource[data-resource="${resource}"]`)!;
    expect(['fuel', 'food', 'morale', 'population'].map((resource) => amount(resource).textContent)).toEqual(['8', '7', '9', '12']);
    expect([amount('fuel').getAttribute('x'), amount('fuel').getAttribute('y')]).toEqual([String(RESOURCES.fuel.x), String(RESOURCES.fuel.y)]);
    expect(amount('food').getAttribute('aria-label')).toBe('Food: 7');
    expect(board.querySelectorAll('.resource.red-zone')).toHaveLength(0);

    // the red zones: fuel and food under 5, morale under 6, population under 7
    const low = renderBoard(undefined, { ...game, galactica: { ...game.galactica!, fuel: 4, food: 5, morale: 5, population: 7 } } as GameProjection);
    expect([...low.querySelectorAll('.resource.red-zone')].map((red) => red.getAttribute('data-resource'))).toEqual(['fuel', 'morale']);
  });

  it('stands each character on the printed name of his location, reaching a little into its picture', () => {
    const board = renderBoard();

    const standee = board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"] image')!;
    const top = Number(standee.getAttribute('y'));
    const bottom = top + Number(standee.getAttribute('height'));
    const title = TITLES.RESEARCH_LAB!;
    expect(bottom).toBeLessThanOrEqual(title.y + title.height);
    expect(bottom).toBeGreaterThan(title.y + title.height - 10);
    expect(top).toBeLessThan(title.y);
    expect(top).toBeGreaterThan(title.y - 30);
  });

  it("shows the player's mat beside the character sheet while the pointer is on his standee", () => {
    const board = renderBoard();

    const standee = board.querySelector('[data-character="WILLIAM_ADAMA"] image')!;
    fireEvent.mouseEnter(standee);
    expect(document.querySelector('.art-preview .art-preview-art img')?.getAttribute('src')).toContain('WILLIAM_ADAMA');
    expect(document.querySelector('.art-preview .art-preview-extra .mat')?.getAttribute('data-player')).toBe('1');
    fireEvent.mouseLeave(standee);
    expect(document.querySelector('.art-preview')).toBeNull();
  });

  it("shows a location's card on hover, and a standee's character sheet over it", () => {
    const board = renderBoard();

    fireEvent.mouseEnter(board.querySelector('[data-location="RESEARCH_LAB"] .location-hotspot')!);
    expect(usePreview.getState().art?.key).toBe('Location/RESEARCH_LAB');
    fireEvent.mouseEnter(board.querySelector('[data-character="WILLIAM_ADAMA"] image')!);
    expect(usePreview.getState().art?.key).toBe('Character/player-token/WILLIAM_ADAMA');
    // a space area has no card
    expect(board.querySelector('[data-location="GALACTICA_SPACE_4_OCLOCK"] .location-hotspot')).toBeNull();
  });

  it('lays the ships of a space area in its middle, the Cylon ships first, each as large as anywhere on its board', () => {
    const ships = [
      inSpace(20, 'VIPER', 'GALACTICA_SPACE_8_OCLOCK'),
      inSpace(21, 'RAIDER', 'GALACTICA_SPACE_8_OCLOCK'),
      inSpace(22, 'RAIDER', 'GALACTICA_SPACE_8_OCLOCK'),
      inSpace(23, 'BASESTAR', 'GALACTICA_SPACE_8_OCLOCK'),
      inSpace(24, 'VIPER', 'GALACTICA_SPACE_12_OCLOCK'),
      inSpace(25, 'VIPER', 'GALACTICA_SPACE_10_OCLOCK'),
      inSpace(26, 'VIPER', 'GALACTICA_SPACE_10_OCLOCK'),
    ];
    const board = renderBoard(undefined, { ...game, galactica: { ...game.galactica!, ships } } as unknown as GameProjection);

    // a lone ship in the middle of its area
    const [lone] = shipBoxes(board, 'GALACTICA_SPACE_12_OCLOCK');
    const area = LAYOUT.GALACTICA_SPACE_12_OCLOCK;
    expect(lone.x + lone.width / 2).toBeCloseTo(area.x + area.width / 2);
    expect(lone.y + lone.height / 2).toBeCloseTo(area.y + area.height / 2);

    // the basestar first, then the raiders, then the viper, the whole lot in the middle of the strip
    const strip = shipBoxes(board, 'GALACTICA_SPACE_8_OCLOCK');
    expect(strip.map((ship) => ship.art)).toEqual(['ShipType/BASESTAR', 'ShipType/RAIDER', 'ShipType/RAIDER', 'ShipType/VIPER']);
    const narrow = LAYOUT.GALACTICA_SPACE_8_OCLOCK;
    const top = Math.min(...strip.map((ship) => ship.y));
    const bottom = Math.max(...strip.map((ship) => ship.y + ship.height));
    expect(Math.abs(top - narrow.y - (narrow.y + narrow.height - bottom))).toBeLessThan(10);

    // a viper is as large alone, piled and in the narrow strip
    const vipers = [lone, ...shipBoxes(board, 'GALACTICA_SPACE_10_OCLOCK'), strip[3]];
    expect(new Set(vipers.map((viper) => `${viper.width}×${viper.height}`)).size).toBe(1);
  });

  it("shows what a civilian ship costs when destroyed on hover, in a sandbox, and to no one who doesn't know it", () => {
    const civilians = [
      inSpace(30, 'CIVILIAN', 'GALACTICA_SPACE_2_OCLOCK', { cargo: { fuel: 1, morale: 0, population: 1 } }),
      inSpace(31, 'CIVILIAN', 'GALACTICA_SPACE_2_OCLOCK', { cargo: { fuel: 0, morale: 0, population: 0 } }),
    ];
    const sandbox = { ...game, viewer: { kind: 'Sandbox' }, galactica: { ...game.galactica!, ships: civilians } } as unknown as GameProjection;
    const board = renderBoard(undefined, sandbox, true);

    fireEvent.mouseEnter(board.querySelector('[data-reveal="cargo-30"]')!);
    const lines = [...document.querySelectorAll('.reveal-pop ol.pile li')];
    expect(lines.map((line) => line.textContent)).toEqual(['11Civilian ship #30', 'nothingCivilian ship #31']);
    expect(lines[0].querySelector('.cargo')?.getAttribute('aria-label')).toBe('1 population, 1 fuel');
    expect([...lines[0].querySelectorAll('.cargo-amount')].map((amount) => amount.getAttribute('data-resource'))).toEqual(['population', 'fuel']);
    expect(document.querySelector('.reveal-pop .sandbox-only')).not.toBeNull();
    // the ship's own preview would cover the costs
    fireEvent.mouseEnter(board.querySelector('[data-ship="30"] image')!);
    expect(usePreview.getState().art).toBeNull();

    const unknown = { ...game, galactica: { ...game.galactica!, ships: civilians.map((ship) => ({ ...ship, cargo: null })) } } as unknown as GameProjection;
    const table = renderBoard(undefined, unknown);
    expect(table.querySelector('[data-reveal^="cargo-"]')).toBeNull();
  });

  it("names what each civilian ship of the deck costs, in a sandbox's draw pile", () => {
    const decks = [{ deck: 'CIVILIAN_SHIPS', size: 1, discardSize: 0, discarded: [], drawPile: [{ id: 40, fuelCost: 0, moraleCost: 1, populationCost: 1 }] }];
    const board = renderBoard(undefined, { ...game, viewer: { kind: 'Sandbox' }, decks } as unknown as GameProjection, true);

    fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CIVILIAN_SHIPS"]')!);
    expect(document.querySelector('.reveal-pop ol.pile li')?.textContent).toBe('civilian ship #40: 1 population, 1 morale');
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

  it('lays the decks in the rack with their counts and the top of the discard pile over the deck, clear of the next', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"]')?.textContent).toContain('70');
    const discard = board.querySelector('[data-deck="CRISIS"][data-pile="discard"] [data-art="CrisisCard/WATER_SABOTAGED"]')!;
    const right = Number(discard.getAttribute('x')) + Number(discard.getAttribute('width'));
    expect(right).toBeLessThan(DECK_SLOTS.SUPER_CRISIS.x);
    expect(board.querySelector('[data-deck="POLITICS"][data-pile="discard"] [data-art="SkillCardType/RED_TAPE_0"]')).not.toBeNull();
    expect(board.querySelector('[data-slot="distance"] [data-art="DestinationCard/ALGAE_PLANET"]')).not.toBeNull();
  });

  it("leaves the card being resolved, its skill check and the die to the turn panel, and the nukes to the Admiral's mat", () => {
    const board = renderBoard();

    // the turn's Crisis Card lies only in its discard pile, the skill check's card nowhere
    expect(board.querySelectorAll('[data-art="CrisisCard/WATER_SABOTAGED"]')).toHaveLength(1);
    expect(board.querySelector('[data-art="CrisisCard/CYLON_SCREENINGS"]')).toBeNull();
    expect(board.querySelector('[data-pile="skill-check"]')).toBeNull();
    expect(board.querySelector('[data-art^="Die/"]')).toBeNull();
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

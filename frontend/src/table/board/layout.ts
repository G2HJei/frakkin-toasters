// Where everything lies on board.jpg, in the image's own pixels (4000×2000): the printed location cards, the space
// areas between the blue lines, and the printed slots of the decks, tracks, dials and reserves. A test checks that every
// Location of the schema has a place here.
//
// Galactica's bow points left: the area in front of it (8 o'clock, where Cylon ships appear) is the wedge at the bow,
// the one behind it (2 o'clock, where civilian ships appear) the wedge at the stern, the others clockwise. The Cylon
// Fleet board's areas sit around the basestar at the clock positions of the Galactica areas they lead to.
import type { Location } from '../../wire/types';

export const BOARD_WIDTH = 4000;
export const BOARD_HEIGHT = 2000;

export type BoardArea = 'galactica' | 'galacticaSpace' | 'colonialOne' | 'pegasus' | 'cylon' | 'cylonFleet' | 'cylonFleetSpace';

export interface Place {
  x: number;
  y: number;
  width: number;
  height: number;
  area: BoardArea;
}

export interface Box {
  x: number;
  y: number;
  width: number;
  height: number;
}

const card = (x: number, y: number, width: number, height: number, area: BoardArea): Place => ({ x, y, width, height, area });

export const LAYOUT: Record<Location, Place> = {
  FTL_CONTROL: card(1453, 950, 149, 215, 'galactica'),
  WEAPONS_CONTROL: card(1640, 822, 155, 216, 'galactica'),
  COMMUNICATIONS: card(1888, 822, 157, 216, 'galactica'),
  RESEARCH_LAB: card(2133, 822, 157, 216, 'galactica'),
  ARMORY: card(2340, 950, 155, 215, 'galactica'),
  COMMAND: card(1640, 1080, 155, 215, 'galactica'),
  ADMIRALS_QUARTERS: card(1888, 1080, 157, 215, 'galactica'),
  HANGAR_DECK: card(2133, 1080, 157, 215, 'galactica'),
  SICKBAY: card(2585, 815, 160, 215, 'galactica'),
  BRIG: card(2563, 1085, 180, 225, 'galactica'),
  // the space areas: the box their ships are laid out in
  GALACTICA_SPACE_12_OCLOCK: card(2210, 505, 600, 250, 'galacticaSpace'),
  GALACTICA_SPACE_2_OCLOCK: card(2880, 700, 260, 680, 'galacticaSpace'),
  GALACTICA_SPACE_4_OCLOCK: card(2220, 1400, 620, 185, 'galacticaSpace'),
  GALACTICA_SPACE_6_OCLOCK: card(1500, 1400, 600, 185, 'galacticaSpace'),
  GALACTICA_SPACE_8_OCLOCK: card(1175, 680, 260, 700, 'galacticaSpace'),
  GALACTICA_SPACE_10_OCLOCK: card(1460, 505, 640, 250, 'galacticaSpace'),
  QUORUM_CHAMBER: card(3180, 878, 150, 212, 'colonialOne'),
  PRESS_ROOM: card(3363, 878, 152, 212, 'colonialOne'),
  PRESIDENTS_OFFICE: card(3548, 878, 152, 212, 'colonialOne'),
  ADMINISTRATION: card(3735, 878, 150, 212, 'colonialOne'),
  PEGASUS_CIC: card(3163, 1285, 152, 220, 'pegasus'),
  AIRLOCK: card(3350, 1350, 175, 215, 'pegasus'),
  MAIN_BATTERIES: card(3560, 1350, 170, 215, 'pegasus'),
  ENGINE_ROOM: card(3745, 1290, 155, 215, 'pegasus'),
  CAPRICA: card(113, 555, 154, 215, 'cylon'),
  CYLON_FLEET: card(993, 555, 154, 215, 'cylon'),
  HUMAN_FLEET: card(113, 1230, 154, 215, 'cylon'),
  RESURRECTION_SHIP: card(990, 1225, 160, 215, 'cylon'),
  BASESTAR_BRIDGE: card(557, 893, 155, 212, 'cylonFleet'),
  // Cylon Fleet space N leads to the Galactica area at the same clock position (CylonFleetBoard in the core)
  CYLON_FLEET_SPACE_1: card(880, 1035, 270, 150, 'cylonFleetSpace'),
  CYLON_FLEET_SPACE_2: card(660, 1235, 300, 200, 'cylonFleetSpace'),
  CYLON_FLEET_SPACE_3: card(660, 580, 320, 200, 'cylonFleetSpace'),
  CYLON_FLEET_SPACE_4: card(290, 1235, 320, 200, 'cylonFleetSpace'),
  CYLON_FLEET_SPACE_5_6: card(290, 580, 310, 200, 'cylonFleetSpace'),
  CYLON_FLEET_SPACE_7_8: card(115, 795, 310, 180, 'cylonFleetSpace'),
};

// the centre of each printed position of a track
export const JUMP_TRACK: Record<string, { x: number; y: number }> = {
  START: { x: 1840, y: 170 },
  POSITION_1: { x: 1940, y: 170 },
  POSITION_2: { x: 2035, y: 170 },
  MINUS_3: { x: 2135, y: 170 },
  MINUS_1: { x: 2235, y: 170 },
};

export const PURSUIT_TRACK: Record<string, { x: number; y: number }> = {
  START: { x: 485, y: 465 },
  ONE_CIVILIAN_SHIP: { x: 585, y: 465 },
  POSITION_2: { x: 683, y: 465 },
  TWO_CIVILIAN_SHIPS: { x: 783, y: 465 },
  AUTO_ATTACK: { x: 840, y: 465 },
};

export const BOARDING_TRACK: Record<string, { x: number; y: number }> = {
  START: { x: 1322, y: 402 },
  POSITION_1: { x: 1420, y: 402 },
  POSITION_2: { x: 1490, y: 402 },
  POSITION_3: { x: 1560, y: 402 },
  HUMANS_LOSE: { x: 1725, y: 402 },
};

// the printed dials: their centre, their radius, and the value the printed wheel shows under the pointer
export const DIALS: Record<'fuel' | 'food' | 'morale' | 'population', { x: number; y: number; r: number; printed: number }> = {
  fuel: { x: 3158, y: 210, r: 101, printed: 8 },
  food: { x: 3378, y: 210, r: 101, printed: 8 },
  morale: { x: 3595, y: 210, r: 101, printed: 10 },
  population: { x: 3816, y: 210, r: 101, printed: 12 },
};

const box = (x: number, y: number, width: number, height: number): Box => ({ x, y, width, height });

/** The printed slots, and the free space for what has none. */
export const SLOTS = {
  cylonReserves: box(110, 20, 285, 420),
  damagedVipers: box(1190, 120, 300, 180),
  shipReserves: box(2570, 120, 380, 180),
  civilianShips: box(3400, 460, 280, 210),
  destinationDeck: box(1555, 80, 160, 220),
  distanceTraveled: box(2355, 80, 160, 220),
  skillCheck: box(1800, 250, 475, 230),
  basestarDamage: box(915, 425, 85, 85),
  galacticaDamage: box(2330, 1236, 85, 85),
  pegasusDamage: box(3643, 1253, 85, 85),
  // the deck row
  crisisDeck: box(100, 1604, 214, 316),
  crisisDiscard: box(330, 1604, 214, 316),
  superCrisis: box(596, 1604, 212, 316),
  mutiny: box(856, 1604, 214, 316),
  loyalty: box(1116, 1604, 214, 316),
  destiny: box(3738, 1688, 162, 232),
  // no printed slot: the free starfield
  turnCrisis: box(440, 30, 330, 230),
  inPlay: box(790, 30, 330, 230),
  die: box(470, 275, 100, 107),
  motive: box(990, 270, 100, 140),
  quorum: box(3190, 430, 140, 210),
  objective: box(3730, 430, 136, 210),
  nukes: box(3880, 440, 110, 220),
};

// each Skill Card deck and its discard pile, in the deck row
export const SKILL_SLOTS: Record<string, { deck: Box; discard: Box }> = Object.fromEntries(
  [
    ['POLITICS', 1396],
    ['LEADERSHIP', 1780],
    ['TACTICS', 2166],
    ['PILOTING', 2552],
    ['ENGINEERING', 2936],
    ['TREACHERY', 3322],
  ].map(([color, x]) => [color, { deck: box(x as number, 1688, 162, 232), discard: box((x as number) + 182, 1688, 162, 232) }]),
);

export function isSpace(location: string | null | undefined): boolean {
  const place = location ? LAYOUT[location as Location] : undefined;
  return place?.area === 'galacticaSpace' || place?.area === 'cylonFleetSpace';
}

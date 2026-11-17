// Where each location sits on the board, in the SVG's coordinates: Galactica's rooms inside its hull, its six space
// areas around it in clock order, Colonial One, Pegasus and the Cylon locations beside it, and the Cylon Fleet board
// below. A room is shaped like its location card. A test checks that every Location of the schema has a place here.
import type { Location } from '../../wire/types';

export const BOARD_WIDTH = 1240;
export const BOARD_HEIGHT = 830;

export type BoardArea = 'galactica' | 'galacticaSpace' | 'colonialOne' | 'pegasus' | 'cylon' | 'cylonFleet' | 'cylonFleetSpace';

export interface Place {
  x: number;
  y: number;
  width: number;
  height: number;
  area: BoardArea;
}

// the strip above the boards: the dials, the tracks, the objective and the destinations
export const STRIP = { y: 8, height: 112 };

export const HULL = { x: 220, y: 262, width: 480, height: 272 };

const ROOM = { width: 86, height: 112 };

function room(column: number, row: number): Place {
  return { x: HULL.x + 11 + column * 92, y: HULL.y + 22 + row * 120, ...ROOM, area: 'galactica' };
}

function space(x: number, y: number, width: number, height: number): Place {
  return { x, y, width, height, area: 'galacticaSpace' };
}

export const SIDE = { x: 930, width: 300 };

const SIDE_CARD = { width: 70, height: 92, gap: 6 };

export const SIDE_ROWS = { colonialOne: 166, pegasus: 284, cylon: 402 };

function side(index: number, area: 'colonialOne' | 'pegasus' | 'cylon'): Place {
  return { x: SIDE.x + index * (SIDE_CARD.width + SIDE_CARD.gap), y: SIDE_ROWS[area], width: SIDE_CARD.width, height: SIDE_CARD.height, area };
}

export const FLEET = { y: 680 };

function fleetSpace(index: number): Place {
  return { x: 120 + index * 186, y: FLEET.y, width: 178, height: 141, area: 'cylonFleetSpace' };
}

export const LAYOUT: Record<Location, Place> = {
  FTL_CONTROL: room(0, 0),
  WEAPONS_CONTROL: room(1, 0),
  COMMUNICATIONS: room(2, 0),
  RESEARCH_LAB: room(3, 0),
  ARMORY: room(4, 0),
  COMMAND: room(0, 1),
  ADMIRALS_QUARTERS: room(1, 1),
  HANGAR_DECK: room(2, 1),
  SICKBAY: room(3, 1),
  BRIG: room(4, 1),
  GALACTICA_SPACE_12_OCLOCK: space(330, 150, 260, 104),
  GALACTICA_SPACE_2_OCLOCK: space(710, 205, 200, 140),
  GALACTICA_SPACE_4_OCLOCK: space(710, 452, 200, 140),
  GALACTICA_SPACE_6_OCLOCK: space(330, 542, 260, 104),
  GALACTICA_SPACE_8_OCLOCK: space(10, 452, 200, 140),
  GALACTICA_SPACE_10_OCLOCK: space(10, 205, 200, 140),
  QUORUM_CHAMBER: side(0, 'colonialOne'),
  PRESS_ROOM: side(1, 'colonialOne'),
  PRESIDENTS_OFFICE: side(2, 'colonialOne'),
  ADMINISTRATION: side(3, 'colonialOne'),
  PEGASUS_CIC: side(0, 'pegasus'),
  AIRLOCK: side(1, 'pegasus'),
  MAIN_BATTERIES: side(2, 'pegasus'),
  ENGINE_ROOM: side(3, 'pegasus'),
  CAPRICA: side(0, 'cylon'),
  CYLON_FLEET: side(1, 'cylon'),
  HUMAN_FLEET: side(2, 'cylon'),
  RESURRECTION_SHIP: side(3, 'cylon'),
  BASESTAR_BRIDGE: { x: 10, y: FLEET.y, width: 100, height: 141, area: 'cylonFleet' },
  CYLON_FLEET_SPACE_1: fleetSpace(0),
  CYLON_FLEET_SPACE_2: fleetSpace(1),
  CYLON_FLEET_SPACE_3: fleetSpace(2),
  CYLON_FLEET_SPACE_4: fleetSpace(3),
  CYLON_FLEET_SPACE_5_6: fleetSpace(4),
  CYLON_FLEET_SPACE_7_8: fleetSpace(5),
};

export const AREA_TITLES: { area: BoardArea; title: string; x: number; y: number }[] = [
  { area: 'galactica', title: 'Galactica', x: HULL.x + 12, y: HULL.y + 16 },
  { area: 'colonialOne', title: 'Colonial One', x: SIDE.x, y: SIDE_ROWS.colonialOne - 7 },
  { area: 'pegasus', title: 'Pegasus', x: SIDE.x, y: SIDE_ROWS.pegasus - 7 },
  { area: 'cylon', title: 'Cylon locations', x: SIDE.x, y: SIDE_ROWS.cylon - 7 },
  { area: 'cylonFleet', title: 'Cylon Fleet board', x: 10, y: FLEET.y - 8 },
];

export function isSpace(location: string | null | undefined): boolean {
  const place = location ? LAYOUT[location as Location] : undefined;
  return place?.area === 'galacticaSpace' || place?.area === 'cylonFleetSpace';
}

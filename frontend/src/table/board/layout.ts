// Where each location sits on the schematic board, in the SVG's coordinates. Our own layout: Galactica's locations
// inside its hull, its six space areas around it in clock order, Colonial One, Pegasus and the Cylon locations beside
// it, and the Cylon Fleet board below. A test checks that every Location of the schema has a place here.
import type { Location } from '../../wire/types';

export const BOARD_WIDTH = 1140;
export const BOARD_HEIGHT = 780;

export type BoardArea = 'galactica' | 'galacticaSpace' | 'colonialOne' | 'pegasus' | 'cylon' | 'cylonFleet' | 'cylonFleetSpace';

export interface Place {
  x: number;
  y: number;
  width: number;
  height: number;
  area: BoardArea;
}

export const HULL = { x: 250, y: 250, width: 440, height: 210 };

const ROOM = { width: 80, height: 82 };

function room(column: number, row: number): Place {
  return { x: HULL.x + 12 + column * 85, y: HULL.y + 22 + row * 95, ...ROOM, area: 'galactica' };
}

// The space areas around the hull, by the clock.
function space(centerX: number, centerY: number): Place {
  return { x: centerX - 90, y: centerY - 48, width: 180, height: 96, area: 'galacticaSpace' };
}

function side(x: number, y: number, area: BoardArea): Place {
  return { x, y, width: 112, height: 70, area };
}

function fleetSpace(index: number): Place {
  return { x: 170 + index * 150, y: 640, width: 140, height: 120, area: 'cylonFleetSpace' };
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
  GALACTICA_SPACE_12_OCLOCK: space(470, 175),
  GALACTICA_SPACE_2_OCLOCK: space(790, 245),
  GALACTICA_SPACE_4_OCLOCK: space(790, 465),
  GALACTICA_SPACE_6_OCLOCK: space(470, 535),
  GALACTICA_SPACE_8_OCLOCK: space(150, 465),
  GALACTICA_SPACE_10_OCLOCK: space(150, 245),
  QUORUM_CHAMBER: side(902, 30, 'colonialOne'),
  PRESS_ROOM: side(1020, 30, 'colonialOne'),
  PRESIDENTS_OFFICE: side(902, 110, 'colonialOne'),
  ADMINISTRATION: side(1020, 110, 'colonialOne'),
  PEGASUS_CIC: side(902, 225, 'pegasus'),
  AIRLOCK: side(1020, 225, 'pegasus'),
  MAIN_BATTERIES: side(902, 305, 'pegasus'),
  ENGINE_ROOM: side(1020, 305, 'pegasus'),
  CAPRICA: side(902, 420, 'cylon'),
  CYLON_FLEET: side(1020, 420, 'cylon'),
  HUMAN_FLEET: side(902, 500, 'cylon'),
  RESURRECTION_SHIP: side(1020, 500, 'cylon'),
  BASESTAR_BRIDGE: { x: 20, y: 640, width: 140, height: 120, area: 'cylonFleet' },
  CYLON_FLEET_SPACE_1: fleetSpace(0),
  CYLON_FLEET_SPACE_2: fleetSpace(1),
  CYLON_FLEET_SPACE_3: fleetSpace(2),
  CYLON_FLEET_SPACE_4: fleetSpace(3),
  CYLON_FLEET_SPACE_5_6: fleetSpace(4),
  CYLON_FLEET_SPACE_7_8: fleetSpace(5),
};

export const AREA_TITLES: { area: BoardArea; title: string; x: number; y: number }[] = [
  { area: 'galactica', title: 'Galactica', x: HULL.x + 12, y: HULL.y + 16 },
  { area: 'colonialOne', title: 'Colonial One', x: 902, y: 22 },
  { area: 'pegasus', title: 'Pegasus', x: 902, y: 217 },
  { area: 'cylon', title: 'Cylon locations', x: 902, y: 412 },
  { area: 'cylonFleet', title: 'Cylon Fleet board', x: 20, y: 632 },
];

export function isSpace(location: string | null | undefined): boolean {
  const place = location ? LAYOUT[location as Location] : undefined;
  return place?.area === 'galacticaSpace' || place?.area === 'cylonFleetSpace';
}

// Where everything lies on board.jpg, in the image's own pixels (2003×2317). board.layout.json, measured with the
// image, has what is printed: the location cards and the panels their names are printed in, the space areas, the
// tracks, the resources, the deck rack and the reserves. The free starfield for the little that has no printed slot is
// below. A test checks that every Location of the schema has a place here.
//
// Galactica's bow points left: the area in front of it (8 o'clock, where Cylon ships appear) is the strip at the bow,
// the one behind it (2 o'clock, where civilian ships appear) the strip at the stern, the others clockwise. The Cylon
// Fleet board's areas sit around the basestar at the clock positions of the Galactica areas they lead to.
import type { Location } from '../../wire/types';
import printed from './board.layout.json';
import type { ResourceName } from './resources';

export const BOARD_WIDTH = printed.width;
export const BOARD_HEIGHT = printed.height;

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

export interface Point {
  x: number;
  y: number;
}

// the json's boxes are [x, y, width, height], and a location's ends with its BoardArea
type Printed = readonly (number | string)[];

const box = (x: number, y: number, width: number, height: number): Box => ({ x, y, width, height });
const printedBox = ([x, y, width, height]: Printed): Box => box(x as number, y as number, width as number, height as number);
const printedPlace = (value: Printed): Place => ({ ...printedBox(value), area: value[4] as BoardArea });

function byName<T>(entries: Record<string, Printed>, read: (value: Printed) => T): Record<string, T> {
  return Object.fromEntries(Object.entries(entries).map(([name, value]) => [name, read(value)]));
}

// the location cards, and the space areas: the box their ships are laid out in
export const LAYOUT = {
  ...byName(printed.locations, printedPlace),
  ...byName(printed.spaceAreas, printedPlace),
} as Record<Location, Place>;

// the panel each location's name is printed in, under its picture
export const TITLES = byName(printed.locationTitles, printedBox) as Partial<Record<Location, Box>>;

// the centre of each printed position of a track
export const JUMP_TRACK: Record<string, Point> = printed.tracks.jump;
export const PURSUIT_TRACK: Record<string, Point> = printed.tracks.pursuit;
export const BOARDING_TRACK: Record<string, Point> = printed.tracks.boarding;

// the resources, printed in a row, each an icon and a label ("FUEL:"), and where the amount goes after the label: the
// left end of the text and its baseline
export const RESOURCES: Record<ResourceName, Point> = {
  fuel: printed.resources.fuel,
  food: printed.resources.food,
  morale: printed.resources.morale,
  population: printed.resources.population,
};

// the deck rack: a printed slot per deck, by DeckId ("deck.superCrisis" is SUPER_CRISIS)
export const DECK_SLOTS: Record<string, Box> = Object.fromEntries(
  Object.entries(printed.slots)
    .filter(([name]) => name.startsWith('deck.'))
    .map(([name, value]) => [name.slice('deck.'.length).replace(/([a-z])([A-Z])/g, '$1_$2').toUpperCase(), printedBox(value as Printed)]),
);

// the printed launch tubes at the stern, which the ships of their area leave free
export const LAUNCH_TUBES: Partial<Record<Location, Box>> = Object.fromEntries(
  Object.entries(printed.slots)
    .filter(([name]) => name.startsWith('launch.'))
    .map(([name, value]) => [name.slice('launch.'.length), printedBox(value as Printed)]),
);

// the destinations tuck under the objective, a strip of each showing its distance below it
export const DISTANCE = printed.slots.distanceTraveled;

/** The printed slots, and the free starfield for what has none. */
export const SLOTS = {
  objective: printedBox(printed.slots.objective),
  cylonReserves: printedBox(printed.slots.cylonReserves),
  damagedVipers: printedBox(printed.slots.damagedVipers),
  shipReserves: printedBox(printed.slots.shipReserves),
  // not printed: the Boarding Party track lies over Galactica's hull while a centurion is on it
  boardingParty: printedBox(printed.slots.boardingParty),
  // no printed slot: the free starfield
  inPlay: box(785, 1262, 110, 155),
};

export function isSpace(location: string | null | undefined): boolean {
  const place = location ? LAYOUT[location as Location] : undefined;
  return place?.area === 'galacticaSpace' || place?.area === 'cylonFleetSpace';
}

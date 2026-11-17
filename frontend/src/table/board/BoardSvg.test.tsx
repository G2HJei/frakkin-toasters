import { render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { BoardSvg, shortName } from './BoardSvg';
import { Table, tableContext } from '../tableContext';
import type { GameProjection } from '../../wire/types';

const location = (name: string, characters: string[] = [], damaged = false) => ({ location: name, damaged, characters });

// A projection as the backend sends it, cut down to what the board draws.
const game = {
  viewer: { kind: 'Observer' },
  titles: { president: 'LAURA_ROSLIN', admiral: 'WILLIAM_ADAMA', cag: 'KARA_STARBUCK_THRACE' },
  galactica: {
    fuel: 8,
    food: 7,
    morale: 9,
    population: 12,
    jumpPreparation: 'POSITION_2',
    locations: [location('RESEARCH_LAB', ['WILLIAM_ADAMA']), location('ARMORY', [], true), location('GALACTICA_SPACE_4_OCLOCK')],
    ships: [{ id: 3, type: 'VIPER', location: 'GALACTICA_SPACE_4_OCLOCK', pilot: 'KARA_STARBUCK_THRACE', damage: [], cargo: null }],
    reserves: [],
    damagedVipers: [],
    boardingParty: [{ id: 1040, position: 'POSITION_1' }],
  },
  pegasus: { engineRoomActivated: false, locations: [location('AIRLOCK', ['LAURA_ROSLIN'])] },
  cylonFleet: {
    pursuit: 'START',
    locations: [location('CYLON_FLEET_SPACE_2')],
    ships: [{ id: 1019, type: 'RAIDER', location: 'CYLON_FLEET_SPACE_2', pilot: null, damage: [], cargo: null }],
  },
  destinations: [],
  distanceTraveled: 0,
  nukes: 2,
} as unknown as GameProjection;

function renderBoard(targets?: Set<string>) {
  return render(
    <Table.Provider value={tableContext(game, false, null)}>
      <BoardSvg targets={targets} />
    </Table.Provider>,
  ).container;
}

describe('the board', () => {
  it('draws each character at the location the projection puts him', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"]')).not.toBeNull();
    expect(board.querySelector('[data-location="AIRLOCK"] [data-character="LAURA_ROSLIN"]')).not.toBeNull();
    expect(board.querySelectorAll('[data-character="WILLIAM_ADAMA"]')).toHaveLength(1);
  });

  it('draws each ship in its space area, with its pilot', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-location="GALACTICA_SPACE_4_OCLOCK"] [data-ship="3"]')?.textContent).toBe('Viper #3 · Starbuck');
    expect(board.querySelector('[data-location="CYLON_FLEET_SPACE_2"] [data-ship="1019"]')?.textContent).toBe('Raider #1019');
  });

  it('marks a damaged location, the tracks and the dials', () => {
    const board = renderBoard();

    expect(board.querySelector('[data-location="ARMORY"]')?.classList).toContain('damaged');
    expect(board.querySelector('.step.current')?.textContent).toBe('Position 2');
    expect([...board.querySelectorAll('.dial-value')].map((dial) => dial.textContent)).toEqual(['8', '7', '9', '12']);
    expect(board.textContent).toContain('Position 1 ×1'); // a centurion on the boarding party track
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

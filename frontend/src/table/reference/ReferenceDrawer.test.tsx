import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { ReferenceDrawer, search, useReference } from './ReferenceDrawer';
import { schema } from '../../wire/schema';
import type { ArtRef } from '../../art/images';
import type { GameProjection } from '../../wire/types';
import { Table, tableContext } from '../tableContext';

afterEach(() => act(() => useReference.getState().close()));

describe('the reference', () => {
  it('finds a card by the words of its name', () => {
    const cards = [{ label: 'Water Sabotaged' }, { label: 'Food Shortage' }, { label: 'Water Shortage' }] as ArtRef[];

    expect(search(cards, 'water').map((card) => card.label)).toEqual(['Water Sabotaged', 'Water Shortage']);
    expect(search(cards, 'shortage food').map((card) => card.label)).toEqual(['Food Shortage']);
    expect(search(cards, '  ')).toBe(cards);
  });

  it('opens at the cheat sheets', () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show());

    expect(screen.getByRole('tab', { name: 'Cheat sheets' })).toHaveAttribute('aria-selected', 'true');
    expect(document.querySelector('.cheat-sheet')).not.toBeNull();
  });

  it('opens and closes on R', () => {
    render(<ReferenceDrawer />);

    fireEvent.keyDown(window, { key: 'r', code: 'KeyR' });
    expect(screen.getByRole('dialog', { name: 'reference' })).toBeInTheDocument();
    fireEvent.keyDown(window, { key: 'Escape', code: 'Escape' });
    expect(screen.getByRole('dialog', { name: 'reference' })).toBeInTheDocument();
    fireEvent.keyDown(window, { key: 'R', code: 'KeyR', shiftKey: true });
    expect(screen.queryByRole('dialog')).toBeNull();

    // not with Ctrl, the browser's reload, but the R key of a Cyrillic layout
    fireEvent.keyDown(window, { key: 'r', code: 'KeyR', ctrlKey: true });
    expect(screen.queryByRole('dialog')).toBeNull();
    fireEvent.keyDown(window, { key: 'р', code: 'KeyR' });
    expect(screen.getByRole('dialog', { name: 'reference' })).toBeInTheDocument();
  });

  it('takes an R typed into a field as text', () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show('cards'));

    // the search doesn't close it
    fireEvent.keyDown(screen.getByLabelText('search'), { key: 'r', code: 'KeyR' });
    expect(screen.getByRole('dialog', { name: 'reference' })).toBeInTheDocument();

    // a field elsewhere on the table doesn't open it
    act(() => useReference.getState().close());
    const field = document.body.appendChild(document.createElement('input'));
    fireEvent.keyDown(field, { key: 'r', code: 'KeyR' });
    expect(screen.queryByRole('dialog')).toBeNull();
    field.remove();
  });

  it('lists every deck and narrows it to the search', () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show('cards'));

    expect(screen.getByRole('heading', { name: /^Crisis Cards/ })).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText('search'), { target: { value: 'Riots No Jump' } });
    expect(screen.getAllByRole('heading', { level: 3 }).map((heading) => heading.textContent)).toEqual(['Crisis Cards 1']);
  });

  it("shows the Attack Table as the game applies it, and a character's sheet first", () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show('sheets'));

    const rows = document.querySelectorAll('.attack-table tbody tr');
    expect(rows).toHaveLength((schema as unknown as { attackTable: unknown[] }).attackTable.length);
    expect(rows[0].textContent).toBe('ViperRaider3–8Destroyed');

    act(() => useReference.getState().show('characters', 'LAURA_ROSLIN'));
    expect(document.querySelector('.reference-grid li.focus img')?.getAttribute('alt')).toBe('Laura Roslin');
  });

  it("lists each title's line of succession in a column, a portrait and a name on each row", () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show('titles'));

    const columns = [...document.querySelectorAll('.succession-line')];
    expect(columns.map((column) => column.querySelector('h3')?.textContent)).toEqual(['President', 'Admiral', 'CAG']);
    const rows = (column: Element) => [...column.querySelectorAll('li')];
    expect(rows(columns[0])).toHaveLength(schema.linesOfSuccession?.PRESIDENT.length ?? -1);
    expect(rows(columns[0])[0].textContent).toBe('Laura Roslin');
    expect(rows(columns[0])[0].querySelector('img.succession-portrait')?.getAttribute('alt')).toBe('Laura Roslin');
    expect(rows(columns[1])[0].textContent).toBe('Helena Cain');
    expect(rows(columns[2])[0].textContent).toBe('Lee "Apollo" Adama');
    // an alternate sheet is named like its character
    expect(columns.flatMap(rows).map((row) => row.textContent).filter((name) => /alternate/i.test(name ?? ''))).toEqual([]);
  });

  it('lists what left the game, and counts it on its tab', () => {
    const ship = (id: number, type: string, cargo: unknown) => ({ id, type, location: null, pilot: null, damage: [], cargo });
    const game = {
      players: [],
      removedCharacters: ['SAUL_TIGH'],
      destroyedShips: [ship(2012, 'CIVILIAN', { fuel: 0, morale: 1, population: 1 }), ship(11, 'RAPTOR', null)],
    } as unknown as GameProjection;
    render(
      <Table.Provider value={tableContext(game, false, 1)}>
        <ReferenceDrawer />
      </Table.Provider>,
    );
    act(() => useReference.getState().show('out'));

    expect(screen.getByRole('tab', { name: 'Out of the game 3' })).toHaveAttribute('aria-selected', 'true');
    expect(document.querySelector('.reference-grid.sheets img')?.getAttribute('alt')).toBe('Saul Tigh');
    const ships = [...document.querySelectorAll('.reference-ships li')];
    expect(ships.map((row) => row.querySelector('span')?.textContent)).toEqual(['Civilian ship #2012', 'Raptor #11']);
    expect(ships[0].querySelector('.cargo')).not.toBeNull();
    expect(ships[1].querySelector('.cargo')).toBeNull();
  });

  it('says when nothing left the game', () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show('out'));

    expect(screen.getByRole('tab', { name: 'Out of the game' })).toBeInTheDocument();
    expect(screen.getByText('Nothing has left the game yet.')).toBeInTheDocument();
  });
});

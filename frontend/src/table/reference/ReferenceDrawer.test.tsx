import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';
import { ReferenceDrawer, search, useReference } from './ReferenceDrawer';
import { schema } from '../../wire/schema';
import type { ArtRef } from '../../art/images';

afterEach(() => act(() => useReference.getState().close()));

describe('the reference', () => {
  it('finds a card by the words of its name', () => {
    const cards = [{ label: 'Water Sabotaged' }, { label: 'Food Shortage' }, { label: 'Water Shortage' }] as ArtRef[];

    expect(search(cards, 'water').map((card) => card.label)).toEqual(['Water Sabotaged', 'Water Shortage']);
    expect(search(cards, 'shortage food').map((card) => card.label)).toEqual(['Food Shortage']);
    expect(search(cards, '  ')).toBe(cards);
  });

  it('lists every deck and narrows it to the search', () => {
    render(<ReferenceDrawer />);
    act(() => useReference.getState().show());

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
    expect(document.querySelector('.reference-grid li.focus')?.textContent).toBe('Laura Roslin');
  });
});

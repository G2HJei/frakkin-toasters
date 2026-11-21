import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { AnytimePanel } from './AnytimePanel';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

// Cavil holds two Motive Cards while William Adama is asked to choose.
const game = (viewer: unknown) =>
  ({
    viewer,
    players: [
      { number: 1, character: 'WILLIAM_ADAMA', motiveCards: null },
      { number: 2, character: 'CAVIL', motiveCards: { count: 2, revealed: [], cards: ['END_THE_CHASE', 'MAKE_AN_ALLY'] } },
    ],
    input: { kind: 'WaitingFor', player: 1 },
  }) as unknown as GameProjection;

function renderPanel(projection: GameProjection, onSubmit = vi.fn()) {
  return render(
    <Table.Provider value={tableContext(projection, projection.viewer?.kind === 'Sandbox', null)}>
      <AnytimePanel onSubmit={onSubmit} />
    </Table.Provider>,
  );
}

describe('acting at any time', () => {
  it('lets a Cylon Leader reveal one of his Motive Cards while another player is asked', () => {
    const onSubmit = vi.fn();
    const { container } = renderPanel(game({ kind: 'PlayerViewer', playerNumber: 2 }), onSubmit);

    // the panel is closed until opened, so the form is found by its field
    const select = container.querySelector('[data-field="card"] select') as HTMLSelectElement;
    // his two cards, and "none" for the nullable field
    expect(Array.from(select.options).map((option) => option.value)).toEqual(['', 'END_THE_CHASE', 'MAKE_AN_ALLY']);
    fireEvent.change(select, { target: { value: 'MAKE_AN_ALLY' } });
    fireEvent.click(screen.getByRole('button', { name: 'Reveal', hidden: true }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'RevealMotiveCardEvent', playerNumber: 2, card: 'MAKE_AN_ALLY' });
  });

  it('shows nothing to a player without Motive Cards', () => {
    renderPanel(game({ kind: 'PlayerViewer', playerNumber: 1 }));

    expect(screen.queryByRole('region', { name: 'at any time' })).toBeNull();
  });

  it('lets a sandbox reveal for any player who holds one', () => {
    renderPanel(game({ kind: 'Sandbox' }));

    expect(screen.getByText(/Cavil: reveal a Motive Card/)).toBeTruthy();
    expect(screen.queryByText(/William Adama: reveal/)).toBeNull();
  });
});

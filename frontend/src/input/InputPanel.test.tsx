import { fireEvent, render, screen, within } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { InputPanel } from './InputPanel';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

const section = (side: string) => ({
  kind: 'Single',
  event: { kind: 'ChooseCrisisSectionEvent', playerNumber: 1, card: 'RIOTS_NO_JUMP', section: side },
});

// The current player chooses a section of a Crisis Card.
const game = {
  viewer: { kind: 'PlayerViewer', playerNumber: 1 },
  players: [{ number: 1, character: 'WILLIAM_ADAMA' }],
  input: { kind: 'Choose', player: 1, options: [section('TOP'), section('BOTTOM')] },
} as unknown as GameProjection;

describe('the input', () => {
  it('shows the card a choice is about once, above its options', () => {
    const onMove = vi.fn();
    const { container } = render(
      <Table.Provider value={tableContext(game, false, 1)}>
        <InputPanel onChoose={vi.fn()} onSubmit={vi.fn()} onMove={onMove} />
      </Table.Provider>,
    );

    expect(container.querySelectorAll('[data-art="CrisisCard/RIOTS_NO_JUMP"]')).toHaveLength(1);
    expect(container.querySelector('.choice-subject [data-art="CrisisCard/RIOTS_NO_JUMP"]')).not.toBeNull();
    fireEvent.click(screen.getAllByRole('button', { name: /Bottom of Riots No Jump/ })[0]);
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ option: 1, submits: false }), section('BOTTOM').event);
  });

  it('keeps the plain options under Advanced', () => {
    const onChoose = vi.fn();
    const { container } = render(
      <Table.Provider value={tableContext(game, false, 1)}>
        <InputPanel onChoose={onChoose} onSubmit={vi.fn()} onMove={vi.fn()} />
      </Table.Provider>,
    );

    fireEvent.click(within(container.querySelector('details.advanced') as HTMLElement).getByRole('button', { name: /Bottom of Riots No Jump/ }));
    expect(onChoose).toHaveBeenCalledWith(1);
  });
});

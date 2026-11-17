import { fireEvent, render, screen } from '@testing-library/react';
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
    const onChoose = vi.fn();
    const { container } = render(
      <Table.Provider value={tableContext(game, false, 1)}>
        <InputPanel onChoose={onChoose} onSubmit={vi.fn()} />
      </Table.Provider>,
    );

    expect(container.querySelectorAll('[data-art="CrisisCard/RIOTS_NO_JUMP"]')).toHaveLength(1);
    expect(container.querySelector('.choice-subject [data-art="CrisisCard/RIOTS_NO_JUMP"]')).not.toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /Bottom of Riots No Jump/ }));
    expect(onChoose).toHaveBeenCalledWith(1);
  });
});

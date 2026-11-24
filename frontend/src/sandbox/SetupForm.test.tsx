import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { SetupForm } from './SetupForm';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

// A sandbox with two players and two Skill Cards in the Politics deck.
const game = {
  viewer: { kind: 'Sandbox' },
  players: [
    { number: 1, character: 'WILLIAM_ADAMA' },
    { number: 2, character: 'LAURA_ROSLIN' },
  ],
  decks: [{ deck: 'POLITICS', drawPile: [{ value: 1, type: 'CONSOLIDATE_POWER' }, { value: 2, type: 'CONSOLIDATE_POWER' }] }],
} as unknown as GameProjection;

function renderForm(kind: string) {
  const onSubmit = vi.fn();
  render(
    <Table.Provider value={tableContext(game, true, null)}>
      <SetupForm kind={kind} onSubmit={onSubmit} />
    </Table.Provider>,
  );
  return onSubmit;
}

describe("the sandbox's setup", () => {
  it('picks a player among those of the game and a Skill Card of the decks, by its position', () => {
    const onSubmit = renderForm('GiveSkillCardToPlayerEvent');

    // a player is his character
    expect(screen.getByRole('option', { name: 'Laura Roslin' })).toBeInTheDocument();
    fireEvent.change(screen.getByRole('combobox', { name: 'target player number' }), { target: { value: '2' } });
    fireEvent.change(screen.getByRole('combobox', { name: 'card' }), { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: 'Inject' }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'GiveSkillCardToPlayerEvent', targetPlayerNumber: 2, card: { value: 2, type: 'CONSOLIDATE_POWER' } });
  });

  it('builds a setup with a constant and a number as the codec writes it', () => {
    const onSubmit = renderForm('ChangeResourceEvent');

    fireEvent.change(screen.getByRole('combobox', { name: 'resource' }), { target: { value: 'FOOD' } });
    fireEvent.change(screen.getByRole('spinbutton', { name: 'amount' }), { target: { value: '-2' } });
    fireEvent.click(screen.getByRole('button', { name: 'Inject' }));

    expect(JSON.stringify(onSubmit.mock.calls[0][0])).toBe('{"kind":"ChangeResourceEvent","resource":"FOOD","amount":-2}');
  });
});

import { act, fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AnswerPanel } from './AnswerPanel';
import { useSelection } from './selection';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

const card = (type: string, value: number) => ({ type, value });
const players = [
  { number: 1, character: 'WILLIAM_ADAMA', location: 'COMMAND' },
  { number: 2, character: 'LAURA_ROSLIN', location: 'PRESIDENTS_OFFICE' },
];

function game(input: unknown): GameProjection {
  return { viewer: { kind: 'PlayerViewer', playerNumber: 1 }, players, input } as unknown as GameProjection;
}

function renderPanel(projection: GameProjection) {
  const onMove = vi.fn();
  const view = render(
    <Table.Provider value={tableContext(projection, false, 1)}>
      <AnswerPanel onMove={onMove} />
    </Table.Provider>,
  );
  return { onMove, ...view };
}

const decision = (action: string) => ({ kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action, eventConstraints: [] } });

beforeEach(() => useSelection.getState().clear());

describe('the answers', () => {
  it("unfold the Action step's action, and send an answer with its option", () => {
    const order = { kind: 'ExecutiveOrderEvent', playerNumber: 1, card: card('EXECUTIVE_ORDER', 2), targetPlayerNumber: 2 };
    const { onMove } = renderPanel(
      game({
        kind: 'Choose',
        player: 1,
        options: [decision('ActionEvent'), { kind: 'Single', event: { kind: 'NoOpEvent', playerNumber: 1 } }],
        answers: [{ option: 0, answers: { events: [{ kind: 'CommandEvent', playerNumber: 1, anywhere: false }, order], shapes: [] } }],
      }),
    );

    expect(screen.getByRole('button', { name: 'Take an action' })).toHaveAttribute('aria-expanded', 'true');
    fireEvent.click(screen.getByRole('button', { name: /Executive Order/ }));
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ option: 0, submits: true, event: order }), order);

    fireEvent.click(screen.getByRole('button', { name: /Pass/ }));
    expect(onMove).toHaveBeenLastCalledWith(expect.objectContaining({ option: 1, submits: false }), { kind: 'NoOpEvent', playerNumber: 1 });
  });

  it('count a draw per color, held to what an answer allows', () => {
    const draw = (selection: Record<string, number>) => ({ kind: 'DrawSkillCardsEvent', playerNumber: 1, selection, drawLimit: 'DRAW_EXACTLY_2' });
    const { onMove } = renderPanel(
      game({ kind: 'Decide', player: 1, action: 'DrawSkillCardsEvent', constraints: ['DRAW_EXACTLY_2'], answers: { events: [draw({ LEADERSHIP: 2 }), draw({ LEADERSHIP: 1, TACTICS: 1 })], shapes: [] } }),
    );
    const send = screen.getByRole('button', { name: 'Draw Skill Cards' });

    expect(send).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'more Tactics' }));
    // no answer has 2 Tactics
    expect(screen.getByRole('button', { name: 'more Tactics' })).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'more Leadership' }));
    expect(screen.getByRole('button', { name: 'more Leadership' })).toBeDisabled();
    expect(screen.getByText('2 of 2')).toBeInTheDocument();
    fireEvent.click(send);
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ event: draw({ LEADERSHIP: 1, TACTICS: 1 }) }), draw({ LEADERSHIP: 1, TACTICS: 1 }));
  });

  it("pick a shape's cards, sorted, and send them once there are enough", () => {
    const cards = [card('REPAIR', 1), card('CONSOLIDATE_POWER', 2), card('BAIT', 0)];
    const example = { kind: 'DiscardSkillCardsEvent', playerNumber: 1, cards: [cards[0], cards[1]], discardLimit: 'DISCARD_EXACTLY_2' };
    const { onMove, container } = renderPanel(
      game({
        kind: 'Decide',
        player: 1,
        action: 'DiscardSkillCardsEvent',
        constraints: ['DISCARD_EXACTLY_2'],
        answers: { events: [], shapes: [{ example, pick: { kind: 'SkillCardPick', component: 'cards', cards, min: 2, max: 2, ordered: false, minStrength: 0 } }] },
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: /Discard Skill Cards/ }));
    expect([...container.querySelectorAll('.pick-widget .card-pick')].map((tile) => tile.getAttribute('aria-label'))).toEqual(['Consolidate Power 2', 'Repair 1', 'Bait 0']);
    const send = screen.getByRole('button', { name: 'Send' });
    fireEvent.click(screen.getByRole('button', { name: 'Bait 0' }));
    expect(send).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Repair 1' }));
    // two is the most
    fireEvent.click(screen.getByRole('button', { name: 'Consolidate Power 2' }));
    expect(screen.getByRole('button', { name: 'Consolidate Power 2' })).toHaveAttribute('aria-pressed', 'false');
    fireEvent.click(send);
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ pick: expect.anything() }), { ...example, cards: [card('BAIT', 0), card('REPAIR', 1)] });
  });

  it('show only what the picks on the board left, with a way back', () => {
    const move = (destination: string) => ({ kind: 'MoveEvent', playerNumber: 1, destination, discardCard: null });
    renderPanel(game({ kind: 'Decide', player: 1, action: 'MoveEvent', constraints: [], answers: { events: [move('ARMORY'), move('ADMIRALS_QUARTERS')], shapes: [] } }));

    expect(screen.getAllByRole('button', { name: /Armory|Admiral/ })).toHaveLength(2);
    // the board picked a location
    act(() => useSelection.getState().pick({ type: 'location', location: 'ARMORY' }));
    expect(screen.getAllByRole('button', { name: /Armory|Admiral/ })).toHaveLength(1);
    expect(screen.getByText('1 answer left')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Back' }));
    expect(screen.getAllByRole('button', { name: /Armory|Admiral/ })).toHaveLength(2);
  });
});

import { act, fireEvent, render, screen, within } from '@testing-library/react';
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

  // every ordered pair of different cards, as the search lists the Engine Room's two discards
  const engineRoom = (hand: { type: string; value: number }[]) => ({
    kind: 'Decide',
    player: 1,
    action: 'ActionEvent',
    constraints: [],
    answers: {
      events: hand.flatMap((first, at) => hand.filter((_, other) => other !== at).map((second) => ({ kind: 'EngineRoomEvent', playerNumber: 1, discardCard1: first, discardCard2: second }))),
      shapes: [],
    },
  });

  it("pick the Engine Room's two cards from one pool, held to the answers", () => {
    const hand = [card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2), card('LAUNCH_SCOUT', 3)];
    const { onMove, container } = renderPanel(game(engineRoom(hand)));

    expect(container.querySelectorAll('button.chip')).toHaveLength(0);
    const pool = screen.getByRole('group', { name: 'Discard card' });
    expect([...pool.querySelectorAll('.part-value')].map((tile) => tile.getAttribute('aria-label'))).toEqual(['Executive Order 2', 'Launch Scout 3', 'Repair 1', 'Bait 0']);
    expect(screen.getByText('discard 2 cards')).toBeInTheDocument();
    const send = screen.getByRole('button', { name: 'Engine Room' });
    fireEvent.click(screen.getByRole('button', { name: 'Bait 0' }));
    expect(send).toBeDisabled();
    fireEvent.click(screen.getByRole('button', { name: 'Repair 1' }));
    // a pair: the others are closed
    expect(screen.getByRole('button', { name: 'Launch Scout 3' })).toBeDisabled();
    fireEvent.click(send);
    const listed = { kind: 'EngineRoomEvent', playerNumber: 1, discardCard1: hand[0], discardCard2: hand[1] };
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ event: listed }), listed);
  });

  it('leave three pairs of the Engine Room as chips, each pair once', () => {
    const { container } = renderPanel(game(engineRoom([card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2)])));

    expect(container.querySelector('.parts-widget')).toBeNull();
    expect(container.querySelectorAll('button.chip')).toHaveLength(3);
  });

  it("pick a choice's damage tokens, the returned ones implied, and choose that option", () => {
    const tokens = ['FUEL', 'FOOD', 'COMMAND', 'ARMORY'];
    const options = [];
    for (let first = 0; first < tokens.length; first++) {
      for (let second = first + 1; second < tokens.length; second++) {
        const event = { kind: 'ChooseGalacticaDamageTokensEvent', playerNumber: 1, resolved: [tokens[first], tokens[second]], returned: tokens.filter((_, at) => at !== first && at !== second) };
        options.push({ kind: 'Single', event });
      }
    }
    const { onMove, container } = renderPanel(game({ kind: 'Choose', player: 1, options, answers: [] }));

    expect(container.querySelectorAll('.parts-widget')).toHaveLength(1);
    const resolved = screen.getByRole('group', { name: 'Resolved' });
    const returned = screen.getByRole('group', { name: 'Returned' });
    fireEvent.click(within(resolved).getByRole('button', { name: 'Food' }));
    fireEvent.click(within(resolved).getByRole('button', { name: 'Armory' }));
    expect([...returned.querySelectorAll('.implied')].map((tile) => tile.getAttribute('aria-label'))).toEqual(['Command', 'Fuel']);
    fireEvent.click(container.querySelector('.parts-widget button.primary')!);
    // FOOD and ARMORY are the fifth pair
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ option: 4, submits: false }), options[4].event);
  });

  it('offer none beside the cards of a part, and settle what the others leave no choice about', () => {
    const ability = (name: string, used: unknown, result: number | null) => ({ kind: 'UseRollAbilityEvent', playerNumber: 1, ability: name, card: used, result });
    const events = [
      ability('ECO_OFFICER', null, null),
      ability('HOTSHOT', card('REPAIR', 1), null),
      ability('HOTSHOT', card('BAIT', 0), null),
      ...[1, 2, 3, 4, 5, 6, 7, 8].map((result) => ability('LONGSHOT', null, result)),
    ];
    const { onMove, container } = renderPanel(game({ kind: 'Decide', player: 1, action: 'UseRollAbilityEvent', constraints: [], answers: { events, shapes: [] } }));

    const cards = screen.getByRole('group', { name: 'Card' });
    expect([...cards.querySelectorAll('.part-value')].map((tile) => tile.getAttribute('aria-label'))).toEqual(['none', 'Repair 1', 'Bait 0']);
    fireEvent.click(within(screen.getByRole('group', { name: 'Ability' })).getByRole('button', { name: /Longshot/ }));
    expect(within(cards).getByRole('button', { name: 'none' })).toHaveClass('implied');
    fireEvent.click(within(screen.getByRole('group', { name: 'Result' })).getByRole('button', { name: '5' }));
    fireEvent.click(container.querySelector('.parts-widget button.primary')!);
    expect(onMove).toHaveBeenCalledWith(expect.objectContaining({ event: events[7] }), events[7]);
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

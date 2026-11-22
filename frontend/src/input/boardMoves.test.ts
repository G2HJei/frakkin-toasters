import { describe, expect, it } from 'vitest';
import { actLocation, actOption, moveDiscards, moveEvent, moveTargets } from './boardMoves';
import { emptyEvent, toWire } from './eventModel';
import { LAYOUT } from '../table/board/layout';
import { schema } from '../wire/schema';
import type { GameProjection, SkillCard } from '../wire/types';

const players = [{ number: 1, location: 'RESEARCH_LAB' }, { number: 2, location: 'PRESS_ROOM' }];
const card = { value: 2, type: 'EXECUTIVE_ORDER' } as SkillCard;

// A move on Galactica needs no discard, one to Colonial One picks a card of the hand.
const pendingMove = {
  players,
  input: {
    kind: 'Decide',
    player: 1,
    action: 'MoveEvent',
    constraints: [],
    answers: {
      events: [{ kind: 'MoveEvent', playerNumber: 1, destination: 'COMMAND', discardCard: null }],
      shapes: [
        {
          example: { kind: 'MoveEvent', playerNumber: 1, destination: 'PRESS_ROOM', discardCard: card },
          pick: { kind: 'SkillCardPick', component: 'discardCard', cards: [card], min: 1, max: 1, ordered: false, minStrength: 0 },
        },
      ],
    },
  },
} as unknown as GameProjection;

function actionStep(answers: unknown[]): GameProjection {
  const decision = { kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'ActionEvent', eventConstraints: [] } };
  return {
    players,
    input: {
      kind: 'Choose',
      player: 1,
      options: [decision, { kind: 'Single', event: { kind: 'NoOpEvent', playerNumber: 1 } }],
      answers: [{ option: 0, answers: { events: answers, shapes: [] } }],
    },
  } as unknown as GameProjection;
}

describe('the board', () => {
  it('places every location of the schema', () => {
    const locations = (schema.types.Location as { values: string[] }).values;

    expect(locations.filter((location) => !(location in LAYOUT))).toEqual([]);
    expect(Object.keys(LAYOUT).filter((location) => !locations.includes(location))).toEqual([]);
  });

  it('builds the same move as the builder', () => {
    expect(moveEvent(1, 'COMMAND', null)).toEqual(toWire({ ...emptyEvent('MoveEvent', 1), destination: 'COMMAND' }));
    expect(moveEvent(1, 'PRESS_ROOM', card)).toEqual(toWire({ ...emptyEvent('MoveEvent', 1), destination: 'PRESS_ROOM', discardCard: card }));
  });

  it('offers the destinations of the answers while a move is pending', () => {
    const targets = moveTargets(pendingMove);

    expect([...targets].sort()).toEqual(['COMMAND', 'PRESS_ROOM']);
    expect(moveTargets({ ...pendingMove, input: { kind: 'WaitingFor', player: 1 } } as unknown as GameProjection).size).toBe(0);
  });

  it('asks for a discard where the answers pick one', () => {
    expect(moveDiscards(pendingMove, 'PRESS_ROOM')).toEqual([card]);
    expect(moveDiscards(pendingMove, 'COMMAND')).toBeNull();
  });

  it('picks the option of a location action with the Act button', () => {
    const game = actionStep([{ kind: 'ResearchLabEvent', playerNumber: 1, skillCardColor: 'TACTICS' }]);

    expect(actOption(game)).toBe(0);
    expect(actLocation(game)).toBe('RESEARCH_LAB');
  });

  it('shows no Act button when the location has no action among the answers', () => {
    const game = actionStep([{ kind: 'ExecutiveOrderEvent', playerNumber: 1 }]);

    expect(actOption(game)).toBe(0);
    expect(actLocation(game)).toBeNull();
  });
});

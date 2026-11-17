import { describe, expect, it } from 'vitest';
import { actLocation, actOption, leavesArea, moveEvent, moveTargets } from './boardMoves';
import { emptyEvent, toWire } from './eventModel';
import { LAYOUT } from '../table/board/layout';
import { schema } from '../wire/schema';
import type { GameProjection, SkillCard } from '../wire/types';

const players = [{ number: 1, location: 'RESEARCH_LAB' }, { number: 2, location: 'PRESS_ROOM' }];

describe('the board', () => {
  it('places every location of the schema', () => {
    const locations = (schema.types.Location as { values: string[] }).values;

    expect(locations.filter((location) => !(location in LAYOUT))).toEqual([]);
    expect(Object.keys(LAYOUT).filter((location) => !locations.includes(location))).toEqual([]);
  });

  it('builds the same move as the builder', () => {
    const card = { value: 2, type: 'EXECUTIVE_ORDER' } as SkillCard;

    expect(moveEvent(1, 'COMMAND', null)).toEqual(toWire({ ...emptyEvent('MoveEvent', 1), destination: 'COMMAND' }));
    expect(moveEvent(1, 'PRESS_ROOM', card)).toEqual(toWire({ ...emptyEvent('MoveEvent', 1), destination: 'PRESS_ROOM', discardCard: card }));
  });

  it('offers the locations off space, other than the mover\'s own, while a move is pending', () => {
    const game = { players, input: { kind: 'Decide', player: 1, action: 'MoveEvent', constraints: [] } } as unknown as GameProjection;

    const targets = moveTargets(game);

    expect(targets.has('COMMAND')).toBe(true);
    expect(targets.has('RESEARCH_LAB')).toBe(false);
    expect(targets.has('GALACTICA_SPACE_2_OCLOCK')).toBe(false);
    expect(moveTargets({ ...game, input: { kind: 'WaitingFor', player: 1 } } as unknown as GameProjection).size).toBe(0);
  });

  it('asks for a discard when a move leaves its ship', () => {
    expect(leavesArea('RESEARCH_LAB', 'PRESS_ROOM')).toBe(true);
    expect(leavesArea('RESEARCH_LAB', 'COMMAND')).toBe(false);
    expect(leavesArea('GALACTICA_SPACE_2_OCLOCK', 'COMMAND')).toBe(false); // a pilot lands
  });

  it('picks the option of a location action with the Act button', () => {
    const decision = (action: string) => ({ kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action, eventConstraints: [] } });
    const game = {
      players,
      input: { kind: 'Choose', player: 1, options: [decision('ActionEvent'), { kind: 'Single', event: { kind: 'NoOpEvent', playerNumber: 1 } }] },
    } as unknown as GameProjection;

    expect(actOption(game)).toBe(0);
    expect(actLocation(game)).toBe('RESEARCH_LAB');
  });
});

import { describe, expect, it } from 'vitest';
import { canonical, covers, fillPick, groupMoves, intentDue, moves, narrow, plan, targetsOf, targetKey, varyingComponents, type Answer } from './answers';
import { toWire, type EventValue } from './eventModel';
import type { InputProjection, LegalAnswers } from '../wire/types';

const card = (type: string, value: number) => ({ type, value });
const decision = (action: string) => ({ kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 3, action, eventConstraints: [] } });
const noOp = { kind: 'Single', event: { kind: 'NoOpEvent', playerNumber: 3 } };

// The answers of a move, as the probe of a seeded sandbox found them: on Galactica no discard, to Colonial One a card.
const moveAnswers = {
  events: [
    { kind: 'MoveEvent', playerNumber: 3, destination: 'FTL_CONTROL', discardCard: null },
    { kind: 'MoveEvent', playerNumber: 3, destination: 'RESEARCH_LAB', discardCard: null },
  ],
  shapes: [
    {
      example: { kind: 'MoveEvent', playerNumber: 3, destination: 'PRESS_ROOM', discardCard: card('SCIENTIFIC_RESEARCH', 3) },
      pick: { kind: 'SkillCardPick', component: 'discardCard', cards: [card('SCIENTIFIC_RESEARCH', 3), card('EXECUTIVE_ORDER', 1)], min: 1, max: 1, ordered: false, minStrength: 0 },
    },
  ],
} as unknown as LegalAnswers;

const actionAnswers = {
  events: [
    { kind: 'PresidentsOfficeEvent', playerNumber: 3 },
    { kind: 'ExecutiveOrderEvent', playerNumber: 3, card: card('EXECUTIVE_ORDER', 1), targetPlayerNumber: 1 },
    { kind: 'ExecutiveOrderEvent', playerNumber: 3, card: card('EXECUTIVE_ORDER', 1), targetPlayerNumber: 2 },
    { kind: 'LaunchNukeEvent', playerNumber: 3, area: 'GALACTICA_SPACE_12_OCLOCK' },
  ],
  shapes: [],
} as unknown as LegalAnswers;

const actionStep = {
  kind: 'Choose',
  player: 3,
  options: [decision('ActionEvent'), noOp],
  answers: [{ option: 0, answers: actionAnswers }],
} as unknown as InputProjection;

const viperAnswers = {
  events: [1, 2].flatMap((fighter) => [
    { kind: 'ActivateViperEvent', playerNumber: 4, activation: { launchShipType: null, launchLocation: null, humanFighterId: fighter, moveLocation: 'GALACTICA_SPACE_2_OCLOCK', attackCylonShipId: null, escortCivilianShipId: null } },
    { kind: 'ActivateViperEvent', playerNumber: 4, activation: { launchShipType: null, launchLocation: null, humanFighterId: fighter, moveLocation: null, attackCylonShipId: 30, escortCivilianShipId: null } },
  ]),
  shapes: [],
} as unknown as LegalAnswers;

const draw = (selection: Record<string, number>) => ({ kind: 'DrawSkillCardsEvent', playerNumber: 4, selection, drawLimit: 'DRAW_EXACTLY_3' });

describe('the moves of an input', () => {
  it('are the concrete answers and the shapes of a decision', () => {
    const all = moves({ kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null });

    expect(all.map((move) => [move.option, move.event?.destination, move.submits, move.pick?.component ?? null])).toEqual([
      [null, 'FTL_CONTROL', true, null],
      [null, 'RESEARCH_LAB', true, null],
      [null, 'PRESS_ROOM', true, 'discardCard'],
    ]);
  });

  it('unfold an option that asks a decision into its answers, and keep the other options as they are', () => {
    const all = moves(actionStep);

    expect(all).toHaveLength(5);
    expect(all.slice(0, 4).every((move) => move.option === 0 && move.submits)).toBe(true);
    expect(all[4]).toMatchObject({ option: 1, submits: false, event: { kind: 'NoOpEvent' } });
  });

  it('pick an option as it is when its decision came without answers', () => {
    const all = moves({ kind: 'Choose', player: 3, options: [decision('MoveEvent'), noOp], answers: [] } as unknown as InputProjection);

    expect(all[0]).toMatchObject({ option: 0, event: null, submits: false });
  });

  it('build exactly the JSON the builder would for the same answer', () => {
    for (const move of [...moves(actionStep), ...moves({ kind: 'Decide', player: 4, action: 'ActivateViperEvent', constraints: [], answers: viperAnswers, topic: null })]) {
      const event = move.event as EventValue;

      expect(toWire(event)).toEqual(event);
    }
  });

  it('are none for a viewer who is not asked', () => {
    expect(moves({ kind: 'WaitingFor', player: 2 } as InputProjection)).toEqual([]);
  });
});

describe('what a move names', () => {
  it('is its kind, the location of a location action, its targets and its cards, not its player', () => {
    const [office, order] = moves(actionStep);

    expect(targetsOf(office).map(targetKey)).toEqual(['kind:PresidentsOfficeEvent', 'location:PRESIDENTS_OFFICE']);
    expect(targetsOf(order).map(targetKey)).toEqual(['kind:ExecutiveOrderEvent', 'card:SkillCard:EXECUTIVE_ORDER:1', 'player:1']);
  });

  it('reaches into a viper activation', () => {
    const [toArea, attack] = moves({ kind: 'Decide', player: 4, action: 'ActivateViperEvent', constraints: [], answers: viperAnswers, topic: null });

    expect(targetsOf(toArea).map(targetKey)).toEqual(['kind:ActivateViperEvent', 'ship:1', 'location:GALACTICA_SPACE_2_OCLOCK']);
    expect(targetsOf(attack).map(targetKey)).toEqual(['kind:ActivateViperEvent', 'ship:1', 'ship:30']);
  });

  it("leaves a shape's part to pick out", () => {
    const shape = moves({ kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null })[2];

    expect(targetsOf(shape).map(targetKey)).toEqual(['kind:MoveEvent', 'location:PRESS_ROOM']);
  });

  it('is the player a plain event targets', () => {
    const option = { kind: 'Single', event: { kind: 'SendToSickbayEvent', targetPlayerNumber: 2 } };
    const [sickbay] = moves({ kind: 'Choose', player: 1, options: [option], answers: [] } as unknown as InputProjection);

    expect(targetsOf(sickbay).map(targetKey)).toContain('player:2');
  });
});

describe('narrowing', () => {
  const vipers = moves({ kind: 'Decide', player: 4, action: 'ActivateViperEvent', constraints: [], answers: viperAnswers, topic: null });

  it('keeps the moves that name every pick', () => {
    expect(narrow(vipers, [{ type: 'ship', id: 2 }])).toHaveLength(2);
    expect(narrow(vipers, [{ type: 'ship', id: 2 }, { type: 'ship', id: 30 }])).toEqual([vipers[3]]);
    expect(narrow(vipers, [{ type: 'location', location: 'GALACTICA_SPACE_2_OCLOCK' }])).toEqual([vipers[0], vipers[2]]);
    expect(narrow(vipers, [])).toBe(vipers);
  });
});

describe('the groups of the panel', () => {
  it('are the moves by kind, with the components they differ in', () => {
    const groups = groupMoves(moves(actionStep));

    expect(groups.map((group) => [group.kind, group.moves.length])).toEqual([
      ['PresidentsOfficeEvent', 1],
      ['ExecutiveOrderEvent', 2],
      ['LaunchNukeEvent', 1],
      ['NoOpEvent', 1],
    ]);
    expect(varyingComponents(groups[1])).toEqual(['targetPlayerNumber']);
  });

  it("don't count a shape's example as a difference", () => {
    const group = groupMoves(moves({ kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null }))[0];

    expect(varyingComponents(group)).toEqual(['destination', 'discardCard']);
  });
});

describe('sending', () => {
  const [ftl, , press] = moves({ kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null });

  it('fills a single component with one element, a list with all of them', () => {
    expect(fillPick(press, [card('EXECUTIVE_ORDER', 1)])?.discardCard).toEqual(card('EXECUTIVE_ORDER', 1));
    const into = {
      option: null,
      followup: null,
      submits: true,
      event: { kind: 'PlayCardsIntoSkillCheckEvent', playerNumber: 4, skillCards: [card('ESTABLISH_NETWORK', 0)] } as Answer,
      pick: { kind: 'SkillCardPick' as const, component: 'skillCards', cards: [], min: 1, max: 3, ordered: false, minStrength: 0 },
    };

    expect(fillPick(into, [card('A', 1), card('B', 2)])?.skillCards).toEqual([card('A', 1), card('B', 2)]);
  });

  it('submits an answer, picks an option, or picks it and answers the decision it asks next', () => {
    const [, order, , , pass] = moves(actionStep);

    expect(plan(ftl)).toEqual({ choose: null, submit: ftl.event, intent: null });
    expect(plan(pass)).toEqual({ choose: 1, submit: null, intent: null });
    expect(plan(order)).toEqual({ choose: 0, submit: null, intent: order.event });
  });

  it('answers the intent only on a later decision of the same player that takes it', () => {
    const intent = { player: 3, event: ftl.event, afterVersion: 4 };
    const decide = { kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null } as InputProjection;

    expect(intentDue(intent, 5, decide)).toEqual(ftl.event);
    expect(intentDue(intent, 4, decide)).toBeNull();
    expect(intentDue(intent, 5, { ...decide, player: 2 } as InputProjection)).toBeNull();
    expect(intentDue({ ...intent, event: { ...ftl.event, destination: 'ARMORY' } }, 5, decide)).toBeNull();
    expect(intentDue(intent, 5, actionStep)).toBeNull();
  });
});

describe('covers', () => {
  it('takes a concrete answer in any key order, and a draw without its zero counts', () => {
    const answers = { events: [draw({ LEADERSHIP: 1, PILOTING: 2 })], shapes: [] } as unknown as LegalAnswers;

    expect(covers(answers, { playerNumber: 4, kind: 'DrawSkillCardsEvent', drawLimit: 'DRAW_EXACTLY_3', selection: { PILOTING: 2, LEADERSHIP: 1, TACTICS: 0 } })).toBe(true);
    expect(covers(answers, draw({ LEADERSHIP: 2, PILOTING: 1 }))).toBe(false);
  });

  it("takes a shape's part picked from its elements, within its bounds", () => {
    const shape = moves({ kind: 'Decide', player: 3, action: 'MoveEvent', constraints: [], answers: moveAnswers, topic: null })[2];

    expect(covers(moveAnswers, fillPick(shape, [card('EXECUTIVE_ORDER', 1)])!)).toBe(true);
    expect(covers(moveAnswers, fillPick(shape, [card('EXECUTIVE_ORDER', 2)])!)).toBe(false);
    expect(covers(moveAnswers, fillPick(shape, [])!)).toBe(false);
    expect(covers(moveAnswers, { ...fillPick(shape, [card('EXECUTIVE_ORDER', 1)])!, destination: 'ARMORY' })).toBe(false);
  });

  it('compares lists as multisets', () => {
    expect(canonical([card('A', 1), card('B', 2)])).toBe(canonical([card('B', 2), card('A', 1)]));
    expect(canonical([card('A', 1)])).not.toBe(canonical([card('A', 1), card('A', 1)]));
  });
});

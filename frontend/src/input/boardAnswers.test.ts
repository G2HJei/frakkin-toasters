import { describe, expect, it } from 'vitest';
import { afterPick, offerOf } from './boardAnswers';
import { moves } from './answers';
import { LAYOUT } from '../table/board/layout';
import { schema } from '../wire/schema';
import type { InputProjection, SkillCard } from '../wire/types';

const card = (type: string, value: number) => ({ type, value }) as SkillCard;
const decision = (action: string) => ({ kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action, eventConstraints: [] } });
const noOp = { kind: 'Single', event: { kind: 'NoOpEvent', playerNumber: 1 } };

// A move on Galactica needs no discard, one to Colonial One picks a card of the hand.
const pendingMove = {
  kind: 'Decide',
  player: 1,
  action: 'MoveEvent',
  constraints: [],
  answers: {
    events: [{ kind: 'MoveEvent', playerNumber: 1, destination: 'COMMAND', discardCard: null }],
    shapes: [
      {
        example: { kind: 'MoveEvent', playerNumber: 1, destination: 'PRESS_ROOM', discardCard: card('EXECUTIVE_ORDER', 2) },
        pick: { kind: 'SkillCardPick', component: 'discardCard', cards: [card('EXECUTIVE_ORDER', 2)], min: 1, max: 1, ordered: false, minStrength: 0 },
      },
    ],
  },
} as unknown as InputProjection;

function choice(options: unknown[], answers: unknown[]): InputProjection {
  return { kind: 'Choose', player: 1, options, answers } as unknown as InputProjection;
}

const order = (target: number) => ({ kind: 'ExecutiveOrderEvent', playerNumber: 1, card: card('EXECUTIVE_ORDER', 2), targetPlayerNumber: target });
const actionStep = choice(
  [decision('ActionEvent'), noOp],
  [{ option: 0, answers: { events: [{ kind: 'ResearchLabEvent', playerNumber: 1, skillCardColor: 'TACTICS' }, order(2), order(3), { kind: 'LaunchNukeEvent', playerNumber: 1, area: 'GALACTICA_SPACE_2_OCLOCK' }], shapes: [] } }],
);

const activation = (fighter: number, moveLocation: string | null, attack: number | null) => ({
  kind: 'ActivateViperEvent',
  playerNumber: 1,
  activation: { launchShipType: null, launchLocation: null, humanFighterId: fighter, moveLocation, attackCylonShipId: attack, escortCivilianShipId: null },
});
const viperDecision = {
  kind: 'Decide',
  player: 1,
  action: 'ActivateViperEvent',
  constraints: [],
  answers: { events: [activation(1, 'GALACTICA_SPACE_2_OCLOCK', null), activation(1, null, 30), activation(2, 'GALACTICA_SPACE_2_OCLOCK', null)], shapes: [] },
} as unknown as InputProjection;

const keys = (map: Map<string, unknown>) => [...map.keys()].sort();

describe('the board', () => {
  it('places every location of the schema', () => {
    const locations = (schema.types.Location as { values: string[] }).values;

    expect(locations.filter((location) => !(location in LAYOUT))).toEqual([]);
    expect(Object.keys(LAYOUT).filter((location) => !locations.includes(location))).toEqual([]);
  });

  it("lights a move's destinations, also while the Movement step offers it beside a card", () => {
    expect(keys(offerOf(moves(pendingMove), []).targets)).toEqual(['location:COMMAND', 'location:PRESS_ROOM']);
    const movement = choice(
      [decision('MoveEvent'), decision('MovementAbilityEvent'), noOp],
      [
        { option: 0, answers: (pendingMove as { answers: unknown }).answers },
        { option: 1, answers: { events: [{ kind: 'PreventivePolicyEvent', playerNumber: 1, card: card('PREVENTIVE_POLICY', 5), resource: 'FUEL' }], shapes: [] } },
      ],
    );

    expect(keys(offerOf(moves(movement), []).targets)).toEqual(['card:SkillCard:PREVENTIVE_POLICY:5', 'location:COMMAND', 'location:PRESS_ROOM']);
  });

  it('sends a move to a destination at once, and picks the discard of one to another ship', () => {
    const all = moves(pendingMove);

    expect(afterPick(all, [], { type: 'location', location: 'COMMAND' }).send?.event?.destination).toBe('COMMAND');
    expect(afterPick(all, [], { type: 'location', location: 'PRESS_ROOM' }).send?.pick?.component).toBe('discardCard');
  });

  it("lights only what starts a kind in the Action step: the location's Act chip and the cards", () => {
    const offer = offerOf(moves(actionStep), []);

    expect(keys(offer.targets)).toEqual(['card:SkillCard:EXECUTIVE_ORDER:2']);
    expect([...offer.acts.entries()].map(([location, act]) => [location, act.kind, act.moves.length])).toEqual([['RESEARCH_LAB', 'ResearchLabEvent', 1]]);
  });

  it("lights a card's targets once it is picked", () => {
    const offer = offerOf(moves(actionStep), [{ type: 'card', deck: 'SkillCard', card: card('EXECUTIVE_ORDER', 2) }]);

    expect(keys(offer.targets)).toEqual(['player:2', 'player:3']);
    expect(offer.picked.has('card:SkillCard:EXECUTIVE_ORDER:2')).toBe(true);
    expect(offer.acts.size).toBe(0);
  });

  it("lights a viper's activations: its fighters, where they go and what they attack", () => {
    const all = moves(viperDecision);

    expect(keys(offerOf(all, []).targets)).toEqual(['location:GALACTICA_SPACE_2_OCLOCK', 'ship:1', 'ship:2', 'ship:30']);
    // a fighter keeps its own activations, which a popover lists
    expect(afterPick(all, [], { type: 'ship', id: 1 }).left).toHaveLength(2);
    expect(keys(offerOf(all, [{ type: 'ship', id: 1 }]).targets)).toEqual(['location:GALACTICA_SPACE_2_OCLOCK', 'ship:30']);
    expect(afterPick(all, [{ type: 'ship', id: 1 }], { type: 'ship', id: 30 }).send).toEqual(all[1]);
  });

  it('lights the space areas a choice of the raider activation order names', () => {
    const area = (location: string) => ({ kind: 'Single', event: { kind: 'ActivateRaiderAreaEvent', area: location } });
    const order = choice([area('GALACTICA_SPACE_2_OCLOCK'), area('GALACTICA_SPACE_8_OCLOCK')], []);

    expect(keys(offerOf(moves(order), []).targets)).toEqual(['location:GALACTICA_SPACE_2_OCLOCK', 'location:GALACTICA_SPACE_8_OCLOCK']);
  });

  it('lights nothing for a viewer who is not asked', () => {
    const offer = offerOf(moves({ kind: 'WaitingFor', player: 2 } as InputProjection), []);

    expect(offer.targets.size + offer.acts.size).toBe(0);
  });
});

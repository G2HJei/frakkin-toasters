import { describe, expect, it } from 'vitest';
import { groupMoves, moves, type Move } from './answers';
import { allows, distinct, fitting, isProduct, optionGroups, partsOf, settled, shared, sizes } from './parts';
import type { InputProjection } from '../wire/types';

const card = (type: string, value: number) => ({ type, value });

function decide(events: unknown[], shapes: unknown[] = []): Move[] {
  return moves({ kind: 'Decide', player: 1, action: 'ActionEvent', constraints: [], answers: { events, shapes }, topic: null } as unknown as InputProjection);
}

function choose(options: unknown[]): Move[] {
  return moves({ kind: 'Choose', player: 1, options: options.map((event) => ({ kind: 'Single', event })), answers: [] } as unknown as InputProjection);
}

const group = (all: Move[]) => groupMoves(all)[0];

// every ordered pair of different cards, and a pair of the same card when the hand holds it twice
function engineRoom(hand: { type: string; value: number }[]): Move[] {
  const events = [];
  for (let first = 0; first < hand.length; first++) {
    for (let second = 0; second < hand.length; second++) {
      if (first !== second) {
        events.push({ kind: 'EngineRoomEvent', playerNumber: 1, discardCard1: hand[first], discardCard2: hand[second] });
      }
    }
  }
  // the search tries each card once per component, so a pair repeats when the hand holds a card twice
  const seen = new Set<string>();
  return decide(events.filter((event) => !seen.has(JSON.stringify(event)) && seen.add(JSON.stringify(event))));
}

describe('the parts of a group', () => {
  it('merge numbered siblings into one part of several cards, and count each pair once', () => {
    const hand = [card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2), card('LAUNCH_SCOUT', 3)];
    const all = group(engineRoom(hand)).moves;
    const parts = partsOf(group(all));

    expect(parts).toHaveLength(1);
    expect(parts[0]).toMatchObject({ key: 'discardCard', components: ['discardCard1', 'discardCard2'], many: true, values: hand });
    const pairs = distinct(all, parts);
    expect(pairs).toHaveLength(6);
    expect(isProduct(pairs, parts)).toBe(true);
    expect(sizes(pairs, parts[0])).toEqual({ min: 2, max: 2 });
  });

  it('settle on the pair picked, in the order the engine listed it', () => {
    const hand = [card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2), card('LAUNCH_SCOUT', 3)];
    const all = group(engineRoom(hand)).moves;
    const parts = partsOf(group(all));
    const pairs = distinct(all, parts);

    expect(settled(pairs, parts, { discardCard: [hand[1]] })).toEqual([]);
    const picked = settled(pairs, parts, { discardCard: [hand[1], hand[0]] });
    expect(picked.map((move) => [move.event?.discardCard1, move.event?.discardCard2])).toEqual([[hand[0], hand[1]]]);
  });

  it('offer a card twice only when an answer discards both copies', () => {
    const twice = [card('REPAIR', 1), card('REPAIR', 1), card('BAIT', 0)];
    const parts = partsOf(group(engineRoom(twice)));
    expect(parts[0].values).toEqual([card('REPAIR', 1), card('REPAIR', 1), card('BAIT', 0)]);
    expect(allows(engineRoom(twice), parts, { discardCard: [card('REPAIR', 1)] }, parts[0], card('REPAIR', 1))).toBe(true);

    const once = [card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2)];
    const onceParts = partsOf(group(engineRoom(once)));
    expect(onceParts[0].values).toEqual(once);
    expect(allows(engineRoom(once), onceParts, { discardCard: [card('REPAIR', 1)] }, onceParts[0], card('REPAIR', 1))).toBe(false);
  });

  it('leave a few combinations as chips: three cards are three pairs', () => {
    const all = engineRoom([card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2)]);
    const parts = partsOf(group(all));

    expect(distinct(all, parts)).toHaveLength(3);
    expect(isProduct(distinct(all, parts), parts)).toBe(false);
  });

  it('pick a target, a destination and a drawer, each held to the answers left', () => {
    const deal = (target: number, destination: string, drawer: number) => ({ kind: 'MakeADealEvent', playerNumber: 1, targetPlayerNumber: target, destination, drawerPlayerNumber: drawer });
    const events = [2, 3].flatMap((target) =>
      ['ARMORY', 'COMMAND', 'SICKBAY'].filter((destination) => !(target === 3 && destination === 'SICKBAY')).flatMap((destination) => [1, 2].map((drawer) => deal(target, destination, drawer))),
    );
    const all = decide(events);
    const parts = partsOf(group(all));

    expect(parts.map((part) => [part.key, part.many, part.values])).toEqual([
      ['targetPlayerNumber', false, [2, 3]],
      ['destination', false, ['ARMORY', 'COMMAND', 'SICKBAY']],
      ['drawerPlayerNumber', false, [1, 2]],
    ]);
    expect(isProduct(all, parts)).toBe(true);
    // a tuple keeps its places: target 2 and drawer 1 isn't target 1 and drawer 2
    expect(distinct(all, parts)).toHaveLength(10);
    const destination = parts[1];
    expect(allows(all, parts, { targetPlayerNumber: [2] }, destination, 'SICKBAY')).toBe(true);
    expect(allows(all, parts, { targetPlayerNumber: [3] }, destination, 'SICKBAY')).toBe(false);
    // a single part's value is replaced, so another target stays open
    expect(allows(all, parts, { targetPlayerNumber: [3] }, parts[0], 2)).toBe(true);
    expect(settled(all, parts, { targetPlayerNumber: [3], destination: ['COMMAND'] })).toEqual([]);
    expect(settled(all, parts, { targetPlayerNumber: [3], destination: ['COMMAND'], drawerPlayerNumber: [2] }).map((move) => move.event)).toEqual([deal(3, 'COMMAND', 2)]);
  });

  it("pick the damage tokens to resolve of a choice, the returned ones implied", () => {
    const tokens = ['FTL', 'ARMORY', 'COMMAND', 'FOOD'];
    const options = [];
    for (let first = 0; first < tokens.length; first++) {
      for (let second = first + 1; second < tokens.length; second++) {
        options.push({ kind: 'ChooseGalacticaDamageTokensEvent', playerNumber: 1, resolved: [tokens[first], tokens[second]], returned: tokens.filter((_, at) => at !== first && at !== second) });
      }
    }
    const all = choose(options);
    expect(groupMoves(all)).toHaveLength(6);
    const groups = optionGroups(all);
    expect(groups).toHaveLength(1);
    const parts = partsOf(groups[0]);

    expect(parts.map((part) => [part.key, part.many, part.values])).toEqual([
      ['resolved', true, ['FTL', 'ARMORY', 'COMMAND', 'FOOD']],
      ['returned', true, ['COMMAND', 'FOOD', 'ARMORY', 'FTL']],
    ]);
    expect(isProduct(all, parts)).toBe(true);
    const picks = { resolved: ['COMMAND', 'FTL'] };
    expect(shared(fitting(all, parts, picks), parts[1])).toEqual({ value: ['ARMORY', 'FOOD'] });
    expect(settled(all, parts, picks).map((move) => move.option)).toEqual([1]);
    // a pair: no third token
    expect(allows(all, parts, { resolved: ['FTL'] }, parts[0], 'COMMAND')).toBe(true);
    expect(allows(all, parts, picks, parts[0], 'FOOD')).toBe(false);
  });

  it('settle a part that every answer left shares, a card left out as none', () => {
    const ability = (name: string, used: unknown, result: number | null) => ({ kind: 'UseRollAbilityEvent', playerNumber: 1, ability: name, card: used, result });
    const all = decide([
      ability('ECO_OFFICER', null, null),
      ability('HOTSHOT', card('REPAIR', 1), null),
      ability('HOTSHOT', card('BAIT', 0), null),
      ...[1, 2, 3, 4, 5, 6, 7, 8].map((result) => ability('LONGSHOT', null, result)),
    ]);
    const parts = partsOf(group(all));

    expect(parts.map((part) => part.key)).toEqual(['ability', 'card', 'result']);
    expect(isProduct(all, parts)).toBe(true);
    expect(settled(all, parts, { ability: ['ECO_OFFICER'] }).map((move) => move.event)).toEqual([ability('ECO_OFFICER', null, null)]);
    expect(shared(fitting(all, parts, { ability: ['LONGSHOT'] }), parts[1])).toEqual({ value: null });
    expect(settled(all, parts, { ability: ['LONGSHOT'] })).toEqual([]);
    expect(settled(all, parts, { ability: ['LONGSHOT'], result: [5] }).map((move) => move.event)).toEqual([ability('LONGSHOT', null, 5)]);
  });

  it('keep a shape apart from a concrete answer of the same values', () => {
    const repair = (value: number) => ({ kind: 'RepairEvent', playerNumber: 1, card: card('REPAIR', value), viperIds: [] });
    const pick = { kind: 'ShipPick', component: 'viperIds', shipIds: [5, 6], min: 1, max: 2 };
    const all = decide([repair(1), repair(2)], [
      { example: { ...repair(1), viperIds: [5] }, pick },
      { example: { ...repair(2), viperIds: [5] }, pick },
    ]);
    const parts = partsOf(group(all));

    // the vipers are a shape's pick, no part
    expect(parts.map((part) => part.key)).toEqual(['card']);
    expect(distinct(all, parts)).toHaveLength(4);
    expect(isProduct(all, parts)).toBe(false);
    expect(settled(all, parts, { card: [card('REPAIR', 2)] }).map((move) => move.pick?.component ?? null)).toEqual([null, 'viperIds']);
  });
});

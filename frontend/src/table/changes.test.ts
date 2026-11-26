import { afterEach, describe, expect, it } from 'vitest';
import { changesBetween, NO_CHANGES } from './changes';
import { useGameStore } from '../live/gameStore';
import type { GameProjection, LogEntry, ServerMessage } from '../wire/types';

const card = (type: string, value: number) => ({ type, value });

function game(overrides: Record<string, unknown> = {}, players: unknown[] = []): GameProjection {
  return {
    turn: { number: 3, dieRoll: null },
    galactica: {
      fuel: 8,
      food: 8,
      morale: 10,
      population: 12,
      jumpPreparation: 'POSITION_1',
      locations: [{ location: 'ARMORY', damaged: false, characters: [] }],
    },
    pegasus: null,
    cylonFleet: { pursuit: 'START' },
    distanceTraveled: 0,
    nukes: 2,
    players,
    ...overrides,
  } as unknown as GameProjection;
}

afterEach(() => useGameStore.getState().reset());

describe('the changes of a game message', () => {
  it('are the resources, the tracks, the distance and the nukes that moved', () => {
    const next = game({
      galactica: { fuel: 7, food: 8, morale: 9, population: 13, jumpPreparation: 'START', locations: [{ location: 'ARMORY', damaged: true, characters: [] }] },
      cylonFleet: { pursuit: 'ONE_CIVILIAN_SHIP' },
      distanceTraveled: 2,
      nukes: 1,
    });

    const changes = changesBetween(game(), next);

    expect(changes.resources).toEqual([
      { resource: 'fuel', delta: -1 },
      { resource: 'morale', delta: -1 },
      { resource: 'population', delta: 1 },
    ]);
    // a reset after the jump moves the marker too
    expect(changes.tracks).toEqual([
      { track: 'JumpPreparation', from: 'POSITION_1' },
      { track: 'Pursuit', from: 'START' },
    ]);
    expect(changesBetween(next, next).tracks).toEqual([]);
    expect([changes.distance, changes.nukes]).toEqual([2, -1]);
    expect(changes.damaged).toEqual(['ARMORY']);
  });

  it('are the cards that came into a hand, face up or counted, and a new roll', () => {
    const before = game({}, [{ number: 1, skillCards: { count: 1, cards: [card('RED_TAPE', 1)] } }]);
    const after = game({ turn: { number: 3, dieRoll: { value: 6 } } }, [
      { number: 1, skillCards: { count: 3, cards: [card('RED_TAPE', 1), card('RED_TAPE', 1), card('LAUNCH_SCOUT', 1)] } },
    ]);

    const changes = changesBetween(before, after);

    expect(changes.gained.get(1)).toEqual([card('RED_TAPE', 1), card('LAUNCH_SCOUT', 1)]);
    expect(changes.gainedBacks.get(1)).toBe(2);
    expect(changes.rolled).toBe(true);
    expect(changesBetween(after, after).rolled).toBe(false);
  });

  it("are Galactica's damage turning critical: the message that brings it to 4 damaged locations, not the ones after", () => {
    const damaged = (count: number) =>
      game({
        galactica: {
          ...game().galactica,
          locations: ['ARMORY', 'BRIG', 'COMMAND', 'SICKBAY', 'HANGAR_DECK'].map((location, index) => ({ location, damaged: index < count, characters: [] })),
        },
      });

    expect(changesBetween(damaged(3), damaged(4)).critical).toBe(true);
    expect(changesBetween(damaged(2), damaged(5)).critical).toBe(true);
    expect(changesBetween(damaged(2), damaged(3)).critical).toBe(false);
    expect(changesBetween(damaged(4), damaged(5)).critical).toBe(false);
    expect(changesBetween(damaged(4), damaged(3)).critical).toBe(false);
  });

  it("are Preventive Policy's shields: up with the card, spent by the loss it reduced, down unspent with the turn", () => {
    const shielded = (lossReductions: Record<string, number>, number = 3) => game({ turn: { number, dieRoll: null, lossReductions } });
    const reduced = (resource: string) =>
      [{ kind: 'ScopeLog', scopeId: 9, parentId: 0, actor: 1, event: null, details: null, automatic: false, facts: [{ kind: 'Shown', fact: { kind: 'GameFact.LossReduced', resource } }] }] as unknown as LogEntry[];

    expect(changesBetween(shielded({}), shielded({ FOOD: 1 })).shieldsRaised).toEqual(['food']);
    expect(changesBetween(shielded({ FOOD: 1 }), shielded({ FOOD: 1, MORALE: 1 })).shieldsRaised).toEqual(['morale']);
    expect(changesBetween(shielded({ FOOD: 1 }), shielded({ FOOD: 1 }))).toMatchObject({ shieldsRaised: [], shieldsSpent: [] });
    expect(changesBetween(shielded({ FOOD: 1, MORALE: 1 }), shielded({ MORALE: 1 }), reduced('FOOD')).shieldsSpent).toEqual(['food']);
    // the loss and the end of the turn in one message
    expect(changesBetween(shielded({ FOOD: 1 }), shielded({}, 4), reduced('FOOD')).shieldsSpent).toEqual(['food']);
    expect(changesBetween(shielded({ FOOD: 1 }), shielded({}, 4)).shieldsSpent).toEqual([]);
    expect(changesBetween(shielded({ FOOD: 1 }), shielded({ FOOD: 1 }, 4)).shieldsRaised).toEqual(['food']);
  });

  it('are none without a previous game: a snapshot after a reconnect or an undo animates nothing', () => {
    const message = (fuel: number) => ({ type: 'game', token: 't', game: game({ galactica: { ...game().galactica, fuel } }) }) as unknown as ServerMessage;
    const store = useGameStore.getState();

    store.receive(message(8));
    store.receive(message(7));
    expect(useGameStore.getState().previous?.galactica?.fuel).toBe(8);

    useGameStore.getState().expectSnapshot();
    useGameStore.getState().receive(message(6));
    expect(useGameStore.getState().previous).toBeNull();
    expect(changesBetween(useGameStore.getState().previous, useGameStore.getState().game)).toBe(NO_CHANGES);

    useGameStore.getState().setConnection('open', 0);
    useGameStore.getState().receive(message(5));
    expect(useGameStore.getState().previous).toBeNull();
  });
});

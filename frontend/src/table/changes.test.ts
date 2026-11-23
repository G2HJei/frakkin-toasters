import { afterEach, describe, expect, it } from 'vitest';
import { changesBetween, NO_CHANGES } from './changes';
import { useGameStore } from '../live/gameStore';
import type { GameProjection, ServerMessage } from '../wire/types';

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
    distanceTraveled: 0,
    nukes: 2,
    players,
    ...overrides,
  } as unknown as GameProjection;
}

afterEach(() => useGameStore.getState().reset());

describe('the changes of a game message', () => {
  it('are the resources, the jump track, the distance and the nukes that moved', () => {
    const next = game({
      galactica: { fuel: 7, food: 8, morale: 9, population: 13, jumpPreparation: 'START', locations: [{ location: 'ARMORY', damaged: true, characters: [] }] },
      distanceTraveled: 2,
      nukes: 1,
    });

    const changes = changesBetween(game(), next);

    expect(changes.resources).toEqual([
      { resource: 'fuel', delta: -1 },
      { resource: 'morale', delta: -1 },
      { resource: 'population', delta: 1 },
    ]);
    expect(changes.jump).toEqual({ delta: -1, reset: true });
    expect([changes.distance, changes.nukes]).toEqual([2, -1]);
    expect(changes.damaged).toEqual(['ARMORY']);
  });

  it('are the cards that came into a hand, face up or by color, and a new roll', () => {
    const before = game({}, [{ number: 1, skillCards: { count: 1, colors: { POLITICS: 1 }, cards: [card('RED_TAPE', 1)] } }]);
    const after = game({ turn: { number: 3, dieRoll: { value: 6 } } }, [
      { number: 1, skillCards: { count: 3, colors: { POLITICS: 2, TACTICS: 1 }, cards: [card('RED_TAPE', 1), card('RED_TAPE', 1), card('LAUNCH_SCOUT', 1)] } },
    ]);

    const changes = changesBetween(before, after);

    expect(changes.gained.get(1)).toEqual([card('RED_TAPE', 1), card('LAUNCH_SCOUT', 1)]);
    expect(changes.gainedColors.get(1)).toEqual({ POLITICS: 1, TACTICS: 1 });
    expect(changes.rolled).toBe(true);
    expect(changesBetween(after, after).rolled).toBe(false);
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

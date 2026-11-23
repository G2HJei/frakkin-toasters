import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { LogPanel } from './LogPanel';
import { useFlash } from '../flash';
import { useGameStore } from '../../live/gameStore';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, LogEntry } from '../../wire/types';

const scope = (scopeId: number) => ({ kind: 'ScopeLog', scopeId, parentId: 0, actor: 1, event: 'NoOpEvent', details: null, facts: [], automatic: false }) as LogEntry;
const turnStarted = (scopeId: number, turn: number) =>
  ({ ...scope(scopeId), event: 'StartTurnEvent', facts: [{ kind: 'Shown', fact: { kind: 'GameFact.TurnStarted', turn } }] }) as LogEntry;

// jsdom lays nothing out: the log body is 100px high and its content as high as the test says, with a scroll position
// that clamps like a browser's
const layout = { scrollHeight: 500 };
const positions = new WeakMap<Element, number>();
const clientHeight = 100;
const patched = ['scrollHeight', 'clientHeight', 'scrollTop'] as const;
const originals = new Map<string, PropertyDescriptor | undefined>();

beforeEach(() => {
  layout.scrollHeight = 500;
  for (const name of patched) originals.set(name, Object.getOwnPropertyDescriptor(HTMLElement.prototype, name));
  Object.defineProperty(HTMLElement.prototype, 'scrollHeight', { configurable: true, get: () => layout.scrollHeight });
  Object.defineProperty(HTMLElement.prototype, 'clientHeight', { configurable: true, get: () => clientHeight });
  Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
    configurable: true,
    get(this: Element) {
      return positions.get(this) ?? 0;
    },
    set(this: Element, value: number) {
      positions.set(this, Math.max(0, Math.min(value, layout.scrollHeight - clientHeight)));
    },
  });
});

afterEach(() => {
  for (const name of patched) {
    const original = originals.get(name);
    if (original) Object.defineProperty(HTMLElement.prototype, name, original);
    else delete (HTMLElement.prototype as unknown as Record<string, unknown>)[name];
  }
  useGameStore.getState().reset();
});

function renderLog(entries: LogEntry[], players = [{ number: 1, character: 'WILLIAM_ADAMA' }]) {
  useGameStore.setState({ log: entries });
  const game = { viewer: { kind: 'PlayerViewer', playerNumber: 1 }, players } as unknown as GameProjection;
  const container = render(
    <Table.Provider value={tableContext(game, false, 1)}>
      <LogPanel />
    </Table.Provider>,
  ).container;
  return container.querySelector('.log-body') as HTMLElement;
}

// the lines' words
function lines(body: HTMLElement): string[] {
  return [...body.querySelectorAll('.lines > li .event')].map((line) => line.textContent ?? '');
}

// each run's player, or null, then its lines' words
function runs(body: HTMLElement): (string | null)[][] {
  return [...body.querySelectorAll('.entries > .run')].map((run) => [
    run.querySelector('.run-head .actor')?.textContent ?? null,
    ...[...run.querySelectorAll('.lines > li .event')].map((line) => line.textContent ?? ''),
  ]);
}

function addEntry(entry: LogEntry, scrollHeight: number) {
  layout.scrollHeight = scrollHeight;
  act(() => useGameStore.setState((state) => ({ log: [...state.log, entry] })));
}

function scrollTo(body: HTMLElement, top: number) {
  body.scrollTop = top;
  fireEvent.scroll(body);
}

describe('the log', () => {
  it('has no title and keeps every turn open', () => {
    const body = renderLog([scope(1), turnStarted(2, 1), scope(3), turnStarted(4, 2), scope(5)]);
    const turns = [...body.querySelectorAll<HTMLDetailsElement>('details.turn')];

    expect(document.querySelector('.log .tab')).toBeNull();
    expect(turns.map((turn) => turn.querySelector('summary')?.textContent)).toEqual(['Setup', 'Turn 1 · William Adama', 'Turn 2 · William Adama']);
    expect(turns.every((turn) => turn.open)).toBe(true);
  });

  it('starts at the newest entry and follows the new ones', () => {
    const body = renderLog([scope(1), scope(2)]);

    expect(body.scrollTop).toBe(400);

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(500);
    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();
  });

  it('stays where the user scrolled up to, and goes to the bottom on the button', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 120);

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(120);

    fireEvent.click(screen.getByRole('button', { name: 'Go to bottom' }));

    expect(body.scrollTop).toBe(500);
    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(4), 700);

    expect(body.scrollTop).toBe(600);
  });

  it('keeps following when the end moves before the event of its own scroll arrives', () => {
    const body = renderLog([scope(1), scope(2)]);
    addEntry(scope(3), 600);

    // an image of the new entry loads before the browser reports the scroll to the end
    layout.scrollHeight = 650;
    fireEvent.scroll(body);

    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(4), 700);

    expect(body.scrollTop).toBe(600);
  });

  it('lights the arrow once new entries come below, until the user is at the end again', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 120);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).not.toHaveClass('unseen');

    addEntry(scope(3), 600);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).toHaveClass('unseen');

    fireEvent.click(screen.getByRole('button', { name: 'Go to bottom' }));
    scrollTo(body, 120);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).not.toHaveClass('unseen');
  });

  it('follows again once the user scrolls back down to the end', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 0);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).toBeInTheDocument();

    scrollTo(body, 396);

    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(500);
  });

  it('groups a turn by its steps, the facts under their line, what changes the table first', () => {
    const step = (scopeId: number, name: string) => ({ ...scope(scopeId), event: 'MovementStepEvent', facts: [{ kind: 'Shown', fact: { kind: 'GameFact.StepChanged', step: name } }] }) as LogEntry;
    const move = {
      ...scope(7),
      event: 'MoveEvent',
      details: { kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null },
      facts: [
        { kind: 'Shown', fact: { kind: 'GameFact.TurnLimitUsed', playerNumber: 1, limit: 'CAG_PILOT_ACTION' } },
        { kind: 'Shown', fact: { kind: 'PlayerFact.CardsSeen', playerNumber: 1, deck: 'CRISIS', cards: ['RIOTS_NO_JUMP'] } },
        { kind: 'Shown', fact: { kind: 'PlayerFact.CharacterMoved', character: 'WILLIAM_ADAMA', to: 'ARMORY' } },
        { kind: 'Shown', fact: { kind: 'BoardFact.ResourceDecreased', resource: 'FOOD', amount: 1 } },
      ],
    } as LogEntry;
    const entries = [turnStarted(1, 1), step(2, 'MOVEMENT'), move, step(8, 'ACTION')];

    const body = renderLog(entries);
    // a step without a line has no header
    expect([...body.querySelectorAll('.step-name')].map((name) => name.textContent)).toEqual(['Movement']);
    expect(lines(body)).toEqual(['moves to the Armory']);
    // the move says what its line says, the turn limit keeps the books
    expect([...body.querySelectorAll('.facts li')].map((fact) => fact.textContent)).toEqual(['The fleet loses 1 food', 'looks at Riots No Jump']);
    // the destination's card shows on hover
    expect(body.querySelector('.event .named')?.textContent).toBe('Armory');
  });

  it("names a player once above his lines in a row, and nobody above the game's", () => {
    const draw = (scopeId: number, player: number) =>
      ({ ...scope(scopeId), actor: player, event: 'DrawMutinyCardEvent', details: { kind: 'DrawMutinyCardEvent', playerNumber: player } }) as LogEntry;
    const loss = { ...scope(4), event: 'LoseResourceEvent', facts: [{ kind: 'Shown', fact: { kind: 'BoardFact.ResourceDecreased', resource: 'MORALE', amount: 1 } }] } as LogEntry;
    const body = renderLog([draw(1, 1), draw(2, 1), draw(3, 2), loss, draw(5, 2)], [
      { number: 1, character: 'WILLIAM_ADAMA' },
      { number: 2, character: 'KARL_HELO_AGATHON_ALT' },
    ]);

    expect(runs(body)).toEqual([
      ['William Adama', 'draws a Mutiny Card', 'draws a Mutiny Card'],
      ['Karl "Helo" Agathon', 'draws a Mutiny Card'],
      [null, 'The fleet loses 1 morale'],
      ['Karl "Helo" Agathon', 'draws a Mutiny Card'],
    ]);
    expect(body.querySelector('.run-head img')?.getAttribute('data-art')).toBe('Character/player-token/WILLIAM_ADAMA');
  });

  it('tells a choice by the event it picked, under the player who picked it', () => {
    const area = { kind: 'ActivateRaiderAreaEvent', area: 'GALACTICA_SPACE_8_OCLOCK', raiderShipIds: [] };
    const choice = { kind: 'ChoiceLog', scopeId: 3, chooser: 1, option: { kind: 'Single', event: area } } as LogEntry;
    const picked = { ...scope(4), parentId: 3, event: 'ActivateRaiderAreaEvent', details: area } as LogEntry;
    const body = renderLog([choice, picked]);

    expect(runs(body)).toEqual([['William Adama', "activates the raiders at Space 8 o'clock"]]);
  });

  it('leaves out a pass on a card every player is asked about, and says what other passes passed on', () => {
    const total = { ...scope(10), event: 'TotalSkillCheckStrengthEvent' } as LogEntry;
    const movement = { ...scope(20), event: 'MovementStepEvent' } as LogEntry;
    const pass = (scopeId: number, parentId: number) => ({ ...scope(scopeId), parentId, details: { kind: 'NoOpEvent', playerNumber: 1 } }) as LogEntry;
    const body = renderLog([total, pass(11, 10), movement, pass(21, 20)]);

    expect(runs(body)).toEqual([['William Adama', "doesn't move"]]);
  });

  it('tells an event about the game by its facts, and joins ships that moved alike', () => {
    const raider = (scopeId: number, shipId: number) =>
      ({
        ...scope(scopeId),
        event: 'ActivateRaiderEvent',
        details: { kind: 'ActivateRaiderEvent', raiderShipId: shipId },
        facts: [{ kind: 'Shown', fact: { kind: 'ShipFact.ShipMoved', shipId, from: 'GALACTICA_SPACE', to: 'GALACTICA_SPACE', location: 'GALACTICA_SPACE_6_OCLOCK' } }],
      }) as LogEntry;
    const body = renderLog([raider(1, 31), raider(2, 32)]);

    expect(runs(body)).toEqual([[null, "ship #31 and ship #32 move to Space 6 o'clock"]]);
  });

  it('shows every line, and lights what a line touched on a click', () => {
    const other = { ...scope(2), actor: 2 } as LogEntry;
    const body = renderLog([scope(1), other]);

    expect(body.querySelectorAll('.lines > li')).toHaveLength(2);

    fireEvent.click(body.querySelector('.lines > li')!);
    expect([...useFlash.getState().keys]).toEqual([]);
  });

  it('marks an answer the server took because it was the only one', () => {
    const taken = { ...scope(2), automatic: true } as LogEntry;
    const body = renderLog([scope(1), taken]);

    const icons = body.querySelectorAll('.auto-icon');
    expect(icons).toHaveLength(1);
    // in front of the line it marks
    expect(icons[0]).toBe(body.querySelectorAll('.lines > li')[1].firstElementChild);
    expect(icons[0].getAttribute('aria-label')).toBe('automatic');
    expect(icons[0].getAttribute('title')).toBe('The only answer, taken automatically');
  });

  it('words what happened to a ship', () => {
    const destroyed = {
      ...scope(3),
      event: 'DestroyRaidersEvent',
      facts: [{ kind: 'Shown', fact: { kind: 'ShipFact.ShipMoved', shipId: 34, from: 'GALACTICA_SPACE', to: 'CYLON_SUPPLY', location: null } }],
    } as LogEntry;
    const body = renderLog([destroyed]);

    expect(lines(body)).toEqual(['ship #34 is destroyed']);
  });
});

import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { groupBySteps, LogPanel, splitFacts } from './LogPanel';
import { useFlash } from '../flash';
import { useGameStore } from '../../live/gameStore';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, LogEntry } from '../../wire/types';

const scope = (scopeId: number) => ({ kind: 'ScopeLog', scopeId, parentId: 0, actor: 1, event: 'NoOpEvent', details: null, facts: [] }) as LogEntry;
const turnStarted = (scopeId: number, turn: number) =>
  ({ ...scope(scopeId), facts: [{ kind: 'Shown', fact: { kind: 'GameFact.TurnStarted', turn } }] }) as LogEntry;

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

function renderLog(entries: LogEntry[]) {
  useGameStore.setState({ log: entries });
  const game = { viewer: { kind: 'PlayerViewer', playerNumber: 1 }, players: [{ number: 1, character: 'WILLIAM_ADAMA' }] } as unknown as GameProjection;
  const container = render(
    <Table.Provider value={tableContext(game, false, 1)}>
      <LogPanel />
    </Table.Provider>,
  ).container;
  return container.querySelector('.log-body') as HTMLElement;
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
    expect(turns.map((turn) => turn.querySelector('summary')?.textContent)).toEqual(['Setup', 'Turn 1', 'Turn 2']);
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

  it('follows again once the user scrolls back down to the end', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 0);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).toBeInTheDocument();

    scrollTo(body, 396);

    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(500);
  });

  it('groups a turn by its steps, and lists every fact, the story first', () => {
    const step = (scopeId: number, name: string) => ({ ...scope(scopeId), facts: [{ kind: 'Shown', fact: { kind: 'GameFact.StepChanged', step: name } }] }) as LogEntry;
    const move = {
      ...scope(7),
      event: 'MoveEvent',
      details: { kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null },
      facts: [
        { kind: 'Shown', fact: { kind: 'PlayerFact.CharacterMoved', character: 'WILLIAM_ADAMA', to: 'ARMORY' } },
        { kind: 'Shown', fact: { kind: 'GameFact.TurnLimitUsed', playerNumber: 1, limit: 'CAG_PILOT_ACTION' } },
      ],
    } as LogEntry;
    const entries = [turnStarted(1, 1), step(2, 'MOVEMENT'), move, step(8, 'ACTION')];

    expect(groupBySteps(entries).map((group) => [group.step, group.entries.length])).toEqual([[null, 1], ['MOVEMENT', 2], ['ACTION', 1]]);
    const { story, rest } = splitFacts(move.kind === 'ScopeLog' ? (move.facts ?? []) : []);
    expect([story.length, rest.length]).toEqual([1, 1]);

    const body = renderLog(entries);
    expect([...body.querySelectorAll('.step-name')].map((name) => name.textContent)).toEqual(['Movement', 'Action']);
    // the turn's and the steps' own scopes say nothing their headers don't
    expect(body.querySelectorAll('.entries > li')).toHaveLength(1);
    // the move's own fact says what its line says, the turn limit is listed unfolded
    expect([...body.querySelectorAll('.facts li')]).toHaveLength(1);
    expect(screen.queryByRole('toolbar')).toBeNull();
    // the destination's card shows on hover
    expect(body.querySelector('.event .named')?.textContent).toBe('Armory');
  });

  it('shows every entry, and lights what an entry touched on a click', () => {
    const other = { ...scope(2), actor: 2 } as LogEntry;
    const body = renderLog([scope(1), other]);

    expect(body.querySelectorAll('.entries > li')).toHaveLength(2);

    fireEvent.click(body.querySelector('.entries > li')!);
    expect([...useFlash.getState().keys]).toEqual([]);
  });

  it('words what happened to a ship', () => {
    const destroyed = { ...scope(3), facts: [{ kind: 'Shown', fact: { kind: 'ShipFact.ShipMoved', shipId: 34, from: 'GALACTICA_SPACE', to: 'CYLON_SUPPLY', location: null } }] } as LogEntry;
    const body = renderLog([destroyed]);

    expect(body.querySelector('.facts li')?.textContent).toBe('ship #34 is destroyed');
  });
});

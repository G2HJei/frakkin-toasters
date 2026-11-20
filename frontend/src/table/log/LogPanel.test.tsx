import { act, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { LogPanel } from './LogPanel';
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
  const game = { players: [{ number: 1, character: 'WILLIAM_ADAMA' }] } as unknown as GameProjection;
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
    const turns = [...body.querySelectorAll('details')];

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
});

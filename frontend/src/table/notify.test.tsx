import { renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useYourMoveNotice } from './notify';

const shown: { title: string; body?: string }[] = [];

class FakeNotification {
  static permission = 'granted';
  onclick: (() => void) | null = null;
  constructor(title: string, options?: NotificationOptions) {
    shown.push({ title, body: options?.body });
  }
  close() {}
}

let visibility: DocumentVisibilityState = 'hidden';

beforeEach(() => {
  shown.length = 0;
  visibility = 'hidden';
  vi.stubGlobal('Notification', FakeNotification);
  Object.defineProperty(document, 'visibilityState', { configurable: true, get: () => visibility });
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('useYourMoveNotice', () => {
  it('notifies once when the move comes while the tab is hidden, and flashes the title', () => {
    const { rerender } = renderHook(({ move }) => useYourMoveNotice(move, 'Galactica', 'Draw Skill Cards', 'g1', false), { initialProps: { move: false } });

    rerender({ move: true });
    rerender({ move: true });

    expect(shown).toEqual([{ title: 'Your move · Galactica', body: 'Draw Skill Cards' }]);
    vi.advanceTimersByTime(1000);
    expect(document.title).toBe('▶ Your move');
    vi.advanceTimersByTime(1000);
    expect(document.title).toBe('● Galactica');
  });

  it('stays quiet while the tab is in front, and in a sandbox', () => {
    visibility = 'visible';
    const { rerender } = renderHook(({ move }) => useYourMoveNotice(move, 'Galactica', null, 'g1', false), { initialProps: { move: false } });
    rerender({ move: true });
    visibility = 'hidden';
    const sandbox = renderHook(({ move }) => useYourMoveNotice(move, 'Sandbox', null, 'g2', true), { initialProps: { move: false } });
    sandbox.rerender({ move: true });

    expect(shown).toEqual([]);
  });
});

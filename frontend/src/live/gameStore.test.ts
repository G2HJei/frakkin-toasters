import { beforeEach, describe, expect, it } from 'vitest';
import { GameSocket, type SocketLike } from './GameSocket';
import { chooseMessage, useGameStore } from './gameStore';
import type { GameProjection, LogEntry, ServerMessage } from '../wire/types';

class FakeSocket implements SocketLike {
  readyState = 0;
  sent: string[] = [];
  onopen: ((event: Event) => void) | null = null;
  onclose: ((event: CloseEvent) => void) | null = null;
  onmessage: ((event: MessageEvent) => void) | null = null;

  constructor(readonly url: string) {}

  send(data: string) {
    this.sent.push(data);
  }

  close() {
    this.readyState = 3;
  }

  open() {
    this.readyState = 1;
    this.onopen?.(new Event('open'));
  }

  receive(message: ServerMessage) {
    this.onmessage?.(new MessageEvent('message', { data: JSON.stringify(message) }));
  }

  drop(code?: number) {
    this.readyState = 3;
    this.onclose?.(new CloseEvent('close', { code }));
  }
}

const game = (step: string) => ({ turn: { step } }) as unknown as GameProjection;
const scope = (scopeId: number) => ({ kind: 'ScopeLog', scopeId, parentId: 0, actor: 1, event: 'NoOpEvent', details: null, facts: [], automatic: false }) as LogEntry;

describe('the game store, fed by a socket', () => {
  let sockets: FakeSocket[];
  // the pending timers in the order they were set; a cleared one is gone
  let timers: (() => void)[];
  let clock: number;
  let socket: GameSocket;

  beforeEach(() => {
    useGameStore.getState().reset();
    sockets = [];
    timers = [];
    clock = 0;
    socket = new GameSocket(
      'g1',
      (message) => useGameStore.getState().receive(message),
      (state, attempt) => useGameStore.getState().setConnection(state, attempt),
      {
        createSocket: (url) => {
          const fake = new FakeSocket(url);
          sockets.push(fake);
          return fake;
        },
        setTimer: (callback) => {
          timers.push(callback);
          return callback;
        },
        clearTimer: (timer) => {
          timers = timers.filter((pending) => pending !== timer);
        },
        pingEvery: 20,
        deadAfter: 50,
        now: () => clock,
      },
    );
    socket.connect();
    sockets[0].open();
  });

  it('takes the game, then the log, then the updates in the order they come', () => {
    sockets[0].receive({ type: 'game', token: 't1', game: game('SETUP') });
    sockets[0].receive({ type: 'log', log: { entries: [scope(1), scope(2)] } });
    sockets[0].receive({ type: 'update', update: { entries: [scope(3)] } });
    sockets[0].receive({ type: 'game', token: 't2', game: game('MOVEMENT') });

    const state = useGameStore.getState();
    expect(state.token).toBe('t2');
    expect(state.game?.turn?.step).toBe('MOVEMENT');
    expect(state.log.map((entry) => (entry as { scopeId: number }).scopeId)).toEqual([1, 2, 3]);
    expect(state.connection).toBe('open');
  });

  it('hands the table the log entries that came with each game, whose update comes before it', () => {
    const fresh = () => useGameStore.getState().fresh.map((entry) => (entry as { scopeId: number }).scopeId);
    sockets[0].receive({ type: 'game', token: 't1', game: game('SETUP') });
    sockets[0].receive({ type: 'update', update: { entries: [scope(3)] } });
    sockets[0].receive({ type: 'update', update: { entries: [scope(4)] } });
    expect(fresh()).toEqual([]);

    sockets[0].receive({ type: 'game', token: 't2', game: game('MOVEMENT') });
    expect(fresh()).toEqual([3, 4]);

    sockets[0].receive({ type: 'game', token: 't3', game: game('ACTION') });
    expect(fresh()).toEqual([]);
  });

  it('replaces the log when a whole log comes, e.g. after an undo', () => {
    sockets[0].receive({ type: 'log', log: { entries: [scope(1), scope(2), scope(3)] } });
    sockets[0].receive({ type: 'log', log: { entries: [scope(1)] } });

    expect(useGameStore.getState().log).toHaveLength(1);
  });

  it('keeps a rejection until the next game, which a stale command gets right after it', () => {
    sockets[0].receive({ type: 'game', token: 't1', game: game('SETUP') });
    sockets[0].receive({ type: 'rejected', reason: 'INVALID', message: 'Invalid action!' });
    expect(useGameStore.getState().rejected?.message).toBe('Invalid action!');

    sockets[0].receive({ type: 'rejected', reason: 'STALE', message: 'The game has moved on' });
    sockets[0].receive({ type: 'game', token: 't9', game: game('ACTION') });
    expect(useGameStore.getState().rejected).toBeNull();
    expect(useGameStore.getState().token).toBe('t9');
  });

  it('sends commands with the token of the latest game', () => {
    sockets[0].receive({ type: 'game', token: 't7', game: game('ACTION') });

    socket.send(chooseMessage(useGameStore.getState().token, 2));

    expect(JSON.parse(sockets[0].sent[0])).toEqual({ type: 'choose', token: 't7', option: 2 });
  });

  it('reconnects with backoff, and the new connection gets the game and the log again', () => {
    sockets[0].drop();
    expect(useGameStore.getState().connection).toBe('reconnecting');
    expect(socket.send(chooseMessage('t', 0))).toBe(false); // nothing goes out while it is down

    timers.shift()?.();
    expect(sockets).toHaveLength(2);
    expect(sockets[1].url).toContain('/ws/games/g1');
    sockets[1].open();
    sockets[1].receive({ type: 'game', token: 't3', game: game('CRISIS') });
    sockets[1].receive({ type: 'log', log: { entries: [scope(1)] } });

    expect(useGameStore.getState().connection).toBe('open');
    expect(useGameStore.getState().token).toBe('t3');
    expect(useGameStore.getState().log).toHaveLength(1);
  });

  it('keeps the reason and stops when the server refuses the game, e.g. one saved by another version', () => {
    sockets[0].receive({ type: 'rejected', reason: 'UNAVAILABLE', message: 'The game was saved by another version (format 2)' });
    sockets[0].drop(1008);

    expect(timers).toHaveLength(0); // no reconnect, and no heartbeat
    expect(useGameStore.getState().connection).toBe('closed');
    expect(useGameStore.getState().rejected?.message).toContain('format 2');
  });

  it("drops an intent once the server took its player's only answer itself", () => {
    const automatic = (actor: number) => ({ ...scope(4), actor, automatic: true }) as LogEntry;
    useGameStore.getState().setIntent({ player: 1, event: { kind: 'NoOpEvent', playerNumber: 1 }, afterVersion: 1 });

    sockets[0].receive({ type: 'update', update: { entries: [scope(3), automatic(2)] } });
    expect(useGameStore.getState().intent).not.toBeNull();

    sockets[0].receive({ type: 'update', update: { entries: [automatic(1)] } });
    expect(useGameStore.getState().intent).toBeNull();
  });

  it('stops reconnecting once closed', () => {
    socket.close();
    sockets[0].drop();

    expect(timers).toHaveLength(0);
    expect(useGameStore.getState().connection).toBe('closed');
  });

  it('pings while open, and the pong stays with the socket', () => {
    clock = 20;
    timers.shift()?.(); // the heartbeat

    expect(JSON.parse(sockets[0].sent.at(-1) ?? '')).toEqual({ type: 'ping' });
    sockets[0].receive({ type: 'pong' });
    expect(useGameStore.getState().version).toBe(0);
    expect(timers).toHaveLength(1); // the next beat
  });

  it('drops a connection that heard nothing for too long and opens a new one at once', () => {
    clock = 20;
    timers.shift()?.();
    clock = 60;
    timers.shift()?.();

    expect(sockets[0].readyState).toBe(3);
    expect(useGameStore.getState().connection).toBe('reconnecting');
    timers.shift()?.();
    expect(sockets).toHaveLength(2);
  });

  it('reconnects at once when the seat changed', () => {
    sockets[0].drop(4001);
    expect(timers).toHaveLength(1);

    timers.shift()?.();

    expect(sockets).toHaveLength(2);
  });
});

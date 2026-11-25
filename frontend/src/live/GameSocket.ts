import { tabUser } from '../api/devTab';
import type { ClientMessage, ServerMessage } from '../wire/types';

export type ConnectionState = 'connecting' | 'open' | 'reconnecting' | 'closed';

export interface SocketLike {
  readyState: number;
  onopen: ((event: Event) => void) | null;
  onclose: ((event: CloseEvent) => void) | null;
  onmessage: ((event: MessageEvent) => void) | null;
  send(data: string): void;
  close(code?: number, reason?: string): void;
}

export interface GameSocketOptions {
  createSocket?: (url: string) => SocketLike;
  // the delays between reconnects, the last one repeats
  backoff?: number[];
  setTimer?: (callback: () => void, delay: number) => unknown;
  clearTimer?: (timer: unknown) => void;
  // the heartbeat: a ping this often, and a connection that heard nothing for this long is dropped and opened again
  pingEvery?: number;
  deadAfter?: number;
  now?: () => number;
}

const OPEN = 1;
// the server can't open the game (e.g. saved by another version), and said why in a rejection before closing
const REFUSED = 1008;
// the user's seat changed: the next connection gets the viewer of the new one, at once
const SEAT_CHANGED = 4001;

/**
 * The web socket of one open table. It reconnects with backoff when the connection drops; on a new connection the
 * server sends the game and then the whole log, so nothing is lost. Messages sent while it is down are dropped: the
 * caller sends again with the token of the latest game. A game the server refuses isn't tried again. It pings while
 * open, so the server knows the user is there, and a connection that hears nothing back for long has lost its network:
 * it is dropped and opened again.
 */
export class GameSocket {
  private socket: SocketLike | null = null;
  private attempt = 0;
  private timer: unknown = null;
  private closed = false;
  private readonly createSocket: (url: string) => SocketLike;
  private readonly backoff: number[];
  private readonly setTimer: (callback: () => void, delay: number) => unknown;
  private readonly clearTimer: (timer: unknown) => void;
  private readonly pingEvery: number;
  private readonly deadAfter: number;
  private readonly now: () => number;
  private heartbeat: unknown = null;
  private heardAt = 0;

  constructor(
    private readonly gameId: string,
    private readonly onMessage: (message: ServerMessage) => void,
    private readonly onState: (state: ConnectionState, attempt: number) => void,
    options: GameSocketOptions = {},
  ) {
    this.createSocket = options.createSocket ?? ((url) => new WebSocket(url) as unknown as SocketLike);
    this.backoff = options.backoff ?? [500, 1000, 2000, 4000, 8000];
    this.setTimer = options.setTimer ?? ((callback, delay) => setTimeout(callback, delay));
    this.clearTimer = options.clearTimer ?? ((timer) => clearTimeout(timer as ReturnType<typeof setTimeout>));
    this.pingEvery = options.pingEvery ?? 20_000;
    this.deadAfter = options.deadAfter ?? 50_000;
    this.now = options.now ?? (() => Date.now());
  }

  static url(gameId: string): string {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    // a tab with a dev user of its own names him: a handshake sends no header
    const devUser = tabUser();
    return `${protocol}//${window.location.host}/ws/games/${gameId}${devUser ? `?devUser=${encodeURIComponent(devUser)}` : ''}`;
  }

  connect(): void {
    this.closed = false;
    this.open();
  }

  send(message: ClientMessage): boolean {
    if (this.socket?.readyState !== OPEN) {
      return false;
    }
    this.socket.send(JSON.stringify(message));
    return true;
  }

  close(): void {
    this.closed = true;
    this.stopHeartbeat();
    if (this.timer !== null) {
      this.clearTimer(this.timer);
      this.timer = null;
    }
    this.socket?.close(1000, 'Left the table');
    this.socket = null;
    this.onState('closed', this.attempt);
  }

  private open(): void {
    this.onState(this.attempt === 0 ? 'connecting' : 'reconnecting', this.attempt);
    const socket = this.createSocket(GameSocket.url(this.gameId));
    this.socket = socket;
    socket.onopen = () => {
      this.attempt = 0;
      this.heardAt = this.now();
      this.onState('open', 0);
      this.beat(socket);
    };
    socket.onmessage = (event) => {
      this.heardAt = this.now();
      const message = JSON.parse(String(event.data)) as ServerMessage;
      if (message.type !== 'pong') {
        this.onMessage(message);
      }
    };
    socket.onclose = (event) => {
      if (this.closed || this.socket !== socket) {
        return;
      }
      this.stopHeartbeat();
      if (event.code === REFUSED) {
        this.closed = true;
        this.socket = null;
        this.onState('closed', this.attempt);
        return;
      }
      this.reconnect(event.code === SEAT_CHANGED ? 0 : this.backoff[Math.min(this.attempt, this.backoff.length - 1)]);
    };
  }

  private reconnect(delay: number): void {
    this.attempt += 1;
    this.onState('reconnecting', this.attempt);
    this.timer = this.setTimer(() => {
      this.timer = null;
      if (!this.closed) {
        this.open();
      }
    }, delay);
  }

  // A ping while the socket is open; one that heard nothing for too long is given up, without waiting for its close.
  private beat(socket: SocketLike): void {
    this.heartbeat = this.setTimer(() => {
      this.heartbeat = null;
      if (this.closed || this.socket !== socket) {
        return;
      }
      if (this.now() - this.heardAt > this.deadAfter) {
        socket.onclose = null;
        socket.close(4000, 'Silent');
        this.socket = null;
        this.reconnect(0);
        return;
      }
      if (socket.readyState === OPEN) {
        socket.send(JSON.stringify({ type: 'ping' } satisfies ClientMessage));
      }
      this.beat(socket);
    }, this.pingEvery);
  }

  private stopHeartbeat(): void {
    if (this.heartbeat !== null) {
      this.clearTimer(this.heartbeat);
      this.heartbeat = null;
    }
  }
}

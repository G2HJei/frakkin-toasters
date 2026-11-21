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
}

const OPEN = 1;
// the server can't open the game (e.g. saved by another version), and said why in a rejection before closing
const REFUSED = 1008;

/**
 * The web socket of one open table. It reconnects with backoff when the connection drops; on a new connection the
 * server sends the game and then the whole log, so nothing is lost. Messages sent while it is down are dropped: the
 * caller sends again with the token of the latest game. A game the server refuses isn't tried again.
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
  }

  static url(gameId: string): string {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${protocol}//${window.location.host}/ws/games/${gameId}`;
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
      this.onState('open', 0);
    };
    socket.onmessage = (event) => {
      this.onMessage(JSON.parse(String(event.data)) as ServerMessage);
    };
    socket.onclose = (event) => {
      if (this.closed || this.socket !== socket) {
        return;
      }
      if (event.code === REFUSED) {
        this.closed = true;
        this.socket = null;
        this.onState('closed', this.attempt);
        return;
      }
      const delay = this.backoff[Math.min(this.attempt, this.backoff.length - 1)];
      this.attempt += 1;
      this.onState('reconnecting', this.attempt);
      this.timer = this.setTimer(() => {
        this.timer = null;
        if (!this.closed) {
          this.open();
        }
      }, delay);
    };
  }
}

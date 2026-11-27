import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router';
import { useMe, useRoom, type Room } from '../api/queries';
import { AnytimePanel } from '../input/AnytimePanel';
import { InputPanel } from '../input/InputPanel';
import { intentDue, moves, plan, type Answer, type Move } from '../input/answers';
import { useSelection } from '../input/selection';
import type { EventValue } from '../input/eventModel';
import { GameSocket } from '../live/GameSocket';
import { isChatMessage, useChatStore } from '../live/chatStore';
import { chooseMessage, injectMessage, submitMessage, undoMessage, useGameStore } from '../live/gameStore';
import { SandboxToolbar } from '../sandbox/SandboxToolbar';
import { BoardSvg } from '../table/board/BoardSvg';
import { ArtPreview } from '../art/Art';
import { AnswerPopover } from '../input/AnswerPopover';
import { afterPick, AnsweringContext, offerOf, type Answering } from '../input/boardAnswers';
import { pauseLineText, pauseOf } from '../labels/pause';
import { ChatPanel } from '../table/chat/ChatPanel';
import { useYourMoveNotice } from '../table/notify';
import { readLastSeen, writeLastSeen } from '../table/lastSeen';
import { LogPanel } from '../table/log/LogPanel';
import { PlayersPanel } from '../table/players/PlayersPanel';
import { RevealLayer } from '../table/reveal';
import { ReferenceDrawer } from '../table/reference/ReferenceDrawer';
import { ProblemDrawer } from '../table/problem/ProblemDrawer';
import { ResultsDialog } from '../table/results/ResultsDialog';
import { SessionPanel } from '../table/session/SessionPanel';
import { Table, tableContext, type SeatState } from '../table/tableContext';
import { TurnPanel } from '../table/turn/TurnPanel';
import type { ClientMessage } from '../wire/types';
import { RoomPage } from './RoomPage';
import { SiteFrame } from './Site';

export function TablePage() {
  const { id = '' } = useParams();
  const room = useRoom(id);
  const me = useMe();
  const seats = room.data?.players;
  const userNames = useMemo(
    () => Object.fromEntries((seats ?? []).filter((seat) => seat.playerNumber !== null).map((seat) => [seat.playerNumber as number, seat.name])),
    [seats],
  );
  const online = room.data?.online;
  const playerCount = room.data?.mode === 'STANDARD' ? (room.data.playerCount ?? 0) : 0;
  const seatStates = useMemo(() => {
    const states: Partial<Record<number, SeatState>> = {};
    for (let seat = 1; seat <= playerCount; seat++) {
      const holder = (seats ?? []).find((candidate) => candidate.playerNumber === seat);
      states[seat] = !holder ? 'vacant' : (online ?? []).includes(holder.userId) ? 'online' : 'away';
    }
    return states;
  }, [seats, online, playerCount]);
  if (room.data && room.data.status === 'LOBBY') {
    return (
      <SiteFrame>
        <RoomPage room={room.data} />
      </SiteFrame>
    );
  }
  // a game saved by another version can't be opened, so no socket is tried; nor one the host cancelled, nor a running
  // game the user was removed from (a finished one anyone may watch)
  const myId = me.data?.id;
  const inGame = !!room.data && [...room.data.players, ...room.data.observers].some((participant) => participant.userId === myId);
  const unavailable =
    room.error?.message ??
    room.data?.unavailableReason ??
    (room.data?.endReason === 'CANCELLED' ? 'The host cancelled this game.' : null) ??
    (room.data?.status === 'RUNNING' && room.data.mode === 'STANDARD' && myId && !inGame ? 'You are not in this game: join it from the games to watch.' : null);
  if (unavailable) {
    return (
      <SiteFrame>
        <main className="page">
          <p className="error">{unavailable}</p>
          <Link to="/play">Back to the games</Link>
        </main>
      </SiteFrame>
    );
  }
  return <LiveTable gameId={id} room={room.data ?? null} myId={myId} userNames={userNames} seatStates={seatStates} />;
}

interface LiveTableProps {
  gameId: string;
  room: Room | null;
  myId: string | undefined;
  userNames: Partial<Record<number, string>>;
  seatStates: Partial<Record<number, SeatState>>;
}

function LiveTable({ gameId, room, myId, userNames, seatStates }: LiveTableProps) {
  const name = room?.name ?? '';
  const seed = room?.seed ?? null;
  const game = useGameStore((state) => state.game);
  const token = useGameStore((state) => state.token);
  const connection = useGameStore((state) => state.connection);
  const rejected = useGameStore((state) => state.rejected);
  const version = useGameStore((state) => state.version);
  const socket = useRef<GameSocket | null>(null);

  useEffect(() => {
    const store = useGameStore.getState();
    store.reset();
    useChatStore.getState().reset();
    const live = new GameSocket(
      gameId,
      (message) => (isChatMessage(message) ? useChatStore.getState().receive(message) : useGameStore.getState().receive(message)),
      (state, attempt) => useGameStore.getState().setConnection(state, attempt),
    );
    socket.current = live;
    live.connect();
    return () => live.close();
  }, [gameId]);

  const sandbox = game?.viewer?.kind === 'Sandbox';
  const asked = game?.input?.kind === 'Decide' || game?.input?.kind === 'Choose' ? game.input.player : null;
  const { chat, logSeen, chatSeenBefore, unread, decided, markLogSeen } = useTalk(gameId, myId, sandbox);
  const context = useMemo(() => tableContext(game, sandbox, asked, userNames, seatStates), [game, sandbox, asked, userNames, seatStates]);
  const send = (message: ClientMessage) => socket.current?.send(message);
  const all = useMemo(() => moves(game?.input), [game]);
  const picked = useSelection((state) => state.picked);

  // a move picks an option, submits an answer, or picks the option and answers the decision it asks next
  const sendMove = (move: Move, event: Answer | null) => {
    decided();
    const { choose, submit, intent } = plan(move, event);
    if (choose !== null) {
      if (intent && asked !== null) {
        useGameStore.getState().setIntent({ player: asked, event: intent, afterVersion: version });
      }
      send(chooseMessage(token, choose));
    } else if (submit) {
      send(submitMessage(token, submit));
    }
  };

  // the board and the mats: what the moves left name lights up; a click picks it, and sends the only move left
  const answering = useMemo<Answering>(
    () => ({
      offer: offerOf(all, picked),
      onTarget: (target, anchor) => {
        const selection = useSelection.getState();
        const { send: only } = afterPick(all, selection.picked, target);
        if (only && !only.pick) {
          selection.clear();
          sendMove(only, only.event);
          return;
        }
        selection.pick(target, anchor);
        if (only) {
          selection.startPick(only);
        }
      },
    }),
    // sendMove reads the latest token and version
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [all, picked, token, version],
  );

  // a new game answers the decision a move chose its option for, while it still takes it; the panel stays busy until
  // the game after that answer comes (a rejection clears it too)
  useEffect(() => {
    const store = useGameStore.getState();
    const intent = store.intent;
    if (!intent || version <= intent.afterVersion) {
      return;
    }
    const due = intent.sent ? null : intentDue(intent, version, game?.input);
    if (due) {
      store.setIntent({ ...intent, sent: true, afterVersion: version });
      send(submitMessage(store.token, due));
    } else {
      store.setIntent(null);
    }
  }, [version, game]);

  const yourMove = game?.input?.kind === 'Decide' || game?.input?.kind === 'Choose';
  // what the game asks, for the notification while the tab is in the background
  const asks = yourMove ? [pauseOf(game?.input, context).heading, pauseLineText(pauseOf(game?.input, context))].filter(Boolean).join(': ') : null;
  useYourMoveNotice(yourMove, name || 'Frakkin Toasters', asks, gameId, sandbox);
  useEffect(() => {
    document.title = `${yourMove ? '● ' : ''}${name || 'Frakkin Toasters'}`;
    // the tab's icon marks an input for you too
    const icon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
    if (icon) {
      icon.href = yourMove ? '/favicon-move.svg' : '/logo.svg';
    }
  }, [game, name]);

  return (
    <Table.Provider value={context}>
      <AnsweringContext.Provider value={answering}>
        <main className="table-page">
          {!game ? (
            // before a game, only a refusal is rejected: the server says why it can't open the game
            rejected ? (
              <div className="connecting">
                <p className="error">{rejected.message}</p>
                <Link to="/">Back</Link>
              </div>
            ) : (
              <p className="muted connecting">Connecting…</p>
            )
          ) : (
            <div className="table-main">
              <div className="table-side left">
                {/* the turn and the decision in one panel, red while the game waits for the viewer */}
                <div className={`turn-input ${yourMove ? 'your-move' : ''}`.trim()}>
                  <TurnPanel name={name} connection={connection} />
                  <InputPanel onMove={sendMove} />
                </div>
                <AnytimePanel
                  onSubmit={(event) => {
                    decided();
                    send(submitMessage(token, event));
                  }}
                />
                {/* the log, or the chat in its place, takes what the panels above leave */}
                {chat.shown ? (
                  <ChatPanel
                    toggle={<ChatToggle chat={chat} unread={unread} />}
                    writes={writes(room, myId)}
                    seenBefore={chatSeenBefore}
                    send={(text: string) => !!send({ type: 'say', text })}
                  />
                ) : (
                  <LogPanel toggle={<ChatToggle chat={chat} unread={unread} />} seenBefore={sandbox ? null : logSeen} onSeen={markLogSeen} />
                )}
              </div>
              <div className={`board ${yourMove ? 'your-move' : ''}`.trim()}>
                <BoardSvg />
              </div>
              <div className="table-side right">
                {sandbox && (
                  <SandboxToolbar
                    gameId={gameId}
                    seed={seed}
                    onUndo={() => {
                      // the game comes back as it was: nothing to animate
                      useGameStore.getState().expectSnapshot();
                      send(undoMessage(token));
                    }}
                    onInject={(event: EventValue) => send(injectMessage(token, event))}
                  />
                )}
                {room && room.mode === 'STANDARD' && room.status !== 'LOBBY' && <SessionPanel room={room} myId={myId} />}
                <PlayersPanel />
              </div>
            </div>
          )}
          <ArtPreview />
          <RevealLayer />
          <AnswerPopover onMove={sendMove} />
          <ReferenceDrawer />
          <ResultsDialog gameId={gameId} room={room} myId={myId} />
          <ProblemDrawer gameId={gameId} />
        </main>
      </AnsweringContext.Provider>
    </Table.Provider>
  );
}

// The players and observers of a game write in its chat; whoever only watches a finished game reads it.
function writes(room: Room | null, myId: string | undefined): boolean {
  return !!room && room.mode === 'STANDARD' && [...room.players, ...room.observers].some((participant) => participant.userId === myId);
}

interface ChatSwitch {
  shown: boolean;
  show(shown: boolean): void;
}

// The chat in the log's place and back: one button over the panel's top right, named for where it goes ("Logs" while
// the chat shows), with what came since the chat was last open. A sandbox has its "Facts" switch there instead.
function ChatToggle({ chat, unread }: { chat: ChatSwitch; unread: number }) {
  return (
    <button type="button" className="log-toggle cf" title={chat.shown ? 'Back to the log' : 'Show the chat'} onClick={() => chat.show(!chat.shown)}>
      {chat.shown ? 'Logs' : 'Chat'}
      {unread > 0 && (
        <span className="unread" aria-label={`${unread} new`}>
          {unread}
        </span>
      )}
    </button>
  );
}

/**
 * Where the viewer left off, kept per game and user in this browser. The log's red line goes above the first entry
 * after his last decision: it moves on with the game that follows his next one. The chat's goes under the last message
 * he had seen when he opened it, and everything in it is seen while it is open. A sandbox has neither.
 */
function useTalk(gameId: string, myId: string | undefined, sandbox: boolean) {
  const logLength = useGameStore((state) => state.log.length);
  const version = useGameStore((state) => state.version);
  const messages = useChatStore((state) => state.messages);
  const key = (kind: string) => `${kind}:${gameId}:${myId ?? ''}`;
  const [logSeen, setLogSeen] = useState<number | null>(() => readLastSeen(key('log')));
  const [chatSeen, setChatSeen] = useState<number | null>(() => readLastSeen(key('chat')));
  const [chatSeenBefore, setChatSeenBefore] = useState<number | null>(null);
  const [shown, setShown] = useState(false);
  const seenOnNextGame = useRef(false);

  const markLogSeen = () => {
    const length = useGameStore.getState().log.length;
    setLogSeen(length);
    writeLastSeen(key('log'), length);
  };
  // the first visit has seen everything so far
  useEffect(() => {
    if (logSeen === null && logLength > 0) {
      markLogSeen();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [logLength]);
  // the game after his decision, with its entries, is his: the line moves under them
  useEffect(() => {
    if (seenOnNextGame.current) {
      seenOnNextGame.current = false;
      markLogSeen();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [version]);

  const newest = messages.at(-1)?.id ?? null;
  useEffect(() => {
    if (shown && newest !== null && newest !== chatSeen) {
      setChatSeen(newest);
      writeLastSeen(key('chat'), newest);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [shown, newest]);

  const unread = shown || sandbox ? 0 : messages.filter((line) => line.id > (chatSeen ?? 0) && line.userId !== myId).length;
  return {
    chat: {
      shown: shown && !sandbox,
      show: (value: boolean) => {
        if (value && !shown) {
          setChatSeenBefore(chatSeen);
        }
        setShown(value);
      },
    },
    logSeen,
    chatSeenBefore,
    unread,
    decided: () => {
      seenOnNextGame.current = true;
    },
    markLogSeen,
  };
}

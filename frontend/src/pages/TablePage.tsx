import { useEffect, useMemo, useRef } from 'react';
import { Link, useParams } from 'react-router';
import { useRoom } from '../api/queries';
import { AnytimePanel } from '../input/AnytimePanel';
import { InputPanel } from '../input/InputPanel';
import { intentDue, moves, plan, type Answer, type Move } from '../input/answers';
import { useSelection } from '../input/selection';
import type { EventValue } from '../input/eventModel';
import { GameSocket } from '../live/GameSocket';
import { chooseMessage, injectMessage, submitMessage, undoMessage, useGameStore } from '../live/gameStore';
import { SandboxToolbar } from '../sandbox/SandboxToolbar';
import { BoardSvg } from '../table/board/BoardSvg';
import { ArtPreview } from '../art/Art';
import { AnswerPopover } from '../input/AnswerPopover';
import { afterPick, AnsweringContext, offerOf, type Answering } from '../input/boardAnswers';
import { LogPanel } from '../table/log/LogPanel';
import { PlayersPanel } from '../table/players/PlayersPanel';
import { RevealLayer } from '../table/reveal';
import { ReferenceDrawer } from '../table/reference/ReferenceDrawer';
import { Table, tableContext } from '../table/tableContext';
import { TurnPanel } from '../table/turn/TurnPanel';
import type { ClientMessage } from '../wire/types';
import { RoomPage } from './RoomPage';
import { TopNav } from './TopNav';

/**
 * The table of a game, in a tab of its own and with no bar over it: its socket, the board as high as the screen, and
 * the panels on its sides: on the left the game and the turn with its skill check and the input, with the log under
 * them, and on the right the sandbox's tools in a sandbox, above the players. The room before the game starts keeps the
 * top bar.
 */
export function TablePage() {
  const { id = '' } = useParams();
  const room = useRoom(id);
  const seats = room.data?.players;
  // the user in each seat, which only the players panel names
  const userNames = useMemo(
    () => Object.fromEntries((seats ?? []).filter((seat) => seat.playerNumber !== null).map((seat) => [seat.playerNumber as number, seat.name])),
    [seats],
  );
  if (room.data && room.data.status === 'LOBBY') {
    return (
      <>
        <TopNav />
        <RoomPage room={room.data} />
      </>
    );
  }
  // a game saved by another version can't be opened, so no socket is tried
  const unavailable = room.error?.message ?? room.data?.unavailableReason;
  if (unavailable) {
    return (
      <>
        <TopNav />
        <main className="page">
          <p className="error">{unavailable}</p>
          <Link to="/">Back</Link>
        </main>
      </>
    );
  }
  return <LiveTable gameId={id} name={room.data?.name ?? ''} seed={room.data?.seed ?? null} userNames={userNames} />;
}

function LiveTable({ gameId, name, seed, userNames }: { gameId: string; name: string; seed: number | null; userNames: Partial<Record<number, string>> }) {
  const game = useGameStore((state) => state.game);
  const token = useGameStore((state) => state.token);
  const connection = useGameStore((state) => state.connection);
  const rejected = useGameStore((state) => state.rejected);
  const version = useGameStore((state) => state.version);
  const socket = useRef<GameSocket | null>(null);

  useEffect(() => {
    const store = useGameStore.getState();
    store.reset();
    const live = new GameSocket(gameId, (message) => useGameStore.getState().receive(message), (state, attempt) =>
      useGameStore.getState().setConnection(state, attempt),
    );
    socket.current = live;
    live.connect();
    return () => live.close();
  }, [gameId]);

  const sandbox = game?.viewer?.kind === 'Sandbox';
  const asked = game?.input?.kind === 'Decide' || game?.input?.kind === 'Choose' ? game.input.player : null;
  const context = useMemo(() => tableContext(game, sandbox, asked, userNames), [game, sandbox, asked, userNames]);
  const send = (message: ClientMessage) => socket.current?.send(message);
  const all = useMemo(() => moves(game?.input), [game]);
  const picked = useSelection((state) => state.picked);

  // a move picks an option, submits an answer, or picks the option and answers the decision it asks next
  const sendMove = (move: Move, event: Answer | null) => {
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
                <TurnPanel name={name} connection={connection} />
                <InputPanel onMove={sendMove} />
                <AnytimePanel onSubmit={(event) => send(submitMessage(token, event))} />
                {/* the log takes what the panels above leave */}
                <LogPanel />
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
                <PlayersPanel />
              </div>
            </div>
          )}
          <ArtPreview />
          <RevealLayer />
          <AnswerPopover onMove={sendMove} />
          <ReferenceDrawer />
        </main>
      </AnsweringContext.Provider>
    </Table.Provider>
  );
}

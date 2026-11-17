import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useParams } from 'react-router';
import { useRoom } from '../api/queries';
import { InputPanel } from '../input/InputPanel';
import type { EventValue } from '../input/eventModel';
import { GameSocket } from '../live/GameSocket';
import { chooseMessage, injectMessage, submitMessage, undoMessage, useGameStore } from '../live/gameStore';
import { SandboxToolbar } from '../sandbox/SandboxToolbar';
import { BoardExtras, DecksPanel } from '../table/board/BoardPanels';
import { BoardSvg } from '../table/board/BoardSvg';
import { actLocation, actOption, leavesArea, moveEvent, moveTargets } from '../input/boardMoves';
import { skillCardArt } from '../art/images';
import { ArtImg, ArtPreview } from '../art/Art';
import { constantLabel, skillCardLabel } from '../labels/describe';
import { LogPanel } from '../table/log/LogPanel';
import { PlayersPanel } from '../table/players/PlayersPanel';
import { Table, tableContext } from '../table/tableContext';
import { SkillCheckPanel, TurnPanel } from '../table/turn/TurnPanel';
import type { ClientMessage } from '../wire/types';
import { RoomPage } from './RoomPage';

/** The table of a game: its socket, the state panels, the input, and the sandbox's tools in a sandbox. */
export function TablePage() {
  const { id = '' } = useParams();
  const room = useRoom(id);
  if (room.data && room.data.status === 'LOBBY') {
    return <RoomPage room={room.data} />;
  }
  if (room.error) {
    return (
      <main className="page">
        <p className="error">{room.error.message}</p>
        <Link to="/">Back</Link>
      </main>
    );
  }
  return <LiveTable gameId={id} name={room.data?.name ?? ''} seed={room.data?.seed ?? null} />;
}

function LiveTable({ gameId, name, seed }: { gameId: string; name: string; seed: number | null }) {
  const game = useGameStore((state) => state.game);
  const token = useGameStore((state) => state.token);
  const connection = useGameStore((state) => state.connection);
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
  const context = useMemo(() => tableContext(game, sandbox, asked), [game, sandbox, asked]);
  const targets = useMemo(() => moveTargets(game), [game]);
  const [pendingMove, setPendingMove] = useState<string | null>(null);
  const send = (message: ClientMessage) => socket.current?.send(message);

  useEffect(() => {
    const yourMove = game?.input?.kind === 'Decide' || game?.input?.kind === 'Choose';
    document.title = `${yourMove ? '● ' : ''}${name || 'Frakkin Toasters'}`;
    // the tab's icon marks an input for you too
    const icon = document.querySelector<HTMLLinkElement>('link[rel="icon"]');
    if (icon) {
      icon.href = yourMove ? '/favicon-move.svg' : '/favicon.svg';
    }
  }, [game, name]);

  return (
    <Table.Provider value={context}>
      <main className="table-page">
        <header className="table-header">
          <Link to="/">← Games</Link>
          <h1>{name}</h1>
          <span className={`connection ${connection}`}>{connection}</span>
        </header>
        {!game ? (
          <p className="muted">Connecting…</p>
        ) : (
          <>
            {sandbox && (
              <SandboxToolbar
                gameId={gameId}
                seed={seed}
                onUndo={() => send(undoMessage(token))}
                onInject={(event: EventValue) => send(injectMessage(token, event))}
              />
            )}
            <div className="table-grid">
              <div className="column">
                <InputPanel onChoose={(option) => send(chooseMessage(token, option))} onSubmit={(event) => send(submitMessage(token, event))} />
                <TurnPanel />
                <SkillCheckPanel />
                <LogPanel />
              </div>
              <div className="column">
                <BoardSvg
                  targets={targets}
                  onLocation={(location) => {
                    const from = game.players?.find((player) => player.number === asked)?.location;
                    if (asked !== null && leavesArea(from, location)) {
                      setPendingMove(location);
                    } else if (asked !== null) {
                      send(submitMessage(token, moveEvent(asked, location, null)));
                    }
                  }}
                  actAt={actLocation(game)}
                  onAct={() => send(chooseMessage(token, actOption(game) ?? 0))}
                />
                {pendingMove && asked !== null && (
                  <section className="panel move-prompt" aria-label="discard to move">
                    <h2>Moving to {constantLabel('Location', pendingMove)}: discard a Skill Card</h2>
                    <div className="picker">
                      {context.hand(asked).map((card, index) => (
                        <button key={index} type="button" className="tile card-pick" onClick={() => { send(submitMessage(token, moveEvent(asked, pendingMove, card))); setPendingMove(null); }}>
                          {skillCardArt(card) && <ArtImg art={skillCardArt(card)!} />}
                          <span>{skillCardLabel(card)}</span>
                        </button>
                      ))}
                      <button type="button" onClick={() => { send(submitMessage(token, moveEvent(asked, pendingMove, null))); setPendingMove(null); }}>No discard</button>
                      <button type="button" className="link" onClick={() => setPendingMove(null)}>Cancel</button>
                    </div>
                  </section>
                )}
                <BoardExtras />
              </div>
              <div className="column">
                <PlayersPanel />
                <DecksPanel />
              </div>
            </div>
          </>
        )}
        <ArtPreview />
      </main>
    </Table.Provider>
  );
}

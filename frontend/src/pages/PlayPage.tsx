import { useState, type ReactNode } from 'react';
import { Link, useNavigate } from 'react-router';
import { withTabUser } from '../api/devTab';
import { useGames, useLobbyAction, type GameSummary } from '../api/queries';
import { openGameTab } from './gameTab';

/**
 * The lobby: my games with hosting, my sandboxes with a new one, open lobbies to join and running games to watch. Each
 * game opens in a tab of its own.
 */
export function PlayPage() {
  const games = useGames();
  const action = useLobbyAction();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const mine = games.data?.mine ?? [];
  const join = (game: GameSummary, as: 'PLAYER' | 'OBSERVER') => {
    const tab = openGameTab(navigate);
    action.mutate({ path: `/api/games/${game.id}/join`, body: { as } }, { onSuccess: () => tab.go(game.id), onError: tab.close });
  };

  return (
    <main className="page wide play">
      <header className="page-head">
        <h1 className="label">Play</h1>
        <p className="muted">The Cylons were created by man. They rebelled. They evolved. … And they have a plan.</p>
      </header>
      {action.error && <p className="error">{action.error.message}</p>}
      {/* my games and sandboxes on the left, the others' games on the right; one column on a narrow screen */}
      <div className="play-grid">
        <div className="play-column">
          <GameList
            title="My games"
            empty="You are in no game. Host one, or join an open lobby."
            games={mine.filter((game) => game.mode === 'STANDARD')}
            tools={
              <form
                className="host-form"
                onSubmit={(event) => {
                  event.preventDefault();
                  const tab = openGameTab(navigate);
                  action.mutate(
                    { path: '/api/games', body: { name } },
                    {
                      onSuccess: (room) => {
                        tab.go(room.id);
                        setName('');
                      },
                      onError: tab.close,
                    },
                  );
                }}
              >
                <input value={name} onChange={(event) => setName(event.target.value)} placeholder="Game name" aria-label="game name" />
                <button type="submit" className="primary">
                  Host a game
                </button>
              </form>
            }
          />
          <GameList
            title="My sandboxes"
            empty="No sandbox yet."
            games={mine.filter((game) => game.mode === 'SANDBOX')}
            tools={
              <Link className="button primary" to="/sandbox/new">
                New sandbox
              </Link>
            }
          />
        </div>
        <div className="play-column">
          <GameList
            title="Open lobbies"
            empty="No lobby is open right now."
            games={games.data?.open ?? []}
            action={(game) => (
              <>
                <button type="button" className="primary" onClick={() => join(game, 'PLAYER')}>
                  Join
                </button>
                <button type="button" onClick={() => join(game, 'OBSERVER')}>
                  Observe
                </button>
              </>
            )}
          />
          <GameList
            title="Watch"
            empty="No game is running right now."
            games={games.data?.running ?? []}
            action={(game) => (
              <button type="button" onClick={() => join(game, 'OBSERVER')}>
                Watch
              </button>
            )}
          />
        </div>
      </div>
    </main>
  );
}

interface GameListProps {
  title: string;
  empty: string;
  games: GameSummary[];
  // what the list's head offers at its right: hosting, a new sandbox
  tools?: ReactNode;
  action?: (game: GameSummary) => ReactNode;
}

function GameList({ title, empty, games, tools, action }: GameListProps) {
  return (
    <section className="panel cf" aria-label={title}>
      <div className="panel-head">
        <h2>{title}</h2>
        {tools && <div className="panel-tools">{tools}</div>}
      </div>
      {games.length === 0 ? (
        <p className="muted empty">{empty}</p>
      ) : (
        <ul className="games">
          {games.map((game) => (
            <li key={game.id}>
              <div className="game-name">
                {game.participant ? (
                  <Link to={withTabUser(`/games/${game.id}`)} target="_blank">
                    {game.name}
                  </Link>
                ) : (
                  <span>{game.name}</span>
                )}
                <span className={`status-chip ${game.status.toLowerCase()}`}>{game.status.toLowerCase()}</span>
              </div>
              <div className="game-facts muted">
                {game.players} players · host {game.hostName}
                {game.status !== 'LOBBY' ? ` · turn ${game.turn}` : ''}
                {game.winnerTeam ? ` · ${game.winnerTeam.toLowerCase()} won` : ''}
                {game.endReason === 'ABANDONED' ? ' · abandoned' : ''}
                {!game.openable ? ' · saved by another version' : ''}
              </div>
              <div className="game-actions">
                {game.waitingForMe && game.mode === 'STANDARD' && <span className="badge">your move</span>}
                {game.vacantSeats > 0 && (
                  <span className="badge alert">{game.vacantSeats === 1 ? 'a seat is vacant' : `${game.vacantSeats} seats are vacant`}</span>
                )}
                {action?.(game)}
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

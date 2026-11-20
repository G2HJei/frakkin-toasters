import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { useGames, useLobbyAction, type GameSummary } from '../api/queries';
import { openGameTab } from './gameTab';

/** The lobby: open lobbies to join, my games and sandboxes, running games to watch, and hosting a game. Each game opens in a tab of its own. */
export function GamesPage() {
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
    <main className="page">
      <div className="page-actions">
        <Link className="button primary" to="/sandbox/new">
          New sandbox
        </Link>
        <form
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
          <button type="submit">Host a game</button>
        </form>
      </div>
      {action.error && <p className="error">{action.error.message}</p>}
      <GameList title="My sandboxes" games={mine.filter((game) => game.mode === 'SANDBOX')} />
      <GameList title="My games" games={mine.filter((game) => game.mode === 'STANDARD')} />
      <GameList
        title="Open lobbies"
        games={games.data?.open ?? []}
        action={(game) => (
          <>
            <button type="button" onClick={() => join(game, 'PLAYER')}>
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
        games={games.data?.running ?? []}
        action={(game) => (
          <button type="button" onClick={() => join(game, 'OBSERVER')}>
            Watch
          </button>
        )}
      />
    </main>
  );
}

function GameList({ title, games, action }: { title: string; games: GameSummary[]; action?: (game: GameSummary) => React.ReactNode }) {
  return (
    <section className="panel" aria-label={title}>
      <h2>{title}</h2>
      {games.length === 0 ? (
        <p className="muted">None.</p>
      ) : (
        <ul className="games">
          {games.map((game) => (
            <li key={game.id}>
              {game.participant ? <Link to={`/games/${game.id}`} target="_blank">{game.name}</Link> : <span>{game.name}</span>}
              <span className="muted">
                {' '}
                · {game.status.toLowerCase()} · {game.players} players · host {game.hostName}
                {game.status !== 'LOBBY' ? ` · turn ${game.turn}` : ''}
                {game.winnerTeam ? ` · ${game.winnerTeam.toLowerCase()} won` : ''}
                {!game.openable ? ' · saved by another version' : ''}
              </span>
              {game.waitingForMe && game.mode === 'STANDARD' && <span className="badge">your move</span>}
              {action?.(game)}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

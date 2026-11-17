import { Link } from 'react-router';
import { useLobbyAction, useMe, type Room } from '../api/queries';

/** A game before it starts: seats 1 to 7 and the observers, join, leave and switch, and the host's start. */
export function RoomPage({ room }: { room: Room }) {
  const me = useMe();
  const action = useLobbyAction();
  const myId = me.data?.id;
  const seated = room.players.some((player) => player.userId === myId);
  const observing = room.observers.some((observer) => observer.userId === myId);
  const isHost = room.hostUserId === myId;
  const post = (path: string, body?: unknown) => action.mutate({ path: `/api/games/${room.id}/${path}`, body });

  return (
    <main className="page room">
      <Link to="/">← Games</Link>
      <h1>{room.name}</h1>
      <section className="panel" aria-label="seats">
        <h2>Seats</h2>
        <ol className="seats">
          {[1, 2, 3, 4, 5, 6, 7].map((seat) => {
            const player = room.players.find((candidate) => candidate.playerNumber === seat);
            return (
              <li key={seat} className={player ? 'taken' : 'free'}>
                {player ? player.name : <span className="muted">free</span>}
                {player?.userId === room.hostUserId ? <span className="badge">host</span> : null}
              </li>
            );
          })}
        </ol>
        <h2>Observers</h2>
        <p>{room.observers.map((observer) => observer.name).join(', ') || <span className="muted">none</span>}</p>
      </section>
      <div className="page-actions">
        {!seated && <button type="button" onClick={() => post('join', { as: 'PLAYER' })}>Take a seat</button>}
        {!observing && <button type="button" onClick={() => post('join', { as: 'OBSERVER' })}>Observe</button>}
        {(seated || observing) && !isHost && <button type="button" onClick={() => post('leave')}>Leave</button>}
        {isHost && (
          <button type="button" className="primary" disabled={room.players.length < 3} onClick={() => post('start')}>
            Start with {room.players.length} players
          </button>
        )}
      </div>
      {action.error && <p className="error">{action.error.message}</p>}
      {isHost && room.players.length < 3 && <p className="muted">A game needs 3 to 7 players.</p>}
    </main>
  );
}

import { Link } from 'react-router';
import { useLobbyAction, useMe, type Participant, type Room } from '../api/queries';

/**
 * A game before it starts: seats 1 to 7 and the observers, join, leave and switch, and the host's tools: start, remove
 * or ban a user, cancel the game.
 */
export function RoomPage({ room }: { room: Room }) {
  const me = useMe();
  const action = useLobbyAction();
  const myId = me.data?.id;
  const seated = room.players.some((player) => player.userId === myId);
  const observing = room.observers.some((observer) => observer.userId === myId);
  const isHost = room.hostUserId === myId;
  const post = (path: string, body?: unknown) => action.mutate({ path: `/api/games/${room.id}/${path}`, body });
  // whose room page polls: who is there
  const presence = (participant: Participant) => {
    const online = room.online.includes(participant.userId);
    return <span className={`presence ${online ? 'online' : 'away'}`} role="img" aria-label={online ? 'here' : 'away'} title={online ? 'Here' : 'Away'} />;
  };

  // the host's tools beside every other user
  const hostTools = (participant: Participant) =>
    isHost &&
    participant.userId !== myId && (
      <span className="host-tools">
        <button type="button" className="link" onClick={() => post('kick', { userId: participant.userId, ban: false })}>
          Remove
        </button>
        <button
          type="button"
          className="link danger"
          onClick={() => window.confirm(`Ban ${participant.name} from this game?`) && post('kick', { userId: participant.userId, ban: true })}
        >
          Ban
        </button>
      </span>
    );

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
                {player && presence(player)} {player ? player.name : <span className="muted">free</span>}
                {player?.userId === room.hostUserId ? <span className="badge">host</span> : null}
                {player && hostTools(player)}
              </li>
            );
          })}
        </ol>
        <h2>Observers</h2>
        {room.observers.length === 0 ? (
          <p className="muted">none</p>
        ) : (
          <ul className="observers">
            {room.observers.map((observer) => (
              <li key={observer.userId}>
                {presence(observer)} {observer.name}
                {hostTools(observer)}
              </li>
            ))}
          </ul>
        )}
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
        {isHost && (
          <button type="button" className="danger" onClick={() => window.confirm('Cancel this game for everyone?') && post('cancel')}>
            Cancel the game
          </button>
        )}
      </div>
      {action.error && <p className="error">{action.error.message}</p>}
      {isHost && room.players.length < 3 && <p className="muted">A game needs 3 to 7 players.</p>}
    </main>
  );
}

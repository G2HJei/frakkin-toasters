import { useNavigate } from 'react-router';
import { useLobbyAction, type Room, type Vote, type VoteKind } from '../../api/queries';
import { SeatPresence } from '../players/PlayersPanel';
import { useResults } from '../results/ResultsDialog';
import { useTable } from '../tableContext';

export interface SessionPanelProps {
  room: Room;
  myId: string | undefined;
}

/**
 * The seats of a running game and who holds them: a vacant seat to take, a vote to hand a seat over or to abandon the
 * game (the host's passes at once), and leaving the seat or the table. The game waits at a vacant seat like at anyone's;
 * the server decides who may do what and says why it refuses. Open while a seat is vacant or a vote is open.
 */
export function SessionPanel({ room, myId }: SessionPanelProps) {
  const table = useTable();
  const action = useLobbyAction();
  const navigate = useNavigate();
  const post = (path: string, body?: unknown, then?: () => void) =>
    action.mutate({ path: `/api/games/${room.id}/${path}`, body }, { onSuccess: () => then?.() });
  const isHost = room.hostUserId === myId;
  const mySeat = room.players.find((player) => player.userId === myId)?.playerNumber ?? null;
  const observing = room.observers.some((observer) => observer.userId === myId);
  const seats = Array.from({ length: room.playerCount ?? 0 }, (_, index) => index + 1);
  const abandon = voteOf(room.votes, 'ABANDON', 0);
  const attention = room.vacantSeats.length > 0 || room.votes.length > 0;
  const votes = room.votes.length;
  const running = room.status === 'RUNNING';

  const voteButton = (kind: VoteKind, seat: number, label: string, hostLabel: string, confirmText: string) => {
    const vote = voteOf(room.votes, kind, seat);
    const mine = !!vote?.voters.includes(myId ?? '');
    if (isHost) {
      return (
        <>
          {vote && <span className="muted">{tally(vote)}</span>}
          <button type="button" onClick={() => window.confirm(confirmText) && post('votes', { kind, playerNumber: seat })}>
            {hostLabel}
          </button>
        </>
      );
    }
    if (mySeat === null) {
      return vote ? <span className="muted">{tally(vote)}</span> : null;
    }
    return (
      <button
        type="button"
        className={mine ? 'on' : undefined}
        aria-pressed={mine}
        onClick={() => post(mine ? 'votes/withdraw' : 'votes', { kind, playerNumber: seat })}
      >
        {mine ? 'Withdraw my vote' : label}
        {vote ? ` (${tally(vote)})` : ''}
      </button>
    );
  };

  return (
    <section className="panel cf session" aria-label="seats">
      {/* the key opens it again when something needs the players */}
      <details key={attention ? 'attention' : 'quiet'} open={attention}>
        <summary>
          Seats
          {room.vacantSeats.length > 0 && <span className="badge alert">{room.vacantSeats.length} vacant</span>}
          {votes > 0 && <span className="badge">{votes === 1 ? '1 vote' : `${votes} votes`}</span>}
        </summary>
        {!running && room.endReason === 'ABANDONED' && (
          <p className="note">
            The game was abandoned.{' '}
            <button type="button" className="link" onClick={() => useResults.getState().show()}>
              Results
            </button>
          </p>
        )}
        <ol className="session-seats">
          {seats.map((seat) => {
            const holder = room.players.find((player) => player.playerNumber === seat);
            const vacant = !holder;
            return (
              <li key={seat} className={vacant ? 'vacant' : holder.userId === myId ? 'mine' : undefined} data-seat={seat}>
                <span className="seat-name">
                  {holder && <SeatPresence state={table.seatState(seat)} />}
                  <strong>{table.playerName(seat)}</strong>
                  <span className="muted">{vacant ? 'vacant' : holder.name}</span>
                  {holder?.userId === room.hostUserId && <span className="badge">host</span>}
                </span>
                <span className="seat-actions toolbar">
                  {running && vacant && mySeat === null && (
                    <button type="button" className="primary" onClick={() => post('seat', { playerNumber: seat })}>
                      Take this seat
                    </button>
                  )}
                  {running && !vacant && holder.userId === myId && (
                    <button
                      type="button"
                      onClick={() => window.confirm('Leave your seat? You watch on, and anyone may take it.') && post('leave')}
                    >
                      Leave my seat
                    </button>
                  )}
                  {running &&
                    !vacant &&
                    holder.userId !== myId &&
                    voteButton(
                      'VACATE',
                      seat,
                      'Vote to hand over',
                      'Hand over',
                      `Hand ${holder.name}'s seat over? It becomes vacant, and anyone may take it.`,
                    )}
                  {running && isHost && !vacant && holder.userId !== myId && (
                    <button
                      type="button"
                      onClick={() =>
                        window.confirm(`Ban ${holder.name}? The seat becomes vacant, and they can't come back.`) &&
                        post('kick', { userId: holder.userId, ban: true })
                      }
                    >
                      Ban
                    </button>
                  )}
                </span>
              </li>
            );
          })}
        </ol>
        {room.observers.length > 0 && (
          <p className="session-observers">
            <span className="muted">Observers </span>
            {room.observers.map((observer, index) => (
              <span key={observer.userId}>
                {index > 0 && ', '}
                <span className={`presence ${room.online.includes(observer.userId) ? 'online' : 'away'}`} /> {observer.name}
              </span>
            ))}
          </p>
        )}
        {running && (
          <div className="toolbar session-actions">
            {(mySeat !== null || isHost) &&
              voteButton('ABANDON', 0, 'Vote to abandon', 'Abandon the game', 'Abandon the game? It ends without a winner.')}
            {mySeat === null && !isHost && abandon && <span className="muted">Abandon: {tally(abandon)}</span>}
            {observing && !isHost && (
              <button type="button" onClick={() => post('leave', undefined, () => navigate('/'))}>
                Leave the table
              </button>
            )}
          </div>
        )}
        {action.error && <p className="error">{action.error.message}</p>}
      </details>
    </section>
  );
}

function voteOf(votes: Vote[], kind: VoteKind, seat: number): Vote | undefined {
  return votes.find((vote) => vote.kind === kind && vote.playerNumber === seat);
}

function tally(vote: Vote): string {
  return `${vote.voters.length} of ${vote.needed}`;
}

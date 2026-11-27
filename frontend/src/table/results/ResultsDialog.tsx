import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router';
import { create } from 'zustand';
import { useLobbyAction, type Room } from '../../api/queries';
import { ArtImg, CardTile } from '../../art/Art';
import { cardArt, characterArt } from '../../art/images';
import { constantLabel } from '../../labels/describe';
import { endingHeading, gameSummary, momentLabel, PLAYER_STATISTICS, teamLabel } from '../../labels/results';
import { useGameStore } from '../../live/gameStore';
import type { GameReport, PlayerReport } from '../../wire/types';
import { useTable } from '../tableContext';
import { ResourcesChart } from './ResourcesChart';

interface ResultsState {
  open: boolean;
  show(): void;
  hide(): void;
}

/** Whether the results lie over the table; they open by themselves once, when the game ends. */
export const useResults = create<ResultsState>((set) => ({
  open: false,
  show: () => set({ open: true }),
  hide: () => set({ open: false }),
}));

export interface ResultsDialogProps {
  gameId: string;
  room: Room | null;
  myId: string | undefined;
}

/**
 * An abandoned game has no report: it names its players. The table's URL is the read-only link to share.
 */
export function ResultsDialog({ gameId, room, myId }: ResultsDialogProps) {
  const open = useResults((state) => state.open);
  const report = useGameStore((state) => state.report);
  const abandoned = room?.endReason === 'ABANDONED';
  const ended = !!report || abandoned;

  // once per game, when it is over
  useEffect(() => {
    if (ended) {
      useResults.getState().show();
    }
  }, [ended, gameId]);
  useEffect(() => () => useResults.getState().hide(), [gameId]);

  if (!open || !ended) {
    return null;
  }
  return (
    <div className="results-backdrop" onClick={() => useResults.getState().hide()}>
      <section className="results panel cf" role="dialog" aria-modal="true" aria-label="results" onClick={(event) => event.stopPropagation()}>
        <header className="results-head">
          <div>
            <h2>{endingHeading(report?.winner, abandoned)}</h2>
            {report && <p className="muted">{gameSummary(report)}</p>}
          </div>
          <button type="button" className="link" aria-label="Close" onClick={() => useResults.getState().hide()}>
            Back to the table
          </button>
        </header>
        <div className="results-body">
          {report ? <Report report={report} room={room} /> : <Abandoned room={room} />}
        </div>
        <Actions gameId={gameId} room={room} myId={myId} />
      </section>
    </div>
  );
}

function holdersOf(room: Room | null, seat: number): string {
  const names: string[] = [];
  for (const holder of room?.seatHistory ?? []) {
    if (holder.playerNumber === seat && !names.includes(holder.name)) {
      names.push(holder.name);
    }
  }
  return names.join(', then ');
}

function Report({ report, room }: { report: GameReport; room: Room | null }) {
  const table = useTable();
  const players = report.players ?? [];
  const winners = players.filter((player) => (report.winners ?? []).includes(player.number));
  const statistics = report.statistics;
  return (
    <>
      <section aria-label="winners" className="results-winners">
        {winners.map((player) => (
          <span key={player.number} className="winner">
            <Portrait character={player.character} />
            <span>
              <strong>{constantLabel('Character', player.character)}</strong>
              <span className="muted"> {holdersOf(room, player.number)}</span>
            </span>
          </span>
        ))}
      </section>
      <section aria-label="players">
        <h3>Players</h3>
        <div className="results-scroll">
          <table className="results-players">
            <thead>
              <tr>
                <th scope="col">Player</th>
                <th scope="col">Loyalty</th>
                <th scope="col">Motive</th>
                <th scope="col">Turns</th>
                {PLAYER_STATISTICS.map((statistic) => (
                  <th key={statistic.key} scope="col" title={statistic.title}>
                    {statistic.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {players.map((player) => (
                <PlayerRow key={player.number} player={player} users={holdersOf(room, player.number)} won={(report.winners ?? []).includes(player.number)} />
              ))}
            </tbody>
          </table>
        </div>
      </section>
      {statistics && (
        <section aria-label="game statistics">
          <h3>The game</h3>
          <dl className="results-tiles">
            <Tile label="Crisis Cards" value={statistics.crisisCards} />
            <Tile label="Skill checks passed" value={statistics.skillChecks?.PASS ?? 0} />
            <Tile label="Partly passed" value={statistics.skillChecks?.PARTIAL_PASS ?? 0} />
            <Tile label="Failed" value={statistics.skillChecks?.FAIL ?? 0} />
            <Tile label="Jumps" value={statistics.jumps} />
            {Object.entries(statistics.shipsLost ?? {}).map(([type, count]) => (
              <Tile key={type} label={`${constantLabel('ShipType', type)} lost`} value={count ?? 0} />
            ))}
            <Tile label="Vipers damaged" value={statistics.vipersDamaged} />
            <Tile label="Locations damaged" value={statistics.locationsDamaged} />
            <Tile label="Nukes used" value={statistics.nukesUsed} />
          </dl>
        </section>
      )}
      <ResourcesChart resources={report.resources ?? []} />
      <section aria-label="story">
        <h3>The story</h3>
        <ol className="results-story">
          {(report.story ?? []).map((moment, index) => (
            <li key={index} className={`moment ${moment.kind?.toLowerCase() ?? ''}`}>
              <span className="moment-turn">Turn {moment.turn}</span>
              {moment.character && <Portrait character={moment.character} />}
              <span>{momentLabel(moment, table)}</span>
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}

function PlayerRow({ player, users, won }: { player: PlayerReport; users: string; won: boolean }) {
  return (
    <tr className={won ? 'won' : undefined}>
      <th scope="row">
        <span className="results-player">
          <Portrait character={player.character} />
          <span>
            <strong>{constantLabel('Character', player.character)}</strong>
            {(player.formerCharacters ?? []).length > 0 && (
              <span className="muted"> before: {(player.formerCharacters ?? []).map((character) => constantLabel('Character', character)).join(', ')}</span>
            )}
            <span className="muted results-users">{users}</span>
            <span className={player.cylon ? 'team cylon' : 'team human'}>{teamLabel(player.cylon, player.revealedOnTurn)}</span>
          </span>
        </span>
      </th>
      <td>
        <span className="cards">
          {(player.loyaltyCards ?? []).map((card, index) => (
            <CardTile key={index} art={cardArt('LoyaltyCard', card)} label={constantLabel('LoyaltyCard', card)} size="xs" />
          ))}
        </span>
      </td>
      <td>
        <span className="cards">
          {(player.motiveCards ?? []).length === 0 && <span className="muted">—</span>}
          {(player.motiveCards ?? []).map((card, index) => (
            <CardTile key={index} art={cardArt('MotiveCard', card)} label={constantLabel('MotiveCard', card)} size="xs" />
          ))}
        </span>
      </td>
      <td>{player.turns}</td>
      {PLAYER_STATISTICS.map((statistic) => (
        <td key={statistic.key}>{player.statistics?.[statistic.key] ?? 0}</td>
      ))}
    </tr>
  );
}

function Portrait({ character }: { character: string | null | undefined }) {
  const art = characterArt(character);
  return <span className="results-portrait">{art ? <ArtImg art={art} /> : null}</span>;
}

function Tile({ label, value }: { label: string; value: number }) {
  return (
    <div className="tile-stat">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}

// An abandoned game shows nothing of what was hidden: only who held each seat.
function Abandoned({ room }: { room: Room | null }) {
  const table = useTable();
  const seats = Array.from({ length: room?.playerCount ?? 0 }, (_, index) => index + 1);
  return (
    <section aria-label="players">
      <p className="muted">It ended without a winner, and what was hidden stays hidden.</p>
      <ul className="results-abandoned">
        {seats.map((seat) => (
          <li key={seat}>
            <strong>{table.playerName(seat)}</strong> <span className="muted">{holdersOf(room, seat) || 'nobody'}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}

function Actions({ gameId, room, myId }: { gameId: string; room: Room | null; myId: string | undefined }) {
  const action = useLobbyAction();
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);
  const isHost = room?.hostUserId === myId;
  const standard = room?.mode === 'STANDARD';
  return (
    <footer className="results-actions toolbar">
      <button
        type="button"
        onClick={() => {
          void navigator.clipboard?.writeText(window.location.href).then(() => setCopied(true));
        }}
        title="Anyone logged in can watch the finished game at this link"
      >
        {copied ? 'Link copied' : 'Copy the link to this game'}
      </button>
      {standard && room?.rematchId && (
        <Link className="button primary" to={`/games/${room.rematchId}`}>
          Go to the rematch
        </Link>
      )}
      {standard && !room?.rematchId && isHost && (
        <button
          type="button"
          className="primary"
          disabled={action.isPending}
          onClick={() => action.mutate({ path: `/api/games/${gameId}/rematch` }, { onSuccess: (rematch) => navigate(`/games/${rematch.id}`) })}
        >
          Rematch with the same seats
        </button>
      )}
      {standard && !room?.rematchId && !isHost && <span className="muted">The host can open a rematch.</span>}
      {action.error && <span className="error">{action.error.message}</span>}
    </footer>
  );
}

import { useGameStore } from '../live/gameStore';
import { useTable } from '../table/tableContext';
import { schema } from '../wire/schema';
import type { GameProjection } from '../wire/types';
import { EventBuilder } from './EventBuilder';
import type { EventValue } from './eventModel';

export interface AnytimePanelProps {
  onSubmit(event: EventValue): void;
}

/**
 * What a player may do at any pause without being asked (`AnytimeEvent`): a Cylon Leader reveals a Motive Card, even
 * while the game waits for someone else. A player sees his own; a sandbox every player's. The pending input stays as it
 * is; the engine judges whether the requirement is met.
 */
export function AnytimePanel({ onSubmit }: AnytimePanelProps) {
  const table = useTable();
  const version = useGameStore((state) => state.version);
  const game = table.game;
  if (!game || game.outcome || schema.lists.anytimeEvents.length === 0) {
    return null;
  }
  const actors = anytimeActors(game);
  if (actors.length === 0) {
    return null;
  }
  return (
    <section className="panel cf anytime-panel" aria-label="at any time">
      {actors.map(({ playerNumber, motiveCards }) => (
        <details key={playerNumber}>
          <summary>{table.playerName(playerNumber)}: reveal a Motive Card</summary>
          <EventBuilder
            key={`${version}-${playerNumber}`}
            action="AnytimeEvent"
            playerNumber={playerNumber}
            choices={{ MotiveCard: motiveCards }}
            submitLabel="Reveal"
            onSubmit={onSubmit}
          />
        </details>
      ))}
    </section>
  );
}

// The players the viewer acts for who hold a Motive Card face down: only a narrowing, the engine judges.
function anytimeActors(game: GameProjection): { playerNumber: number; motiveCards: string[] }[] {
  const viewer = game.viewer;
  const players = (game.players ?? []).filter((player) =>
    viewer?.kind === 'Sandbox' || (viewer?.kind === 'PlayerViewer' && viewer.playerNumber === player.number),
  );
  return players
    .map((player) => ({
      playerNumber: player.number,
      motiveCards: (player.motiveCards?.cards ?? []).flatMap((card) => (card ? [card] : [])),
    }))
    .filter((actor) => actor.motiveCards.length > 0);
}

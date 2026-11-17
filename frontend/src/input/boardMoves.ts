// The inputs answered on the board. Each builds the same event, or picks the same option, as the builder would for the
// same answer, so the builder can stay as "Advanced". The board only offers plausible answers; the engine judges them.
import { emptyEvent, toWire, type EventValue } from './eventModel';
import { isSpace, LAYOUT } from '../table/board/layout';
import type { Followup, GameProjection, Location, SkillCard } from '../wire/types';

/** The locations a pending move can go to: any that isn't in space, other than the mover's own. */
export function moveTargets(game: GameProjection | null): Set<string> {
  const input = game?.input;
  if (input?.kind !== 'Decide' || input.action !== 'MoveEvent') {
    return new Set();
  }
  const here = game?.players?.find((player) => player.number === input.player)?.location;
  return new Set((Object.keys(LAYOUT) as Location[]).filter((location) => !isSpace(location) && location !== here));
}

/** A move that leaves its area of the board, e.g. Galactica for Colonial One, costs a Skill Card. */
export function leavesArea(from: string | null | undefined, to: string): boolean {
  const start = from ? LAYOUT[from as Location]?.area : undefined;
  return start !== undefined && start !== LAYOUT[to as Location]?.area && !isSpace(from);
}

export function moveEvent(playerNumber: number, destination: string, discardCard: SkillCard | null): EventValue {
  return toWire({ ...emptyEvent('MoveEvent', playerNumber), destination, discardCard });
}

/** The option of a choice that asks for a location action, which the "Act" button on the board picks. */
export function actOption(game: GameProjection | null): number | null {
  const input = game?.input;
  if (input?.kind !== 'Choose') {
    return null;
  }
  const index = (input.options ?? []).findIndex((option: Followup) => {
    const event = option.kind === 'Single' ? (option.event as unknown as Record<string, unknown> | null) : null;
    return event?.kind === 'PlayerDecisionEvent' && event.action === 'ActionEvent';
  });
  return index >= 0 ? index : null;
}

/** Where the "Act" button goes: the asked player's location, when he can take its action. */
export function actLocation(game: GameProjection | null): string | null {
  if (actOption(game) === null || game?.input?.kind !== 'Choose') {
    return null;
  }
  const asked = game.input.player;
  return game.players?.find((player) => player.number === asked)?.location ?? null;
}

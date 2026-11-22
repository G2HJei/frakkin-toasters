// The inputs answered on the board. Each builds the same event, or picks the same option, as the builder would for the
// same answer, so the builder can stay as "Advanced". What the board offers comes from the input's legal answers: the
// browser holds no rules.
import { emptyEvent, toWire, type EventValue } from './eventModel';
import { recordNamed } from '../wire/schema';
import type { Followup, GameProjection, LegalAnswers, SkillCard } from '../wire/types';

type Answer = Record<string, unknown> & { kind?: string };

/** The answers of a pending move: the destinations it may take and the discard each needs. */
function moveAnswers(game: GameProjection | null): LegalAnswers | null {
  const input = game?.input;
  return input?.kind === 'Decide' && input.action === 'MoveEvent' ? (input.answers ?? null) : null;
}

/** Every answer as an event: the concrete ones, and the example of each shaped one. */
function answerEvents(answers: LegalAnswers | null | undefined): Answer[] {
  const events = (answers?.events ?? []) as unknown as (Answer | null)[];
  const examples = (answers?.shapes ?? []).map((shape) => (shape?.example ?? null) as unknown as Answer | null);
  return [...events, ...examples].filter((event): event is Answer => event !== null);
}

/** The locations a pending move can go to. */
export function moveTargets(game: GameProjection | null): Set<string> {
  return new Set(
    answerEvents(moveAnswers(game))
      .map((event) => event.destination)
      .filter((destination): destination is string => typeof destination === 'string'),
  );
}

/**
 * @returns the cards a move to the destination discards one of, e.g. from Galactica to Colonial One, or null when it
 * needs none
 */
export function moveDiscards(game: GameProjection | null, destination: string): SkillCard[] | null {
  const shape = (moveAnswers(game)?.shapes ?? []).find(
    (candidate) => (candidate?.example as unknown as Answer | null)?.destination === destination && candidate?.pick?.component === 'discardCard',
  );
  const pick = shape?.pick;
  return pick?.kind === 'SkillCardPick' ? (pick.cards ?? []).filter((card): card is SkillCard => card !== null) : null;
}

export function moveEvent(playerNumber: number, destination: string, discardCard: SkillCard | null): EventValue {
  return toWire({ ...emptyEvent('MoveEvent', playerNumber), destination, discardCard });
}

/** The option of a choice that asks for an action, which the "Act" button on the board picks. */
export function actOption(game: GameProjection | null): number | null {
  const input = game?.input;
  if (input?.kind !== 'Choose') {
    return null;
  }
  const index = (input.options ?? []).findIndex((option: Followup | null) => {
    const event = option?.kind === 'Single' ? (option.event as unknown as Answer | null) : null;
    return event?.kind === 'PlayerDecisionEvent' && event.action === 'ActionEvent';
  });
  return index >= 0 ? index : null;
}

/** Where the "Act" button goes: the asked player's location, when its action is among the answers of that option. */
export function actLocation(game: GameProjection | null): string | null {
  const option = actOption(game);
  const input = game?.input;
  if (option === null || input?.kind !== 'Choose') {
    return null;
  }
  const location = game?.players?.find((player) => player?.number === input.player)?.location ?? null;
  const answers = (input.answers ?? []).find((candidate) => candidate?.option === option)?.answers;
  const acts = answerEvents(answers).some((event) => event.kind !== undefined && recordNamed(event.kind)?.location === location);
  return location !== null && acts ? location : null;
}

// The pending input as moves: every way the asked viewer can answer it, from the legal answers the engine sends with
// it. The panel, the board and the mats show the same moves; a click narrows them by what it names, and one move left
// is sent. The browser adds no rule: a move is an answer the engine listed, or a shape whose filling the engine judges.
import { recordNamed, type TypeRef } from '../wire/schema';
import type { Followup, InputProjection, LegalAnswers, Pick, SkillCard } from '../wire/types';
import { isServerFilled } from './eventModel';

export type Answer = Record<string, unknown> & { kind: string };

export interface Move {
  // the option of the choice it picks, null for an answer of a decision
  option: number | null;
  followup: Followup | null;
  // what it names: the answer it submits, or the event the option is
  event: Answer | null;
  // the event is submitted: an answer of the decision, or of the decision the option asks first
  submits: boolean;
  // a part still to pick, the event being the shape's example
  pick: Pick | null;
}

/** The moves of the pending input, for the viewer it asks. */
export function moves(input: InputProjection | null | undefined): Move[] {
  if (input?.kind === 'Decide') {
    return answerMoves(input.answers, null, null);
  }
  if (input?.kind !== 'Choose') {
    return [];
  }
  return (input.options ?? []).flatMap((followup, option): Move[] => {
    if (!followup) {
      return [];
    }
    const event = followup.kind === 'Single' ? (followup.event as unknown as Answer | null) : null;
    if (event?.kind === 'PlayerDecisionEvent') {
      const answers = (input.answers ?? []).find((candidate) => candidate?.option === option)?.answers;
      const asked = answerMoves(answers, option, followup);
      // an option without answers is picked as it is, and the decision it asks comes next
      return asked.length > 0 ? asked : [{ option, followup, event: null, submits: false, pick: null }];
    }
    return [{ option, followup, event, submits: false, pick: null }];
  });
}

function answerMoves(answers: LegalAnswers | null | undefined, option: number | null, followup: Followup | null): Move[] {
  const events = ((answers?.events ?? []) as unknown as (Answer | null)[])
    .filter((event): event is Answer => event !== null)
    .map((event) => ({ option, followup, event, submits: true, pick: null }));
  const shapes = (answers?.shapes ?? [])
    .filter((shape) => shape?.example && shape.pick)
    .map((shape) => ({ option, followup, event: shape!.example as unknown as Answer, submits: true, pick: shape!.pick }));
  return [...events, ...shapes];
}

/** The option of a decision a move picks first, e.g. the Action step's "take an action". */
export function decisionAction(move: Move): string | null {
  const event = move.followup?.kind === 'Single' ? (move.followup.event as unknown as Answer | null) : null;
  return event?.kind === 'PlayerDecisionEvent' ? String(event.action) : null;
}

/* what a move names */

export type Target =
  | { type: 'kind'; kind: string }
  | { type: 'option'; option: number }
  | { type: 'location'; location: string }
  | { type: 'ship'; id: number }
  | { type: 'player'; playerNumber: number }
  | { type: 'card'; deck: string; card: string | SkillCard };

// the cards a player holds that a move can name, by their enum
const HELD_CARDS = ['QuorumCard', 'MutinyCard', 'LoyaltyCard', 'MotiveCard', 'SuperCrisisCard'];

export function targetKey(target: Target): string {
  switch (target.type) {
    case 'kind':
      return `kind:${target.kind}`;
    case 'option':
      return `option:${target.option}`;
    case 'location':
      return `location:${target.location}`;
    case 'ship':
      return `ship:${target.id}`;
    case 'player':
      return `player:${target.playerNumber}`;
    case 'card':
      return typeof target.card === 'string' ? `card:${target.deck}:${target.card}` : `card:${target.deck}:${target.card.type}:${target.card.value}`;
  }
}

/**
 * What a move names, which a click can pick: its kind, the location of a location's action, and every location, ship,
 * other player and held card among its components, nested records included. Lists are picks, not targets.
 */
export function targetsOf(move: Move): Target[] {
  const found: Target[] = move.option !== null && !move.submits ? [{ type: 'option', option: move.option }] : [];
  const events = move.event ? [move.event] : followupEvents(move.followup);
  for (const event of events) {
    found.push({ type: 'kind', kind: event.kind });
    const record = recordNamed(event.kind);
    if (record?.location) {
      found.push({ type: 'location', location: record.location });
    }
    if (record?.quorumCard) {
      found.push({ type: 'card', deck: 'QuorumCard', card: record.quorumCard });
    }
    if (record?.mutinyCard) {
      found.push({ type: 'card', deck: 'MutinyCard', card: record.mutinyCard });
    }
    collect(event, event.kind, found, move.pick?.component ?? null);
  }
  const keys = new Set<string>();
  return found.filter((target) => !keys.has(targetKey(target)) && keys.add(targetKey(target)));
}

function followupEvents(followup: Followup | null | undefined): Answer[] {
  switch (followup?.kind) {
    case 'Single': {
      const event = followup.event as unknown as Answer | null;
      return event && event.kind !== 'PlayerDecisionEvent' ? [event] : [];
    }
    case 'AllOf':
      return (followup.followups ?? []).flatMap(followupEvents);
    default:
      return [];
  }
}

function collect(value: Record<string, unknown>, recordName: string, found: Target[], skip: string | null, depth = 0) {
  const record = recordNamed(typeof value.kind === 'string' ? value.kind : recordName);
  if (!record || depth > 3) {
    return;
  }
  // a PlayerEvent's playerNumber is who answers; a plain event's names a target
  const submitter = record.interfaces.includes('PlayerEvent');
  for (const component of record.components) {
    const field = value[component.name];
    const serverFilled = isServerFilled(component) && !(component.name === 'playerNumber' && !submitter);
    if (field === null || field === undefined || component.name === skip || serverFilled) {
      continue;
    }
    target(field, component.type, found, depth);
  }
}

function target(field: unknown, type: TypeRef, found: Target[], depth: number) {
  if ((type.t === 'int' || type.t === 'long') && typeof field === 'number') {
    if (type.role === 'player') {
      found.push({ type: 'player', playerNumber: field });
    } else if (type.role === 'ship') {
      found.push({ type: 'ship', id: field });
    }
  } else if (type.t === 'enum' && typeof field === 'string') {
    if (type.name === 'Location') {
      found.push({ type: 'location', location: field });
    } else if (HELD_CARDS.includes(type.name ?? '')) {
      found.push({ type: 'card', deck: type.name ?? '', card: field });
    }
  } else if (type.t === 'ref' && field && typeof field === 'object') {
    if (type.name === 'SkillCard') {
      found.push({ type: 'card', deck: 'SkillCard', card: field as SkillCard });
    } else {
      collect(field as Record<string, unknown>, type.name ?? '', found, null, depth + 1);
    }
  }
}

/** The moves that name every picked target. */
export function narrow(all: Move[], picked: Target[]): Move[] {
  if (picked.length === 0) {
    return all;
  }
  const keys = picked.map(targetKey);
  return all.filter((move) => {
    const named = new Set(targetsOf(move).map(targetKey));
    return keys.every((key) => named.has(key));
  });
}

/* groups for the panel */

export interface MoveGroup {
  key: string;
  // the event kind its moves share, null for an option that isn't one event
  kind: string | null;
  moves: Move[];
}

/** The moves by event kind, in the order they come, an option that isn't one event alone. */
export function groupMoves(all: Move[]): MoveGroup[] {
  const groups: MoveGroup[] = [];
  for (const move of all) {
    const kind = move.event?.kind ?? null;
    const key = kind === null ? `option:${move.option}` : `${move.option ?? ''}:${kind}`;
    const group = groups.find((candidate) => candidate.key === key);
    if (group) {
      group.moves.push(move);
    } else {
      groups.push({ key, kind, moves: [move] });
    }
  }
  return groups;
}

/** The components the events of a group differ in, the part of a shape still to pick left out. */
export function varyingComponents(group: MoveGroup): string[] {
  const kind = group.kind;
  if (!kind || group.moves.length < 2) {
    return [];
  }
  return (recordNamed(kind)?.components ?? [])
    .filter((component) => !isServerFilled(component))
    .map((component) => component.name)
    .filter((name) => {
      const values = new Set(group.moves.map((move) => (move.pick?.component === name ? '?' : canonical(move.event?.[name] ?? null))));
      return values.size > 1;
    });
}

/* sending */

/** The event of a shape with its part picked: one element for a single component, the list for a list. */
export function fillPick(move: Move, picked: unknown[]): Answer | null {
  if (!move.event || !move.pick?.component) {
    return move.event;
  }
  const component = recordNamed(move.event.kind)?.components.find((candidate) => candidate.name === move.pick?.component);
  const list = component?.type.t === 'list' || component?.type.t === 'set';
  return { ...move.event, [move.pick.component]: list ? picked : (picked[0] ?? null) };
}

/** What to send for a move: the option to choose, and the event to submit now or once the option's decision asks. */
export interface Plan {
  choose: number | null;
  submit: Answer | null;
  intent: Answer | null;
}

export function plan(move: Move, event: Answer | null = move.event): Plan {
  if (move.option !== null) {
    return { choose: move.option, submit: null, intent: move.submits ? event : null };
  }
  return { choose: null, submit: event, intent: null };
}

/**
 * @returns the answer to submit for an intent when a game comes: a later game than the one it was chosen in, asking the
 * same player a decision that takes it; null otherwise, which drops the intent
 */
export function intentDue(intent: { player: number; event: unknown; afterVersion: number }, version: number, input: InputProjection | null | undefined): Answer | null {
  if (version <= intent.afterVersion || input?.kind !== 'Decide' || input.player !== intent.player) {
    return null;
  }
  const event = intent.event as Answer;
  return covers(input.answers, event) ? event : null;
}

/** Whether the answers take the event: one of the concrete answers, or a shape with its part picked from the pick. */
export function covers(answers: LegalAnswers | null | undefined, event: Answer): boolean {
  const wanted = canonical(event);
  if ((answers?.events ?? []).some((candidate) => canonical(candidate) === wanted)) {
    return true;
  }
  return (answers?.shapes ?? []).some((shape) => {
    const example = shape?.example as unknown as Answer | null;
    const pick = shape?.pick;
    if (!example || !pick?.component || example.kind !== event.kind) {
      return false;
    }
    const { [pick.component]: picked, ...rest } = event;
    const { [pick.component]: _, ...exampleRest } = example;
    if (canonical(rest) !== canonical(exampleRest)) {
      return false;
    }
    const elements = Array.isArray(picked) ? picked : picked === null || picked === undefined ? [] : [picked];
    return elements.length >= pick.min && elements.length <= pick.max && within(elements, pickElements(pick));
  });
}

export function pickElements(pick: Pick): unknown[] {
  switch (pick.kind) {
    case 'SkillCardPick':
      return pick.cards ?? [];
    case 'QuorumCardPick':
      return pick.cards ?? [];
    case 'ShipPick':
      return pick.shipIds ?? [];
  }
}

// a multiset inclusion
function within(elements: unknown[], from: unknown[]): boolean {
  const left = from.map(canonical);
  return elements.every((element) => {
    const at = left.indexOf(canonical(element));
    return at >= 0 && left.splice(at, 1).length === 1;
  });
}

/**
 * A value as the engine compares it: keys in order, a list as a multiset, and a count per color without its zeros (a
 * draw's selection).
 */
export function canonical(value: unknown): string {
  return JSON.stringify(normalized(value));
}

function normalized(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map(normalized).sort((a, b) => JSON.stringify(a).localeCompare(JSON.stringify(b)));
  }
  if (value && typeof value === 'object') {
    const entries = Object.entries(value as Record<string, unknown>);
    const counts = !('kind' in value) && entries.length > 0 && entries.every(([key, count]) => /^[A-Z_]+$/.test(key) && typeof count === 'number');
    return Object.fromEntries(
      entries
        .filter(([, field]) => !(counts && field === 0))
        .sort(([a], [b]) => a.localeCompare(b))
        .map(([key, field]) => [key, normalized(field)]),
    );
  }
  return value;
}

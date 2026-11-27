// The parts of an answer: what the moves of a group differ in, each with the values it takes. A group whose moves are a
// product of their parts (every card with every target, every pair of cards) is picked part by part instead of one
// chip per combination, and the answers are the check: a value stays open while some answer still has it, and the
// answer the picks name in full is the one sent, as the engine listed it.
import { recordNamed, type TypeRef } from '../wire/schema';
import { canonical, varyingComponents, within, type Move, type MoveGroup } from './answers';

export interface Part {
  // the component's name, or the stem of the numbered siblings it holds ("discardCard")
  key: string;
  components: string[];
  // the type of one value: the component's, or a list's element
  type: TypeRef;
  // several elements: a list, or numbered siblings ("discardCard1", "discardCard2"), in any order
  many: boolean;
  // a single part's values in the order they come; a part of several the elements to pick from, each as often as one
  // move holds it at most
  values: unknown[];
}

// what is picked of each part, by its key: the value of a single part, the elements of a part of several
export type PartPicks = Record<string, unknown[] | undefined>;

const SIBLING = /^(.*\D)\d+$/;

export function partsOf(group: MoveGroup): Part[] {
  const record = group.kind ? recordNamed(group.kind) : undefined;
  if (!record) {
    return [];
  }
  const picked = pickComponents(group.moves);
  const components = varyingComponents(group)
    .filter((name) => !picked.has(name))
    .map((name) => record.components.find((component) => component.name === name))
    .filter((component) => component !== undefined);
  const parts: Part[] = [];
  for (const component of components) {
    if (parts.some((part) => part.components.includes(component.name))) {
      continue;
    }
    const list = isList(component.type);
    const stem = list ? undefined : SIBLING.exec(component.name)?.[1];
    const siblings = stem
      ? components.filter((other) => SIBLING.exec(other.name)?.[1] === stem && canonical(other.type) === canonical(component.type))
      : [component];
    const element = list && component.type.of && typeof component.type.of === 'object' ? component.type.of : component.type;
    const part: Part = {
      key: siblings.length > 1 ? stem! : component.name,
      components: siblings.map((sibling) => sibling.name),
      type: element,
      many: list || siblings.length > 1,
      values: [],
    };
    part.values = part.many ? pool(group.moves, part) : uniqueValues(group.moves.map((move) => partValue(move, part)));
    parts.push(part);
  }
  return parts;
}

/** The options of a choice that are one event, by its kind: `groupMoves` keeps each option apart. */
export function optionGroups(moves: Move[]): MoveGroup[] {
  const groups = new Map<string, MoveGroup>();
  for (const move of moves) {
    const kind = move.event?.kind;
    if (move.option === null || move.submits || !kind) {
      continue;
    }
    const group = groups.get(kind) ?? { key: `options:${kind}`, kind, moves: [] };
    group.moves.push(move);
    groups.set(kind, group);
  }
  return [...groups.values()];
}

export function partValue(move: Move, part: Part): unknown {
  const event: Record<string, unknown> = move.event ?? {};
  if (!part.many) {
    return event[part.components[0]] ?? null;
  }
  if (part.components.length > 1) {
    return part.components.map((name) => event[name]).filter((value) => value !== null && value !== undefined);
  }
  const value = event[part.components[0]];
  return Array.isArray(value) ? value : [];
}

export function distinct(moves: Move[], parts: Part[]): Move[] {
  if (parts.length === 0) {
    return moves;
  }
  const picked = [...pickComponents(moves)];
  const seen = new Set<string>();
  return moves.filter((move) => {
    // a shape is apart from a concrete answer of the same values
    const shape = picked.map((name) => (move.pick?.component === name ? '?' : (move.event?.[name] ?? null)));
    const key = tupleKey([move.option, ...shape, ...parts.map((part) => partValue(move, part))]);
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

/** Whether the moves are a product of their parts: more ways than the largest part has values. */
export function isProduct(moves: Move[], parts: Part[]): boolean {
  if (parts.length === 0) {
    return false;
  }
  const ways = new Set(moves.map((move) => tupleKey(parts.map((part) => partValue(move, part)))));
  return ways.size > Math.max(...parts.map((part) => part.values.length));
}

export function fitting(moves: Move[], parts: Part[], picks: PartPicks): Move[] {
  return moves.filter((move) =>
    parts.every((part) => {
      const picked = picks[part.key];
      if (!picked || picked.length === 0) {
        return true;
      }
      const value = partValue(move, part);
      return part.many ? within(picked, value as unknown[]) : canonical(value) === canonical(picked[0]);
    }),
  );
}

export function allows(moves: Move[], parts: Part[], picks: PartPicks, part: Part, value: unknown): boolean {
  const next = part.many ? [...(picks[part.key] ?? []), value] : [value];
  return fitting(moves, parts, { ...picks, [part.key]: next }).length > 0;
}

export function shared(moves: Move[], part: Part): { value: unknown } | null {
  if (moves.length === 0) {
    return null;
  }
  const first = canonical(partValue(moves[0], part));
  return moves.every((move) => canonical(partValue(move, part)) === first) ? { value: partValue(moves[0], part) } : null;
}

/**
 * The moves the picks name in full: each part picked as the move has it, or not picked and the same in every move that
 * fits (for a part of several also none of its elements). One is sent; several differ only in a shape.
 */
export function settled(moves: Move[], parts: Part[], picks: PartPicks): Move[] {
  const left = fitting(moves, parts, picks);
  return left.filter((move) =>
    parts.every((part) => {
      const picked = picks[part.key] ?? [];
      const value = partValue(move, part);
      if (picked.length > 0) {
        return canonical(value) === canonical(part.many ? picked : picked[0]);
      }
      return shared(left, part) !== null || (part.many && (value as unknown[]).length === 0);
    }),
  );
}

export function sizes(moves: Move[], part: Part): { min: number; max: number } {
  const counts = moves.map((move) => (partValue(move, part) as unknown[]).length);
  return { min: Math.min(...counts), max: Math.max(...counts) };
}

// each value as the engine compares it, in its place: canonical() would take the tuple for a multiset
function tupleKey(values: unknown[]): string {
  return JSON.stringify(values.map((value) => canonical(value)));
}

function pickComponents(moves: Move[]): Set<string> {
  return new Set(moves.map((move) => move.pick?.component).filter((name): name is string => !!name));
}

function isList(type: TypeRef): boolean {
  return type.t === 'list' || type.t === 'set';
}

function uniqueValues(values: unknown[]): unknown[] {
  const seen = new Set<string>();
  return values.filter((value) => {
    const key = canonical(value);
    if (seen.has(key)) {
      return false;
    }
    seen.add(key);
    return true;
  });
}

// each element as often as one move holds it at most, in the order they come
function pool(moves: Move[], part: Part): unknown[] {
  const most = new Map<string, { element: unknown; count: number }>();
  for (const move of moves) {
    const counts = new Map<string, number>();
    for (const element of partValue(move, part) as unknown[]) {
      const key = canonical(element);
      const count = (counts.get(key) ?? 0) + 1;
      counts.set(key, count);
      const known = most.get(key);
      if (!known) {
        most.set(key, { element, count });
      } else if (count > known.count) {
        known.count = count;
      }
    }
  }
  return [...most.values()].flatMap(({ element, count }) => Array.from({ length: count }, () => element));
}

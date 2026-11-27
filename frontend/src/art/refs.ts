import { deckBackArt, deckCardArt, deckCards, enumArt, pileDeck, skillCardArt, type ArtRef } from './images';
import { recordNamed, type Component, type RecordType, type TypeRef } from '../wire/schema';
import type { FactProjection, Followup } from '../wire/types';

type Fields = Record<string, unknown>;

/**
 * The images a place shows for a value of a record instead, e.g. the answers show a player by his character: undefined
 * leaves the value to `recordArt`. A list asks for each of its elements.
 */
export type ValueArt = (value: unknown, component: Component, record: RecordType) => ArtRef[] | undefined;

const MAX_DEPTH = 4;

export function recordArt(value: Fields | null | undefined, recordName?: string, override?: ValueArt): ArtRef[] {
  return walk(value, recordName, override, 0);
}

function walk(value: Fields | null | undefined, recordName: string | undefined, override: ValueArt | undefined, depth: number): ArtRef[] {
  if (!value || depth > MAX_DEPTH) {
    return [];
  }
  const name = typeof value.kind === 'string' ? value.kind : recordName;
  const record = name ? recordNamed(name) : undefined;
  if (!record) {
    return [];
  }
  return unique(record.components.flatMap((component) => valueArt(value[component.name], component.type, value, depth, override, component, record)));
}

function valueArt(value: unknown, type: TypeRef, siblings: Fields, depth: number, override?: ValueArt, component?: Component, record?: RecordType): ArtRef[] {
  if (value === null || value === undefined) {
    return [];
  }
  // a list's elements and a nested record's components are asked one by one
  const own = override && component && record && !['list', 'set', 'ref'].includes(type.t) ? override(value, { ...component, type }, record) : undefined;
  if (own) {
    return own;
  }
  switch (type.t) {
    case 'enum': {
      const found = enumArt(type.name ?? '', value as string);
      return found ? [found] : [];
    }
    case 'list':
    case 'set':
      return Array.isArray(value) ? value.flatMap((element) => valueArt(element, type.of as TypeRef, siblings, depth, override, component, record)) : [];
    case 'cards': {
      // a list of cards takes its kind from the deck or the pile beside it
      const kind = deckCards(siblings.deck as string | undefined) ?? pileCards(siblings.pile as string | undefined);
      return Array.isArray(value) ? value.map((card) => deckCardArt(card, kind)).filter((found): found is ArtRef => found !== null) : [];
    }
    case 'ref': {
      if (type.name === 'SkillCard') {
        const found = skillCardArt(value as { type: string | null; value: number });
        return found ? [found] : [];
      }
      return typeof value === 'object' ? walk(value as Fields, type.name, override, depth + 1) : [];
    }
    default:
      return [];
  }
}

function pileCards(pile: string | undefined): string | null {
  return pile === 'SKILL' ? 'SkillCard' : deckCards(pileDeck(pile));
}

export function factArt(fact: FactProjection | null | undefined): ArtRef[] {
  if (!fact) {
    return [];
  }
  if (fact.kind === 'Hidden') {
    const back = deckBackArt(fact.deck ?? pileDeck(fact.pile));
    return back ? [back] : [];
  }
  return recordArt(fact.fact as unknown as Fields | null);
}

export function optionArt(option: Followup | null | undefined): ArtRef[] {
  if (!option) {
    return [];
  }
  switch (option.kind) {
    case 'Single': {
      const event = option.event as unknown as Fields | null;
      return event?.kind === 'PlayerDecisionEvent' ? [] : recordArt(event);
    }
    case 'AllOf':
      return unique((option.followups ?? []).flatMap((part) => optionArt(part)));
    case 'OneOf':
      return unique((option.options ?? []).flatMap((part) => optionArt(part)));
    default:
      return [];
  }
}

/**
 * The images every option of a choice names, shown once as what the choice is about, e.g. the Destination Cards drawn.
 * A Crisis Card stays on each option instead: the turn panel shows the card being resolved.
 */
export function choiceArt(options: (Followup | null)[]): ArtRef[] {
  const perOption = options.map((option) => optionArt(option));
  if (perOption.length < 2) {
    return [];
  }
  return perOption[0].filter((candidate) => !isCrisisCard(candidate) && perOption.every((refs) => refs.some((ref) => ref.key === candidate.key)));
}

function isCrisisCard(ref: ArtRef): boolean {
  return ref.key.startsWith('CrisisCard/') || ref.key.startsWith('SuperCrisisCard/');
}

export function unique(refs: ArtRef[]): ArtRef[] {
  const seen = new Set<string>();
  return refs.filter((ref) => !seen.has(ref.key) && seen.add(ref.key));
}

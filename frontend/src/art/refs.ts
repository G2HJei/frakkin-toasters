// The components an event, a fact or an option names, found through their types in the schema: the cards, characters,
// locations, ships and damage that have an image. The log and the input show them next to the words.
import { deckBackArt, deckCardArt, deckCards, enumArt, pileDeck, skillCardArt, type ArtRef } from './images';
import { recordNamed, type TypeRef } from '../wire/schema';
import type { FactProjection, Followup } from '../wire/types';

type Fields = Record<string, unknown>;

const MAX_DEPTH = 4;

/** The images a record names, the record known by its `kind` or by the given name. */
export function recordArt(value: Fields | null | undefined, recordName?: string, depth = 0): ArtRef[] {
  if (!value || depth > MAX_DEPTH) {
    return [];
  }
  const name = typeof value.kind === 'string' ? value.kind : recordName;
  const record = name ? recordNamed(name) : undefined;
  if (!record) {
    return [];
  }
  return unique(record.components.flatMap((component) => valueArt(value[component.name], component.type, value, depth)));
}

function valueArt(value: unknown, type: TypeRef, siblings: Fields, depth: number): ArtRef[] {
  if (value === null || value === undefined) {
    return [];
  }
  switch (type.t) {
    case 'enum': {
      const found = enumArt(type.name ?? '', value as string);
      return found ? [found] : [];
    }
    case 'list':
    case 'set':
      return Array.isArray(value) ? value.flatMap((element) => valueArt(element, type.of as TypeRef, siblings, depth)) : [];
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
      return typeof value === 'object' ? recordArt(value as Fields, type.name, depth + 1) : [];
    }
    default:
      return [];
  }
}

function pileCards(pile: string | undefined): string | null {
  return pile === 'SKILL' ? 'SkillCard' : deckCards(pileDeck(pile));
}

/** The images of a fact: what a shown fact names, or the back of the cards a hidden fact counts. */
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

/** The images an option of a choice names: its events, a decision's none. */
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
 * The images of a choice: those every option names, shown once as what the choice is about, and the others per option.
 */
export function choiceArt(options: (Followup | null)[]): { common: ArtRef[]; perOption: ArtRef[][] } {
  const perOption = options.map((option) => optionArt(option));
  if (perOption.length < 2) {
    return { common: [], perOption };
  }
  const common = perOption[0].filter((candidate) => perOption.every((refs) => refs.some((ref) => ref.key === candidate.key)));
  const commonKeys = new Set(common.map((ref) => ref.key));
  return { common, perOption: perOption.map((refs) => refs.filter((ref) => !commonKeys.has(ref.key))) };
}

export function unique(refs: ArtRef[]): ArtRef[] {
  const seen = new Set<string>();
  return refs.filter((ref) => !seen.has(ref.key) && seen.add(ref.key));
}

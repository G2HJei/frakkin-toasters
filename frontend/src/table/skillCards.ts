// The order a hand of Skill Cards lies in: by color, in the order of SkillCardColor (Politics, Leadership, Tactics,
// Piloting, Engineering, Treachery), then by value, highest first. The engine compares cards as a multiset, so the order
// is only how they look.
import { enumValues, skillCardColor } from '../wire/schema';
import type { SkillCard } from '../wire/types';

interface CardLike {
  value: number;
  type: string | null;
}

export function compareSkillCards(a: CardLike, b: CardLike): number {
  const colors = enumValues('SkillCardColor');
  const rank = (card: CardLike) => {
    const at = colors.indexOf(skillCardColor(card.type) ?? '');
    return at < 0 ? colors.length : at;
  };
  return rank(a) - rank(b) || b.value - a.value || (a.type ?? '').localeCompare(b.type ?? '');
}

export function sortSkillCards<T extends CardLike = SkillCard>(cards: readonly T[]): T[] {
  return [...cards].sort(compareSkillCards);
}

/** The positions of the cards in the order they lie in, so a picker shows them sorted and answers with the originals. */
export function sortedPositions(cards: readonly CardLike[]): number[] {
  return cards.map((_, position) => position).sort((a, b) => compareSkillCards(cards[a], cards[b]) || a - b);
}

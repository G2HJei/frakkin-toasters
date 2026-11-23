import { describe, expect, it } from 'vitest';
import { sortedPositions, sortSkillCards } from './skillCards';

const card = (type: string, value: number) => ({ type, value });

describe('a hand of Skill Cards', () => {
  it('lies by color, Politics to Treachery, then by value, highest first', () => {
    const hand = [card('REPAIR', 1), card('BAIT', 0), card('EXECUTIVE_ORDER', 2), card('CONSOLIDATE_POWER', 1), card('EXECUTIVE_ORDER', 4), card('EVASIVE_MANOEUVRES', 3)];

    expect(sortSkillCards(hand)).toEqual([
      card('CONSOLIDATE_POWER', 1),
      card('EXECUTIVE_ORDER', 4),
      card('EXECUTIVE_ORDER', 2),
      card('EVASIVE_MANOEUVRES', 3),
      card('REPAIR', 1),
      card('BAIT', 0),
    ]);
  });

  it('gives a picker the positions of the cards in that order', () => {
    expect(sortedPositions([card('BAIT', 0), card('RED_TAPE', 0), card('RED_TAPE', 2)])).toEqual([2, 1, 0]);
  });
});

import { describe, expect, it } from 'vitest';
import { rule, ruleFor, ruleHref, RULES } from './rules';
import type { InputProjection } from '../wire/types';

describe('the rules', () => {
  it('know a page for every article they list, in the order of the book', () => {
    expect(RULES.every((found) => found.page > 0)).toBe(true);
    const main = RULES.filter((found) => !found.appendix).map((found) => found.page);
    expect([...main].sort((a, b) => a - b)).toEqual(main);
    expect(ruleHref(rule('10.3')!)).toMatch(/Battlestar_Galactica_CR2-0.*\.pdf#page=25$/);
    expect(ruleHref(rule('A3.7')!)).toMatch(/Appendix.*\.pdf#page=17$/);
  });

  it('point an input at the article to read', () => {
    const decide = (action: string) => ({ kind: 'Decide', player: 1, action, constraints: [], answers: null, topic: null }) as InputProjection;

    expect(ruleFor(decide('MoveEvent'))?.article).toBe('8.3');
    expect(ruleFor(decide('PlayCardsIntoSkillCheckEvent'))?.article).toBe('10.3');
    expect(ruleFor(decide('ArrestOrderEvent'))?.article).toBe('4.4');
    const crisis = { kind: 'Choose', player: 1, options: [], answers: [], topic: { event: 'ResolveCrisisCardEvent', details: null } } as unknown as InputProjection;
    expect(ruleFor(crisis)?.article).toBe('8.5');
    expect(ruleFor({ kind: 'Waiting' } as InputProjection)).toBeNull();
  });
});

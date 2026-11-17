import { describe, expect, it } from 'vitest';
import { choiceArt, factArt, optionArt, recordArt } from './refs';
import type { FactProjection, Followup } from '../wire/types';

const keys = (refs: { key: string }[]) => refs.map((ref) => ref.key);
const single = (event: object) => ({ kind: 'Single', event }) as unknown as Followup;

describe('what a log entry or an option names', () => {
  it('finds the cards of a list by the deck beside it', () => {
    const fact = { kind: 'Shown', fact: { kind: 'DeckFact.CardsDrawn', deck: 'CRISIS', cards: ['WATER_SABOTAGED'] } } as unknown as FactProjection;

    expect(keys(factArt(fact))).toEqual(['CrisisCard/WATER_SABOTAGED']);
  });

  it('finds Skill Cards by the pile beside them, and a character', () => {
    const gained = { kind: 'Shown', fact: { kind: 'PlayerFact.CardsGained', playerNumber: 1, pile: 'SKILL', cards: [{ type: 'REPAIR', value: 2 }] } };
    const moved = { kind: 'Shown', fact: { kind: 'PlayerFact.CharacterMoved', character: 'SAUL_TIGH', to: 'BRIG' } };

    expect(keys(factArt(gained as unknown as FactProjection))).toEqual(['SkillCardType/REPAIR_2']);
    expect(keys(factArt(moved as unknown as FactProjection))).toEqual(['Character/player-token/SAUL_TIGH', 'Location/BRIG']);
  });

  it('shows the back of the cards a hidden fact counts', () => {
    const hidden = { kind: 'Hidden', fact: 'DeckFact.CardsDrawn', playerNumber: 2, deck: 'LOYALTY', count: 1 } as unknown as FactProjection;

    expect(keys(factArt(hidden))).toEqual(['DeckId/LOYALTY']);
  });

  it('finds the Skill Card an event names, and none for a decision', () => {
    expect(keys(recordArt({ kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: { type: 'REPAIR', value: 1 } }))).toEqual([
      'Location/ARMORY',
      'SkillCardType/REPAIR_1',
    ]);
    expect(optionArt(single({ kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'MoveEvent', constraints: [] }))).toEqual([]);
  });

  it('shows the card every option of a choice names once, and the rest per option', () => {
    const section = (side: string) => single({ kind: 'ChooseCrisisSectionEvent', playerNumber: 1, card: 'RIOTS_NO_JUMP', section: side });

    const { common, perOption } = choiceArt([section('TOP'), section('BOTTOM')]);

    expect(keys(common)).toEqual(['CrisisCard/RIOTS_NO_JUMP']);
    expect(perOption).toEqual([[], []]);
  });
});

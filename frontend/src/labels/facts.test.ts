import { describe, expect, it } from 'vitest';
import { partsText } from './describe';
import { factSentences, sentenceParts, type FactContext } from './facts';
import type { FactProjection } from '../wire/types';

const context: FactContext = {
  playerName: (playerNumber) => ['', 'William Adama', 'Laura Roslin'][playerNumber] ?? `Player ${playerNumber}`,
  playerOf: (character) => (character === 'WILLIAM_ADAMA' ? 1 : null),
};
const shown = (fact: object) => ({ kind: 'Shown', fact }) as unknown as FactProjection;
const hidden = (fact: string, fields: object) =>
  ({ kind: 'Hidden', fact, playerNumber: null, otherPlayerNumber: null, deck: null, pile: null, skillCheckPile: null, count: 0, ...fields }) as unknown as FactProjection;
const card = (type: string, value: number) => ({ type, value });
const words = (facts: FactProjection[], actor: number | null = null) => factSentences(facts, context).map((one) => partsText(sentenceParts(one, actor, context)));

describe('the facts of a scope', () => {
  it('tells a card drawn into a hand as a draw, and draws in a row as one', () => {
    const facts = [
      shown({ kind: 'DeckFact.CardsDrawn', deck: 'POLITICS', cards: [card('CONSOLIDATE_POWER', 2)] }),
      shown({ kind: 'PlayerFact.CardsGained', playerNumber: 1, pile: 'SKILL', cards: [card('CONSOLIDATE_POWER', 2)] }),
      shown({ kind: 'DeckFact.CardsDrawn', deck: 'TACTICS', cards: [card('LAUNCH_SCOUT', 1)] }),
      shown({ kind: 'PlayerFact.CardsGained', playerNumber: 1, pile: 'SKILL', cards: [card('LAUNCH_SCOUT', 1)] }),
    ];

    expect(words(facts)).toEqual(['William Adama draws Consolidate Power 2 and Launch Scout 1']);
    // under his name
    expect(words(facts, 1)).toEqual(['draws Consolidate Power 2 and Launch Scout 1']);
  });

  it('tells a card a hand loses to a discard pile as a discard, and a card played for its ability as a play', () => {
    const discard = [
      shown({ kind: 'PlayerFact.CardsLost', playerNumber: 2, pile: 'SKILL', cards: [card('REPAIR', 1), card('REPAIR', 2)] }),
      shown({ kind: 'DeckFact.CardsDiscarded', deck: 'ENGINEERING', cards: [card('REPAIR', 1)] }),
      shown({ kind: 'DeckFact.CardsDiscarded', deck: 'ENGINEERING', cards: [card('REPAIR', 2)] }),
    ];
    const play = [
      shown({ kind: 'PlayerFact.CardsLost', playerNumber: 2, pile: 'SKILL', cards: [card('CONSOLIDATE_POWER', 1)] }),
      shown({ kind: 'GameFact.SkillCardPlayed', playerNumber: 2, card: card('CONSOLIDATE_POWER', 1) }),
      shown({ kind: 'DeckFact.CardsDiscarded', deck: 'POLITICS', cards: [card('CONSOLIDATE_POWER', 1)] }),
    ];

    expect(words(discard)).toEqual(['Laura Roslin discards Repair 1 and Repair 2']);
    expect(words(play)).toEqual(['Laura Roslin plays Consolidate Power 1']);
  });

  it("sorts a skill check's cards into its piles and discards them in one sentence", () => {
    const removed = (value: number) => [
      shown({ kind: 'SkillCheckFact.CardRemoved', card: card('REPAIR', value), playerNumber: null }),
      shown({ kind: 'DeckFact.CardsDiscarded', deck: 'ENGINEERING', cards: [card('REPAIR', value)] }),
    ];
    const piles = [
      shown({ kind: 'SkillCheckFact.CardsDrawn', pile: 'CARDS', cards: [card('REPAIR', 1), card('BAIT', 0)] }),
      shown({ kind: 'SkillCheckFact.CardsPlaced', pile: 'MATCHING', cards: [card('REPAIR', 1)], onTop: false }),
      shown({ kind: 'SkillCheckFact.CardsPlaced', pile: 'NON_MATCHING', cards: [card('BAIT', 0)], onTop: false }),
      shown({ kind: 'SkillCheckFact.CardsPlaced', pile: 'MATCHING', cards: [card('REPAIR', 2)], onTop: false }),
    ];

    expect(words([...removed(1), ...removed(2)])).toEqual(['Repair 1 and Repair 2 are discarded']);
    expect(words(piles)).toEqual(['Matching: Repair 1 and Repair 2', 'Not matching: Bait 0']);
  });

  it('counts the cards the viewer may not see, by their kind', () => {
    const facts = [
      hidden('PlayerFact.CardsLost', { playerNumber: 2, pile: 'SKILL', count: 2 }),
      hidden('SkillCheckFact.CardsPlaced', { skillCheckPile: 'CARDS', count: 2 }),
    ];

    expect(words(facts)).toEqual(['Laura Roslin puts 2 Skill Cards into the skill check']);
    expect(words([hidden('DeckFact.CardsDrawn', { deck: 'LOYALTY', count: 1 }), hidden('PlayerFact.CardsGained', { playerNumber: 1, pile: 'LOYALTY', count: 1 })])).toEqual([
      'William Adama draws a Loyalty Card',
    ]);
  });

  it("names the colors of another player's draws by the decks, and none of the cards he gets otherwise", () => {
    const draw = (deck: string) => [hidden('DeckFact.CardsDrawn', { deck, count: 1 }), hidden('PlayerFact.CardsGained', { playerNumber: 2, pile: 'SKILL', count: 1 })];

    expect(words([...draw('POLITICS'), ...draw('POLITICS'), ...draw('TACTICS')])).toEqual(['Laura Roslin draws 3 Skill Cards (2 Politics, 1 Tactics)']);
    expect(words([...draw('DESTINY'), ...draw('DESTINY')])).toEqual(['Laura Roslin draws 2 Destiny cards']);
    expect(words([hidden('PlayerFact.CardsGained', { playerNumber: 2, pile: 'SKILL', count: 2 })])).toEqual(['Laura Roslin gets 2 Skill Cards']);
  });

  it('counts a card that comes three times or more', () => {
    const notCylon = Array.from({ length: 4 }, () => 'NOT_CYLON');

    expect(words([shown({ kind: 'DeckFact.CardsAddedOnTop', deck: 'LOYALTY', cards: [...notCylon, 'MUTINEER'] })])).toEqual([
      'You Are Not a Cylon ×4 and You Are the Mutineer go on top of the Loyalty deck',
    ]);
  });

  it('leaves out the bookkeeping, and names a character by his player', () => {
    const facts = [
      shown({ kind: 'GameFact.TurnLimitUsed', playerNumber: 1, limit: 'CAG_PILOT_ACTION' }),
      shown({ kind: 'GameFact.DieRollStarted' }),
      shown({ kind: 'DeckFact.CardsDrawn', deck: 'GALACTICA_DAMAGE', cards: ['ARMORY'] }),
      shown({ kind: 'BoardFact.LocationDamaged', location: 'ARMORY' }),
      shown({ kind: 'PlayerFact.CharacterMoved', character: 'WILLIAM_ADAMA', to: 'SICKBAY' }),
    ];

    expect(words(facts, 1)).toEqual(['The Armory is damaged', 'moves to Sickbay']);
  });

  it('shuffles every deck in one sentence', () => {
    const shuffled = (deck: string) => shown({ kind: 'DeckFact.DeckShuffled', deck, drawOrder: [] });

    expect(words([shuffled('POLITICS'), shuffled('CRISIS'), shuffled('MUTINY')])).toEqual(['The Politics, Crisis and Mutiny decks are shuffled']);
  });
});

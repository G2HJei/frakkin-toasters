import { describe, expect, it } from 'vitest';
import { constantLabel, factLabel, NO_CONTEXT, optionLabel, TEMPLATED_EVENTS, TEMPLATED_FACTS, typeLabel } from './describe';
import { humanizeConstant, humanizeType } from './humanize';
import { CONSTANT_LABELS, TYPE_LABELS } from './overrides';
import { enumValues, schema } from '../wire/schema';
import type { Followup } from '../wire/types';

describe('labels', () => {
  it('names every override after a constant or a type of the schema', () => {
    const unknown: string[] = [];
    for (const [enumName, constants] of Object.entries(CONSTANT_LABELS)) {
      for (const constant of Object.keys(constants)) {
        if (!enumValues(enumName).includes(constant)) {
          unknown.push(`${enumName}.${constant}`);
        }
      }
    }
    for (const name of [...Object.keys(TYPE_LABELS), ...TEMPLATED_EVENTS, ...TEMPLATED_FACTS]) {
      if (!schema.types[name]) {
        unknown.push(name);
      }
    }
    expect(unknown).toEqual([]);
  });

  it('falls back to the humanized constant and type', () => {
    expect(humanizeConstant('GALACTICA_SPACE_12_OCLOCK')).toBe("Galactica Space 12 o'clock");
    expect(humanizeConstant('FTL_CONTROL')).toBe('FTL Control');
    expect(humanizeType('ResolveCrisisCardEvent')).toBe('Resolve crisis card');
    expect(humanizeType('DeckFact.CardsDrawn')).toBe('Cards drawn');
    expect(constantLabel('Location', 'ARMORY')).toBe('Armory');
    expect(constantLabel('Character', 'KARA_STARBUCK_THRACE')).toBe('Kara "Starbuck" Thrace');
    expect(typeLabel('ActionEvent')).toBe('Take an action');
  });

  describe('the options of a choice', () => {
    const single = (event: object) => ({ kind: 'Single', event }) as unknown as Followup;

    it('says "Pass" for a NoOpEvent', () => {
      expect(optionLabel(single({ kind: 'NoOpEvent', playerNumber: 1 }), NO_CONTEXT)).toBe('Pass');
    });

    it('names the action a decision asks for', () => {
      const decision = single({ kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'MoveEvent', eventConstraints: [] });
      expect(optionLabel(decision, NO_CONTEXT)).toBe('Move');
    });

    it('joins the parts of an AllOf with "then"', () => {
      const all = {
        kind: 'AllOf',
        followups: [single({ kind: 'NoOpEvent', playerNumber: 1 }), single({ kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'DrawSkillCardsEvent', eventConstraints: [] })],
      } as unknown as Followup;
      expect(optionLabel(all, NO_CONTEXT)).toBe('Pass then Draw Skill Cards');
    });

    it('fills the template of an event with its fields', () => {
      const section = single({ kind: 'ChooseCrisisSectionEvent', playerNumber: 2, card: 'RIOTS_NO_JUMP', section: 'TOP' });
      expect(optionLabel(section, NO_CONTEXT)).toBe('Player 2 chooses the Top of Riots No Jump');
    });

    it('says which Quorum Card a player plays', () => {
      const event = single({ kind: 'EnactProductionQuotasEvent', playerNumber: 1 });
      expect(optionLabel(event, NO_CONTEXT)).toBe('Player 1 plays Enact Production Quotas');
    });

    it('names the event and its fields when it has no template', () => {
      const event = single({ kind: 'SendToBrigEvent', targetPlayerNumber: 3 });
      expect(optionLabel(event, NO_CONTEXT)).toBe('Send to brig (target player number Player 3)');
    });
  });

  it('words a hidden fact by its count and colors', () => {
    const hidden = { kind: 'Hidden', fact: 'PlayerFact.CardsGained', playerNumber: 2, otherPlayerNumber: null, deck: null, pile: 'SKILL', skillCheckPile: null, count: 2, colors: { POLITICS: 1, TACTICS: 1 } };
    expect(factLabel(hidden as never, NO_CONTEXT)).toBe('Player 2: Cards gained: 2 hidden (Skill) — 1 politics, 1 tactics');
  });
});

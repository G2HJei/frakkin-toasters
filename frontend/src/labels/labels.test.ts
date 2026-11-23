import { describe, expect, it } from 'vitest';
import {
  constantLabel,
  eventLabel,
  factLabel,
  fillTemplate,
  NO_CONTEXT,
  optionEventParts,
  optionLabel,
  partsText,
  TEMPLATED_EVENTS,
  TEMPLATED_FACTS,
  typeLabel,
  withoutSubject,
  type LabelContext,
} from './describe';
import { humanizeConstant, humanizeType } from './humanize';
import { CONSTANT_LABELS, TYPE_LABELS } from './overrides';
import { CHOSEN_TEMPLATES, EVENT_TEMPLATES, FACT_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES } from './templates';
import { enumValues, recordNamed, schema } from '../wire/schema';
import type { Followup } from '../wire/types';

const names: LabelContext = { playerName: (playerNumber) => ['', 'William Adama', 'Laura Roslin', 'Kara "Starbuck" Thrace'][playerNumber] ?? `Player ${playerNumber}` };

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
    for (const name of [...Object.keys(TYPE_LABELS), ...TEMPLATED_EVENTS, ...TEMPLATED_FACTS, ...Object.keys(CHOSEN_TEMPLATES), ...Object.keys(HIDDEN_TEMPLATES), ...Object.keys(OPTION_TEMPLATES)]) {
      if (!schema.types[name]) {
        unknown.push(name);
      }
    }
    expect(unknown).toEqual([]);
  });

  it('has a sentence for every event, so none is named by its type', () => {
    const events = Object.entries(schema.types).filter(([name, type]) => type.kind === 'record' && type.interfaces?.includes('Event') && name !== 'PlayerDecisionEvent');
    // a Quorum or a Mutiny Card's event without one is "… plays the card"
    const missing = events.filter(([name, type]) => !EVENT_TEMPLATES[name] && !(type.kind === 'record' && (type.quorumCard || type.mutinyCard))).map(([name]) => name);

    expect(missing).toEqual([]);
  });

  it('names only fields its record has in every template', () => {
    const wrong: string[] = [];
    const templates = [EVENT_TEMPLATES, CHOSEN_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES, FACT_TEMPLATES];
    for (const all of templates) {
      for (const [name, template] of Object.entries(all)) {
        const components = recordNamed(name)?.components.map((component) => component.name) ?? [];
        for (const match of template.matchAll(/\{(\$?\w+)/g)) {
          if (match[1] !== '$card' && !components.includes(match[1])) {
            wrong.push(`${name}.${match[1]}`);
          }
        }
      }
    }
    expect(wrong).toEqual([]);
  });

  it('starts the sentence of an event about a player with him, and its hidden words too', () => {
    const notFirst = Object.entries(HIDDEN_TEMPLATES).filter(([, template]) => !template.startsWith('{playerNumber} ')).map(([name]) => name);

    expect(notFirst).toEqual([]);
  });

  it('shows no version of a character sheet', () => {
    const versions = enumValues('Character').filter((character) => /alternate|\balt\b/i.test(constantLabel('Character', character)));

    expect(versions).toEqual([]);
  });

  it('falls back to the humanized constant and type', () => {
    expect(humanizeConstant('GALACTICA_SPACE_12_OCLOCK')).toBe("Galactica Space 12 o'clock");
    expect(humanizeConstant('FTL_CONTROL')).toBe('FTL Control');
    expect(humanizeType('ResolveCrisisCardEvent')).toBe('Resolve crisis card');
    expect(humanizeType('DeckFact.CardsDrawn')).toBe('Cards drawn');
    expect(constantLabel('Location', 'ARMORY')).toBe('Armory');
    expect(constantLabel('Character', 'KARA_STARBUCK_THRACE')).toBe('Kara "Starbuck" Thrace');
    expect(constantLabel('Character', 'KARL_HELO_AGATHON_ALT')).toBe('Karl "Helo" Agathon');
    expect(typeLabel('ActionEvent')).toBe('Take an action');
  });

  describe('a template', () => {
    const fill = (template: string, value: Record<string, unknown>, record: string) => partsText(fillTemplate(template, value, record, names));

    it('puts "the" in front of a location that takes one', () => {
      expect(eventLabel({ kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null }, names)).toBe('William Adama moves to the Armory');
      expect(eventLabel({ kind: 'MoveEvent', playerNumber: 1, destination: 'SICKBAY', discardCard: null }, names)).toBe('William Adama moves to Sickbay');
      expect(factLabel({ kind: 'Shown', fact: { kind: 'BoardFact.LocationDamaged', location: 'ARMORY' } } as never, names)).toBe('The Armory is damaged');
    });

    it('leaves out an optional part without its field, and takes the other one', () => {
      expect(eventLabel({ kind: 'MoveEvent', playerNumber: 1, destination: 'COMMAND', discardCard: { type: 'REPAIR', value: 1 } }, names)).toBe('William Adama moves to Command, discarding Repair 1');
      expect(eventLabel({ kind: 'PlayInterruptCardEvent', playerNumber: 2, card: null, calculationsModifier: 0 }, names)).toBe('Laura Roslin plays no interrupt card');
    });

    it('counts, picks the words of a value and joins a list', () => {
      expect(eventLabel({ kind: 'DrawSkillCardsOfColorEvent', playerNumber: 1, color: 'TREACHERY', count: 1 }, names)).toBe('William Adama draws 1 Treachery card');
      expect(eventLabel({ kind: 'DrawSkillCardsEvent', playerNumber: 1, selection: { POLITICS: 1, TACTICS: 2 }, drawLimit: null }, names)).toBe('William Adama draws 1 Politics and 2 Tactics cards');
      expect(fill('{result=PASS:passes|*:fails}', { result: 'FAIL' }, 'SkillCheckFact.ResultDetermined')).toBe('fails');
      expect(eventLabel({ kind: 'DiscardSkillCardsEvent', playerNumber: 2, cards: [{ type: 'REPAIR', value: 1 }, { type: 'REPAIR', value: 2 }, { type: 'LAUNCH_SCOUT', value: 1 }], discardLimit: null }, names)).toBe(
        'Laura Roslin discards Repair 1, Repair 2 and Launch Scout 1',
      );
    });

    it('names the card a Quorum Card event plays', () => {
      expect(eventLabel({ kind: 'ArrestOrderEvent', playerNumber: 2, targetPlayerNumber: 3 }, names)).toBe('Laura Roslin plays Arrest Order on Kara "Starbuck" Thrace');
    });
  });

  it('drops the subject of a line under his name', () => {
    const parts = [{ text: 'William Adama', art: null }, { text: ' moves to Sickbay' }];

    expect(partsText(withoutSubject(parts, 'William Adama') ?? [])).toBe('moves to Sickbay');
    expect(withoutSubject(parts, 'Laura Roslin')).toBeNull();
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
      const grant = (grant: string) => single({ kind: 'GrantEvent', playerNumber: 2, grant, grantedBy: 'EXECUTIVE_ORDER' });
      const all = { kind: 'AllOf', followups: [grant('MOVE'), grant('ACTION')] } as unknown as Followup;
      expect(optionLabel(all, NO_CONTEXT)).toBe('Move, then take an action');
    });

    it('words an answer as an order to the player it names', () => {
      const section = { kind: 'ChooseCrisisSectionEvent', playerNumber: 2, card: 'RIOTS_NO_JUMP', section: 'TOP' };
      expect(optionLabel(single(section), NO_CONTEXT)).toBe('Choose the top option of Riots No Jump');
      expect(partsText(optionEventParts({ kind: 'BrigEvent', playerNumber: 1 }, names))).toBe('Try to leave the Brig');
      expect(optionLabel(single({ kind: 'EnactProductionQuotasEvent', playerNumber: 1 }), NO_CONTEXT)).toBe('Play Enact Production Quotas');
    });

    it('keeps the sentence of an event about nobody', () => {
      expect(optionLabel(single({ kind: 'SendToBrigEvent', targetPlayerNumber: 3 }), names)).toBe('Kara "Starbuck" Thrace goes to the Brig');
    });
  });

  it('words a hidden fact by who, how many and their colors', () => {
    const hidden = { kind: 'Hidden', fact: 'PlayerFact.CardsGained', playerNumber: 2, otherPlayerNumber: null, deck: null, pile: 'SKILL', skillCheckPile: null, count: 2, colors: { POLITICS: 1, TACTICS: 1 } };
    const loyalty = { ...hidden, fact: 'PlayerFact.LoyaltyCardSeen', otherPlayerNumber: 1, pile: 'LOYALTY', count: 1, colors: null };

    expect(factLabel(hidden as never, names)).toBe('Laura Roslin gets 2 Skill Cards (1 Politics, 1 Tactics)');
    expect(factLabel(loyalty as never, names)).toBe('Laura Roslin looks at a Loyalty Card of William Adama');
  });
});

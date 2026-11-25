import { describe, expect, it } from 'vitest';
import {
  constantLabel,
  eventLabel,
  eventParts,
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
  type Part,
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

  it('names only fields its record has in every template, and constants that exist', () => {
    const wrong: string[] = [];
    const templates = [EVENT_TEMPLATES, CHOSEN_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES, FACT_TEMPLATES];
    // the tokens have icons but no enum, and a holder holds one of the three titles
    const pseudo: Record<string, string[]> = { Token: ['MIRACLE', 'NUKE', 'CENTURION'], Holder: ['PRESIDENT', 'ADMIRAL', 'CAG'] };
    for (const all of templates) {
      for (const [name, template] of Object.entries(all)) {
        const components = recordNamed(name)?.components.map((component) => component.name) ?? [];
        for (const match of template.matchAll(/\{!?(\$?\w+)(?::(\w+))?/g)) {
          const [, field, constant] = match;
          const known = constant ? (pseudo[field] ?? enumValues(field)).includes(constant) : field === '$card' || components.includes(field);
          if (!known) {
            wrong.push(`${name}.${field}${constant ? `:${constant}` : ''}`);
          }
        }
      }
    }
    expect(wrong).toEqual([]);
  });

  // The constants of an enum the log's templates spell out in their words instead of naming them, as "Template: Name".
  function spelledOut(enumName: string, constants: string[]): string[] {
    const labels = constants.map((constant) => constantLabel(enumName, constant));
    const escaped = (text: string) => text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return [EVENT_TEMPLATES, CHOSEN_TEMPLATES, HIDDEN_TEMPLATES, FACT_TEMPLATES].flatMap((templates) =>
      Object.entries(templates).flatMap(([name, template]) => {
        // a field names its value already, but the words a field picks are the template's own: {pass?passes:fails},
        // {shipType=RAIDER:raiders}; and the Cylon Fleet board is a board
        const words = template
          .replace(/\{\w+\?([^:}]*):([^}]*)\}/g, ' $1 $2 ')
          .replace(/\{\w+=([^}]*)\}/g, (_, cases: string) => ` ${cases.split('|').map((entry) => entry.split(':')[1] ?? '').join(' ')} `)
          .replace(/\{[^}]*\}/g, '')
          .replace(/Cylon Fleet board/g, '');
        return labels.filter((label) => new RegExp(`\\b${escaped(label)}\\b`).test(words)).map((label) => `${name}: ${label}`);
      }),
    );
  }

  it('names every location the log mentions, so it shows its card on hover', () => {
    expect(spelledOut('Location', enumValues('Location').filter((location) => !location.includes('SPACE')))).toEqual([]);
  });

  it('names every type of Skill Cards the log mentions, so it shows in its color', () => {
    expect(spelledOut('SkillCardColor', enumValues('SkillCardColor'))).toEqual([]);
  });

  it('names every Quorum Card the log mentions, so it shows the card on hover', () => {
    expect(spelledOut('QuorumCard', enumValues('QuorumCard'))).toEqual([]);
  });

  it('names every title the log mentions, by its holder or by its insignia where it changes hands', () => {
    expect(spelledOut('Title', ['PRESIDENT', 'ADMIRAL', 'CAG'])).toEqual([]);
  });

  it('writes CAG in capitals in every name and sentence', () => {
    const enums = Object.entries(schema.types).flatMap(([name, type]) => (type.kind === 'enum' ? type.values.map((value) => constantLabel(name, value)) : []));
    const types = Object.keys(schema.types).map(typeLabel);
    const templates = [EVENT_TEMPLATES, CHOSEN_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES, FACT_TEMPLATES].flatMap((all) => Object.values(all).map((template) => template.replace(/\{[^}]*\}/g, '')));
    const lowered = fillTemplate('{ability|lower}', { ability: 'CAG_PLACES_CIVILIAN_SHIP' }, 'BasestarBridgeAbilityEvent', names);

    expect([...enums, ...types, ...templates, partsText(lowered)].filter((words) => /\bcag\b/i.test(words) && !/\bCAG\b/.test(words))).toEqual([]);
  });

  it('makes only the first letter of a value small inside a sentence, unless it starts with an acronym', () => {
    const lower = (ability: string) => partsText(fillTemplate('{playerNumber} chooses {ability|lower}', { playerNumber: 1, ability }, 'BasestarBridgeAbilityEvent', names));

    expect(lower('CAG_PLACES_CIVILIAN_SHIP')).toBe('William Adama chooses a civilian ship for the CAG');
    expect(lower('GALACTICA_DAMAGE')).toBe('William Adama chooses damage to Galactica');
    expect(partsText(fillTemplate('{resource|lower}', { resource: 'FOOD' }, 'LoseResourceEvent', names))).toBe('food');
  });

  it('counts the random Skill Cards a player discards, reddens an execution and puts the insignia before the CAG title', () => {
    const icons: LabelContext = { ...names, iconOf: (enumName, constant) => (enumName === 'Title' ? { key: `TitleIcon/${String(constant)}`, url: '', shape: 'title' } : null) };
    const executed = eventParts({ kind: 'ExecuteEvent', playerNumber: 1, withoutMoraleLoss: false }, names);
    const activation = { launchShipType: 'VIPER', launchLocation: 'GALACTICA_SPACE_8_OCLOCK', humanFighterId: null, moveLocation: null, attackCylonShipId: null, escortCivilianShipId: null };
    const passed = eventParts({ kind: 'PassCagTitleEvent', playerNumber: 3, activation, targetPlayerNumber: 2 }, icons);

    expect(eventLabel({ kind: 'DiscardRandomSkillCardsEvent', playerNumber: 2, count: 2 }, names)).toBe('Laura Roslin discards 2 random Skill Cards');
    expect(eventLabel({ kind: 'DiscardRandomSkillCardsEvent', playerNumber: 2, count: 1 }, names)).toBe('Laura Roslin discards 1 random Skill Card');
    expect(partsText(executed)).toBe('William Adama is executed');
    expect(executed.filter((part) => part.tone === 'bad').map((part) => part.text)).toEqual(['executed']);
    expect(partsText(passed)).toBe('Kara "Starbuck" Thrace launches a Viper to Space 8 o\'clock and passes the CAG title to Laura Roslin');
    expect(passed.find((part) => part.text === 'CAG')?.icon?.key).toBe('TitleIcon/CAG');
  });

  it("puts a ship's icon in front of the words that name its kind", () => {
    const icons: LabelContext = { ...names, iconOf: (enumName, constant) => (enumName === 'ShipType' ? { key: `ShipType/${String(constant)}`, url: '', shape: 'ship' } : null) };
    const raiders = eventParts({ kind: 'BasestarBridgeShipsEvent', playerNumber: 4, shipType: 'RAIDER', location: 'GALACTICA_SPACE_8_OCLOCK' }, icons);
    const basestar = eventParts({ kind: 'BasestarBridgeShipsEvent', playerNumber: 4, shipType: 'BASESTAR', location: 'GALACTICA_SPACE_8_OCLOCK' }, icons);
    const civilian = eventParts({ kind: 'PlaceCivilianShipEvent', playerNumber: 3, location: 'GALACTICA_SPACE_4_OCLOCK' }, icons);
    // the words after each icon
    const iconed = (parts: Part[]) => parts.flatMap((part, index) => (part.icon ? [`${part.icon.key} ${parts[index + 1]?.text ?? ''}`] : []));

    expect(partsText(raiders)).toBe("Player 4 places 3 raiders at Space 8 o'clock");
    expect(iconed(raiders)).toEqual(["ShipType/RAIDER raiders at Space 8 o'clock"]);
    expect(partsText(basestar)).toBe("Player 4 places a basestar at Space 8 o'clock");
    expect(iconed(basestar)).toEqual(["ShipType/BASESTAR basestar at Space 8 o'clock"]);
    expect(partsText(civilian)).toBe('Kara "Starbuck" Thrace places a civilian ship at Space 4 o\'clock');
    expect(iconed(civilian)).toEqual(["ShipType/CIVILIAN civilian ship at Space 4 o'clock"]);
  });

  it('names a Quorum Card its template names, so the log shows it on hover', () => {
    const cards: LabelContext = { ...names, artOf: (value) => ({ key: `QuorumCard/${String(value)}`, url: '', label: String(value), shape: 'card' }) };
    const arbitrator = eventParts({ kind: 'UseArbitratorEvent', playerNumber: 1, adjustment: -3 }, cards);
    const defends = (discardAcceptProphecy: boolean) => eventParts({ kind: 'DefendPresidencyEvent', playerNumber: 2, discardAcceptProphecy }, cards);
    const named = (parts: Part[]) => parts.filter((part) => part.art).map((part) => part.text);

    expect(partsText(arbitrator)).toBe('William Adama uses Assign Arbitrator to change the difficulty by -3');
    expect(named(arbitrator)).toEqual(['Assign Arbitrator']);
    expect(partsText(defends(true))).toBe('Laura Roslin discards Accept Prophecy to stay President');
    expect(partsText(defends(false))).toBe('Laura Roslin gives up the presidency rather than discard Accept Prophecy');
    expect(named(defends(false))).toEqual(['Accept Prophecy']);
  });

  it('counts the ships of a kind, each kind with its icon', () => {
    const icons: LabelContext = { ...names, iconOf: (enumName, constant) => (enumName === 'ShipType' ? { key: `ShipType/${String(constant)}`, url: '', shape: 'ship' } : null) };
    const launches = (shipTypes: string[]) => eventParts({ kind: 'LaunchReservesEvent', playerNumber: 3, card: { type: 'LAUNCH_RESERVES', value: 5 }, area: 'GALACTICA_SPACE_8_OCLOCK', shipTypes }, icons);

    expect(partsText(launches(['VIPER', 'VIPER']))).toBe('Kara "Starbuck" Thrace plays Launch Reserves 5 and launches 2 vipers to Space 8 o\'clock');
    expect(launches(['VIPER', 'VIPER']).filter((part) => part.icon).map((part) => `${part.icon?.key} ${part.text}`)).toEqual(['ShipType/VIPER vipers']);
    expect(partsText(launches(['VIPER']))).toBe('Kara "Starbuck" Thrace plays Launch Reserves 5 and launches a viper to Space 8 o\'clock');
    expect(partsText(launches(['VIPER', 'ASSAULT_RAPTOR']))).toBe('Kara "Starbuck" Thrace plays Launch Reserves 5 and launches a viper and an assault raptor to Space 8 o\'clock');
  });

  describe('a title', () => {
    const log: LabelContext = {
      ...names,
      characterOf: (playerNumber) => ['', 'WILLIAM_ADAMA', 'LAURA_ROSLIN', 'KARA_STARBUCK_THRACE'][playerNumber] ?? null,
      titleHolder: (title) => ({ PRESIDENT: 2, ADMIRAL: 1, CAG: 3 })[title] ?? null,
      // as the log's: characters and titles have images and icons
      artOf: (value, type) => (type.name === 'Character' || type.name === 'Title' ? { key: `art:${String(value)}`, url: '', label: String(value), shape: 'card' } : null),
      iconOf: (enumName, constant) => (enumName === 'Character' || enumName === 'Title' ? { key: `${enumName}:${String(constant)}`, url: '', shape: 'icon' } : null),
    };
    // each part with an icon after the subject: its icon, its words and its image on hover
    const iconed = (parts: Part[]) =>
      parts
        .slice(1)
        .filter((part) => part.icon)
        .map((part) => [part.icon?.key, part.text, part.art?.key ?? null]);

    it("shows its holder's portrait, with the title's card on hover", () => {
      const parts = eventParts({ kind: 'PopularInfluenceChoiceEvent', playerNumber: 1, given: 'ARREST_ORDER', other: 'RESIGNATION' }, log);

      expect(partsText(parts)).toBe('William Adama gives Arrest Order to the President and keeps Resignation');
      expect(iconed(parts)).toEqual([['Character:LAURA_ROSLIN', 'President', 'art:PRESIDENT']]);
      expect(partsText(eventParts({ kind: 'PopularInfluenceChoiceEvent', playerNumber: 1, given: 'ARREST_ORDER', other: 'RESIGNATION' }, names))).toBe(
        'William Adama gives Arrest Order to the President and keeps Resignation',
      );
    });

    it('shows its insignia where it changes hands, and its holder where he is only named', () => {
      const martialLaw = eventParts({ kind: 'DeclareMartialLawEvent', playerNumber: 1 }, log);

      // "The next President …": its first part is no subject
      expect(iconed([{ text: '' }, ...eventParts({ kind: 'DetermineNextPresidentEvent' }, log)])).toEqual([['Title:PRESIDENT', 'President', 'art:PRESIDENT']]);
      expect(partsText(martialLaw)).toBe('William Adama makes the Admiral President');
      expect(iconed(martialLaw)).toEqual([
        ['Character:WILLIAM_ADAMA', 'Admiral', 'art:ADMIRAL'],
        ['Title:PRESIDENT', 'President', 'art:PRESIDENT'],
      ]);
    });

    it('shows the holder a label names', () => {
      const chooses = eventParts({ kind: 'BasestarBridgeAbilityEvent', playerNumber: 4, ability: 'CAG_PLACES_CIVILIAN_SHIP' }, log);

      expect(partsText(chooses)).toBe('Player 4 chooses a civilian ship for the CAG');
      expect(iconed(chooses)).toEqual([['Character:KARA_STARBUCK_THRACE', 'CAG', 'art:CAG']]);
    });
  });

  it('counts the repaired ships of a type, and names a ship alone of its type', () => {
    const types: LabelContext = { ...names, shipName: (id) => (id > 70 ? `Viper Mk VII #${id}` : `Viper #${id}`), shipType: (id) => (id > 70 ? 'VIPER_MARK_VII' : 'VIPER') };
    const repairs = (viperIds: number[]) => eventLabel({ kind: 'RepairEvent', playerNumber: 1, card: { type: 'REPAIR', value: 1 }, viperIds }, types);

    expect(repairs([1, 2])).toBe('William Adama plays Repair 1 and repairs 2 vipers');
    expect(repairs([1])).toBe('William Adama plays Repair 1 and repairs Viper #1');
    expect(repairs([1, 71])).toBe('William Adama plays Repair 1 and repairs Viper #1 and Viper Mk VII #71');
    expect(repairs([])).toBe('William Adama plays Repair 1 and repairs a location');
    expect(eventLabel({ kind: 'ResourcesForGalacticaEvent', playerNumber: 2, location: null, viperIds: [3, 4] }, types)).toBe('Laura Roslin plays Resources for Galactica and 2 vipers');
  });

  it("puts the ship a Raptor Specialist builds or returns in front of its words", () => {
    const icons: LabelContext = { ...names, iconOf: (enumName, constant) => (enumName === 'Choice' ? { key: `Choice:${String(constant)}`, url: '', shape: 'ship' } : null) };
    const plays = (choice: string) => eventParts({ kind: 'RaptorSpecialistEvent', playerNumber: 3, card: { type: 'RAPTOR_SPECIALIST', value: 3 }, choice }, icons);

    // the icon, then the words it stands in front of
    const iconed = (parts: Part[]) => parts.flatMap((part, index) => (part.icon ? [`${part.icon.key} ${parts[index + 1]?.text ?? ''}`] : []));

    expect(partsText(plays('BUILD_ASSAULT_RAPTOR'))).toBe('Kara "Starbuck" Thrace plays Raptor Specialist 3 to build an assault raptor');
    expect(iconed(plays('BUILD_ASSAULT_RAPTOR'))).toEqual(['Choice:BUILD_ASSAULT_RAPTOR assault raptor']);
    expect(iconed(plays('RETURN_RAPTOR'))).toEqual(['Choice:RETURN_RAPTOR raptor']);
    expect(partsText(plays('RETURN_RAPTOR'))).toBe('Kara "Starbuck" Thrace plays Raptor Specialist 3 to return a destroyed raptor');
  });

  it('words the damage a player chooses for Galactica, the locations as names and the damage in red', () => {
    const cards: LabelContext = { ...names, artOf: (value) => ({ key: `Location/${String(value)}`, url: '', label: String(value), shape: 'location' }) };
    const chooses = (resolved: string[]) => eventParts({ kind: 'ChooseGalacticaDamageTokensEvent', playerNumber: 2, resolved, returned: ['WEAPONS_CONTROL'] }, cards);
    const bad = (parts: Part[]) => parts.filter((part) => part.tone === 'bad').map((part) => part.text);

    expect(partsText(chooses(['ARMORY', 'COMMAND']))).toBe('Laura Roslin chooses to damage the Armory and Command');
    expect(bad(chooses(['ARMORY', 'COMMAND']))).toEqual(['damage']);
    expect(chooses(['ARMORY', 'COMMAND']).filter((part) => part.art).map((part) => part.text)).toEqual(['Armory', 'Command']);
    expect(partsText(chooses(['ARMORY', 'FUEL']))).toBe('Laura Roslin chooses to damage the Armory and lose 1 fuel');
    expect(partsText(chooses(['FUEL', 'FOOD']))).toBe('Laura Roslin chooses to lose 1 fuel and 1 food');
    expect(bad(chooses(['FUEL', 'FOOD'])).join('')).toBe('1 fuel1 food');
    expect(eventLabel({ kind: 'ChooseGalacticaDamageEvent', playerNumber: 2, resolved: 'ADMIRALS_QUARTERS', returned: 'FOOD' }, names)).toBe("Laura Roslin chooses to damage the Admiral's Quarters");
  });

  it('names a location a template names, with "the" where it takes one', () => {
    const cards: LabelContext = { ...names, artOf: (value) => ({ key: `Location/${String(value)}`, url: '', label: String(value), shape: 'location' }) };
    const parts = eventParts({ kind: 'LeaveBrigEvent', playerNumber: 1, destination: 'SICKBAY' }, cards);

    expect(partsText(parts)).toBe('William Adama leaves the Brig for Sickbay');
    expect(parts.filter((part) => part.art).map((part) => part.text)).toEqual(['Brig', 'Sickbay']);
  });

  it('names a player by his portrait and his sheet where the context knows his character', () => {
    const log: LabelContext = {
      ...names,
      characterOf: (playerNumber) => ['', 'WILLIAM_ADAMA', 'LAURA_ROSLIN'][playerNumber] ?? null,
      artOf: (value) => ({ key: `sheet:${String(value)}`, url: '', label: String(value), shape: 'token' }),
      iconOf: (enumName, constant) => (enumName === 'Character' ? { key: `portrait:${String(constant)}`, url: '', shape: 'portrait' } : null),
    };
    const leaves = { kind: 'MoveFromBrigEvent', targetPlayerNumber: 2, destination: 'ARMORY' };

    expect(eventParts(leaves, log)[0]).toMatchObject({ text: 'Laura Roslin', player: 2, art: { key: 'sheet:LAURA_ROSLIN' }, icon: { key: 'portrait:LAURA_ROSLIN' } });
    // the answers and the pause line know no characters: his name alone, which keeps its player
    expect(eventParts(leaves, names)[0]).toEqual({ text: 'Laura Roslin', player: 2 });
    expect(partsText(eventParts(leaves, names))).toBe('Laura Roslin leaves the Brig for the Armory');
  });

  it('opens and closes every tone marker of a template', () => {
    const unbalanced = [EVENT_TEMPLATES, CHOSEN_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES, FACT_TEMPLATES]
      .flatMap((all) => Object.entries(all))
      .filter(([, template]) => (template.match(/<[+-]/g) ?? []).length !== (template.match(/>/g) ?? []).length)
      .map(([name]) => name);

    expect(unbalanced).toEqual([]);
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

    describe('with icons and tones', () => {
      const icons: LabelContext = {
        ...names,
        iconOf: (enumName, constant) => ({ key: `${enumName}/${String(constant)}`, url: '', shape: 'icon' }),
        shipType: (id) => (id === 1016 ? 'RAIDER' : null),
        shipName: (id) => (id === 1016 ? 'Raider #1016' : `ship #${id}`),
      };
      // each part's words, icon and tone
      const parts = (template: string, value: Record<string, unknown>, record: string) =>
        fillTemplate(template, value, record, icons).map((part) => [part.text, part.icon?.key ?? null, part.tone ?? null]);

      it('tones the words between the markers, the fields inside too, and drops the markers', () => {
        expect(parts(FACT_TEMPLATES['BoardFact.ResourceDecreased'], { resource: 'FOOD', amount: 1 }, 'BoardFact.ResourceDecreased')).toEqual([
          ['The fleet loses ', null, null],
          ['1 ', null, 'bad'],
          ['food', 'Resource/FOOD', 'bad'],
        ]);
        expect(parts(FACT_TEMPLATES['BoardFact.ResourceIncreased'], { resource: 'MORALE', amount: 2 }, 'BoardFact.ResourceIncreased').at(-1)).toEqual(['morale', 'Resource/MORALE', 'good']);
      });

      it("tones a skill check's result by its words", () => {
        const result = (value: string) => parts(FACT_TEMPLATES['SkillCheckFact.ResultDetermined'], { result: value }, 'SkillCheckFact.ResultDetermined').at(-1);

        expect(result('PASS')).toEqual(['passes', null, 'good']);
        expect(result('PARTIAL_PASS')).toEqual(['partially passes', null, 'good']);
        expect(result('FAIL')).toEqual(['fails', null, 'bad']);
        expect(parts(EVENT_TEMPLATES.MysteriousIntuitionEvent, { playerNumber: 1, pass: false }, 'MysteriousIntuitionEvent').at(-1)).toEqual(['fail', null, 'bad']);
      });

      it('puts an icon in front of a constant it names, a ship, a resource, and the die', () => {
        expect(parts(EVENT_TEMPLATES.PlaceRaidersBehindEvent, { count: 3 }, 'PlaceRaidersBehindEvent')).toEqual([
          ['3 ', null, null],
          ['', 'ShipType/RAIDER', null],
          ['raiders appear behind Galactica', null, null],
        ]);
        expect(parts(EVENT_TEMPLATES.NoOneGetsLeftBehindEvent, { playerNumber: 1 }, 'NoOneGetsLeftBehindEvent').filter(([, icon]) => icon)).toEqual([
          ['fuel', 'Resource/FUEL', 'bad'],
          ['population', 'Resource/POPULATION', 'good'],
        ]);
        expect(parts(EVENT_TEMPLATES.ActivateRaiderEvent, { raiderShipId: 1016 }, 'ActivateRaiderEvent')[0]).toEqual(['Raider #1016', 'ShipType/RAIDER', null]);
        expect(parts(FACT_TEMPLATES['GameFact.DieRolled'], { value: 5 }, 'GameFact.DieRolled')).toEqual([
          ['The die shows ', null, null],
          ['5', 'Die/5', null],
        ]);
        expect(parts(FACT_TEMPLATES['TitleFact.CagAssigned'], { character: 'LEE_APOLLO_ADAMA' }, 'TitleFact.CagAssigned').at(-1)).toEqual(['CAG', 'Title/CAG', null]);
      });

      it('words the same without icons', () => {
        expect(fill(EVENT_TEMPLATES.PlaceRaidersBehindEvent, { count: 1 }, 'PlaceRaidersBehindEvent')).toBe('1 raider appears behind Galactica');
        expect(fill(FACT_TEMPLATES['SkillCheckFact.ResultDetermined'], { result: 'PARTIAL_PASS' }, 'SkillCheckFact.ResultDetermined')).toBe('The skill check partially passes');
      });
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

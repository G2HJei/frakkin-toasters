import { describe, expect, it } from 'vitest';
import {
  artPaths,
  CARD_ENUMS,
  characterArt,
  deckBackArt,
  enumArt,
  locationArt,
  resourceArt,
  skillCardArt,
  skillCheckArt,
  skillTypeArt,
  trackArt,
} from './images';
import { isSpace } from '../table/board/layout';
import { enumValues } from '../wire/schema';

// The folders of materials-sorted named after an enum, with the constants that have no image there.
const ENUM_FOLDERS: Record<string, { enumName: string; without: string[] }> = {
  BasestarDamage: { enumName: 'BasestarDamage', without: [] },
  GalacticaDamage: { enumName: 'GalacticaDamage', without: [] },
  PegasusDamage: { enumName: 'PegasusDamage', without: [] },
  ShipType: { enumName: 'ShipType', without: [] },
  JumpPreparation: { enumName: 'JumpPreparation', without: [] },
  // the token never stays on the last space: the Cylons attack and it goes back to the start
  Pursuit: { enumName: 'Pursuit', without: ['AUTO_ATTACK'] },
  'Character/card': { enumName: 'Character', without: [] },
  'Character/player-token': { enumName: 'Character', without: [] },
  ...Object.fromEntries(CARD_ENUMS.map((enumName) => [enumName, { enumName, without: [] }])),
};

const folderOf = (path: string) => path.substring(0, path.lastIndexOf('/'));
const nameOf = (path: string) => path.substring(path.lastIndexOf('/') + 1);

describe('the images', () => {
  it('has an image for every constant of the enums with images', () => {
    const missing: string[] = [];
    for (const [folder, { enumName, without }] of Object.entries(ENUM_FOLDERS)) {
      for (const constant of enumValues(enumName)) {
        if (!without.includes(constant) && !artPaths().includes(`${folder}/${constant}`)) {
          missing.push(`${folder}/${constant}`);
        }
      }
    }
    expect(missing).toEqual([]);
  });

  it('names every image after a constant of the schema', () => {
    const named = (path: string): boolean => {
      const folder = folderOf(path);
      const name = nameOf(path);
      if (ENUM_FOLDERS[folder]) {
        return enumValues(ENUM_FOLDERS[folder].enumName).includes(name);
      }
      switch (folder) {
        case 'Character/piloting-token':
          return enumValues('Character').includes(name);
        case 'SkillCardType':
          return enumValues('SkillCardType').includes(name.replace(/_\d+$/, ''));
        case 'Resource':
          return /_(\d|1[0-5])$/.test(name) && enumValues('Resource').includes(name.replace(/_\d+$/, ''));
        case 'Location':
          return enumValues('Location').includes(name.replace(/_\d$/, ''));
        case 'DeckId':
          return enumValues('DeckId').includes(name);
        default:
          return false;
      }
    };
    expect(artPaths().filter((path) => !named(path))).toEqual([]);
  });

  it('has a card for every location but the space areas, the Basestar Bridge in three parts', () => {
    const missing = enumValues('Location').filter((location) => !isSpace(location) && locationArt(location) === null);

    expect(missing).toEqual([]);
    expect(locationArt('BASESTAR_BRIDGE')?.preview).toHaveLength(3);
    expect(locationArt('GALACTICA_SPACE_12_OCLOCK')).toBeNull();
  });

  it('finds a Skill Card by its type and value, and a type by its lowest value', () => {
    expect(skillCardArt({ type: 'CALCULATIONS', value: 4 })?.key).toBe('SkillCardType/CALCULATIONS_4');
    expect(skillCardArt({ type: 'CALCULATIONS', value: 9 })).toBeNull();
    expect(skillTypeArt('CALCULATIONS')?.key).toBe('SkillCardType/CALCULATIONS_3');
    expect(enumValues('SkillCardType').filter((type) => skillTypeArt(type) === null)).toEqual([]);
  });

  it('shows a character by his standee, with his sheet as the preview', () => {
    const art = characterArt('LAURA_ROSLIN');

    expect(art?.key).toBe('Character/player-token/LAURA_ROSLIN');
    expect(art?.label).toBe('Laura Roslin');
    expect(art?.preview).toHaveLength(1);
  });

  it('finds the card of every skill check: a Crisis Card, a Super Crisis Card or a location', () => {
    expect(enumValues('SkillCheckType').filter((type) => skillCheckArt(type) === null)).toEqual([]);
    expect(enumArt('SkillCheckType', 'BRIG')?.key).toBe('Location/BRIG');
  });

  it('sets the dials from 0 to 15, and puts the tokens on the tracks', () => {
    expect(resourceArt('FOOD', 0)?.key).toBe('Resource/FOOD_0');
    expect(resourceArt('MORALE', 15)?.key).toBe('Resource/MORALE_15');
    expect(resourceArt('MORALE', 16)).toBeNull();
    expect(trackArt('JumpPreparation', 'MINUS_3')?.key).toBe('JumpPreparation/MINUS_3');
  });

  it('shows the back of a deck, the Loyalty back for the "You Are Not a Cylon" cards, and none for a Skill Card deck', () => {
    expect(deckBackArt('CRISIS')?.key).toBe('DeckId/CRISIS');
    expect(deckBackArt('LOYALTY_NOT_CYLON')?.key).toBe('DeckId/LOYALTY');
    expect(deckBackArt('POLITICS')).toBeNull();
  });
});

import { describe, expect, it } from 'vitest';
import { generate, identifier, typeOf } from './gen-types.mjs';

const sample = {
  formatVersion: 7,
  types: {
    Location: { kind: 'enum', values: ['ARMORY', 'BRIG'] },
    SkillCard: {
      kind: 'record',
      tagged: false,
      interfaces: [],
      components: [
        { name: 'value', type: { t: 'int', nullable: false, role: 'count' } },
        { name: 'type', type: { t: 'string', nullable: true } },
      ],
    },
    Event: { kind: 'union', discriminator: 'kind', members: ['MoveEvent', 'DeckFact.CardsDrawn'] },
    MoveEvent: {
      kind: 'record',
      tagged: true,
      interfaces: ['Event'],
      location: null,
      components: [
        { name: 'playerNumber', type: { t: 'int', nullable: false, role: 'player' } },
        { name: 'destination', type: { t: 'enum', name: 'Location', nullable: true } },
        { name: 'discardCard', type: { t: 'ref', name: 'SkillCard', nullable: true } },
      ],
    },
    'DeckFact.CardsDrawn': {
      kind: 'record',
      tagged: true,
      interfaces: [],
      components: [{ name: 'cards', type: { t: 'cards', nullable: true } }],
    },
    HiddenCards: {
      kind: 'record',
      tagged: false,
      interfaces: [],
      typeParams: ['T'],
      components: [
        { name: 'revealed', type: { t: 'list', of: { t: 'var', name: 'T', nullable: true }, nullable: true } },
        { name: 'colors', type: { t: 'map', key: { t: 'enum', name: 'Location' }, value: { t: 'int', nullable: true, role: 'count' }, nullable: true } },
      ],
    },
    GameMessage: { kind: 'record', type: 'game', interfaces: [], components: [{ name: 'token', type: { t: 'string', nullable: true } }] },
  },
  lists: { events: ['MoveEvent'] },
};

describe('gen-types', () => {
  const ts = generate(sample);

  it('makes identifiers of qualified names', () => {
    expect(identifier('DeckFact.CardsDrawn')).toBe('DeckFact_CardsDrawn');
  });

  it('writes enums as unions of their constants', () => {
    expect(ts).toContain("export type Location = 'ARMORY' | 'BRIG';");
    expect(ts).toContain("export const Location_VALUES: readonly Location[] = ['ARMORY', 'BRIG'];");
  });

  it('writes a tagged record with its kind and nullable references', () => {
    expect(ts).toContain("export interface MoveEvent {\n  kind: 'MoveEvent';\n  playerNumber: number;\n  destination: Location | null;\n  discardCard: SkillCard | null;\n}");
    expect(ts).toContain("kind: 'DeckFact.CardsDrawn';");
  });

  it('writes unions of their members', () => {
    expect(ts).toContain('export type Event = MoveEvent | DeckFact_CardsDrawn;');
  });

  it('writes an untagged record without a kind, and a generic one with its parameters', () => {
    expect(ts).toContain('export interface SkillCard {\n  value: number;\n  type: string | null;\n}');
    expect(ts).toContain('export interface HiddenCards<T> {');
    expect(ts).toContain('revealed: Array<T | null> | null;');
    expect(ts).toContain('colors: Partial<Record<Location, number | null>> | null;');
  });

  it('writes a socket message with its type', () => {
    expect(ts).toContain("export interface GameMessage {\n  type: 'game';");
  });

  it('writes the cards of a deck and the lists', () => {
    expect(typeOf({ t: 'cards', nullable: false })).toBe('DeckCard[]');
    expect(ts).toContain('export type DeckCard = string | SkillCard;');
    expect(ts).toContain("export const EVENTS: readonly string[] = ['MoveEvent'];");
    expect(ts).toContain('export const FORMAT_VERSION = 7;');
  });
});

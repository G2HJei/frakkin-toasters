// The wording of the game: a name for every enum constant and type, and the sentences of events, facts and options
// (`templates.ts`). A template names fields in braces; a value is worded by its type in the schema.
import { humanizeConstant, humanizeType } from './humanize';
import { CONSTANT_LABELS, TYPE_LABELS } from './overrides';
import { CHOSEN_TEMPLATES, EVENT_TEMPLATES, FACT_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES } from './templates';
import { recordNamed, typeNamed, type SourceRef, type TypeRef } from '../wire/schema';
import type { FactProjection, Followup } from '../wire/types';

export interface LabelContext {
  playerName(playerNumber: number): string;
  // a ship by its type and id, e.g. "Viper #1", where the table knows it
  shipName?(id: number): string;
  // a ship's type by its id (a ShipType constant, or CENTURION), where the table knows it
  shipType?(id: number): string | null;
  // the image a value names, for a name to hover (`Words`); the labels don't load images themselves
  artOf?(value: unknown, type: TypeRef): PartArt | null;
  // the small picture in front of a value's words: a resource, a ship type, a title, a token, a die's face ("Die"), a
  // character's portrait
  iconOf?(enumName: string, constant: unknown): PartIcon | null;
  // the character a player plays, where his name shows its portrait and sheet (the log)
  characterOf?(playerNumber: number): string | null;
  // the player who plays a character, where the table knows it
  playerOf?(character: string): number | null;
  // the player who held a title (PRESIDENT, ADMIRAL, CAG) when the words happened, where the log knows him
  titleHolder?(title: string): number | null;
}

/** An image a part of a label names, as `art/images.ts` gives it. */
export interface PartArt {
  key: string;
  url: string;
  label: string;
  shape: string;
}

/** A small picture in front of a part's words; a tinted one takes the color of its words. */
export interface PartIcon {
  key: string;
  url: string;
  shape: string;
  tint?: boolean;
}

/** Words that are good or bad news for the fleet. */
export type Tone = 'good' | 'bad';

/** A piece of a label: its words, the image of what it names, the icon in front of it and its tone. */
export interface Part {
  text: string;
  art?: PartArt | null;
  icon?: PartIcon | null;
  tone?: Tone;
  // the player a name stands for, so a line of the log can leave its own player without his portrait
  player?: number | null;
  // the Skill Card color it names, a type of cards or its deck, which the log shows in that color
  skillColor?: string | null;
}

// The Skill Card colors, which are their decks' names too.
const SKILL_COLORS = new Set(['POLITICS', 'LEADERSHIP', 'TACTICS', 'PILOTING', 'ENGINEERING', 'TREACHERY']);

/** A Skill Card color in words of that color: "Tactics". */
export function skillColorPart(color: string): Part {
  return { text: constantLabel('SkillCardColor', color), skillColor: color };
}

/** A deck by its name, a Skill Card deck's in its color: "Tactics", "Crisis". */
export function deckNamePart(deck: string): Part {
  return SKILL_COLORS.has(deck) ? skillColorPart(deck) : { text: constantLabel('DeckId', deck) };
}

/** A deck in words: "the Tactics deck", its name in its color. */
export function deckParts(deck: string | null | undefined): Part[] {
  return deck ? merged([{ text: 'the ' }, deckNamePart(deck), { text: ' deck' }]) : [{ text: 'the deck' }];
}

/** The words with their first letter in capitals: "A Mutiny Card is drawn". */
export function capitalizedParts(parts: Part[]): Part[] {
  return parts.map((part, index) => (index === 0 ? { ...part, text: capitalized(part.text) } : part));
}

function withIcon(part: Part, icon: PartIcon | null | undefined): Part {
  return icon ? { ...part, icon } : part;
}

function toned(part: Part, tone: Tone | undefined): Part {
  return tone && !part.tone ? { ...part, tone } : part;
}

// "<+passes>" is good news, "<-fails>" bad news: the markers of a template's words
const TONE_MARK = /<([+-])|>/g;

// Words with their tone markers as parts, starting in a tone and saying the tone they end in.
function marked(text: string, tone: Tone | undefined): { parts: Part[]; tone: Tone | undefined } {
  const parts: Part[] = [];
  let current = tone;
  let at = 0;
  for (const match of text.matchAll(TONE_MARK)) {
    if (match.index > at) {
      parts.push(toned({ text: text.substring(at, match.index) }, current));
    }
    current = match[1] === '+' ? 'good' : match[1] === '-' ? 'bad' : undefined;
    at = match.index + match[0].length;
  }
  if (at < text.length) {
    parts.push(toned({ text: text.substring(at) }, current));
  }
  return { parts, tone: current };
}

/** Words with tone markers as parts: "is <-destroyed>". */
export function markedParts(text: string): Part[] {
  return marked(text, undefined).parts;
}

export function partsText(parts: Part[]): string {
  return parts.map((part) => part.text).join('');
}

export const NO_CONTEXT: LabelContext = { playerName: (playerNumber) => `Player ${playerNumber}` };

export function constantLabel(enumName: string, value: string | null | undefined): string {
  if (value === null || value === undefined) {
    return '—';
  }
  return CONSTANT_LABELS[enumName]?.[value] ?? humanizeConstant(value);
}

export function typeLabel(name: string): string {
  return TYPE_LABELS[name] ?? humanizeType(name);
}

export interface SkillCardLike {
  value: number;
  type: string | null;
}

export function skillCardLabel(card: SkillCardLike | null | undefined): string {
  return card ? `${constantLabel('SkillCardType', card.type)} ${card.value}` : '—';
}

// An interrupt window, as the words after "Interrupt cards may be played".
const WINDOWS: Record<string, string> = {
  BeforeDieRoll: 'before the die roll',
  AfterDieRoll: 'after the die roll',
  AfterFtlControlRoll: 'after the FTL Control roll',
  AfterViperAttacked: 'after a viper is attacked',
  BeforePilotedViperAttack: "before a piloted viper's attack",
  BeforeRaidersActivate: 'before the raiders activate',
  AfterBasestarOrCenturionDestroyed: 'after a basestar or a centurion is destroyed',
};

export function windowLabel(kind: string | null | undefined): string {
  return kind ? (WINDOWS[kind] ?? humanizeType(kind).toLowerCase()) : '—';
}

/** "a", "a and b", "a, b and c". */
export function joinWords(words: string[]): string {
  return partsText(joinParts(words.map((text) => [{ text }])));
}

/** Items of words joined like `joinWords`. */
export function joinParts(items: Part[][]): Part[] {
  const parts: Part[] = [];
  items.forEach((item, index) => {
    if (index > 0) {
      parts.push({ text: index === items.length - 1 ? ' and ' : ', ' });
    }
    parts.push(...item);
  });
  return parts;
}

/** A value worded by its type in the schema. */
export function valueLabel(value: unknown, type: TypeRef | undefined, context: LabelContext): string {
  return partsText(valueParts(value, type, context, {}));
}

// The cards a deck or a pile holds, by the enum that names them; Skill Cards are records.
const DECK_CARDS: Record<string, string> = {
  CRISIS: 'CrisisCard',
  SUPER_CRISIS: 'SuperCrisisCard',
  QUORUM: 'QuorumCard',
  LOYALTY: 'LoyaltyCard',
  LOYALTY_NOT_CYLON: 'LoyaltyCard',
  MUTINY: 'MutinyCard',
  MOTIVE: 'MotiveCard',
  DESTINATION: 'DestinationCard',
  GALACTICA_DAMAGE: 'GalacticaDamage',
  PEGASUS_DAMAGE: 'PegasusDamage',
  BASESTAR_DAMAGE: 'BasestarDamage',
};

/** The type of one card of a deck or a pile: a Skill Card, a card enum, or a civilian ship. */
export function cardType(deck: string | null | undefined, pile?: string | null): TypeRef {
  const name = DECK_CARDS[deck ?? ''] ?? (pile && pile !== 'SKILL' ? DECK_CARDS[pile] : undefined);
  if (name) {
    return { t: 'enum', nullable: false, name };
  }
  return deck === 'CIVILIAN_SHIPS' ? { t: 'ref', nullable: false, name: 'CivilianShip' } : { t: 'ref', nullable: false, name: 'SkillCard' };
}

/** A value in parts, a card or a name with its image. */
export function valueParts(value: unknown, type: TypeRef | undefined, context: LabelContext, siblings: Record<string, unknown>): Part[] {
  if (value === null || value === undefined) {
    return [{ text: '—' }];
  }
  if (!type) {
    return [{ text: typeof value === 'object' ? JSON.stringify(value) : String(value) }];
  }
  switch (type.t) {
    case 'int':
    case 'long':
      if (type.role === 'player') {
        return [playerPart(value as number, context)];
      }
      if (type.role === 'ship') {
        return [shipPart(value as number, context)];
      }
      return [{ text: String(value) }];
    case 'boolean':
      return [{ text: value ? 'yes' : 'no' }];
    case 'string':
      return [{ text: String(value) }];
    case 'class':
      return [{ text: typeLabel(String(value)) }];
    case 'enum': {
      const part = withIcon({ text: constantLabel(type.name ?? '', value as string), art: context.artOf?.(value, type) ?? null }, context.iconOf?.(type.name ?? '', value));
      if (type.name === 'Character') {
        return [{ ...part, player: context.playerOf?.(value as string) ?? null }];
      }
      // a color of Skill Cards, or the deck of one
      if ((type.name === 'SkillCardColor' || type.name === 'DeckId') && SKILL_COLORS.has(value as string)) {
        return [{ ...part, skillColor: value as string }];
      }
      return type.name === 'Title' ? [part] : holdersNamed(part, context);
    }
    case 'list':
    case 'set': {
      const elements = value as unknown[];
      return elements.length > 0 ? joinParts(elements.map((element) => valueParts(element, type.of as TypeRef, context, siblings))) : [{ text: 'nothing' }];
    }
    case 'map':
      return mapParts(value as Record<string, unknown>, type, context);
    case 'cards': {
      const cards = value as unknown[];
      const of = cardType(siblings.deck as string | undefined, siblings.pile as string | undefined);
      return cards.length > 0 ? joinParts(cards.map((card) => cardParts(card, of, context))) : [{ text: 'nothing' }];
    }
    case 'ref':
      return refParts(value as Record<string, unknown>, type.name ?? '', context);
    default:
      return [{ text: JSON.stringify(value) }];
  }
}

/**
 * A player by his character's name: where the context knows his character (the log), with its portrait in front and
 * its sheet on hover.
 */
export function playerPart(playerNumber: number, context: LabelContext): Part {
  const part: Part = { text: context.playerName(playerNumber), player: playerNumber };
  const character = context.characterOf?.(playerNumber);
  if (!character) {
    return part;
  }
  const type: TypeRef = { t: 'enum', nullable: false, name: 'Character' };
  return withIcon({ ...part, art: context.artOf?.(character, type) ?? null }, context.iconOf?.('Character', character));
}

/**
 * A title as the player who held it: where the log knows him, his portrait in front of the title's name and the title's
 * card on hover ("the [portrait] President draws …"). A line about a change of the title shows its insignia instead
 * (`{Title:PRESIDENT}`).
 */
export function holderPart(title: string, context: LabelContext): Part {
  const text = constantLabel('Title', title);
  const holder = context.titleHolder?.(title) ?? null;
  const character = holder !== null ? context.characterOf?.(holder) : null;
  if (!character) {
    return { text };
  }
  const type: TypeRef = { t: 'enum', nullable: false, name: 'Title' };
  return withIcon({ text, art: context.artOf?.(title, type) ?? null, player: holder }, context.iconOf?.('Character', character));
}

const HOLDER_TITLES: Record<string, string> = { President: 'PRESIDENT', Admiral: 'ADMIRAL', CAG: 'CAG' };

// A label that names a title's holder, "A civilian ship for the CAG", shows him where the log knows him.
function holdersNamed(part: Part, context: LabelContext): Part[] {
  if (!context.titleHolder || part.art || part.icon) {
    return [part];
  }
  const pieces = part.text.split(/\b(?<=the )(President|Admiral|CAG)\b(?!')/);
  if (pieces.length === 1) {
    return [part];
  }
  return merged(pieces.map((piece, index) => (index % 2 === 1 ? holderPart(HOLDER_TITLES[piece], context) : { ...part, text: piece })));
}

function shipLabel(id: number, context: LabelContext): string {
  return context.shipName?.(id) ?? `ship #${id}`;
}

/**
 * A ship by its id, with its type's icon where its type is known: "Raider #1016". A civilian ship the table doesn't
 * show is "Civilian ship #2007".
 */
export function shipPart(id: number, context: LabelContext, civilian = false): Part {
  const type = context.shipType?.(id) ?? (civilian ? 'CIVILIAN' : null);
  const text = shipLabel(id, context);
  return withIcon({ text: civilian ? text.replace(/^ship #/, 'Civilian ship #') : text }, type ? context.iconOf?.('ShipType', type) : null);
}

// The Skill Card colors a draw takes: "1 Leadership and 2 Piloting cards".
function mapParts(value: Record<string, unknown>, type: TypeRef, context: LabelContext): Part[] {
  const entries = Object.entries(value).filter(([, count]) => count !== 0);
  if (type.key?.t === 'enum' && type.key.name === 'SkillCardColor') {
    const total = entries.reduce((sum, [, count]) => sum + Number(count), 0);
    if (total === 0) {
      return [{ text: 'no cards' }];
    }
    const counts = entries.map(([color, count]) => [{ text: `${count} ` }, skillColorPart(color)]);
    return [...joinParts(counts), { text: total === 1 ? ' card' : ' cards' }];
  }
  return entries.length > 0
    ? joinParts(entries.map(([key, element]) => [...valueParts(key, type.key, context, {}), { text: ` ×${valueLabel(element, type.value, context)}` }]))
    : [{ text: 'none' }];
}

/** A card of a deck or a pile, whatever its kind. */
export function cardLabel(card: unknown): string {
  if (typeof card === 'string') {
    return humanizeConstant(card);
  }
  if (card && typeof card === 'object' && 'type' in card && 'value' in card) {
    return skillCardLabel(card as SkillCardLike);
  }
  if (card && typeof card === 'object' && 'id' in card) {
    return `Civilian ship #${(card as { id: number }).id}`;
  }
  return JSON.stringify(card);
}

/** A card as a name with its image, by the type of the cards it lies among. */
export function cardParts(card: unknown, type: TypeRef, context: LabelContext): Part[] {
  if (typeof card === 'string' && type.t === 'enum') {
    return [{ text: constantLabel(type.name ?? '', card), art: context.artOf?.(card, type) ?? null }];
  }
  if (card && typeof card === 'object' && 'type' in card && 'value' in card) {
    return [{ text: skillCardLabel(card as SkillCardLike), art: context.artOf?.(card, { t: 'ref', nullable: false, name: 'SkillCard' }) ?? null }];
  }
  return [{ text: cardLabel(card) }];
}

function refParts(value: Record<string, unknown>, name: string, context: LabelContext): Part[] {
  switch (name) {
    case 'SkillCard':
      return cardParts(value, { t: 'ref', nullable: false, name }, context);
    case 'ViperActivation':
      return activationParts(value, context, 'third');
    // a card or an ability someone played: "Laura Roslin's Declare Emergency 5"
    case 'PlayedCard':
    case 'PlayedInterruptCard':
    case 'UsedRollAbility': {
      const own = name === 'UsedRollAbility' ? valueParts(value.ability, { t: 'enum', nullable: false, name: 'CharacterAbility' }, context, {}) : cardParts(value.skillCard ?? value.card, { t: 'ref', nullable: false, name: 'SkillCard' }, context);
      return [playerPart(value.playerNumber as number, context), { text: "'s " }, ...own];
    }
    default:
      return [{ text: recordLabel(value, name, context) }];
  }
}

function recordLabel(value: Record<string, unknown>, name: string, context: LabelContext): string {
  const kind = typeof value.kind === 'string' ? value.kind : name;
  const record = recordNamed(kind);
  if (!record) {
    return JSON.stringify(value);
  }
  const parts = record.components
    .filter((component) => value[component.name] !== null && value[component.name] !== undefined)
    .map((component) => `${humanizeType(component.name).toLowerCase()} ${valueLabel(value[component.name], component.type, context)}`);
  return parts.length > 0 ? `${typeLabel(kind)} (${parts.join(', ')})` : typeLabel(kind);
}

/**
 * One activation of a viper (rule 11.1.1): a launch, a move, an attack or an escort. On a button it is an order
 * ("Launch a Viper to …"), in a sentence about its player what he does ("launches a Viper to …").
 */
export function activationLabel(activation: Record<string, unknown>, context: LabelContext, form: 'order' | 'third' = 'order'): string {
  return partsText(activationParts(activation, context, form));
}

/** An activation of a viper in parts, each ship with its icon. */
export function activationParts(activation: Record<string, unknown>, context: LabelContext, form: 'order' | 'third' = 'order'): Part[] {
  const verb = (order: string, third: string) => (form === 'order' ? order : third);
  const ship = (id: unknown, civilian = false) => shipPart(id as number, context, civilian);
  if (activation.launchShipType) {
    const type = activation.launchShipType as string;
    return merged([
      { text: `${verb('Launch', 'launches')} a ` },
      withIcon({ text: constantLabel('ShipType', type) }, context.iconOf?.('ShipType', type)),
      { text: ` to ${constantLabel('Location', activation.launchLocation as string)}` },
    ]);
  }
  const fighter = ship(activation.humanFighterId);
  if (activation.moveLocation) {
    return merged([{ text: `${verb('Move', 'moves')} ` }, fighter, { text: ` to ${constantLabel('Location', activation.moveLocation as string)}` }]);
  }
  if (activation.attackCylonShipId !== null && activation.attackCylonShipId !== undefined) {
    return merged([{ text: `${verb('Attack', 'attacks')} ` }, ship(activation.attackCylonShipId), { text: ' with ' }, fighter]);
  }
  if (activation.escortCivilianShipId !== null && activation.escortCivilianShipId !== undefined) {
    return merged([{ text: `${verb('Escort', 'escorts')} ` }, ship(activation.escortCivilianShipId, true), { text: ' with ' }, fighter]);
  }
  return merged([{ text: `${verb('Activate', 'activates')} ` }, fighter]);
}

/* templates */

// The locations a sentence names with "the": "moves to the Armory", "to Sickbay".
const WITH_ARTICLE = new Set([
  'ADMIRALS_QUARTERS',
  'ARMORY',
  'BRIG',
  'HANGAR_DECK',
  'RESEARCH_LAB',
  'QUORUM_CHAMBER',
  'PRESS_ROOM',
  'PRESIDENTS_OFFICE',
  'ADMINISTRATION',
  'AIRLOCK',
  'MAIN_BATTERIES',
  'ENGINE_ROOM',
  'CYLON_FLEET',
  'HUMAN_FLEET',
  'RESURRECTION_SHIP',
  'BASESTAR_BRIDGE',
]);

const PLACEHOLDER = /\{(\$?\w+|!?[A-Z]\w*:\w+|!\w+)(?:\|(lower|die|damage|count)|#([^}]+)|\?([^:}]*):([^}]*)|=([^}]+))?\}/g;

// a constant a template names itself, worded like a field of its enum: {Resource:FUEL}
const CONSTANT = /^([A-Z]\w*):(\w+)$/;

interface Fill {
  value: Record<string, unknown>;
  recordName: string;
  context: LabelContext;
}

function componentOf(fill: Fill, name: string): { value: unknown; type: TypeRef | undefined } {
  const record = recordNamed(fill.recordName);
  const constant = CONSTANT.exec(name);
  if (constant) {
    return { value: constant[2], type: { t: 'enum', nullable: false, name: constant[1] } };
  }
  if (name === '$card') {
    const enumName = record?.quorumCard ? 'QuorumCard' : 'MutinyCard';
    return { value: record?.quorumCard ?? record?.mutinyCard ?? null, type: { t: 'enum', nullable: false, name: enumName } };
  }
  return { value: fill.value[name], type: record?.components.find((candidate) => candidate.name === name)?.type };
}

function present(value: unknown): boolean {
  return value !== null && value !== undefined && value !== false && !(Array.isArray(value) && value.length === 0);
}

function countOf(value: unknown): number {
  return Array.isArray(value) ? value.length : Number(value);
}

// The words of one placeholder, after the parts so far, which decide "the" or "The" in front of a location.
function placeholderParts(match: RegExpMatchArray, fill: Fill, before: Part[]): Part[] {
  const [, name, modifier, forms, yes, no, cases] = match;
  const holder = /^Holder:(\w+)$/.exec(name);
  if (holder) {
    return [holderPart(holder[1], fill.context)];
  }
  if (name.startsWith('!')) {
    // only the icons of the value, in front of words of the template's own: "{!ShipType:RAIDER}raiders"
    const { value, type } = componentOf(fill, name.substring(1));
    return valueParts(value, type, fill.context, fill.value).flatMap((part) => (part.icon ? [{ text: '', icon: part.icon }] : []));
  }
  const { value, type } = componentOf(fill, name);
  if (forms !== undefined) {
    const [one, many] = forms.includes('/') ? forms.split('/') : [forms, `${forms}s`];
    return [{ text: countOf(value) === 1 ? one : many }];
  }
  if (yes !== undefined) {
    return markedParts(value ? yes : no);
  }
  if (cases !== undefined) {
    const words = Object.fromEntries(cases.split('|').map((entry) => entry.split(':') as [string, string]));
    return markedParts(words[String(value)] ?? words['*'] ?? constantLabel(type?.name ?? '', value as string));
  }
  if (modifier === 'die') {
    // the die's face in front of the number it shows
    return [withIcon({ text: String(value) }, fill.context.iconOf?.('Die', value))];
  }
  if (modifier === 'damage') {
    return galacticaDamageParts(Array.isArray(value) ? value : [value], fill.context);
  }
  if (modifier === 'count') {
    return countedParts(Array.isArray(value) ? value : [value], type, fill.context);
  }
  const parts = civilianNamed(valueParts(value, type, fill.context, fill.value), /civilian/i.test(name), fill.context);
  if (modifier === 'lower') {
    return parts.map((part) => ({ ...part, text: inSentence(part.text) }));
  }
  if (type?.t === 'enum' && (type.name === 'Location' || type.name === 'SkillCheckType') && WITH_ARTICLE.has(value as string)) {
    const text = partsText(before);
    if (!/\bthe $/i.test(text)) {
      return [{ text: text.trim() === '' ? 'The ' : 'the ' }, ...parts];
    }
  }
  return parts;
}

// A label is a sentence's start: inside one its first letter is small, unless it starts with an acronym ("CAG").
function inSentence(label: string): string {
  return /^[A-Z]{2,}\b/.test(label) ? label : label.charAt(0).toLowerCase() + label.substring(1);
}

// A constant's label as a common noun: "Assault Raptor" → "assault raptor"; a short or all-capitals word stays as it
// is: "viper Mk VII".
function commonNoun(enumName: string, constant: string): string {
  return constantLabel(enumName, constant).replace(/\b[A-Z][a-z]{2,}\b/g, (word) => word.toLowerCase());
}

// Constants counted by kind, each kind as a common noun in the plural its count asks for, with its icon in front:
// "2 vipers", "a viper and an assault raptor". Ships are counted by their type where two or more have it, and a ship
// alone keeps its name: "2 vipers and Viper Mk VII #71".
function countedParts(values: unknown[], type: TypeRef | undefined, context: LabelContext): Part[] {
  const of = typeof type?.of === 'object' && type.of ? type.of : type;
  if (of?.role === 'ship') {
    return countedShips(values as number[], context);
  }
  const enumName = of?.name ?? '';
  const counts = new Map<unknown, number>();
  for (const value of values) {
    counts.set(value, (counts.get(value) ?? 0) + 1);
  }
  const kinds = [...counts].map(([value, count]) => {
    const noun = commonNoun(enumName, value as string);
    const amount = count === 1 ? (/^[aeiou]/i.test(noun) ? 'an ' : 'a ') : `${count} `;
    return [{ text: amount }, withIcon({ text: count === 1 ? noun : `${noun}s` }, context.iconOf?.(enumName, value))];
  });
  return merged(joinParts(kinds));
}

function countedShips(ids: number[], context: LabelContext): Part[] {
  const byType = new Map<string, number[]>();
  for (const id of ids) {
    // a ship of no known type is a kind of its own
    const type = context.shipType?.(id) ?? `#${id}`;
    byType.set(type, [...(byType.get(type) ?? []), id]);
  }
  const items = [...byType].flatMap(([type, ships]) =>
    ships.length > 1 ? [[{ text: `${ships.length} ` }, withIcon({ text: `${commonNoun('ShipType', type)}s` }, context.iconOf?.('ShipType', type))]] : ships.map((id) => [shipPart(id, context)]),
  );
  return merged(joinParts(items));
}

// What Galactica's damage tokens do, after "to": the locations they damage, by their names to hover, and what the fuel
// and the food token cost (1 of their resource): "damage the Armory and Command", "damage the Armory and lose 1 fuel".
function galacticaDamageParts(tokens: unknown[], context: LabelContext): Part[] {
  const location: TypeRef = { t: 'enum', nullable: false, name: 'Location' };
  const resource: TypeRef = { t: 'enum', nullable: false, name: 'Resource' };
  const costs: unknown[] = tokens.filter((token) => token === 'FUEL' || token === 'FOOD');
  const places = tokens.filter((token) => !costs.includes(token));
  const named = (place: unknown) => [...(WITH_ARTICLE.has(place as string) ? [{ text: 'the ' }] : []), ...valueParts(place, location, context, {})];
  const lost = (cost: unknown) => [{ text: '1 ' }, ...valueParts(cost, resource, context, {}).map((part) => ({ ...part, text: part.text.toLowerCase() }))].map((part) => toned(part, 'bad'));
  const damage = places.length > 0 ? [{ text: 'damage', tone: 'bad' as Tone }, { text: ' ' }, ...joinParts(places.map(named))] : [];
  const lose = costs.length > 0 ? [{ text: 'lose ' }, ...joinParts(costs.map(lost))] : [];
  return merged(joinParts([damage, lose].filter((words) => words.length > 0)));
}

// "[a|b]": a when every field it names has a value, otherwise b, or nothing. A field it names only for its icon is a
// condition too: "[{!discardAcceptProphecy}discards …|gives up …]".
function chooseAlternative(segment: string, fill: Fill): string {
  const alternatives: string[] = [];
  let depth = 0;
  let start = 0;
  for (let at = 0; at < segment.length; at++) {
    const char = segment[at];
    if (char === '{') depth++;
    else if (char === '}') depth--;
    else if (char === '|' && depth === 0) {
      alternatives.push(segment.substring(start, at));
      start = at + 1;
    }
  }
  alternatives.push(segment.substring(start));
  // only a field whose value it prints has to be there, not one that picks a plural or a word
  const prints = (match: RegExpMatchArray) => match[3] === undefined && match[4] === undefined && match[6] === undefined;
  const fits = (alternative: string) => [...alternative.matchAll(PLACEHOLDER)].filter(prints).every((match) => present(componentOf(fill, match[1].replace(/^!/, '')).value));
  if (fits(alternatives[0])) {
    return alternatives[0];
  }
  return alternatives.length > 1 ? alternatives[alternatives.length - 1] : '';
}

/** A template's words, each field a part of its own with the image of what it names. */
export function fillTemplate(template: string, value: Record<string, unknown>, recordName: string, context: LabelContext): Part[] {
  const fill: Fill = { value, recordName, context };
  // the optional segments first, so their placeholders fill like the others
  const chosen = template.replace(/\[([^\]]*)]/g, (_, segment: string) => chooseAlternative(segment, fill));
  const parts: Part[] = [];
  // the tone of a marker that the words between the fields open, e.g. "<-{amount} {resource}>"
  let tone: Tone | undefined;
  const words = (text: string) => {
    const next = marked(text, tone);
    parts.push(...next.parts);
    tone = next.tone;
  };
  let at = 0;
  for (const match of chosen.matchAll(PLACEHOLDER)) {
    if (match.index > at) {
      words(chosen.substring(at, match.index));
    }
    parts.push(...placeholderParts(match, fill, parts).map((part) => toned(part, tone)));
    at = match.index + match[0].length;
  }
  if (at < chosen.length) {
    words(chosen.substring(at));
  }
  return merged(parts);
}

// Neighbouring plain words of a tone as one part; a player's name stays a part of its own, which knows its player.
function merged(parts: Part[]): Part[] {
  const out: Part[] = [];
  const plain = (part: Part) => !part.art && !part.icon && (part.player ?? null) === null && !part.skillColor;
  for (const part of parts) {
    const last = out[out.length - 1];
    if (last && plain(last) && plain(part) && last.tone === part.tone) {
      last.text += part.text;
    } else if (part.text !== '' || part.art || part.icon) {
      out.push({ ...part });
    }
  }
  return out;
}

/** Whether an event is about one player, who the log names above it. */
export function isPlayerEvent(kind: string | null | undefined): boolean {
  return !!recordNamed(kind ?? '')?.components.some((component) => component.name === 'playerNumber');
}

/**
 * The words without their subject, for a line under the name of the player it is about: "moves to the Armory" under
 * "William Adama". Null when they don't start with him.
 */
export function withoutSubject(parts: Part[], subject: string): Part[] | null {
  const first = parts[0];
  if (!first || !first.text.startsWith(subject)) {
    return null;
  }
  const rest = first.text.substring(subject.length);
  if (rest !== '' && !rest.startsWith(' ') && !rest.startsWith("'")) {
    return null;
  }
  const head = rest.replace(/^ /, '');
  const after = parts.slice(1).map((part, index) => (index === 0 && head === '' ? { ...part, text: part.text.replace(/^ /, '') } : part));
  return merged([...(head !== '' ? [{ ...first, text: head, art: null }] : []), ...after]);
}

/* events */

/** An event with its fields, by its template, or its name and fields. */
export function eventLabel(event: Record<string, unknown> | null | undefined, context: LabelContext): string {
  return partsText(eventParts(event, context));
}

/** An event's sentence in parts, the names it fills in with their images. */
export function eventParts(event: Record<string, unknown> | null | undefined, context: LabelContext): Part[] {
  if (!event) {
    return [{ text: '—' }];
  }
  const kind = String(event.kind);
  const template = EVENT_TEMPLATES[kind] ?? fallbackTemplate(kind);
  return template ? fillTemplate(template, event, kind, context) : [{ text: recordLabel(event, kind, context) }];
}

// A Quorum or a Mutiny Card's event is the card: "Laura Roslin plays Arrest Order".
function fallbackTemplate(kind: string): string | null {
  const record = recordNamed(kind);
  return record?.quorumCard || record?.mutinyCard ? '{playerNumber} plays {$card}' : null;
}

/**
 * An event as an answer on a button, an order to the player it names: "Pass", "Move or take an action", "Choose the
 * top option of Riots". An event about someone else keeps its sentence.
 */
export function optionEventParts(event: Record<string, unknown> | null | undefined, context: LabelContext): Part[] {
  const template = event ? OPTION_TEMPLATES[String(event.kind)] : undefined;
  if (template) {
    return fillTemplate(template, event!, String(event!.kind), context);
  }
  const parts = eventParts(event, context);
  const player = event?.playerNumber;
  const rest = typeof player === 'number' ? withoutSubject(parts, context.playerName(player)) : null;
  if (!rest || rest.length === 0 || rest[0].art) {
    return parts;
  }
  // "may activate Viper #1 again" is the order "Activate Viper #1 again"
  const [verb, ...words] = rest[0].text.replace(/^may /, '').split(' ');
  return [{ ...rest[0], text: [capitalized(baseForm(verb)), ...words].join(' ') }, ...rest.slice(1)];
}

// "chooses" → "choose", "tries" → "try", "passes" → "pass", "has" → "have"
function baseForm(verb: string): string {
  const irregular: Record<string, string> = { has: 'have', is: 'be', does: 'do', goes: 'go', "doesn't": "don't" };
  if (irregular[verb]) {
    return irregular[verb];
  }
  if (/[^aeiou]ies$/.test(verb)) {
    return verb.replace(/ies$/, 'y');
  }
  if (/(ch|sh|ss|x|z)es$/.test(verb)) {
    return verb.replace(/es$/, '');
  }
  return verb.endsWith('s') && !verb.endsWith('ss') ? verb.substring(0, verb.length - 1) : verb;
}

/** The words of an event that names no player, under the name of the player who picked it; null without any. */
export function chosenParts(event: Record<string, unknown>, context: LabelContext): Part[] | null {
  const template = CHOSEN_TEMPLATES[String(event.kind)];
  return template ? fillTemplate(template, event, String(event.kind), context) : null;
}

/** The words of an event whose fields are hidden, about the player the log names, or its type. */
export function hiddenEventParts(kind: string, actor: number | null, context: LabelContext): Part[] {
  const template = HIDDEN_TEMPLATES[kind];
  if (template && actor !== null) {
    return fillTemplate(template, { kind, playerNumber: actor }, kind, context);
  }
  return [{ text: typeLabel(kind) }];
}

/* sources */

/**
 * The card or ability an event exists for, where its fields don't name it: its record's `source`, or the source of a
 * constant it holds, e.g. the card a GrantEvent's `grantedBy` names. The type alone gives the first, so an event whose
 * fields are hidden still has it. Null for most events.
 */
export function eventSource(kind: string, event: Record<string, unknown> | null | undefined): SourceRef | null {
  const record = recordNamed(kind);
  if (!record) {
    return null;
  }
  if (record.source) {
    return record.source;
  }
  for (const component of event ? record.components : []) {
    const value = event?.[component.name];
    const type = component.type.t === 'enum' ? typeNamed(component.type.name ?? '') : undefined;
    const source = type?.kind === 'enum' && typeof value === 'string' ? type.sources?.[value] : undefined;
    if (source) {
      return source;
    }
  }
  return null;
}

/** A source as a name to hover: the card, or the character sheet that prints the ability. */
export function sourcePart(source: SourceRef, context: LabelContext): Part {
  const type: TypeRef = { t: 'enum', nullable: false, name: source.enum };
  return { text: constantLabel(source.enum, source.constant), art: context.artOf?.(source.constant, type) ?? null };
}

/**
 * The source of an event, to show beside its words: null when it has none, or when its words name it already, e.g.
 * "Cavil uses Primacy".
 */
export function unnamedSource(kind: string, event: Record<string, unknown> | null | undefined, words: Part[], context: LabelContext): Part | null {
  const source = eventSource(kind, event);
  if (!source) {
    return null;
  }
  const part = sourcePart(source, context);
  return partsText(words).toLowerCase().includes(part.text.toLowerCase()) ? null : part;
}

/* options of a choice */

/**
 * An option of a choice, as a button says it: "Pass" for a NoOpEvent, the action a decision asks for, the parts of
 * an AllOf one after the other ("Move, then take an action"), otherwise the event's sentence.
 */
export function optionLabel(option: Followup | null | undefined, context: LabelContext): string {
  if (!option) {
    return '—';
  }
  switch (option.kind) {
    case 'None':
      return 'Nothing';
    case 'Single': {
      const event = option.event as unknown as Record<string, unknown> | null;
      if (event?.kind === 'PlayerDecisionEvent') {
        return typeLabel(String(event.action));
      }
      return partsText(optionEventParts(event, context));
    }
    case 'AllOf':
      return (option.followups ?? []).map((part, index) => (index === 0 ? optionLabel(part, context) : lowerFirst(optionLabel(part, context)))).join(', then ');
    case 'OneOf':
      return (option.options ?? []).map((part, index) => (index === 0 ? optionLabel(part, context) : lowerFirst(optionLabel(part, context)))).join(' or ');
    default:
      return JSON.stringify(option);
  }
}

// "Take an action" after "then"; a name, e.g. a character's, stays as it is
const LOWER_AFTER_THEN = new Set(['Take', 'Move', 'Pass', 'Choose', 'Play', 'Draw', 'Discard', 'Let', 'Put', 'Keep', 'Resolve', 'Activate', 'Launch', 'Attack', 'Escort', 'The', 'A', 'An', 'Nothing']);

function lowerFirst(text: string): string {
  return LOWER_AFTER_THEN.has(text.split(' ')[0]) ? text.charAt(0).toLowerCase() + text.substring(1) : text;
}

/* facts */

export function factLabel(fact: FactProjection | null | undefined, context: LabelContext): string {
  return partsText(factParts(fact, context));
}

const IN_SPACE = new Set(['GALACTICA_SPACE', 'CYLON_FLEET_SPACE', 'BOARDING_PARTY']);

// "ship #2007" for a civilian ship the table doesn't show is "Civilian ship #2007"
function civilianNamed(parts: Part[], civilian: boolean, context: LabelContext): Part[] {
  if (!civilian) {
    return parts;
  }
  return parts.map((part) => (/^ship #/.test(part.text) ? withIcon({ ...part, text: part.text.replace(/^ship #/, 'Civilian ship #') }, part.icon ?? context.iconOf?.('ShipType', 'CIVILIAN')) : part));
}

/**
 * A ship's move in words: the ship, the verb for one ship and for several, and where. The verb has tone markers: a
 * human ship lost is bad news, a Cylon ship destroyed good news.
 */
export interface ShipMove {
  ship: Part;
  one: string;
  many: string;
  tail: Part[];
}

const CYLON_SHIPS = new Set(['RAIDER', 'HEAVY_RAIDER', 'BASESTAR', 'CENTURION']);
const CYLON_ZONES = new Set(['CYLON_SUPPLY', 'CENTURION_SUPPLY', 'CYLON_FLEET_SPACE', 'BOARDING_PARTY']);

// What happened to a ship, by where it went: destroyed, damaged, launched, landed, repaired or moved in space.
export function shipMove(fact: Record<string, unknown>, context: LabelContext): ShipMove {
  const from = fact.from as string | null;
  const to = fact.to as string | null;
  const civilian = from === 'CIVILIAN_DECK' || to === 'CIVILIAN_DECK';
  const ship = shipPart(fact.shipId as number, context, civilian);
  const type = context.shipType?.(fact.shipId as number) ?? null;
  const cylon = type ? CYLON_SHIPS.has(type) : CYLON_ZONES.has(from ?? '') || CYLON_ZONES.has(to ?? '');
  // what a loss is for the fleet: bad for its own ships, good for the Cylons'
  const lost = cylon ? '+' : '-';
  const where = (location: unknown) => fillTemplate(' {location}', { location }, 'ShipFact.ShipMoved', context);
  const move = (one: string, many: string, tail: Part[] = []): ShipMove => ({ ship, one, many, tail });
  if ((IN_SPACE.has(from ?? '') || from === 'RESERVES') && (to === 'REMOVED' || to === 'CYLON_SUPPLY' || to === 'CENTURION_SUPPLY')) {
    return move(`is <${lost}destroyed>`, `are <${lost}destroyed>`);
  }
  switch (to) {
    case 'DAMAGED':
      return move(`is <${lost}damaged>`, `are <${lost}damaged>`);
    case 'RESERVES':
      if (IN_SPACE.has(from ?? '')) {
        return move('lands', 'land');
      }
      return from === 'HUMAN_SUPPLY' ? move('joins the Reserves', 'join the Reserves') : move('is <+repaired>', 'are <+repaired>');
    case 'GALACTICA_SPACE':
      if (from === 'GALACTICA_SPACE') {
        return move('moves to', 'move to', where(fact.location));
      }
      if (from === 'CIVILIAN_DECK') {
        return move('appears at', 'appear at', where(fact.location));
      }
      if (from === 'CYLON_FLEET_SPACE') {
        return move('comes from the Cylon Fleet board to', 'come from the Cylon Fleet board to', where(fact.location));
      }
      return move('launches to', 'launch to', where(fact.location));
    case 'CYLON_FLEET_SPACE':
      return move('goes to', 'go to', [...where(fact.location), { text: ' of the Cylon Fleet board' }]);
    case 'BOARDING_PARTY':
      return move('<-boards Galactica>', '<-board Galactica>');
    case 'CIVILIAN_DECK':
      return move('goes back into the deck', 'go back into the deck');
    case 'HUMAN_SUPPLY':
    case 'CYLON_SUPPLY':
    case 'CENTURION_SUPPLY':
      return move('goes back to the supply', 'go back to the supply');
    case 'REMOVED':
      return move('leaves the game', 'leave the game');
    default:
      return move('moves', 'move');
  }
}

/** Ship moves of one kind as one sentence: "Raider #1016 and Raider #1017 move to Space 6 o'clock". */
export function shipMovesParts(ships: Part[], move: ShipMove): Part[] {
  return merged([...joinParts(ships.map((ship) => [ship])), ...markedParts(` ${ships.length === 1 ? move.one : move.many}`), ...move.tail]);
}

// The cards of a hidden fact by their kind: "2 Skill Cards (1 Politics, 1 Tactics)", "a Loyalty Card".
export function hiddenCardsLabel(count: number, deck: string | null | undefined, pile: string | null | undefined, colors: Record<string, number | undefined> | null | undefined): string {
  return partsText(hiddenCardsParts(count, deck, pile, colors));
}

/** The cards of a hidden fact in parts, each Skill Card color in its color: "a Tactics card", "2 Skill Cards (…)". */
export function hiddenCardsParts(count: number, deck: string | null | undefined, pile: string | null | undefined, colors: Record<string, number | undefined> | null | undefined): Part[] {
  const shown = Object.entries(colors ?? {}).filter(([, n]) => (n ?? 0) > 0);
  const cards = count === 1 ? ' card' : ' cards';
  if (shown.length === 1) {
    return [{ text: `${count} ` }, skillColorPart(shown[0][0]), { text: cards }];
  }
  const noun = cardNoun(deck, pile);
  const amount = count === 1 && noun !== 'card' ? `${/^[AEIOU]/i.test(noun) ? 'an' : 'a'} ` : `${count} `;
  // a Skill Card deck's cards by its color: "an Engineering card"
  const kind: Part[] = deck && SKILL_COLORS.has(deck) ? [skillColorPart(deck), { text: cards }] : [{ text: count === 1 ? noun : `${noun}s` }];
  const split = shown.length > 1 ? [{ text: ' (' }, ...joinWith(shown.map(([color, n]) => [{ text: `${n} ` }, skillColorPart(color)]), ', '), { text: ')' }] : [];
  return merged([{ text: amount }, ...kind, ...split]);
}

function joinWith(items: Part[][], separator: string): Part[] {
  return items.flatMap((item, index) => (index > 0 ? [{ text: separator }, ...item] : item));
}

function cardNoun(deck: string | null | undefined, pile: string | null | undefined): string {
  switch (deck ?? pile) {
    case 'SKILL':
      return 'Skill Card';
    case 'LOYALTY':
    case 'LOYALTY_NOT_CYLON':
      return 'Loyalty Card';
    case 'MUTINY':
      return 'Mutiny Card';
    case 'QUORUM':
      return 'Quorum Card';
    case 'SUPER_CRISIS':
      return 'Super Crisis Card';
    case 'MOTIVE':
      return 'Motive Card';
    case 'CRISIS':
      return 'Crisis Card';
    case 'DESTINATION':
      return 'Destination Card';
    case 'DESTINY':
      return 'Destiny card';
    case 'CIVILIAN_SHIPS':
      return 'civilian ship';
    case 'GALACTICA_DAMAGE':
    case 'PEGASUS_DAMAGE':
    case 'BASESTAR_DAMAGE':
      return 'damage token';
    case 'POLITICS':
    case 'LEADERSHIP':
    case 'TACTICS':
    case 'PILOTING':
    case 'ENGINEERING':
    case 'TREACHERY':
      return `${constantLabel('DeckId', deck)} card`;
    default:
      return 'card';
  }
}

// A fact about cards the viewer may not see, in words: who, how many and of what kind.
function hiddenFactParts(fact: Extract<FactProjection, { kind: 'Hidden' }>, context: LabelContext): Part[] {
  const who: Part = fact.playerNumber !== null ? playerPart(fact.playerNumber, context) : { text: 'Someone' };
  const other: Part = fact.otherPlayerNumber !== null ? playerPart(fact.otherPlayerNumber, context) : { text: 'another player' };
  const cards = hiddenCardsParts(fact.count, fact.deck, fact.pile, fact.colors);
  const deck = deckParts(fact.deck);
  const many = fact.count !== 1;
  // the cards first, as the sentence starts with them: "A Mutiny Card is drawn"
  const first = (...words: Part[]) => merged([...capitalizedParts(cards), ...words]);
  switch (fact.fact) {
    case 'PlayerFact.CardsGained':
      return [who, { text: ' gets ' }, ...cards];
    case 'PlayerFact.CardsLost':
      return [who, { text: ' loses ' }, ...cards];
    case 'PlayerFact.CardsSeen':
      return [who, { text: ' looks at ' }, ...cards];
    case 'PlayerFact.LoyaltyCardSeen':
      return [who, { text: ' looks at a Loyalty Card of ' }, other];
    case 'DeckFact.CardsDrawn':
      return first({ text: ` ${many ? 'are' : 'is'} drawn` });
    case 'DeckFact.CardsDiscarded':
      return first({ text: ` ${many ? 'are' : 'is'} discarded face down` });
    case 'DeckFact.CardsAddedOnTop':
      return first({ text: ` ${many ? 'go' : 'goes'} on top of ` }, ...deck);
    case 'DeckFact.CardsAddedToBottom':
      return first({ text: ` ${many ? 'go' : 'goes'} to the bottom of ` }, ...deck);
    case 'DeckFact.CardsPulled':
      return first({ text: ` ${many ? 'are' : 'is'} taken out of ` }, ...deck);
    case 'DeckFact.DeckShuffled':
      return merged([...capitalizedParts(deck), { text: ' is shuffled' }]);
    case 'SkillCheckFact.CardsPlaced':
      return first({ text: ` ${many ? 'go' : 'goes'} into the skill check face down` });
    case 'SkillCheckFact.CardsDrawn':
      return [{ text: "The skill check's cards are shuffled" }];
    case 'SkillCheckFact.CardsPlayedBefore':
      return [who, { text: ' plays ' }, ...cards, { text: ' before the skill check' }];
    case 'SkillCheckFact.CardsLookedAt':
      return [who, { text: ' looks at the cards ' }, other, { text: ' put in' }];
    case 'TitleFact.PresidentQuorumCardGained':
      return [{ text: 'The ' }, holderPart('PRESIDENT', context), { text: ` gets ${hiddenCardsLabel(fact.count, 'QUORUM', null, null)}` }];
    case 'TitleFact.PresidentQuorumCardsLost':
      return [{ text: 'The ' }, holderPart('PRESIDENT', context), { text: ` loses ${hiddenCardsLabel(fact.count, 'QUORUM', null, null)}` }];
    case 'InterruptFact.InterruptCardPlayed':
      return [who, { text: ' plays an interrupt card' }];
    case 'InterruptFact.RollAbilityUsed':
      return [who, { text: ' uses an ability on the roll' }];
    default:
      return [who, { text: `: ${typeLabel(fact.fact ?? '')}, ` }, ...cards];
  }
}

function capitalized(text: string): string {
  return text.charAt(0).toUpperCase() + text.substring(1);
}

/** A fact's sentence in parts, the names it fills in with their images. */
export function factParts(fact: FactProjection | null | undefined, context: LabelContext): Part[] {
  if (!fact) {
    return [{ text: '—' }];
  }
  if (fact.kind === 'Hidden') {
    return hiddenFactParts(fact, context);
  }
  const shown = fact.fact as unknown as Record<string, unknown> | null;
  if (!shown) {
    return [{ text: '—' }];
  }
  const kind = String(shown.kind);
  if (kind === 'ShipFact.ShipMoved') {
    const move = shipMove(shown, context);
    return shipMovesParts([move.ship], move);
  }
  const card = interruptCardParts(kind, shown, context);
  if (card) {
    return card;
  }
  const template = FACT_TEMPLATES[kind];
  return template ? fillTemplate(template, shown, kind, context) : [{ text: recordLabel(shown, kind, context) }];
}

// The interrupt cards and the roll abilities, by who played them.
function interruptCardParts(kind: string, fact: Record<string, unknown>, context: LabelContext): Part[] | null {
  const played = (fact.card ?? fact.ability) as Record<string, unknown> | undefined;
  if (!played) {
    return null;
  }
  const who = playerPart(played.playerNumber as number, context);
  const card = () => cardParts(played.card, { t: 'ref', nullable: false, name: 'SkillCard' }, context);
  const ability = () => valueParts(played.ability, { t: 'enum', nullable: false, name: 'CharacterAbility' }, context, {});
  switch (kind) {
    case 'InterruptFact.InterruptCardPlayed':
      return [who, { text: ' plays ' }, ...card()];
    case 'InterruptFact.InterruptCardReturned':
      return [...card(), { text: ' goes back to ' }, who];
    case 'InterruptFact.InterruptCardResolved':
      return [who, { text: "'s " }, ...card(), { text: ' takes effect' }];
    case 'InterruptFact.RollAbilityUsed':
      return [who, { text: ' uses ' }, ...ability(), { text: ' on the roll' }];
    case 'InterruptFact.RollAbilityWithdrawn':
      return [who, { text: "'s " }, ...ability(), { text: ' waits for the next roll' }];
    case 'InterruptFact.RollAbilityResolved':
      return [who, { text: "'s " }, ...ability(), { text: ' takes effect' }];
    default:
      return null;
  }
}

export const TEMPLATED_EVENTS = Object.keys(EVENT_TEMPLATES);
export const TEMPLATED_FACTS = Object.keys(FACT_TEMPLATES);
export { CHOSEN_TEMPLATES, EVENT_TEMPLATES, FACT_TEMPLATES, HIDDEN_TEMPLATES, OPTION_TEMPLATES };

// The images of materials-sorted at the repository's root: a folder per enum of the core, a file per constant, named
// like it. Vite copies them into the build and gives each its URL. A component that has no image falls back to its name.
import { constantLabel, skillCardLabel, type SkillCardLike } from '../labels/describe';
import { abilityCharacter } from '../wire/schema';
import admiralIcon from '../table/players/icons/ADMIRAL.png';
import cagIcon from '../table/players/icons/CAG.png';
import presidentIcon from '../table/players/icons/PRESIDENT.png';
import { RESOURCE_NAMES, resourceIconUrl, resourceLabel, type ResourceName } from '../table/board/resources';
import { stepIconSvg, type TurnStep } from '../table/turn/StepIcon';
import { glyphSvg, type GlyphName } from './Glyph';

// only the folders: the full-size board scan at the root isn't for the browser (the table's board is board.jpg)
const FILES = import.meta.glob<string>('./*/**/*.{png,jpg,gif,svg}', {
  base: '../../../materials-sorted',
  eager: true,
  query: '?url',
  import: 'default',
});

// "./CrisisCard/WATER_SABOTAGED.png" -> "CrisisCard/WATER_SABOTAGED"
const URLS = new Map<string, string>(Object.entries(FILES).map(([path, url]) => [path.replace(/^\.\//, '').replace(/\.\w+$/, ''), url]));

export function artPaths(): string[] {
  return [...URLS.keys()];
}

export type ArtShape = 'card' | 'token' | 'pilot' | 'location' | 'ship' | 'damage' | 'dial' | 'track' | 'back' | 'icon';

export interface ArtRef {
  key: string;
  url: string;
  label: string;
  shape: ArtShape;
  preview: string[];
}

function art(path: string, label: string, shape: ArtShape, previewPaths: string[] = [path]): ArtRef | null {
  const url = URLS.get(path);
  if (!url) {
    return null;
  }
  const preview = previewPaths.map((previewPath) => URLS.get(previewPath)).filter((found): found is string => found !== undefined);
  return { key: path, url, label, shape, preview };
}

export const CARD_ENUMS = [
  'CrisisCard',
  'SuperCrisisCard',
  'DestinationCard',
  'LoyaltyCard',
  'MotiveCard',
  'MutinyCard',
  'QuorumCard',
  'ObjectiveCard',
] as const;

export type CardEnum = (typeof CARD_ENUMS)[number];

export type DamageEnum = 'GalacticaDamage' | 'PegasusDamage' | 'BasestarDamage';

type Constant = string | null | undefined;

export function cardArt(enumName: CardEnum, card: Constant): ArtRef | null {
  return card ? art(`${enumName}/${card}`, constantLabel(enumName, card), 'card') : null;
}

// A Skill Card's image is named by its type and value, e.g. SkillCardType/CALCULATIONS_3.
export function skillCardArt(card: SkillCardLike | null | undefined): ArtRef | null {
  return card?.type ? art(`SkillCardType/${card.type}_${card.value}`, skillCardLabel(card), 'card') : null;
}

/** A Skill Card known only by its type, e.g. a card played on a die roll: the image of its lowest value. */
export function skillTypeArt(type: Constant): ArtRef | null {
  if (!type) {
    return null;
  }
  const path = artPaths()
    .filter((candidate) => new RegExp(`^SkillCardType/${type}_\\d+$`).test(candidate))
    .sort()[0];
  return path ? art(path, constantLabel('SkillCardType', type), 'card') : null;
}

export function characterArt(character: Constant): ArtRef | null {
  return character
    ? art(`Character/player-token/${character}`, constantLabel('Character', character), 'token', [`Character/card/${character}`])
    : null;
}

export function characterSheetArt(character: Constant): ArtRef | null {
  return character ? art(`Character/card/${character}`, constantLabel('Character', character), 'card') : null;
}

/** A pilot's round token, for his ship. Only the pilots have one. */
export function pilotArt(character: Constant): ArtRef | null {
  return character
    ? art(`Character/piloting-token/${character}`, constantLabel('Character', character), 'pilot', [`Character/card/${character}`])
    : null;
}

// The Basestar Bridge card comes in three parts, its name and its six abilities.
const LOCATION_PARTS: Partial<Record<string, string[]>> = {
  BASESTAR_BRIDGE: ['BASESTAR_BRIDGE_1', 'BASESTAR_BRIDGE_2', 'BASESTAR_BRIDGE_3'],
};

/** A location's card. The space areas have none. */
export function locationArt(location: Constant): ArtRef | null {
  if (!location) {
    return null;
  }
  const parts = (LOCATION_PARTS[location] ?? [location]).map((part) => `Location/${part}`);
  return art(parts[0], constantLabel('Location', location), 'location', parts);
}

export function shipArt(type: Constant): ArtRef | null {
  return type ? art(`ShipType/${type}`, constantLabel('ShipType', type), 'ship') : null;
}

export function damageArt(enumName: DamageEnum, damage: Constant): ArtRef | null {
  return damage ? art(`${enumName}/${damage}`, constantLabel(enumName, damage), 'damage') : null;
}

export type TitleName = 'PRESIDENT' | 'ADMIRAL' | 'CAG' | 'INFILTRATING';

const TITLE_LABELS: Record<TitleName, string> = { PRESIDENT: 'President', ADMIRAL: 'Admiral', CAG: 'CAG', INFILTRATING: 'Infiltrating' };

export function titleArt(title: TitleName): ArtRef | null {
  return art(`Title/${title}`, TITLE_LABELS[title], 'card');
}

// The insignia of the title cards, cut out of their cards: the whole card is too small to read in a badge or a button.
// The Infiltration Card has none.
const TITLE_ICONS: Partial<Record<TitleName, string>> = { PRESIDENT: presidentIcon, ADMIRAL: admiralIcon, CAG: cagIcon };

export function titleIcon(title: TitleName): string | undefined {
  return TITLE_ICONS[title];
}

export function titleIconArt(title: TitleName): ArtRef | null {
  const icon = TITLE_ICONS[title];
  const card = titleArt(title);
  return icon && card ? { key: `TitleIcon/${title}`, url: icon, label: card.label, shape: 'icon', preview: card.preview } : card;
}

export function isTitleName(constant: Constant): constant is TitleName {
  return constant === 'PRESIDENT' || constant === 'ADMIRAL' || constant === 'CAG' || constant === 'INFILTRATING';
}

export type TokenName = 'CURRENT_PLAYER' | 'MIRACLE' | 'NUKE' | 'CENTURION';

const TOKEN_LABELS: Record<TokenName, string> = { CURRENT_PLAYER: 'Current player', MIRACLE: 'Miracle token', NUKE: 'Nuke', CENTURION: 'Centurion' };

export function tokenArt(token: TokenName): ArtRef | null {
  return art(`Token/${token}`, TOKEN_LABELS[token], 'token', []);
}

export function dieArt(value: number | null | undefined): ArtRef | null {
  return value ? art(`Die/${value}`, `Die: ${value}`, 'token', []) : null;
}

export function skillBackArt(): ArtRef | null {
  return art('Back/SKILL', 'Skill Card', 'back', []);
}

// the colors of the Skill Cards, as the table's styles have them (--politics, ...)
const SKILL_COLORS: Record<string, string> = {
  POLITICS: '#c9a227',
  LEADERSHIP: '#5f9e4f',
  TACTICS: '#8a63c9',
  PILOTING: '#c0503f',
  ENGINEERING: '#3f7fc0',
  TREACHERY: '#6d4a3a',
};

export function skillColorBackArt(color: Constant): ArtRef | null {
  const fill = color ? SKILL_COLORS[color] : undefined;
  if (!color || !fill) {
    return null;
  }
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 46 72">' +
    '<pattern id="h" width="9" height="9" patternUnits="userSpaceOnUse" patternTransform="rotate(45)"><rect width="4" height="9" fill="#ffffff" fill-opacity="0.16"/></pattern>' +
    `<rect x="0.5" y="0.5" width="45" height="71" rx="4" fill="${fill}" stroke="#ffffff" stroke-opacity="0.35"/>` +
    '<rect x="0.5" y="0.5" width="45" height="71" rx="4" fill="url(#h)"/></svg>';
  return { key: `SkillCardColor/${color}`, url: `data:image/svg+xml,${encodeURIComponent(svg)}`, label: constantLabel('SkillCardColor', color), shape: 'back', preview: [] };
}

/**
 * The back of a deck's cards. The Skill Card decks have none: their color shows instead. A back has nothing to read, so
 * it has no preview, which would only cover the pile's popover.
 */
export function deckBackArt(deck: Constant): ArtRef | null {
  if (deck === 'PEGASUS_DAMAGE') {
    return pegasusDamageBackArt();
  }
  // the "You Are Not a Cylon" cards are Loyalty Cards, with their back
  const back = deck === 'LOYALTY_NOT_CYLON' ? 'LOYALTY' : deck;
  return back ? art(`DeckId/${back}`, constantLabel('DeckId', deck), 'back', []) : null;
}

// Pegasus' damage tokens, which the materials have no back of: drawn round like Galactica's, a fire on black
function pegasusDamageBackArt(): ArtRef {
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 150 150">' +
    '<radialGradient id="f"><stop offset="0" stop-color="#ffd27a"/><stop offset="0.45" stop-color="#e0531f"/><stop offset="1" stop-color="#7a1a0a" stop-opacity="0"/></radialGradient>' +
    '<circle cx="75" cy="75" r="73" fill="#101215" stroke="#6b7682" stroke-width="2"/><circle cx="75" cy="75" r="46" fill="url(#f)"/>' +
    '<path d="M75 37 82 62 106 52 90 72 113 80 88 86 98 110 77 94 66 116 64 90 40 98 56 79 36 64 61 64z" fill="#f08a3a" fill-opacity="0.8"/></svg>';
  return { key: 'DeckId/PEGASUS_DAMAGE', url: `data:image/svg+xml,${encodeURIComponent(svg)}`, label: constantLabel('DeckId', 'PEGASUS_DAMAGE'), shape: 'back', preview: [] };
}

export function resourceArt(resource: Constant, value: number): ArtRef | null {
  return resource ? art(`Resource/${resource}_${value}`, `${constantLabel('Resource', resource)} ${value}`, 'dial', []) : null;
}

export function resourceIconArt(resource: Constant): ArtRef | null {
  const name = resource?.toLowerCase();
  if (!(RESOURCE_NAMES as string[]).includes(name ?? '')) {
    return null;
  }
  const own = name as ResourceName;
  return { key: `ResourceIcon/${resource}`, url: resourceIconUrl(own), label: resourceLabel(own), shape: 'icon', preview: [] };
}

// the line icons in the resource icons' light steel
const ICON_COLOR = '#cfe0f0';

export function stepIconArt(step: TurnStep): ArtRef {
  const svg = stepIconSvg(step, ICON_COLOR);
  return { key: `StepIcon/${step}`, url: `data:image/svg+xml,${encodeURIComponent(svg)}`, label: constantLabel('GameStep', step), shape: 'icon', preview: [] };
}

function glyphArt(key: string, name: GlyphName, label: string, preview: string[] = []): ArtRef {
  return { key, url: `data:image/svg+xml,${encodeURIComponent(glyphSvg(name, ICON_COLOR))}`, label, shape: 'icon', preview };
}

export function locationPinArt(location?: Constant): ArtRef {
  if (!location) {
    return glyphArt('Glyph/LOCATION', 'LOCATION', 'Location');
  }
  return glyphArt(`Glyph/LOCATION/${location}`, 'LOCATION', constantLabel('Location', location), locationArt(location)?.preview);
}

export function deckPlaceArt(place: 'DECK_TOP' | 'DECK_BOTTOM'): ArtRef {
  return glyphArt(`Glyph/${place}`, place, place === 'DECK_TOP' ? 'On top of the deck' : 'At the bottom of the deck');
}

export function trackArt(enumName: 'JumpPreparation' | 'Pursuit', position: Constant): ArtRef | null {
  return position ? art(`${enumName}/${position}`, constantLabel(enumName, position), 'track') : null;
}

export function skillCheckArt(type: Constant): ArtRef | null {
  return cardArt('CrisisCard', type) ?? cardArt('SuperCrisisCard', type) ?? locationArt(type);
}

export function enumArt(enumName: string, constant: Constant): ArtRef | null {
  if ((CARD_ENUMS as readonly string[]).includes(enumName)) {
    return cardArt(enumName as CardEnum, constant);
  }
  switch (enumName) {
    case 'Character':
      return characterArt(constant);
    case 'Location':
      return locationArt(constant);
    case 'ShipType':
      return shipArt(constant);
    case 'GalacticaDamage':
    case 'PegasusDamage':
    case 'BasestarDamage':
      return damageArt(enumName, constant);
    case 'SkillCheckType':
      return skillCheckArt(constant);
    case 'Risk':
      return cardArt('DestinationCard', constant);
    case 'SkillCardType':
      return skillTypeArt(constant);
    case 'CharacterAbility':
      return characterSheetArt(abilityCharacter(constant));
    case 'Title':
      return isTitleName(constant) && constant !== 'INFILTRATING' ? titleArt(constant) : null;
    default:
      return null;
  }
}

export function deckCards(deck: Constant): string | null {
  switch (deck) {
    case 'CRISIS':
      return 'CrisisCard';
    case 'SUPER_CRISIS':
      return 'SuperCrisisCard';
    case 'QUORUM':
      return 'QuorumCard';
    case 'LOYALTY':
    case 'LOYALTY_NOT_CYLON':
      return 'LoyaltyCard';
    case 'MUTINY':
      return 'MutinyCard';
    case 'MOTIVE':
      return 'MotiveCard';
    case 'DESTINATION':
      return 'DestinationCard';
    case 'GALACTICA_DAMAGE':
      return 'GalacticaDamage';
    case 'PEGASUS_DAMAGE':
      return 'PegasusDamage';
    case 'BASESTAR_DAMAGE':
      return 'BasestarDamage';
    case 'CIVILIAN_SHIPS':
      return 'CivilianShip';
    case null:
    case undefined:
      return null;
    default:
      return 'SkillCard';
  }
}

export function pileDeck(pile: Constant): string | null {
  switch (pile) {
    case 'SKILL':
      return null;
    case 'LOYALTY':
      return 'LOYALTY';
    case 'MOTIVE':
      return 'MOTIVE';
    case 'MUTINY':
      return 'MUTINY';
    case 'SUPER_CRISIS':
      return 'SUPER_CRISIS';
    default:
      return null;
  }
}

export function deckCardArt(card: unknown, kind: string | null): ArtRef | null {
  if (card && typeof card === 'object' && 'type' in card && 'value' in card) {
    return skillCardArt(card as SkillCardLike);
  }
  if (card && typeof card === 'object' && 'id' in card) {
    const ship = shipArt('CIVILIAN');
    return ship ? { ...ship, key: `${ship.key}#${(card as { id: number }).id}`, label: `Civilian ship #${(card as { id: number }).id}` } : null;
  }
  return typeof card === 'string' && kind ? enumArt(kind, card) : null;
}

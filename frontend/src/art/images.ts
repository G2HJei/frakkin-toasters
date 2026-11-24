// The images of materials-sorted at the repository's root: a folder per enum of the core, a file per constant, named
// like it. Vite copies them into the build and gives each its URL. A component that has no image falls back to its name.
import { constantLabel, skillCardLabel, type SkillCardLike } from '../labels/describe';
import { abilityCharacter } from '../wire/schema';
import admiralIcon from '../table/players/icons/ADMIRAL.png';
import cagIcon from '../table/players/icons/CAG.png';
import presidentIcon from '../table/players/icons/PRESIDENT.png';
import { RESOURCE_NAMES, resourceIconUrl, resourceLabel, type ResourceName } from '../table/board/resources';
import { stepIconSvg, type TurnStep } from '../table/turn/StepIcon';

// only the folders: the full-size board scan at the root isn't for the browser (the table's board is board.jpg)
const FILES = import.meta.glob<string>('./*/**/*.{png,jpg,gif,svg}', {
  base: '../../../materials-sorted',
  eager: true,
  query: '?url',
  import: 'default',
});

// "./CrisisCard/WATER_SABOTAGED.png" -> "CrisisCard/WATER_SABOTAGED"
const URLS = new Map<string, string>(Object.entries(FILES).map(([path, url]) => [path.replace(/^\.\//, '').replace(/\.\w+$/, ''), url]));

/** Every image by its path without the extension, e.g. `Character/player-token/LAURA_ROSLIN`. */
export function artPaths(): string[] {
  return [...URLS.keys()];
}

export type ArtShape = 'card' | 'token' | 'pilot' | 'location' | 'ship' | 'damage' | 'dial' | 'track' | 'back' | 'icon';

/** An image of a component: its file, its name as alt text, and what the preview shows large. */
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

/** A character's standee, with the character sheet as its preview. */
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

/** A title card, which lies in front of the player who holds it. */
export function titleArt(title: TitleName): ArtRef | null {
  return art(`Title/${title}`, TITLE_LABELS[title], 'card');
}

// The insignia of the title cards, cut out of their cards: the whole card is too small to read in a badge or a button.
// The Infiltration Card has none.
const TITLE_ICONS: Partial<Record<TitleName, string>> = { PRESIDENT: presidentIcon, ADMIRAL: admiralIcon, CAG: cagIcon };

export function titleIcon(title: TitleName): string | undefined {
  return TITLE_ICONS[title];
}

/** A title's insignia, with its card as the preview; the card where it has none. */
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

/** The die showing a value, 1 to 8. */
export function dieArt(value: number | null | undefined): ArtRef | null {
  return value ? art(`Die/${value}`, `Die: ${value}`, 'token', []) : null;
}

/** The back of every Skill Card, whatever its color. */
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

/** A Skill Card back in a color, hatched as the table draws a color's backs (`.card-back`), e.g. for a draw of it. */
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
  // the "You Are Not a Cylon" cards are Loyalty Cards, with their back
  const back = deck === 'LOYALTY_NOT_CYLON' ? 'LOYALTY' : deck;
  return back ? art(`DeckId/${back}`, constantLabel('DeckId', deck), 'back', []) : null;
}

/** A resource dial set to a value, 0 to 15. */
export function resourceArt(resource: Constant, value: number): ArtRef | null {
  return resource ? art(`Resource/${resource}_${value}`, `${constantLabel('Resource', resource)} ${value}`, 'dial', []) : null;
}

/** A resource's icon, as the board's gauges draw it: what an answer gains or loses. */
export function resourceIconArt(resource: Constant): ArtRef | null {
  const name = resource?.toLowerCase();
  if (!(RESOURCE_NAMES as string[]).includes(name ?? '')) {
    return null;
  }
  const own = name as ResourceName;
  return { key: `ResourceIcon/${resource}`, url: resourceIconUrl(own), label: resourceLabel(own), shape: 'icon', preview: [] };
}

/** A step of the turn, as the turn panel draws it, in the resource icons' light steel: the step an answer acts in. */
export function stepIconArt(step: TurnStep): ArtRef {
  const svg = stepIconSvg(step, '#cfe0f0');
  return { key: `StepIcon/${step}`, url: `data:image/svg+xml,${encodeURIComponent(svg)}`, label: constantLabel('GameStep', step), shape: 'icon', preview: [] };
}

/** A track with its token at a position. */
export function trackArt(enumName: 'JumpPreparation' | 'Pursuit', position: Constant): ArtRef | null {
  return position ? art(`${enumName}/${position}`, constantLabel(enumName, position), 'track') : null;
}

/** The card a skill check comes from: a Crisis Card, a Super Crisis Card, or the location of a location's check. */
export function skillCheckArt(type: Constant): ArtRef | null {
  return cardArt('CrisisCard', type) ?? cardArt('SuperCrisisCard', type) ?? locationArt(type);
}

/** The image of a constant of an enum, for the enums that have images. */
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
    case 'SkillCardType':
      return skillTypeArt(constant);
    case 'CharacterAbility':
      // the character sheet that prints it
      return characterSheetArt(abilityCharacter(constant));
    case 'Title':
      return isTitleName(constant) && constant !== 'INFILTRATING' ? titleArt(constant) : null;
    default:
      return null;
  }
}

/** The kind of card a deck holds: a card enum, a Skill Card, or a civilian ship. */
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

/** The deck a player's pile comes from, for the back of its hidden cards. */
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

/** A card of a deck or a pile, whatever its kind: an enum's constant, a Skill Card, or a civilian ship. */
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

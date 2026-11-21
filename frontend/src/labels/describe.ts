// The wording of the game: a name for every enum constant and type, and templates for events, facts and options. A
// template names fields in braces; a value is worded by its type in the schema.
import { humanizeConstant, humanizeType } from './humanize';
import { CONSTANT_LABELS, TYPE_LABELS } from './overrides';
import { recordNamed, type TypeRef } from '../wire/schema';
import type { FactProjection, Followup } from '../wire/types';

export interface LabelContext {
  playerName(playerNumber: number): string;
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

/** A value worded by its type in the schema. */
export function valueLabel(value: unknown, type: TypeRef | undefined, context: LabelContext): string {
  if (value === null || value === undefined) {
    return '—';
  }
  if (!type) {
    return typeof value === 'object' ? JSON.stringify(value) : String(value);
  }
  switch (type.t) {
    case 'int':
    case 'long':
      if (type.role === 'player') {
        return context.playerName(value as number);
      }
      if (type.role === 'ship') {
        return `ship #${value}`;
      }
      return String(value);
    case 'boolean':
      return value ? 'yes' : 'no';
    case 'string':
      return String(value);
    case 'class':
      return typeLabel(String(value));
    case 'enum':
      return constantLabel(type.name ?? '', value as string);
    case 'list':
    case 'set':
      return (value as unknown[]).map((element) => valueLabel(element, type.of as TypeRef, context)).join(', ') || 'none';
    case 'map':
      return (
        Object.entries(value as Record<string, unknown>)
          .map(([key, element]) => `${valueLabel(key, type.key, context)} ×${valueLabel(element, type.value, context)}`)
          .join(', ') || 'none'
      );
    case 'cards':
      return (value as unknown[]).map((card) => cardLabel(card)).join(', ') || 'none';
    case 'ref':
      return recordLabel(value as Record<string, unknown>, type.name ?? '', context);
    default:
      return JSON.stringify(value);
  }
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
    const ship = card as { id: number; fuelCost: number; moraleCost: number; populationCost: number };
    return `civilian ship #${ship.id}`;
  }
  return JSON.stringify(card);
}

function recordLabel(value: Record<string, unknown>, name: string, context: LabelContext): string {
  if (name === 'SkillCard') {
    return skillCardLabel(value as unknown as SkillCardLike);
  }
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

/* events */

const EVENT_TEMPLATES: Record<string, string> = {
  NoOpEvent: 'Pass',
  MoveEvent: '{playerNumber} moves to {destination}',
  SelectCharacterEvent: '{playerNumber} chooses {selectedCharacter}',
  DrawSkillCardsEvent: '{playerNumber} draws {selection}',
  DiscardSkillCardsEvent: '{playerNumber} discards {cards}',
  ChooseCrisisSectionEvent: '{playerNumber} chooses the {section} of {card}',
  ResolveCrisisCardEvent: 'Crisis: {card}',
  PlaceCivilianShipEvent: '{playerNumber} places a civilian ship at {location}',
  ResearchLabEvent: '{playerNumber} draws a {skillCardColor} card at the Research Lab',
  PutCrisisCardOnTopEvent: 'Setup: {card} on top of the Crisis deck',
  PutDestinationCardOnTopEvent: 'Setup: {card} on top of the Destination deck',
  PutSkillCardOnTopEvent: 'Setup: {card} on top of its deck',
  PutDestinyCardOnTopEvent: 'Setup: {card} on top of the Destiny deck',
  GiveSkillCardToPlayerEvent: 'Setup: {targetPlayerNumber} gets {card}',
  GiveQuorumCardToPresidentEvent: 'Setup: the President gets {card}',
  PresidentTitleActionEvent: '{playerNumber} draws a Quorum Card',
  MoveCharacterEvent: 'Setup: {character} goes to {destination}',
  PlaceShipEvent: 'Setup: a {shipType} at {location}',
  ChangeResourceEvent: 'Setup: {resource} changes by {amount}',
  SetJumpPreparationEvent: 'Setup: jump preparation at {position}',
  DamageLocationEvent: 'Setup: {location} is damaged',
  AssignTitleEvent: 'Setup: {character} becomes {title}',
};

function fill(template: string, value: Record<string, unknown>, recordName: string, context: LabelContext): string {
  const record = recordNamed(recordName);
  return template.replace(/\{(\w+)}/g, (_, field: string) => {
    const component = record?.components.find((candidate) => candidate.name === field);
    return valueLabel(value[field], component?.type, context);
  });
}

/** An event with its fields, by its template, or its name and fields. */
export function eventLabel(event: Record<string, unknown> | null | undefined, context: LabelContext): string {
  if (!event) {
    return '—';
  }
  const kind = String(event.kind);
  const template = EVENT_TEMPLATES[kind];
  if (template) {
    return fill(template, event, kind, context);
  }
  const card = recordNamed(kind)?.quorumCard;
  return card ? quorumCardLabel(event, kind, card, context) : recordLabel(event, kind, context);
}

// The record of a Quorum Card's event is the card: "Player 1 plays Arrest Order (target player number Player 3)".
function quorumCardLabel(event: Record<string, unknown>, kind: string, card: string, context: LabelContext): string {
  const components = recordNamed(kind)?.components ?? [];
  const player = components.find((component) => component.name === 'playerNumber');
  const parts = components
    .filter((component) => component !== player && event[component.name] !== null && event[component.name] !== undefined)
    .map((component) => `${humanizeType(component.name).toLowerCase()} ${valueLabel(event[component.name], component.type, context)}`);
  const played = `${valueLabel(event.playerNumber, player?.type, context)} plays ${constantLabel('QuorumCard', card)}`;
  return parts.length > 0 ? `${played} (${parts.join(', ')})` : played;
}

/* options of a choice */

/**
 * An option of a choice, as a button says it: "Pass" for a NoOpEvent, the action a decision asks for, the parts of
 * an AllOf joined with "then", otherwise the event's template.
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
      return eventLabel(event, context);
    }
    case 'AllOf':
      return (option.followups ?? []).map((part) => optionLabel(part, context)).join(' then ');
    case 'OneOf':
      return (option.options ?? []).map((part) => optionLabel(part, context)).join(' or ');
    default:
      return JSON.stringify(option);
  }
}

/* facts */

const FACT_TEMPLATES: Record<string, string> = {
  'PlayerFact.CharacterSelected': '{playerNumber} is {character}',
  'PlayerFact.CharacterMoved': '{character} moves to {to}',
  'PlayerFact.CardsGained': '{playerNumber} gains {cards}',
  'PlayerFact.CardsLost': '{playerNumber} loses {cards}',
  'PlayerFact.CardsSeen': '{playerNumber} looks at {cards}',
  'PlayerFact.LoyaltyCardSeen': '{playerNumber} looks at a Loyalty Card of {targetPlayerNumber}: {card}',
  'PlayerFact.LoyaltyCardRevealed': '{playerNumber} reveals {card}',
  'BoardFact.ResourceDecreased': '{resource} −{amount}',
  'BoardFact.ResourceIncreased': '{resource} +{amount}',
  'BoardFact.LocationDamaged': '{location} is damaged',
  'BoardFact.JumpPreparationIncreased': 'Jump preparation advances',
  'BoardFact.JumpPreparationDecreased': 'Jump preparation goes back',
  'ShipFact.ShipMoved': 'Ship #{shipId}: {from} → {to} {location}',
  'ShipFact.PilotChanged': 'Ship #{shipId} pilot: {pilot}',
  'GameFact.DieRolled': 'The die shows {value}',
  'GameFact.DieRollCardPlayed': '{card} changes the roll by {modifier}',
  'GameFact.CrisisCardPlayed': 'Crisis Card: {card}',
  'GameFact.StepChanged': 'Step: {step}',
  'GameFact.TurnStarted': 'Turn {turn} begins',
  'GameFact.CurrentPlayerChanged': '{playerNumber} is the current player',
  'GameFact.DestinationReached': 'The fleet reaches {card}',
  'GameFact.GameEnded': 'The {winner} win',
  'GameFact.MoraleLostToExecution': 'The execution cost the fleet morale',
  'TitleFact.PresidentAssigned': '{character} is the President',
  'TitleFact.AdmiralAssigned': '{character} is the Admiral',
  'TitleFact.CagAssigned': '{character} is the CAG',
  'SkillCheckFact.SkillCheckStarted': 'Skill check: {type}',
  'SkillCheckFact.TotalStrengthDetermined': 'The skill check totals {totalStrength}',
  'SkillCheckFact.ResultDetermined': 'The skill check: {result}',
  'DeckFact.CardsDiscarded': '{deck} discards {cards}',
  'DeckFact.CardsRemovedFromGame': '{cards} leave the game',
  'InterruptFact.InterruptCardResolved': '{card} takes effect',
};

export function factLabel(fact: FactProjection | null | undefined, context: LabelContext): string {
  if (!fact) {
    return '—';
  }
  if (fact.kind === 'Hidden') {
    const who = fact.playerNumber !== null ? `${context.playerName(fact.playerNumber)}: ` : '';
    const where = fact.deck ? ` (${constantLabel('DeckId', fact.deck)})` : fact.pile ? ` (${constantLabel('PlayerPile', fact.pile)})` : '';
    const colors = fact.colors
      ? ` — ${Object.entries(fact.colors).map(([color, count]) => `${count} ${constantLabel('SkillCardColor', color).toLowerCase()}`).join(', ')}`
      : '';
    return `${who}${typeLabel(fact.fact ?? '')}: ${fact.count} hidden${where}${colors}`;
  }
  const shown = fact.fact as unknown as Record<string, unknown> | null;
  if (!shown) {
    return '—';
  }
  const kind = String(shown.kind);
  const template = FACT_TEMPLATES[kind];
  return template ? fill(template, shown, kind, context) : recordLabel(shown, kind, context);
}

export const TEMPLATED_EVENTS = Object.keys(EVENT_TEMPLATES);
export const TEMPLATED_FACTS = Object.keys(FACT_TEMPLATES);

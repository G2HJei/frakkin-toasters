// The wording of the game: a name for every enum constant and type, and templates for events, facts and options. A
// template names fields in braces; a value is worded by its type in the schema.
import { humanizeConstant, humanizeType } from './humanize';
import { CONSTANT_LABELS, TYPE_LABELS } from './overrides';
import { recordNamed, typeNamed, type SourceRef, type TypeRef } from '../wire/schema';
import type { FactProjection, Followup } from '../wire/types';

export interface LabelContext {
  playerName(playerNumber: number): string;
  // a ship by its type and id, e.g. "Viper #1", where the table knows it
  shipName?(id: number): string;
  // the image a value names, for a name to hover (`Words`); the labels don't load images themselves
  artOf?(value: unknown, type: TypeRef): PartArt | null;
}

/** An image a part of a label names, as `art/images.ts` gives it. */
export interface PartArt {
  key: string;
  url: string;
  label: string;
  shape: string;
}

/** A piece of a label: its words, and the image of what it names. */
export interface Part {
  text: string;
  art?: PartArt | null;
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
        return context.shipName?.(value as number) ?? `ship #${value}`;
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
  if (name === 'ViperActivation') {
    return activationLabel(value, context);
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

// One activation of a viper (rule 11.1.1): a launch, a move, an attack or an escort.
function activationLabel(activation: Record<string, unknown>, context: LabelContext): string {
  const ship = (id: unknown) => context.shipName?.(id as number) ?? `ship #${id}`;
  if (activation.launchShipType) {
    return `Launch a ${constantLabel('ShipType', activation.launchShipType as string)} to ${constantLabel('Location', activation.launchLocation as string)}`;
  }
  const fighter = ship(activation.humanFighterId);
  if (activation.moveLocation) {
    return `${fighter} to ${constantLabel('Location', activation.moveLocation as string)}`;
  }
  if (activation.attackCylonShipId !== null && activation.attackCylonShipId !== undefined) {
    return `${fighter} attacks ${ship(activation.attackCylonShipId)}`;
  }
  if (activation.escortCivilianShipId !== null && activation.escortCivilianShipId !== undefined) {
    return `${fighter} escorts ${ship(activation.escortCivilianShipId)}`;
  }
  return fighter;
}

/* events */

const EVENT_TEMPLATES: Record<string, string> = {
  NoOpEvent: 'Pass',
  // an option of "Executive Order" reads "Move then Action"
  GrantEvent: '{grant}',
  CrisisStepEvent: 'Crisis step',
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
  GiveMutinyCardToPlayerEvent: 'Setup: {targetPlayerNumber} gets {card}',
  GiveLoyaltyCardToPlayerEvent: 'Setup: {targetPlayerNumber} gets {card}',
  DiscardPlayedMutinyCardEvent: '{card} goes to the Mutiny discard pile',
  RemoveMutinyCardFromGameEvent: '{card} leaves the game',
  SetTheAgendaLookEvent: "{playerNumber} looks at the President's Quorum Cards",
  SetTheAgendaChoiceEvent: '{playerNumber} puts 2 Quorum Cards at the bottom of the deck',
  ActivateUnmannedViperEvent: '{playerNumber} activates an unmanned viper',
  ShuffleCivilianShipIntoDeckEvent: 'Civilian ship {civilianShipId} is shuffled back into the deck',
  PresidentTitleActionEvent: '{playerNumber} draws a Quorum Card',
  LaunchNukeEvent: '{playerNumber} launches a nuke at {area}',
  NukeBasestarEvent: 'The nuke hits basestar {basestarId}',
  ActivateRaiderAreasEvent: 'The raiders activate one space area at a time',
  ActivateRaiderAreaEvent: 'The raiders at {area} activate',
  TiebreakInterruptCardsEvent: 'The current player keeps {keptCards}',
  PassCagTitleEvent: '{playerNumber} activates a viper and passes the CAG title to {targetPlayerNumber}',
  CagPilotActionEvent: '{playerNumber} activates an unmanned viper and acts again',
  RevealCylonEvent: '{playerNumber} reveals {card}',
  RevealCylonOnExecutionEvent: '{playerNumber} reveals {card}',
  FinishCylonRevealEvent: '{playerNumber} resurrects on the Resurrection Ship',
  ReturnToResurrectionShipEvent: '{playerNumber} ends the Infiltration on the Resurrection Ship',
  RevealMotiveCardEvent: '{playerNumber} reveals {card}',
  RevealPersonalGoalEvent: '{playerNumber} reveals {card}',
  LookAtAllLoyaltyCardsEvent: '{playerNumber} looks at the Loyalty Cards of {targetPlayerNumber}',
  ResolveFinalFiveCardEvent: '{holderPlayerNumber} reveals {card}, seen by {lookerPlayerNumber}',
  ResolveFinalFiveCardsEvent: 'The Final Five cards of {holderPlayerNumber} resolve',
  CylonPowerPlaceCenturionEvent: '{playerNumber} places a centurion',
  CylonPowerDamageGalacticaEvent: '{playerNumber} draws Galactica damage tokens',
  CylonPowerDecreaseJumpTrackEvent: '{playerNumber} moves the jump track back 2',
  CylonPowerDrawTreacheryEvent: 'Each human trades a Skill Card for a Treachery card',
  CylonPowerReduceMoraleEvent: '{playerNumber} reduces morale by 1',
  CylonPowerSendToBrigEvent: '{playerNumber} sends {targetPlayerNumber} to the Brig',
  CylonPowerSendToSickbayEvent: '{playerNumber} sends {targetPlayerNumber} to Sickbay',
  ChooseGalacticaDamageTokensEvent: '{playerNumber} resolves {resolved}',
  PrimacyEvent: '{playerNumber} uses Primacy',
  VisionsEvent: '{playerNumber} looks at a Loyalty Card of {targetPlayerNumber} and ends the Infiltration',
  DontTrustAnyoneEvent: '{playerNumber} draws 2 Super Crisis Cards',
  CultLeaderEvent: '{playerNumber} trades a miracle token with {targetPlayerNumber}',
  BroadcastEvent: '{playerNumber} raises {resource} by 2',
  StarPlayerEvent: '{playerNumber} trades Skill Cards for {color}',
  TreatmentEvent: '{playerNumber} treats {targetPlayerNumber}',
  TreatmentGiveEvent: '{playerNumber} gives 2 Skill Cards to {targetPlayerNumber}',
  DischargeOfAFirearmEvent: '{playerNumber} executes {targetPlayerNumber}',
  CoupEvent: '{playerNumber} takes the Admiral title',
  CylonDetectorEvent: '{playerNumber} looks at every Loyalty Card of {targetPlayerNumber}',
  BlindJumpEvent: '{playerNumber} jumps the fleet blind',
  SkilledPoliticianEvent: '{playerNumber} draws 4 Quorum Cards',
  SkilledPoliticianChoiceEvent: '{playerNumber} resolves a Quorum Card',
  CagAbilityEvent: '{playerNumber} activates unmanned vipers',
  UnmannedViperRoundEvent: '{playerNumber} may activate another unmanned viper',
  CrypticMessageEvent: '{playerNumber} swaps Skill Cards with {targetPlayerNumber}',
  SacrificeEvent: '{playerNumber} goes to Sickbay to destroy ships',
  OrganizedEvent: '{playerNumber} acts at 3 locations',
  OrganizedLocationEvent: '{playerNumber} acts at {location}',
  OrganizedNextEvent: '{playerNumber} chooses the next location',
  AttorneyEvent: '{playerNumber} frees {targetPlayerNumber} to {destination}',
  DeclareMartialLawEvent: '{playerNumber} makes the Admiral President',
  ResoluteEvent: '{playerNumber} acts at {location}',
  QuarantineEvent: '{playerNumber} looks at every civilian ship',
  IntimateEvent: '{playerNumber} takes a Skill Card from {targetPlayerNumber}, who draws {deck}',
  AggressiveTacticsEvent: '{playerNumber} takes two actions',
  GlimpseTheFaceOfGodEvent: '{playerNumber} takes the top 2 Destiny cards',
  GlimpseTheFaceOfGodPlaceEvent: '{playerNumber} puts 2 cards on the Destiny deck',
  HereticDiscardEvent: '{playerNumber} discards {card} to leave the Resurrection Ship',
  PoliticallyAdroitEvent: '{playerNumber} gives a Skill Card to {targetPlayerNumber}',
  KleptomaniaBrigEvent: '{playerNumber} goes to the Brig at the end of the turn',
  EmotionallyFragileEvent: 'Morale is down to 2 or less: {playerNumber} is executed',
  ReligiousVisionsEvent: '{playerNumber} resolves {crisisCardToPlay} and puts the other card at the bottom',
  DelusionalIntuitionEvent: '{playerNumber} draws a Skill Card of any type',
  SecretDestinyEvent: '{playerNumber} discards {card} and draws another Crisis Card',
  ReconEvent: '{playerNumber} looks at the top Crisis Card',
  MoralCompassEvent: '{playerNumber} changes the choice on {card} to its {section}',
  ResolveCrisisSectionEvent: 'The chosen {section} of {card} resolves',
  ChooseADifferentPathEvent: '{playerNumber} makes the current player discard 5 Skill Cards instead',
  MysteriousIntuitionEvent: '{playerNumber} decides the skill check',
  FastLearnerEvent: '{playerNumber} takes the top {color} cards',
  FastLearnerIntoCheckEvent: '{playerNumber} puts the cards into the skill check',
  BlindDevotionEvent: '{playerNumber} makes {color} count 0 in the skill check',
  QuickFixEvent: '{playerNumber} discards {card} from the skill check',
  HumanDelusionEvent: '{playerNumber} adds cards to the skill check',
  LogicBoundEvent: '{playerNumber} shows {card} in the skill check',
  CommandAuthorityEvent: '{playerNumber} takes the cards of the skill check',
  OfferCommandAuthorityEvent: 'The cards of the skill check may be taken',
  CylonHatredEvent: '{playerNumber} makes the check 3 easier',
  FriendsInLowPlacesEvent: '{playerNumber} changes the difficulty by {adjustment}',
  IntolerantEvent: '{playerNumber} has {targetPlayerNumber} executed instead',
  ModificationsEvent: '{playerNumber} picks {activation} instead of the icon',
  DeceitfulEvent: '{playerNumber} discards 1 card fewer',
  ForcedDiscardEvent: '{discarderPlayerNumber} has to discard',
  ForLoveEvent: '{playerNumber} draws a Treachery card so {discarderPlayerNumber} discards 1 fewer',
  OfferMementoEvent: '{playerNumber} may draw Piloting cards',
  MementoEvent: '{playerNumber} draws 3 Piloting cards and keeps 2',
  EscortEvent: '{playerNumber} loses viper {viperId} to save civilian ship {civilianShipId}',
  NecessaryStepsEvent: '{playerNumber} picks the Mutiny Card {drawerPlayerNumber} gets',
  OfferAdaptableEvent: 'Skill Cards may be drawn after the Quorum Card',
  AdaptableEvent: '{playerNumber} draws 2 Skill Cards',
  NoOneGetsLeftBehindEvent: '{playerNumber} trades 1 fuel for 2 population',
  ManipulativeEvent: '{playerNumber} takes the {title} title for the turn',
  MaintenanceEngineerEvent: '{playerNumber} takes another action',
  OfferForwardThinkerEvent: '{playerNumber} may activate his location',
  ForwardThinkerEvent: '{playerNumber} activates his location',
  OfferAlertViperPilotEvent: 'A launched viper may be piloted',
  AlertViperPilotEvent: '{playerNumber} pilots viper {viperId}',
  RaptorPilotEvent: '{playerNumber} looks at the top 2 cards of {deck}',
  OfferDutifulEvent: '{playerNumber} may activate {location} again',
  DutifulEvent: '{playerNumber} discards {card} to activate {location} again',
  QuarantineChoiceEvent: 'Civilian ship {civilianShipId} is replaced',
  UnconventionalTacticsEvent: '{playerNumber} trades 1 population for {resource}',
  AbusePowerEvent: '{playerNumber} draws 4 Mutiny Cards',
  AbusePowerChoiceEvent: '{playerNumber} plays a Mutiny Card',
  InfluentialEvent: '{playerNumber} looks at the top Quorum Cards',
  ReorderTopQuorumCardsEvent: '{playerNumber} puts the Quorum Cards back',
  MeticulousEvent: '{playerNumber} ends the Infiltration at {location}',
  EndRemoteActionEvent: 'The action from afar is over',
  EndInfiltrationEvent: '{playerNumber} goes back to the Resurrection Ship',
  PopularInfluenceChoiceEvent: '{playerNumber} gives {given} to the President and keeps {other}',
  DiscardHeldQuorumCardEvent: '{playerNumber} discards {card}',
  MoveCharacterEvent: 'Setup: {character} goes to {destination}',
  PlaceShipEvent: 'Setup: a {shipType} at {location}',
  ChangeResourceEvent: 'Setup: {resource} changes by {amount}',
  SetJumpPreparationEvent: 'Setup: jump preparation at {position}',
  DamageLocationEvent: 'Setup: {location} is damaged',
  AssignTitleEvent: 'Setup: {character} becomes {title}',
  GiveSuperCrisisCardToPlayerEvent: 'Setup: {targetPlayerNumber} gets {card}',
  CapricaPlaySuperCrisisCardEvent: '{playerNumber} plays {superCrisisCard} at Caprica',
  ResolveSuperCrisisCardEvent: 'Super Crisis: {card}',
  ChooseSuperCrisisSectionEvent: '{playerNumber} chooses the {section} of {card}',
  DiscardSuperCrisisCardEvent: '{card} goes to the Super Crisis discard pile',
  HumanPrisonerTakeHandEvent: "{playerNumber} takes {targetPlayerNumber}'s Skill Cards",
  ReplaceDestinyDeckEvent: 'The Destiny deck is rebuilt from {count} {color} cards',
  DamageBattlestarEvent: 'Galactica is hit',
  DamageChosenViperEvent: '{playerNumber} picks ship {humanFighterId} to be damaged',
  DestroyPegasusEvent: 'Pegasus is destroyed',
};

// A template's words, each field a part of its own with the image of what it names.
function fill(template: string, value: Record<string, unknown>, recordName: string, context: LabelContext): Part[] {
  const record = recordNamed(recordName);
  const parts: Part[] = [];
  let at = 0;
  for (const match of template.matchAll(/\{(\w+)}/g)) {
    if (match.index > at) {
      parts.push({ text: template.substring(at, match.index) });
    }
    const component = record?.components.find((candidate) => candidate.name === match[1]);
    const field = value[match[1]];
    const art = component && field !== null && field !== undefined && !Array.isArray(field) ? (context.artOf?.(field, component.type) ?? null) : null;
    parts.push({ text: valueLabel(field, component?.type, context), art });
    at = match.index + match[0].length;
  }
  if (at < template.length) {
    parts.push({ text: template.substring(at) });
  }
  return parts;
}

/** An event with its fields, by its template, or its name and fields. */
export function eventLabel(event: Record<string, unknown> | null | undefined, context: LabelContext): string {
  return partsText(eventParts(event, context));
}

/** An event's label in parts, the names it fills in with their images. */
export function eventParts(event: Record<string, unknown> | null | undefined, context: LabelContext): Part[] {
  if (!event) {
    return [{ text: '—' }];
  }
  const kind = String(event.kind);
  const template = EVENT_TEMPLATES[kind];
  if (template) {
    return fill(template, event, kind, context);
  }
  return [{ text: eventFallback(event, kind, context) }];
}

function eventFallback(event: Record<string, unknown>, kind: string, context: LabelContext): string {
  const quorumCard = recordNamed(kind)?.quorumCard;
  if (quorumCard) {
    return playedCardLabel(event, kind, 'QuorumCard', quorumCard, context);
  }
  const mutinyCard = recordNamed(kind)?.mutinyCard;
  return mutinyCard ? playedCardLabel(event, kind, 'MutinyCard', mutinyCard, context) : recordLabel(event, kind, context);
}

// The record of a Quorum or a Mutiny Card's event is the card: "Player 1 plays Arrest Order (target player number
// Player 3)".
function playedCardLabel(event: Record<string, unknown>, kind: string, enumName: string, card: string, context: LabelContext): string {
  const components = recordNamed(kind)?.components ?? [];
  const player = components.find((component) => component.name === 'playerNumber');
  const parts = components
    .filter((component) => component !== player && event[component.name] !== null && event[component.name] !== undefined)
    .map((component) => `${humanizeType(component.name).toLowerCase()} ${valueLabel(event[component.name], component.type, context)}`);
  const played = `${valueLabel(event.playerNumber, player?.type, context)} plays ${constantLabel(enumName, card)}`;
  return parts.length > 0 ? `${played} (${parts.join(', ')})` : played;
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
  'PlayerFact.FinalFiveCardShown': '{playerNumber} shows {card}',
  'PlayerFact.AbilityUsed': '{playerNumber} uses {ability}',
  'PlayerFact.AbilityTriggered': '{ability} of {playerNumber} may answer it',
  'PlayerFact.AbilityTriggerCleared': '{ability} of {playerNumber} was asked',
  'GameFact.RemoteActionAllowed': '{playerNumber} may act at {location} from anywhere',
  'GameFact.RemoteActionEnded': 'The action from afar is over',
  'BoardFact.ResourceDecreased': '{resource} −{amount}',
  'BoardFact.ResourceIncreased': '{resource} +{amount}',
  'BoardFact.LocationDamaged': '{location} is damaged',
  'BoardFact.PegasusDestroyed': 'Pegasus leaves the game',
  'BoardFact.JumpPreparationIncreased': 'Jump preparation advances',
  'BoardFact.JumpPreparationDecreased': 'Jump preparation goes back',
  'ShipFact.ShipMoved': 'Ship #{shipId}: {from} → {to} {location}',
  'ShipFact.PilotChanged': 'Ship #{shipId} pilot: {pilot}',
  'GameFact.DieRolled': 'The die shows {value}',
  'GameFact.DieRollCardPlayed': '{card} changes the roll by {modifier}',
  'GameFact.DieRollAbilityUsed': '{ability} acts on the roll',
  'GameFact.DieRollReplaced': 'The roll is {value} instead of the die',
  'GameFact.CrisisCardPlayed': 'Crisis Card: {card}',
  'GameFact.StepChanged': 'Step: {step}',
  'GameFact.TurnStarted': 'Turn {turn} begins',
  'GameFact.CurrentPlayerChanged': '{playerNumber} is the current player',
  'GameFact.DestinationReached': 'The fleet reaches {card}',
  'GameFact.GameEnded': 'The {winner} win',
  'GameFact.MoraleLostToExecution': 'The execution cost the fleet morale',
  'GameFact.TurnLimitUsed': '{playerNumber} used {limit} this turn',
  'GameFact.StartLocationNoted': 'The turn starts at {location}',
  'GameFact.SentToBrigAtEndOfTurn': '{playerNumber} goes to the Brig at the end of the turn',
  'GameFact.ActionCostPaid': 'The cost of the action is paid',
  'GameFact.TitleBorrowed': 'The {title} title is taken from {previousHolder} for the turn',
  'TitleFact.PresidentAssigned': '{character} is the President',
  'TitleFact.AdmiralAssigned': '{character} is the Admiral',
  'TitleFact.CagAssigned': '{character} is the CAG',
  'TitleFact.QuorumCardPutInPlay': '{card} stays in play with {holderPlayerNumber}',
  'TitleFact.QuorumCardLeftPlay': '{card} leaves play',
  'SkillCheckFact.SkillCheckStarted': 'Skill check: {type}',
  'SkillCheckFact.PlayedBy': '{playerNumber} played the Super Crisis Card',
  'SkillCheckFact.TotalStrengthDetermined': 'The skill check totals {totalStrength}',
  'SkillCheckFact.ResultDetermined': 'The skill check: {result}',
  'SkillCheckFact.DifficultyAdjusted': 'The difficulty changes by {delta}',
  'SkillCheckFact.PoliticsCountPositive': 'Politics counts positive in this check',
  'SkillCheckFact.StrengthZeroed': '{color} counts 0 in this check',
  'SkillCheckFact.CardsDivided': 'The cards are sorted into their piles',
  'SkillCheckFact.CardsLookedAt': '{playerNumber} looks at the cards {targetPlayerNumber} played: {cards}',
  'DeckFact.CardsDiscarded': '{deck} discards {cards}',
  'DeckFact.CardsRemovedFromGame': '{cards} leave the game',
  'InterruptFact.InterruptCardResolved': '{card} takes effect',
  'InterruptFact.RollAbilityResolved': '{ability} takes effect',
};

export function factLabel(fact: FactProjection | null | undefined, context: LabelContext): string {
  return partsText(factParts(fact, context));
}

// What happened to a ship, by where it went: destroyed, damaged, launched, landed or moved in space.
function shipMovedParts(fact: Record<string, unknown>, context: LabelContext): Part[] | null {
  const ship = context.shipName?.(fact.shipId as number) ?? `Ship #${fact.shipId}`;
  const from = fact.from as string | null;
  const to = fact.to as string | null;
  const inSpace = (zone: string | null) => zone === 'GALACTICA_SPACE' || zone === 'CYLON_FLEET_SPACE' || zone === 'BOARDING_PARTY';
  const where = (location: unknown) => [{ text: constantLabel('Location', location as string), art: context.artOf?.(location, { t: 'enum', nullable: false, name: 'Location' }) ?? null }];
  if (inSpace(from) && (to === 'REMOVED' || to === 'CYLON_SUPPLY' || to === 'CENTURION_SUPPLY')) {
    return [{ text: `${ship} is destroyed` }];
  }
  if (to === 'DAMAGED') {
    return [{ text: `${ship} is damaged` }];
  }
  if (to === 'RESERVES' && inSpace(from)) {
    return [{ text: `${ship} lands` }];
  }
  if (to === 'GALACTICA_SPACE' && fact.location) {
    return [{ text: `${ship} ${from === 'GALACTICA_SPACE' ? 'moves' : 'launches'} to ` }, ...where(fact.location)];
  }
  return null;
}

/** A fact's label in parts, the names it fills in with their images. */
export function factParts(fact: FactProjection | null | undefined, context: LabelContext): Part[] {
  if (!fact) {
    return [{ text: '—' }];
  }
  if (fact.kind === 'Hidden') {
    const who = fact.playerNumber !== null ? `${context.playerName(fact.playerNumber)}: ` : '';
    const where = fact.deck ? ` (${constantLabel('DeckId', fact.deck)})` : fact.pile ? ` (${constantLabel('PlayerPile', fact.pile)})` : '';
    const colors = fact.colors
      ? ` — ${Object.entries(fact.colors).map(([color, count]) => `${count} ${constantLabel('SkillCardColor', color).toLowerCase()}`).join(', ')}`
      : '';
    return [{ text: `${who}${typeLabel(fact.fact ?? '')}: ${fact.count} hidden${where}${colors}` }];
  }
  const shown = fact.fact as unknown as Record<string, unknown> | null;
  if (!shown) {
    return [{ text: '—' }];
  }
  const kind = String(shown.kind);
  const ship = kind === 'ShipFact.ShipMoved' ? shipMovedParts(shown, context) : null;
  if (ship) {
    return ship;
  }
  const template = FACT_TEMPLATES[kind];
  return template ? fill(template, shown, kind, context) : [{ text: recordLabel(shown, kind, context) }];
}

export const TEMPLATED_EVENTS = Object.keys(EVENT_TEMPLATES);
export const TEMPLATED_FACTS = Object.keys(FACT_TEMPLATES);

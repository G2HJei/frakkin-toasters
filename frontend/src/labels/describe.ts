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

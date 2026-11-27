// The log as a story: the scopes and choices of the journal as lines, by turn and step, each under the player it is
// about. A choice is told by the event it picked, bookkeeping is left out, and an event whose facts say everything is
// told by them.
import { recordArt, unique, factArt } from '../../art/refs';
import type { ArtRef } from '../../art/images';
import {
  chosenParts,
  constantLabel,
  eventParts,
  eventSource,
  sourcePart,
  hiddenEventParts,
  isPlayerEvent,
  optionLabel,
  partsText,
  shipMovesParts,
  type ShipMove,
  unnamedSource,
  withoutSubject,
  type Part,
} from '../../labels/describe';
import { factKind, factSentences, namedCards, repeats, sameShipMove, sentenceParts, type FactContext, type Sentence } from '../../labels/facts';
import { recordNamed } from '../../wire/schema';
import type { FactProjection, LogEntry, ScopeLog } from '../../wire/types';

export interface LogLine {
  key: string;
  // the scope or the choice it tells, for what a click lights on the board
  entry: LogEntry;
  // the player it is about, named above it; null for what happens to the game
  actor: number | null;
  // named above it by the user in his seat instead of his character: the choice of a character
  namesUser?: boolean;
  words: Part[];
  // the card or ability it comes from, when its words don't name it
  source: Part | null;
  facts: Part[][];
  art: ArtRef[];
  automatic: boolean;
  // ships that moved alike, which the next such line joins
  ships?: { names: Part[]; move: ShipMove };
}

export interface StepStory {
  // the step the lines happen in, null before the first step of a turn
  step: string | null;
  lines: LogLine[];
}

export interface TurnStory {
  // 0 for the setup
  turn: number;
  // the current player of the turn
  player: number | null;
  steps: StepStory[];
}

// Events told by their facts alone: the steps, the mechanics of a skill check, the rolls, what a card's or a roll's
// result did. Their words would only say that something happens; the facts say what.
const TOLD = new Set([
  'StartTurnEvent',
  'ReceiveSkillsStepEvent',
  'MovementStepEvent',
  'ActionStepEvent',
  'CrisisStepEvent',
  'ActivateCylonShipsStepEvent',
  'PrepareForJumpStepEvent',
  'DiscardStepEvent',
  'EndOfMovementStepEvent',
  'EndOfActionStepEvent',
  'ReturnToCrisisStepEvent',
  'EndTurnEvent',
  'ResetActingPlayerEvent',
  'MakeSkillCheckEvent',
  'PlayFromDestinyDeckEvent',
  'RevealCardsBeforeSkillCheckEvent',
  'ResolveCardsBeforeSkillCheckEvent',
  'ResolveConsequencesEvent',
  'TotalSkillCheckStrengthEvent',
  'DetermineSkillCheckResultEvent',
  'ResolveSkillCheckResultEvent',
  'EndSkillCheckEvent',
  'DrawAndResolveRecklessCardsEvent',
  'ABetterMachineEvent',
  'ASecondChanceEvent',
  'BaitEvent',
  'DradisContactEvent',
  'IronWillEvent',
  'InstallUpgradesEvent',
  'PersonalVicesEvent',
  'RedTapeEvent',
  'TrustInstinctsEvent',
  'ViolentOutburstsEvent',
  'RollDieEvent',
  'OpenInterruptWindowEvent',
  'CloseInterruptWindowEvent',
  'DetermineNextPresidentEvent',
  'DetermineNextAdmiralEvent',
  'DetermineNextCagEvent',
  'MakePresidentEvent',
  'MakeAdmiralEvent',
  'MakeCagEvent',
  'LoseResourceEvent',
  'GainResourceEvent',
  'IncreaseJumpPreparationEvent',
  'DecreaseJumpPreparationEvent',
  'ResetJumpPreparationEvent',
  'AdvancePursuitTrackEvent',
  'ActivateRaiderEvent',
  'ActivateHeavyRaidersAndCenturionsEvent',
  'ActivateCylonBasestarEvent',
  'ActivateCylonBasestarsEvent',
  'ActivateRaidersEvent',
  'DamageHumanFighterEvent',
  'DamageGalacticaEvent',
  'DamagePegasusEvent',
  'DamageBasestarEvent',
  'DestroyCenturionEvent',
  'DestroyHeavyRaiderEvent',
  'DestroyRaidersEvent',
  'DestroyRaptorEvent',
  'DestroyCivilianShipEvent',
  // one ship per scope, each queuing the rest: its words would count 2, then 1 more
  'DrawAndDestroyCivilianShipsEvent',
  'MoveCivilianShipEvent',
  'ShuffleCivilianShipIntoDeckEvent',
  'SendToBrigEvent',
  'SendToSickbayEvent',
  'ResolveCrisisCardEvent',
  'ResolveCrisisSectionThenEvent',
  'ResolveCrisisRollEvent',
  'DiscardPlayedQuorumCardEvent',
  'DiscardPlayedMutinyCardEvent',
  'RemoveMutinyCardFromGameEvent',
  'DiscardSuperCrisisCardEvent',
  'ResolveDestinationCardEvent',
  'ObjectiveInstructionsEvent',
  'KeepCrisisCardInPlayEvent',
  'FinishSleeperAgentPhaseEvent',
]);

// What a card's or a die's result did, a Crisis Card's pass or fail
const TOLD_NAME = /(Result|Pass|Fail|FailResult)Event$/;

// Events about no player that say something even when they change nothing yet: an attack before its roll, a hit
// before its damage, an ability of a skill check before what it does.
const KEEP_EMPTY = new Set([
  'JumpingTheFleetEvent',
  'AttackGalacticaEvent',
  'AttackViperEvent',
  'DamageBattlestarEvent',
  'ResolveSkillCheckAbilityEvent',
  'ActivateRaiderAreaEvent',
  'DestroyCivilianShipsEvent',
  'DamageVipersEvent',
  'SleeperAgentPhaseEvent',
  'EndGameEvent',
  'CylonsWinEvent',
  'DestroyPegasusEvent',
]);

// Events about a player that only ask him something: the answer is the story, unless they changed something.
const ASKS = new Set([
  'DetermineSkillCheckAbilitiesOrderEvent',
  'ReplaceMutinousPresidentEvent',
  'GiveTitlesToNextInLineEvent',
  'ViperActivationRoundEvent',
  'UnmannedViperRoundEvent',
  'OrganizedNextEvent',
  'ResolveFinalFiveCardsEvent',
  'ForcedDiscardEvent',
]);

// The facts that change the table, which a line lists first; the rest is mechanics (cards drawn and discarded, decks
// shuffled).
const STORY = new Set([
  'BoardFact.ResourceDecreased',
  'BoardFact.ResourceIncreased',
  'BoardFact.LocationDamaged',
  'BoardFact.LocationRepaired',
  'BoardFact.PegasusDestroyed',
  'BoardFact.JumpPreparationIncreased',
  'BoardFact.JumpPreparationDecreased',
  'BoardFact.JumpPreparationReset',
  'BoardFact.BoardingPartyAdvanced',
  'BoardFact.PursuitAdvanced',
  'GameFact.DieRolled',
  'GameFact.DieRollReplaced',
  'GameFact.DieRollCardPlayed',
  'GameFact.CrisisCardPlayed',
  'GameFact.DestinationReached',
  'GameFact.GameEnded',
  'GameFact.TurnEndedEarly',
  'GameFact.NukeDiscarded',
  'PlayerFact.CharacterMoved',
  'PlayerFact.CharacterRemoved',
  'PlayerFact.LoyaltyCardRevealed',
  'PlayerFact.FinalFiveCardShown',
  'PlayerFact.MotiveCardRevealed',
  'PlayerFact.AbilityUsed',
  'SkillCheckFact.SkillCheckStarted',
  'SkillCheckFact.DifficultyAdjusted',
  'SkillCheckFact.TotalStrengthDetermined',
  'SkillCheckFact.ResultDetermined',
  'TitleFact.PresidentAssigned',
  'TitleFact.AdmiralAssigned',
  'TitleFact.CagAssigned',
  'TitleFact.QuorumCardPutInPlay',
  'InterruptFact.InterruptCardResolved',
  'InterruptFact.InterruptCardReturned',
  'InterruptFact.RollAbilityResolved',
  'InterruptFact.RollAbilityWithdrawn',
]);

// The answers to an interrupt window, secret while it is open: its close tells what each player played.
const SECRET_ANSWERS = new Set(['PlayInterruptCardEvent', 'UseRollAbilityEvent']);

// An interrupt card that goes back to the hand in a tiebreak, told on its own line as the window's other cards are.
function returnsInterruptCard(one: Sentence): boolean {
  return one.facts.some((fact) => factKind(fact) === 'InterruptFact.InterruptCardReturned');
}

const RESOURCES = new Set(['BoardFact.ResourceDecreased', 'BoardFact.ResourceIncreased', 'GameFact.LossReduced']);

// What the fleet gains or loses is told on its own line, as the facts under a line are a sandbox's only: "is
// executed", then "The fleet loses 1 morale".
function changesResource(one: Sentence): boolean {
  return one.facts.some((fact) => RESOURCES.has(factKind(fact)));
}

// Told on a line of its own after the line of its scope.
function toldApart(one: Sentence): boolean {
  return returnsInterruptCard(one) || changesResource(one);
}

/** Whether a fact changes the table: a ship that is destroyed, damaged or launched does. */
export function isStoryFact(fact: FactProjection): boolean {
  const kind = factKind(fact);
  if (kind === 'ShipFact.ShipMoved' && fact.kind === 'Shown') {
    const ship = fact.fact as unknown as { from: string | null; to: string | null };
    // a civilian ship drawn from the deck is destroyed, one placed from it only appears
    const placed = ship.from === 'CIVILIAN_DECK' && ship.to !== 'REMOVED';
    return ship.to !== ship.from && ship.to !== 'CYLON_FLEET_SPACE' && ship.from !== 'HUMAN_SUPPLY' && !placed;
  }
  return STORY.has(kind);
}

function shownFact(entry: LogEntry, kind: string): Record<string, unknown> | null {
  if (entry.kind !== 'ScopeLog') {
    return null;
  }
  const found = (entry.facts ?? []).find((fact) => fact.kind === 'Shown' && fact.fact?.kind === kind);
  return found?.kind === 'Shown' ? (found.fact as unknown as Record<string, unknown>) : null;
}

// The tiebreaks of the current player: the cards he keeps when two conflict, before a skill check or in an interrupt
// window, told under his name whether he chose or not.
const KEPT_BY_CURRENT_PLAYER = new Set(['TiebreakCardsBeforeSkillCheckEvent', 'TiebreakInterruptCardsEvent']);

// The facts that give a title, borrowed for a turn ("Manipulative") and given back too.
const TITLES_ASSIGNED: Record<string, string> = { 'TitleFact.PresidentAssigned': 'PRESIDENT', 'TitleFact.AdmiralAssigned': 'ADMIRAL', 'TitleFact.CagAssigned': 'CAG' };

// The damage tokens a player chooses among: his words say what they damage, so no token is pictured after them.
const DAMAGE_CHOSEN = new Set(['ChooseGalacticaDamageEvent', 'ChooseGalacticaDamageTokensEvent']);

export function logStory(entries: LogEntry[], table: FactContext): TurnStory[] {
  const learned = withLearnedShips(table, learnedShips(entries));
  // who held each title when each line happened, by the facts that gave it; before the first, the table's holder
  const holders = new Map<string, number | null>();
  const context: FactContext = { ...learned, titleHolder: (title) => (holders.has(title) ? (holders.get(title) ?? null) : (learned.titleHolder?.(title) ?? null)) };
  const scopes = new Map<number, ScopeLog>();
  for (const entry of entries) {
    if (entry.kind === 'ScopeLog') {
      scopes.set(entry.scopeId, entry);
    }
  }
  const turns: TurnStory[] = [{ turn: 0, player: null, steps: [{ step: null, lines: [] }] }];
  const state = { cardsBefore: false };
  // the player who picked the next scope's event in a choice
  let chosenBy: number | null = null;
  entries.forEach((entry, index) => {
    if (entry.kind === 'ChoiceLog') {
      const next = entries[index + 1];
      const option = entry.option;
      const event = option?.kind === 'Single' ? (option.event as unknown as Record<string, unknown> | null) : null;
      if (event?.kind === 'PlayerDecisionEvent') {
        // the answer to the decision follows and tells it
        return;
      }
      if (event && next?.kind === 'ScopeLog' && next.event === event.kind && next.parentId === entry.scopeId) {
        chosenBy = entry.chooser;
        return;
      }
      addLine(turns, {
        key: `choice-${entry.scopeId}-${index}`,
        entry,
        actor: entry.chooser,
        words: [{ text: option ? `chooses: ${optionLabel(option, context)}` : 'makes a choice' }],
        source: null,
        facts: [],
        art: [],
        automatic: false,
      });
      return;
    }
    const started = shownFact(entry, 'GameFact.TurnStarted');
    if (started) {
      turns.push({ turn: started.turn as number, player: entry.actor, steps: [{ step: null, lines: [] }] });
    }
    const changed = shownFact(entry, 'GameFact.StepChanged');
    if (changed) {
      turns[turns.length - 1].steps.push({ step: changed.step as string, lines: [] });
    }
    // a tiebreak with one way to keep the cards asks nobody, and the current player keeps them all the same
    const by = chosenBy ?? (KEPT_BY_CURRENT_PLAYER.has(entry.event ?? '') ? turns[turns.length - 1].player : null);
    for (const line of scopeLines(entry, index, by, scopes, state, context)) {
      addLine(turns, line);
    }
    for (const fact of entry.facts ?? []) {
      const title = fact.kind === 'Shown' ? TITLES_ASSIGNED[factKind(fact)] : undefined;
      if (title) {
        const character = (fact.fact as unknown as { character?: string | null }).character ?? null;
        holders.set(title, character ? (context.playerOf?.(character) ?? null) : null);
      }
    }
    chosenBy = null;
  });
  return turns
    .map((turn) => ({
      ...turn,
      steps: turn.steps.filter((step) => step.lines.length > 0).map((step) => ({ ...step, lines: step.lines.map((line) => withoutShownArt(withoutOwnPortrait(line))) })),
    }))
    .filter((turn) => turn.turn > 0 || turn.steps.some((step) => step.lines.length > 0));
}

// The portrait of the line's own player leads it already: where its words name him again, they do without it. A title
// he holds keeps it ("makes the [Saul Tigh] Admiral President"), as its words don't say it is him.
function withoutOwnPortrait(line: LogLine): LogLine {
  if (line.actor === null) {
    return line;
  }
  const own = (parts: Part[]) => parts.map((part) => (part.icon && part.player === line.actor && !part.title ? { ...part, icon: null } : part));
  return { ...line, words: own(line.words), facts: line.facts.map(own) };
}

// The images a line's words show already, as a name to hover or an icon, aren't repeated after them.
function withoutShownArt(line: LogLine): LogLine {
  const shown = new Set([...line.words, ...(line.source ? [line.source] : [])].flatMap((part) => [part.art?.key, part.icon?.key]).filter((key) => key));
  return shown.size === 0 ? line : { ...line, art: line.art.filter((ref) => !shown.has(ref.key)) };
}

// The fields that name a ship of one type, and the events whose ships are all of one type.
const SHIP_FIELDS: [RegExp, string][] = [
  [/^raider/, 'RAIDER'],
  [/basestar/i, 'BASESTAR'],
  [/^centurion/, 'CENTURION'],
  [/civilian/i, 'CIVILIAN'],
];
const SHIP_EVENTS: Record<string, string> = { DestroyRaidersEvent: 'RAIDER', DestroyHeavyRaiderEvent: 'HEAVY_RAIDER', DestroyCivilianShipEvent: 'CIVILIAN' };

/**
 * The type of every ship the log's events name by a typed field (a raider that activates, a basestar that attacks) or
 * that moves where only one type goes: the table doesn't show a raider back in the supply, but its line still says
 * "Raider #1012".
 */
function learnedShips(entries: LogEntry[]): Map<number, string> {
  const types = new Map<number, string>();
  const learn = (ids: unknown, type: string) => {
    for (const id of Array.isArray(ids) ? ids : [ids]) {
      if (typeof id === 'number') {
        types.set(id, type);
      }
    }
  };
  for (const entry of entries) {
    if (entry.kind !== 'ScopeLog') {
      continue;
    }
    const details = entry.details as unknown as Record<string, unknown> | null;
    for (const component of details ? (recordNamed(entry.event ?? '')?.components ?? []) : []) {
      const of = typeof component.type.of === 'object' ? component.type.of : null;
      if ((component.type.role ?? of?.role) !== 'ship') {
        continue;
      }
      const name = component.name;
      const type = (name === 'shipId' || name === 'shipIds' ? SHIP_EVENTS[entry.event ?? ''] : undefined) ?? SHIP_FIELDS.find(([pattern]) => pattern.test(name))?.[1];
      if (type) {
        learn(details?.[name], type);
      }
    }
    for (const fact of entry.facts ?? []) {
      const moved = fact.kind === 'Shown' ? (fact.fact as unknown as { kind: string; shipId: number; from: string | null; to: string | null }) : null;
      if (moved?.kind !== 'ShipFact.ShipMoved') {
        continue;
      }
      const zones = [moved.from, moved.to];
      if (zones.includes('CENTURION_SUPPLY') || zones.includes('BOARDING_PARTY')) {
        learn(moved.shipId, 'CENTURION');
      } else if (zones.includes('CIVILIAN_DECK')) {
        learn(moved.shipId, 'CIVILIAN');
      }
    }
  }
  return types;
}

function withLearnedShips(context: FactContext, learned: Map<number, string>): FactContext {
  if (learned.size === 0) {
    return context;
  }
  const shown = (id: number) => context.shipType?.(id) ?? null;
  return {
    ...context,
    shipType: (id) => shown(id) ?? learned.get(id) ?? null,
    shipName: (id) => {
      const type = learned.get(id);
      return !shown(id) && type ? `${constantLabel('ShipType', type)} #${id}` : (context.shipName?.(id) ?? `ship #${id}`);
    },
  };
}

function addLine(turns: TurnStory[], line: LogLine) {
  const steps = turns[turns.length - 1].steps;
  const lines = steps[steps.length - 1].lines;
  const previous = lines[lines.length - 1];
  if (previous && previous.actor === line.actor && previous.ships && line.ships && sameShipMove(previous.ships.move, line.ships.move)) {
    // "Raider #1016, Raider #1017 and Raider #1018 move to Space 7 o'clock", one activation each
    const ships = { ...previous.ships, names: [...previous.ships.names, ...line.ships.names] };
    lines[lines.length - 1] = { ...previous, ships, words: shipMovesParts(ships.names, ships.move), art: [...previous.art, ...line.art] };
  } else {
    lines.push(line);
  }
}

function scopeLines(entry: ScopeLog, index: number, chosenBy: number | null, scopes: Map<number, ScopeLog>, state: { cardsBefore: boolean }, context: FactContext): LogLine[] {
  const kind = entry.event ?? '';
  const details = entry.details as unknown as Record<string, unknown> | null;
  const facts = entry.facts ?? [];
  if (facts.some((fact) => factKind(fact) === 'SkillCheckFact.SkillCheckStarted')) {
    state.cardsBefore = false;
  }
  const sentences = factSentences(facts, context);
  const player = isPlayerEvent(kind);
  const actor = chosenBy ?? (player ? ((details?.playerNumber as number | undefined) ?? entry.actor) : null);
  const key = `scope-${entry.scopeId}-${index}`;
  const base = { entry, automatic: entry.automatic };

  if (SECRET_ANSWERS.has(kind)) {
    return [];
  }
  // a player who discards nothing does nothing
  if ((kind === 'DiscardSkillCardsEvent' || kind === 'DiscardQuorumCardsEvent') && details && ((details.cards as unknown[] | null) ?? []).length === 0) {
    return [];
  }
  if (kind === 'PlayCardsBeforeSkillCheckEvent' || kind === 'PlayCardsIntoSkillCheckEvent') {
    // nobody needs to read that a player put no card in
    const cards = details ? ((details.skillCards as unknown[] | null) ?? []).length : sentences.length;
    if (cards === 0) {
      return [];
    }
    if (kind === 'PlayCardsBeforeSkillCheckEvent') {
      state.cardsBefore = true;
    }
  }
  if (kind === 'RevealCardsBeforeSkillCheckEvent' && !state.cardsBefore) {
    return [];
  }

  const chosen = chosenBy !== null && !player && details ? chosenParts(details, context) : null;
  if (!chosen && (TOLD.has(kind) || TOLD_NAME.test(kind))) {
    return storyFirst(sentences).map((one, at) => toldLine(one, `${key}-${at}`, base, context));
  }

  if (kind === 'NoOpEvent') {
    const parent = scopes.get(entry.parentId);
    if (ASKED_EVERYONE.has(parent?.event ?? '')) {
      // every player is asked about these cards, so a pass says nothing
      return [];
    }
    const parentDetails = (parent?.details as unknown as Record<string, unknown> | null) ?? null;
    const source = parent ? eventSource(parent.event ?? '', parentDetails) : null;
    const words = [{ text: noOpWords(parent?.event ?? null, parentDetails, source !== null) }];
    return [{ ...base, key, actor, words, source: source ? sourcePart(source, context) : null, facts: [], art: [] }];
  }

  if (sentences.length === 0) {
    if (!player && chosenBy === null && !KEEP_EMPTY.has(kind)) {
      return [];
    }
    if (player && (ASKS.has(kind) || kind.startsWith('Offer'))) {
      return [];
    }
  }

  const told = chosen ?? (details ? eventParts(details, context) : hiddenEventParts(kind, actor, context));
  // an "Investigative Committee" shows the cards a player puts in
  const shownFaceUp = kind === 'PlayCardsIntoSkillCheckEvent' && facts.some((fact) => factKind(fact) === 'SkillCheckFact.CardsShown');
  const words = shownFaceUp ? [...told, { text: ' face up' }] : told;
  const line = actor !== null && !chosen ? (withoutSubject(words, context.playerName(actor)) ?? words) : words;
  const named = namedCards(details);
  const source = unnamedSource(kind, details, words, context);
  const said = (one: Sentence) => repeats(one, line, named, actor, context);
  const nested = sentences.filter(
    (one) => !toldApart(one) && !said(one) && !usesSource(one, source, actor, context) && !activationMove(one, details, line) && !(shownFaceUp && shows(one)),
  );
  const ownLines = sentences.filter((one) => toldApart(one) && !said(one));
  const apart = new Set(ownLines.flatMap((one) => one.facts));
  return [
    {
      ...base,
      key,
      actor,
      // "User123 chooses William Adama": his character is the one he chooses
      namesUser: kind === 'SelectCharacterEvent',
      words: line,
      source,
      facts: storyFirst(nested).map((one) => sentenceParts(one, actor, context)),
      art: unique([...recordArt(details), ...facts.filter((fact) => !apart.has(fact)).flatMap(lineArt)]).filter((ref) => !(DAMAGE_CHOSEN.has(kind) && ref.key.startsWith('GalacticaDamage/'))),
    },
    ...ownLines.map((one, at) => toldLine(one, `${key}-apart-${at}`, base, context)),
  ];
}

function toldLine(one: Sentence, key: string, base: { entry: LogEntry; automatic: boolean }, context: FactContext): LogLine {
  return { ...base, key, actor: one.subject, words: sentenceParts(one, one.subject, context), source: null, facts: [], art: unique(one.facts.flatMap(lineArt)), ships: one.ships };
}

// The scopes that ask every player in turn whether he plays a card ("Political Prowess" before a location's check,
// "Declare Emergency" once the strength is totaled, "Change of Plans" after the result), holder or not.
const ASKED_EVERYONE = new Set([
  'OfferPlayOrPassEvent',
  'TotalSkillCheckStrengthEvent',
  'DetermineSkillCheckResultEvent',
  'AdmiralsQuartersEvent',
  'BrigEvent',
  'AirlockEvent',
]);

function noOpWords(parent: string | null, details: Record<string, unknown> | null, offered: boolean): string {
  switch (parent) {
    case 'MovementStepEvent':
      return "doesn't move";
    case 'ActionStepEvent':
    case 'CriticalSituationEvent':
      return 'takes no action';
    case 'GrantEvent':
      return details?.grant === 'MOVE' ? "doesn't move" : details?.grant === 'ACTION' ? 'takes no action' : 'does nothing';
    case 'CommunicationsEvent':
      return 'leaves a civilian ship where it is';
    // what the start of a turn, the end of a Movement step and the cards before a skill check offer are abilities
    case 'StartTurnEvent':
    case 'EndOfMovementStepEvent':
    case 'ResolveCardsBeforeSkillCheckEvent':
      return 'declines';
    default:
      return offered || parent?.startsWith('Offer') ? 'declines' : 'passes';
  }
}

// The images a fact names beside its line; a shuffled deck's order is no picture of what happened, and a card played
// on a roll is pictured by its play, with its value.
function lineArt(fact: FactProjection): ArtRef[] {
  const kind = factKind(fact);
  return kind === 'DeckFact.DeckShuffled' || kind === 'GameFact.DieRollCardPlayed' ? [] : factArt(fact);
}

function shows(one: Sentence): boolean {
  return one.facts.every((fact) => factKind(fact) === 'SkillCheckFact.CardsShown');
}

function storyFirst(sentences: Sentence[]): Sentence[] {
  return [...sentences.filter((one) => one.facts.some(isStoryFact)), ...sentences.filter((one) => !one.facts.some(isStoryFact))];
}

// "uses Fast Learner" under a line tagged with Fast Learner
function usesSource(one: Sentence, source: Part | null, actor: number | null, context: FactContext): boolean {
  return !!source && partsText(sentenceParts(one, actor, context)) === `uses ${source.text}`;
}

// The move of a ship the line names, which it says already: the viper an activation moves or launches, the civilian
// ship a player moves or destroys.
function activationMove(one: Sentence, details: Record<string, unknown> | null, line: Part[]): boolean {
  const fact = one.facts.length === 1 && one.facts[0].kind === 'Shown' ? (one.facts[0].fact as unknown as Record<string, unknown>) : null;
  if (!details || fact?.kind !== 'ShipFact.ShipMoved') {
    return false;
  }
  const activation = details.activation as Record<string, unknown> | null | undefined;
  if (activation?.moveLocation) {
    return fact.shipId === activation.humanFighterId && fact.location === activation.moveLocation;
  }
  if (activation?.launchShipType) {
    return fact.from === 'RESERVES' && fact.location === activation.launchLocation;
  }
  const named = Object.values(details).includes(fact.shipId);
  if (!named) {
    return false;
  }
  return fact.location ? Object.values(details).includes(fact.location) : /destroy/.test(partsText(line));
}

export function lineText(line: LogLine): string {
  return partsText(line.words);
}


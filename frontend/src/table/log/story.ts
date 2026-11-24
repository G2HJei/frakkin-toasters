// The log as a story: the scopes and choices of the journal as lines, by turn and step, each under the player it is
// about. A choice is told by the event it picked, bookkeeping is left out, and an event whose facts say everything is
// told by them.
import { recordArt, unique, factArt } from '../../art/refs';
import type { ArtRef } from '../../art/images';
import {
  chosenParts,
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
import type { FactProjection, LogEntry, ScopeLog } from '../../wire/types';

export interface LogLine {
  key: string;
  // the scope or the choice it tells, for what a click lights on the board
  entry: LogEntry;
  // the player it is about, named above it; null for what happens to the game
  actor: number | null;
  words: Part[];
  // the card or ability it comes from, when its words don't name it
  source: Part | null;
  facts: Part[][];
  art: ArtRef[];
  automatic: boolean;
  // ships that moved alike, which the next such line joins
  ships?: { names: string[]; move: ShipMove };
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
  'InterruptFact.RollAbilityResolved',
]);

/** Whether a fact changes the table: a ship that is destroyed, damaged or launched does. */
export function isStoryFact(fact: FactProjection): boolean {
  const kind = factKind(fact);
  if (kind === 'ShipFact.ShipMoved' && fact.kind === 'Shown') {
    const ship = fact.fact as unknown as { from: string | null; to: string | null };
    return ship.to !== ship.from && ship.to !== 'CYLON_FLEET_SPACE' && ship.from !== 'HUMAN_SUPPLY' && ship.from !== 'CIVILIAN_DECK';
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

/** The log by turn and step. */
export function logStory(entries: LogEntry[], context: FactContext): TurnStory[] {
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
    for (const line of scopeLines(entry, index, chosenBy, scopes, state, context)) {
      addLine(turns, line);
    }
    chosenBy = null;
  });
  return turns
    .map((turn) => ({ ...turn, steps: turn.steps.filter((step) => step.lines.length > 0) }))
    .filter((turn) => turn.turn > 0 || turn.steps.some((step) => step.lines.length > 0));
}

function addLine(turns: TurnStory[], line: LogLine) {
  const steps = turns[turns.length - 1].steps;
  const lines = steps[steps.length - 1].lines;
  const previous = lines[lines.length - 1];
  if (previous && previous.actor === line.actor && previous.ships && line.ships && sameShipMove(previous.ships.move, line.ships.move)) {
    // "Raider #1016, Raider #1017 and Raider #1018 move to Space 6 o'clock", one activation each
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
  const nested = sentences.filter(
    (one) => !repeats(one, line, named, actor, context) && !usesSource(one, source, actor, context) && !activationMove(one, details, line) && !(shownFaceUp && shows(one)),
  );
  return [
    {
      ...base,
      key,
      actor,
      words: line,
      source,
      facts: storyFirst(nested).map((one) => sentenceParts(one, actor, context)),
      art: unique([...recordArt(details), ...facts.flatMap(lineArt)]),
    },
  ];
}

// A fact as a line of its own, under the player it is about.
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

// A pass, by what it passed on.
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

// The images a fact names beside its line; a shuffled deck's order is no picture of what happened.
function lineArt(fact: FactProjection): ArtRef[] {
  return factKind(fact) === 'DeckFact.DeckShuffled' ? [] : factArt(fact);
}

function shows(one: Sentence): boolean {
  return one.facts.every((fact) => factKind(fact) === 'SkillCheckFact.CardsShown');
}

// What changes the table first, the mechanics after.
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

/** A line's words as plain text, e.g. for a title. */
export function lineText(line: LogLine): string {
  return partsText(line.words);
}


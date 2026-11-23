// Where to read a rule: the combined rulebook and its Appendix ship with the app (the project is for private play),
// and a reminder names the article and the page to open, never the rule's text. The pages are the PDFs' own page
// numbers, which `#page=` opens. Only the sections of the Kobol game are listed (CLAUDE.md "Scope").
import rulebookUrl from '../../../materials-sorted/bsg-rules/Battlestar_Galactica_CR2-0.pdf?url';
import appendixUrl from '../../../materials-sorted/bsg-rules/Battlestar_Galactica_CR2-0_Appendix.pdf?url';
import { recordNamed } from '../wire/schema';
import type { InputProjection } from '../wire/types';

export interface Rule {
  article: string;
  // what the section is about, in our words
  name: string;
  page: number;
  appendix: boolean;
}

const MAIN: [string, string, number][] = [
  ['1.3', 'What the game is about', 6],
  ['2.1', 'How the humans win', 7],
  ['2.2', 'How the Cylons win', 7],
  ['2.3', 'How a Cylon Leader wins', 7],
  ['3.1', 'Cylon players and humans', 7],
  ['3.2', 'What stays secret', 7],
  ['4.1', 'Character sheets', 9],
  ['4.2', 'Using abilities', 9],
  ['4.3', 'Miracle tokens', 9],
  ['4.4', 'The titles and the Quorum Cards', 10],
  ['5.1', 'Loyalty Cards', 12],
  ['5.2', 'The Mutineer and Mutiny Cards', 12],
  ['5.3', 'Personal Goals', 13],
  ['5.4', 'The Final Five', 13],
  ['5.5', 'The Sleeper Agent phase', 13],
  ['6.1', 'Revealed Cylon players', 14],
  ['6.2', 'Cylon Leaders', 14],
  ['7.1', 'Executions', 16],
  ['8.1', 'The turn', 18],
  ['8.2', 'Receive Skills step', 18],
  ['8.3', 'Movement step', 18],
  ['8.4', 'Action step', 19],
  ['8.5', 'Crisis step', 20],
  ['8.6', 'Activate Cylon Ships step', 20],
  ['8.7', 'Prepare for Jump step', 21],
  ['8.8', 'Discard step', 21],
  ['9.1', 'Hazardous locations', 21],
  ['9.2', 'The Brig', 22],
  ['9.3', 'Sickbay', 22],
  ['9.4', 'Colonial One', 22],
  ['9.5', 'The Resurrection Ship', 22],
  ['9.6', 'The Cylon Fleet board', 22],
  ['9.7', 'The Basestar Bridge', 23],
  ['10.1', 'The Destiny deck', 24],
  ['10.2', 'Treachery cards', 24],
  ['10.3', 'Skill checks', 25],
  ['10.4', 'Skill Card abilities', 25],
  ['11.1', 'Activating vipers', 26],
  ['11.2', 'Piloting', 27],
  ['11.3', 'Ships', 27],
  ['11.4', 'Placing Cylon ships', 28],
  ['11.5', 'Activating Cylon ships', 28],
  ['11.6', 'Attacks', 29],
  ['11.7', 'Damage', 30],
  ['12.1', 'Jumping the fleet', 31],
  ['12.2', 'Jumping from FTL Control', 32],
  ['12.3', 'Destination Cards', 32],
  ['12.4', 'The Objective Card', 32],
  ['13.1', 'Changing die rolls', 33],
  ['13.2', 'Losing and gaining resources', 33],
  ['13.3', 'Timing', 33],
  ['13.4', 'Running out of components', 33],
  ['13.5', 'Raptors and risking ships', 33],
  ['13.6', 'Destroying civilian ships', 33],
  ['16.1', 'The end of the game', 38],
  ['16.2', 'Personal Goals at the end', 38],
  ['16.3', 'Motive Cards at the end', 38],
];

const APPENDIX: [string, string, number][] = [
  ['A2.1', 'The character sheets, one by one', 6],
  ['A2.2', 'The game boards', 8],
  ['A2.3', 'The cards', 9],
  ['A2.5', 'Tokens and markers', 13],
  ['A2.7', 'Components left out', 14],
  ['A3.1', 'Setting up', 15],
  ['A3.7', 'Choosing characters', 17],
  ['A3.8', 'The Loyalty deck', 17],
  ['A3.9', 'The starting Skill Cards', 18],
  ['A5.3', 'Reaching Kobol', 23],
];

export const RULES: Rule[] = [
  ...MAIN.map(([article, name, page]) => ({ article, name, page, appendix: false })),
  ...APPENDIX.map(([article, name, page]) => ({ article, name, page, appendix: true })),
];

export function rule(article: string): Rule | null {
  return RULES.find((candidate) => candidate.article === article) ?? null;
}

/** The PDF page a rule is on. */
export function ruleHref(found: Rule): string {
  return `${found.appendix ? appendixUrl : rulebookUrl}#page=${found.page}`;
}

export function ruleText(found: Rule): string {
  return `Rule ${found.article}, page ${found.page}${found.appendix ? ' of the Appendix' : ''}`;
}

// the article to read for a kind of input, by the event it asks for or is about
const BY_KIND: Record<string, string> = {
  StartGameEvent: 'A3.1',
  SelectCharacterEvent: 'A3.7',
  ReceiveStartingSkillsEvent: 'A3.9',
  ReceiveSkillsStepEvent: '8.2',
  DrawSkillCardsEvent: '8.2',
  MovementStepEvent: '8.3',
  MoveEvent: '8.3',
  MovementAbilityEvent: '8.3',
  ActionStepEvent: '8.4',
  ActionEvent: '8.4',
  CrisisStepEvent: '8.5',
  ResolveCrisisCardEvent: '8.5',
  ChooseCrisisSectionEvent: '8.5',
  ChooseCrisisTargetEvent: '8.5',
  ResolveSuperCrisisCardEvent: '6.1',
  ActivateCylonShipsStepEvent: '8.6',
  ActivateRaiderAreasEvent: '11.5',
  ActivateRaiderAreaEvent: '11.5',
  PrepareForJumpStepEvent: '8.7',
  JumpingTheFleetEvent: '12.1',
  DrawDestinationCardsEvent: '12.3',
  ChooseDestinationEvent: '12.3',
  DiscardStepEvent: '8.8',
  DiscardSkillCardsEvent: '8.8',
  MakeSkillCheckEvent: '10.3',
  RevealCardsBeforeSkillCheckEvent: '10.3',
  PlayCardsBeforeSkillCheckEvent: '10.3',
  PlayCardsIntoSkillCheckEvent: '10.3',
  OfferPlayOrPassEvent: '10.3',
  DetermineSkillCheckAbilitiesOrderEvent: '10.4',
  ResolveSkillCheckAbilityEvent: '10.4',
  ActivateViperEvent: '11.1',
  GrantedViperActivationEvent: '11.1',
  ViperActivationRoundEvent: '11.1',
  DamageVipersEvent: '11.7',
  DamageChosenViperEvent: '11.7',
  DamageBattlestarEvent: '11.7',
  OpenInterruptWindowEvent: '13.1',
  PlayInterruptCardEvent: '13.1',
  UseRollAbilityEvent: '13.1',
  RevealCylonEvent: '6.1',
  FinishCylonRevealEvent: '6.1',
  ExecuteEvent: '7.1',
  SendToBrigEvent: '9.2',
  ChooseCharacterForBrigEvent: '9.2',
  DrawMutinyCardEvent: '5.2',
  Discard1MutinyCardEvent: '5.2',
  DealLoyaltyCardsEvent: '5.1',
  SleeperAgentPhaseEvent: '5.5',
  ResolveFinalFiveCardEvent: '5.4',
  ResolveFinalFiveCardsEvent: '5.4',
  RevealPersonalGoalEvent: '5.3',
  RevealMotiveCardEvent: '16.3',
  RiskShipsEvent: '13.5',
  DestroyCivilianShipEvent: '13.6',
  EndGameEvent: '16.1',
};

function ruleOfKind(kind: string | null | undefined): Rule | null {
  if (!kind) {
    return null;
  }
  const record = recordNamed(kind);
  const article = BY_KIND[kind] ?? (record?.quorumCard ? '4.4' : record?.mutinyCard ? '5.2' : record?.interfaces.includes('SkillCardEvent') ? '10.4' : null);
  return article ? rule(article) : null;
}

/** The rule to read for an input: by the decision's event, then by what queued it, then by a choice's options. */
export function ruleFor(input: InputProjection | null | undefined): Rule | null {
  switch (input?.kind) {
    case 'Decide':
      return ruleOfKind(input.action) ?? ruleOfKind(input.topic?.event);
    case 'Choose': {
      const options = (input.options ?? []).map((option) => (option?.kind === 'Single' ? (option.event as unknown as { kind?: string; action?: string }) : null));
      const asked = options.map((event) => (event?.kind === 'PlayerDecisionEvent' ? event.action : event?.kind));
      return ruleOfKind(input.topic?.event) ?? asked.map(ruleOfKind).find((found) => found !== null) ?? null;
    }
    case 'WaitingFor':
      return ruleOfKind(input.topic?.event);
    default:
      return null;
  }
}

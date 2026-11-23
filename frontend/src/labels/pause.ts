// What the game waits for, in words: who is asked, and what about. The topic is the event that queued the input, as the
// log shows it (`Topic` in the projection); a secret input says only that someone may play a card.
import { constantLabel, eventParts, partsText, typeLabel, unnamedSource, type LabelContext, type Part } from './describe';
import type { InputProjection, Topic } from '../wire/types';

// what a topic is about, where the event's own words would read oddly as a reason to wait
const TOPICS: Record<string, string> = {
  StartGameEvent: 'Setting up the game',
  ReceiveStartingSkillsEvent: 'Starting Skill Cards',
  ReceiveSkillsStepEvent: 'Receive Skills step',
  MovementStepEvent: 'Movement step',
  ActionStepEvent: 'Action step',
  CrisisStepEvent: 'Crisis step',
  ActivateCylonShipsStepEvent: 'Activate Cylon Ships step',
  PrepareForJumpStepEvent: 'Prepare for Jump step',
  DiscardStepEvent: 'Discard step',
  MakeSkillCheckEvent: 'The skill check',
  RevealCardsBeforeSkillCheckEvent: 'The skill check',
  DamageBattlestarEvent: 'A hit on the battlestars',
  ActivateRaiderAreasEvent: 'The raiders activate one area at a time',
};

/**
 * The topic in parts, led by the card or ability it comes from when its own words don't name it: "Forward Thinker: Lee
 * Adama may activate his location". Null without a topic.
 */
export function topicParts(topic: Topic | null | undefined, context: LabelContext): Part[] | null {
  if (!topic?.event) {
    return null;
  }
  const details = topic.details as unknown as Record<string, unknown> | null;
  const words = ownWords(topic.event, details, context);
  const source = unnamedSource(topic.event, details, words, context);
  return source ? [source, { text: ': ' }, ...words] : words;
}

export function topicLabel(topic: Topic | null | undefined, context: LabelContext): string | null {
  const parts = topicParts(topic, context);
  return parts ? partsText(parts) : null;
}

function ownWords(event: string, details: Record<string, unknown> | null, context: LabelContext): Part[] {
  if (event === 'OfferPlayOrPassEvent' && details) {
    return [{ text: `${constantLabel('SkillCardType', details.card as string)} may be played` }];
  }
  if (event === 'OpenInterruptWindowEvent' && details) {
    const window = details.window as { kind?: string } | null;
    return [{ text: `An interrupt window: ${typeLabel(window?.kind ?? '')}` }];
  }
  if (TOPICS[event]) {
    return [{ text: TOPICS[event] }];
  }
  return details ? eventParts(details, context) : [{ text: typeLabel(event) }];
}

export interface Pause {
  heading: string;
  // what for, its names with their images; null when nothing more is known
  line: Part[] | null;
}

export function pauseOf(input: InputProjection | null | undefined, context: LabelContext): Pause {
  switch (input?.kind) {
    case 'Decide': {
      const topic = topicParts(input.topic, context);
      const action = input.action ? [{ text: typeLabel(input.action) }] : null;
      const line = action && topic ? [...action, { text: ' · ' }, ...topic] : (action ?? topic);
      return { heading: `${context.playerName(input.player)} decides`, line };
    }
    case 'Choose':
      return { heading: `${context.playerName(input.player)} chooses`, line: topicParts(input.topic, context) };
    case 'WaitingFor':
      return { heading: `Waiting for ${context.playerName(input.player)}`, line: topicParts(input.topic, context) };
    case 'Waiting':
      // only an interrupt window's cards and abilities are secret: who holds one stays unknown
      return { heading: 'Waiting', line: [{ text: 'Someone may play an interrupt card or use an ability' }] };
    default:
      return { heading: 'The game is over', line: null };
  }
}

/** The pause's line as plain text, null without one. */
export function pauseLineText(pause: Pause): string | null {
  return pause.line ? partsText(pause.line) : null;
}

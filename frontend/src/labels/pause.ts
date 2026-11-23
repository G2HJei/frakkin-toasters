// What the game waits for, in words: who is asked, and what about. The topic is the event that queued the input, as the
// log shows it (`Topic` in the projection); a secret input says only that someone may play a card.
import { constantLabel, eventLabel, typeLabel, type LabelContext } from './describe';
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

export function topicLabel(topic: Topic | null | undefined, context: LabelContext): string | null {
  if (!topic?.event) {
    return null;
  }
  const details = topic.details as unknown as Record<string, unknown> | null;
  if (topic.event === 'OfferPlayOrPassEvent' && details) {
    return `${constantLabel('SkillCardType', details.card as string)} may be played`;
  }
  if (topic.event === 'OpenInterruptWindowEvent' && details) {
    const window = details.window as { kind?: string } | null;
    return `An interrupt window: ${typeLabel(window?.kind ?? '')}`;
  }
  return TOPICS[topic.event] ?? (details ? eventLabel(details, context) : typeLabel(topic.event));
}

export interface Pause {
  heading: string;
  // what for, null when nothing more is known
  line: string | null;
}

export function pauseOf(input: InputProjection | null | undefined, context: LabelContext): Pause {
  switch (input?.kind) {
    case 'Decide': {
      const topic = topicLabel(input.topic, context);
      const action = input.action ? typeLabel(input.action) : null;
      return { heading: `${context.playerName(input.player)} decides`, line: [action, topic].filter((part) => part).join(' · ') || null };
    }
    case 'Choose':
      return { heading: `${context.playerName(input.player)} chooses`, line: topicLabel(input.topic, context) };
    case 'WaitingFor':
      return { heading: `Waiting for ${context.playerName(input.player)}`, line: topicLabel(input.topic, context) };
    case 'Waiting':
      // only an interrupt window's cards and abilities are secret: who holds one stays unknown
      return { heading: 'Waiting', line: 'Someone may play an interrupt card or use an ability' };
    default:
      return { heading: 'The game is over', line: null };
  }
}

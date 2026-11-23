import { describe, expect, it } from 'vitest';
import { pauseOf, topicLabel } from './pause';
import type { LabelContext } from './describe';
import type { InputProjection, Topic } from '../wire/types';

const context: LabelContext = { playerName: (playerNumber) => ['', 'William Adama', 'Laura Roslin'][playerNumber] ?? `Player ${playerNumber}` };
const topic = (event: string, details: unknown = null) => ({ event, details }) as Topic;

describe('a pause', () => {
  it('says who decides, the decision and what it is about', () => {
    const decide = { kind: 'Decide', player: 1, action: 'DrawSkillCardsEvent', constraints: [], answers: null, topic: topic('ReceiveSkillsStepEvent') } as InputProjection;

    expect(pauseOf(decide, context)).toEqual({ heading: 'William Adama decides', line: 'Draw Skill Cards · Receive Skills step' });
  });

  it('tells the others what the asked player is choosing', () => {
    const crisis = topic('ResolveCrisisCardEvent', { kind: 'ResolveCrisisCardEvent', card: 'RIOTS_NO_JUMP' });

    expect(pauseOf({ kind: 'WaitingFor', player: 2, topic: crisis } as InputProjection, context)).toEqual({ heading: 'Waiting for Laura Roslin', line: 'Crisis: Riots No Jump' });
    expect(pauseOf({ kind: 'WaitingFor', player: 2, topic: null } as InputProjection, context).line).toBeNull();
  });

  it('names an offer of a card every player is asked about, and nothing of a secret input', () => {
    expect(topicLabel(topic('OfferPlayOrPassEvent', { kind: 'OfferPlayOrPassEvent', playerNumber: 2, card: 'DECLARE_EMERGENCY' }), context)).toBe('Declare Emergency may be played');
    expect(pauseOf({ kind: 'Waiting' } as InputProjection, context)).toEqual({ heading: 'Waiting', line: 'Someone may play an interrupt card or use an ability' });
  });

  it('names a topic by its type when its details are hidden', () => {
    expect(topicLabel(topic('PlayInterruptCardEvent'), context)).toBe('Play an interrupt card');
  });
});

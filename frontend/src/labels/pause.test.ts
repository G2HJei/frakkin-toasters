import { describe, expect, it } from 'vitest';
import { pauseLineText, pauseOf, topicLabel, topicParts } from './pause';
import type { LabelContext } from './describe';
import type { InputProjection, Topic } from '../wire/types';

const context: LabelContext = { playerName: (playerNumber) => ['', 'William Adama', 'Laura Roslin'][playerNumber] ?? `Player ${playerNumber}` };
const topic = (event: string, details: unknown = null) => ({ event, details }) as Topic;
const text = (input: InputProjection) => {
  const pause = pauseOf(input, context);
  return { heading: pause.heading, line: pauseLineText(pause) };
};

describe('a pause', () => {
  it('says who decides, the decision and what it is about', () => {
    const decide = { kind: 'Decide', player: 1, action: 'DrawSkillCardsEvent', constraints: [], answers: null, topic: topic('ReceiveSkillsStepEvent') } as InputProjection;

    expect(text(decide)).toEqual({ heading: 'William Adama decides', line: 'Draw Skill Cards · Receive Skills step' });
  });

  it('tells the others what the asked player is choosing', () => {
    const crisis = topic('ResolveCrisisCardEvent', { kind: 'ResolveCrisisCardEvent', card: 'RIOTS_NO_JUMP' });

    expect(text({ kind: 'WaitingFor', player: 2, topic: crisis } as InputProjection)).toEqual({ heading: 'Waiting for Laura Roslin', line: 'Crisis: Riots No Jump' });
    expect(pauseOf({ kind: 'WaitingFor', player: 2, topic: null } as InputProjection, context).line).toBeNull();
  });

  it('names an offer of a card every player is asked about, and nothing of a secret input', () => {
    expect(topicLabel(topic('OfferPlayOrPassEvent', { kind: 'OfferPlayOrPassEvent', playerNumber: 2, card: 'DECLARE_EMERGENCY' }), context)).toBe('Declare Emergency may be played');
    expect(text({ kind: 'Waiting' } as InputProjection)).toEqual({ heading: 'Waiting', line: 'Someone may play an interrupt card or use an ability' });
  });

  it('names a topic by its type when its details are hidden', () => {
    expect(topicLabel(topic('PlayInterruptCardEvent'), context)).toBe('Play an interrupt card');
  });

  it('leads with the ability a topic comes from, its sheet to hover', () => {
    const offer = topic('OfferForwardThinkerEvent', { kind: 'OfferForwardThinkerEvent', playerNumber: 1 });
    const withArt: LabelContext = { ...context, artOf: (value, type) => ({ key: `${type.name}/${String(value)}`, url: '', label: '', shape: 'card' }) };

    expect(topicLabel(offer, context)).toBe("Forward Thinker: William Adama may take a location's action");
    expect(topicParts(offer, withArt)?.[0].art?.key).toBe('CharacterAbility/FORWARD_THINKER');
  });

  it('names what granted an action', () => {
    const grant = topic('GrantEvent', { kind: 'GrantEvent', playerNumber: 2, grant: 'MOVE_OR_ACTION', grantedBy: 'EXECUTIVE_ORDER' });

    expect(topicLabel(grant, context)).toBe('Executive Order: Laura Roslin may move or take an action');
  });

  it('leaves out an ability the topic names already', () => {
    const primacy = topic('PrimacyEvent', { kind: 'PrimacyEvent', playerNumber: 1, removedBasestarId: null, placeRaidersAndCivilianShip: false });

    expect(topicLabel(primacy, context)).toBe('William Adama uses Primacy');
  });
});

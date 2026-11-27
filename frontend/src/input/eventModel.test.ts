import { describe, expect, it } from 'vitest';
import { askedComponents, emptyEvent, isServerFilled, toWire } from './eventModel';
import { recordNamed } from '../wire/schema';

// An event's JSON has to be exactly what the backend's codec writes: `kind`, then every component in order.
describe('the event model', () => {
  it('builds a move as the codec writes it', () => {
    const move = { ...emptyEvent('MoveEvent', 2), destination: 'COMMAND' };

    expect(JSON.stringify(toWire(move))).toBe('{"kind":"MoveEvent","playerNumber":2,"destination":"COMMAND","discardCard":null}');
  });

  it('builds a draw with counts per color and leaves the constraint to the server', () => {
    const draw = { ...emptyEvent('DrawSkillCardsEvent', 1), selection: { POLITICS: 3 } };

    expect(toWire(draw)).toEqual({ kind: 'DrawSkillCardsEvent', playerNumber: 1, selection: { POLITICS: 3 }, drawLimit: null });
    expect(askedComponents('DrawSkillCardsEvent').map((component) => component.name)).toEqual(['selection']);
  });

  it('builds a list of cards from the hand and a nested record', () => {
    const discard = { ...emptyEvent('DiscardSkillCardsEvent', 3), cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }] };
    const activation = {
      ...emptyEvent('ActivateViperEvent', 1),
      activation: { launchShipType: null, launchLocation: null, humanFighterId: 4, moveLocation: 'GALACTICA_SPACE_3_OCLOCK', attackCylonShipId: null, escortCivilianShipId: null },
    };

    expect(toWire(discard)).toEqual({ kind: 'DiscardSkillCardsEvent', playerNumber: 3, cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }], discardLimit: null });
    expect(Object.keys(toWire(activation).activation as object)).toEqual([
      'launchShipType',
      'launchLocation',
      'humanFighterId',
      'moveLocation',
      'attackCylonShipId',
      'escortCivilianShipId',
    ]);
  });

  it('builds a sandbox setup, which has no player', () => {
    const setup = { ...emptyEvent('PutCrisisCardOnTopEvent'), card: 'RIOTS_NO_JUMP' };

    expect(toWire(setup)).toEqual({ kind: 'PutCrisisCardOnTopEvent', card: 'RIOTS_NO_JUMP' });
  });

  it('starts every field at a value the codec reads', () => {
    expect(emptyEvent('LaunchViperEvent', 2)).toEqual({
      kind: 'LaunchViperEvent',
      playerNumber: 2,
      shipType: null,
      location: null,
      pilot: null,
      unmannedViperId: null,
      mustLaunchInViper: false,
    });
    expect(emptyEvent('ChangeResourceEvent').amount).toBe(0);
  });

  it('hides the fields the server fills in', () => {
    const launch = recordNamed('LaunchViperEvent')!;

    expect(launch.components.filter(isServerFilled).map((component) => component.name)).toEqual(['playerNumber', 'mustLaunchInViper']);
    // "Unorthodox Plan" sets it through a constraint
    expect(recordNamed('CommandEvent')!.components.filter(isServerFilled).map((component) => component.name)).toEqual(['playerNumber', 'anywhere']);
  });
});

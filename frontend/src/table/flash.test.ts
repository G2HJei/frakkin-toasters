import { describe, expect, it } from 'vitest';
import { entryTargets } from './flash';
import type { GameProjection, LogEntry } from '../wire/types';

const game = { players: [{ number: 1, character: 'WILLIAM_ADAMA' }, { number: 2, character: 'LAURA_ROSLIN' }] } as unknown as GameProjection;
const shown = (fact: unknown) => ({ kind: 'Shown', fact });

describe('what a log entry touched', () => {
  it('is the locations, ships and players its event and its facts name, a character as his player', () => {
    const entry = {
      kind: 'ScopeLog',
      scopeId: 4,
      parentId: 0,
      actor: 1,
      event: 'MoveEvent',
      details: { kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null },
      facts: [shown({ kind: 'PlayerFact.CharacterMoved', character: 'WILLIAM_ADAMA', to: 'ARMORY' }), shown({ kind: 'ShipFact.ShipMoved', shipId: 7, from: 'RESERVES', to: 'GALACTICA_SPACE', location: 'GALACTICA_SPACE_6_OCLOCK' })],
    } as unknown as LogEntry;

    expect(entryTargets(entry, game).sort()).toEqual(['location:ARMORY', 'location:GALACTICA_SPACE_6_OCLOCK', 'player:1', 'ship:7']);
  });

  it("is a choice's options", () => {
    const choice = { kind: 'ChoiceLog', scopeId: 5, chooser: 2, option: { kind: 'Single', event: { kind: 'SendToSickbayEvent', targetPlayerNumber: 1 } } } as unknown as LogEntry;

    expect(entryTargets(choice, game)).toEqual(['player:1']);
  });
});

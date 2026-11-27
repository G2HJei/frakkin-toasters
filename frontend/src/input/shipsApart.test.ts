import { describe, expect, it } from 'vitest';
import { tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';
import { moves } from './answers';
import { shipsApart, shipsOf } from './shipsApart';

const ship = (id: number, type: string, location: string | null, more: Record<string, unknown> = {}) => ({ id, type, location, pilot: null, damage: [], cargo: null, ...more });

function game(ships: unknown[], { reserves = [] as unknown[], known = [] as number[], sandbox = false } = {}): GameProjection {
  return {
    viewer: sandbox ? { kind: 'Sandbox' } : { kind: 'PlayerViewer', playerNumber: 1 },
    players: [
      { number: 1, character: 'WILLIAM_ADAMA', knownCivilianShips: known },
      { number: 2, character: 'KARA_STARBUCK_THRACE', knownCivilianShips: [] },
    ],
    galactica: { ships, reserves, damagedVipers: [], boardingParty: [] },
  } as unknown as GameProjection;
}

const namesOf = (projection: GameProjection, ids: number[], sandbox = false) => {
  const table = shipsApart(tableContext(projection, sandbox, 1), ids);
  return ids.map((id) => table.shipName?.(id));
};

describe('ships named apart in the answers', () => {
  it('tell two ships of one type apart by where they are, and leave a ship alone of its type as it is', () => {
    const projection = game([ship(1016, 'RAIDER', 'GALACTICA_SPACE_7_OCLOCK'), ship(1017, 'RAIDER', 'GALACTICA_SPACE_9_OCLOCK'), ship(1021, 'HEAVY_RAIDER', 'GALACTICA_SPACE_7_OCLOCK')], {
      reserves: [ship(3, 'VIPER', null)],
    });

    expect(namesOf(projection, [1016, 1017, 1021])).toEqual(["Raider · Space 7 o'clock", "Raider · Space 9 o'clock", 'Heavy Raider']);
    // only the ships of the list count
    expect(namesOf(projection, [1016, 1021])).toEqual(['Raider', 'Heavy Raider']);
  });

  it('leave ships of one area alike, but for a pilot or damage', () => {
    const projection = game(
      [
        ship(1, 'VIPER', 'GALACTICA_SPACE_3_OCLOCK'),
        ship(2, 'VIPER', 'GALACTICA_SPACE_3_OCLOCK'),
        ship(4, 'VIPER', 'GALACTICA_SPACE_3_OCLOCK', { pilot: 'KARA_STARBUCK_THRACE' }),
        ship(1001, 'BASESTAR', 'GALACTICA_SPACE_3_OCLOCK'),
        ship(1002, 'BASESTAR', 'GALACTICA_SPACE_3_OCLOCK', { damage: ['DISABLED_HANGAR_BAY'] }),
      ],
      { reserves: [ship(3, 'VIPER', null)] },
    );

    expect(namesOf(projection, [1, 2, 3, 4])).toEqual(["Viper · Space 3 o'clock", "Viper · Space 3 o'clock", 'Viper · Reserves', "Viper · Space 3 o'clock · Starbuck"]);
    expect(namesOf(projection, [1001, 1002])).toEqual(["Basestar · Space 3 o'clock", "Basestar · Space 3 o'clock · Disabled Hangar Bay"]);
  });

  it("show what a civilian ship holds to the player who decides, when he looked at it", () => {
    const cargo = { population: 1, morale: 1, fuel: 0 };
    const ships = [ship(40, 'CIVILIAN', 'GALACTICA_SPACE_5_OCLOCK', { cargo }), ship(41, 'CIVILIAN', 'GALACTICA_SPACE_5_OCLOCK')];

    expect(namesOf(game(ships, { known: [40] }), [40, 41])).toEqual(['Civilian ship · 1 population, 1 morale', 'Civilian ship']);
    // a sandbox's projection holds every cargo: only what the asked player looked at shows
    const both = [ship(40, 'CIVILIAN', 'GALACTICA_SPACE_5_OCLOCK', { cargo }), ship(41, 'CIVILIAN', 'GALACTICA_SPACE_5_OCLOCK', { cargo: { population: 0, morale: 0, fuel: 0 } })];
    expect(namesOf(game(both, { known: [40], sandbox: true }), [40, 41], true)).toEqual(['Civilian ship · 1 population, 1 morale', 'Civilian ship']);
    // two he looked at read apart by what they hold
    expect(namesOf(game(both, { known: [40, 41] }), [40, 41])).toEqual(['Civilian ship · 1 population, 1 morale', 'Civilian ship · nothing']);
  });

  it('find the ships the answers name, nested ones and a shape\'s ships to pick too', () => {
    const activation = (fighter: number, target: number | null) => ({ launchShipType: null, launchLocation: null, humanFighterId: fighter, moveLocation: null, attackCylonShipId: target, escortCivilianShipId: null });
    const all = moves({
      kind: 'Decide',
      player: 1,
      action: 'ActivateViperEvent',
      constraints: [],
      topic: null,
      answers: {
        events: [
          { kind: 'ActivateViperEvent', playerNumber: 1, activation: activation(1, 1016) },
          { kind: 'RepairEvent', playerNumber: 1, card: { type: 'REPAIR', value: 1 }, viperIds: [7, 8] },
        ],
        shapes: [{ example: { kind: 'SacrificeEvent', playerNumber: 1, shipIds: [1017] }, pick: { kind: 'ShipPick', component: 'shipIds', shipIds: [1017, 1018], min: 1, max: 2 } }],
      },
    } as never);

    expect(shipsOf(all).sort((a, b) => a - b)).toEqual([1, 7, 8, 1016, 1017, 1018]);
  });
});

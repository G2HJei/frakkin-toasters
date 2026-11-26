import { describe, expect, it } from 'vitest';
import { actionArt, ART_KINDS, moveArt, partValueArt } from './answerArt';
import type { Answer, Move } from './answers';
import { titleArt } from '../art/images';
import { tableContext } from '../table/tableContext';
import { isMiracle, recordNamed, schema, typeNamed } from '../wire/schema';
import type { Followup, GameProjection, ShipProjection } from '../wire/types';

const keys = (refs: { key: string }[]) => refs.map((ref) => ref.key);
const game = {
  viewer: { kind: 'PlayerViewer', playerNumber: 1 },
  players: [
    { number: 1, character: 'GAIUS_BALTAR', location: 'RESEARCH_LAB' },
    { number: 2, character: 'LAURA_ROSLIN', location: 'PRESIDENTS_OFFICE' },
  ],
} as unknown as GameProjection;
const table = tableContext(game, false, 1);
const answer = (event: Answer): Move => ({ option: null, followup: null, event, submits: true, pick: null });
const art = (event: Answer) => keys(moveArt(answer(event), table));

describe("an answer's images", () => {
  it('name events of the schema, or an interface of events a decision asks for', () => {
    const isEvent = (kind: string) => recordNamed(kind) !== undefined && schema.lists.events.includes(kind);
    expect(ART_KINDS.filter((kind) => !isEvent(kind) && typeNamed(kind)?.kind !== 'union')).toEqual([]);
  });

  it("show a move as the turn panel's Movement step, before the card it discards", () => {
    const repair = { type: 'REPAIR', value: 1 };
    expect(art({ kind: 'MoveEvent', playerNumber: 1, destination: 'PRESS_ROOM', discardCard: repair })).toEqual(['StepIcon/MOVEMENT', 'SkillCardType/REPAIR_1']);
    expect(art({ kind: 'LeaveBrigEvent', playerNumber: 1, destination: 'ARMORY', destinationLimit: null })).toEqual(['StepIcon/MOVEMENT']);
    const decision = { kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'MovementAbilityEvent', eventConstraints: [] } } as unknown as Followup;
    expect(keys(actionArt({ option: 0, followup: decision, event: null, submits: false, pick: null }))).toEqual(['StepIcon/MOVEMENT']);
    expect(moveArt(answer({ kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null }), table)[0]).toMatchObject({
      shape: 'icon',
      label: 'Movement',
    });
  });

  it("show a target by his character's portrait, not the player who answers, and no location", () => {
    expect(art({ kind: 'ExecutiveOrderEvent', playerNumber: 1, card: { type: 'EXECUTIVE_ORDER', value: 2 }, targetPlayerNumber: 2 })).toEqual([
      'SkillCardType/EXECUTIVE_ORDER_2',
      'Character/player-token/LAURA_ROSLIN',
    ]);
    // the step it moves in, the board shows where to
    expect(art({ kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null })).toEqual(['StepIcon/MOVEMENT']);
  });

  it('end with the miracle token on an answer of a miracle ability, after its character', () => {
    expect(art({ kind: 'CylonDetectorEvent', playerNumber: 1, targetPlayerNumber: 2 })).toEqual([
      'Character/player-token/GAIUS_BALTAR',
      'Character/player-token/LAURA_ROSLIN',
      'Token/MIRACLE',
    ]);
    // "Memento" is no miracle
    expect(art({ kind: 'MementoEvent', playerNumber: 1 })).toEqual(['Character/player-token/BRENDAN_HOTDOG_COSTANZA']);
    expect(art({ kind: 'UseRollAbilityEvent', playerNumber: 1, ability: 'LONGSHOT', card: null, result: 5 })).toEqual([
      'Character/player-token/SAMUEL_T_ANDERS',
      'Die/5',
      'Token/MIRACLE',
    ]);
  });

  it("show a title by its insignia, with the card on hover", () => {
    const [icon] = moveArt(answer({ kind: 'ManipulativeEvent', playerNumber: 1, title: 'ADMIRAL' }), table).filter((ref) => ref.key.startsWith('TitleIcon/'));

    expect(icon).toMatchObject({ key: 'TitleIcon/ADMIRAL', shape: 'icon', preview: titleArt('ADMIRAL')!.preview });
    expect(art({ kind: 'PresidentTitleActionEvent', playerNumber: 1 })).toEqual(['TitleIcon/PRESIDENT', 'DeckId/QUORUM']);
  });

  it('show the back of the deck a draw or a look takes from, a Skill Card color in its color', () => {
    expect(art({ kind: 'DrawQuorumCardEvent' })).toEqual(['DeckId/QUORUM']);
    expect(art({ kind: 'LookAtTopCardEvent', playerNumber: 1, deck: 'CRISIS' })).toEqual(['DeckId/CRISIS']);
    // a location's action at its pin
    expect(art({ kind: 'ResearchLabEvent', playerNumber: 1, skillCardColor: 'TACTICS' })).toEqual(['Glyph/LOCATION/RESEARCH_LAB', 'SkillCardColor/TACTICS']);
  });

  it('show a location to pick as the map pin, with its card on hover, but a move by its step', () => {
    const [pin] = moveArt(answer({ kind: 'PlaceInSetupLocationEvent', playerNumber: 1, location: 'COMMAND' }), table);

    expect(pin).toMatchObject({ key: 'Glyph/LOCATION/COMMAND', shape: 'icon', label: 'Command' });
    expect(pin.preview).toHaveLength(1);
    expect(art({ kind: 'PlaceCivilianShipEvent', playerNumber: 1, location: 'GALACTICA_SPACE_2_OCLOCK' })).toEqual([
      'ShipType/CIVILIAN',
      'Glyph/LOCATION/GALACTICA_SPACE_2_OCLOCK',
    ]);
    // a nuke's target at the pin
    expect(art({ kind: 'LaunchNukeEvent', playerNumber: 1, area: 'GALACTICA_SPACE_2_OCLOCK' })).toEqual(['Token/NUKE', 'Glyph/LOCATION/GALACTICA_SPACE_2_OCLOCK']);
    // a viper's move by the Movement step, its launch at the pin
    const withShips = { ...table, ship: (id: number) => ({ id, type: 'VIPER' }) as unknown as ShipProjection };
    const activation = { launchShipType: null, launchLocation: null, humanFighterId: 3, moveLocation: 'GALACTICA_SPACE_4_OCLOCK', attackCylonShipId: null, escortCivilianShipId: null };
    expect(keys(moveArt(answer({ kind: 'ActivateViperEvent', playerNumber: 1, activation }), withShips))).toEqual(['ShipType/VIPER', 'StepIcon/MOVEMENT']);
    const launch = { ...activation, launchShipType: 'VIPER', launchLocation: 'GALACTICA_SPACE_4_OCLOCK', humanFighterId: null, moveLocation: null };
    expect(keys(moveArt(answer({ kind: 'ActivateViperEvent', playerNumber: 1, activation: launch }), withShips))).toEqual([
      'ShipType/VIPER',
      'Glyph/LOCATION/GALACTICA_SPACE_4_OCLOCK',
    ]);
  });

  it("show the Action step's icon on what takes an action, before or after a move", () => {
    const decision = (action: string) => ({ kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action, eventConstraints: [] } }) as unknown as Followup;
    const option = (followup: Followup): Move => ({ option: 0, followup, event: null, submits: false, pick: null });

    expect(keys(actionArt({ ...option(decision('ActionEvent')), submits: true }))).toEqual(['StepIcon/ACTION']);
    const both = { kind: 'AllOf', followups: [decision('MoveEvent'), decision('ActionEvent')] } as unknown as Followup;
    expect(keys(moveArt(option(both), table))).toEqual(['StepIcon/MOVEMENT', 'StepIcon/ACTION']);
    const twice = { kind: 'AllOf', followups: [decision('ActionEvent'), decision('ActionEvent')] } as unknown as Followup;
    expect(keys(moveArt(option(twice), table))).toEqual(['StepIcon/ACTION']);
    // what a grant lets him do
    expect(art({ kind: 'GrantEvent', playerNumber: 1, grant: 'MOVE_OR_ACTION', grantedBy: null })).toEqual(['StepIcon/MOVEMENT', 'StepIcon/ACTION']);
    expect(art({ kind: 'GrantEvent', playerNumber: 1, grant: 'ACTION', grantedBy: null })).toEqual(['StepIcon/ACTION']);
    // what an ability of the Basestar Bridge acts on
    expect(art({ kind: 'BasestarBridgeAbilityEvent', playerNumber: 1, ability: 'JUMP_OR_PURSUIT' })).toEqual(['StepIcon/PREPARE_FOR_JUMP']);
    // a decision's head shows the card or the ability it comes from
    expect(keys(actionArt({ ...option(decision('AuthorizationOfBrutalForceEvent')), submits: true }))).toEqual(['QuorumCard/AUTHORIZATION_OF_BRUTAL_FORCE']);
    expect(keys(actionArt({ ...option(decision('HumanDelusionEvent')), submits: true }))).toEqual(['Character/player-token/CAPRICA_SIX']);
    // an interrupt card of one type by its card, which its answers name each
    expect(keys(actionArt({ ...option(decision('ChangeOfPlansEvent')), submits: true }))).toEqual(['SkillCardType/CHANGE_OF_PLANS_5']);
    expect(art({ kind: 'ChangeOfPlansEvent', playerNumber: 1, changeOfPlansCard: { type: 'CHANGE_OF_PLANS', value: 5 } })).toEqual(['SkillCardType/CHANGE_OF_PLANS_5']);
    expect(keys(actionArt({ ...option(decision('Discard1MutinyCardEvent')), submits: true }))).toEqual(['DeckId/MUTINY']);
  });

  it('show where a card goes in its deck, and the damage a battlestar takes', () => {
    expect(art({ kind: 'PlaceDestinationCardOnTopEvent', playerNumber: 1, card: 'GAS_CLOUD' })).toEqual(['Glyph/DECK_TOP', 'DestinationCard/GAS_CLOUD']);
    expect(art({ kind: 'PlaceCrisisCardOnBottomEvent', playerNumber: 1, card: 'RIOTS_JUMP' })).toEqual(['Glyph/DECK_BOTTOM', 'CrisisCard/RIOTS_JUMP']);
    expect(art({ kind: 'DamageGalacticaEvent' })).toEqual(['DeckId/GALACTICA_DAMAGE']);
    expect(art({ kind: 'DamagePegasusEvent' })).toEqual(['DeckId/PEGASUS_DAMAGE']);
  });

  it('show the Destination back on the choice of a destination, before the cards it names', () => {
    expect(art({ kind: 'ChooseDestinationEvent', playerNumber: 1, chosen: 'A_CIVILIAN_CONVOY', others: ['DERELICT_BASESTAR'] })).toEqual([
      'DeckId/DESTINATION',
      'DestinationCard/A_CIVILIAN_CONVOY',
      'DestinationCard/DERELICT_BASESTAR',
    ]);
  });

  it('show the Skill Card back where Skill Cards are taken without being named: a shape to pick, a random discard', () => {
    const cards = [{ type: 'REPAIR', value: 1 }];
    const pick = { kind: 'SkillCardPick', component: 'skillCards', cards, min: 1, max: 3, ordered: false, minStrength: 0 } as unknown as Move['pick'];
    const shape: Move = { ...answer({ kind: 'PlayCardsIntoSkillCheckEvent', playerNumber: 1, skillCards: cards }), pick };

    expect(keys(moveArt(shape, table))).toEqual(['Back/SKILL']);
    expect(art({ kind: 'DiscardRandomSkillCardsEvent', playerNumber: 1, count: 2 })).toEqual(['Back/SKILL']);
    // a concrete answer shows its cards
    expect(art({ kind: 'PlayCardsIntoSkillCheckEvent', playerNumber: 1, skillCards: cards })).toEqual(['SkillCardType/REPAIR_1']);
    // a color shows in it, the Cylon power's Treachery too
    expect(art({ kind: 'DrawSkillCardsOfColorEvent', playerNumber: 1, color: 'TACTICS', count: 2 })).toEqual(['SkillCardColor/TACTICS']);
    expect(art({ kind: 'CylonPowerDrawTreacheryEvent', playerNumber: 1 })).toEqual(['SkillCardColor/TREACHERY']);
  });

  it('show the ships an answer names, and a viper on the option that asks for an activation', () => {
    const types: Record<number, string> = { 3: 'VIPER', 40: 'RAIDER' };
    const withShips = { ...table, ship: (id: number) => ({ id, type: types[id] }) as unknown as ShipProjection };
    const decision = { kind: 'Single', event: { kind: 'PlayerDecisionEvent', playerNumber: 1, action: 'ActivateViperEvent', eventConstraints: [] } } as unknown as Followup;

    expect(keys(moveArt(answer({ kind: 'AttackCylonShipEvent', humanFighterId: 3, cylonShipId: 40 }), withShips))).toEqual(['ShipType/VIPER', 'ShipType/RAIDER']);
    expect(keys(actionArt({ option: 0, followup: decision, event: null, submits: false, pick: null }))).toEqual(['ShipType/VIPER']);
  });

  it('show the resources an answer loses or gains by their icons, in its order', () => {
    const trade = { kind: 'AllOf', followups: [
      { kind: 'Single', event: { kind: 'LoseResourceEvent', resource: 'MORALE', amount: 1 } },
      { kind: 'Single', event: { kind: 'GainResourceEvent', resource: 'FUEL', amount: 1 } },
    ] } as unknown as Followup;

    expect(keys(moveArt({ option: 0, followup: trade, event: null, submits: false, pick: null }, table))).toEqual(['ResourceIcon/MORALE', 'ResourceIcon/FUEL']);
    expect(art({ kind: 'PreventivePolicyEvent', playerNumber: 1, card: { type: 'PREVENTIVE_POLICY', value: 3 }, resource: 'FOOD' })).toEqual([
      'SkillCardType/PREVENTIVE_POLICY_3',
      'ResourceIcon/FOOD',
    ]);
    expect(moveArt(answer({ kind: 'LoseResourceEvent', resource: 'POPULATION', amount: 1 }), table)[0]).toMatchObject({ shape: 'icon', label: 'Population' });
  });

  it("show a part's value: a player, an ability with its miracle token", () => {
    expect(keys(partValueArt(2, 'targetPlayerNumber', { t: 'int', nullable: false, role: 'player' }, table))).toEqual(['Character/player-token/LAURA_ROSLIN']);
    expect(keys(partValueArt('LONGSHOT', 'ability', { t: 'enum', nullable: false, name: 'CharacterAbility' }, table))).toEqual([
      'Character/player-token/SAMUEL_T_ANDERS',
      'Token/MIRACLE',
    ]);
  });

  it('know the miracle abilities from the schema', () => {
    expect([isMiracle('BROADCAST'), isMiracle('SECRET_DESTINY'), isMiracle('MEMENTO'), isMiracle(null)]).toEqual([true, true, false, false]);
  });
});

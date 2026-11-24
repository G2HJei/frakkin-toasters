import { describe, expect, it } from 'vitest';
import { actionArt, ART_KINDS, moveArt, partValueArt } from './answerArt';
import type { Answer, Move } from './answers';
import { titleArt } from '../art/images';
import { tableContext } from '../table/tableContext';
import { isMiracle, recordNamed, schema } from '../wire/schema';
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
  it('name events of the schema', () => {
    expect(ART_KINDS.filter((kind) => !recordNamed(kind) || !schema.lists.events.includes(kind))).toEqual([]);
  });

  it("show a target by his character's portrait, not the player who answers, and no location", () => {
    expect(art({ kind: 'ExecutiveOrderEvent', playerNumber: 1, card: { type: 'EXECUTIVE_ORDER', value: 2 }, targetPlayerNumber: 2 })).toEqual([
      'SkillCardType/EXECUTIVE_ORDER_2',
      'Character/player-token/LAURA_ROSLIN',
    ]);
    expect(art({ kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null })).toEqual([]);
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
    expect(art({ kind: 'ResearchLabEvent', playerNumber: 1, skillCardColor: 'TACTICS' })).toEqual(['SkillCardColor/TACTICS']);
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

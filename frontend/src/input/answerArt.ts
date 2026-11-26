// The images on an answer's button: what the answer names, the card or ability it comes from, and what it does where
// its fields don't say, e.g. the back of the deck it draws from or the step it acts in. A player shows as his
// character's portrait, a title as its insignia, an ability as the portrait of the character it is printed on, a die
// roll as the die's face, a resource as the board's icon, a location as the map pin (the board shows its card), and an
// answer of a miracle ability ends with the miracle token.
import {
  cardArt,
  characterArt,
  deckBackArt,
  deckPlaceArt,
  deckCards,
  dieArt,
  enumArt,
  isTitleName,
  locationPinArt,
  resourceIconArt,
  shipArt,
  stepIconArt,
  skillBackArt,
  skillColorBackArt,
  skillTypeArt,
  titleIconArt,
  tokenArt,
  type ArtRef,
  type TitleName,
} from '../art/images';
import { recordArt, unique, type ValueArt } from '../art/refs';
import type { TableContext } from '../table/tableContext';
import { abilityCharacter, isMiracle, recordNamed, type TypeRef } from '../wire/schema';
import type { Followup } from '../wire/types';
import { decisionAction, type Answer, type Move } from './answers';
import { isServerFilled } from './eventModel';

type Found = ArtRef | null;

const title = (name: TitleName): Found => titleIconArt(name);
const back = (deck: string): Found => deckBackArt(deck);
const movement = (): Found => stepIconArt('MOVEMENT');
const action = (): Found => stepIconArt('ACTION');

// What an event, or a decision on it, does that its fields don't name: the deck it draws from, the Skill Cards it takes
// without naming them, the title it acts with, a move as the turn panel's Movement step, an action as its Action step,
// where a card goes in its deck. A draw of a color shows its color instead (`SkillCardColor`).
const KIND_ART: Record<string, () => Found[]> = {
  MoveEvent: () => [movement()],
  LeaveBrigEvent: () => [movement()],
  ActionEvent: () => [action()],
  DrawQuorumCardEvent: () => [back('QUORUM')],
  DrawMutinyCardEvent: () => [back('MUTINY')],
  DrawSkillCardsEvent: () => [skillBackArt()],
  DrawAndResolveRecklessCardsEvent: () => [skillBackArt()],
  DiscardRandomSkillCardsEvent: () => [skillBackArt()],
  DiscardAllSkillCardsEvent: () => [skillBackArt()],
  PlayFromDestinyDeckEvent: () => [skillBackArt()],
  DiscardFromDestinyDeckEvent: () => [skillBackArt()],
  CylonPowerDrawTreacheryEvent: () => [skillColorBackArt('TREACHERY')],
  DrawDestinationCardsEvent: () => [back('DESTINATION')],
  // the cards drawn show once above the options: each option keeps the back
  ChooseDestinationEvent: () => [back('DESTINATION')],
  DrawAndDestroyCivilianShipsEvent: () => [shipArt('CIVILIAN')],
  PresidentTitleActionEvent: () => [title('PRESIDENT'), back('QUORUM')],
  DefendPresidencyEvent: () => [title('PRESIDENT')],
  MakePresidentEvent: () => [title('PRESIDENT')],
  MakeAdmiralEvent: () => [title('ADMIRAL')],
  MakeCagEvent: () => [title('CAG')],
  // the area it hits at its pin; only the Admiral holds nukes
  LaunchNukeEvent: () => [tokenArt('NUKE')],
  DiscardNukeTokenEvent: () => [tokenArt('NUKE')],
  PassCagTitleEvent: () => [title('CAG')],
  CagPilotActionEvent: () => [title('CAG')],
  PlaceCivilianShipEvent: () => [shipArt('CIVILIAN')],
  DamageGalacticaEvent: () => [back('GALACTICA_DAMAGE')],
  DamagePegasusEvent: () => [back('PEGASUS_DAMAGE')],
  CylonPowerDamageGalacticaEvent: () => [back('GALACTICA_DAMAGE')],
  CylonPowerReduceMoraleEvent: () => [resourceIconArt('MORALE')],
  CylonPowerDecreaseJumpTrackEvent: () => [stepIconArt('PREPARE_FOR_JUMP')],
  CylonPowerPlaceCenturionEvent: () => [tokenArt('CENTURION')],
  PlaceCrisisCardOnTopEvent: () => [deckPlaceArt('DECK_TOP')],
  PlaceCrisisCardOnBottomEvent: () => [deckPlaceArt('DECK_BOTTOM')],
  PlaceDestinationCardOnTopEvent: () => [deckPlaceArt('DECK_TOP')],
  PlaceDestinationCardAtBottomEvent: () => [deckPlaceArt('DECK_BOTTOM')],
  GasCloudPlaceEvent: () => [deckPlaceArt('DECK_TOP')],
};

// What each ability of the "Basestar Bridge" acts on.
const BRIDGE_ABILITY_ART: Record<string, () => Found[]> = {
  CAG_PLACES_CIVILIAN_SHIP: () => [title('CAG'), shipArt('CIVILIAN')],
  JUMP_OR_PURSUIT: () => [stepIconArt('PREPARE_FOR_JUMP')],
  BASESTAR_OR_RAIDERS: () => [shipArt('BASESTAR'), shipArt('RAIDER')],
  GALACTICA_DAMAGE: () => [back('GALACTICA_DAMAGE')],
};

// The kinds whose image says what their location is for, so it shows no pin: a move.
const PLACED_BY_KIND = new Set(['MoveEvent', 'LeaveBrigEvent']);

// A decision that an option of a choice asks, as the head of its answers: the activation of a viper, a "Movement:"
// card or ability, a location's action, an interrupt card of one type, a discard of Mutiny or Quorum Cards (each answer
// shows its card or its character)
const ACTION_ART: Record<string, () => Found[]> = {
  ActivateViperEvent: () => [shipArt('VIPER')],
  MovementAbilityEvent: () => [movement()],
  LocationEvent: () => [locationPinArt()],
  DeclareEmergencyEvent: () => [skillTypeArt('DECLARE_EMERGENCY')],
  ChangeOfPlansEvent: () => [skillTypeArt('CHANGE_OF_PLANS')],
  PoliticalProwessEvent: () => [skillTypeArt('POLITICAL_PROWESS')],
  Discard1MutinyCardEvent: () => [back('MUTINY')],
  DiscardDownTo1MutinyCardEvent: () => [back('MUTINY')],
  DiscardQuorumCardsEvent: () => [back('QUORUM')],
  DiscardHeldQuorumCardEvent: () => [back('QUORUM')],
};

/** The event kinds the images above are for, which a test holds to the schema. */
export const ART_KINDS = [...Object.keys(KIND_ART), ...Object.keys(ACTION_ART)];

export function kindArt(kind: string | null | undefined): ArtRef[] {
  return present(kind ? (KIND_ART[kind]?.() ?? []) : []);
}

/** The images of an option that asks a decision first, e.g. "Activate a viper", "Take an action". */
export function actionArt(move: Move | undefined): ArtRef[] {
  const asked = move ? decisionAction(move) : null;
  return asked ? decisionArt(asked) : [];
}

// a decision by the kind it asks for: what the kind does, the card or ability it comes from, the card it plays
function decisionArt(asked: string): ArtRef[] {
  return present([...(ACTION_ART[asked]?.() ?? []), ...kindArt(asked), ...sourceArt(asked), ...playedCardArt(asked)]);
}

/**
 * The images of a move's button, the miracle token last. The part a shape is still to pick shows the back of its cards,
 * as its example isn't what will be picked, e.g. the Skill Cards to play into a skill check.
 */
export function moveArt(move: Move | undefined, table: TableContext): ArtRef[] {
  if (!move) {
    return [];
  }
  const events = move.event ? [move.pick?.component ? { ...move.event, [move.pick.component]: null } : move.event] : followupEvents(move.followup);
  const shown = unique([
    ...events.flatMap((event) =>
      event.kind === 'PlayerDecisionEvent'
        ? decisionArt(String(event.action))
        : [
            ...kindArt(event.kind),
            ...sourceArt(event.kind),
            ...playedCardArt(event.kind),
            ...locationActionArt(event.kind),
            ...recordArt(event, undefined, answerValueArt(table, event.kind)),
          ],
    ),
    ...pickArt(move),
  ]);
  return events.some(fromMiracle) ? [...shown, miracleArt()].filter((art): art is ArtRef => art !== null) : shown;
}

// the back of the cards a shape's part picks: Skill Cards or Quorum Cards; a ship to pick is on the board
function pickArt(move: Move): ArtRef[] {
  switch (move.pick?.kind) {
    case 'SkillCardPick':
      return present([skillBackArt()]);
    case 'QuorumCardPick':
      return present([back('QUORUM')]);
    default:
      return [];
  }
}

export function miracleArt(): ArtRef | null {
  return tokenArt('MIRACLE');
}

/** Whether the miracle token is one of the images, which stays on every button of a miracle ability. */
export function isMiracleArt(art: ArtRef): boolean {
  return art.key === miracleArt()?.key;
}

/**
 * Whether an image stays on every button of its group instead of going with the group's name: the miracle token, and
 * the step a move acts in, which a section's lone group, without a name, would otherwise lose.
 */
export function staysOnEachAnswer(art: ArtRef): boolean {
  return isMiracleArt(art) || art.key === movement()?.key;
}

/** The images of a value a part of an answer picks, e.g. a target player, an ability or a die's result. */
export function partValueArt(value: unknown, name: string, type: TypeRef, table: TableContext): ArtRef[] {
  if (value === null || value === undefined) {
    return [];
  }
  const own = answerValueArt(table)(value, { name, type }, { kind: 'record', interfaces: [], components: [] });
  const shown = own ?? present([type.t === 'enum' ? enumArt(type.name ?? '', value as string) : null]);
  const miracle = type.t === 'enum' && type.name === 'CharacterAbility' && isMiracle(value as string) ? [miracleArt()] : [];
  return present([...shown, ...miracle]);
}

// what the answers show for a value instead of the log's image, undefined for the same
function answerValueArt(table: TableContext, kind?: string): ValueArt {
  return (value, component, record) => {
    const type = component.type;
    if (type.t === 'enum' && typeof value === 'string') {
      switch (type.name) {
        case 'Location':
          // a viper's move shows the step it moves in
          if (component.name === 'moveLocation') {
            return present([movement()]);
          }
          return kind && PLACED_BY_KIND.has(kind) ? [] : [locationPinArt(value)];
        case 'Title':
          return present([isTitleName(value) ? titleIconArt(value) : null]);
        case 'CharacterAbility':
          return present([characterArt(abilityCharacter(value))]);
        case 'DeckId':
          return present([deckBackArt(value) ?? skillColorBackArt(value) ?? (deckCards(value) === 'SkillCard' ? skillBackArt() : null)]);
        case 'SkillCardColor':
          return present([skillColorBackArt(value)]);
        case 'Resource':
          return present([resourceIconArt(value)]);
        case 'Grant':
          // what a card or an ability lets a player do: the steps' icons
          return present([value === 'ACTION' ? null : movement(), value === 'MOVE' ? null : action()]);
        case 'BasestarBridgeAbility':
          return present(BRIDGE_ABILITY_ART[value]?.() ?? []);
        default:
          return undefined;
      }
    }
    if ((type.t === 'int' || type.t === 'long') && typeof value === 'number') {
      // a PlayerEvent's playerNumber is who answers; a plain event's names a target
      if (isServerFilled(component) && !(component.name === 'playerNumber' && !record.interfaces.includes('PlayerEvent'))) {
        return [];
      }
      if (type.role === 'player') {
        return present([characterArt(table.game?.players?.find((player) => player?.number === value)?.character)]);
      }
      if (type.role === 'ship') {
        return present([shipArt(table.ship(value)?.type)]);
      }
      if (component.name === 'result') {
        return present([dieArt(value)]);
      }
    }
    return undefined;
  };
}

// the card or ability an event exists for, where its fields don't name it: an ability by its character's portrait
function sourceArt(kind: string): ArtRef[] {
  const source = recordNamed(kind)?.source;
  if (!source) {
    return [];
  }
  return present([source.enum === 'CharacterAbility' ? characterArt(abilityCharacter(source.constant)) : enumArt(source.enum, source.constant)]);
}

// the Quorum Card or the Mutiny Card an event plays
function playedCardArt(kind: string): ArtRef[] {
  const record = recordNamed(kind);
  return present([record?.quorumCard ? cardArt('QuorumCard', record.quorumCard) : null, record?.mutinyCard ? cardArt('MutinyCard', record.mutinyCard) : null]);
}

// the action of a location, at its pin: the board shows the location
function locationActionArt(kind: string): ArtRef[] {
  const location = recordNamed(kind)?.location;
  return location ? [locationPinArt(location)] : [];
}

// an event of a miracle ability: its source, or an ability it names
function fromMiracle(event: Answer): boolean {
  const record = recordNamed(event.kind);
  if (record?.source?.enum === 'CharacterAbility' && isMiracle(record.source.constant)) {
    return true;
  }
  return (record?.components ?? []).some((component) => component.type.t === 'enum' && component.type.name === 'CharacterAbility' && isMiracle(event[component.name] as string | null));
}

// the events an option of a choice runs, a decision too: it shows the kind it asks for
function followupEvents(followup: Followup | null | undefined): Answer[] {
  switch (followup?.kind) {
    case 'Single': {
      const event = followup.event as unknown as Answer | null;
      return event ? [event] : [];
    }
    case 'AllOf':
      return (followup.followups ?? []).flatMap(followupEvents);
    default:
      return [];
  }
}

function present(found: Found[]): ArtRef[] {
  return unique(found.filter((art): art is ArtRef => art !== null));
}

/** The Pass of a choice or a decision: a `NoOpEvent`. */
export function isNoOp(move: Move): boolean {
  return move.event?.kind === 'NoOpEvent';
}

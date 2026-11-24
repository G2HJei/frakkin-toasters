// The images on an answer's button: what the answer names, the card or ability it comes from, and what it does where
// its fields don't say, e.g. the back of the deck it draws from. A player shows as his character's portrait, a title as
// its insignia, an ability as the portrait of the character it is printed on, a die roll as the die's face, and an
// answer of a miracle ability ends with the miracle token. The board shows the locations: none of their cards.
import {
  characterArt,
  deckBackArt,
  deckCards,
  dieArt,
  enumArt,
  isTitleName,
  shipArt,
  skillBackArt,
  skillColorBackArt,
  titleIconArt,
  tokenArt,
  type ArtRef,
  type TitleName,
} from '../art/images';
import { recordArt, unique } from '../art/refs';
import type { TableContext } from '../table/tableContext';
import { abilityCharacter, isMiracle, recordNamed, type Component, type TypeRef } from '../wire/schema';
import type { Followup } from '../wire/types';
import { decisionAction, type Answer, type Move } from './answers';
import { isServerFilled } from './eventModel';

type Found = ArtRef | null;

const title = (name: TitleName): Found => titleIconArt(name);
const back = (deck: string): Found => deckBackArt(deck);

// What an event, or a decision on it, does that its fields don't name: the deck it draws from, the Skill Cards it takes
// without naming them, the title it acts with. A draw of a color shows its color instead (`SkillCardColor`).
const KIND_ART: Record<string, () => Found[]> = {
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
  DrawAndDestroyCivilianShipsEvent: () => [shipArt('CIVILIAN')],
  PresidentTitleActionEvent: () => [title('PRESIDENT'), back('QUORUM')],
  DefendPresidencyEvent: () => [title('PRESIDENT')],
  MakePresidentEvent: () => [title('PRESIDENT')],
  MakeAdmiralEvent: () => [title('ADMIRAL')],
  MakeCagEvent: () => [title('CAG')],
  LaunchNukeEvent: () => [title('ADMIRAL'), tokenArt('NUKE')],
  DiscardNukeTokenEvent: () => [tokenArt('NUKE')],
  PassCagTitleEvent: () => [title('CAG')],
  CagPilotActionEvent: () => [title('CAG')],
};

// A decision that an option of a choice asks, as the head of its answers: the activation of a viper
const ACTION_ART: Record<string, () => Found[]> = {
  ActivateViperEvent: () => [shipArt('VIPER')],
};

/** The event kinds the images above are for, which a test holds to the schema. */
export const ART_KINDS = [...Object.keys(KIND_ART), ...Object.keys(ACTION_ART)];

export function kindArt(kind: string | null | undefined): ArtRef[] {
  return present(kind ? (KIND_ART[kind]?.() ?? []) : []);
}

/** The images of an option that asks a decision first, e.g. "Activate a viper". */
export function actionArt(move: Move | undefined): ArtRef[] {
  const action = move ? decisionAction(move) : null;
  return action ? present([...(ACTION_ART[action]?.() ?? []), ...kindArt(action)]) : [];
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
    ...events.flatMap((event) => [...kindArt(event.kind), ...sourceArt(event.kind), ...recordArt(event, undefined, answerValueArt(table))]),
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

/** The images of a value a part of an answer picks, e.g. a target player, an ability or a die's result. */
export function partValueArt(value: unknown, name: string, type: TypeRef, table: TableContext): ArtRef[] {
  if (value === null || value === undefined) {
    return [];
  }
  const own = answerValueArt(table)(value, { name, type }, { interfaces: [] });
  const shown = own ?? present([type.t === 'enum' ? enumArt(type.name ?? '', value as string) : null]);
  const miracle = type.t === 'enum' && type.name === 'CharacterAbility' && isMiracle(value as string) ? [miracleArt()] : [];
  return present([...shown, ...miracle]);
}

// what the answers show for a value instead of the log's image, undefined for the same
function answerValueArt(table: TableContext) {
  return (value: unknown, component: Component, record: { interfaces: string[] }): ArtRef[] | undefined => {
    const type = component.type;
    if (type.t === 'enum' && typeof value === 'string') {
      switch (type.name) {
        case 'Location':
          return [];
        case 'Title':
          return present([isTitleName(value) ? titleIconArt(value) : null]);
        case 'CharacterAbility':
          return present([characterArt(abilityCharacter(value))]);
        case 'DeckId':
          return present([deckBackArt(value) ?? skillColorBackArt(value) ?? (deckCards(value) === 'SkillCard' ? skillBackArt() : null)]);
        case 'SkillCardColor':
          return present([skillColorBackArt(value)]);
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

// an event of a miracle ability: its source, or an ability it names
function fromMiracle(event: Answer): boolean {
  const record = recordNamed(event.kind);
  if (record?.source?.enum === 'CharacterAbility' && isMiracle(record.source.constant)) {
    return true;
  }
  return (record?.components ?? []).some((component) => component.type.t === 'enum' && component.type.name === 'CharacterAbility' && isMiracle(event[component.name] as string | null));
}

// the events an option of a choice runs, a decision's none
function followupEvents(followup: Followup | null | undefined): Answer[] {
  switch (followup?.kind) {
    case 'Single': {
      const event = followup.event as unknown as Answer | null;
      return event && event.kind !== 'PlayerDecisionEvent' ? [event] : [];
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

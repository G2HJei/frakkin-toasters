// The words of a move: a group's title (the action, the card, the location), what tells its moves apart, and what a
// shape asks to pick. Names only, never a card's printed text.
import { constantLabel, eventLabel, optionLabel, typeLabel, valueLabel, type LabelContext } from '../labels/describe';
import { humanizeType } from '../labels/humanize';
import { recordNamed } from '../wire/schema';
import type { Pick } from '../wire/types';
import { isServerFilled } from './eventModel';
import type { Move, MoveGroup } from './answers';

export function groupTitle(group: MoveGroup, context: LabelContext): string {
  const first = group.moves[0];
  if (!group.kind) {
    return optionLabel(first?.followup, context);
  }
  if (group.kind === 'NoOpEvent') {
    return 'Pass';
  }
  const record = recordNamed(group.kind);
  if (record?.quorumCard) {
    return constantLabel('QuorumCard', record.quorumCard);
  }
  if (record?.mutinyCard) {
    return constantLabel('MutinyCard', record.mutinyCard);
  }
  if (record?.location) {
    return constantLabel('Location', record.location);
  }
  // a Skill Card played for its ability is named after the card
  const card = first?.event?.card as { type?: string } | null | undefined;
  if (record?.interfaces.includes('SkillCardEvent') && card?.type) {
    return constantLabel('SkillCardType', card.type);
  }
  return typeLabel(group.kind);
}

/**
 * What sets a move apart in its group: the values of the components the group's moves differ in, "Pass" when they are
 * all empty, and a shape's pick to come.
 */
export function moveDetail(move: Move, components: string[], context: LabelContext): string {
  const event = move.event;
  if (!event) {
    return optionLabel(move.followup, context);
  }
  const record = recordNamed(event.kind);
  const parts = components
    .filter((name) => name !== move.pick?.component)
    .map((name) => {
      const value = event[name];
      const component = record?.components.find((candidate) => candidate.name === name);
      if (value === null || value === undefined || (Array.isArray(value) && value.length === 0)) {
        return null;
      }
      return valueLabel(value, component?.type, context);
    })
    .filter((part): part is string => part !== null);
  const pick = move.pick ? pickHint(move.pick) : null;
  if (parts.length === 0) {
    return pick ?? (components.length > 0 ? 'Pass' : '');
  }
  return pick ? `${parts.join(' · ')} · ${pick}` : parts.join(' · ');
}

/** The components of a lone move worth naming: what it targets or chooses, the played card left to its image. */
export function shownComponents(move: Move): string[] {
  const kind = move.event?.kind;
  return (recordNamed(kind ?? '')?.components ?? [])
    .filter((component) => !isServerFilled(component))
    .filter((component) => !(component.type.t === 'ref' && component.type.name === 'SkillCard' && component.name === 'card'))
    .map((component) => component.name);
}

/** "discard 1 card", "choose 1 to 3 cards", "choose 2 ships": a discard by its component's name. */
export function pickHint(pick: Pick): string {
  const things = pick.kind === 'ShipPick' ? 'ship' : 'card';
  const count = pick.min === pick.max ? `${pick.min}` : `${pick.min} to ${pick.max}`;
  const verb = /^discard/i.test(pick.component ?? '') ? 'discard' : 'choose';
  return `${verb} ${count} ${things}${pick.max === 1 ? '' : 's'}`;
}

/** A pick's component as a heading, e.g. "Discard card". */
export function pickName(pick: Pick): string {
  const name = humanizeType(pick.component ?? '');
  return name.charAt(0).toUpperCase() + name.substring(1);
}

/** The whole move as a sentence, for a title on hover. */
export function moveSentence(move: Move, context: LabelContext): string {
  return move.event ? eventLabel(move.event, context) : optionLabel(move.followup, context);
}

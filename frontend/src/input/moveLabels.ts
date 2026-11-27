// The words of a move: a group's title (the action, the card, the location), what tells its moves apart, and what a
// shape asks to pick. Names only, never a card's printed text.
import { constantLabel, optionEventParts, optionLabel, partsText, typeLabel, valueLabel, type LabelContext } from '../labels/describe';
import { humanizeType } from '../labels/humanize';
import { recordNamed, type TypeRef } from '../wire/schema';
import type { Pick } from '../wire/types';
import { isServerFilled } from './eventModel';
import type { Move, MoveGroup } from './answers';
import type { Part } from './parts';

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
    // an action is never a pass, it uses the action even when it names nothing, e.g. "Repair" of the location
    return pick ?? (components.length > 0 && !takesAction(record?.interfaces) ? 'Pass' : '');
  }
  return pick ? `${parts.join(' · ')} · ${pick}` : parts.join(' · ');
}

function takesAction(interfaces: string[] | undefined): boolean {
  return !!interfaces?.some((name) => name === 'ActionEvent' || name === 'MovementAbilityEvent');
}

export function shownComponents(move: Move): string[] {
  const kind = move.event?.kind;
  return (recordNamed(kind ?? '')?.components ?? [])
    .filter((component) => !isServerFilled(component))
    .filter((component) => !(component.type.t === 'ref' && component.type.name === 'SkillCard' && component.name === 'card'))
    .map((component) => component.name);
}

export function pickHint(pick: Pick): string {
  const things = pick.kind === 'ShipPick' ? 'ship' : 'card';
  const count = pick.min === pick.max ? `${pick.min}` : `${pick.min} to ${pick.max}`;
  const verb = /^discard/i.test(pick.component ?? '') ? 'discard' : 'choose';
  return `${verb} ${count} ${things}${pick.max === 1 ? '' : 's'}`;
}

export function pickName(pick: Pick): string {
  const name = humanizeType(pick.component ?? '');
  return name.charAt(0).toUpperCase() + name.substring(1);
}

export function partName(part: Part): string {
  const name = humanizeType(part.key.replace(/(Number|Ids?)$/, ''));
  return name.charAt(0).toUpperCase() + name.substring(1);
}

export function partHint(part: Part, size: { min: number; max: number }): string {
  const count = size.min === size.max ? `${size.min}` : `${size.min} to ${size.max}`;
  const verb = /^discard/i.test(part.key) ? 'discard' : 'choose';
  const thing = partThing(part.type);
  return thing ? `${verb} ${count} ${thing}${size.max === 1 ? '' : 's'}` : `${verb} ${count}`;
}

function partThing(type: TypeRef): string | null {
  if ((type.t === 'ref' && type.name === 'SkillCard') || (type.t === 'enum' && /Card$/.test(type.name ?? ''))) {
    return 'card';
  }
  if (type.t === 'enum' && /Damage$/.test(type.name ?? '')) {
    return 'token';
  }
  if (type.role === 'ship' || (type.t === 'enum' && type.name === 'ShipType')) {
    return 'ship';
  }
  return null;
}

export function moveSentence(move: Move, context: LabelContext): string {
  return move.event ? partsText(optionEventParts(move.event, context)) : optionLabel(move.followup, context);
}

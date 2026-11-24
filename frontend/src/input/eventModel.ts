// An event as the client sends it: the event as the codec writes it, `kind` and every component in order. The
// sandbox's setup form builds one from the schema. The server fills in what a client must not forge: the player of a
// player's event, and the constraints.
import { enumValues, recordNamed, type Component, type TypeRef } from '../wire/schema';

export type Value = unknown;
export type EventValue = Record<string, Value> & { kind: string };

/** A component a client doesn't fill in, because the server sets it. */
export function isServerFilled(component: Component): boolean {
  return (
    component.name === 'playerNumber' ||
    component.name === 'mustLaunchInViper' ||
    component.name === 'anywhere' ||
    (component.type.t === 'enum' && component.type.name === 'EventConstraint')
  );
}

/** The value a field starts with. */
export function emptyValue(type: TypeRef): Value {
  switch (type.t) {
    case 'int':
    case 'long':
      return type.nullable || type.role === 'player' || type.role === 'ship' ? null : 0;
    case 'boolean':
      return type.nullable ? null : false;
    case 'string':
    case 'class':
    case 'var':
      return null;
    case 'enum':
      return type.nullable ? null : (enumValues(type.name ?? '')[0] ?? null);
    case 'list':
    case 'set':
    case 'cards':
      return [];
    case 'map':
      return {};
    case 'ref':
      return type.nullable ? null : emptyRecord(type.name ?? '');
    default:
      return null;
  }
}

/** A record with every component at its empty value, with `kind` when the record carries it. */
export function emptyRecord(name: string, playerNumber?: number | null): Record<string, Value> {
  const record = recordNamed(name);
  const value: Record<string, Value> = {};
  if (record?.tagged) {
    value.kind = name;
  }
  for (const component of record?.components ?? []) {
    value[component.name] = component.name === 'playerNumber' && playerNumber != null ? playerNumber : emptyValue(component.type);
  }
  return value;
}

export function emptyEvent(kind: string, playerNumber?: number | null): EventValue {
  return { ...emptyRecord(kind, playerNumber), kind } as EventValue;
}

/** The fields of a record a form asks for. */
export function askedComponents(kind: string): Component[] {
  return (recordNamed(kind)?.components ?? []).filter((component) => !isServerFilled(component));
}

/**
 * @returns the event as the codec reads it: `kind` first, then the components in their order, so it compares equal to
 * what the backend writes
 */
export function toWire(event: EventValue): EventValue {
  const record = recordNamed(event.kind);
  const wire: Record<string, Value> = { kind: event.kind };
  for (const component of record?.components ?? []) {
    wire[component.name] = event[component.name] ?? null;
  }
  return wire as EventValue;
}

/** What is missing before the event can be sent, for a hint next to the submit button. */
export function missingFields(event: EventValue): string[] {
  return askedComponents(event.kind)
    .filter((component) => !component.type.nullable && (event[component.name] === null || event[component.name] === undefined))
    .map((component) => component.name);
}

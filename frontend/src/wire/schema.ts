// Typed access to the wire schema the backend exports, which the event builder makes its forms from.
import schemaJson from './schema.json';

export interface TypeRef {
  t: 'int' | 'long' | 'boolean' | 'string' | 'enum' | 'class' | 'list' | 'set' | 'map' | 'ref' | 'var' | 'cards';
  nullable: boolean;
  name?: string;
  role?: 'player' | 'ship' | 'scope' | 'count' | null;
  of?: TypeRef | string | null;
  key?: TypeRef;
  value?: TypeRef;
  args?: TypeRef[];
}

export interface Component {
  name: string;
  type: TypeRef;
}

export interface RecordType {
  kind: 'record';
  tagged?: boolean;
  type?: string;
  interfaces: string[];
  typeParams?: string[];
  location?: string | null;
  // the card a Quorum Card's or a Mutiny Card's event plays
  quorumCard?: string | null;
  mutinyCard?: string | null;
  components: Component[];
}

export interface UnionType {
  kind: 'union';
  discriminator: 'kind' | 'type';
  members: string[];
}

export interface EnumType {
  kind: 'enum';
  values: string[];
}

export type SchemaType = RecordType | UnionType | EnumType;

export interface Schema {
  formatVersion: number;
  types: Record<string, SchemaType>;
  // anytimeEvents: what a player may do at any pause, without being asked (`AnytimeEvent`)
  lists: { events: string[]; sandboxEvents: string[]; anytimeEvents: string[]; facts: string[] };
  messages: Record<string, string>;
}

export const schema = schemaJson as unknown as Schema;

export function typeNamed(name: string): SchemaType | undefined {
  return schema.types[name];
}

export function recordNamed(name: string): RecordType | undefined {
  const type = schema.types[name];
  return type?.kind === 'record' ? type : undefined;
}

export function enumValues(name: string): string[] {
  const type = schema.types[name];
  return type?.kind === 'enum' ? type.values : [];
}

/**
 * @returns the concrete events an action asks for: the event itself, or the members of an interface such as ActionEvent
 */
export function concreteEvents(action: string): string[] {
  const type = schema.types[action];
  if (!type) {
    return [];
  }
  if (type.kind === 'record') {
    return [action];
  }
  if (type.kind === 'union') {
    return type.members.filter((member) => recordNamed(member) && schema.lists.events.includes(member));
  }
  return [];
}

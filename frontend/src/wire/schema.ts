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

/** A card or an ability, as an enum of the schema and its constant: what an event exists for (`Source` in core). */
export interface SourceRef {
  enum: string;
  constant: string;
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
  // the card or ability the event exists for, where its fields don't name it
  source?: SourceRef | null;
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
  // SkillCardType only: the color of each card type
  colors?: Record<string, string>;
  // CharacterAbility only: the character whose sheet prints each ability, and the miracle tokens a miracle one spends
  characters?: Record<string, string>;
  miracles?: Record<string, number>;
  // Character only: the type of each character, and the skill set his sheet prints, in its order
  types?: Record<string, string>;
  skillSets?: Record<string, SkillSetOption[]>;
  // what a constant stands for, e.g. the card a GrantedBy names
  sources?: Record<string, SourceRef>;
}

/** A line of a skill set: draw `count` Skill Cards, each of any of the colors. */
export interface SkillSetOption {
  count: number;
  colors: string[];
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

/** The character whose sheet prints an ability, e.g. LEE_APOLLO_ADAMA for ALERT_VIPER_PILOT. */
export function abilityCharacter(ability: string | null | undefined): string | null {
  const characterAbility = schema.types.CharacterAbility;
  return ability && characterAbility?.kind === 'enum' ? (characterAbility.characters?.[ability] ?? null) : null;
}

/** Whether an ability is a miracle ability ("Once per game"), which spends miracle tokens, e.g. SECRET_DESTINY. */
export function isMiracle(ability: string | null | undefined): boolean {
  const characterAbility = schema.types.CharacterAbility;
  return !!ability && characterAbility?.kind === 'enum' && (characterAbility.miracles?.[ability] ?? 0) > 0;
}

/** The type of a character, e.g. CYLON_LEADER for CAVIL. */
export function characterType(character: string | null | undefined): string | null {
  const characters = schema.types.Character;
  return character && characters?.kind === 'enum' ? (characters.types?.[character] ?? null) : null;
}

/** The skill set a character's sheet prints, e.g. 3 LEADERSHIP and 2 TACTICS for WILLIAM_ADAMA. */
export function characterSkillSet(character: string | null | undefined): SkillSetOption[] {
  const characters = schema.types.Character;
  return character && characters?.kind === 'enum' ? (characters.skillSets?.[character] ?? []) : [];
}

/** The color of a Skill Card type, e.g. POLITICS for RED_TAPE. */
export function skillCardColor(type: string | null | undefined): string | null {
  const skillCardType = schema.types.SkillCardType;
  return type && skillCardType?.kind === 'enum' ? (skillCardType.colors?.[type] ?? null) : null;
}

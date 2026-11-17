// One widget per component type: a player, a ship, a number, a checkbox, a constant, cards of a hand or a deck, counts
// per color, a nested form. "None" for a nullable field. A shape the builder doesn't know falls back to raw JSON.
import { useState } from 'react';
import { constantLabel, skillCardLabel, typeLabel } from '../../labels/describe';
import { humanizeType } from '../../labels/humanize';
import { enumValues, recordNamed, schema, type Component, type TypeRef } from '../../wire/schema';
import type { PlayedCard, PlayedInterruptCard, SkillCard } from '../../wire/types';
import { useTable } from '../../table/tableContext';
import { emptyRecord, isServerFilled, type Value } from '../eventModel';

export interface FieldProps {
  component: Component;
  value: Value;
  onChange(value: Value): void;
  // the record the field belongs to, so a card picker knows a setup from an answer
  owner: string;
}

export function Field({ component, value, onChange, owner }: FieldProps) {
  const label = humanizeType(component.name);
  return (
    <div className="field" data-field={component.name}>
      <span className="field-name">{label}</span>
      <Widget type={component.type} value={value} onChange={onChange} owner={owner} name={label} />
    </div>
  );
}

interface WidgetProps {
  type: TypeRef;
  value: Value;
  onChange(value: Value): void;
  owner: string;
  name: string;
}

function Widget(props: WidgetProps) {
  const { type, value, onChange } = props;
  switch (type.t) {
    case 'int':
    case 'long':
      if (type.role === 'player') {
        return <PlayerPicker {...props} />;
      }
      if (type.role === 'ship') {
        return <ShipPicker {...props} />;
      }
      return <NumberInput {...props} />;
    case 'boolean':
      return <input type="checkbox" aria-label={props.name} checked={value === true} onChange={(event) => onChange(event.target.checked)} />;
    case 'enum':
      return <EnumSelect {...props} />;
    case 'class':
      return <ClassSelect {...props} />;
    case 'ref':
      if (type.name === 'SkillCard') {
        return <SkillCardPicker {...props} />;
      }
      return <NestedRecord {...props} />;
    case 'list':
    case 'set':
      return <ListWidget {...props} />;
    case 'map':
      return <MapCounts {...props} />;
    default:
      return <RawJson {...props} />;
  }
}

function Nullable({ type, value, onChange, children, empty }: WidgetProps & { children: React.ReactNode; empty: Value }) {
  if (!type.nullable) {
    return <>{children}</>;
  }
  return (
    <span className="nullable">
      {children}
      <button type="button" className="link" onClick={() => onChange(value === null ? empty : null)}>
        {value === null ? 'set' : 'none'}
      </button>
    </span>
  );
}

function PlayerPicker(props: WidgetProps) {
  const table = useTable();
  const { value, onChange } = props;
  return (
    <select aria-label={props.name} value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value === '' ? null : Number(event.target.value))}>
      <option value="">{props.type.nullable ? 'none' : 'choose…'}</option>
      {table.players().map((player) => (
        <option key={player.number} value={player.number}>
          {player.label}
        </option>
      ))}
    </select>
  );
}

function ShipPicker(props: WidgetProps) {
  const table = useTable();
  const { value, onChange } = props;
  return (
    <select aria-label={props.name} value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value === '' ? null : Number(event.target.value))}>
      <option value="">{props.type.nullable ? 'none' : 'choose…'}</option>
      {table.ships().map((ship) => (
        <option key={ship.id} value={ship.id}>
          {ship.label}
        </option>
      ))}
    </select>
  );
}

function NumberInput(props: WidgetProps) {
  const { value, onChange } = props;
  return (
    <Nullable {...props} empty={0}>
      {value !== null && (
        <input type="number" aria-label={props.name} value={Number(value ?? 0)} onChange={(event) => onChange(Number(event.target.value))} />
      )}
    </Nullable>
  );
}

function EnumSelect(props: WidgetProps) {
  const { type, value, onChange } = props;
  const values = enumValues(type.name ?? '');
  return (
    <select aria-label={props.name} value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value === '' ? null : event.target.value)}>
      {type.nullable && <option value="">none</option>}
      {values.map((constant) => (
        <option key={constant} value={constant}>
          {constantLabel(type.name ?? '', constant)}
        </option>
      ))}
    </select>
  );
}

function ClassSelect({ value, onChange }: WidgetProps) {
  return (
    <select value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value || null)}>
      <option value="">none</option>
      {schema.lists.events.map((name) => (
        <option key={name} value={name}>
          {typeLabel(name)}
        </option>
      ))}
    </select>
  );
}

// A setup takes a card from a deck; an answer takes one from the asked player's hand. Equal cards exist, so a card is
// picked by its position.
function skillCardsFor(owner: string, table: ReturnType<typeof useTable>): SkillCard[] {
  return schema.lists.sandboxEvents.includes(owner) ? table.deckSkillCards() : table.hand(table.askedPlayer);
}

function SkillCardPicker(props: WidgetProps) {
  const table = useTable();
  const { value, onChange, owner } = props;
  const cards = skillCardsFor(owner, table);
  const selected = value === null ? -1 : cards.findIndex((card) => sameCard(card, value as SkillCard));
  return (
    <select aria-label={props.name} value={selected} onChange={(event) => onChange(Number(event.target.value) < 0 ? null : cards[Number(event.target.value)])}>
      <option value={-1}>none</option>
      {cards.map((card, index) => (
        <option key={index} value={index}>
          {skillCardLabel(card)}
        </option>
      ))}
    </select>
  );
}

function ListWidget(props: WidgetProps) {
  const of = props.type.of as TypeRef;
  if (of?.t === 'ref' && of.name === 'SkillCard') {
    return <CardsMultiPicker {...props} />;
  }
  if (of?.t === 'ref' && (of.name === 'PlayedCard' || of.name === 'PlayedInterruptCard')) {
    return <PlayedCardsPicker {...props} />;
  }
  if (of?.t === 'enum') {
    return <OrderedEnums {...props} />;
  }
  if (of?.t === 'int' && of.role === 'ship') {
    return <ShipsMultiPicker {...props} />;
  }
  return <RawJson {...props} />;
}

function CardsMultiPicker(props: WidgetProps) {
  const table = useTable();
  const cards = skillCardsFor(props.owner, table);
  return (
    <PositionsPicker
      labels={cards.map(skillCardLabel)}
      onChange={(positions) => props.onChange(positions.map((position) => cards[position]))}
    />
  );
}

// The tiebreak lists, picked from the conflicting played cards.
function PlayedCardsPicker(props: WidgetProps) {
  const table = useTable();
  const interrupt = (props.type.of as TypeRef).name === 'PlayedInterruptCard';
  const candidates: (PlayedCard | PlayedInterruptCard)[] = interrupt ? table.windowCards() : table.playedBefore();
  const label = (played: PlayedCard | PlayedInterruptCard) =>
    `${table.playerName(played.playerNumber)}: ${skillCardLabel('skillCard' in played ? played.skillCard : played.card)}`;
  return (
    <PositionsPicker
      labels={candidates.map(label)}
      onChange={(positions) => props.onChange(positions.map((position) => candidates[position]))}
    />
  );
}

function PositionsPicker({ labels, onChange }: { labels: string[]; onChange(positions: number[]): void }) {
  const [picked, setPicked] = useState<number[]>([]);
  const toggle = (position: number) => {
    const next = picked.includes(position) ? picked.filter((candidate) => candidate !== position) : [...picked, position];
    setPicked(next);
    onChange(next);
  };
  if (labels.length === 0) {
    return <span className="muted">nothing to pick</span>;
  }
  return (
    <span className="picker">
      {labels.map((text, position) => (
        <button
          type="button"
          key={position}
          className={picked.includes(position) ? 'tile picked' : 'tile'}
          aria-pressed={picked.includes(position)}
          onClick={() => toggle(position)}
        >
          {text}
        </button>
      ))}
    </span>
  );
}

function OrderedEnums(props: WidgetProps) {
  const name = (props.type.of as TypeRef).name ?? '';
  const values = enumValues(name);
  const picked = (props.value as string[] | null) ?? [];
  return (
    <span className="picker">
      {picked.map((constant, index) => (
        <button type="button" key={index} className="tile picked" onClick={() => props.onChange(picked.filter((_, at) => at !== index))}>
          {index + 1}. {constantLabel(name, constant)} ✕
        </button>
      ))}
      <select value="" onChange={(event) => event.target.value && props.onChange([...picked, event.target.value])}>
        <option value="">add…</option>
        {values.map((constant) => (
          <option key={constant} value={constant}>
            {constantLabel(name, constant)}
          </option>
        ))}
      </select>
    </span>
  );
}

function ShipsMultiPicker(props: WidgetProps) {
  const table = useTable();
  const ships = table.ships();
  return (
    <PositionsPicker labels={ships.map((ship) => ship.label)} onChange={(positions) => props.onChange(positions.map((position) => ships[position].id))} />
  );
}

// A count per key, e.g. how many Skill Cards of each color to draw.
function MapCounts(props: WidgetProps) {
  const key = props.type.key;
  if (key?.t !== 'enum') {
    return <RawJson {...props} />;
  }
  const counts = (props.value as Record<string, number> | null) ?? {};
  const set = (constant: string, count: number) => {
    const next = { ...counts };
    if (count > 0) {
      next[constant] = count;
    } else {
      delete next[constant];
    }
    props.onChange(next);
  };
  return (
    <span className="counts">
      {enumValues(key.name ?? '').map((constant) => (
        <span key={constant} className="count">
          <span>{constantLabel(key.name ?? '', constant)}</span>
          <input type="number" min={0} value={counts[constant] ?? 0} aria-label={constant} onChange={(event) => set(constant, Number(event.target.value))} />
        </span>
      ))}
    </span>
  );
}

// A record inside the event, e.g. a viper's activation: its own form.
function NestedRecord(props: WidgetProps) {
  const name = props.type.name ?? '';
  const record = recordNamed(name);
  if (!record) {
    return <RawJson {...props} />;
  }
  const value = props.value as Record<string, Value> | null;
  return (
    <Nullable {...props} empty={emptyRecord(name)}>
      {value !== null && (
        <span className="nested">
          {record.components
            .filter((component) => !isServerFilled(component))
            .map((component) => (
              <Field
                key={component.name}
                component={component}
                value={value[component.name] ?? null}
                owner={props.owner}
                onChange={(next) => props.onChange({ ...value, [component.name]: next })}
              />
            ))}
        </span>
      )}
    </Nullable>
  );
}

function RawJson({ value, onChange }: WidgetProps) {
  const [text, setText] = useState(JSON.stringify(value));
  const [error, setError] = useState<string | null>(null);
  return (
    <span className="raw">
      <textarea
        value={text}
        onChange={(event) => {
          setText(event.target.value);
          try {
            onChange(JSON.parse(event.target.value));
            setError(null);
          } catch {
            setError('not JSON');
          }
        }}
      />
      {error && <span className="error">{error}</span>}
    </span>
  );
}

function sameCard(card: SkillCard, other: SkillCard): boolean {
  return card.value === other.value && card.type === other.type;
}

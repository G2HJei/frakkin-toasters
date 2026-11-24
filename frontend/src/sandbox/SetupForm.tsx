// The sandbox's setup: a form made from the schema for one setup event, which builds exactly the JSON the codec reads.
// A setup names a player, a number, constants and Skill Cards; a shape it doesn't know falls back to raw JSON.
import { useState } from 'react';
import { characterArt, enumArt, skillCardArt } from '../art/images';
import { ArtImg } from '../art/Art';
import { constantLabel, skillCardLabel } from '../labels/describe';
import { humanizeType } from '../labels/humanize';
import { askedComponents, emptyEvent, missingFields, toWire, type EventValue, type Value } from '../input/eventModel';
import { enumValues, type TypeRef } from '../wire/schema';
import type { SkillCard } from '../wire/types';
import { useTable } from '../table/tableContext';

export interface SetupFormProps {
  // the setup event, one of the schema's sandbox events
  kind: string;
  onSubmit(event: EventValue): void;
}

export function SetupForm({ kind, onSubmit }: SetupFormProps) {
  const [value, setValue] = useState<EventValue>(() => emptyEvent(kind));
  const missing = missingFields(value);
  return (
    <form
      className="setup-form"
      onSubmit={(event) => {
        event.preventDefault();
        onSubmit(toWire(value));
      }}
    >
      {askedComponents(kind).map((component) => {
        const label = humanizeType(component.name);
        return (
          <div key={component.name} className="field" data-field={component.name}>
            <span className="field-name">{label}</span>
            <Widget type={component.type} value={value[component.name] ?? null} name={label} onChange={(next) => setValue({ ...value, [component.name]: next })} />
          </div>
        );
      })}
      <div className="actions">
        <button type="submit" className="primary">
          Inject
        </button>
        {missing.length > 0 && <span className="muted">not set: {missing.join(', ')}</span>}
      </div>
    </form>
  );
}

interface WidgetProps {
  type: TypeRef;
  value: Value;
  name: string;
  onChange(value: Value): void;
}

function Widget(props: WidgetProps) {
  switch (props.type.t) {
    case 'int':
    case 'long':
      return props.type.role === 'player' ? <PlayerPicker {...props} /> : <NumberInput {...props} />;
    case 'enum':
      return <EnumSelect {...props} />;
    case 'ref':
      return props.type.name === 'SkillCard' ? <SkillCardPicker {...props} /> : <RawJson {...props} />;
    default:
      return <RawJson {...props} />;
  }
}

function PlayerPicker({ type, value, name, onChange }: WidgetProps) {
  const table = useTable();
  return (
    <select aria-label={name} value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value === '' ? null : Number(event.target.value))}>
      <option value="">{type.nullable ? 'none' : 'choose…'}</option>
      {table.players().map((player) => (
        <option key={player.number} value={player.number}>
          {player.label}
        </option>
      ))}
    </select>
  );
}

function NumberInput({ type, value, name, onChange }: WidgetProps) {
  return (
    <input
      type="number"
      aria-label={name}
      value={value === null ? '' : Number(value)}
      onChange={(event) => onChange(event.target.value === '' && type.nullable ? null : Number(event.target.value))}
    />
  );
}

function EnumSelect({ type, value, name, onChange }: WidgetProps) {
  const values = enumValues(type.name ?? '');
  const art = value === null ? null : enumArt(type.name ?? '', value as string);
  return (
    <>
      <select aria-label={name} value={value === null ? '' : String(value)} onChange={(event) => onChange(event.target.value === '' ? null : event.target.value)}>
        {type.nullable && <option value="">none</option>}
        {values.map((constant) => (
          <option key={constant} value={constant}>
            {constantLabel(type.name ?? '', constant)}
          </option>
        ))}
      </select>
      {art && <ArtImg art={art} className="field-art" />}
      {type.name === 'Character' && <CharacterGallery value={value as string | null} onChange={onChange} />}
    </>
  );
}

// Every character's standee, to pick one by its face. The engine judges whether he can be placed.
function CharacterGallery({ value, onChange }: { value: string | null; onChange(value: Value): void }) {
  return (
    <span className="gallery" role="group" aria-label="characters">
      {enumValues('Character').map((character) => {
        const art = characterArt(character);
        return art ? (
          <button
            type="button"
            key={character}
            className={character === value ? 'gallery-item picked' : 'gallery-item'}
            aria-pressed={character === value}
            title={art.label}
            onClick={() => onChange(character)}
          >
            <ArtImg art={art} />
          </button>
        ) : null;
      })}
    </span>
  );
}

// A setup takes a Skill Card from the draw piles. Equal cards exist, so a card is picked by its position.
function SkillCardPicker({ value, name, onChange }: WidgetProps) {
  const table = useTable();
  const cards = table.deckSkillCards();
  const selected = value === null ? -1 : cards.findIndex((card) => sameCard(card, value as SkillCard));
  const art = value === null ? null : skillCardArt(value as SkillCard);
  return (
    <>
      <select aria-label={name} value={selected} onChange={(event) => onChange(Number(event.target.value) < 0 ? null : cards[Number(event.target.value)])}>
        <option value={-1}>none</option>
        {cards.map((card, index) => (
          <option key={index} value={index}>
            {skillCardLabel(card)}
          </option>
        ))}
      </select>
      {art && <ArtImg art={art} className="field-art" />}
    </>
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

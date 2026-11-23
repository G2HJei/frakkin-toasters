// A label in its parts: a name that has an image shows it large on hover, like a card in the log or a prompt.
import type { Part } from '../labels/describe';
import type { TypeRef } from '../wire/schema';
import { previewHandlers } from './Art';
import { enumArt, skillCardArt, type ArtRef } from './images';

export function Words({ parts }: { parts: Part[] }) {
  return (
    <>
      {parts.map((part, index) =>
        part.art ? (
          <span key={index} className="named" {...previewHandlers(part.art as ArtRef)}>
            {part.text}
          </span>
        ) : (
          <span key={index}>{part.text}</span>
        ),
      )}
    </>
  );
}

/** The image a value of the schema names: a card, a character, a location, a Skill Card. For `LabelContext.artOf`. */
export function partArt(value: unknown, type: TypeRef): ArtRef | null {
  if (type.t === 'enum' && typeof value === 'string') {
    return enumArt(type.name ?? '', value);
  }
  if (type.t === 'ref' && type.name === 'SkillCard' && value && typeof value === 'object') {
    return skillCardArt(value as { type: string | null; value: number });
  }
  return null;
}

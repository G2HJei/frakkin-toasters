// A label in its parts: a name that has an image shows it large on hover, like a card in the log or a prompt; a part
// may have a small icon in front of its words (a resource, a ship, a title, the die, a character's portrait) and a
// tone, good or bad news.
import type { CSSProperties } from 'react';
import type { Part, PartIcon } from '../labels/describe';
import type { TypeRef } from '../wire/schema';
import { previewHandlers } from './Art';
import { characterArt, dieArt, enumArt, isTitleName, resourceIconArt, shipArt, skillCardArt, titleIconArt, tokenArt, type ArtRef, type TokenName } from './images';

export function Words({ parts }: { parts: Part[] }) {
  return (
    <>
      {parts.map((part, index) => {
        const className = [part.art ? 'named' : '', part.tone ? `tone-${part.tone}` : '', part.skillColor ? `skill-type-${part.skillColor.toLowerCase()}` : ''].filter((name) => name).join(' ');
        return (
          <span key={index} className={className || undefined} {...(part.art ? previewHandlers(part.art as ArtRef) : {})}>
            {part.icon && <Icon icon={part.icon} />}
            {part.text}
          </span>
        );
      })}
    </>
  );
}

// The words name what it shows, so a screen reader skips it. A tinted icon is a mask in the color of its words.
function Icon({ icon }: { icon: PartIcon }) {
  if (icon.tint) {
    const style = { '--icon': `url("${icon.url}")` } as CSSProperties;
    return <span className={`part-icon tint icon-${icon.shape}`} style={style} data-icon={icon.key} aria-hidden="true" />;
  }
  return <img className={`part-icon icon-${icon.shape}`} src={icon.url} alt="" draggable={false} data-icon={icon.key} aria-hidden="true" />;
}

export function partArt(value: unknown, type: TypeRef): ArtRef | null {
  if (type.t === 'enum' && typeof value === 'string') {
    return enumArt(type.name ?? '', value);
  }
  if (type.t === 'ref' && type.name === 'SkillCard' && value && typeof value === 'object') {
    return skillCardArt(value as { type: string | null; value: number });
  }
  return null;
}

export function partIcon(enumName: string, constant: unknown): PartIcon | null {
  const icon = (art: ArtRef | null, shape: string, tint = false): PartIcon | null => (art ? { key: art.key, url: art.url, shape, tint } : null);
  switch (enumName) {
    case 'Resource':
      return icon(resourceIconArt(constant as string), 'resource', true);
    case 'ShipType':
      return icon(constant === 'CENTURION' ? tokenArt('CENTURION') : shipArt(constant as string), 'ship');
    case 'Title':
      return typeof constant === 'string' && isTitleName(constant) ? icon(titleIconArt(constant), 'title') : null;
    case 'Token':
      return icon(tokenArt(constant as TokenName), 'token');
    case 'Die':
      return icon(dieArt(constant as number), 'die');
    case 'Character':
      return icon(characterArt(constant as string), 'portrait');
    case 'Choice':
      // "Raptor Specialist" builds an assault raptor or returns a raptor
      return icon(shipArt(constant === 'BUILD_ASSAULT_RAPTOR' ? 'ASSAULT_RAPTOR' : 'RAPTOR'), 'ship');
    default:
      return null;
  }
}

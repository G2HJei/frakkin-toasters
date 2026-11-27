// The images on the table. A thumbnail is too small to read, so hovering one shows it large in the preview, which
// follows the pointer. A target of the pending input says there at once what a click on it does (`TargetHint`), over
// the preview of what is under the pointer.
import { createContext, useContext, useEffect, useId, useRef, useState, type ReactNode } from 'react';
import { create } from 'zustand';
import { deckBackArt, skillBackArt, type ArtRef } from './images';
import { usePlaceNear, type Point } from './nearPointer';
import { constantLabel } from '../labels/describe';

interface Hint {
  id: string;
  text: string;
  element: Element;
}

interface PreviewState {
  art: ArtRef | null;
  extra: ReactNode;
  // the hints of the targets under the pointer: a target can lie in another, e.g. a standee in a location
  hints: Hint[];
  // one for the hint and the preview, which show together
  pointer: Point | null;
  show(art: ArtRef, pointer: Point, extra?: ReactNode): void;
  move(pointer: Point): void;
  hide(art: ArtRef): void;
  hideKey(key: string): void;
  hint(hint: Hint, pointer: Point): void;
  rehint(id: string, text: string): void;
  unhint(id: string): void;
}

export const usePreview = create<PreviewState>((set, get) => ({
  art: null,
  extra: null,
  hints: [],
  pointer: null,
  show: (art, pointer, extra = null) => set({ art, pointer, extra }),
  move: (pointer) => {
    const { art, hints, pointer: before } = get();
    // the hint's target and the image in it both follow the pointer: one place a move
    if ((art || hints.length > 0) && (before?.x !== pointer.x || before?.y !== pointer.y)) {
      set({ pointer });
    }
  },
  // only the image that showed the preview hides it
  hide: (art) => get().hideKey(art.key),
  hideKey: (key) => {
    if (get().art?.key === key) {
      set({ art: null, extra: null, pointer: get().hints.length > 0 ? get().pointer : null });
    }
  },
  hint: (hint, pointer) => set({ hints: [...get().hints.filter((other) => other.id !== hint.id), hint], pointer }),
  rehint: (id, text) => {
    if (get().hints.some((hint) => hint.id === id && hint.text !== text)) {
      set({ hints: get().hints.map((hint) => (hint.id === id ? { ...hint, text } : hint)) });
    }
  },
  unhint: (id) => {
    const hints = get().hints.filter((hint) => hint.id !== id);
    if (hints.length !== get().hints.length) {
      set({ hints, pointer: hints.length > 0 || get().art ? get().pointer : null });
    }
  },
}));

// of targets that lie in one another, the click goes to the innermost, and so does the hint
function innermost(hints: Hint[]): Hint | null {
  return hints.find((hint) => !hints.some((other) => other !== hint && hint.element.contains(other.element))) ?? null;
}

export function previewHandlers(art: ArtRef, extra?: ReactNode) {
  if (art.preview.length === 0) {
    return {};
  }
  return {
    onMouseEnter: (event: React.MouseEvent) => usePreview.getState().show(art, { x: event.clientX, y: event.clientY }, extra),
    onMouseMove: (event: React.MouseEvent) => usePreview.getState().move({ x: event.clientX, y: event.clientY }),
    onMouseLeave: () => usePreview.getState().hide(art),
  };
}

/**
 * Inside a target of the pending input, whose hint says what a click does: an image there names itself to a screen
 * reader only, so no tooltip of its name comes up over the hint.
 */
export const InTarget = createContext(false);

/**
 * What a click on a target of the pending input does, shown at once on hover, over the preview of what is under the
 * pointer. Like an SVG `<title>`, it is the hint of the element it lies in: in the board's SVG a `<desc>`, which
 * describes its target to a screen reader too, and in HTML (`html`) a hidden span. Of targets in one another, the
 * innermost shows. A target that stops being one takes its hint with it: no mouse leaves it.
 */
export function TargetHint({ text, html = false }: { text: string; html?: boolean }) {
  const id = useId();
  const [element, setElement] = useState<Element | null>(null);
  // what a pointer entering the target finds, also after the moves changed under it
  const current = useRef(text);
  useEffect(() => {
    current.current = text;
    usePreview.getState().rehint(id, text);
  }, [id, text]);
  useEffect(() => {
    const target = element?.parentElement;
    if (!target) {
      return;
    }
    const enter = (event: MouseEvent) => usePreview.getState().hint({ id, text: current.current, element: target }, { x: event.clientX, y: event.clientY });
    const move = (event: MouseEvent) => usePreview.getState().move({ x: event.clientX, y: event.clientY });
    const leave = () => usePreview.getState().unhint(id);
    target.addEventListener('mouseenter', enter);
    target.addEventListener('mousemove', move);
    target.addEventListener('mouseleave', leave);
    return () => {
      target.removeEventListener('mouseenter', enter);
      target.removeEventListener('mousemove', move);
      target.removeEventListener('mouseleave', leave);
      leave();
    };
  }, [element, id]);
  return html ? (
    <span ref={setElement} hidden>
      {text}
    </span>
  ) : (
    <desc ref={setElement}>{text}</desc>
  );
}

export function ArtPreview() {
  const art = usePreview((state) => state.art);
  const pointer = usePreview((state) => state.pointer);
  const extra = usePreview((state) => state.extra);
  const hint = innermost(usePreview((state) => state.hints));
  const ref = useRef<HTMLDivElement>(null);
  usePlaceNear(ref, pointer);
  if (!art && !hint) {
    return null;
  }
  return (
    <div ref={ref} className={`hover-pop${hint ? ' with-hint' : ''}`} aria-hidden="true">
      {hint && <div className="target-hint">{hint.text}</div>}
      {art && (
        <div className={`art-preview preview-${art.shape}${extra ? ' with-extra' : ''}`}>
          <div className="art-preview-art">
            <div className="art-preview-images">
              {art.preview.map((url) => (
                <img key={url} src={url} alt="" />
              ))}
            </div>
          </div>
          {extra && <div className="art-preview-extra">{extra}</div>}
        </div>
      )}
    </div>
  );
}

/**
 * An image with the preview on hover, unless what it is part of shows it (`preview={false}`). An image that goes away
 * under the pointer, e.g. on the button just clicked, takes its preview with it: no mouse leaves it.
 */
export function ArtImg({ art, className = '', preview = true }: { art: ArtRef; className?: string; preview?: boolean }) {
  // by its key: a render makes new refs of the same image
  const { key } = art;
  // only the preview this image showed: a preview can show the same image again (a standee's, with his mat), which
  // StrictMode mounts twice
  const showing = useRef(false);
  useEffect(
    () => () => {
      if (showing.current) {
        usePreview.getState().hideKey(key);
      }
    },
    [key],
  );
  const handlers = preview ? previewHandlers(art) : {};
  return (
    <img
      src={art.url}
      alt={art.label}
      loading="lazy"
      draggable={false}
      className={`art art-${art.shape} ${className}`.trim()}
      data-art={art.key}
      {...handlers}
      {...(handlers.onMouseEnter
        ? {
            onMouseEnter: (event: React.MouseEvent) => {
              showing.current = true;
              handlers.onMouseEnter(event);
            },
            onMouseLeave: () => {
              showing.current = false;
              handlers.onMouseLeave();
            },
          }
        : {})}
    />
  );
}

// `full` is the card as large as its image, smaller only where its place is narrower
export type CardSize = 'xs' | 'sm' | 'md' | 'lg' | 'full';

/**
 * A card face up: its image, or its name where it has none. A face-up card is a `tile`, a face-down one a `tile back`.
 * A card at full size has no preview, which would show the same image again, unless the preview shows more parts.
 */
export function CardTile({ art, label, size = 'sm', className = '', caption }: {
  art: ArtRef | null;
  label: string;
  size?: CardSize;
  className?: string;
  caption?: string;
}) {
  if (!art) {
    return (
      <span className={`tile ${className}`.trim()}>
        {caption ? `${caption}: ` : ''}
        {label}
      </span>
    );
  }
  return (
    <span className={`tile card-tile size-${size} ${className}`.trim()}>
      <ArtImg art={art} preview={size !== 'full' || art.preview.length > 1} />
      {caption && <span className="card-caption">{caption}</span>}
    </span>
  );
}

/**
 * Cards face down: the back of their deck, or a back in their Skill Card color, with how many there are. The words
 * stay for a screen reader and where there is no image.
 */
export function CardBack({ deck, color, count, size = 'sm', label }: {
  deck?: string | null;
  color?: string | null;
  count: number;
  size?: CardSize;
  label?: string;
}) {
  const back = deck ? deckBackArt(deck) : null;
  const words = label ?? (color ? `${count} ${constantLabel('SkillCardColor', color).toLowerCase()}` : `${count} face down`);
  return (
    <span className={`tile back card-back size-${size} ${color ? `back-${color.toLowerCase()}` : ''}`.trim()} title={words}>
      {back && <ArtImg art={back} />}
      <span className="back-count" aria-label={words}>
        {back || color ? `×${count}` : words}
      </span>
    </span>
  );
}

export function SkillCardBack({ count, size = 'sm' }: { count: number; size?: CardSize }) {
  const back = skillBackArt();
  const words = `${count} Skill Card${count === 1 ? '' : 's'}`;
  return (
    <span className={`tile back card-back size-${size}`} title={words}>
      {back && <ArtImg art={back} />}
      <span className="back-count" aria-label={words}>
        {back ? `×${count}` : words}
      </span>
    </span>
  );
}

export function ArtStrip({ refs, className = '' }: { refs: ArtRef[]; className?: string }) {
  if (refs.length === 0) {
    return null;
  }
  return (
    <span className={`art-strip ${className}`.trim()}>
      {refs.map((art) => (
        <ArtImg key={art.key} art={art} />
      ))}
    </span>
  );
}

/**
 * An image inside the board's SVG, with the same preview unless what it is part of shows more on hover, and what the
 * preview shows beside it. Its name is a tooltip, but inside a target, whose hint shows instead.
 */
export function SvgArt({ art, x, y, width, height, className, title, preview = true, previewExtra, transform }: {
  art: ArtRef;
  x: number;
  y: number;
  width: number;
  height: number;
  className?: string;
  title?: string;
  preview?: boolean;
  previewExtra?: ReactNode;
  transform?: string;
}) {
  const inTarget = useContext(InTarget);
  const name = title ?? art.label;
  return (
    <image
      href={art.url}
      x={x}
      y={y}
      width={width}
      height={height}
      preserveAspectRatio="xMidYMid meet"
      className={className}
      transform={transform}
      data-art={art.key}
      aria-label={inTarget ? name : undefined}
      {...(preview ? previewHandlers(art, previewExtra) : {})}
    >
      {!inTarget && <title>{name}</title>}
    </image>
  );
}

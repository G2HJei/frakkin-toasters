// The images on the table. A thumbnail is too small to read, so hovering one shows it large in the preview, which
// follows the pointer.
import { useRef, type ReactNode } from 'react';
import { create } from 'zustand';
import { deckBackArt, type ArtRef } from './images';
import { usePlaceNear, type Point } from './nearPointer';
import { constantLabel } from '../labels/describe';

interface PreviewState {
  art: ArtRef | null;
  pointer: Point | null;
  // shown beside the images, e.g. a player's mat by his character sheet
  extra: ReactNode;
  show(art: ArtRef, pointer: Point, extra?: ReactNode): void;
  move(art: ArtRef, pointer: Point): void;
  hide(art: ArtRef): void;
}

export const usePreview = create<PreviewState>((set, get) => ({
  art: null,
  pointer: null,
  extra: null,
  show: (art, pointer, extra = null) => set({ art, pointer, extra }),
  move: (art, pointer) => {
    if (get().art?.key === art.key) {
      set({ pointer });
    }
  },
  // only the image that showed the preview hides it
  hide: (art) => {
    if (get().art?.key === art.key) {
      set({ art: null, pointer: null, extra: null });
    }
  },
}));

export function previewHandlers(art: ArtRef, extra?: ReactNode) {
  if (art.preview.length === 0) {
    return {};
  }
  return {
    onMouseEnter: (event: React.MouseEvent) => usePreview.getState().show(art, { x: event.clientX, y: event.clientY }, extra),
    onMouseMove: (event: React.MouseEvent) => usePreview.getState().move(art, { x: event.clientX, y: event.clientY }),
    onMouseLeave: () => usePreview.getState().hide(art),
  };
}

export function ArtPreview() {
  const art = usePreview((state) => state.art);
  const pointer = usePreview((state) => state.pointer);
  const extra = usePreview((state) => state.extra);
  const ref = useRef<HTMLDivElement>(null);
  usePlaceNear(ref, pointer);
  if (!art) {
    return null;
  }
  return (
    <div ref={ref} className={`art-preview preview-${art.shape}${extra ? ' with-extra' : ''}`} aria-hidden="true">
      <div className="art-preview-art">
        <div className="art-preview-images">
          {art.preview.map((url) => (
            <img key={url} src={url} alt="" />
          ))}
        </div>
      </div>
      {extra && <div className="art-preview-extra">{extra}</div>}
    </div>
  );
}

/** An image with the preview on hover, unless what it is part of shows it (`preview={false}`). */
export function ArtImg({ art, className = '', preview = true }: { art: ArtRef; className?: string; preview?: boolean }) {
  return (
    <img
      src={art.url}
      alt={art.label}
      loading="lazy"
      draggable={false}
      className={`art art-${art.shape} ${className}`.trim()}
      data-art={art.key}
      {...(preview ? previewHandlers(art) : {})}
    />
  );
}

export type CardSize = 'xs' | 'sm' | 'md' | 'lg';

/** A card face up: its image, or its name where it has none. A face-up card is a `tile`, a face-down one a `tile back`. */
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
      <ArtImg art={art} />
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

/** A row of small images: what a log entry or an option names. */
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
 * preview shows beside it.
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
      {...(preview ? previewHandlers(art, previewExtra) : {})}
    >
      <title>{title ?? art.label}</title>
    </image>
  );
}

import { ArtImg } from '../art/Art';
import type { ArtRef } from '../art/images';

/**
 * A button's images in a slot as wide and as high as on every button of the answers, so their words line up and the
 * buttons stand as high with images or without. A pass shows its icon instead.
 */
export function ArtSlot({ refs, pass = false }: { refs: ArtRef[]; pass?: boolean }) {
  return (
    <span className={`art-slot${pass ? ' pass-icon' : ''}`} data-count={pass ? undefined : refs.length}>
      {pass ? <PassIcon /> : refs.map((art) => <ArtImg key={art.key} art={art} />)}
    </span>
  );
}

/** Skip ahead: two arrowheads against a bar, in the color of its text. A pass, and the log's automatic answer. */
export function PassIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" focusable="false">
      <path d="M3 5.5 11 12l-8 6.5zM11 5.5l8 6.5-8 6.5z" fill="currentColor" />
      <rect x="19" y="5.5" width="2.5" height="13" fill="currentColor" />
    </svg>
  );
}

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

export function NextIcon() {
  return (
    <svg className="next-icon" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true" focusable="false">
      <path d="M9 5.5 15.5 12 9 18.5" fill="none" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function PassIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true" focusable="false">
      <path d="M3 5.5 11 12l-8 6.5zM11 5.5l8 6.5-8 6.5z" fill="currentColor" />
      <rect x="19" y="5.5" width="2.5" height="13" fill="currentColor" />
    </svg>
  );
}

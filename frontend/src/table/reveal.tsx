// What a pile holds, shown on hover: a deck's discard pile for everyone, and in a sandbox what the players at the table
// can't see (a hand, a draw pile, the face-down cards of a skill check). The table looks the same in a sandbox and in a
// live game; only hovering shows more. The popover lives in one fixed layer, so no panel clips it.
import { create } from 'zustand';
import { ArtImg } from '../art/Art';
import type { ArtRef } from '../art/images';
import { useTable } from './tableContext';

export interface RevealCard {
  art: ArtRef | null;
  label: string;
}

export interface RevealSection {
  title: string;
  cards: RevealCard[];
  // what the players at the table can't see, which only a sandbox shows
  hidden?: boolean;
}

interface Open {
  id: string;
  title: string;
  sections: RevealSection[];
  rect: { left: number; top: number; right: number; bottom: number };
}

interface RevealState {
  open: Open | null;
  closing: ReturnType<typeof setTimeout> | null;
  show(open: Open): void;
  hideSoon(id: string): void;
  keep(): void;
  hide(): void;
}

export const useReveal = create<RevealState>((set, get) => ({
  open: null,
  closing: null,
  show: (open) => {
    const closing = get().closing;
    if (closing) {
      clearTimeout(closing);
    }
    set({ open, closing: null });
  },
  // a moment to move the pointer onto the popover, to hover its cards
  hideSoon: (id) => {
    if (get().open?.id !== id) {
      return;
    }
    const closing = get().closing;
    if (closing) {
      clearTimeout(closing);
    }
    set({ closing: setTimeout(() => set({ open: null, closing: null }), 150) });
  },
  keep: () => {
    const closing = get().closing;
    if (closing) {
      clearTimeout(closing);
    }
    set({ closing: null });
  },
  hide: () => set({ open: null, closing: null }),
}));

/** The sections this viewer may see on hover: the public ones, and in a sandbox the hidden ones too. */
export function visibleSections(sections: RevealSection[], sandbox: boolean): RevealSection[] {
  return sections.filter((section) => section.cards.length > 0 && (!section.hidden || sandbox));
}

/**
 * The hover handlers that open the popover for an element, HTML or SVG, or none when there is nothing to show. The id
 * names the element, so only the element that opened the popover closes it.
 */
export function useRevealHandlers(id: string, title: string, sections: RevealSection[]) {
  const table = useTable();
  const shown = visibleSections(sections, table.sandbox);
  if (shown.length === 0) {
    return {};
  }
  return {
    onMouseEnter: (event: React.MouseEvent<Element>) => {
      const box = event.currentTarget.getBoundingClientRect();
      useReveal.getState().show({ id, title, sections: shown, rect: { left: box.left, top: box.top, right: box.right, bottom: box.bottom } });
    },
    onMouseLeave: () => useReveal.getState().hideSoon(id),
    'data-reveal': id,
  };
}

const WIDTH = 460;

/** The one popover of the table, beside the element that opened it. */
export function RevealLayer() {
  const open = useReveal((state) => state.open);
  if (!open) {
    return null;
  }
  // below the element when it fits, otherwise above it; never off the window's sides
  const below = open.rect.bottom + 8;
  const fitsBelow = below + 320 < window.innerHeight;
  const left = Math.max(8, Math.min(open.rect.left, window.innerWidth - WIDTH - 8));
  const style: React.CSSProperties = fitsBelow
    ? { left, top: below, width: WIDTH }
    : { left, bottom: window.innerHeight - open.rect.top + 8, width: WIDTH };
  return (
    <div
      className="reveal-pop"
      role="tooltip"
      style={style}
      onMouseEnter={() => useReveal.getState().keep()}
      onMouseLeave={() => useReveal.getState().hide()}
    >
      <div className="reveal-title">{open.title}</div>
      {open.sections.map((section) => (
        <div key={section.title} className={section.hidden ? 'reveal-section hidden-info' : 'reveal-section'}>
          <div className="reveal-section-title">
            {section.title} · {section.cards.length}
            {section.hidden && <span className="sandbox-only">hidden at the table</span>}
          </div>
          <ol className="pile">
            {section.cards.map((card, index) => (
              <li key={index}>
                {card.art && <ArtImg art={card.art} />}
                <span>{card.label}</span>
              </li>
            ))}
          </ol>
        </div>
      ))}
    </div>
  );
}

/** A multiset difference: the cards of a pile that aren't among the revealed ones. */
export function faceDown<T>(cards: T[], revealed: T[]): T[] {
  const left = [...revealed];
  return cards.filter((card) => {
    const at = left.findIndex((candidate) => JSON.stringify(candidate) === JSON.stringify(card));
    if (at >= 0) {
      left.splice(at, 1);
      return false;
    }
    return true;
  });
}

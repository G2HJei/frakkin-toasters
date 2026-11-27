// What a pile holds, shown on hover: a deck's discard pile for everyone, and in a sandbox what the players at the table
// can't see (a hand, a draw pile, the face-down cards of a skill check). The table looks the same in a sandbox and in a
// live game; only hovering shows more. The popover lives in one fixed layer, so no panel clips it, and shows the cards
// as large as their images.
import { useRef, type ReactNode } from 'react';
import { create } from 'zustand';
import { ArtImg } from '../art/Art';
import type { ArtRef } from '../art/images';
import { roomBeside, usePlaceNear, type Point } from '../art/nearPointer';
import { useTable } from './tableContext';

export interface RevealCard {
  art: ArtRef | null;
  label: string;
  // drawn in place of an image, e.g. a step's icon
  icon?: ReactNode;
}

export interface RevealSection {
  // empty for a list with nothing to count, which then has no heading
  title: string;
  cards: RevealCard[];
  // what the players at the table can't see, which only a sandbox shows
  hidden?: boolean;
}

interface Open {
  id: string;
  title: string;
  sections: RevealSection[];
  // where the pointer entered the element; the popover stays there, so the pointer can move onto it
  pointer: Point;
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
  // a moment to move the pointer onto the popover, to scroll it
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
    onMouseEnter: (event: React.MouseEvent<Element>) =>
      useReveal.getState().show({ id, title, sections: shown, pointer: { x: event.clientX, y: event.clientY } }),
    onMouseLeave: () => useReveal.getState().hideSoon(id),
    'data-reveal': id,
  };
}

// a Crisis Card with the popover's padding, border and scrollbar
const ONE_CARD = 330;

function pileClass(section: RevealSection): string {
  if (section.cards.some((card) => card.icon)) {
    return 'pile icons';
  }
  return section.cards.some((card) => card.art) ? 'pile cards' : 'pile list';
}

/**
 * The one popover of the table, next to the pointer that opened it. The cards are as large as their images, to read
 * them without a preview of their own, and the popover scrolls when they don't fit in the window.
 */
export function RevealLayer() {
  const open = useReveal((state) => state.open);
  const ref = useRef<HTMLDivElement>(null);
  usePlaceNear(ref, open?.pointer ?? null);
  if (!open) {
    return null;
  }
  // beside the pointer, with fewer cards in a row, so it doesn't cover the pile; a window too narrow for even one card
  // there leaves it the window's width
  const room = roomBeside(open.pointer);
  return (
    <div
      ref={ref}
      className="reveal-pop"
      role="tooltip"
      style={{ maxWidth: room >= ONE_CARD ? room : undefined }}
      onMouseEnter={() => useReveal.getState().keep()}
      onMouseLeave={() => useReveal.getState().hide()}
    >
      <div className="reveal-title">{open.title}</div>
      <div className="reveal-body">
        {open.sections.map((section) => (
          <div key={section.title} className={section.hidden ? 'reveal-section hidden-info' : 'reveal-section'}>
            {section.title && (
              <div className="reveal-section-title">
                {section.title} · {section.cards.length}
                {section.hidden && <span className="sandbox-only">hidden at the table</span>}
              </div>
            )}
            <ol className={pileClass(section)}>
              {section.cards.map((card, index) => (
                <li key={index}>
                  {card.icon ?? (card.art && <ArtImg art={card.art} preview={false} />)}
                  {!namedByImage(card) && <span>{card.label}</span>}
                </li>
              ))}
            </ol>
          </div>
        ))}
      </div>
    </div>
  );
}

// an image needs no name under it; a label that says more stays, e.g. who played the card
function namedByImage(card: RevealCard): boolean {
  return !card.icon && card.art !== null && card.label === card.art.label;
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

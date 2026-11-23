// What the asked viewer picked so far, before anything is sent: the targets clicked on the board, the mats or the
// panel, and a shape being filled. It belongs to one game message: a new one clears it. "Back" and "Clear" take picks
// away; nothing reaches the engine until a move is sent.
import { create } from 'zustand';
import { useGameStore } from '../live/gameStore';
import { targetKey, type Move, type Target } from './answers';

export interface Anchor {
  x: number;
  y: number;
}

interface SelectionState {
  version: number;
  picked: Target[];
  // where the last click was, for the popover of the moves it left
  anchor: Anchor | null;
  // a shape whose part is being picked
  picking: Move | null;
  pick(target: Target, anchor?: Anchor | null): void;
  back(): void;
  clear(): void;
  startPick(move: Move | null): void;
  sync(version: number): void;
}

export const useSelection = create<SelectionState>((set, get) => ({
  version: 0,
  picked: [],
  anchor: null,
  picking: null,
  pick: (target, anchor = null) => {
    const picked = get().picked;
    if (picked.some((candidate) => targetKey(candidate) === targetKey(target))) {
      return;
    }
    set({ picked: [...picked, target], anchor, picking: null });
  },
  back: () => {
    if (get().picking) {
      set({ picking: null });
      return;
    }
    const picked = get().picked.slice(0, -1);
    set({ picked, anchor: picked.length > 0 ? get().anchor : null });
  },
  clear: () => set({ picked: [], anchor: null, picking: null }),
  startPick: (move) => set({ picking: move }),
  sync: (version) => {
    if (get().version !== version) {
      set({ version, picked: [], anchor: null, picking: null });
    }
  },
}));

// a new game message clears the picks before anything renders it
useGameStore.subscribe((state, previous) => {
  if (state.version !== previous.version) {
    useSelection.getState().sync(state.version);
  }
});

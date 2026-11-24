// The names of the board's sections, which the board labels where its picture once printed them.

export const BOARD_LABELS = {
  objective: 'Objective',
  pursuit: 'Pursuit track',
  cylonReserves: 'Cylon reserves',
  colonialOne: 'Colonial One',
  pegasus: 'Pegasus',
  damagedVipers: 'Damaged vipers',
  shipReserves: 'Ship reserves',
  jump: 'Jump preparation',
} as const;

export type BoardLabel = keyof typeof BOARD_LABELS;

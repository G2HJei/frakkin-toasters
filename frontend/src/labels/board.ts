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

// the warning plate on Galactica's hull while it is critically damaged, and what its popover explains
export const CRITICAL_DAMAGE_LABEL = 'Critical damage';

export const CRITICAL_DAMAGE_EXPLAINED = 'Galactica is destroyed when 6 of its locations are damaged, and the Cylons win.';

// the shield around a resource whose next loss this turn is smaller, the card that raised it and what its popover
// explains
export const LOSS_REDUCTION_CARD = 'PREVENTIVE_POLICY';

export function lossReductionExplained(resource: string, amount: number): string {
  return `The next ${resource} loss this turn is reduced by ${amount}.`;
}

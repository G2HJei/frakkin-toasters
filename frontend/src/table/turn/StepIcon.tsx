import { StrokedIcon, strokedSvg } from '../../art/Glyph';
import type { GameStep } from '../../wire/types';

export type TurnStep = Exclude<GameStep, 'SETUP'>;

// drawn on a 24px grid, stroked in the step's color (`art/Glyph`): markup, which the turn panel draws inline and the
// answers as an image (`stepIconSvg`)
const ICONS: Record<TurnStep, string> = {
  // a card on a deck, with a plus
  RECEIVE_SKILLS:
    '<path d="M9 17H4.5A1.5 1.5 0 0 1 3 15.5v-11A1.5 1.5 0 0 1 4.5 3h7A1.5 1.5 0 0 1 13 4.5V7"/>' +
    '<rect x="9" y="7" width="12" height="14" rx="1.5"/>' +
    '<path d="M15 11v6M12 14h6"/>',
  // a dotted route from a dot to an arrowhead
  MOVEMENT:
    '<circle cx="5" cy="19" r="2.2" fill="currentColor" stroke="none"/>' +
    '<path d="M8.5 18.5c3 0 3.5-2.5 3.5-6.5s1-6 4.5-6" stroke-dasharray="0 3.8" stroke-width="2.6"/>' +
    '<path d="M16.5 3l3.5 3.5-3.5 3.5"/>',
  // a lightning bolt
  ACTION: '<path d="M13 2 4 14h7l-1 8 10-12h-7l1-8z"/>',
  // a warning triangle
  CRISIS:
    '<path d="M10.3 4.2 2.6 17.5a2 2 0 0 0 1.7 3h15.4a2 2 0 0 0 1.7-3L13.7 4.2a2 2 0 0 0-3.4 0z"/>' +
    '<path d="M12 9.5v4M12 17h.01"/>',
  // a targeting reticle
  ACTIVATE_CYLON_SHIPS:
    '<circle cx="12" cy="12" r="6.5"/>' +
    '<path d="M12 2v4.5M12 17.5V22M2 12h4.5M17.5 12H22"/>' +
    '<circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none"/>',
  // the swirl of a jump
  PREPARE_FOR_JUMP: '<path d="M9.5 13.25a2.5 2.5 0 0 1 5 0a5 5 0 0 1-10 0a7.5 7.5 0 0 1 15 0"/>',
  // a bin
  DISCARD: '<path d="M4 7h16M9.5 7V4.5h5V7M6 7l1 13h10l1-13M10 11v5.5M14 11v5.5"/>',
};

export function stepIconSvg(step: TurnStep, color: string): string {
  return strokedSvg(ICONS[step], color);
}

export function StepIcon({ step }: { step: TurnStep }) {
  return <StrokedIcon markup={ICONS[step]} className="step-icon" />;
}

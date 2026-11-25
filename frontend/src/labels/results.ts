// The words of a finished game's results: its ending, its turning points and its statistics. Names only, never a card's
// printed text.

import type { GameReport, Moment, PlayerStatistics, Team } from '../wire/types';
import { constantLabel, type LabelContext } from './describe';

export function endingHeading(winner: Team | null | undefined, abandoned: boolean): string {
  if (abandoned) {
    return 'The game was abandoned';
  }
  return winner === 'HUMANS' ? 'The humans win' : winner === 'CYLONS' ? 'The Cylons win' : 'The game is over';
}

export function gameSummary(report: GameReport): string {
  const jumps = report.statistics?.jumps ?? 0;
  return [
    `${report.turns} ${report.turns === 1 ? 'turn' : 'turns'}`,
    `distance ${report.distance}`,
    `${jumps} ${jumps === 1 ? 'jump' : 'jumps'}`,
  ].join(' · ');
}

/** A turning point in a sentence. */
export function momentLabel(moment: Moment, context: LabelContext): string {
  const who = moment.character ? constantLabel('Character', moment.character) : moment.playerNumber !== null ? context.playerName(moment.playerNumber) : 'Someone';
  switch (moment.kind) {
    case 'JUMPED':
      return `The fleet jumps to ${constantLabel('DestinationCard', moment.destination)}: distance ${moment.distance ?? 0}`;
    case 'CYLON_REVEALED':
      return `${who} reveals a Cylon Loyalty Card`;
    case 'CHARACTER_REMOVED':
      return `${who} leaves the game`;
    case 'PRESIDENT':
      return `${who} becomes the President`;
    case 'ADMIRAL':
      return `${who} becomes the Admiral`;
    case 'CAG':
      return `${who} becomes the CAG`;
    case 'PEGASUS_DESTROYED':
      return 'Pegasus is destroyed';
    case 'GAME_ENDED':
      return endingHeading(moment.winner, false);
    default:
      return '';
  }
}

export const PLAYER_STATISTICS: { key: keyof PlayerStatistics; label: string; title: string }[] = [
  { key: 'cardsIntoChecks', label: 'Into checks', title: 'Skill Cards played into skill checks' },
  { key: 'strengthIntoChecks', label: 'Strength', title: 'The strength of those cards, as printed' },
  { key: 'skillCardsPlayed', label: 'Cards played', title: 'Skill Cards played for their text' },
  { key: 'skillCardsGained', label: 'Cards gained', title: 'Skill Cards that came into the hand' },
  { key: 'crisisCards', label: 'Crises', title: 'Crisis Cards drawn on the turns of the player' },
  { key: 'abilitiesUsed', label: 'Abilities', title: 'Uses of the character’s abilities' },
  { key: 'sentToBrig', label: 'Brig', title: 'Times sent to the “Brig”' },
];

export const RESOURCE_NAMES = { fuel: 'Fuel', food: 'Food', morale: 'Morale', population: 'Population' } as const;

export function teamLabel(cylon: boolean, revealedOnTurn: number | null | undefined): string {
  if (!cylon) {
    return 'Human';
  }
  return revealedOnTurn ? `Cylon, revealed on turn ${revealedOnTurn}` : 'Cylon';
}

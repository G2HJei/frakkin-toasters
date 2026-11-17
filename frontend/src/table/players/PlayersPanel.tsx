import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { HiddenCards, PlayerProjection, SkillHand } from '../../wire/types';
import { useTable } from '../tableContext';

/** Every player: character, location, titles, cards (or their counts and colors), tokens and what he alone knows. */
export function PlayersPanel() {
  const table = useTable();
  const players = table.game?.players ?? [];
  return (
    <section className="panel players" aria-label="players">
      <h2>Players</h2>
      <div className="player-list">
        {players.map((player) => (
          <PlayerCard key={player.number} player={player} />
        ))}
      </div>
    </section>
  );
}

function PlayerCard({ player }: { player: PlayerProjection }) {
  const table = useTable();
  const titles = table.game?.titles;
  const turn = table.game?.turn;
  const badges = [
    titles?.president && titles.president === player.character ? 'President' : null,
    titles?.admiral && titles.admiral === player.character ? 'Admiral' : null,
    titles?.cag && titles.cag === player.character ? 'CAG' : null,
    turn?.currentPlayer === player.number ? 'current' : null,
    player.revealedCylon ? 'revealed Cylon' : null,
    player.infiltrating ? 'infiltrating' : null,
    player.mutineer ? 'Mutineer' : null,
    player.miracleToken ? 'miracle token' : null,
  ].filter((badge): badge is string => badge !== null);
  return (
    <article className="player" data-player={player.number}>
      <header>
        <strong>P{player.number}</strong> {player.character ? constantLabel('Character', player.character) : <em>no character yet</em>}
        <span className="muted"> · {player.location ? constantLabel('Location', player.location) : 'off the board'}</span>
      </header>
      {badges.length > 0 && (
        <p className="badges">
          {badges.map((badge) => (
            <span key={badge} className="badge">
              {badge}
            </span>
          ))}
        </p>
      )}
      <Hand label="Skill Cards" hand={player.skillCards} />
      <Pile label="Loyalty" cards={player.loyaltyCards} enumName="LoyaltyCard" />
      <Pile label="Motive" cards={player.motiveCards} enumName="MotiveCard" />
      <Pile label="Mutiny" cards={player.mutinyCards} enumName="MutinyCard" />
      <Pile label="Super Crisis" cards={player.superCrisisCards} enumName="SuperCrisisCard" />
      {player.seenLoyaltyCards && player.seenLoyaltyCards.length > 0 && (
        <p className="row">
          <span className="row-name">Looked at</span>
          {player.seenLoyaltyCards.map((seen, index) => (
            <span key={index} className="tile">
              P{seen.ownerPlayerNumber}: {constantLabel('LoyaltyCard', seen.card)}
            </span>
          ))}
        </p>
      )}
      {player.knownCivilianShips && player.knownCivilianShips.length > 0 && (
        <p className="row">
          <span className="row-name">Knows ships</span> {player.knownCivilianShips.map((id) => `#${id}`).join(', ')}
        </p>
      )}
      <p className="muted small">
        {player.turns} turns · hand limit {player.handLimit}
        {player.human === false ? ' · not human' : ''}
      </p>
    </article>
  );
}

export function Hand({ label, hand }: { label: string; hand: SkillHand | null }) {
  if (!hand) {
    return null;
  }
  return (
    <p className="row">
      <span className="row-name">
        {label} ({hand.count})
      </span>
      {hand.cards
        ? hand.cards.map((card, index) => (
            <span key={index} className={`tile color-${(card.type ?? '').toLowerCase()} skill-${colorOf(card.type)}`}>
              {skillCardLabel(card)}
            </span>
          ))
        : Object.entries(hand.colors ?? {}).map(([color, count]) => (
            <span key={color} className={`tile back skill-${color.toLowerCase()}`}>
              {count} {constantLabel('SkillCardColor', color).toLowerCase()}
            </span>
          ))}
    </p>
  );
}

function Pile<T extends string>({ label, cards, enumName }: { label: string; cards: HiddenCards<T> | null; enumName: string }) {
  if (!cards || (cards.count === 0 && (cards.revealed ?? []).length === 0)) {
    return null;
  }
  return (
    <p className="row">
      <span className="row-name">{label}</span>
      {(cards.revealed ?? []).map((card, index) => (
        <span key={`revealed-${index}`} className="tile revealed">
          {constantLabel(enumName, card)}
        </span>
      ))}
      {cards.cards
        ? cards.cards.map((card, index) => (
            <span key={`card-${index}`} className="tile">
              {constantLabel(enumName, card)}
            </span>
          ))
        : cards.count > 0 && <span className="tile back">{cards.count} face down</span>}
    </p>
  );
}

// The color of a Skill Card type, for its tile, from the deck it comes from.
function colorOf(type: string | null): string {
  return (type ?? '').toLowerCase();
}

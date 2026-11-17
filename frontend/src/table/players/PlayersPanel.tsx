import { cardArt, characterArt, skillCardArt, type CardEnum } from '../../art/images';
import { ArtImg, CardBack, CardTile } from '../../art/Art';
import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { HiddenCards, PlayerProjection, SkillHand } from '../../wire/types';
import { useTable } from '../tableContext';

/** Every player: character, location, titles, cards (or their backs), tokens and what he alone knows. */
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
  const president = !!titles?.president && titles.president === player.character;
  const badges = [
    president ? 'President' : null,
    titles?.admiral && titles.admiral === player.character ? 'Admiral' : null,
    titles?.cag && titles.cag === player.character ? 'CAG' : null,
    turn?.currentPlayer === player.number ? 'current' : null,
    turn?.actingPlayer === player.number && turn.actingPlayer !== turn.currentPlayer ? 'acting' : null,
    player.revealedCylon ? 'revealed Cylon' : null,
    player.infiltrating ? 'infiltrating' : null,
    player.mutineer ? 'Mutineer' : null,
    player.miracleToken ? 'miracle token' : null,
  ].filter((badge): badge is string => badge !== null);
  const token = characterArt(player.character);
  const classes = ['player', turn?.currentPlayer === player.number ? 'current' : '', player.revealedCylon ? 'cylon' : ''].join(' ').trim();
  return (
    <article className={classes} data-player={player.number}>
      <div className="player-head">
        {token && <ArtImg art={token} className="avatar" />}
        <div>
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
        </div>
      </div>
      <Hand label="Skill Cards" hand={player.skillCards} />
      <Pile label="Loyalty" cards={player.loyaltyCards} enumName="LoyaltyCard" deck="LOYALTY" />
      <Pile label="Motive" cards={player.motiveCards} enumName="MotiveCard" deck="MOTIVE" />
      <Pile label="Mutiny" cards={player.mutinyCards} enumName="MutinyCard" deck="MUTINY" />
      <Pile label="Super Crisis" cards={player.superCrisisCards} enumName="SuperCrisisCard" deck="SUPER_CRISIS" />
      {president && <Pile label="Quorum" cards={table.game?.presidentHand ?? null} enumName="QuorumCard" deck="QUORUM" />}
      {player.seenLoyaltyCards && player.seenLoyaltyCards.length > 0 && (
        <div className="row">
          <span className="row-name">Looked at</span>
          {player.seenLoyaltyCards.map((seen, index) => (
            <CardTile
              key={index}
              art={cardArt('LoyaltyCard', seen.card)}
              label={constantLabel('LoyaltyCard', seen.card)}
              caption={`P${seen.ownerPlayerNumber}`}
              size="xs"
            />
          ))}
        </div>
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

/** A hand of Skill Cards: the cards face up for who may see them, otherwise a back per color with how many. */
export function Hand({ label, hand }: { label: string; hand: SkillHand | null }) {
  if (!hand) {
    return null;
  }
  return (
    <div className="row">
      <span className="row-name">
        {label} ({hand.count})
      </span>
      <span className="hand">
        {hand.cards
          ? hand.cards.map((card, index) => (
              <CardTile key={index} art={skillCardArt(card)} label={skillCardLabel(card)} className={`skill-${(card.type ?? '').toLowerCase()}`} />
            ))
          : Object.entries(hand.colors ?? {}).map(([color, count]) => <CardBack key={color} color={color} count={count ?? 0} />)}
      </span>
    </div>
  );
}

function Pile<T extends string>({ label, cards, enumName, deck }: { label: string; cards: HiddenCards<T> | null; enumName: CardEnum; deck: string }) {
  if (!cards || (cards.count === 0 && (cards.revealed ?? []).length === 0)) {
    return null;
  }
  return (
    <div className="row">
      <span className="row-name">{label}</span>
      <span className="hand">
        {(cards.revealed ?? []).map((card, index) => (
          <CardTile key={`revealed-${index}`} art={cardArt(enumName, card)} label={constantLabel(enumName, card)} className="revealed" />
        ))}
        {cards.cards
          ? cards.cards.map((card, index) => <CardTile key={`card-${index}`} art={cardArt(enumName, card)} label={constantLabel(enumName, card)} />)
          : cards.count > 0 && <CardBack deck={deck} count={cards.count} label={`${cards.count} face down`} />}
      </span>
    </div>
  );
}

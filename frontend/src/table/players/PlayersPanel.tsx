import type { ReactNode } from 'react';
import { cardArt, characterArt, skillCardArt, titleArt, tokenArt, type ArtRef, type CardEnum, type TitleName, type TokenName } from '../../art/images';
import { ArtImg, CardBack, CardTile, previewHandlers } from '../../art/Art';
import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { HiddenCards, PlayerProjection, QuorumCardInPlay, SkillHand } from '../../wire/types';
import { faceDown, useRevealHandlers, type RevealSection } from '../reveal';
import { useTable } from '../tableContext';

/**
 * Every player's mat: his portrait with the titles and tokens in front of him under it (the nukes with the Admiral),
 * his character and the user in the seat, where he is, and his cards: face up for the viewer they belong to,
 * otherwise their backs. A sandbox shows the backs too, like a live game, and what they hide on hover.
 */
export function PlayersPanel() {
  const table = useTable();
  const players = table.game?.players ?? [];
  return (
    <section className="players" aria-label="players">
      <div className="mats">
        {players.map((player) => (
          <PlayerMat key={player.number} player={player} />
        ))}
      </div>
    </section>
  );
}

/** A player's mat as the game stands, e.g. beside his character sheet when the pointer is on his standee. */
export function PlayerMatOf({ playerNumber }: { playerNumber: number }) {
  const player = useTable().game?.players?.find((candidate) => candidate.number === playerNumber);
  return player ? <PlayerMat player={player} /> : null;
}

function PlayerMat({ player }: { player: PlayerProjection }) {
  const table = useTable();
  const game = table.game;
  const titles = game?.titles;
  const turn = game?.turn;
  const holds = (character: string | null | undefined) => !!character && character === player.character;
  const president = holds(titles?.president);
  const portrait = characterArt(player.character);
  const user = table.userName(player.number);
  const current = turn?.currentPlayer === player.number;
  const classes = ['player', 'mat', 'cf', current ? 'current' : '', player.revealedCylon ? 'cylon' : ''].join(' ').trim();
  return (
    <article className={classes} data-player={player.number}>
      <div className="mat-side">
        {portrait ? <ArtImg art={portrait} className="avatar" /> : <span className="avatar empty" />}
        <p className="badges">
          {president && <TitleBadge title="PRESIDENT" />}
          {holds(titles?.admiral) && <TitleBadge title="ADMIRAL" />}
          {/* the nukes lie in front of the Admiral */}
          {holds(titles?.admiral) && (game?.nukes ?? 0) > 0 && <Badge className="token nukes" art={tokenArt('NUKE')} label={`${game?.nukes} nuke${game?.nukes === 1 ? '' : 's'}`} />}
          {holds(titles?.cag) && <TitleBadge title="CAG" />}
          {player.infiltrating && <TitleBadge title="INFILTRATING" />}
          {player.mutineer && <Badge className="mutineer" art={cardArt('LoyaltyCard', 'MUTINEER')} label="Mutineer" />}
          {player.revealedCylon && <Badge className="cylon" art={null} label="Cylon" />}
          {/* a Cylon Leader who isn't infiltrating */}
          {player.human === false && !player.revealedCylon && <Badge className="cylon" art={null} label="Not human" />}
          {current && <TokenBadge token="CURRENT_PLAYER" label="Current" />}
          {turn?.actingPlayer === player.number && turn.actingPlayer !== turn.currentPlayer && <Badge className="token" art={null} label="Acting" />}
          {player.miracleTokens > 0 && <TokenBadge token="MIRACLE" label={player.miracleTokens > 1 ? `${player.miracleTokens} miracles` : 'Miracle'} />}
        </p>
      </div>
      <div className="mat-body">
        <header className="who">
          <strong>{player.character ? constantLabel('Character', player.character) : `Player ${player.number}`}</strong>
          {user && <span className="user">{user}</span>}
          {viewerIs(table.game, player.number) && <em className="you">you</em>}
        </header>
        <p className="where">{player.location ? constantLabel('Location', player.location) : player.character ? 'off the board' : 'no character yet'}</p>
        <Hand hand={player.skillCards} owner={player.number} />
        <div className="piles">
          <Pile label="Loyalty" cards={player.loyaltyCards} enumName="LoyaltyCard" deck="LOYALTY" owner={player.number} />
          <Pile label="Motive" cards={player.motiveCards} enumName="MotiveCard" deck="MOTIVE" owner={player.number} />
          <Pile label="Mutiny" cards={player.mutinyCards} enumName="MutinyCard" deck="MUTINY" owner={player.number} />
          <Pile label="Super Crisis" cards={player.superCrisisCards} enumName="SuperCrisisCard" deck="SUPER_CRISIS" owner={player.number} />
          {president && <Pile label="Quorum" cards={game?.presidentHand ?? null} enumName="QuorumCard" deck="QUORUM" owner={player.number} />}
          {/* the Quorum Card "Popular Influence" left him to play or discard */}
          {(player.quorumCards?.count ?? 0) > 0 && (
            <Pile label="Quorum to play" cards={player.quorumCards} enumName="QuorumCard" deck="QUORUM" owner={player.number} />
          )}
        </div>
        <InPlay cards={quorumCardsInPlay(game?.quorumCardsInPlay ?? [], player.number, president)} />
        <Learned player={player} />
      </div>
    </article>
  );
}

// The Quorum Cards in play in front of the player, face up: the ones given to him, and the ones without a holder
// ("Accept Prophecy") beside the President's hand.
function quorumCardsInPlay(cards: QuorumCardInPlay[], playerNumber: number, president: boolean): string[] {
  return cards
    .filter((card) => card.holderPlayerNumber === playerNumber || (card.holderPlayerNumber === null && president))
    .flatMap((card) => (card.card ? [card.card] : []));
}

function InPlay({ cards }: { cards: string[] }) {
  if (cards.length === 0) {
    return null;
  }
  return (
    <div className="in-play" aria-label="Quorum Cards in play">
      {cards.map((card) => (
        <CardTile key={card} art={cardArt('QuorumCard', card)} label={constantLabel('QuorumCard', card)} />
      ))}
    </div>
  );
}

function viewerIs(game: { viewer?: { kind?: string; playerNumber?: number } | null } | null, playerNumber: number): boolean {
  return game?.viewer?.kind === 'PlayerViewer' && game.viewer.playerNumber === playerNumber;
}

function TitleBadge({ title }: { title: TitleName }) {
  const art = titleArt(title);
  return <Badge className="title" art={art} label={art?.label ?? constantLabel('Title', title)} />;
}

function TokenBadge({ token, label }: { token: TokenName; label: string }) {
  return <Badge className="token" art={tokenArt(token)} label={label} round />;
}

// The whole badge shows the preview, not just its icon.
function Badge({ className, art, label, round = false }: { className: string; art: ArtRef | null; label: string; round?: boolean }) {
  return (
    <span className={`badge ${className}`} {...(art ? previewHandlers(art) : {})}>
      {art && <ArtImg art={art} className={round ? 'round' : ''} preview={false} />}
      {label}
    </span>
  );
}

// Face up only in a live game, for the viewer whose cards they are; a sandbox shows backs like an observer sees them.
function faceUp<T>(cards: T[] | null, sandbox: boolean): cards is T[] {
  return cards !== null && !sandbox;
}

/** A hand of Skill Cards: the cards face up for who may see them, otherwise a back per color with how many. */
export function Hand({ hand, owner }: { hand: SkillHand | null; owner: number }) {
  const table = useTable();
  if (!hand || hand.count === 0) {
    return null;
  }
  const cards = hand.cards;
  if (faceUp(cards, table.sandbox)) {
    return (
      <div className="hand">
        {cards.map((card, index) => (
          <CardTile key={index} art={skillCardArt(card)} label={skillCardLabel(card)} className={`skill-${(card.type ?? '').toLowerCase()}`} />
        ))}
      </div>
    );
  }
  const sections: RevealSection[] = [
    { title: 'Skill Cards', cards: (cards ?? []).map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: true },
  ];
  return (
    <Hidden id={`hand-${owner}`} title={`${table.playerName(owner)}: Skill Cards`} sections={sections} className="hand">
      {Object.entries(hand.colors ?? {}).map(([color, count]) => (
        <CardBack key={color} color={color} count={count ?? 0} size="xs" />
      ))}
    </Hidden>
  );
}

function Pile<T extends string>({ label, cards, enumName, deck, owner }: {
  label: string;
  cards: HiddenCards<T> | null;
  enumName: CardEnum;
  deck: string;
  owner: number;
}) {
  const table = useTable();
  if (!cards) {
    return null;
  }
  const revealed = cards.revealed ?? [];
  // who may see the pile gets all of it, the revealed cards among them
  const down = cards.cards ? faceDown(cards.cards, revealed) : null;
  const downCount = down ? down.length : cards.count;
  if (revealed.length === 0 && downCount === 0) {
    return null;
  }
  const tile = (card: T, key: string) => <CardTile key={key} art={cardArt(enumName, card)} label={constantLabel(enumName, card)} size="xs" />;
  return (
    <div className="pile">
      <span className="pile-cards">
        {revealed.map((card, index) => tile(card, `revealed-${index}`))}
        {faceUp(down, table.sandbox)
          ? down.map((card, index) => tile(card, `down-${index}`))
          : downCount > 0 && (
              <Hidden
                id={`${deck}-${owner}`}
                title={`${table.playerName(owner)}: ${label}`}
                sections={[{ title: label, cards: (down ?? []).map((card) => ({ art: cardArt(enumName, card), label: constantLabel(enumName, card) })), hidden: true }]}
              >
                <CardBack deck={deck} count={downCount} size="xs" label={`${downCount} face down`} />
              </Hidden>
            )}
      </span>
      <span className="pile-name">{label}</span>
    </div>
  );
}

// What the player alone knows: the Loyalty Cards he looked at, and the cargo of the civilian ships he looked at.
function Learned({ player }: { player: PlayerProjection }) {
  const table = useTable();
  const seen = player.seenLoyaltyCards ?? [];
  const known = player.knownCivilianShips ?? [];
  if (seen.length === 0 && known.length === 0) {
    return null;
  }
  const ships = [...(table.game?.galactica?.ships ?? []), ...(table.game?.galactica?.reserves ?? [])];
  const lines = (
    <>
      {seen.map((look, index) => {
        const art = cardArt('LoyaltyCard', look.card);
        return (
          <span key={`seen-${index}`} className="learned">
            {art && <ArtImg art={art} />}
            Saw {table.playerName(look.ownerPlayerNumber)}'s Loyalty Card: {constantLabel('LoyaltyCard', look.card)}
          </span>
        );
      })}
      {known.map((id) => {
        const cargo = ships.find((ship) => ship.id === id)?.cargo;
        return (
          <span key={`ship-${id}`} className="learned">
            Civilian ship #{id}
            {cargo ? `: ${cargo.fuel} fuel, ${cargo.morale} morale, ${cargo.population} population` : ''}
          </span>
        );
      })}
    </>
  );
  // in a sandbox everyone's looks are known: they stay out of sight until hovered, like the cards
  if (table.sandbox) {
    const sections: RevealSection[] = [
      { title: 'Loyalty Cards looked at', cards: seen.map((look) => ({ art: cardArt('LoyaltyCard', look.card), label: `${table.playerName(look.ownerPlayerNumber)}: ${constantLabel('LoyaltyCard', look.card)}` })), hidden: true },
      { title: 'Civilian ships looked at', cards: known.map((id) => ({ art: null, label: `Civilian ship #${id}` })), hidden: true },
    ];
    return (
      <Hidden id={`learned-${player.number}`} title={`${table.playerName(player.number)} knows`} sections={sections} className="learned-row">
        <span className="learned muted">knows {seen.length + known.length} hidden thing{seen.length + known.length > 1 ? 's' : ''}</span>
      </Hidden>
    );
  }
  return <div className="learned-row">{lines}</div>;
}

// Something the table can't see, which a sandbox shows on hover.
function Hidden({ id, title, sections, className = '', children }: { id: string; title: string; sections: RevealSection[]; className?: string; children: ReactNode }) {
  const handlers = useRevealHandlers(id, title, sections);
  return (
    <span className={`hidden-cards ${className}`.trim()} {...handlers}>
      {children}
    </span>
  );
}

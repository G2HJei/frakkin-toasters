import type { ReactNode } from 'react';
import { cardArt, characterArt, skillCardArt, titleArt, titleIcon, tokenArt, type ArtRef, type CardEnum, type TitleName, type TokenName } from '../../art/images';
import { ArtImg, CardBack, CardTile, previewHandlers, SkillCardBack } from '../../art/Art';
import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { HiddenCards, PlayerProjection, QuorumCardInPlay, SkillCard, SkillHand } from '../../wire/types';
import { faceDown, useRevealHandlers, type RevealSection } from '../reveal';
import { knowingPlayer, useTable, type SeatState } from '../tableContext';
import { sortSkillCards } from '../skillCards';
import { signed, useChanges } from '../changes';
import { useReference } from '../reference/ReferenceDrawer';
import { anchorOf, useAnswering } from '../../input/boardAnswers';
import { targetKey, type Target } from '../../input/answers';
import { characterSkillSet, characterType } from '../../wire/schema';

/**
 * Every player's mat: his portrait with his skill set's colors beside it and the titles and tokens in front of him
 * under it (the nukes with the Admiral),
 * his character and the user in the seat, where he is, and his cards: face up for the viewer they belong to,
 * otherwise their backs, his Loyalty Cards in a halo when the viewer looked at one. A sandbox shows the backs too, like
 * a live game, and what they hide on hover.
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

// Whether the user in the seat is there: a dot by his name, and a chip when he isn't, or nobody holds the seat.
export function SeatPresence({ state }: { state: SeatState | null }) {
  if (!state) {
    return null;
  }
  if (state === 'vacant') {
    return <span className="presence-chip vacant">vacant seat</span>;
  }
  return (
    <>
      <span className={`presence ${state}`} role="img" aria-label={state === 'online' ? 'connected' : 'disconnected'} title={state === 'online' ? 'Connected' : 'Disconnected'} />
      {state === 'away' && <span className="presence-chip">away</span>}
    </>
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
  const acting = turn?.actingPlayer === player.number;
  // the Loyalty Cards of his that the viewer looked at, whether he still holds them or not
  const knowing = knowingPlayer(table);
  const seen =
    knowing && knowing.number !== player.number
      ? (knowing.seenLoyaltyCards ?? []).flatMap((look) => (look.ownerPlayerNumber === player.number && look.card ? [look.card] : []))
      : [];
  const { offer, onTarget } = useAnswering();
  const key = `player:${player.number}`;
  const lit = offer.targets.has(key);
  const classes = ['player', 'mat', 'cf', acting ? 'acting' : '', player.revealedCylon ? 'cylon' : '', lit ? 'target' : '', offer.picked.has(key) ? 'picked' : '']
    .join(' ')
    .trim();
  return (
    <article
      className={classes}
      data-player={player.number}
      onClick={lit ? (event) => onTarget({ type: 'player', playerNumber: player.number }, anchorOf(event)) : undefined}
    >
      <div className="mat-side">
        <div className="portrait">
          {portrait ? (
            // the character sheet in the reference, a click away
            <button
              type="button"
              className="avatar-frame avatar-button"
              aria-label={`${constantLabel('Character', player.character)}: character sheet`}
              onClick={(event) => {
                event.stopPropagation();
                useReference.getState().show('characters', player.character);
              }}
            >
              <ArtImg art={portrait} className="avatar" />
            </button>
          ) : (
            <span className="avatar-frame">
              <span className="avatar empty" />
            </span>
          )}
          {/* a revealed Cylon draws 2 cards of any colors instead (rule 8.2.2.3), a Cylon Leader his skill set, infiltrating or not (rule 8.2.2.4) */}
          {(!player.revealedCylon || characterType(player.character) === 'CYLON_LEADER') && <SkillStrip character={player.character} />}
        </div>
        <p className="badges">
          {president && <TitleBadge title="PRESIDENT" />}
          {holds(titles?.admiral) && <TitleBadge title="ADMIRAL" />}
          {/* the nukes lie in front of the Admiral */}
          {holds(titles?.admiral) && (game?.nukes ?? 0) > 0 && <Badge className="token nukes" art={tokenArt('NUKE')} label={`${game?.nukes} nuke${game?.nukes === 1 ? '' : 's'}`} />}
          {holds(titles?.admiral) && <NukesDelta />}
          {holds(titles?.cag) && <TitleBadge title="CAG" />}
          {player.infiltrating && <TitleBadge title="INFILTRATING" />}
          {player.mutineer && <Badge className="mutineer" art={cardArt('LoyaltyCard', 'MUTINEER')} label="Mutineer" />}
          {player.revealedCylon && <Badge className="cylon" art={null} label="Cylon" />}
          {/* a Cylon Leader who isn't infiltrating */}
          {player.human === false && !player.revealedCylon && <Badge className="cylon" art={null} label="Not human" />}
          {acting && !current && <Badge className="token" art={null} label="Acting" />}
          {player.miracleTokens > 0 && <TokenBadge token="MIRACLE" label={player.miracleTokens > 1 ? `${player.miracleTokens} miracles` : 'Miracle'} />}
        </p>
      </div>
      <div className="mat-body">
        <header className="who">
          <strong>{player.character ? constantLabel('Character', player.character) : `Player ${player.number}`}</strong>
          {user && <span className="user">{user}</span>}
          <SeatPresence state={table.seatState(player.number)} />
          {viewerIs(table.game, player.number) && <em className="you">you</em>}
        </header>
        <p className="where">{player.location ? constantLabel('Location', player.location) : player.character ? 'off the board' : 'no character yet'}</p>
        <Hand hand={player.skillCards} owner={player.number} />
        <div className="piles">
          <Pile label="Loyalty" cards={player.loyaltyCards} enumName="LoyaltyCard" deck="LOYALTY" owner={player.number} seen={seen} />
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

/** The colors of the Skill Cards his sheet draws, a cell per card, and a cell split in two colors draws either. */
function SkillStrip({ character }: { character: string | null | undefined }) {
  const skillSet = characterSkillSet(character);
  if (skillSet.length === 0) {
    return null;
  }
  const label = `Skills: ${skillSet.map((option) => `${option.count} ${option.colors.map((color) => constantLabel('SkillCardColor', color)).join(' or ')}`).join(', ')}`;
  return (
    <span className="skill-strip" role="img" aria-label={label} title={label}>
      {skillSet.flatMap((option, line) =>
        Array.from({ length: option.count }, (_, card) => (
          <span key={`${line}-${card}`} className="skill-cell" data-colors={option.colors.join(' ')} style={{ background: cellBackground(option.colors) }} />
        )),
      )}
    </span>
  );
}

// Two colors split the cell along its diagonal: a corner keyword runs the 50% line through the other two corners.
function cellBackground(colors: string[]): string {
  const [first, second] = colors.map((color) => `var(--${color.toLowerCase()})`);
  return second ? `linear-gradient(to bottom right, ${first} 50%, ${second} 50%)` : first;
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
  return <Badge className="title" art={art} icon={titleIcon(title)} label={art?.label ?? constantLabel('Title', title)} />;
}

function TokenBadge({ token, label }: { token: TokenName; label: string }) {
  return <Badge className="token" art={tokenArt(token)} label={label} round />;
}

// The whole badge shows the preview, not just its icon. An icon stands in for the art's image, which still previews.
function Badge({ className, art, icon, label, round = false }: { className: string; art: ArtRef | null; icon?: string; label: string; round?: boolean }) {
  return (
    <span className={`badge ${className}`} {...(art ? previewHandlers(art) : {})}>
      {icon ? <img className="icon" src={icon} alt={label} draggable={false} /> : art && <ArtImg art={art} className={round ? 'round' : ''} preview={false} />}
      {label}
    </span>
  );
}

// Face up only in a live game, for the viewer whose cards they are; a sandbox shows backs like an observer sees them,
// but the asked player's own, which the sandbox answers for as he would.
function faceUp<T>(cards: T[] | null, sandbox: boolean, asked = false): cards is T[] {
  return cards !== null && (!sandbox || asked);
}

/** A card that starts a move of the pending input lights up, and a click picks it. */
function Playable({ target, children }: { target: Target; children: ReactNode }) {
  const { offer, onTarget } = useAnswering();
  const key = targetKey(target);
  if (!offer.targets.has(key) && !offer.picked.has(key)) {
    return <>{children}</>;
  }
  return (
    <button
      type="button"
      className={`playable ${offer.picked.has(key) ? 'picked' : ''}`.trim()}
      aria-pressed={offer.picked.has(key)}
      onClick={(event) => {
        event.stopPropagation();
        onTarget(target, anchorOf(event));
      }}
    >
      {children}
    </button>
  );
}

// The nukes the Admiral gained or spent with the last game message, floating up once.
function NukesDelta() {
  const { changes, version } = useChanges();
  return changes.nukes !== 0 ? (
    <span key={version} className={`float-delta ${changes.nukes < 0 ? 'loss' : 'gain'}`}>
      {signed(changes.nukes)} nuke{Math.abs(changes.nukes) === 1 ? '' : 's'}
    </span>
  ) : null;
}

// The cards of a hand that came with the last game message: as many of each as it gained.
function drawnFlags(cards: SkillCard[], gained: SkillCard[]): boolean[] {
  const left = gained.map((card) => JSON.stringify(card));
  return cards.map((card) => {
    const at = left.indexOf(JSON.stringify(card));
    return at >= 0 && left.splice(at, 1).length === 1;
  });
}

/** A hand of Skill Cards: the cards face up for who may see them, otherwise the back of a Skill Card with how many. */
export function Hand({ hand, owner }: { hand: SkillHand | null; owner: number }) {
  const table = useTable();
  const { changes, version } = useChanges();
  if (!hand || hand.count === 0) {
    return null;
  }
  const cards = hand.cards ? sortSkillCards(hand.cards) : null;
  if (faceUp(cards, table.sandbox, table.askedPlayer === owner)) {
    return (
      <div className="hand">
        {(() => {
          // a card that just came slides in, once
          const drawn = drawnFlags(cards, changes.gained.get(owner) ?? []);
          return cards.map((card, index) => (
            <Playable key={drawn[index] ? `${index}-${version}` : index} target={{ type: 'card', deck: 'SkillCard', card }}>
              <CardTile art={skillCardArt(card)} label={skillCardLabel(card)} className={`skill-${(card.type ?? '').toLowerCase()}${drawn[index] ? ' drawn' : ''}`} />
            </Playable>
          ));
        })()}
      </div>
    );
  }
  const sections: RevealSection[] = [
    { title: 'Skill Cards', cards: (cards ?? []).map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: true },
  ];
  return (
    <Hidden id={`hand-${owner}`} title={`${table.playerName(owner)}: Skill Cards`} sections={sections} className="hand">
      {(() => {
        // the backs don't show the colors: one back with how many, which slides in when the hand grew
        const drawn = (changes.gainedBacks.get(owner) ?? 0) > 0;
        return (
          <span key={drawn ? `back-${version}` : 'back'} className={drawn ? 'drawn' : undefined}>
            <SkillCardBack count={hand.count} size="xs" />
          </span>
        );
      })()}
    </Hidden>
  );
}

function Pile<T extends string>({ label, cards, enumName, deck, owner, seen = [] }: {
  label: string;
  cards: HiddenCards<T> | null;
  enumName: CardEnum;
  deck: string;
  owner: number;
  // the cards of his the viewer looked at, whether he still holds them or not: the face down cards lie in a halo, and
  // their popover lists them without telling which of the face down cards they are
  seen?: T[];
}) {
  const table = useTable();
  if (!cards) {
    return null;
  }
  const revealed = cards.revealed ?? [];
  // who may see the pile gets all of it, the revealed cards among them
  const down = cards.cards ? faceDown(cards.cards, revealed) : null;
  const downCount = down ? down.length : cards.count;
  const open = faceUp(down, table.sandbox, table.askedPlayer === owner);
  const looked = open ? [] : seen;
  if (revealed.length === 0 && downCount === 0 && looked.length === 0) {
    return null;
  }
  const tile = (card: T, key: string) => (
    <Playable key={key} target={{ type: 'card', deck: enumName, card }}>
      <CardTile art={cardArt(enumName, card)} label={constantLabel(enumName, card)} size="xs" />
    </Playable>
  );
  const asTiles = (list: T[]) => list.map((card) => ({ art: cardArt(enumName, card), label: constantLabel(enumName, card) }));
  const sections: RevealSection[] = [
    { title: label, cards: asTiles(down ?? []), hidden: true },
    { title: `${label} Cards looked at`, cards: asTiles(looked) },
  ];
  return (
    <div className="pile">
      <span className="pile-cards">
        {revealed.map((card, index) => tile(card, `revealed-${index}`))}
        {open
          ? down.map((card, index) => tile(card, `down-${index}`))
          : (downCount > 0 || looked.length > 0) && (
              <Hidden id={`${deck}-${owner}`} title={`${table.playerName(owner)}: ${label}`} sections={sections} className={looked.length > 0 ? 'known' : ''}>
                <CardBack deck={deck} count={downCount} size="xs" label={`${downCount} face down`} />
              </Hidden>
            )}
      </span>
      <span className="pile-name">{label}</span>
    </div>
  );
}

// What the player alone knows in a sandbox, where everyone's looks are known: they stay out of sight until hovered,
// like the cards. A live table shows the viewer's in a halo: the Loyalty Cards of a player he looked at, and the
// civilian ships on the board.
function Learned({ player }: { player: PlayerProjection }) {
  const table = useTable();
  const seen = player.seenLoyaltyCards ?? [];
  const known = player.knownCivilianShips ?? [];
  if (!table.sandbox || (seen.length === 0 && known.length === 0)) {
    return null;
  }
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

// Something the table can't see, which a sandbox shows on hover.
function Hidden({ id, title, sections, className = '', children }: { id: string; title: string; sections: RevealSection[]; className?: string; children: ReactNode }) {
  const handlers = useRevealHandlers(id, title, sections);
  return (
    <span className={`hidden-cards ${className}`.trim()} {...handlers}>
      {children}
    </span>
  );
}

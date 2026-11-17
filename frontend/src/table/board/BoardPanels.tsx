import { characterArt, deckBackArt, deckCardArt, deckCards, shipArt } from '../../art/images';
import { ArtImg, CardBack } from '../../art/Art';
import { cardLabel, constantLabel } from '../../labels/describe';
import type { DeckProjection, ShipProjection } from '../../wire/types';
import { useTable } from '../tableContext';

/** What the board leaves out: the reserves, the damaged vipers, the Cylon supply and what is out of the game. */
export function BoardExtras() {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const galactica = game.galactica;
  const supply = game.cylonSupply;
  return (
    <section className="panel board-extras" aria-label="board extras">
      <ShipRow title="Reserves" ships={galactica?.reserves ?? []} />
      <ShipRow title="Damaged vipers" ships={galactica?.damagedVipers ?? []} />
      <p className="row">
        <span className="row-name">Cylon supply</span>
        <Supply type="BASESTAR" count={supply?.basestars} />
        <Supply type="RAIDER" count={supply?.raiders} />
        <Supply type="HEAVY_RAIDER" count={supply?.heavyRaiders} />
        <span className="ship-chip">{supply?.centurions ?? 0} centurions</span>
      </p>
      {((game.removedCharacters ?? []).length > 0 || (game.destroyedShips ?? []).length > 0) && (
        <p className="row out-of-game">
          <span className="row-name">Out of the game</span>
          {(game.removedCharacters ?? []).map((character) => {
            const art = characterArt(character);
            return art ? <ArtImg key={character} art={art} className="removed" /> : <span key={character}>{constantLabel('Character', character)}</span>;
          })}
          {(game.destroyedShips ?? []).map((ship) => (
            <ShipChip key={ship.id} ship={ship} />
          ))}
        </p>
      )}
    </section>
  );
}

function ShipRow({ title, ships }: { title: string; ships: ShipProjection[] }) {
  if (ships.length === 0) {
    return null;
  }
  return (
    <p className="row">
      <span className="row-name">{title}</span>
      {ships.map((ship) => (
        <ShipChip key={ship.id} ship={ship} />
      ))}
    </p>
  );
}

function ShipChip({ ship }: { ship: ShipProjection }) {
  const art = shipArt(ship.type);
  return (
    <span className="ship-chip" data-ship={ship.id} title={shipLabel(ship)}>
      {art && <ArtImg art={{ ...art, label: shipLabel(ship) }} />}
      <span>#{ship.id}</span>
    </span>
  );
}

function Supply({ type, count }: { type: string; count: number | undefined }) {
  const art = shipArt(type);
  return (
    <span className="ship-chip" title={`${count ?? 0} ${constantLabel('ShipType', type)}`}>
      {art ? <ArtImg art={art} /> : constantLabel('ShipType', type)}
      <span>×{count ?? 0}</span>
    </span>
  );
}

export function shipLabel(ship: ShipProjection): string {
  const parts = [`${constantLabel('ShipType', ship.type)} #${ship.id}`];
  if (ship.pilot) {
    parts.push(`pilot ${constantLabel('Character', ship.pilot)}`);
  }
  if (ship.damage && ship.damage.length > 0) {
    parts.push(`damage ${ship.damage.map((damage) => constantLabel('BasestarDamage', damage)).join(', ')}`);
  }
  if (ship.cargo) {
    parts.push(`cargo ${ship.cargo.fuel} fuel, ${ship.cargo.morale} morale, ${ship.cargo.population} population`);
  }
  return parts.join(' · ');
}

/** The decks: their backs and counts, the top of each discard pile face up, and the draw piles in order for the sandbox. */
export function DecksPanel() {
  const table = useTable();
  return (
    <section className="panel decks" aria-label="decks">
      <h2>Decks</h2>
      <table>
        <thead>
          <tr>
            <th>Deck</th>
            <th>Draw</th>
            <th>Discard</th>
          </tr>
        </thead>
        <tbody>
          {(table.game?.decks ?? []).map((deck) => (
            <tr key={deck.deck} data-deck={deck.deck}>
              <td>
                <span className="deck-name">
                  <DeckBack deck={deck} />
                  {constantLabel('DeckId', deck.deck)}
                </span>
              </td>
              <td>{deck.drawPile ? <Pile cards={deck.drawPile} deck={deck} count={deck.size} /> : deck.size}</td>
              <td>
                {deck.discarded && deck.discarded.length > 0 ? (
                  <span className="discard">
                    <TopCard deck={deck} />
                    <Pile cards={deck.discarded} deck={deck} count={deck.discardSize} />
                  </span>
                ) : (
                  deck.discardSize
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

const SKILL_COLORS = ['POLITICS', 'LEADERSHIP', 'TACTICS', 'PILOTING', 'ENGINEERING', 'TREACHERY'];

// The back of a deck, or a Skill Card deck's color.
function DeckBack({ deck }: { deck: DeckProjection }) {
  const back = deckBackArt(deck.deck);
  if (back) {
    return <ArtImg art={back} className="deck-back" />;
  }
  if (SKILL_COLORS.includes(deck.deck ?? '') || deck.deck === 'DESTINY') {
    return <CardBack color={deck.deck} count={deck.size} size="xs" label={constantLabel('DeckId', deck.deck)} />;
  }
  return null;
}

// The last card discarded lies on top, face up.
function TopCard({ deck }: { deck: DeckProjection }) {
  const top = deck.discarded?.[deck.discarded.length - 1];
  const art = deckCardArt(top, deckCards(deck.deck));
  return art ? <ArtImg art={art} className="top-card" /> : null;
}

function Pile({ cards, deck, count }: { cards: unknown[]; deck: DeckProjection; count: number }) {
  const kind = deckCards(deck.deck);
  return (
    <details>
      <summary>{count}</summary>
      <ol className="pile">
        {cards.map((card, index) => {
          const art = deckCardArt(card, kind);
          return (
            <li key={index}>
              {art && <ArtImg art={art} className="pile-card" />}
              {cardLabel(card)}
            </li>
          );
        })}
      </ol>
    </details>
  );
}

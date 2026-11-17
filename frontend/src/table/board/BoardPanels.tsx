import { cardLabel, constantLabel } from '../../labels/describe';
import type { ShipProjection } from '../../wire/types';
import { useTable } from '../tableContext';

/** What the board's schematic leaves out: the reserves, the damaged vipers, the Cylon supply and what is out of the game. */
export function BoardExtras() {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const galactica = game.galactica;
  return (
    <section className="panel board-extras" aria-label="board extras">
      <ShipRow title="Reserves" ships={galactica?.reserves ?? []} />
      <ShipRow title="Damaged vipers" ships={galactica?.damagedVipers ?? []} />
      <p className="muted small">
        Cylon supply: {game.cylonSupply?.basestars} basestars, {game.cylonSupply?.raiders} raiders, {game.cylonSupply?.heavyRaiders} heavy raiders,{' '}
        {game.cylonSupply?.centurions} centurions
      </p>
      {((game.removedCharacters ?? []).length > 0 || (game.destroyedShips ?? []).length > 0) && (
        <p className="muted small">
          Out of the game: {(game.removedCharacters ?? []).map((character) => constantLabel('Character', character)).join(', ')}{' '}
          {(game.destroyedShips ?? []).map(shipLabel).join(', ')}
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
        <span key={ship.id} className="tile">
          {shipLabel(ship)}
        </span>
      ))}
    </p>
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

/** The decks: their counts, the discard piles that lie face up, and the draw piles in order for the sandbox. */
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
              <td>{constantLabel('DeckId', deck.deck)}</td>
              <td>
                {deck.drawPile ? (
                  <details>
                    <summary>{deck.size}</summary>
                    <ol className="pile">
                      {deck.drawPile.map((card, index) => (
                        <li key={index}>{cardLabel(card)}</li>
                      ))}
                    </ol>
                  </details>
                ) : (
                  deck.size
                )}
              </td>
              <td>
                {deck.discarded && deck.discarded.length > 0 ? (
                  <details>
                    <summary>{deck.discardSize}</summary>
                    <ol className="pile">
                      {deck.discarded.map((card, index) => (
                        <li key={index}>{cardLabel(card)}</li>
                      ))}
                    </ol>
                  </details>
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

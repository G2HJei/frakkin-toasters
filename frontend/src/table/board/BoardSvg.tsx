import { useLayoutEffect, useRef, type ReactNode } from 'react';
import { signed, useChanges, type Changes } from '../changes';
import { useGameStore } from '../../live/gameStore';
import {
  cardArt,
  characterArt,
  damageArt,
  deckBackArt,
  deckCardArt,
  deckCards,
  locationArt,
  pilotArt,
  shipArt,
  skillBackArt,
  skillCheckArt,
  tokenArt,
  type ArtRef,
} from '../../art/images';
import { previewHandlers, SvgArt } from '../../art/Art';
import { BOARD_LABELS, type BoardLabel } from '../../labels/board';
import { cardLabel, constantLabel } from '../../labels/describe';
import type { CivilianShip, Character, DeckProjection, GameProjection, Location, ShipProjection } from '../../wire/types';
import { PlayerMatOf } from '../players/PlayersPanel';
import { anchorOf, useAnswering } from '../../input/boardAnswers';
import { useFlash } from '../flash';
import { moveSentence } from '../../input/moveLabels';
import type { Move, Target } from '../../input/answers';
import type { LabelContext } from '../../labels/describe';
import { useRevealHandlers, type RevealCard, type RevealSection } from '../reveal';
import { useTable } from '../tableContext';
import boardUrl from './board.jpg';
import boardingPartyUrl from './boarding-party.png';
import {
  BOARD_HEIGHT,
  BOARD_WIDTH,
  BOARDING_TRACK,
  DECK_SLOTS,
  DISTANCE,
  JUMP_TRACK,
  LABELS,
  LAUNCH_TUBES,
  LAYOUT,
  PURSUIT_TRACK,
  RESOURCES,
  SLOTS,
  TITLES,
  type Box,
  type Place,
  type Point,
} from './layout';
import { CargoIcons, cargoText, inRedZone, RESOURCE_NAMES, resourceIconUrl, resourceLabel, type ResourceName } from './resources';


// the decks whose discard pile lies face down (Disclosure.FACEDOWN_DISCARDS in the core)
const FACEDOWN_DISCARDS = new Set(['CIVILIAN_SHIPS', 'GALACTICA_DAMAGE', 'PEGASUS_DAMAGE', 'BASESTAR_DAMAGE', 'DESTINATION', 'DESTINY', 'LOYALTY', 'LOYALTY_NOT_CYLON', 'MOTIVE']);

/**
 * The board as it lies on the table: board.jpg, with every component on its printed slot. The characters stand on the
 * printed names of their locations, the ships fly in the middle of the space areas, the resources lie in a row of
 * gauges (an icon and a strip of pips, the amount on hover), the markers sit on the tracks, the decks lie in the rack with their counts and the top of their discard
 * piles, the civilian ships lie with the ship reserves, and the destinations tuck under the objective. The cards kept in
 * play lie on the free starfield. The card being resolved, its skill check and the die are the turn panel's, and the
 * nukes the Admiral's mat's. Hovering a deck shows its discard pile, and in a sandbox what the players can't see;
 * hovering a location shows its card, and hovering a standee its character sheet with its player's mat. What the
 * pending input's moves name lights up and takes a click (`input/boardAnswers.ts`): locations, ships, standees, and the
 * "Act" chip of a location action.
 */
export function BoardSvg() {
  const table = useTable();
  const { offer, onTarget } = useAnswering();
  const flashed = useFlash((state) => state.keys);
  const { changes, version } = useChanges();
  const svg = useRef<SVGSVGElement>(null);
  useGlidingShips(svg, version);
  const game = table.game;
  if (!game) {
    return null;
  }
  const galactica = game.galactica;
  const locations = [...(galactica?.locations ?? []), ...(game.pegasus?.locations ?? []), ...(game.cylonFleet?.locations ?? [])];
  const ships = [...(galactica?.ships ?? []), ...(game.cylonFleet?.ships ?? [])];
  const actingCharacter = game.players?.find((player) => player.number === game.turn?.actingPlayer)?.character ?? null;

  return (
    <svg ref={svg} className="board-svg" viewBox={`0 0 ${BOARD_WIDTH} ${BOARD_HEIGHT}`} role="img" aria-label="the board">
      <image href={boardUrl} x={0} y={0} width={BOARD_WIDTH} height={BOARD_HEIGHT} className="board-image" />
      <SectionLabels />
      <Resources game={game} />
      <Tracks game={game} />
      <Reserves game={game} />
      <DeckRack game={game} />
      <Objective game={game} />
      <FreeSpace game={game} />

      {locations.map((location) => {
        const name = location.location as Location;
        const place = LAYOUT[name];
        if (!place) {
          return null;
        }
        const targetKey = `location:${name}`;
        const lit = offer.targets.get(targetKey);
        const isTarget = lit !== undefined;
        const act = offer.acts.get(name);
        const space = place.area === 'galacticaSpace' || place.area === 'cylonFleetSpace';
        const destroyed = (place.area === 'colonialOne' && (galactica?.colonialOneDestroyed ?? false))
          || (place.area === 'pegasus' && (game.pegasus?.destroyed ?? false));
        // the location's card, large enough to read, on hover; the space areas have none
        const card = locationArt(name);
        return (
          <g
            key={name}
            className={[
              'location',
              place.area,
              location.damaged ? 'damaged' : '',
              destroyed ? 'destroyed' : '',
              isTarget ? 'target' : '',
              offer.picked.has(targetKey) ? 'picked' : '',
              flashed.has(targetKey) ? 'flash' : '',
              changes.damaged.includes(name) ? 'just-damaged' : '',
            ].join(' ')}
            data-location={name}
            onClick={isTarget ? (event) => onTarget({ type: 'location', location: name }, anchorOf(event)) : undefined}
            role={isTarget ? 'button' : undefined}
            aria-label={isTarget ? constantLabel('Location', name) : undefined}
          >
            {lit && <title>{hint(lit, table)}</title>}
            <polygon className={space ? 'space-frame' : 'frame'} points={cutBox(place.x - 6, place.y - 6, place.width + 12, place.height + 12, outsideCut(LOCATION_CUT, 6))} />
            {space ? (
              <Ships place={place} ships={ships.filter((ship) => ship.location === name)} toMiss={game.turn?.raidersToMiss?.[name] ?? 0} avoid={LAUNCH_TUBES[name]} />
            ) : (
              <LocationMarks place={place} destroyed={destroyed} engineRoom={name === 'ENGINE_ROOM' && (game.pegasus?.engineRoomActivated ?? false)} />
            )}
            {/* under the standees, which show their own cards */}
            {card && <rect className="location-hotspot" x={place.x} y={place.y} width={place.width} height={place.height} {...previewHandlers(card)} />}
            <Standees place={place} title={TITLES[name]} characters={(location.characters ?? []) as Character[]} acting={actingCharacter} nameOf={table.playerName} game={game} />
            {!space && location.damaged && <DamagedLabel place={place} title={TITLES[name]} />}
            {act && (
              <g
                className="act"
                role="button"
                aria-label="Act"
                onClick={(event) => {
                  event.stopPropagation();
                  onTarget({ type: 'kind', kind: act.kind }, anchorOf(event));
                }}
              >
                <title>{hint(act.moves, table)}</title>
                <rect x={place.x + place.width - 96} y={place.y - 22} width={110} height={52} rx={6} />
                <text x={place.x + place.width - 41} y={place.y + 14} textAnchor="middle">
                  Act
                </text>
              </g>
            )}
          </g>
        );
      })}
      <Deltas changes={changes} version={version} jump={game.galactica?.jumpPreparation ?? null} />
    </svg>
  );
}

// What changed with the last game message floats up where it shows: the resources, the jump track and the distance.
// Keyed by the version, so each change plays once.
function Deltas({ changes, version, jump }: { changes: Changes; version: number; jump: string | null }) {
  const marker = jump ? JUMP_TRACK[jump] : null;
  return (
    <g className="deltas" aria-hidden="true">
      {changes.resources.map(({ resource, delta }) => (
        <text
          key={`${resource}-${version}`}
          className={`delta ${delta < 0 ? 'loss' : 'gain'}`}
          x={RESOURCES[resource].x + RESOURCES[resource].width / 2}
          y={RESOURCES[resource].y - 14}
          textAnchor="middle"
        >
          {signed(delta)} {resourceLabel(resource)}
        </text>
      ))}
      {changes.jump && marker && (
        <text key={`jump-${version}`} className="delta gain" x={marker.x} y={marker.y - 60} textAnchor="middle">
          {changes.jump.reset ? 'Jump!' : signed(changes.jump.delta)}
        </text>
      )}
      {changes.distance > 0 && (
        <text key={`distance-${version}`} className="delta gain" x={DISTANCE.x + DISTANCE.width / 2} y={DISTANCE.y - 16} textAnchor="middle">
          {signed(changes.distance)} distance
        </text>
      )}
    </g>
  );
}

// A ship that moved glides from where it was: its last place is kept by id, and the new one starts there (FLIP). A
// snapshot (no previous game) only notes the places.
function useGlidingShips(svg: React.RefObject<SVGSVGElement | null>, version: number) {
  const places = useRef(new Map<string, { x: number; y: number }>());
  useLayoutEffect(() => {
    const root = svg.current;
    if (!root) {
      return;
    }
    const animate = useGameStore.getState().previous !== null;
    const next = new Map<string, { x: number; y: number }>();
    root.querySelectorAll<SVGGraphicsElement>('[data-ship]').forEach((element) => {
      const id = element.dataset.ship ?? '';
      let box: { x: number; y: number };
      try {
        box = element.getBBox();
      } catch {
        return;
      }
      next.set(id, { x: box.x, y: box.y });
      const before = places.current.get(id);
      const dx = before ? before.x - box.x : 0;
      const dy = before ? before.y - box.y : 0;
      if (animate && (dx !== 0 || dy !== 0) && typeof element.animate === 'function') {
        element.animate([{ transform: `translate(${dx}px, ${dy}px)` }, { transform: 'translate(0, 0)' }], { duration: 650, easing: 'ease-in-out' });
      }
    });
    places.current = next;
  }, [svg, version]);
}

// What a click on a lit target does: the move it sends, or how many it leaves to choose from.
function hint(moves: Move[], context: LabelContext): string {
  return moves.length === 1 ? moveSentence(moves[0], context) : `${moves.length} answers`;
}

// A lit piece takes the click before the location under it.
function targetHandlers(target: Target, onTarget: (target: Target, anchor: { x: number; y: number }) => void) {
  return {
    role: 'button',
    onClick: (event: React.MouseEvent) => {
      event.stopPropagation();
      onTarget(target, anchorOf(event));
    },
  };
}

// ---------------------------------------------------------------- the section labels

// like the badges on the players' mats: a chip with its top left and bottom right corners cut, in greys here so it stays
// out of the way; the board's font is about 0.62 of its size a letter, its letters 0.08 of it apart
const LABEL_HEIGHT = 30;
const LABEL_FONT = 15;
const LABEL_LETTER = 0.7;
const LABEL_PAD = 12;
const LABEL_CUT = 7;

/** Each section's name, where the picture once printed it: on the starfield, or on the top edge of its box. */
function SectionLabels() {
  return (
    <g className="section-labels">
      {(Object.keys(BOARD_LABELS) as BoardLabel[]).map((key) => {
        const name = BOARD_LABELS[key];
        const at = LABELS[key];
        const width = name.length * LABEL_FONT * LABEL_LETTER + 2 * LABEL_PAD;
        const [left, top, right, bottom] = [at.x - width / 2, at.y - LABEL_HEIGHT / 2, at.x + width / 2, at.y + LABEL_HEIGHT / 2];
        const points = [
          [left + LABEL_CUT, top],
          [right, top],
          [right, bottom - LABEL_CUT],
          [right - LABEL_CUT, bottom],
          [left, bottom],
          [left, top + LABEL_CUT],
        ];
        return (
          <g key={key} className="section-label" data-label={key}>
            <polygon points={points.map(([x, y]) => `${x},${y}`).join(' ')} />
            <text x={at.x} y={at.y + LABEL_FONT * 0.36} textAnchor="middle" fontSize={LABEL_FONT}>
              {name.toUpperCase()}
            </text>
          </g>
        );
      })}
    </g>
  );
}

// ---------------------------------------------------------------- the resources and the tracks

function Resources({ game }: { game: GameProjection }) {
  return (
    <g className="resources">
      {RESOURCE_NAMES.map((resource) => (
        <Gauge key={resource} resource={resource} value={game.galactica?.[resource] ?? null} box={RESOURCES[resource]} />
      ))}
    </g>
  );
}

// the icon right-aligned in its place, so each strip starts as far from it; the printed dials go up to 15: a pip for
// each, as high as the resource's box, in fives a little apart
const ICON_WIDTH = 54;
// the phoenix is taller than wide, so it stands taller than the others to look as large
const ICON_HEIGHT: Record<ResourceName, number> = { fuel: 28, food: 28, morale: 42, population: 28 };
const ICON_GAP = 8;
const PIPS = 15;
const PIP_WIDTH = 6;
const PIP_GAP = 3;
const FIVES_GAP = 4;

/**
 * A resource as its icon and a pip after it for each of its amount, the red zone's pips red; they glow red while the
 * amount is in it. Hovering anywhere on its box shows its name and amount.
 */
function Gauge({ resource, value, box }: { resource: ResourceName; value: number | null; box: Box }) {
  const label = resourceLabel(resource);
  const left = box.x + ICON_WIDTH + ICON_GAP;
  return (
    <g className={inRedZone(resource, value) ? 'gauge red-zone' : 'gauge'} data-gauge={resource} role="img" aria-label={`${label}: ${value ?? 'unknown'}`}>
      {/* takes the hover over the gaps too */}
      <rect className="gauge-hit" x={box.x - 6} y={box.y - 6} width={box.width + 12} height={box.height + 12} />
      <image
        href={resourceIconUrl(resource)}
        x={box.x}
        y={box.y + (box.height - ICON_HEIGHT[resource]) / 2}
        width={ICON_WIDTH}
        height={ICON_HEIGHT[resource]}
        preserveAspectRatio="xMaxYMid meet"
      />
      {/* only the pips up to the amount: the box keeps its place and the hover */}
      <g className="pips">
        {Array.from({ length: Math.min(Math.max(value ?? 0, 0), PIPS) }, (_, index) => {
          const pip = index + 1;
          return (
            <rect
              key={pip}
              className={inRedZone(resource, pip) ? 'pip red' : 'pip'}
              x={left + index * (PIP_WIDTH + PIP_GAP) + Math.floor(index / 5) * FIVES_GAP}
              y={box.y}
              width={PIP_WIDTH}
              height={box.height}
            />
          );
        })}
      </g>
      <Count x={box.x + box.width / 2} y={box.y - 50} value={`${label} ${value ?? '—'}`} size={26} align="middle" className="resource-tip" />
    </g>
  );
}

function Tracks({ game }: { game: GameProjection }) {
  const jump = game.galactica?.jumpPreparation;
  const pursuit = game.cylonFleet?.pursuit;
  // the centurions of a space stand side by side
  const boarding = new Map<string, number>();
  for (const centurion of game.galactica?.boardingParty ?? []) {
    if (centurion.position) {
      boarding.set(centurion.position, (boarding.get(centurion.position) ?? 0) + 1);
    }
  }
  const centurion = tokenArt('CENTURION');
  const panel = SLOTS.boardingParty;
  return (
    <g className="tracks">
      {jump && JUMP_TRACK[jump] && <Marker track="JumpPreparation" position={jump} at={JUMP_TRACK[jump]} size={88} label={constantLabel('JumpPreparation', jump)} />}
      {pursuit && PURSUIT_TRACK[pursuit] && <Marker track="Pursuit" position={pursuit} at={PURSUIT_TRACK[pursuit]} size={88} label={constantLabel('Pursuit', pursuit)} />}
      {/* the track isn't printed: it lies over the hull only while a centurion is on it */}
      {boarding.size > 0 && (
        <g className="boarding" data-track="BoardingParty">
          <image href={boardingPartyUrl} x={panel.x} y={panel.y} width={panel.width} height={panel.height} />
          {[...boarding.entries()].map(([position, count]) => {
            const at = BOARDING_TRACK[position];
            return at ? (
              <g key={position} className="centurions" data-position={position} aria-label={`${count} centurion${count > 1 ? 's' : ''} at ${constantLabel('BoardingParty', position)}`}>
                {centurion ? (
                  Array.from({ length: Math.min(count, 3) }, (_, index) => (
                    <SvgArt key={index} art={centurion} x={at.x - 32 + index * 8} y={at.y - 50 - index * 10} width={64} height={99} preview={false} className="centurion" />
                  ))
                ) : (
                  <circle className="centurion" cx={at.x} cy={at.y} r={30} />
                )}
                {count > 1 && <Count x={at.x + 36} y={at.y - 86} value={`×${count}`} size={24} align="end" />}
              </g>
            ) : null;
          })}
        </g>
      )}
    </g>
  );
}

// the printed spaces of the Jump Preparation and Pursuit tracks: 79 pixels, their corners cut by 12
const TRACK_SPACE = 79;
const TRACK_SPACE_CUT = 12;

// the printed locations' corners are cut by 15, and the rings around the ships by about as much as the table's buttons
// at a usual height
const LOCATION_CUT = 15;
const RING_CUT = 12;

// the portraits' top corners are cut by 14 of their 87 pixels, their bottom is square. The ring around a standee lies
// this far out, the middle of its stroke, which is twice as wide (styles.css)
const PORTRAIT_CUT = 14 / 87;
const TOKEN_RING_OUT = 2.5;

/** The points of a box with its top corners cut at 45 degrees and its bottom square, like the portraits. */
function topCutBox(left: number, top: number, width: number, height: number, cut: number): string {
  const [right, bottom] = [left + width, top + height];
  return [
    [left + cut, top],
    [right - cut, top],
    [right, top + cut],
    [right, bottom],
    [left, bottom],
    [left, top + cut],
  ]
    .map(([x, y]) => `${x},${y}`)
    .join(' ');
}

/** An outline parallel to a box with corners cut by `cut`, moved out by `out`: its cut grows by out times (2 - √2). */
function outsideCut(cut: number, out: number): number {
  return cut + out * (2 - Math.SQRT2);
}

/** The points of a box with its corners cut at 45 degrees, like the printed spaces and the table's buttons. */
function cutBox(left: number, top: number, width: number, height: number, cut: number): string {
  const [right, bottom] = [left + width, top + height];
  return [
    [left + cut, top],
    [right - cut, top],
    [right, top + cut],
    [right, bottom - cut],
    [right - cut, bottom],
    [left + cut, bottom],
    [left, bottom - cut],
    [left, top + cut],
  ]
    .map(([x, y]) => `${x},${y}`)
    .join(' ');
}

/** A highlight around a track's space, its corners cut as the space's, at the same 45 degrees. */
function Marker({ track, position, at, size, label }: { track: string; position: string; at: Point; size: number; label: string }) {
  const cut = outsideCut(TRACK_SPACE_CUT, (size - TRACK_SPACE) / 2);
  return (
    <g className={`marker marker-${track.toLowerCase()}`} data-track={track} data-position={position}>
      <title>{label}</title>
      <polygon points={cutBox(at.x - size / 2, at.y - size / 2, size, size, cut)} />
    </g>
  );
}

// ---------------------------------------------------------------- the reserves and the Cylon supply

// The supply in the Cylon reserves, a pile per kind side by side: where the pile lies and the size of its pieces.
const SUPPLY: { type: string; x: number; size: [number, number] }[] = [
  { type: 'BASESTAR', x: 1356, size: [110, 58] },
  { type: 'RAIDER', x: 1492, size: [64, 42] },
  { type: 'HEAVY_RAIDER', x: 1602, size: [86, 27] },
  { type: 'CENTURION', x: 1730, size: [32, 50] },
];

function Reserves({ game }: { game: GameProjection }) {
  const supply = game.cylonSupply;
  const counts: Record<string, number> = {
    BASESTAR: supply?.basestars ?? 0,
    RAIDER: supply?.raiders ?? 0,
    HEAVY_RAIDER: supply?.heavyRaiders ?? 0,
    CENTURION: supply?.centurions ?? 0,
  };
  const cylon = SLOTS.cylonReserves;
  const reserves = inset(SLOTS.shipReserves, 14, 24);
  // the civilian ships lie face down at the right end of the ship reserves
  const civilians = { ...reserves, x: reserves.x + reserves.width - CIVILIAN_PILE, width: CIVILIAN_PILE };
  return (
    <g className="reserves">
      <g data-slot="reserves">
        <ShipPiles box={{ ...reserves, width: reserves.width - CIVILIAN_PILE - 18 }} ships={game.galactica?.reserves ?? []} />
        <DeckPile deck={game.decks?.find((deck) => deck.deck === 'CIVILIAN_SHIPS')} slot={civilians} back={shipArt('CIVILIAN')} ships />
      </g>
      <g data-slot="damaged-vipers">
        <ShipPiles box={inset(SLOTS.damagedVipers, 14, 24)} ships={game.galactica?.damagedVipers ?? []} />
      </g>
      <g data-slot="cylon-supply">
        {SUPPLY.map(({ type, x, size }) => (
          <CountedPile key={type} type={type} count={counts[type]} x={x} y={cylon.y + (cylon.height - size[1]) / 2 + 6} size={size} />
        ))}
      </g>
    </g>
  );
}

function inset(box: Box, dx: number, dy: number): Box {
  return { x: box.x + dx, y: box.y + dy, width: box.width - 2 * dx, height: box.height - dy };
}

// ---------------------------------------------------------------- the deck rack, the objective, the tokens

function DeckRack({ game }: { game: GameProjection }) {
  const deck = (id: string) => game.decks?.find((candidate) => candidate.deck === id);
  const rack = Object.entries(DECK_SLOTS).filter(([id]) => id !== 'MOTIVE' || (deck(id)?.size ?? 0) > 0);
  return (
    <g className="deck-rack">
      {rack.map(([id, slot]) => (
        <DeckPile key={id} deck={deck(id)} slot={slot} back={deckBackArt(id) ?? skillBackArt()} extra={id === 'LOYALTY' ? deck('LOYALTY_NOT_CYLON') : undefined} />
      ))}
      {/* each face-up discard pile lies over its deck, clear of the next one */}
      {rack
        .filter(([id]) => !FACEDOWN_DISCARDS.has(id))
        .map(([id, slot]) => (
          <Discard key={id} deck={deck(id)} slot={{ ...slot, x: slot.x + slot.width * 0.3, y: slot.y + 6 }} />
        ))}
    </g>
  );
}

/** The objective, and the destinations reached tucked under it with their distance showing, first first. */
function Objective({ game }: { game: GameProjection }) {
  const slot = SLOTS.objective;
  const objective = cardArt('ObjectiveCard', game.objective);
  const destinations = game.destinations ?? [];
  // the strips close up when more cards are reached than the printed space holds
  const strip = destinations.length > DISTANCE.count ? (DISTANCE.strip * DISTANCE.count) / destinations.length : DISTANCE.strip;
  return (
    <g className="objective">
      <RevealGroup id="distance" title="Destinations reached" sections={[{ title: 'Reached, first first', cards: destinations.map((card) => ({ art: cardArt('DestinationCard', card), label: constantLabel('DestinationCard', card) })) }]}>
        <g data-slot="distance">
          <rect className="hotspot" x={DISTANCE.x} y={DISTANCE.y} width={DISTANCE.width} height={Math.max(strip * destinations.length, DISTANCE.strip)} />
          {/* the later cards lie under the earlier ones, so each shows its own strip */}
          {destinations
            .map((card, index) => ({ card, index, art: cardArt('DestinationCard', card) }))
            .reverse()
            .map(({ card, index, art }) =>
              art ? (
                <SvgArt key={`${card}-${index}`} art={art} x={DISTANCE.x} y={DISTANCE.y + (index + 1) * strip - slot.height} width={DISTANCE.width} height={slot.height} className="card" preview={false} />
              ) : null,
            )}
          {objective && <SvgArt art={objective} x={slot.x} y={slot.y} width={slot.width} height={slot.height} className="card" />}
          <Count x={DISTANCE.x + DISTANCE.width + 8} y={DISTANCE.y + strip * destinations.length + 6} value={game.distanceTraveled} size={30} align="end" accent />
        </g>
      </RevealGroup>
    </g>
  );
}

function pileCards(cards: unknown[], kind: string | null): RevealCard[] {
  return cards.map((card) => ({
    art: deckCardArt(card, kind),
    // a civilian ship with what it costs when destroyed
    label: kind === 'CivilianShip' ? `${cardLabel(card)}: ${cargoText(card as CivilianShip)}` : cardLabel(card),
  }));
}

// The draw pile top first, which only a sandbox sees, and the discard pile top first, face up or not.
export function deckSections(deck: DeckProjection | undefined): RevealSection[] {
  if (!deck) {
    return [];
  }
  const kind = deckCards(deck.deck);
  const name = constantLabel('DeckId', deck.deck);
  return [
    { title: `${name}: draw pile, top first`, cards: pileCards(deck.drawPile ?? [], kind), hidden: true },
    { title: `${name}: discard pile, top first`, cards: pileCards([...(deck.discarded ?? [])].reverse(), kind), hidden: FACEDOWN_DISCARDS.has(deck.deck ?? '') },
  ];
}

/** A deck in its slot: its back (a card, or a pile of ships like the reserves'), its count, and its piles on hover. */
function DeckPile({ deck, slot, back, ships = false, extra }: {
  deck: DeckProjection | undefined;
  slot: Box;
  back: ArtRef | null;
  ships?: boolean;
  extra?: DeckProjection | undefined;
}) {
  if (!deck) {
    return null;
  }
  const sections = [...deckSections(deck), ...deckSections(extra)];
  const count = deck.size + (extra?.size ?? 0);
  if (ships) {
    const [width, height] = SHIP_SIZE.CIVILIAN.map((side) => side * RESERVE_SCALE);
    const shown = Math.min(count, CIVILIAN_SHOWN);
    const step = PILE_STEP * RESERVE_SCALE;
    return (
      <RevealGroup id={`deck-${deck.deck}`} title={constantLabel('DeckId', deck.deck)} sections={sections}>
        <g className="deck-slot" data-deck={deck.deck} data-pile="draw">
          <rect className="hotspot" x={slot.x} y={slot.y - 10} width={slot.width} height={slot.height} />
          {back &&
            Array.from({ length: shown }, (_, index) => (
              <SvgArt key={index} art={back} x={slot.x + index * step} y={slot.y + (shown - 1 - index) * 6} width={width} height={height} className="piece" preview={false} />
            ))}
          <Count x={slot.x + width + Math.max(shown - 1, 0) * step + 8} y={slot.y - 18} value={count} size={24} align="end" />
        </g>
      </RevealGroup>
    );
  }
  return (
    <RevealGroup id={`deck-${deck.deck}`} title={constantLabel('DeckId', deck.deck)} sections={sections}>
      <g className="deck-slot" data-deck={deck.deck} data-pile="draw">
        <rect className="hotspot" x={slot.x} y={slot.y} width={slot.width} height={slot.height} />
        {back && count > 0 && <SvgArt art={back} x={slot.x + 3} y={slot.y + 3} width={slot.width - 6} height={slot.height - 6} className="card" preview={false} />}
        <Count x={slot.x - 8} y={slot.y + slot.height - 34} value={count} size={26} />
      </g>
    </RevealGroup>
  );
}

/** A discard pile: its top card face up, the pile on hover. */
function Discard({ deck, slot }: { deck: DeckProjection | undefined; slot: Box }) {
  if (!deck) {
    return null;
  }
  const top = deck.discarded?.[deck.discarded.length - 1];
  const art = top !== undefined ? deckCardArt(top, deckCards(deck.deck)) : null;
  if (!art) {
    return null;
  }
  return (
    <RevealGroup id={`discard-${deck.deck}`} title={`${constantLabel('DeckId', deck.deck)}: discard pile`} sections={deckSections(deck).slice(1)}>
      <g className="deck-slot" data-deck={deck.deck} data-pile="discard">
        <SvgArt art={art} x={slot.x} y={slot.y} width={slot.width} height={slot.height} className="card" preview={false} />
        {/* its count at the top, clear of the next deck's count at the bottom */}
        {deck.discardSize > 1 && <Count x={slot.x + slot.width + 6} y={slot.y - 10} value={deck.discardSize} size={22} align="end" />}
      </g>
    </RevealGroup>
  );
}

// ---------------------------------------------------------------- the free starfield

/** The Crisis Cards kept in play, by the jump track, the later ones on top. */
function FreeSpace({ game }: { game: GameProjection }) {
  const slot = SLOTS.inPlay;
  return (
    <g className="free-space">
      <g className="in-play" data-slot="in-play">
        {(game.crisisCardsInPlay ?? []).map((type, index) => {
          const art = skillCheckArt(type);
          return art ? <SvgArt key={`${type}-${index}`} art={art} x={slot.x + index * 30} y={slot.y} width={slot.width} height={slot.height} className="card kept" /> : null;
        })}
      </g>
    </g>
  );
}

// ---------------------------------------------------------------- the locations

// A destroyed Colonial One or Pegasus gets a veil, and the Engine Room a label once activated.
function LocationMarks({ place, destroyed, engineRoom }: { place: Place; destroyed: boolean; engineRoom: boolean }) {
  return (
    <>
      {destroyed && <rect className="destroyed-veil" x={place.x} y={place.y} width={place.width} height={place.height} rx={10} />}
      {engineRoom && <Count x={place.x + 10} y={place.y + 20} value="activated" size={26} accent />}
    </>
  );
}

// A damaged location's label, like the Engine Room's "activated": at the bottom of the panel its name is printed in,
// under the name (two lines of it leave room for no more than this size) and over the standees' feet.
const DAMAGED_SIZE = 24;

function DamagedLabel({ place, title }: { place: Place; title: Box | undefined }) {
  const bottom = title ? title.y + title.height - 2 : place.y + place.height - 10;
  return <Count x={place.x + place.width / 2} y={bottom - DAMAGED_SIZE * 1.44} value="damaged" size={DAMAGED_SIZE} align="middle" className="damaged-label" />;
}

// The standees stand on the printed name of their location, a little taller than its panel, so they reach up into the
// picture, side by side while they fit and overlapping when they don't. In a space area, where nothing is printed,
// they stand at the bottom. Hovering one shows the character sheet with the player's mat beside it. The acting
// player's standee has a red ring.
function Standees({ place, title, characters, acting, nameOf, game }: {
  place: Place;
  title: Box | undefined;
  characters: Character[];
  acting: Character | null;
  nameOf(playerNumber: number): string;
  game: GameProjection;
}) {
  const table = useTable();
  const { offer, onTarget } = useAnswering();
  const flashed = useFlash((state) => state.keys);
  if (characters.length === 0) {
    return null;
  }
  const height = title ? title.height + 12 : 109;
  const width = (height * 74) / 109;
  const step = characters.length <= 1 ? 0 : Math.min(width + 4, (place.width - 8 - width) / (characters.length - 1));
  const left = place.x + (place.width - (width + step * (characters.length - 1))) / 2;
  const top = title ? title.y + title.height - height - 2 : place.y + place.height - height - 8;
  return (
    <>
      {characters.map((character, index) => {
        const art = characterArt(character);
        const player = game.players?.find((candidate) => candidate.character === character);
        const name = player ? nameOf(player.number) : constantLabel('Character', character);
        const x = left + index * step;
        const key = player ? `player:${player.number}` : '';
        const lit = player ? offer.targets.get(key) : undefined;
        const classes = ['character', character === acting ? 'acting' : '', lit ? 'target' : '', offer.picked.has(key) ? 'picked' : '', flashed.has(key) ? 'flash' : '']
          .join(' ')
          .trim();
        return (
          <g
            key={character}
            className={classes}
            data-character={character}
            {...(lit && player ? { ...targetHandlers({ type: 'player', playerNumber: player.number }, onTarget), 'aria-label': name } : {})}
          >
            {lit && <title>{hint(lit, table)}</title>}
            {art ? (
              <>
                <polygon
                  className="token-ring"
                  points={topCutBox(x - TOKEN_RING_OUT, top - TOKEN_RING_OUT, width + 2 * TOKEN_RING_OUT, height + 2 * TOKEN_RING_OUT, outsideCut(width * PORTRAIT_CUT, TOKEN_RING_OUT))}
                />
                <SvgArt art={art} x={x} y={top} width={width} height={height} title={name} previewExtra={player ? <PlayerMatOf playerNumber={player.number} /> : undefined} />
              </>
            ) : (
              <>
                <rect x={place.x + 6} y={top + index * 40} width={place.width - 12} height={34} rx={14} />
                <text x={place.x + place.width / 2} y={top + 24 + index * 40} textAnchor="middle">
                  {shortName(constantLabel('Character', character))}
                </text>
              </>
            )}
          </g>
        );
      })}
    </>
  );
}

// ---------------------------------------------------------------- the ships

// The size of each ship's image on the board, in the proportions of the scans, and the order the ships of a space area
// lie in: the Cylon ships first.
const SHIP_SIZE: Record<string, [number, number]> = {
  BASESTAR: [214, 112],
  HEAVY_RAIDER: [122, 38],
  RAIDER: [82, 54],
  VIPER: [104, 52],
  VIPER_MARK_VII: [104, 58],
  RAPTOR: [88, 54],
  ASSAULT_RAPTOR: [88, 64],
  CIVILIAN: [104, 52],
  CENTURION: [44, 68],
};
const SHIP_ORDER = Object.keys(SHIP_SIZE);
const PILE_STEP = 12;
const PILE_MAX = 4;
// A ship is as large in every space area of a board, alone or piled: a basestar just fits the strips at Galactica's bow
// and stern, and the areas around the Cylon Fleet board's basestar are smaller.
const SPACE_SCALE: Record<'galacticaSpace' | 'cylonFleetSpace', number> = { galacticaSpace: 0.8, cylonFleetSpace: 0.5 };
// the reserves are a narrow strip: smaller ships
const RESERVE_SCALE = 0.7;
// the civilian ships' pile at the end of the reserves: as many as it shows, and its width
const CIVILIAN_SHOWN = 3;
const CIVILIAN_PILE = (104 + (CIVILIAN_SHOWN - 1) * PILE_STEP) * RESERVE_SCALE;

interface ShipGroup {
  type: string;
  ships: ShipProjection[];
}

/** A pilot or damage sets a ship apart. The others pile up by type. */
export function groupShips(ships: ShipProjection[]): ShipGroup[] {
  const groups: ShipGroup[] = [];
  const piles = new Map<string, ShipGroup>();
  const sorted = [...ships].sort((a, b) => SHIP_ORDER.indexOf(a.type ?? '') - SHIP_ORDER.indexOf(b.type ?? '') || a.id - b.id);
  for (const ship of sorted) {
    const type = ship.type ?? '';
    if (ship.pilot || (ship.damage ?? []).length > 0) {
      groups.push({ type, ships: [ship] });
      continue;
    }
    let pile = piles.get(type);
    if (!pile) {
      pile = { type, ships: [] };
      piles.set(type, pile);
      groups.push(pile);
    }
    pile.ships.push(ship);
  }
  return groups;
}

interface PlacedGroup {
  group: ShipGroup;
  x: number;
  y: number;
  width: number;
  height: number;
  step: number;
}

interface SizedPile {
  group: ShipGroup;
  width: number;
  height: number;
  // the room the pile takes: the ships stepping up to the right, a pilot's token above, damage tokens below
  outerWidth: number;
  outerHeight: number;
  pilot: number;
}

/**
 * The piles in rows, as many as fit the box's width, in the order they come. Centred, the rows lie in the middle of the
 * box and each pile in the middle of its row; otherwise they start at its top left.
 */
export function layOut(box: Box, groups: ShipGroup[], { scale = 1, centred = false }: { scale?: number; centred?: boolean } = {}): PlacedGroup[] {
  const step = PILE_STEP * scale;
  const gap = 18 * scale;
  const rowGap = 14 * scale;
  const piles: SizedPile[] = groups.map((group) => {
    const [width, height] = (SHIP_SIZE[group.type] ?? [90, 50]).map((side) => side * scale);
    const shown = Math.min(group.ships.length, PILE_MAX);
    const pilot = group.ships[0].pilot ? 30 : 0;
    const damage = (group.ships[0].damage ?? []).length > 0 ? 44 : 0;
    return { group, width, height, pilot, outerWidth: width + (shown - 1) * step, outerHeight: pilot + height + (shown - 1) * 6 + damage };
  });
  const rows: SizedPile[][] = [];
  for (const pile of piles) {
    const row = rows[rows.length - 1];
    const rowWidth = row ? row.reduce((sum, other) => sum + other.outerWidth + gap, 0) : 0;
    if (row && rowWidth + pile.outerWidth <= box.width) {
      row.push(pile);
    } else {
      rows.push([pile]);
    }
  }
  const heights = rows.map((row) => Math.max(...row.map((pile) => pile.outerHeight)));
  const total = heights.reduce((sum, height) => sum + height, 0) + rowGap * Math.max(rows.length - 1, 0);
  const placed: PlacedGroup[] = [];
  let y = centred ? box.y + (box.height - total) / 2 : box.y;
  rows.forEach((row, index) => {
    const width = row.reduce((sum, pile) => sum + pile.outerWidth, 0) + gap * (row.length - 1);
    let x = centred ? box.x + (box.width - width) / 2 : box.x;
    for (const pile of row) {
      const top = centred ? y + (heights[index] - pile.outerHeight) / 2 : y;
      placed.push({ group: pile.group, x, y: top + pile.pilot, width: pile.width, height: pile.height, step });
      x += pile.outerWidth + gap;
    }
    y += heights[index] + rowGap;
  });
  return placed;
}

// The part of a space area beside its printed launch tube, on the side with more room.
function besideTube(area: Box, tube: Box | undefined): Box {
  if (!tube) {
    return area;
  }
  const margin = 12;
  const left = tube.x - margin - area.x;
  const right = area.x + area.width - (tube.x + tube.width + margin);
  return left >= right ? { ...area, width: left } : { ...area, x: tube.x + tube.width + margin, width: right };
}

function Ships({ place, ships, toMiss, avoid }: { place: Place; ships: ShipProjection[]; toMiss: number; avoid?: Box }) {
  const scale = place.area === 'cylonFleetSpace' ? SPACE_SCALE.cylonFleetSpace : SPACE_SCALE.galacticaSpace;
  return (
    <g className="ships">
      {toMiss > 0 && <Count x={place.x + place.width - 10} y={place.y} value={`${toMiss} will miss`} size={26} align="end" accent />}
      <Piles placed={layOut(besideTube(place, avoid), groupShips(ships), { scale, centred: true })} />
    </g>
  );
}

function ShipPiles({ box, ships }: { box: Box; ships: ShipProjection[] }) {
  return <Piles placed={layOut(box, groupShips(ships), { scale: RESERVE_SCALE })} />;
}

// Each pile with its count on it. Hovering civilian ships shows what each costs when destroyed, to whoever knows it:
// the players who looked at it, and a sandbox.
function Piles({ placed }: { placed: PlacedGroup[] }) {
  const table = useTable();
  return (
    <>
      {placed.map(({ group, x, y, width, height, step }) => {
        const shown = Math.min(group.ships.length, PILE_MAX);
        const right = x + width + (shown - 1) * step;
        const known = group.ships.filter((ship) => ship.cargo);
        const cargo: RevealSection[] =
          known.length === 0
            ? []
            : [{ title: 'Lost if destroyed', cards: known.map((ship) => ({ art: null, label: `Civilian ship #${ship.id}`, icon: <CargoIcons cargo={ship.cargo!} /> })), hidden: table.sandbox }];
        return (
          <RevealGroup key={group.ships[0].id} id={`cargo-${group.ships[0].id}`} title={group.ships.length > 1 ? 'Civilian ships' : 'Civilian ship'} sections={cargo}>
            <g className={`ship-group ship-${group.type.toLowerCase()}`}>
              {group.ships.map((ship, index) => {
                const at = Math.min(index, PILE_MAX - 1);
                return <Ship key={ship.id} ship={ship} x={x + at * step} y={y + (shown - 1 - at) * 6} width={width} height={height} preview={cargo.length === 0} />;
              })}
              {group.ships.length > 1 && <Count x={right + 8} y={y - 18} value={`×${group.ships.length}`} size={24} align="end" />}
            </g>
          </RevealGroup>
        );
      })}
    </>
  );
}

// A ship, with its image large on hover unless its pile shows more.
function Ship({ ship, x, y, width, height, preview = true }: { ship: ShipProjection; x: number; y: number; width: number; height: number; preview?: boolean }) {
  const table = useTable();
  const { offer, onTarget } = useAnswering();
  const art = shipArt(ship.type);
  const label = boardShipLabel(ship);
  const pilot = ship.pilot ? pilotArt(ship.pilot) : null;
  const flashed = useFlash((state) => state.keys);
  const key = `ship:${ship.id}`;
  const lit = offer.targets.get(key);
  const classes = ['ship', `ship-${(ship.type ?? '').toLowerCase()}`, lit ? 'target' : '', offer.picked.has(key) ? 'picked' : '', flashed.has(key) ? 'flash' : ''].join(' ').trim();
  return (
    <g className={classes} data-ship={ship.id} aria-label={label} {...(lit ? targetHandlers({ type: 'ship', id: ship.id }, onTarget) : {})}>
      {lit && <title>{hint(lit, table)}</title>}
      {(lit || flashed.has(key)) && <polygon className="target-ring" points={cutBox(x - 4, y - 4, width + 8, height + 8, RING_CUT)} />}
      {art ? (
        <SvgArt art={art} x={x} y={y} width={width} height={height} title={label} className="piece" preview={preview} />
      ) : (
        <text x={x} y={y + 30}>
          {label}
        </text>
      )}
      {pilot && <SvgArt art={pilot} x={x + width - 30} y={y - 30} width={50} height={50} className="pilot-token" title={label} />}
      {(ship.damage ?? []).map((damage, index) => {
        const token = damageArt('BasestarDamage', damage);
        return token ? <SvgArt key={index} art={token} x={x + 8 + index * 46} y={y + height + 2} width={42} height={42} className="piece" /> : null;
      })}
    </g>
  );
}

/** Ships counted in a pile: the supply, where only the numbers are known. */
function CountedPile({ type, count, x, y, size }: { type: string; count: number; x: number; y: number; size: [number, number] }) {
  const art = type === 'CENTURION' ? tokenArt('CENTURION') : shipArt(type);
  const [width, height] = size;
  const shown = Math.min(count, PILE_MAX);
  const step = 8;
  return (
    <g className={`supply supply-${type.toLowerCase()}`} data-supply={type} aria-label={`${count} ${constantLabel('ShipType', type)}`}>
      {art &&
        Array.from({ length: shown }, (_, index) => (
          <SvgArt key={index} art={art} x={x + index * step} y={y + (shown - 1 - index) * 5} width={width} height={height} className="piece" preview={false} />
        ))}
      <Count x={x + width + Math.max(shown - 1, 0) * step + 8} y={y - 18} value={`×${count}`} size={24} align="end" />
    </g>
  );
}

export function boardShipLabel(ship: ShipProjection): string {
  return [
    `${constantLabel('ShipType', ship.type)} #${ship.id}`,
    ship.pilot ? shortName(constantLabel('Character', ship.pilot)) : null,
    (ship.damage ?? []).length > 0 ? (ship.damage ?? []).map((damage) => constantLabel('BasestarDamage', damage)).join(', ') : null,
  ]
    .filter((part) => part !== null)
    .join(' · ');
}

// ---------------------------------------------------------------- small parts

/**
 * A number on the board in a chip with cut corners: a count, a difficulty, a value. `y` is its top, and `x` its left
 * edge, its middle or its right edge.
 */
function Count({ x, y, value, size = 36, align = 'start', accent = false, className = '' }: {
  x: number;
  y: number;
  value: number | string;
  size?: number;
  align?: 'start' | 'middle' | 'end';
  accent?: boolean;
  className?: string;
}) {
  const text = String(value);
  const width = size * (0.72 + text.length * 0.56);
  const height = size * 1.44;
  const cut = size * 0.28;
  const left = align === 'start' ? x : align === 'middle' ? x - width / 2 : x - width;
  const points = [
    [left + cut, y],
    [left + width, y],
    [left + width, y + height - cut],
    [left + width - cut, y + height],
    [left, y + height],
    [left, y + cut],
  ]
    .map((point) => point.join(','))
    .join(' ');
  return (
    <g className={['count', accent ? 'accent' : '', className].join(' ').trim()}>
      <polygon points={points} />
      <text x={left + width / 2} y={y + height / 2 + size * 0.36} textAnchor="middle" fontSize={size}>
        {text}
      </text>
    </g>
  );
}

// A group of the board that shows what it hides on hover.
function RevealGroup({ id, title, sections, children }: { id: string; title: string; sections: RevealSection[]; children: ReactNode }) {
  const handlers = useRevealHandlers(id, title, sections);
  return <g {...handlers}>{children}</g>;
}

// A nickname in quotes, otherwise the last name: what fits on a token.
export function shortName(label: string): string {
  const nickname = /"([^"]+)"/.exec(label);
  if (nickname) {
    return nickname[1];
  }
  const words = label.replace(/\(.*\)/, '').trim().split(' ');
  return words[words.length - 1];
}

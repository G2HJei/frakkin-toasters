import type { ReactNode } from 'react';
import {
  cardArt,
  characterArt,
  damageArt,
  deckBackArt,
  deckCardArt,
  deckCards,
  dieArt,
  pilotArt,
  shipArt,
  skillBackArt,
  skillCardArt,
  skillCheckArt,
  tokenArt,
} from '../../art/images';
import { SvgArt } from '../../art/Art';
import { cardLabel, constantLabel, skillCardLabel } from '../../labels/describe';
import type { Character, DeckProjection, GameProjection, Location, LocationProjection, ShipProjection } from '../../wire/types';
import { useRevealHandlers, type RevealCard, type RevealSection } from '../reveal';
import { useTable } from '../tableContext';
import boardUrl from './board.jpg';
import { BOARD_HEIGHT, BOARD_WIDTH, BOARDING_TRACK, DIALS, JUMP_TRACK, LAYOUT, PURSUIT_TRACK, SKILL_SLOTS, SLOTS, type Box, type Place } from './layout';

export interface BoardSvgProps {
  // the locations a pending input can be answered with, which light up and take a click
  targets?: ReadonlySet<string>;
  onLocation?(location: Location): void;
  // the location whose action the asked player can take, with an "Act" button on it
  actAt?: string | null;
  onAct?(): void;
}

// the decks whose discard pile lies face down (Disclosure.FACEDOWN_DISCARDS in the core)
const FACEDOWN_DISCARDS = new Set(['CIVILIAN_SHIPS', 'GALACTICA_DAMAGE', 'PEGASUS_DAMAGE', 'BASESTAR_DAMAGE', 'DESTINATION', 'DESTINY', 'LOYALTY', 'LOYALTY_NOT_CYLON', 'MOTIVE']);

/**
 * The board as it lies on the table: board.jpg, with every component on its printed slot. The characters stand on their
 * locations, the ships fly in the space areas, the dials turn to the resources, the markers sit on the tracks and the
 * decks lie in the deck row with their counts and the top of their discard piles. What has no printed slot (the turn's
 * Crisis Cards, the die, the Quorum and Motive decks, the objective, the nukes) lies on free space. Hovering a deck shows
 * its discard pile, and in a sandbox what the players can't see.
 */
export function BoardSvg({ targets, onLocation, actAt, onAct }: BoardSvgProps) {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const galactica = game.galactica;
  const locations = [...(galactica?.locations ?? []), ...(game.pegasus?.locations ?? []), ...(game.cylonFleet?.locations ?? [])];
  const ships = [...(galactica?.ships ?? []), ...(game.cylonFleet?.ships ?? [])];
  const currentCharacter = game.players?.find((player) => player.number === game.turn?.currentPlayer)?.character ?? null;
  const cylons = new Set((game.players ?? []).filter((player) => player.revealedCylon).map((player) => player.character));

  return (
    <svg className="board-svg" viewBox={`0 0 ${BOARD_WIDTH} ${BOARD_HEIGHT}`} role="img" aria-label="the board">
      <image href={boardUrl} x={0} y={0} width={BOARD_WIDTH} height={BOARD_HEIGHT} className="board-image" />
      <Dials game={game} />
      <Tracks game={game} />
      <Reserves game={game} />
      <DeckRow game={game} />
      <TopDecks game={game} />
      <SkillCheckSlot game={game} />
      <FreeSpace game={game} />

      {locations.map((location) => {
        const name = location.location as Location;
        const place = LAYOUT[name];
        if (!place) {
          return null;
        }
        const isTarget = targets?.has(name) ?? false;
        const space = place.area === 'galacticaSpace' || place.area === 'cylonFleetSpace';
        const destroyed = place.area === 'colonialOne' && (galactica?.colonialOneDestroyed ?? false);
        return (
          <g
            key={name}
            className={['location', place.area, location.damaged ? 'damaged' : '', destroyed ? 'destroyed' : '', isTarget ? 'target' : ''].join(' ')}
            data-location={name}
            onClick={isTarget && onLocation ? () => onLocation(name) : undefined}
            role={isTarget ? 'button' : undefined}
            aria-label={isTarget ? constantLabel('Location', name) : undefined}
          >
            <rect className={space ? 'space-frame' : 'frame'} x={place.x - 6} y={place.y - 6} width={place.width + 12} height={place.height + 12} rx={14} />
            {space ? (
              <Ships place={place} ships={ships.filter((ship) => ship.location === name)} toMiss={game.turn?.raidersToMiss?.[name] ?? 0} />
            ) : (
              <LocationMarks place={place} location={location} destroyed={destroyed} engineRoom={name === 'ENGINE_ROOM' && (game.pegasus?.engineRoomActivated ?? false)} />
            )}
            <Standees place={place} characters={(location.characters ?? []) as Character[]} current={currentCharacter} cylons={cylons} nameOf={table.playerName} game={game} />
            {actAt === name && onAct && (
              <g className="act" role="button" aria-label="Act" onClick={onAct}>
                <rect x={place.x + place.width - 96} y={place.y - 22} width={110} height={52} rx={6} />
                <text x={place.x + place.width - 41} y={place.y + 14} textAnchor="middle">
                  Act
                </text>
              </g>
            )}
          </g>
        );
      })}
    </svg>
  );
}

// ---------------------------------------------------------------- the dials and the tracks

// Each dial is the printed wheel, turned until the value sits under the pointer: 16 values, 22.5° apart.
function Dials({ game }: { game: GameProjection }) {
  const galactica = game.galactica;
  return (
    <g className="dials">
      {(Object.keys(DIALS) as (keyof typeof DIALS)[]).map((resource) => {
        const dial = DIALS[resource];
        const value = galactica?.[resource] ?? null;
        const angle = value === null ? 0 : (dial.printed - value) * 22.5;
        return (
          <g key={resource} className="dial" data-resource={resource}>
            <clipPath id={`dial-${resource}`}>
              <circle cx={dial.x} cy={dial.y} r={dial.r} />
            </clipPath>
            <g clipPath={`url(#dial-${resource})`}>
              <image href={boardUrl} x={0} y={0} width={BOARD_WIDTH} height={BOARD_HEIGHT} transform={`rotate(${angle} ${dial.x} ${dial.y})`} />
            </g>
            <Count x={dial.x + 66} y={dial.y + 102} value={value ?? '—'} className="dial-chip" />
            <text className="dial-caption" x={dial.x + 66} y={dial.y + 102}>
              <tspan className="dial-value">{value ?? '—'}</tspan>
              <tspan className="dial-name"> {resource}</tspan>
            </text>
          </g>
        );
      })}
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
  return (
    <g className="tracks">
      {jump && JUMP_TRACK[jump] && <Marker track="JumpPreparation" position={jump} at={JUMP_TRACK[jump]} label={constantLabel('JumpPreparation', jump)} />}
      {pursuit && PURSUIT_TRACK[pursuit] && <Marker track="Pursuit" position={pursuit} at={PURSUIT_TRACK[pursuit]} label={constantLabel('Pursuit', pursuit)} />}
      <g className="boarding" data-track="BoardingParty">
        {[...boarding.entries()].map(([position, count]) => {
          const at = BOARDING_TRACK[position];
          return at ? (
            <g key={position} className="centurions" data-position={position} aria-label={`${count} centurion${count > 1 ? 's' : ''} at ${constantLabel('BoardingParty', position)}`}>
              {centurion ? (
                Array.from({ length: Math.min(count, 3) }, (_, index) => (
                  <SvgArt key={index} art={centurion} x={at.x - 22 + index * 10} y={at.y - 40 - index * 6} width={44} height={68} preview={false} className="piece" />
                ))
              ) : (
                <circle cx={at.x} cy={at.y} r={22} />
              )}
              {count > 1 && <Count x={at.x + 22} y={at.y - 50} value={`×${count}`} small />}
            </g>
          ) : null;
        })}
      </g>
    </g>
  );
}

function Marker({ track, position, at, label }: { track: string; position: string; at: { x: number; y: number }; label: string }) {
  return (
    <g className={`marker marker-${track.toLowerCase()}`} data-track={track} data-position={position}>
      <title>{label}</title>
      <rect x={at.x - 36} y={at.y - 36} width={72} height={72} rx={12} />
    </g>
  );
}

// ---------------------------------------------------------------- reserves, supply, the decks on the top half

function Reserves({ game }: { game: GameProjection }) {
  const supply = game.cylonSupply;
  const reserves = SLOTS.shipReserves;
  const damaged = SLOTS.damagedVipers;
  const cylon = SLOTS.cylonReserves;
  return (
    <g className="reserves">
      <g data-slot="reserves">
        <ShipPiles box={reserves} ships={game.galactica?.reserves ?? []} />
      </g>
      <g data-slot="damaged-vipers">
        <ShipPiles box={damaged} ships={game.galactica?.damagedVipers ?? []} />
      </g>
      <g data-slot="cylon-supply">
        <CountedPile type="BASESTAR" count={supply?.basestars ?? 0} x={cylon.x + 20} y={cylon.y + 20} />
        <CountedPile type="RAIDER" count={supply?.raiders ?? 0} x={cylon.x + 20} y={cylon.y + 160} />
        <CountedPile type="HEAVY_RAIDER" count={supply?.heavyRaiders ?? 0} x={cylon.x + 20} y={cylon.y + 250} />
        <CountedPile type="CENTURION" count={supply?.centurions ?? 0} x={cylon.x + 20} y={cylon.y + 320} />
      </g>
    </g>
  );
}

function TopDecks({ game }: { game: GameProjection }) {
  const deck = (id: string) => game.decks?.find((candidate) => candidate.deck === id);
  const destinations = game.destinations ?? [];
  const distance = SLOTS.distanceTraveled;
  return (
    <g className="top-decks">
      <DeckPile deck={deck('DESTINATION')} slot={SLOTS.destinationDeck} back />
      <DeckPile deck={deck('CIVILIAN_SHIPS')} slot={SLOTS.civilianShips} ship="CIVILIAN" />
      <RevealGroup id="distance" title="Destinations reached" sections={[{ title: 'Reached, first first', cards: destinations.map((card) => ({ art: cardArt('DestinationCard', card), label: constantLabel('DestinationCard', card) })) }]}>
        <g data-slot="distance">
          <rect className="hotspot" x={distance.x} y={distance.y} width={distance.width} height={distance.height} />
          {destinations.map((card, index) => {
            const art = cardArt('DestinationCard', card);
            return art ? <SvgArt key={index} art={art} x={distance.x + 10 + index * 8} y={distance.y + 10 + index * 10} width={140} height={200} className="card" preview={false} /> : null;
          })}
          <Count x={distance.x + 112} y={distance.y + 172} value={game.distanceTraveled} accent />
        </g>
      </RevealGroup>
      <DeckPile deck={deck('GALACTICA_DAMAGE')} slot={SLOTS.galacticaDamage} back token />
      <DeckPile deck={deck('PEGASUS_DAMAGE')} slot={SLOTS.pegasusDamage} back token />
      <DeckPile deck={deck('BASESTAR_DAMAGE')} slot={SLOTS.basestarDamage} back token />
      <DeckPile deck={deck('QUORUM')} slot={SLOTS.quorum} back />
      {(deck('MOTIVE')?.size ?? 0) > 0 && <DeckPile deck={deck('MOTIVE')} slot={SLOTS.motive} back />}
    </g>
  );
}

// ---------------------------------------------------------------- the deck row

function DeckRow({ game }: { game: GameProjection }) {
  const deck = (id: string) => game.decks?.find((candidate) => candidate.deck === id);
  return (
    <g className="deck-row">
      <DeckPile deck={deck('CRISIS')} slot={SLOTS.crisisDeck} />
      <Discard deck={deck('CRISIS')} slot={SLOTS.crisisDiscard} />
      <DeckPile deck={deck('SUPER_CRISIS')} slot={SLOTS.superCrisis} />
      <DeckPile deck={deck('MUTINY')} slot={SLOTS.mutiny} />
      <DeckPile deck={deck('LOYALTY')} slot={SLOTS.loyalty} extra={deck('LOYALTY_NOT_CYLON')} />
      <DeckPile deck={deck('DESTINY')} slot={SLOTS.destiny} />
      {Object.entries(SKILL_SLOTS).map(([color, slots]) => (
        <g key={color}>
          <DeckPile deck={deck(color)} slot={slots.deck} skillBack />
          <Discard deck={deck(color)} slot={slots.discard} />
        </g>
      ))}
    </g>
  );
}

function pileCards(cards: unknown[], kind: string | null): RevealCard[] {
  return cards.map((card) => ({ art: deckCardArt(card, kind), label: cardLabel(card) }));
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

/** A deck in its slot: the printed back (or a back, or a pile of ships), its count, and its piles on hover. */
function DeckPile({ deck, slot, back = false, skillBack = false, token = false, ship, extra }: {
  deck: DeckProjection | undefined;
  slot: Box;
  back?: boolean;
  skillBack?: boolean;
  token?: boolean;
  ship?: string;
  extra?: DeckProjection | undefined;
}) {
  if (!deck) {
    return null;
  }
  const backArt = back ? deckBackArt(deck.deck) : skillBack ? skillBackArt() : null;
  const shipImage = ship ? shipArt(ship) : null;
  const sections = [...deckSections(deck), ...deckSections(extra)];
  const count = deck.size + (extra?.size ?? 0);
  return (
    <RevealGroup id={`deck-${deck.deck}`} title={constantLabel('DeckId', deck.deck)} sections={sections}>
      <g className="deck-slot" data-deck={deck.deck} data-pile="draw">
        <rect className="hotspot" x={slot.x} y={slot.y} width={slot.width} height={slot.height} />
        {backArt && deck.size > 0 && (
          token ? (
            <SvgArt art={backArt} x={slot.x + 4} y={slot.y + 4} width={slot.width - 8} height={slot.height - 8} className="piece" preview={false} />
          ) : (
            <SvgArt art={backArt} x={slot.x + 8} y={slot.y + 8} width={slot.width - 16} height={slot.height - 16} className="card" preview={false} />
          )
        )}
        {shipImage &&
          Array.from({ length: Math.min(deck.size, 4) }, (_, index) => (
            <SvgArt key={index} art={shipImage} x={slot.x + 70 + index * 10} y={slot.y + 70 - index * 6} width={118} height={58} className="piece" preview={false} />
          ))}
        <Count x={slot.x + (token ? slot.width - 20 : 22)} y={slot.y + slot.height - (token ? 18 : 62)} value={count} small={token} />
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
  return (
    <RevealGroup id={`discard-${deck.deck}`} title={`${constantLabel('DeckId', deck.deck)}: discard pile`} sections={deckSections(deck).slice(1)}>
      <g className="deck-slot" data-deck={deck.deck} data-pile="discard">
        <rect className="hotspot" x={slot.x} y={slot.y} width={slot.width} height={slot.height} />
        {art && <SvgArt art={art} x={slot.x + 6} y={slot.y + 6} width={slot.width - 12} height={slot.height - 12} className="card" preview={false} />}
        {deck.discardSize > 1 && <Count x={slot.x + 22} y={slot.y + slot.height - 62} value={deck.discardSize} small />}
      </g>
    </RevealGroup>
  );
}

// ---------------------------------------------------------------- the skill check and the free space

/** The skill check in progress, in its printed area: its card, the face-down pile, the difficulty and the result. */
function SkillCheckSlot({ game }: { game: GameProjection }) {
  const check = game.skillCheck;
  if (!check) {
    return null;
  }
  const slot = SLOTS.skillCheck;
  const subject = skillCheckArt(check.type);
  const back = skillBackArt();
  const faceDown = check.cards?.count ?? 0;
  const before = (check.playedBefore ?? []).flatMap((played) => played.cards?.cards ?? []);
  const sections: RevealSection[] = [
    { title: 'Face down', cards: (check.cards?.cards ?? []).map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: true },
    { title: 'Played before the check', cards: before.map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: !check.playedBeforeRevealed },
  ];
  return (
    <g className="skill-check-slot" data-slot="skill-check">
      {subject && <SvgArt art={subject} x={slot.x + 12} y={slot.y + 10} width={150} height={212} className="card in-play" />}
      <RevealGroup id="skill-check-cards" title={`Skill check: ${constantLabel('SkillCheckType', check.type)}`} sections={sections}>
        <g className="face-down" data-pile="skill-check">
          <rect className="hotspot" x={slot.x + 180} y={slot.y + 20} width={220} height={200} />
          {back &&
            Array.from({ length: Math.min(faceDown, 10) }, (_, index) => (
              <SvgArt
                key={index}
                art={back}
                x={slot.x + 190 + index * 20}
                y={slot.y + 48 + (index % 2) * 6}
                width={100}
                height={146}
                className="card"
                preview={false}
                transform={`rotate(${(index - Math.min(faceDown, 10) / 2) * 3} ${slot.x + 240 + index * 20} ${slot.y + 120})`}
              />
            ))}
        </g>
      </RevealGroup>
      <Count x={slot.x + 380} y={slot.y + 150} value={`diff ${check.difficulty}`} accent />
      {check.totalStrength !== null && (
        <Count x={slot.x + 380} y={slot.y + 200} value={`${check.totalStrength}${check.result ? ` ${constantLabel('SkillCheckResult', check.result)}` : ''}`} className={`result ${(check.result ?? '').toLowerCase()}`} />
      )}
    </g>
  );
}

/** What has no printed slot: the turn's Crisis Cards, the ones kept in play, the die, the objective and the nukes. */
function FreeSpace({ game }: { game: GameProjection }) {
  const turn = game.turn;
  const checkCard = game.skillCheck?.type ?? null;
  // the Crisis Card a skill check comes from lies in the skill check area
  const crisis = (turn?.crisisCards ?? []).filter((card) => card !== checkCard);
  const inPlay = game.crisisCardsInPlay ?? [];
  const roll = turn?.dieRoll?.value ?? null;
  const die = dieArt(roll);
  const objective = cardArt('ObjectiveCard', game.objective);
  const nuke = tokenArt('NUKE');
  return (
    <g className="free-space">
      {crisis.map((card, index) => {
        const art = cardArt('CrisisCard', card);
        return art ? <SvgArt key={`${card}-${index}`} art={art} x={SLOTS.turnCrisis.x + index * 165} y={SLOTS.turnCrisis.y} width={150} height={212} className="card in-play" /> : null;
      })}
      {inPlay.map((type, index) => {
        const art = skillCheckArt(type);
        return art ? <SvgArt key={`${type}-${index}`} art={art} x={SLOTS.inPlay.x + index * 40} y={SLOTS.inPlay.y + index * 12} width={150} height={212} className="card kept" /> : null;
      })}
      {die && (
        <g className="die" data-die={roll}>
          <SvgArt art={die} x={SLOTS.die.x} y={SLOTS.die.y} width={SLOTS.die.width} height={SLOTS.die.height} className="piece" />
          {turn?.dieRoll?.result !== null && turn?.dieRoll?.result !== undefined && turn.dieRoll.result !== roll && (
            <Count x={SLOTS.die.x + 100} y={SLOTS.die.y + 70} value={`→ ${turn.dieRoll.result}`} small accent />
          )}
        </g>
      )}
      {objective && <SvgArt art={objective} x={SLOTS.objective.x} y={SLOTS.objective.y} width={SLOTS.objective.width} height={SLOTS.objective.height} className="card" />}
      <g className="nukes" data-nukes={game.nukes}>
        {nuke &&
          Array.from({ length: game.nukes }, (_, index) => (
            <SvgArt key={index} art={nuke} x={SLOTS.nukes.x} y={SLOTS.nukes.y + index * 70} width={100} height={88} className="piece" title={`${game.nukes} nuke${game.nukes > 1 ? 's' : ''}`} />
          ))}
      </g>
    </g>
  );
}

// ---------------------------------------------------------------- the locations

function LocationMarks({ place, location, destroyed, engineRoom }: { place: Place; location: LocationProjection; destroyed: boolean; engineRoom: boolean }) {
  const name = location.location as Location;
  const damage = location.damaged ? damageArt(place.area === 'pegasus' ? 'PegasusDamage' : 'GalacticaDamage', name) : null;
  return (
    <>
      {destroyed && <rect className="destroyed-veil" x={place.x} y={place.y} width={place.width} height={place.height} rx={10} />}
      {location.damaged &&
        (damage ? (
          <SvgArt art={damage} x={place.x + (place.width - 96) / 2} y={place.y + 14} width={96} height={96} className="damage-token piece" />
        ) : (
          <text className="damage" x={place.x + place.width / 2} y={place.y + 60} textAnchor="middle">
            damaged
          </text>
        ))}
      {engineRoom && <Count x={place.x + 10} y={place.y + 20} value="activated" small accent />}
    </>
  );
}

// The standees stand at the bottom of the location, side by side while they fit, overlapping when they don't.
function Standees({ place, characters, current, cylons, nameOf, game }: {
  place: Place;
  characters: Character[];
  current: Character | null;
  cylons: Set<Character | null>;
  nameOf(playerNumber: number): string;
  game: GameProjection;
}) {
  if (characters.length === 0) {
    return null;
  }
  const width = 74;
  const height = 109;
  const step = characters.length <= 1 ? 0 : Math.min(width + 4, (place.width - width) / (characters.length - 1));
  const left = place.x + (place.width - (width + step * (characters.length - 1))) / 2;
  const top = place.y + place.height - height - 8;
  return (
    <>
      {characters.map((character, index) => {
        const art = characterArt(character);
        const player = game.players?.find((candidate) => candidate.character === character);
        const name = player ? nameOf(player.number) : constantLabel('Character', character);
        const x = left + index * step;
        const classes = ['character', character === current ? 'current' : '', cylons.has(character) ? 'cylon' : ''].join(' ').trim();
        return (
          <g key={character} className={classes} data-character={character}>
            {art ? (
              <>
                <rect className="token-ring" x={x - 3} y={top - 3} width={width + 6} height={height + 6} rx={8} />
                <SvgArt art={art} x={x} y={top} width={width} height={height} title={name} />
              </>
            ) : (
              <>
                <rect x={place.x + 6} y={place.y + 60 + index * 40} width={place.width - 12} height={34} rx={14} />
                <text x={place.x + place.width / 2} y={place.y + 84 + index * 40} textAnchor="middle">
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

// The size of each ship's image on the board, in the proportions of the scans.
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

// the piles laid out left to right, wrapping inside the box
function layOut(box: Box, groups: ShipGroup[]) {
  const placed: { group: ShipGroup; x: number; y: number; width: number; height: number }[] = [];
  let x = box.x;
  let y = box.y;
  let rowHeight = 0;
  for (const group of groups) {
    const [width, height] = SHIP_SIZE[group.type] ?? [90, 50];
    const shown = Math.min(group.ships.length, PILE_MAX);
    const pilot = group.ships[0].pilot ? 30 : 0;
    const damage = (group.ships[0].damage ?? []).length > 0 ? 44 : 0;
    const groupWidth = width + (shown - 1) * PILE_STEP + (group.ships.length > 1 ? 60 : 0);
    const groupHeight = height + (shown - 1) * 6 + pilot + damage;
    if (x + groupWidth > box.x + box.width && x > box.x) {
      x = box.x;
      y += rowHeight + 14;
      rowHeight = 0;
    }
    placed.push({ group, x, y: y + pilot, width, height });
    x += groupWidth + 18;
    rowHeight = Math.max(rowHeight, groupHeight);
  }
  return placed;
}

function Ships({ place, ships, toMiss }: { place: Place; ships: ShipProjection[]; toMiss: number }) {
  const placed = layOut(place, groupShips(ships));
  return (
    <g className="ships">
      {toMiss > 0 && <Count x={place.x + place.width - 10} y={place.y} value={`${toMiss} will miss`} small accent />}
      {placed.map(({ group, x, y, width, height }) => {
        const shown = Math.min(group.ships.length, PILE_MAX);
        return (
          <g key={group.ships[0].id} className={`ship-group ship-${group.type.toLowerCase()}`}>
            {group.ships.map((ship, index) => {
              const at = Math.min(index, PILE_MAX - 1);
              return <Ship key={ship.id} ship={ship} x={x + at * PILE_STEP} y={y + (shown - 1 - at) * 6} width={width} height={height} />;
            })}
            {group.ships.length > 1 && <Count x={x + width + (shown - 1) * PILE_STEP + 8} y={y + height / 2 - 22} value={`×${group.ships.length}`} small />}
          </g>
        );
      })}
    </g>
  );
}

function Ship({ ship, x, y, width, height }: { ship: ShipProjection; x: number; y: number; width: number; height: number }) {
  const art = shipArt(ship.type);
  const label = boardShipLabel(ship);
  const pilot = ship.pilot ? pilotArt(ship.pilot) : null;
  return (
    <g className={`ship ship-${(ship.type ?? '').toLowerCase()}`} data-ship={ship.id} aria-label={label}>
      {art ? (
        <SvgArt art={art} x={x} y={y} width={width} height={height} title={label} className="piece" />
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
function CountedPile({ type, count, x, y }: { type: string; count: number; x: number; y: number }) {
  const art = type === 'CENTURION' ? tokenArt('CENTURION') : shipArt(type);
  const [width, height] = SHIP_SIZE[type] ?? [90, 50];
  const shown = Math.min(count, PILE_MAX);
  return (
    <g className={`supply supply-${type.toLowerCase()}`} data-supply={type} aria-label={`${count} ${constantLabel('ShipType', type)}`}>
      {art &&
        Array.from({ length: shown }, (_, index) => (
          <SvgArt key={index} art={art} x={x + index * PILE_STEP} y={y + (shown - 1 - index) * 6} width={width} height={height} className="piece" preview={false} />
        ))}
      <Count x={x + width + (shown - 1) * PILE_STEP + 12} y={y + height / 2 - 22} value={`×${count}`} small />
    </g>
  );
}

function ShipPiles({ box, ships }: { box: Box; ships: ShipProjection[] }) {
  return (
    <>
      {layOut(box, groupShips(ships)).map(({ group, x, y, width, height }) => {
        const shown = Math.min(group.ships.length, PILE_MAX);
        return (
          <g key={group.ships[0].id} className={`ship-group ship-${group.type.toLowerCase()}`}>
            {group.ships.map((ship, index) => {
              const at = Math.min(index, PILE_MAX - 1);
              return <Ship key={ship.id} ship={ship} x={x + at * PILE_STEP} y={y + (shown - 1 - at) * 6} width={width} height={height} />;
            })}
            {group.ships.length > 1 && <Count x={x + width + (shown - 1) * PILE_STEP + 8} y={y + height / 2 - 22} value={`×${group.ships.length}`} small />}
          </g>
        );
      })}
    </>
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

/** A number on the board in a chip with cut corners: a count, a difficulty, a value. */
function Count({ x, y, value, small = false, accent = false, className = '' }: {
  x: number;
  y: number;
  value: number | string;
  small?: boolean;
  accent?: boolean;
  className?: string;
}) {
  const text = String(value);
  const size = small ? 30 : 36;
  const width = 26 + text.length * size * 0.56;
  const height = size + 16;
  const cut = 10;
  const points = [
    [x + cut, y],
    [x + width, y],
    [x + width, y + height - cut],
    [x + width - cut, y + height],
    [x, y + height],
    [x, y + cut],
  ]
    .map((point) => point.join(','))
    .join(' ');
  return (
    <g className={['count', accent ? 'accent' : '', className].join(' ').trim()}>
      <polygon points={points} />
      <text x={x + width / 2} y={y + height / 2 + size * 0.36} textAnchor="middle" fontSize={size}>
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


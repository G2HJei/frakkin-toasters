import { cardArt, characterArt, damageArt, locationArt, pilotArt, resourceArt, shipArt, trackArt, type ArtRef } from '../../art/images';
import { SvgArt } from '../../art/Art';
import { constantLabel } from '../../labels/describe';
import type { Character, GameProjection, Location, LocationProjection, ShipProjection } from '../../wire/types';
import { useTable } from '../tableContext';
import { AREA_TITLES, BOARD_HEIGHT, BOARD_WIDTH, HULL, LAYOUT, SIDE, STRIP, type Place } from './layout';

export interface BoardSvgProps {
  // the locations a pending input can be answered with, which light up and take a click
  targets?: ReadonlySet<string>;
  onLocation?(location: Location): void;
  // the location whose action the asked player can take, with an "Act" button on it
  actAt?: string | null;
  onAct?(): void;
}

const RESOURCES = ['fuel', 'food', 'morale', 'population'] as const;
const JUMP_STEPS = ['START', 'POSITION_1', 'POSITION_2', 'MINUS_3', 'MINUS_1'];
const PURSUIT_STEPS = ['START', 'ONE_CIVILIAN_SHIP', 'POSITION_2', 'TWO_CIVILIAN_SHIPS', 'AUTO_ATTACK'];
const BOARDING_STEPS = ['START', 'POSITION_1', 'POSITION_2', 'POSITION_3', 'HUMANS_LOSE'];
// height per width of the track images
const TRACK_ASPECT: Record<string, number> = { JumpPreparation: 187 / 526, Pursuit: 185 / 434 };

/**
 * The boards: every character's standee at his location, every ship in its space area with its pilot and damage, the
 * damaged locations, the tracks and the dials, the objective and the destinations reached.
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
  const characterName = (character: Character) => {
    const player = game.players?.find((candidate) => candidate.character === character);
    return player ? table.playerName(player.number) : constantLabel('Character', character);
  };

  return (
    <svg className="board-svg" viewBox={`0 0 ${BOARD_WIDTH} ${BOARD_HEIGHT}`} role="img" aria-label="the board">
      <Strip game={game} />

      <rect className="hull" x={HULL.x} y={HULL.y} width={HULL.width} height={HULL.height} rx={40} />
      {AREA_TITLES.map((area) => (
        <text key={area.area} className="area-title" x={area.x} y={area.y}>
          {area.title}
          {area.area === 'colonialOne' && galactica?.colonialOneDestroyed ? ' · destroyed' : ''}
          {area.area === 'pegasus' && game.pegasus?.engineRoomActivated ? ' · Engine Room activated' : ''}
        </text>
      ))}

      {locations.map((location) => {
        const name = location.location as Location;
        const place = LAYOUT[name];
        if (!place) {
          return null;
        }
        const isTarget = targets?.has(name) ?? false;
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
            {place.area === 'galacticaSpace' || place.area === 'cylonFleetSpace' ? (
              <SpaceArea place={place} name={name} ships={ships.filter((ship) => ship.location === name)} toMiss={game.turn?.raidersToMiss?.[name] ?? 0} />
            ) : (
              <LocationCard place={place} location={location} />
            )}
            <Tokens place={place} characters={(location.characters ?? []) as Character[]} current={currentCharacter} nameOf={characterName} />
            {actAt === name && onAct && (
              <g className="act" role="button" aria-label="Act" onClick={onAct}>
                <rect x={place.x + place.width - 38} y={place.y + 4} width={34} height={18} rx={5} />
                <text x={place.x + place.width - 21} y={place.y + 17} textAnchor="middle">
                  Act
                </text>
              </g>
            )}
          </g>
        );
      })}

      <Track
        x={SIDE.x}
        y={526}
        title="Boarding party"
        steps={BOARDING_STEPS}
        current={null}
        enumName="BoardingParty"
        stepWidth={56}
        marks={(galactica?.boardingParty ?? []).map((centurion) => centurion.position)}
      />
      <TitleTokens game={game} nameOf={characterName} />
    </svg>
  );
}

/** The dials, the jump preparation and pursuit tracks, the objective with the destinations reached. */
function Strip({ game }: { game: GameProjection }) {
  const galactica = game.galactica;
  const objective = cardArt('ObjectiveCard', game.objective);
  const destinations = game.destinations ?? [];
  return (
    <g className="strip">
      {RESOURCES.map((resource, index) => (
        <Dial key={resource} x={10 + index * 84} resource={resource} value={galactica?.[resource] ?? null} />
      ))}
      <TrackArt
        art={trackArt('JumpPreparation', galactica?.jumpPreparation)}
        x={352}
        width={300}
        title="Jump preparation"
        steps={JUMP_STEPS}
        current={galactica?.jumpPreparation}
        enumName="JumpPreparation"
      />
      <TrackArt
        art={trackArt('Pursuit', game.cylonFleet?.pursuit)}
        x={662}
        width={251}
        title="Pursuit"
        steps={PURSUIT_STEPS}
        current={game.cylonFleet?.pursuit}
        enumName="Pursuit"
      />
      {objective && <SvgArt art={objective} x={925} y={STRIP.y} width={64} height={99} className="objective" />}
      <g className="destinations">
        {destinations.map((card, index) => {
          const art = cardArt('DestinationCard', card);
          return art ? (
            <SvgArt key={index} art={art} x={1000 + index * 24} y={STRIP.y} width={44} height={68} />
          ) : (
            <text key={index} className="note" x={1000} y={STRIP.y + 12 + index * 12}>
              {constantLabel('DestinationCard', card)}
            </text>
          );
        })}
      </g>
      <text className="note" x={1000} y={STRIP.y + 86}>
        distance {game.distanceTraveled}
      </text>
      <text className="note" x={1000} y={STRIP.y + 102}>
        nukes {game.nukes}
      </text>
    </g>
  );
}

// The dial's image shows the value, and the caption says it again for the dials past 15 and for a screen reader.
function Dial({ x, resource, value }: { x: number; resource: string; value: number | null }) {
  const art = value === null ? null : resourceArt(resource.toUpperCase(), value);
  return (
    <g className="dial" data-resource={resource}>
      {art ? (
        <SvgArt art={art} x={x} y={STRIP.y} width={72} height={72} />
      ) : (
        <circle cx={x + 36} cy={STRIP.y + 36} r={32} />
      )}
      <text className="dial-caption" x={x + 36} y={STRIP.y + 92} textAnchor="middle">
        <tspan className="dial-name">{resource}</tspan> <tspan className="dial-value">{value ?? '—'}</tspan>
      </text>
    </g>
  );
}

// A track's image has its token at the position. Where there is none, the schematic.
function TrackArt({ art, x, width, title, steps, current, enumName }: {
  art: ArtRef | null;
  x: number;
  width: number;
  title: string;
  steps: string[];
  current: string | null | undefined;
  enumName: string;
}) {
  if (!art) {
    return <Track x={x} y={STRIP.y + 30} title={title} steps={steps} current={current} enumName={enumName} stepWidth={(width - 16) / steps.length} />;
  }
  return (
    <g className="track-art" data-track={enumName} data-position={current ?? ''}>
      <SvgArt art={art} x={x} y={STRIP.y} width={width} height={width * TRACK_ASPECT[enumName]} title={`${title}: ${art.label}`} />
    </g>
  );
}

function LocationCard({ place, location }: { place: Place; location: LocationProjection }) {
  const name = location.location as Location;
  const art = locationArt(name);
  const damage = location.damaged ? damageArt(place.area === 'pegasus' ? 'PegasusDamage' : 'GalacticaDamage', name) : null;
  const damageSize = Math.min(place.width * 0.62, 54);
  return (
    <>
      {art ? <SvgArt art={art} x={place.x} y={place.y} width={place.width} height={place.height} className="location-art" /> : <Name place={place} text={constantLabel('Location', name)} />}
      <rect className="frame" x={place.x} y={place.y} width={place.width} height={place.height} rx={6} />
      {location.damaged &&
        (damage ? (
          <SvgArt art={damage} x={place.x + (place.width - damageSize) / 2} y={place.y + 6} width={damageSize} height={damageSize} className="damage-token" />
        ) : (
          <text className="damage" x={place.x + place.width - 6} y={place.y + 12} textAnchor="end">
            damaged
          </text>
        ))}
    </>
  );
}

function SpaceArea({ place, name, ships, toMiss }: { place: Place; name: Location; ships: ShipProjection[]; toMiss: number }) {
  return (
    <>
      <rect className="space" x={place.x} y={place.y} width={place.width} height={place.height} rx={8} />
      <text className="space-name" x={place.x + 6} y={place.y + 12}>
        {constantLabel('Location', name)}
      </text>
      {toMiss > 0 && (
        <text className="to-miss" x={place.x + place.width - 6} y={place.y + 12} textAnchor="end">
          {toMiss} raider{toMiss > 1 ? 's' : ''} will miss
        </text>
      )}
      <Ships place={place} ships={ships} />
    </>
  );
}

// The standees stand at the bottom of the location, side by side while they fit, overlapping when they don't.
function Tokens({ place, characters, current, nameOf }: {
  place: Place;
  characters: Character[];
  current: Character | null;
  nameOf(character: Character): string;
}) {
  if (characters.length === 0) {
    return null;
  }
  const width = Math.round(place.width * 0.3);
  const height = Math.round((width * 128) / 87);
  const span = place.width - 6;
  const step = characters.length <= 1 ? 0 : Math.min(width + 2, (span - width) / (characters.length - 1));
  const left = place.x + (place.width - (width + step * (characters.length - 1))) / 2;
  const top = place.y + place.height - height - 3;
  return (
    <>
      {characters.map((character, index) => {
        const art = characterArt(character);
        const x = left + index * step;
        return (
          <g key={character} className={character === current ? 'character current' : 'character'} data-character={character}>
            {art ? (
              <>
                <rect className="token-ring" x={x - 1} y={top - 1} width={width + 2} height={height + 2} rx={3} />
                <SvgArt art={art} x={x} y={top} width={width} height={height} title={nameOf(character)} />
              </>
            ) : (
              <>
                <rect x={place.x + 4} y={place.y + 30 + index * 17} width={place.width - 8} height={15} rx={7} />
                <text x={place.x + place.width / 2} y={place.y + 41 + index * 17} textAnchor="middle">
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

// The size of each ship's image on the board, in the proportions of the scans.
const SHIP_SIZE: Record<string, [number, number]> = {
  BASESTAR: [108, 56],
  HEAVY_RAIDER: [62, 18],
  RAIDER: [42, 29],
  VIPER: [54, 28],
  VIPER_MARK_VII: [54, 32],
  RAPTOR: [46, 29],
  ASSAULT_RAPTOR: [46, 34],
  CIVILIAN: [54, 27],
};
const SHIP_ORDER = Object.keys(SHIP_SIZE);
const PILE_STEP = 5;
const PILE_MAX = 5;

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

function Ships({ place, ships }: { place: Place; ships: ShipProjection[] }) {
  const placed: { group: ShipGroup; x: number; y: number; width: number; height: number }[] = [];
  let x = place.x + 6;
  let y = place.y + 22;
  let rowHeight = 0;
  for (const group of groupShips(ships)) {
    const [width, height] = SHIP_SIZE[group.type] ?? [44, 22];
    const shown = Math.min(group.ships.length, PILE_MAX);
    const groupWidth = width + (shown - 1) * PILE_STEP + (group.ships.length > 1 ? 22 : 0);
    const groupHeight = height + (shown - 1) * 3 + (group.ships[0].pilot ? 10 : 0) + ((group.ships[0].damage ?? []).length > 0 ? 15 : 0);
    if (x + groupWidth > place.x + place.width - 4 && x > place.x + 6) {
      x = place.x + 6;
      y += rowHeight + 6;
      rowHeight = 0;
    }
    placed.push({ group, x, y: y + (group.ships[0].pilot ? 10 : 0), width, height });
    x += groupWidth + 8;
    rowHeight = Math.max(rowHeight, groupHeight);
  }
  return (
    <g className="ships">
      {placed.map(({ group, x: left, y: top, width, height }) => {
        const shown = Math.min(group.ships.length, PILE_MAX);
        return (
          <g key={group.ships[0].id} className={`ship-group ship-${group.type.toLowerCase()}`}>
            {group.ships.map((ship, index) => {
              const at = Math.min(index, PILE_MAX - 1);
              return <Ship key={ship.id} ship={ship} x={left + at * PILE_STEP} y={top + (shown - 1 - at) * 3} width={width} height={height} />;
            })}
            {group.ships.length > 1 && (
              <text className="ship-count" x={left + width + (shown - 1) * PILE_STEP + 3} y={top + height / 2 + 4}>
                ×{group.ships.length}
              </text>
            )}
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
        <SvgArt art={art} x={x} y={y} width={width} height={height} title={label} />
      ) : (
        <text x={x} y={y + 10}>
          {label}
        </text>
      )}
      {pilot && <SvgArt art={pilot} x={x + width - 14} y={y - 10} width={22} height={22} className="pilot-token" title={label} />}
      {(ship.damage ?? []).map((damage, index) => {
        const token = damageArt('BasestarDamage', damage);
        return token ? <SvgArt key={index} art={token} x={x + index * 15} y={y + height + 1} width={14} height={14} /> : null;
      })}
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

// The title holders, standing by their titles.
function TitleTokens({ game, nameOf }: { game: GameProjection; nameOf(character: Character): string }) {
  const titles = [
    ['President', game.titles?.president],
    ['Admiral', game.titles?.admiral],
    ['CAG', game.titles?.cag],
  ] as const;
  return (
    <g className="titles">
      {titles.map(([title, character], index) => {
        const x = SIDE.x + index * 100;
        const art = characterArt(character);
        return (
          <g key={title} className="title" data-title={title}>
            {art && character && <SvgArt art={art} x={x} y={576} width={30} height={44} title={`${title}: ${nameOf(character)}`} />}
            <text className="note" x={x + (art ? 36 : 0)} y={592}>
              {title}
            </text>
            <text x={x + (art ? 36 : 0)} y={607}>
              {character ? shortName(constantLabel('Character', character)) : '—'}
            </text>
          </g>
        );
      })}
    </g>
  );
}

function Name({ place, text }: { place: Place; text: string }) {
  // a long name takes two lines
  const words = text.split(' ');
  const split = words.length > 1 && text.length > 14 ? Math.ceil(words.length / 2) : words.length;
  return (
    <>
      <rect className="plain" x={place.x} y={place.y} width={place.width} height={place.height} rx={6} />
      <text className="location-name" x={place.x + 6} y={place.y + 13}>
        <tspan>{words.slice(0, split).join(' ')}</tspan>
        {split < words.length && (
          <tspan x={place.x + 6} dy={11}>
            {words.slice(split).join(' ')}
          </tspan>
        )}
      </text>
    </>
  );
}

function Track({ x, y, title, steps, current, enumName, stepWidth, marks = [] }: {
  x: number;
  y: number;
  title: string;
  steps: string[];
  current: string | null | undefined;
  enumName: string;
  stepWidth: number;
  marks?: (string | null)[];
}) {
  return (
    <g className="track" transform={`translate(${x}, ${y})`}>
      <text className="track-title" y={0}>
        {title}
      </text>
      {steps.map((step, index) => {
        const count = marks.filter((mark) => mark === step).length;
        return (
          <g key={step} className={step === current || count > 0 ? 'step current' : 'step'} transform={`translate(${index * (stepWidth + 4)}, 8)`}>
            <rect width={stepWidth} height={24} rx={4} />
            <text x={stepWidth / 2} y={16} textAnchor="middle">
              {constantLabel(enumName, step)}
              {count > 0 ? ` ×${count}` : ''}
            </text>
          </g>
        );
      })}
    </g>
  );
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


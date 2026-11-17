import { constantLabel } from '../../labels/describe';
import type { Location, ShipProjection } from '../../wire/types';
import { useTable } from '../tableContext';
import { AREA_TITLES, BOARD_HEIGHT, BOARD_WIDTH, HULL, LAYOUT, type Place } from './layout';

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
const BOARDING_STEPS = ['START', 'POSITION_1', 'POSITION_2', 'POSITION_3', 'HUMANS_LOSE'];

/** The boards as a schematic: every character at his location, every ship in its space area, the tracks and dials. */
export function BoardSvg({ targets, onLocation, actAt, onAct }: BoardSvgProps) {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const galactica = game.galactica;
  const locations = [...(galactica?.locations ?? []), ...(game.pegasus?.locations ?? []), ...(game.cylonFleet?.locations ?? [])];
  const ships = [...(galactica?.ships ?? []), ...(game.cylonFleet?.ships ?? [])];

  return (
    <svg className="board-svg" viewBox={`0 0 ${BOARD_WIDTH} ${BOARD_HEIGHT}`} role="img" aria-label="the board">
      {RESOURCES.map((resource, index) => (
        <g key={resource} className="dial" transform={`translate(${45 + index * 80}, 55)`}>
          <circle r={32} />
          <text className="dial-value" textAnchor="middle" dy={8}>
            {galactica?.[resource] ?? '—'}
          </text>
          <text className="dial-name" textAnchor="middle" y={48}>
            {resource}
          </text>
        </g>
      ))}
      <Track x={360} y={22} title="Jump preparation" steps={JUMP_STEPS} current={galactica?.jumpPreparation} enumName="JumpPreparation" />
      <Track
        x={360}
        y={78}
        title="Boarding party"
        steps={BOARDING_STEPS}
        current={null}
        enumName="BoardingParty"
        marks={(galactica?.boardingParty ?? []).map((centurion) => centurion.position)}
      />
      <text className="note" x={20} y={135}>
        Distance {game.distanceTraveled} · nukes {game.nukes}
        {(game.destinations ?? []).length > 0 ? ` · ${(game.destinations ?? []).map((card) => constantLabel('DestinationCard', card)).join(', ')}` : ''}
      </text>
      <text className="note" x={20} y={155}>
        President {constantLabel('Character', game.titles?.president)} · Admiral {constantLabel('Character', game.titles?.admiral)} · CAG{' '}
        {constantLabel('Character', game.titles?.cag)}
      </text>

      <rect className="hull" x={HULL.x} y={HULL.y} width={HULL.width} height={HULL.height} rx={40} />
      {AREA_TITLES.map((area) => (
        <text key={area.area} className="area-title" x={area.x} y={area.y}>
          {area.area === 'cylonFleet' ? `${area.title} · pursuit ${constantLabel('Pursuit', game.cylonFleet?.pursuit)}` : area.title}
        </text>
      ))}

      {locations.map((location) => {
        const name = location.location as Location;
        const place = LAYOUT[name];
        if (!place) {
          return null;
        }
        const isTarget = targets?.has(name) ?? false;
        const here = ships.filter((ship) => ship.location === name);
        return (
          <g
            key={name}
            className={['location', place.area, location.damaged ? 'damaged' : '', isTarget ? 'target' : ''].join(' ')}
            data-location={name}
            onClick={isTarget && onLocation ? () => onLocation(name) : undefined}
            role={isTarget ? 'button' : undefined}
            aria-label={isTarget ? `${constantLabel('Location', name)}` : undefined}
          >
            <rect x={place.x} y={place.y} width={place.width} height={place.height} rx={6} />
            <Name place={place} text={constantLabel('Location', name)} />
            {location.damaged && (
              <text className="damage" x={place.x + place.width - 6} y={place.y + 12} textAnchor="end">
                damaged
              </text>
            )}
            {(location.characters ?? []).map((character, index) => (
              <g key={character} className="character" data-character={character}>
                <rect x={place.x + 4} y={place.y + 30 + index * 17} width={place.width - 8} height={15} rx={7} />
                <text x={place.x + place.width / 2} y={place.y + 41 + index * 17} textAnchor="middle">
                  {shortName(constantLabel('Character', character))}
                </text>
              </g>
            ))}
            {here.length > 0 && <Ships place={place} ships={here} />}
            {actAt === name && onAct && (
              <g className="act" role="button" aria-label="Act" onClick={onAct}>
                <rect x={place.x + place.width - 38} y={place.y + place.height - 22} width={34} height={18} rx={5} />
                <text x={place.x + place.width - 21} y={place.y + place.height - 9} textAnchor="middle">
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

function Name({ place, text }: { place: Place; text: string }) {
  // a long name takes two lines
  const words = text.split(' ');
  const split = words.length > 1 && text.length > 14 ? Math.ceil(words.length / 2) : words.length;
  return (
    <text className="location-name" x={place.x + 6} y={place.y + 13}>
      <tspan>{words.slice(0, split).join(' ')}</tspan>
      {split < words.length && (
        <tspan x={place.x + 6} dy={11}>
          {words.slice(split).join(' ')}
        </tspan>
      )}
    </text>
  );
}

const MAX_SHIP_LINES = 5;

function Ships({ place, ships }: { place: Place; ships: ShipProjection[] }) {
  const shown = ships.slice(0, MAX_SHIP_LINES);
  return (
    <g className="ships">
      {shown.map((ship, index) => (
        <text key={ship.id} className={`ship ship-${(ship.type ?? '').toLowerCase()}`} data-ship={ship.id} x={place.x + 8} y={place.y + 40 + index * 14}>
          {constantLabel('ShipType', ship.type)} #{ship.id}
          {ship.pilot ? ` · ${shortName(constantLabel('Character', ship.pilot))}` : ''}
          {ship.damage && ship.damage.length > 0 ? ` · ${ship.damage.length} damage` : ''}
        </text>
      ))}
      {ships.length > MAX_SHIP_LINES && (
        <text className="ship more" x={place.x + 8} y={place.y + 40 + MAX_SHIP_LINES * 14}>
          +{ships.length - MAX_SHIP_LINES} more
        </text>
      )}
    </g>
  );
}

function Track({ x, y, title, steps, current, enumName, marks = [] }: {
  x: number;
  y: number;
  title: string;
  steps: string[];
  current: string | null | undefined;
  enumName: string;
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
          <g key={step} className={step === current || count > 0 ? 'step current' : 'step'} transform={`translate(${index * 104}, 8)`}>
            <rect width={98} height={24} rx={4} />
            <text x={49} y={16} textAnchor="middle">
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

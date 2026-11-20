// The four resources as the board prints them: their icons (cut from the board's picture, where they stand by their
// labels), their red zones, and the resources a civilian ship costs when it is destroyed.
import type { Cargo } from '../../wire/types';
import food from './icons/FOOD.png';
import fuel from './icons/FUEL.png';
import morale from './icons/MORALE.png';
import population from './icons/POPULATION.png';

export type ResourceName = 'fuel' | 'food' | 'morale' | 'population';

export const RESOURCE_NAMES: ResourceName[] = ['fuel', 'food', 'morale', 'population'];

const ICONS: Record<ResourceName, string> = { fuel, food, morale, population };

const LABELS: Record<ResourceName, string> = { fuel: 'Fuel', food: 'Food', morale: 'Morale', population: 'Population' };

// below these the dials of the printed game are red
const RED_ZONE: Record<ResourceName, number> = { fuel: 5, food: 5, morale: 6, population: 7 };

export function resourceLabel(resource: ResourceName): string {
  return LABELS[resource];
}

export function inRedZone(resource: ResourceName, value: number | null | undefined): boolean {
  return value !== null && value !== undefined && value < RED_ZONE[resource];
}

export function ResourceIcon({ resource }: { resource: ResourceName }) {
  return <img className="resource-icon" src={ICONS[resource]} alt={LABELS[resource]} draggable={false} />;
}

// what a civilian ship costs, the deck's cards spelling it out as costs
type Costs = Cargo | { fuelCost: number; moraleCost: number; populationCost: number };

function amounts(costs: Costs): [ResourceName, number][] {
  const values: [ResourceName, number][] =
    'fuelCost' in costs
      ? [['population', costs.populationCost], ['morale', costs.moraleCost], ['fuel', costs.fuelCost]]
      : [['population', costs.population], ['morale', costs.morale], ['fuel', costs.fuel]];
  return values.filter(([, amount]) => amount > 0);
}

/** What a civilian ship costs when destroyed, in words: "1 population, 1 fuel", or "nothing". */
export function cargoText(costs: Costs): string {
  const lost = amounts(costs);
  return lost.length === 0 ? 'nothing' : lost.map(([resource, amount]) => `${amount} ${resource}`).join(', ');
}

/** What a civilian ship costs when destroyed, as the resources' icons with the amounts. */
export function CargoIcons({ cargo }: { cargo: Cargo }) {
  const lost = amounts(cargo);
  return (
    <span className="cargo" aria-label={cargoText(cargo)}>
      {lost.length === 0 ? (
        <span className="cargo-none">nothing</span>
      ) : (
        lost.map(([resource, amount]) => (
          <span key={resource} className="cargo-amount" data-resource={resource}>
            <ResourceIcon resource={resource} />
            {amount}
          </span>
        ))
      )}
    </span>
  );
}

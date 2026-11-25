import {fireEvent, render} from '@testing-library/react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {AnsweringContext, NO_OFFER, type Offer} from '../../input/boardAnswers';
import type {Move} from '../../input/answers';
import {BoardSvg, groupShips, shortName} from './BoardSvg';
import {ArtPreview, usePreview} from '../../art/Art';
import {RevealLayer, useReveal} from '../reveal';
import {Table, tableContext} from '../tableContext';
import type {GameProjection, LogEntry, ShipProjection} from '../../wire/types';
import {BOARD_LABELS, type BoardLabel} from '../../labels/board';
import {DECK_SLOTS, DISTANCE, JUMP_TRACK, LABELS, LAUNCH_TUBES, LAYOUT, RESOURCES, SLOTS, TITLES} from './layout';
import {resourceIconUrl} from './resources';
import {useGameStore} from '../../live/gameStore';

const location = (name: string, characters: string[] = [], damaged = false) => ({location: name, damaged, characters});

// A projection as the backend sends it, cut down to what the board draws.
const game = {
    viewer: {kind: 'Observer'},
    objective: 'KOBOL',
    titles: {president: 'LAURA_ROSLIN', admiral: 'WILLIAM_ADAMA', cag: 'KARA_STARBUCK_THRACE'},
    turn: {
        currentPlayer: 1,
        actingPlayer: 1,
        crisisCards: ['WATER_SABOTAGED'],
        dieRoll: {value: 5, cards: [], modifier: 0, result: 5}
    },
    players: [{number: 1, character: 'WILLIAM_ADAMA'}],
    galactica: {
        fuel: 8,
        food: 7,
        morale: 9,
        population: 12,
        jumpPreparation: 'POSITION_2',
        locations: [
            location('RESEARCH_LAB', ['WILLIAM_ADAMA']),
            location('ARMORY', [], true),
            ...['2', '4', '8', '10', '12'].map((hour) => location(`GALACTICA_SPACE_${hour}_OCLOCK`)),
        ],
        ships: [{
            id: 3,
            type: 'VIPER',
            location: 'GALACTICA_SPACE_4_OCLOCK',
            pilot: 'KARA_STARBUCK_THRACE',
            damage: [],
            cargo: null
        }],
        reserves: [{id: 4, type: 'VIPER', location: null, pilot: null, damage: [], cargo: null}],
        damagedVipers: [],
        boardingParty: [{id: 1040, position: 'POSITION_1'}],
    },
    pegasus: {engineRoomActivated: false, destroyed: false, locations: [location('AIRLOCK', ['LAURA_ROSLIN'])]},
    cylonFleet: {
        pursuit: 'START',
        locations: [location('CYLON_FLEET_SPACE_2')],
        ships: [{id: 1019, type: 'RAIDER', location: 'CYLON_FLEET_SPACE_2', pilot: null, damage: [], cargo: null}],
    },
    cylonSupply: {basestars: 2, raiders: 12, heavyRaiders: 4, centurions: 3},
    decks: [
        {deck: 'CRISIS', size: 70, discardSize: 2, discarded: ['RIOTS_JUMP', 'WATER_SABOTAGED'], drawPile: null},
        {deck: 'POLITICS', size: 12, discardSize: 1, discarded: [{type: 'RED_TAPE', value: 0}], drawPile: null},
    ],
    skillCheck: {
        type: 'CYLON_SCREENINGS',
        targetPlayer: null,
        difficulty: 9,
        playedBefore: [],
        playedBeforeRevealed: false,
        cards: {count: 3, colors: {}, cards: null},
        matchingPile: [],
        nonMatchingPile: [],
        resolvedAbilities: [],
        emergencyDeclared: false,
        totalStrength: null,
        result: null,
    },
    destinations: ['ALGAE_PLANET'],
    distanceTraveled: 2,
    nukes: 2,
} as unknown as GameProjection;

function renderBoard(offer?: Offer, projection: GameProjection = game, sandbox = false, onTarget = vi.fn()) {
    return render(
        <Table.Provider value={tableContext(projection, sandbox, null)}>
            <AnsweringContext.Provider value={{offer: offer ?? NO_OFFER, onTarget}}>
                <BoardSvg/>
                <RevealLayer/>
                <ArtPreview/>
            </AnsweringContext.Provider>
        </Table.Provider>,
    ).container;
}

afterEach(() => {
    useReveal.getState().hide();
    usePreview.setState({art: null, pointer: null, extra: null});
    useGameStore.getState().reset();
});

// where the images of a space area's ships lie
function shipBoxes(board: HTMLElement, location: string) {
    return [...board.querySelectorAll(`[data-location="${location}"] .ship > image`)].map((image) => ({
        art: image.getAttribute('data-art'),
        x: Number(image.getAttribute('x')),
        y: Number(image.getAttribute('y')),
        width: Number(image.getAttribute('width')),
        height: Number(image.getAttribute('height')),
    }));
}

const inSpace = (id: number, type: string, location: string, extra: object = {}) => ({
    id,
    type,
    location,
    pilot: null,
    damage: [],
    cargo: null, ...extra
});

describe('the board', () => {
    it("lies on the board's image, each character's standee on the printed card of the location the projection puts him", () => {
        const board = renderBoard();

        expect(board.querySelector('.board-image')?.getAttribute('href')).toContain('board');
        expect(board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"] [data-art="Character/player-token/WILLIAM_ADAMA"]')).not.toBeNull();
        expect(board.querySelector('[data-location="AIRLOCK"] [data-character="LAURA_ROSLIN"]')).not.toBeNull();
        expect(board.querySelectorAll('[data-character="WILLIAM_ADAMA"]')).toHaveLength(1);
        // the acting player's standee stands out
        expect(board.querySelector('[data-character="WILLIAM_ADAMA"]')?.classList).toContain('acting');
    });

    it("rings the acting player's standee, not the current player's or a revealed Cylon's", () => {
        const acting = {
            ...game,
            turn: {...game.turn, actingPlayer: 2},
            players: [{number: 1, character: 'WILLIAM_ADAMA'}, {
                number: 2,
                character: 'LAURA_ROSLIN',
                revealedCylon: true
            }],
        } as unknown as GameProjection;
        const board = renderBoard(undefined, acting);

        expect(board.querySelector('[data-character="LAURA_ROSLIN"]')?.classList).toContain('acting');
        expect(board.querySelector('[data-character="WILLIAM_ADAMA"]')?.classList).not.toContain('acting');
        expect(board.querySelectorAll('.character.current, .character.cylon')).toHaveLength(0);
    });

    it("draws each ship in its space area, with its pilot's token, and the reserves and the Cylon supply in their slots", () => {
        const board = renderBoard();

        const viper = board.querySelector('[data-location="GALACTICA_SPACE_4_OCLOCK"] [data-ship="3"]');
        expect(viper?.getAttribute('aria-label')).toBe('Viper #3 · Starbuck');
        expect(viper?.querySelector('[data-art="ShipType/VIPER"]')).not.toBeNull();
        expect(viper?.querySelector('[data-art="Character/piloting-token/KARA_STARBUCK_THRACE"]')).not.toBeNull();
        expect(board.querySelector('[data-location="CYLON_FLEET_SPACE_2"] [data-ship="1019"]')?.getAttribute('aria-label')).toBe('Raider #1019');
        expect(board.querySelector('[data-slot="reserves"] [data-ship="4"]')).not.toBeNull();
        expect(board.querySelector('[data-supply="RAIDER"] .count')?.textContent).toBe('12');
    });

    it('piles up the ships of a type, and sets a piloted or damaged one apart', () => {
        const ship = (id: number, type: string, extra: object = {}) => ({
            id,
            type,
            location: 'X',
            pilot: null,
            damage: [],
            cargo: null, ...extra
        }) as unknown as ShipProjection;

        const groups = groupShips([ship(5, 'RAIDER'), ship(1, 'VIPER', {pilot: 'LEE_ADAMA'}), ship(2, 'RAIDER'), ship(9, 'BASESTAR', {damage: ['CRITICAL_HIT']}), ship(3, 'VIPER')]);

        expect(groups.map((group) => group.ships.map((one) => one.id))).toEqual([[9], [2, 5], [1], [3]]);
    });

    it('veils the locations of a destroyed Pegasus', () => {
        const destroyed = {...game, pegasus: {...game.pegasus, destroyed: true}} as unknown as GameProjection;
        expect(renderBoard().querySelector('[data-location="AIRLOCK"]')?.classList).not.toContain('destroyed');
        const board = renderBoard(undefined, destroyed);
        expect(board.querySelector('[data-location="AIRLOCK"]')?.classList).toContain('destroyed');
        expect(board.querySelector('[data-location="AIRLOCK"] .destroyed-veil')).not.toBeNull();
        expect(board.querySelector('[data-location="RESEARCH_LAB"]')?.classList).not.toContain('destroyed');
    });

    it('labels a damaged location inside it, under its printed name, and puts the markers on the tracks', () => {
        const board = renderBoard();

        expect(board.querySelector('[data-location="ARMORY"]')?.classList).toContain('damaged');
        // a chip like the Engine Room's "activated", at the bottom of the panel the name is printed in
        const label = board.querySelector('[data-location="ARMORY"] .count.damaged-label')!;
        expect(label.textContent).toBe('damaged');
        const ys = label.querySelector('polygon')!.getAttribute('points')!.split(' ').map((point) => Number(point.split(',')[1]));
        const title = TITLES.ARMORY!;
        expect(Math.min(...ys)).toBeGreaterThan(title.y + title.height / 2 + 15);
        expect(Math.max(...ys)).toBeLessThanOrEqual(title.y + title.height);
        expect(board.querySelector('[data-location="RESEARCH_LAB"] .damaged-label')).toBeNull();
        expect(board.querySelector('[data-track="JumpPreparation"]')?.getAttribute('data-position')).toBe('POSITION_2');
        expect(board.querySelector('[data-track="Pursuit"]')?.getAttribute('data-position')).toBe('START');
        // the highlights' corners are cut, as the spaces' are
        for (const track of ['JumpPreparation', 'Pursuit']) {
            expect(board.querySelector(`[data-track="${track}"] polygon`)?.getAttribute('points')?.split(' ')).toHaveLength(8);
        }
        expect(board.querySelector('[data-track="BoardingParty"] [data-position="POSITION_1"]')?.getAttribute('aria-label')).toBe('1 centurion at Position 1');
    });

    it('warns of critical damage from 4 damaged Galactica locations on, on the hull between the Research Lab, the Armory and the Brig', () => {
        const damaged = (names: string[]) =>
            ({
                ...game,
                galactica: {...game.galactica!, locations: names.map((name) => location(name, [], true))}
            }) as unknown as GameProjection;

        expect(renderBoard().querySelector('.critical-damage')).toBeNull();
        expect(renderBoard(undefined, damaged(['ARMORY', 'BRIG', 'COMMAND'])).querySelector('.critical-damage')).toBeNull();

        const board = renderBoard(undefined, damaged(['ARMORY', 'BRIG', 'COMMAND', 'SICKBAY']));
        const plate = board.querySelector('.critical-damage')!;
        expect(plate.textContent).toContain('CRITICAL DAMAGE');
        // a snapshot doesn't flicker it on
        expect(plate.classList).not.toContain('just-critical');
        // hovering it explains what it warns of
        fireEvent.mouseEnter(board.querySelector('[data-reveal="critical-damage"]')!);
        expect(board.querySelector('.reveal-pop .reveal-title')?.textContent).toBe('Critical damage');
        expect([...board.querySelectorAll('.reveal-pop ol.pile li')].map((line) => line.textContent)).toEqual([
            'Galactica is destroyed when 6 of its locations are damaged, and the Cylons win.',
        ]);
        expect(plate.getAttribute('aria-label')).toBe('Galactica is destroyed when 6 of its locations are damaged, and the Cylons win.');
        const points = plate.querySelector('.rim')!.getAttribute('points')!.split(' ').map((point) => point.split(',').map(Number));
        const [xs, ys] = [points.map(([x]) => x), points.map(([, y]) => y)];
        expect(Math.min(...xs)).toBeGreaterThan(LAYOUT.RESEARCH_LAB.x + LAYOUT.RESEARCH_LAB.width);
        expect(Math.max(...xs)).toBeLessThan(LAYOUT.BRIG.x);
        expect(Math.max(...ys)).toBeLessThan(LAYOUT.ARMORY.y);
        expect(Math.min(...ys)).toBeGreaterThan(LAYOUT.RESEARCH_LAB.y);
    });

    const shielded = (lossReductions: Record<string, number>, number = 3) =>
        ({...game, turn: {...game.turn!, number, lossReductions}}) as unknown as GameProjection;

    it("shields a resource while Preventive Policy makes its next loss this turn smaller, and explains it on hover", () => {
        expect(renderBoard().querySelector('[data-shield]')).toBeNull();
        expect(renderBoard().querySelector('[data-reveal^="loss-reduction"]')).toBeNull();

        const board = renderBoard(undefined, shielded({FOOD: 1}));
        expect([...board.querySelectorAll('[data-shield]')].map((shield) => shield.getAttribute('data-shield'))).toEqual(['food']);
        const shield = board.querySelector('[data-shield="food"]')!;
        // a snapshot doesn't raise it
        expect(shield.classList).not.toContain('raised');
        expect(shield.getAttribute('aria-label')).toBe('The next food loss this turn is reduced by 1.');
        fireEvent.mouseEnter(board.querySelector('[data-reveal="loss-reduction-food"]')!);
        expect(board.querySelector('.reveal-pop .reveal-title')?.textContent).toBe('Preventive Policy');
        expect([...board.querySelectorAll('.reveal-pop ol.pile li')].map((line) => line.textContent)).toEqual([
            'The next food loss this turn is reduced by 1.',
        ]);

        // around its gauge, short of the gauges beside it
        // the outline starts where its left end's arc meets the top, then runs to where the right end's arc begins
        const [, from, top, to, radius] = /^M([\d.]+),([\d.]+) H([\d.]+) A([\d.]+),/.exec(shield.querySelector('.shield-rim')!.getAttribute('d')!)!.map(Number);
        const [left, right, bottom] = [from - radius, to + radius, top + 2 * radius];
        const food = RESOURCES.food;
        expect(left).toBeLessThan(food.x);
        expect(right).toBeGreaterThan(food.x + food.width);
        expect(top).toBeLessThan(food.y);
        expect(bottom).toBeGreaterThan(food.y + food.height);
        expect(left).toBeGreaterThan(RESOURCES.fuel.x + RESOURCES.fuel.width);
        expect(right).toBeLessThan(RESOURCES.morale.x);
    });

    it('fades a shield in with the message that played the card, and out with the loss it took', () => {
        useGameStore.setState({previous: shielded({}), game: shielded({MORALE: 1}), version: 4});
        expect(renderBoard(undefined, shielded({MORALE: 1})).querySelector('[data-shield="morale"]')?.classList).toContain('raised');

        // the loss it took, and the end of the turn in the same message
        const reduced = [{
            kind: 'ScopeLog', scopeId: 9, parentId: 0, actor: 1, event: null, details: null, automatic: false,
            facts: [{kind: 'Shown', fact: {kind: 'GameFact.LossReduced', resource: 'MORALE'}}],
        }] as unknown as LogEntry[];
        useGameStore.setState({previous: shielded({MORALE: 1}), game: shielded({}, 4), fresh: reduced, version: 5});
        const spent = renderBoard(undefined, shielded({}, 4)).querySelector('[data-shield="morale"]')!;
        expect(spent.classList).toContain('spent');
        // nothing left to explain
        expect(spent.getAttribute('aria-hidden')).toBe('true');
        expect(spent.closest('[data-reveal]')).toBeNull();

        // the end of the turn takes it down unspent
        useGameStore.setState({previous: shielded({MORALE: 1}), game: shielded({}, 4), fresh: [], version: 6});
        expect(renderBoard(undefined, shielded({}, 4)).querySelector('[data-shield]')).toBeNull();
    });

    it('fades a moved marker out of the space it left and in on its new one, with no delta', () => {
        const before = {...game, galactica: {...game.galactica!, jumpPreparation: 'POSITION_1'}} as GameProjection;
        useGameStore.setState({previous: before, game, version: 4});
        const board = renderBoard();

        const leaving = board.querySelectorAll('.marker-jumppreparation.leaving');
        expect(leaving).toHaveLength(1);
        // the space it left, and nothing that names it
        expect(leaving[0].getAttribute('data-track')).toBeNull();
        expect(leaving[0].querySelector('polygon')?.getAttribute('points')).toContain(`${JUMP_TRACK.POSITION_1.x - 44},`);
        expect(board.querySelector('[data-track="JumpPreparation"]')?.classList).toContain('arriving');
        expect(board.querySelector('[data-track="JumpPreparation"]')?.getAttribute('data-position')).toBe('POSITION_2');
        // the track that didn't move stays as it is
        expect(board.querySelector('[data-track="Pursuit"]')?.classList).not.toContain('arriving');
        expect(board.querySelector('.marker-pursuit.leaving')).toBeNull();
        expect(board.querySelector('.deltas text')).toBeNull();
    });

    it('lays the Boarding Party track over the hull only while a centurion is on it', () => {
        const boarded = renderBoard().querySelector('[data-track="BoardingParty"]')!;
        const track = boarded.querySelector(':scope > image')!;
        expect(track.getAttribute('href')).toContain('boarding-party');
        expect(Number(track.getAttribute('x'))).toBe(SLOTS.boardingParty.x);
        expect(boarded.querySelector('[data-position="POSITION_1"] .centurion')).not.toBeNull();

        const nobody = {...game, galactica: {...game.galactica!, boardingParty: []}} as GameProjection;
        expect(renderBoard(undefined, nobody).querySelector('[data-track="BoardingParty"]')).toBeNull();
    });

    it('shows each resource as its icon and a strip of pips, its name and amount only on hover', () => {
        const board = renderBoard();
        const gauge = (resource: string) => board.querySelector(`[data-gauge="${resource}"]`)!;

        expect(['fuel', 'food', 'morale', 'population'].map((resource) => gauge(resource).getAttribute('aria-label'))).toEqual(['Fuel: 8', 'Food: 7', 'Morale: 9', 'Population: 12']);
        // no amount on the board: the chip that hovering shows names it
        expect(board.querySelector('.resources text:not(.resource-tip text)')).toBeNull();
        expect(gauge('population').querySelector('.resource-tip')?.textContent).toBe('Population 12');
        // the icon first, the strip after it, all inside the resource's box, which takes the hover
        const icon = gauge('fuel').querySelector('image')!;
        expect(icon.getAttribute('href')).toBe(resourceIconUrl('fuel'));
        expect(Number(icon.getAttribute('x'))).toBe(RESOURCES.fuel.x);
        const hit = gauge('fuel').querySelector('.gauge-hit')!;
        expect(Number(hit.getAttribute('x'))).toBeLessThan(RESOURCES.fuel.x);
        expect(Number(hit.getAttribute('width'))).toBeGreaterThan(RESOURCES.fuel.width);
    });

    it("labels each section where the picture printed its name, a chip like the mats' badges", () => {
        const board = renderBoard();
        for (const key of Object.keys(BOARD_LABELS) as BoardLabel[]) {
            const label = board.querySelector(`[data-label="${key}"]`)!;
            const text = label.querySelector('text')!;
            expect(text.textContent).toBe(BOARD_LABELS[key].toUpperCase());
            expect([Number(text.getAttribute('x')), text.getAttribute('text-anchor')]).toEqual([LABELS[key].x, 'middle']);
            // the chip's top left and bottom right corners are cut, as a badge's
            expect(label.querySelector('polygon')!.getAttribute('points')!.split(' ')).toHaveLength(6);
        }
        expect(board.querySelectorAll('.section-label')).toHaveLength(8);
    });

    it('puts a pip after each icon for each of its amount, the red zone in red', () => {
        const board = renderBoard();
        const resources = ['fuel', 'food', 'morale', 'population'];
        const pips = (resource: string, root: ParentNode = board) => [...root.querySelectorAll(`[data-gauge="${resource}"] .pip`)];
        const red = (resource: string, root?: ParentNode) => pips(resource, root).filter((pip) => pip.classList.contains('red')).length;

        expect(resources.map((resource) => pips(resource).length)).toEqual([8, 7, 9, 12]);
        // the red zones: fuel and food under 5, morale under 6, population under 7, the first pips
        expect(resources.map((resource) => red(resource))).toEqual([4, 4, 5, 6]);
        expect(pips('fuel').map((pip) => pip.classList.contains('red'))).toEqual([...Array(4).fill(true), ...Array(4).fill(false)]);
        expect(board.querySelectorAll('.gauge.red-zone')).toHaveLength(0);

        // at 15 the pips run after the icon to the end of the box, as high as it, and the box is level with the jump track
        const full = renderBoard(undefined, {...game, galactica: {...game.galactica!, food: 15}} as GameProjection);
        const [first, last] = [pips('food', full)[0], pips('food', full)[14]];
        const box = RESOURCES.food;
        expect(pips('food', full)).toHaveLength(15);
        expect(Number(first.getAttribute('x'))).toBeGreaterThan(box.x + 50);
        expect(Number(last.getAttribute('x')) + Number(last.getAttribute('width'))).toBeCloseTo(box.x + box.width);
        expect([Number(first.getAttribute('y')), Number(first.getAttribute('height'))]).toEqual([box.y, box.height]);
        expect(box.y + box.height / 2).toBeCloseTo(JUMP_TRACK.START.y, 0);

        // none at 0, and the box still takes the hover
        const low = renderBoard(undefined, {
            ...game,
            galactica: {...game.galactica!, fuel: 0, food: 5, morale: 5, population: 7}
        } as GameProjection);
        expect([...low.querySelectorAll('.gauge.red-zone')].map((gauge) => gauge.getAttribute('data-gauge'))).toEqual(['fuel', 'morale']);
        expect(pips('fuel', low)).toHaveLength(0);
        expect(low.querySelector('[data-gauge="fuel"] .gauge-hit')).not.toBeNull();
        expect(red('morale', low)).toBe(5);
    });

    it('stands each character on the printed name of his location, reaching a little into its picture', () => {
        const board = renderBoard();

        const standee = board.querySelector('[data-location="RESEARCH_LAB"] [data-character="WILLIAM_ADAMA"] image')!;
        const top = Number(standee.getAttribute('y'));
        const bottom = top + Number(standee.getAttribute('height'));
        const title = TITLES.RESEARCH_LAB!;
        expect(bottom).toBeLessThanOrEqual(title.y + title.height);
        expect(bottom).toBeGreaterThan(title.y + title.height - 10);
        expect(top).toBeLessThan(title.y);
        expect(top).toBeGreaterThan(title.y - 30);
    });

    it("shows the player's mat beside the character sheet while the pointer is on his standee", () => {
        const board = renderBoard();

        const standee = board.querySelector('[data-character="WILLIAM_ADAMA"] image')!;
        fireEvent.mouseEnter(standee);
        expect(document.querySelector('.art-preview .art-preview-art img')?.getAttribute('src')).toContain('WILLIAM_ADAMA');
        expect(document.querySelector('.art-preview .art-preview-extra .mat')?.getAttribute('data-player')).toBe('1');
        fireEvent.mouseLeave(standee);
        expect(document.querySelector('.art-preview')).toBeNull();
    });

    it("shows a location's card on hover, and a standee's character sheet over it", () => {
        const board = renderBoard();

        fireEvent.mouseEnter(board.querySelector('[data-location="RESEARCH_LAB"] .location-hotspot')!);
        expect(usePreview.getState().art?.key).toBe('Location/RESEARCH_LAB');
        fireEvent.mouseEnter(board.querySelector('[data-character="WILLIAM_ADAMA"] image')!);
        expect(usePreview.getState().art?.key).toBe('Character/player-token/WILLIAM_ADAMA');
        // a space area has no card
        expect(board.querySelector('[data-location="GALACTICA_SPACE_4_OCLOCK"] .location-hotspot')).toBeNull();
    });

    it('lays the ships of a space area in its middle, the Cylon ships first, each as large as anywhere on its board', () => {
        const ships = [
            inSpace(20, 'VIPER', 'GALACTICA_SPACE_8_OCLOCK'),
            inSpace(21, 'RAIDER', 'GALACTICA_SPACE_8_OCLOCK'),
            inSpace(22, 'RAIDER', 'GALACTICA_SPACE_8_OCLOCK'),
            inSpace(23, 'BASESTAR', 'GALACTICA_SPACE_8_OCLOCK'),
            inSpace(24, 'VIPER', 'GALACTICA_SPACE_12_OCLOCK'),
            inSpace(25, 'VIPER', 'GALACTICA_SPACE_10_OCLOCK'),
            inSpace(26, 'VIPER', 'GALACTICA_SPACE_10_OCLOCK'),
        ];
        const board = renderBoard(undefined, {
            ...game,
            galactica: {...game.galactica!, ships}
        } as unknown as GameProjection);

        // a lone ship in the middle of its area
        const [lone] = shipBoxes(board, 'GALACTICA_SPACE_12_OCLOCK');
        const area = LAYOUT.GALACTICA_SPACE_12_OCLOCK;
        expect(lone.x + lone.width / 2).toBeCloseTo(area.x + area.width / 2);
        expect(lone.y + lone.height / 2).toBeCloseTo(area.y + area.height / 2);

        // the basestar first, then the raiders, then the viper, the whole lot in the middle of the strip
        const strip = shipBoxes(board, 'GALACTICA_SPACE_8_OCLOCK');
        expect(strip.map((ship) => ship.art)).toEqual(['ShipType/BASESTAR', 'ShipType/RAIDER', 'ShipType/RAIDER', 'ShipType/VIPER']);
        const narrow = LAYOUT.GALACTICA_SPACE_8_OCLOCK;
        const top = Math.min(...strip.map((ship) => ship.y));
        const bottom = Math.max(...strip.map((ship) => ship.y + ship.height));
        expect(Math.abs(top - narrow.y - (narrow.y + narrow.height - bottom))).toBeLessThan(10);

        // a viper is as large alone, piled and in the narrow strip
        const vipers = [lone, ...shipBoxes(board, 'GALACTICA_SPACE_10_OCLOCK'), strip[3]];
        expect(new Set(vipers.map((viper) => `${viper.width}×${viper.height}`)).size).toBe(1);
    });

    it("shows what a civilian ship costs when destroyed on hover, in a sandbox, and to no one who doesn't know it", () => {
        const civilians = [
            inSpace(30, 'CIVILIAN', 'GALACTICA_SPACE_2_OCLOCK', {cargo: {fuel: 1, morale: 0, population: 1}}),
            inSpace(31, 'CIVILIAN', 'GALACTICA_SPACE_2_OCLOCK', {cargo: {fuel: 0, morale: 0, population: 0}}),
        ];
        const sandbox = {
            ...game,
            viewer: {kind: 'Sandbox'},
            galactica: {...game.galactica!, ships: civilians}
        } as unknown as GameProjection;
        const board = renderBoard(undefined, sandbox, true);

        fireEvent.mouseEnter(board.querySelector('[data-reveal="cargo-30"]')!);
        const lines = [...document.querySelectorAll('.reveal-pop ol.pile li')];
        expect(lines.map((line) => line.textContent)).toEqual(['11Civilian ship #30', 'nothingCivilian ship #31']);
        expect(lines[0].querySelector('.cargo')?.getAttribute('aria-label')).toBe('1 population, 1 fuel');
        expect([...lines[0].querySelectorAll('.cargo-amount')].map((amount) => amount.getAttribute('data-resource'))).toEqual(['population', 'fuel']);
        expect(document.querySelector('.reveal-pop .sandbox-only')).not.toBeNull();
        // the ship's own preview would cover the costs
        fireEvent.mouseEnter(board.querySelector('[data-ship="30"] image')!);
        expect(usePreview.getState().art).toBeNull();

        const unknown = {
            ...game,
            galactica: {...game.galactica!, ships: civilians.map((ship) => ({...ship, cargo: null}))}
        } as unknown as GameProjection;
        const table = renderBoard(undefined, unknown);
        expect(table.querySelector('[data-reveal^="cargo-"]')).toBeNull();
    });

    it("names what each civilian ship of the deck costs, in a sandbox's draw pile", () => {
        const decks = [{
            deck: 'CIVILIAN_SHIPS',
            size: 1,
            discardSize: 0,
            discarded: [],
            drawPile: [{id: 40, fuelCost: 0, moraleCost: 1, populationCost: 1}]
        }];
        const board = renderBoard(undefined, {
            ...game,
            viewer: {kind: 'Sandbox'},
            decks
        } as unknown as GameProjection, true);

        fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CIVILIAN_SHIPS"]')!);
        expect(document.querySelector('.reveal-pop ol.pile li')?.textContent).toBe('Civilian ship #40: 1 population, 1 morale');
    });

    it('tucks the destinations under the objective, the later ones under the earlier, each showing a strip', () => {
        const board = renderBoard(undefined, {
            ...game,
            destinations: ['ALGAE_PLANET', 'ASTEROID_FIELD'],
            distanceTraveled: 3
        } as GameProjection);

        const cards = [...board.querySelectorAll('[data-slot="distance"] image')].map((card) => card.getAttribute('data-art'));
        // drawn first is lowest: the second destination, then the first, then the objective on top
        expect(cards).toEqual(['DestinationCard/ASTEROID_FIELD', 'DestinationCard/ALGAE_PLANET', 'ObjectiveCard/KOBOL']);
        const bottoms = [...board.querySelectorAll('[data-slot="distance"] image')].map((card) => Number(card.getAttribute('y')) + Number(card.getAttribute('height')));
        expect(bottoms).toEqual([DISTANCE.y + 2 * DISTANCE.strip, DISTANCE.y + DISTANCE.strip, SLOTS.objective.y + SLOTS.objective.height]);
        expect(board.querySelector('[data-slot="distance"]')?.textContent).toContain('3');
    });

    it('lays every deck of the rack in its printed slot with its back, a Skill Card back where the deck has none', () => {
        const decks = ['DESTINATION', 'CRISIS', 'DESTINY', 'TACTICS'].map((deck) => ({
            deck,
            size: 5,
            discardSize: 0,
            discarded: [],
            drawPile: null
        }));
        const board = renderBoard(undefined, {...game, decks} as unknown as GameProjection);

        expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"] [data-art="DeckId/CRISIS"]')).not.toBeNull();
        expect(board.querySelector('[data-deck="DESTINY"][data-pile="draw"] [data-art="Back/SKILL"]')).not.toBeNull();
        const tactics = board.querySelector('[data-deck="TACTICS"][data-pile="draw"] .hotspot')!;
        expect([tactics.getAttribute('x'), tactics.getAttribute('y')]).toEqual([String(DECK_SLOTS.TACTICS.x), String(DECK_SLOTS.TACTICS.y)]);
    });

    it('keeps the ships of a space area off its printed launch tube', () => {
        const vipers = Array.from({length: 3}, (_, index) => ({
            id: 10 + index,
            type: 'VIPER',
            location: 'GALACTICA_SPACE_4_OCLOCK',
            pilot: ['LEE_ADAMA', 'KARA_STARBUCK_THRACE', 'KARL_HELO_AGATHON'][index],
            damage: [],
            cargo: null
        }));
        const board = renderBoard(undefined, {
            ...game,
            galactica: {...game.galactica!, ships: vipers}
        } as unknown as GameProjection);

        const tube = LAUNCH_TUBES.GALACTICA_SPACE_4_OCLOCK!;
        for (const viper of board.querySelectorAll('[data-location="GALACTICA_SPACE_4_OCLOCK"] [data-art="ShipType/VIPER"]')) {
            const left = Number(viper.getAttribute('x'));
            const right = left + Number(viper.getAttribute('width'));
            expect(right <= tube.x || left >= tube.x + tube.width).toBe(true);
        }
    });

    it('lays the decks in the rack with their counts and the top of the discard pile over the deck, clear of the next', () => {
        const board = renderBoard();

        expect(board.querySelector('[data-deck="CRISIS"][data-pile="draw"]')?.textContent).toContain('70');
        const discard = board.querySelector('[data-deck="CRISIS"][data-pile="discard"] [data-art="CrisisCard/WATER_SABOTAGED"]')!;
        const right = Number(discard.getAttribute('x')) + Number(discard.getAttribute('width'));
        expect(right).toBeLessThan(DECK_SLOTS.SUPER_CRISIS.x);
        expect(board.querySelector('[data-deck="POLITICS"][data-pile="discard"] [data-art="SkillCardType/RED_TAPE_0"]')).not.toBeNull();
        expect(board.querySelector('[data-slot="distance"] [data-art="DestinationCard/ALGAE_PLANET"]')).not.toBeNull();
    });

    it("leaves the card being resolved, its skill check and the die to the turn panel, and the nukes to the Admiral's mat", () => {
        const board = renderBoard();

        // the turn's Crisis Card lies only in its discard pile, the skill check's card nowhere
        expect(board.querySelectorAll('[data-art="CrisisCard/WATER_SABOTAGED"]')).toHaveLength(1);
        expect(board.querySelector('[data-art="CrisisCard/CYLON_SCREENINGS"]')).toBeNull();
        expect(board.querySelector('[data-pile="skill-check"]')).toBeNull();
        expect(board.querySelector('[data-art^="Die/"]')).toBeNull();
        expect(board.querySelector('[data-art="Token/NUKE"]')).toBeNull();
    });

    it('lays the Crisis Cards kept in play by the jump track, the civilian ships in the ship reserves, and no damage decks', () => {
        const decks = ['CIVILIAN_SHIPS', 'GALACTICA_DAMAGE', 'PEGASUS_DAMAGE', 'BASESTAR_DAMAGE'].map((deck) => ({
            deck,
            size: 10,
            discardSize: 0,
            discarded: [],
            drawPile: null
        }));
        const board = renderBoard(undefined, {
            ...game,
            decks,
            crisisCardsInPlay: ['LEGENDARY_DISCOVERY']
        } as unknown as GameProjection);

        const kept = board.querySelector('[data-slot="in-play"] image')!;
        expect(kept.getAttribute('data-art')).toBe('CrisisCard/LEGENDARY_DISCOVERY');
        expect(Number(kept.getAttribute('x'))).toBeGreaterThan(JUMP_TRACK.MINUS_1.x);
        const civilians = board.querySelector('[data-slot="reserves"] [data-deck="CIVILIAN_SHIPS"]')!;
        expect(civilians.querySelectorAll('[data-art="ShipType/CIVILIAN"]')).toHaveLength(3);
        expect(civilians.textContent).toContain('10');
        const ships = [...civilians.querySelectorAll('image')].map((ship) => Number(ship.getAttribute('x')) + Number(ship.getAttribute('width')));
        expect(Math.max(...ships)).toBeLessThanOrEqual(SLOTS.shipReserves.x + SLOTS.shipReserves.width);
        expect(board.querySelector('[data-deck$="_DAMAGE"]')).toBeNull();
    });

    it('shows a face-up discard pile on hover, top first, but never a draw pile at a live table', () => {
        const board = renderBoard();

        fireEvent.mouseEnter(board.querySelector('[data-reveal="discard-CRISIS"]')!);
        const cards = [...document.querySelectorAll('.reveal-pop ol.pile li')];
        expect(cards.map((card) => card.querySelector('img')?.getAttribute('alt'))).toEqual(['Water Sabotaged', 'Riots Jump']);
        // no name under the images
        expect(cards.map((card) => card.textContent)).toEqual(['', '']);
        // the cards are large enough to read, so they have no preview of their own
        fireEvent.mouseEnter(document.querySelector('.reveal-pop ol.pile.cards li .art')!);
        expect(usePreview.getState().art).toBeNull();

        // the deck shows its discard pile too, and nothing of the draw pile
        fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CRISIS"]')!);
        expect([...document.querySelectorAll('.reveal-section-title')].map((title) => title.textContent)).toEqual(['Crisis: discard pile, top first · 2']);
        expect(document.querySelector('.reveal-pop .sandbox-only')).toBeNull();
    });

    it('shows in a sandbox what the table hides, on hover: the draw pile top first', () => {
        const sandbox = {
            ...game,
            viewer: {kind: 'Sandbox'},
            decks: [{
                deck: 'CRISIS',
                size: 2,
                discardSize: 0,
                discarded: [],
                drawPile: ['RIOTS_NO_JUMP', 'FOOD_SHORTAGE']
            }],
        } as unknown as GameProjection;
        const board = renderBoard(undefined, sandbox, true);

        fireEvent.mouseEnter(board.querySelector('[data-reveal="deck-CRISIS"]')!);
        expect(document.querySelector('.reveal-pop ol.pile li img')?.getAttribute('alt')).toBe('Riots No Jump');
        expect(document.querySelector('.reveal-pop .sandbox-only')).not.toBeNull();
    });

    it('lights up the locations, ships and standees an input can be answered with, and the Act chip', () => {
        const move = {
            option: null,
            followup: null,
            event: {kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null},
            submits: true,
            pick: null
        } as Move;
        const offer: Offer = {
            targets: new Map([
                ['location:ARMORY', [move]],
                ['ship:3', [move, move]],
                ['player:1', [move]],
            ]),
            acts: new Map([['RESEARCH_LAB', {kind: 'ResearchLabEvent', moves: [move]}]]),
            picked: new Set(),
        };
        const onTarget = vi.fn();
        const board = renderBoard(offer, game, false, onTarget);

        const armory = board.querySelector('[data-location="ARMORY"]')!;
        expect(armory.getAttribute('role')).toBe('button');
        expect(armory.querySelector('title')?.textContent).toBe('Move to the Armory');
        expect(board.querySelector('[data-location="RESEARCH_LAB"]')?.getAttribute('role')).toBeNull();
        fireEvent.click(armory);
        expect(onTarget).toHaveBeenLastCalledWith({type: 'location', location: 'ARMORY'}, expect.anything());

        const viper = board.querySelector('[data-ship="3"]')!;
        expect(viper.classList).toContain('target');
        expect(viper.querySelector('title')?.textContent).toBe('2 answers');
        fireEvent.click(viper);
        expect(onTarget).toHaveBeenLastCalledWith({type: 'ship', id: 3}, expect.anything());

        fireEvent.click(board.querySelector('[data-character="WILLIAM_ADAMA"]')!);
        expect(onTarget).toHaveBeenLastCalledWith({type: 'player', playerNumber: 1}, expect.anything());

        fireEvent.click(board.querySelector('[data-location="RESEARCH_LAB"] .act')!);
        expect(onTarget).toHaveBeenLastCalledWith({type: 'kind', kind: 'ResearchLabEvent'}, expect.anything());
    });

    it('puts a nickname, or the last name, on a token', () => {
        expect(shortName('Kara "Starbuck" Thrace')).toBe('Starbuck');
        expect(shortName('William Adama')).toBe('Adama');
        expect(shortName('Gaius Baltar (alternate)')).toBe('Baltar');
    });
});

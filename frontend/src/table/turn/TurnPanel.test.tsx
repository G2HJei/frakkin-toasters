import { fireEvent, render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { TurnPanel } from './TurnPanel';
import { RevealLayer, useReveal } from '../reveal';
import { usePreview } from '../../art/Art';
import { Table, tableContext } from '../tableContext';
import type { GameProjection } from '../../wire/types';

const players = [
  { number: 1, character: 'WILLIAM_ADAMA' },
  { number: 2, character: 'LAURA_ROSLIN' },
];

function renderPanel(turn: { step: string; currentPlayer: number; actingPlayer: number; crisisCards?: string[] }, more: object = {}, sandbox = false) {
  const game = { objective: 'KOBOL', distanceTraveled: 0, nukes: 1, players, turn: { number: 4, ...turn }, ...more } as unknown as GameProjection;
  return render(
    <Table.Provider value={tableContext(game, sandbox, null)}>
      <TurnPanel name="Sandbox" connection="open" />
      <RevealLayer />
    </Table.Provider>,
  ).container;
}

describe('the turn', () => {
  // each line with its text and whether it names the character acting now (red) or not (amber)
  function turnLines(panel: HTMLElement) {
    return [...panel.querySelectorAll('h2 .turn-line')].map((line) => [line.textContent, line.classList.contains('acting')]);
  }

  it("names the current character after the turn's number, in red while he acts", () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });

    expect(turnLines(panel)).toEqual([['Turn 4William Adama', true]]);
  });

  it('names the acting character in red beside the current one, in amber, when it is another', () => {
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 2 });

    expect(turnLines(panel)).toEqual([
      ['Turn 4William Adama', false],
      ['ActingLaura Roslin', true],
    ]);
  });

  it('names an alternate character without his version', () => {
    const alternates = [
      { number: 1, character: 'TOM_ZAREK_ALT' },
      { number: 2, character: 'KARL_HELO_AGATHON_ALT' },
    ];
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 2 }, { players: alternates });

    expect(turnLines(panel)).toEqual([
      ['Turn 4Tom Zarek', false],
      ['ActingKarl "Helo" Agathon', true],
    ]);
  });

  it('shows the steps as icons, the step the turn is at named beside its icon', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });
    const steps = [...panel.querySelectorAll('.steps li')];

    expect(steps.every((step) => step.querySelector('svg.step-icon'))).toBe(true);
    expect(steps.map((step) => step.className)).toEqual(['done', 'done', 'done', 'now', '', '', '']);
    expect(panel.querySelector('[aria-current="step"]')?.textContent).toBe('Crisis');
  });

  it('lists every step with its icon in one popover on hover, whatever step the turn is at', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });

    fireEvent.mouseEnter(panel.querySelector('[data-reveal="turn-steps"]')!);
    const items = [...document.querySelectorAll('.reveal-pop ol.pile.icons li')];
    expect(items.map((item) => item.textContent)).toEqual([
      'Receive Skills',
      'Movement',
      'Action',
      'Crisis',
      'Activate Cylon Ships',
      'Prepare for Jump',
      'Discard',
    ]);
    expect(items.every((item) => item.querySelector('svg.step-icon'))).toBe(true);
    expect(document.querySelector('.reveal-pop .reveal-section-title')).toBeNull();
    expect(document.querySelector('.reveal-pop .now, .reveal-pop [aria-current]')).toBeNull();
    useReveal.getState().hide();
  });

  it("shows a Crisis Card at full size with its skill check beside it, without the card again and without a meter", () => {
    const skillCheck = {
      type: 'CRIPPLED_RAIDER',
      targetPlayer: null,
      difficulty: 10,
      playedBefore: [{ player: 2, cards: { count: 1, cards: null } }],
      cards: { count: 3, cards: null },
      totalStrength: null,
    };
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1, crisisCards: ['CRIPPLED_RAIDER'] }, { skillCheck });
    const cards = panel.querySelector('section.turn [aria-label="cards of the turn"]')!;
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;

    expect([...cards.querySelectorAll('.card-tile')].map((tile) => tile.className)).toEqual(['tile card-tile size-full crisis']);
    expect(cards.nextElementSibling?.contains(check)).toBe(true);
    expect(check.querySelector('.block-text')?.textContent).toBe('Difficulty 10');
    expect(check.querySelector('.card-tile')).toBeNull();
    expect([...check.querySelectorAll('.row-name')].map((row) => row.textContent)).toEqual(['Played before by Laura Roslin', 'Face down']);
    expect(panel.querySelector('.meter-bar')).toBeNull();
  });

  it('shows the Crisis Cards without a label or their names, which the step and the cards show', () => {
    const panel = renderPanel({ step: 'ACTIVATE_CYLON_SHIPS', currentPlayer: 1, actingPlayer: 1, crisisCards: ['FULFILLER_OF_PROPHECY', 'CRIPPLED_RAIDER'] });
    const cards = panel.querySelector('section.turn [aria-label="cards of the turn"]')!;

    expect([...cards.querySelectorAll('.card-tile img')].map((image) => image.getAttribute('alt'))).toEqual(['Fulfiller of Prophecy', 'Crippled Raider']);
    expect(panel.querySelector('section.turn small')).toBeNull();
    expect(panel.textContent).not.toContain('Fulfiller of Prophecy');
    expect(panel.textContent).not.toContain('Crippled Raider');
  });

  it('opens no preview of a Crisis Card, which shows at full size', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1, crisisCards: ['CRIPPLED_RAIDER'] });

    fireEvent.mouseEnter(panel.querySelector('[aria-label="cards of the turn"] img')!);
    expect(usePreview.getState().art).toBeNull();
  });

  it('shows the cards an Investigative Committee put face up, with who played them', () => {
    const skillCheck = {
      type: 'ADMIRALS_QUARTERS',
      targetPlayer: 2,
      difficulty: 7,
      shownCards: [{ player: 2, cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }] }],
      cards: { count: 3, cards: null },
      totalStrength: null,
    };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;
    const rows = [...check.querySelectorAll('.row')];

    expect(rows.map((row) => row.querySelector('.row-name')?.textContent)).toEqual(['Face up from Laura Roslin', 'Face down']);
    expect(rows[0].querySelectorAll('.card-tile')).toHaveLength(1);
  });

  // a skill check with 2 cards Laura Roslin played before it, which a sandbox sees whether they are revealed or not
  function playedBeforeCheck(playedBeforeRevealed: boolean) {
    return {
      type: 'ADMIRALS_QUARTERS',
      targetPlayer: 2,
      difficulty: 7,
      playedBefore: [
        {
          player: 2,
          cards: {
            count: 2,
            cards: [
              { value: 3, type: 'INVESTIGATIVE_COMMITTEE' },
              { value: 3, type: 'SCIENTIFIC_RESEARCH' },
            ],
          },
        },
      ],
      playedBeforeRevealed,
      cards: { count: 0, cards: null },
      totalStrength: null,
    };
  }

  // the "Before the check" row: its face-up cards and its backs
  function beforeTheCheck(panel: HTMLElement) {
    const row = panel.querySelector('section.turn [aria-label="skill check"] .row')!;
    expect(row.querySelector('.row-name')?.textContent).toBe('Played before by Laura Roslin');
    return { faceUp: row.querySelectorAll('.card-tile').length, backs: row.querySelectorAll('.card-back').length };
  }

  it('keeps the cards played before the check face down in a sandbox until they are revealed', () => {
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck: playedBeforeCheck(false) }, true);

    // one back with how many: face down, the cards don't show their colors
    expect(beforeTheCheck(panel)).toEqual({ faceUp: 0, backs: 1 });
  });

  it('shows the cards played before the check face up once they are revealed, in a sandbox too', () => {
    const live = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck: playedBeforeCheck(true) });
    expect(beforeTheCheck(live)).toEqual({ faceUp: 2, backs: 0 });
    live.remove();

    const sandbox = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck: playedBeforeCheck(true) }, true);
    expect(beforeTheCheck(sandbox)).toEqual({ faceUp: 2, backs: 0 });
  });

  // the images of the cards of the turn, by their names
  function turnCards(panel: HTMLElement) {
    return [...panel.querySelectorAll('section.turn [aria-label="cards of the turn"] .card-tile.size-full img')].map((image) => image.getAttribute('alt'));
  }

  it("shows a location's skill check beside its location at full size, without its name", () => {
    const skillCheck = { type: 'ADMIRALS_QUARTERS', targetPlayer: 2, difficulty: 7, cards: { count: 0, cards: null }, totalStrength: 8, result: 'PASS' };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;

    expect(turnCards(panel)).toEqual(["Admiral's Quarters"]);
    // a location isn't a card scan to trim
    expect(panel.querySelector('section.turn .card-tile.crisis')).toBeNull();
    expect(check.querySelector('.card-tile')).toBeNull();
    expect([...check.querySelectorAll('.block-text > *')].map((line) => line.textContent)).toEqual(['Targets Laura Roslin', 'Difficulty 7', 'Strength 8 · passes']);
  });

  it("shows a Super Crisis Card's skill check beside the card at full size", () => {
    const skillCheck = { type: 'CYLON_INTRUDERS', targetPlayer: null, difficulty: 18, cards: { count: 0, cards: null }, totalStrength: null };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });

    expect(turnCards(panel)).toEqual(['Cylon Intruders']);
    // trimmed of its card stock like a Crisis Card
    expect(panel.querySelector('section.turn .card-tile.size-full')?.className).toBe('tile card-tile size-full crisis');
  });

  it("shows a skill check about none of the turn's Crisis Cards with its card after them", () => {
    const skillCheck = { type: 'BRIG', targetPlayer: 1, difficulty: 7, cards: { count: 0, cards: null }, totalStrength: null };
    const panel = renderPanel({ step: 'ACTIVATE_CYLON_SHIPS', currentPlayer: 1, actingPlayer: 1, crisisCards: ['CRIPPLED_RAIDER'] }, { skillCheck });

    expect(turnCards(panel)).toEqual(['Crippled Raider', 'Brig']);
  });

  it('says when the Politics Cards count positive', () => {
    const skillCheck = { type: 'BRIG', targetPlayer: 1, difficulty: 7, cards: { count: 0, cards: null }, totalStrength: null, politicsCountPositive: true };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;

    expect([...check.querySelectorAll('.block-text > *')].map((line) => line.textContent)).toContain('Politics counts positive');
  });

  it('leaves the objective to the board', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });

    expect(panel.textContent).not.toContain('Objective');
    expect(panel.querySelector('.people')).toBeNull();
  });
});

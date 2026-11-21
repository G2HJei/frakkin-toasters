import { fireEvent, render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { TurnPanel } from './TurnPanel';
import { RevealLayer, useReveal } from '../reveal';
import { Table, tableContext } from '../tableContext';
import type { GameProjection } from '../../wire/types';

const players = [
  { number: 1, character: 'WILLIAM_ADAMA' },
  { number: 2, character: 'LAURA_ROSLIN' },
];

function renderPanel(turn: { step: string; currentPlayer: number; actingPlayer: number; crisisCards?: string[] }, more: object = {}) {
  const game = { objective: 'KOBOL', distanceTraveled: 0, nukes: 1, players, turn: { number: 4, ...turn }, ...more } as unknown as GameProjection;
  return render(
    <Table.Provider value={tableContext(game, false, null)}>
      <TurnPanel name="Sandbox" connection="open" />
      <RevealLayer />
    </Table.Provider>,
  ).container;
}

describe('the turn', () => {
  it('names the current character after the turn\'s number', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });

    expect(panel.querySelector('h2')?.textContent).toBe('Turn 4 · William Adama');
  });

  it('names the acting character after the current one when it is another', () => {
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 2 });

    expect(panel.querySelector('h2')?.textContent).toBe('Turn 4 · William Adama · acting Laura Roslin');
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

  it("shows a Crisis Card's skill check under the card, without the card again and without a meter", () => {
    const skillCheck = {
      type: 'CRIPPLED_RAIDER',
      targetPlayer: null,
      difficulty: 10,
      playedBefore: [{ player: 2, cards: { count: 1, colors: { TACTICS: 1 }, cards: null } }],
      cards: { count: 3, colors: { POLITICS: 1, TACTICS: 1, ENGINEERING: 1 }, cards: null },
      totalStrength: null,
    };
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1, crisisCards: ['CRIPPLED_RAIDER'] }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;

    expect(check.previousElementSibling?.querySelector('small')?.textContent).toBe('Crisis');
    expect(check.querySelector('.block-text')?.textContent).toBe('Difficulty 10');
    expect(check.querySelector('.card-tile')).toBeNull();
    expect([...check.querySelectorAll('.row-name')].map((row) => row.textContent)).toEqual(['Before the check: Laura Roslin', 'Face down']);
    expect(panel.querySelector('.meter-bar')).toBeNull();
    expect(panel.querySelector('section.turn .block-text')?.textContent).toBe('Crippled Raider');
  });

  it('shows the cards an Investigative Committee put face up, with who played them', () => {
    const skillCheck = {
      type: 'ADMIRALS_QUARTERS',
      targetPlayer: 2,
      difficulty: 7,
      shownCards: [{ player: 2, cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }] }],
      cards: { count: 3, colors: { LEADERSHIP: 1, TACTICS: 2 }, cards: null },
      totalStrength: null,
    };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;
    const rows = [...check.querySelectorAll('.row')];

    expect(rows.map((row) => row.querySelector('.row-name')?.textContent)).toEqual(['Face up: Laura Roslin', 'Face down']);
    expect(rows[0].querySelectorAll('.card-tile')).toHaveLength(1);
  });

  it("shows a location's skill check with its location", () => {
    const skillCheck = { type: 'ADMIRALS_QUARTERS', targetPlayer: 2, difficulty: 7, cards: { count: 0, colors: null, cards: null }, totalStrength: 8, result: 'PASS' };
    const panel = renderPanel({ step: 'ACTION', currentPlayer: 1, actingPlayer: 1 }, { skillCheck });
    const check = panel.querySelector('section.turn [aria-label="skill check"]')!;

    expect(check.querySelector('.card-tile')).not.toBeNull();
    expect([...check.querySelectorAll('.block-text > *')].map((line) => line.textContent)).toEqual([
      'Admirals Quarters',
      'about Laura Roslin',
      'Difficulty 7',
      'Strength 8 · Pass',
    ]);
  });

  it('leaves the objective to the board', () => {
    const panel = renderPanel({ step: 'CRISIS', currentPlayer: 1, actingPlayer: 1 });

    expect(panel.textContent).not.toContain('Objective');
    expect(panel.querySelector('.people')).toBeNull();
  });
});

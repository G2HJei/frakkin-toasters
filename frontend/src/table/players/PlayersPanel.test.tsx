import { fireEvent, render } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { PlayersPanel } from './PlayersPanel';
import { RevealLayer, useReveal } from '../reveal';
import { Table, tableContext } from '../tableContext';
import type { GameProjection } from '../../wire/types';

// Player 1's view: his own cards face up, player 2's only counted, by color and by deck.
const game = {
  viewer: { kind: 'PlayerViewer', playerNumber: 1 },
  titles: { president: 'LAURA_ROSLIN', admiral: 'WILLIAM_ADAMA', cag: null },
  turn: { currentPlayer: 1, actingPlayer: 1 },
  presidentHand: { count: 2, revealed: [], cards: null },
  players: [
    {
      number: 1,
      character: 'WILLIAM_ADAMA',
      location: 'ADMIRALS_QUARTERS',
      skillCards: { count: 2, colors: { LEADERSHIP: 1, TACTICS: 1 }, cards: [{ type: 'EXECUTIVE_ORDER', value: 2 }, { type: 'LAUNCH_SCOUT', value: 1 }] },
      loyaltyCards: { count: 1, revealed: [], cards: ['NOT_CYLON'] },
      turns: 1,
      handLimit: 10,
    },
    {
      number: 2,
      character: 'LAURA_ROSLIN',
      location: 'PRESIDENTS_OFFICE',
      skillCards: { count: 3, colors: { POLITICS: 2, LEADERSHIP: 1 }, cards: null },
      loyaltyCards: { count: 1, revealed: [], cards: null },
      turns: 0,
      handLimit: 10,
    },
  ],
} as unknown as GameProjection;

function renderPanel() {
  return render(
    <Table.Provider value={tableContext(game, false, null)}>
      <PlayersPanel />
    </Table.Provider>,
  ).container;
}

describe('the players', () => {
  it('shows a hand the viewer may see face up, by the cards\' images', () => {
    const panel = renderPanel();
    const own = panel.querySelector('[data-player="1"]');

    expect([...(own?.querySelectorAll('.tile:not(.back) img') ?? [])].map((image) => image.getAttribute('alt'))).toEqual([
      'Executive Order 2',
      'Launch Scout 1',
      'Not Cylon',
    ]);
    expect(own?.querySelector('img.avatar')?.getAttribute('alt')).toBe('William Adama');
  });

  it('names a player by his character first, and the user in the seat under it', () => {
    const panel = render(
      <Table.Provider value={tableContext(game, false, null, { 1: 'atlas', 2: 'kestrel' })}>
        <PlayersPanel />
      </Table.Provider>,
    ).container;

    expect(panel.querySelector('[data-player="2"] .who strong')?.textContent).toBe('Laura Roslin');
    expect(panel.querySelector('[data-player="2"] .who .user')?.textContent).toBe('kestrel');
    expect(panel.querySelector('[data-player="2"] .badges')?.textContent).toContain('President');
  });

  it('lays out a sandbox like a live table, every hand face down, and shows a hand on hover', () => {
    const sandbox = {
      ...game,
      viewer: { kind: 'Sandbox' },
      players: game.players!.map((player) =>
        player.number === 2 ? { ...player, skillCards: { ...player.skillCards!, cards: [{ type: 'RED_TAPE', value: 0 }, { type: 'RED_TAPE', value: 0 }, { type: 'EXECUTIVE_ORDER', value: 1 }] } } : player,
      ),
    } as unknown as GameProjection;
    const panel = render(
      <Table.Provider value={tableContext(sandbox, true, null)}>
        <PlayersPanel />
        <RevealLayer />
      </Table.Provider>,
    ).container;

    expect(panel.querySelectorAll('.tile:not(.back)')).toHaveLength(0);
    fireEvent.mouseEnter(panel.querySelector('[data-reveal="hand-2"]')!);
    expect([...document.querySelectorAll('.reveal-pop ol.pile li')].map((card) => card.textContent)).toEqual(['Red Tape 0', 'Red Tape 0', 'Executive Order 1']);
    useReveal.getState().hide();
  });

  it('shows only the backs of another player\'s cards: a back per Skill Card color, and the backs of his piles', () => {
    const panel = renderPanel();
    const other = panel.querySelector('[data-player="2"]');

    expect(other?.querySelectorAll('.tile:not(.back)')).toHaveLength(0);
    expect([...(other?.querySelectorAll('.tile.back') ?? [])].map((back) => back.getAttribute('title'))).toEqual([
      '2 politics',
      '1 leadership',
      '1 face down',
      '2 face down',
    ]);
    // the President's Quorum hand lies with her, face down
    expect(other?.querySelector('.tile.back [data-art="DeckId/QUORUM"]')).not.toBeNull();
  });
});

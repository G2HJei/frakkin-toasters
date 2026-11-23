import { fireEvent, render } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { AnsweringContext } from '../../input/boardAnswers';
import type { Move } from '../../input/answers';
import { PlayersPanel } from './PlayersPanel';
import { RevealLayer, useReveal } from '../reveal';
import { usePreview } from '../../art/Art';
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
  it('lays the Quorum Cards in play in front of their holders, and the ones without a holder by the President', () => {
    const inPlay = {
      ...game,
      quorumCardsInPlay: [{ card: 'PROBATION', holderPlayerNumber: 1 }, { card: 'ACCEPT_PROPHECY', holderPlayerNumber: null }],
    } as unknown as GameProjection;
    const container = render(
      <Table.Provider value={tableContext(inPlay, false, null)}>
        <PlayersPanel />
      </Table.Provider>,
    ).container;
    const inPlayOf = (playerNumber: number) =>
      Array.from(container.querySelectorAll(`[data-player="${playerNumber}"] [aria-label="Quorum Cards in play"] img`)).map((img) => img.getAttribute('alt'));

    expect(inPlayOf(1)).toEqual([expect.stringMatching(/probation/i)]);
    expect(inPlayOf(2)).toEqual([expect.stringMatching(/accept prophecy/i)]);
  });

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

  it('lays the titles and tokens under the portrait, and previews one from anywhere on its badge', () => {
    const panel = renderPanel();
    const side = panel.querySelector('[data-player="1"] .mat-side');

    // the portrait first, a button that opens the character sheet in the reference
    expect(side?.firstElementChild?.matches('.portrait')).toBe(true);
    expect(side?.firstElementChild?.firstElementChild?.matches('button.avatar-button')).toBe(true);
    expect(side?.firstElementChild?.querySelector('img.avatar')).not.toBeNull();
    expect(side?.querySelector('.badges')?.textContent).toBe('AdmiralCurrent');
    const admiral = side!.querySelector('.badge.title')!;
    expect(admiral.querySelector('img')?.getAttribute('alt')).toBe('Admiral');
    fireEvent.mouseEnter(admiral);
    expect(usePreview.getState().art?.label).toBe('Admiral');
    fireEvent.mouseLeave(admiral);
    expect(usePreview.getState().art).toBeNull();
  });

  it('strips the right of a portrait with the colors the sheet draws, but a revealed Cylon\'s, a Cylon Leader\'s always', () => {
    const cylons = {
      ...game,
      players: [
        game.players![0],
        { ...game.players![1], revealedCylon: true },
        { ...game.players![1], number: 3, character: 'CAVIL', human: false },
        { ...game.players![1], number: 4, character: 'SHARON_ATHENA_AGATHON', infiltrating: true },
        { ...game.players![1], number: 5, character: 'LEOBEN_CONOY', human: false, revealedCylon: true },
      ],
    } as unknown as GameProjection;
    const panel = render(
      <Table.Provider value={tableContext(cylons, false, null)}>
        <PlayersPanel />
      </Table.Provider>,
    ).container;
    const strip = (playerNumber: number) => panel.querySelector(`[data-player="${playerNumber}"] .portrait .skill-strip`);
    const cells = (playerNumber: number) => [...(strip(playerNumber)?.querySelectorAll('.skill-cell') ?? [])].map((cell) => cell.getAttribute('data-colors'));

    // after the portrait, a cell per card in the order the sheet prints them
    expect(strip(1)?.previousElementSibling?.matches('button.avatar-button')).toBe(true);
    expect(strip(1)?.getAttribute('aria-label')).toBe('Skills: 3 Leadership, 2 Tactics');
    expect(cells(1)).toEqual(['LEADERSHIP', 'LEADERSHIP', 'LEADERSHIP', 'TACTICS', 'TACTICS']);
    expect(strip(2)).toBeNull();
    expect(cells(3)).toEqual(['TACTICS', 'TREACHERY ENGINEERING']);
    expect(strip(3)?.getAttribute('aria-label')).toBe('Skills: 1 Tactics, 1 Treachery or Engineering');
    expect(cells(4)).toEqual(['PILOTING', 'LEADERSHIP ENGINEERING']);
    expect(cells(5)).toEqual(['POLITICS', 'TREACHERY ENGINEERING']);
  });

  it('lays the nukes beside the Admiral, and nowhere once they are used', () => {
    const panel = render(
      <Table.Provider value={tableContext({ ...game, nukes: 2 } as GameProjection, false, null)}>
        <PlayersPanel />
      </Table.Provider>,
    ).container;

    const nukes = panel.querySelector('[data-player="1"] .badges .nukes');
    expect(nukes?.textContent).toBe('2 nukes');
    expect(nukes?.previousElementSibling?.textContent).toContain('Admiral');
    expect(nukes?.querySelector('img')).not.toBeNull();
    expect(panel.querySelector('[data-player="2"] .nukes')).toBeNull();

    const used = render(
      <Table.Provider value={tableContext({ ...game, nukes: 0 } as GameProjection, false, null)}>
        <PlayersPanel />
      </Table.Provider>,
    ).container;
    expect(used.querySelector('.nukes')).toBeNull();
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
    // a back has no preview, which would cover the pile's popover
    fireEvent.mouseEnter(other!.querySelector('[data-art="DeckId/QUORUM"]')!);
    expect(usePreview.getState().art).toBeNull();
  });

  it('lights the cards that start a move and the players a move targets, and picks them on a click', () => {
    const move = { option: null, followup: null, event: { kind: 'NoOpEvent' }, submits: true, pick: null } as unknown as Move;
    const onTarget = vi.fn();
    const offer = {
      targets: new Map([
        ['card:SkillCard:EXECUTIVE_ORDER:2', [move]],
        ['player:2', [move]],
      ]),
      acts: new Map(),
      picked: new Set<string>(),
    };
    const { container } = render(
      <Table.Provider value={tableContext(game, false, 1)}>
        <AnsweringContext.Provider value={{ offer, onTarget }}>
          <PlayersPanel />
        </AnsweringContext.Provider>
      </Table.Provider>,
    );

    const playable = container.querySelectorAll('[data-player="1"] button.playable');
    expect([...playable].map((button) => button.querySelector('img')?.getAttribute('alt'))).toEqual(['Executive Order 2']);
    fireEvent.click(playable[0]);
    expect(onTarget).toHaveBeenLastCalledWith({ type: 'card', deck: 'SkillCard', card: { type: 'EXECUTIVE_ORDER', value: 2 } }, expect.anything());

    const roslin = container.querySelector('[data-player="2"]')!;
    expect(roslin.classList).toContain('target');
    fireEvent.click(roslin);
    expect(onTarget).toHaveBeenLastCalledWith({ type: 'player', playerNumber: 2 }, expect.anything());
  });
});

import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { AnytimePanel } from './AnytimePanel';
import { useSelection } from './selection';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

const reveal = { kind: 'RevealMotiveCardEvent', playerNumber: 2, card: 'END_THE_CHASE' };

// Cavil holds two Motive Cards while William Adama is asked to choose; the engine lists what he may reveal.
const game = (viewer: unknown, anytime: unknown[]) =>
  ({
    viewer,
    players: [
      { number: 1, character: 'WILLIAM_ADAMA', motiveCards: null },
      { number: 2, character: 'CAVIL', motiveCards: { count: 2, revealed: [], cards: ['END_THE_CHASE', 'KEEP_THEM_DOCILE'] } },
    ],
    input: { kind: 'WaitingFor', player: 1 },
    anytime,
  }) as unknown as GameProjection;

const cavilMayReveal = [{ player: 2, answers: { events: [reveal], shapes: [] } }];

function renderPanel(projection: GameProjection, onSubmit = vi.fn()) {
  return render(
    <Table.Provider value={tableContext(projection, projection.viewer?.kind === 'Sandbox', null)}>
      <AnytimePanel onSubmit={onSubmit} />
    </Table.Provider>,
  );
}

describe('acting at any time', () => {
  it('lets a Cylon Leader reveal the Motive Card the engine allows while another player is asked', () => {
    const onSubmit = vi.fn();
    renderPanel(game({ kind: 'PlayerViewer', playerNumber: 2 }, cavilMayReveal), onSubmit);

    // the card whose requirement is met, not the one that waits for the end
    expect(screen.queryByRole('button', { name: /Keep Them Docile/ })).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: /End the Chase/ }));

    expect(onSubmit).toHaveBeenCalledWith(reveal);
  });

  it('opens a group among others in place, where the picks narrow nothing', () => {
    const both = [{ player: 2, answers: { events: [reveal, { ...reveal, card: 'KEEP_THEM_DOCILE' }, { kind: 'RevealCylonEvent', playerNumber: 2, card: null }], shapes: [] } }];
    renderPanel(game({ kind: 'PlayerViewer', playerNumber: 2 }, both));

    expect(screen.queryByRole('button', { name: /End the Chase/ })).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: 'Reveal a Motive Card' }));
    expect(useSelection.getState().picked).toEqual([]);
    expect(screen.getByRole('button', { name: /End the Chase/ })).toBeTruthy();
    fireEvent.click(screen.getByRole('button', { name: 'Back to the answers' }));
    expect(screen.getByRole('button', { name: 'Reveal a Motive Card' })).toBeTruthy();
  });

  it('shows nothing while the engine lists nothing, Motive Cards in hand or not', () => {
    renderPanel(game({ kind: 'PlayerViewer', playerNumber: 2 }, []));

    expect(screen.queryByRole('region', { name: 'at any time' })).toBeNull();
  });

  it('names the player a sandbox acts for', () => {
    renderPanel(game({ kind: 'Sandbox' }, cavilMayReveal));

    expect(screen.getByRole('group', { name: 'Cavil' })).toBeTruthy();
    expect(screen.getByRole('button', { name: /End the Chase/ })).toBeTruthy();
  });
});

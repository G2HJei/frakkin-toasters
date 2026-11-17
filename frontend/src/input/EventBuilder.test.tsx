import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { EventBuilder } from './EventBuilder';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

// A game with two players, Adama holding two Skill Cards, and a viper in space.
const game = {
  viewer: { kind: 'Sandbox' },
  players: [
    {
      number: 1,
      character: 'WILLIAM_ADAMA',
      location: 'RESEARCH_LAB',
      skillCards: { count: 2, colors: {}, cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }, { value: 1, type: 'LAUNCH_SCOUT' }] },
    },
    { number: 2, character: 'LAURA_ROSLIN', location: 'PRESIDENTS_OFFICE', skillCards: { count: 0, colors: {}, cards: [] } },
  ],
  galactica: { ships: [{ id: 4, type: 'VIPER', location: 'GALACTICA_SPACE_4_OCLOCK' }], reserves: [], damagedVipers: [], boardingParty: [] },
  cylonFleet: { ships: [] },
  decks: [],
} as unknown as GameProjection;

function renderBuilder(action: string, playerNumber: number | null) {
  const onSubmit = vi.fn();
  render(
    <Table.Provider value={tableContext(game, true, playerNumber)}>
      <EventBuilder action={action} playerNumber={playerNumber} onSubmit={onSubmit} />
    </Table.Provider>,
  );
  return onSubmit;
}

describe('the event builder', () => {
  it('counts Skill Cards per color', () => {
    const onSubmit = renderBuilder('DrawSkillCardsEvent', 1);

    fireEvent.change(screen.getByLabelText('LEADERSHIP'), { target: { value: '3' } });
    fireEvent.change(screen.getByLabelText('TACTICS'), { target: { value: '2' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'DrawSkillCardsEvent', playerNumber: 1, selection: { LEADERSHIP: 3, TACTICS: 2 }, drawLimit: null });
  });

  it('picks a constant and a card of the hand by its position', () => {
    const onSubmit = renderBuilder('MoveEvent', 1);

    fireEvent.change(screen.getByRole('combobox', { name: 'destination' }), { target: { value: 'COMMAND' } });
    fireEvent.change(screen.getByRole('combobox', { name: 'discard card' }), { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'MoveEvent', playerNumber: 1, destination: 'COMMAND', discardCard: { value: 1, type: 'LAUNCH_SCOUT' } });
  });

  it('asks an action first for the one of the player\'s location', () => {
    renderBuilder('ActionEvent', 1);

    expect((screen.getByRole('combobox', { name: 'event type' }) as HTMLSelectElement).value).toBe('ResearchLabEvent');
  });

  it('picks a player and a ship among those of the game', () => {
    const onSubmit = renderBuilder('GiveSkillCardToPlayerEvent', null);

    fireEvent.change(screen.getByRole('combobox', { name: 'target player number' }), { target: { value: '2' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'GiveSkillCardToPlayerEvent', targetPlayerNumber: 2, card: null });
    expect(screen.getByRole('option', { name: 'P2 Laura Roslin' })).toBeInTheDocument();
  });

  it('fills a nested record, a viper activation', () => {
    const onSubmit = renderBuilder('ActivateViperEvent', 1);

    fireEvent.click(screen.getByRole('button', { name: 'set' }));
    fireEvent.change(screen.getByRole('combobox', { name: 'human fighter id' }), { target: { value: '4' } });
    fireEvent.change(screen.getByRole('combobox', { name: 'move location' }), { target: { value: 'GALACTICA_SPACE_6_OCLOCK' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSubmit.mock.calls[0][0].activation).toEqual({
      launchShipType: null,
      launchLocation: null,
      humanFighterId: 4,
      moveLocation: 'GALACTICA_SPACE_6_OCLOCK',
      attackCylonShipId: null,
      escortCivilianShipId: null,
    });
  });
});

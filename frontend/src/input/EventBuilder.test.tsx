import { fireEvent, render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { EventBuilder } from './EventBuilder';
import { Table, tableContext } from '../table/tableContext';
import type { GameProjection } from '../wire/types';

// A game with two players, Adama holding two Skill Cards and a Mutiny Card, Roslin President with a Quorum Card, and a
// viper in space.
const game = {
  viewer: { kind: 'Sandbox' },
  players: [
    {
      number: 1,
      character: 'WILLIAM_ADAMA',
      location: 'RESEARCH_LAB',
      skillCards: { count: 2, colors: {}, cards: [{ value: 2, type: 'EXECUTIVE_ORDER' }, { value: 1, type: 'LAUNCH_SCOUT' }] },
      quorumCards: { count: 1, revealed: null, cards: ['EULOGY'] },
      mutinyCards: { count: 1, revealed: null, cards: ['FEED_THE_PEOPLE'] },
    },
    { number: 2, character: 'LAURA_ROSLIN', location: 'PRESIDENTS_OFFICE', skillCards: { count: 0, colors: {}, cards: [] } },
  ],
  titles: { president: 'LAURA_ROSLIN', admiral: 'WILLIAM_ADAMA', cag: null },
  presidentHand: { count: 1, revealed: null, cards: ['ENACT_PRODUCTION_QUOTAS'] },
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

function eventKinds(): string[] {
  return Array.from((screen.getByRole('combobox', { name: 'event type' }) as HTMLSelectElement).options).map((option) => option.value);
}

describe('the event builder', () => {
  // "Quick Thinking" and "Dogfight" take a card out of the check, which lies face up: the hand isn't offered.
  it('offers the cards of the skill check to a card ability of the check', () => {
    const withCheck = {
      ...game,
      skillCheck: { matchingPile: [{ value: 3, type: 'CONSOLIDATE_POWER' }], nonMatchingPile: [{ value: 1, type: 'DECLARE_EMERGENCY' }] },
    } as unknown as GameProjection;
    const onSubmit = vi.fn();
    render(
      <Table.Provider value={tableContext(withCheck, true, 1)}>
        <EventBuilder action="QuickThinkingEvent" playerNumber={1} onSubmit={onSubmit} />
      </Table.Provider>,
    );

    const picker = screen.getByRole('combobox', { name: 'card to remove' }) as HTMLSelectElement;
    expect(Array.from(picker.options).map((option) => option.value)).toEqual(['-1', '0', '1']);
    fireEvent.change(picker, { target: { value: '1' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(onSubmit).toHaveBeenCalledWith({ kind: 'QuickThinkingEvent', cardToRemove: { value: 1, type: 'DECLARE_EMERGENCY' } });
  });

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

  it('offers only the Quorum Cards the President holds', () => {
    renderBuilder('ActionEvent', 2);

    const kinds = eventKinds();
    expect(kinds).toContain('EnactProductionQuotasEvent');
    expect(kinds).not.toContain('EstablishDogsvilleEvent');
  });

  it("offers no Quorum Card of the President's hand to a player who is not President", () => {
    renderBuilder('ActionEvent', 1);

    expect(eventKinds()).not.toContain('EnactProductionQuotasEvent');
  });

  it('offers only the Mutiny Cards the player holds', () => {
    renderBuilder('ActionEvent', 1);

    const kinds = eventKinds();
    expect(kinds).toContain('FeedThePeopleEvent');
    expect(kinds).not.toContain('PanicEvent');
  });

  it('offers the Quorum Card a player holds himself', () => {
    renderBuilder('ActionEvent', 1);

    expect(eventKinds()).toContain('EulogyEvent');
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
    // a player is his character
    expect(screen.getByRole('option', { name: 'Laura Roslin' })).toBeInTheDocument();
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

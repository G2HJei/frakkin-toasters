import { fireEvent, render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useChatStore } from '../../live/chatStore';
import type { ChatLine, GameProjection } from '../../wire/types';
import { Table, tableContext } from '../tableContext';
import { ChatPanel } from './ChatPanel';

const game = { players: [{ number: 1, character: 'WILLIAM_ADAMA' }, { number: 2, character: 'LAURA_ROSLIN' }] } as unknown as GameProjection;

const line = (id: number, text: string, playerNumber: number | null = 1): ChatLine => ({
  id,
  userId: `u${id}`,
  name: playerNumber === 1 ? 'Bill' : 'Dee',
  playerNumber,
  text,
  at: '2026-11-25T12:00:00Z',
});

function renderPanel(props: Partial<Parameters<typeof ChatPanel>[0]> = {}) {
  const send = vi.fn(() => true);
  render(
    <Table.Provider value={tableContext(game, false, null)}>
      <ChatPanel tabs={null} writes seenBefore={null} send={send} {...props} />
    </Table.Provider>,
  );
  return send;
}

beforeEach(() => useChatStore.getState().reset());

describe('the chat store', () => {
  it('keeps a message that came both in the log and on its own once, in order', () => {
    const store = useChatStore.getState();
    store.receive({ type: 'chat', message: line(3, 'third') });
    store.receive({ type: 'chatLog', messages: [line(1, 'first'), line(3, 'third')] });
    store.receive({ type: 'chat', message: line(2, 'second') });

    expect(useChatStore.getState().messages.map((message) => message.id)).toEqual([1, 2, 3]);
  });

  it('keeps the reason of a refused message', () => {
    useChatStore.getState().receive({ type: 'chatRejected', message: 'Slow down' });

    expect(useChatStore.getState().rejected).toBe('Slow down');
  });
});

describe('ChatPanel', () => {
  it("shows everyone's messages, a player's with his character and an observer's as such, and sends on Enter", () => {
    useChatStore.setState({ messages: [line(1, 'So say we all'), line(2, 'He is bluffing', null)] });
    const send = renderPanel();

    expect(screen.getByText('So say we all')).toBeVisible();
    expect(screen.getByText('William Adama')).toBeVisible();
    expect(screen.getByText('He is bluffing')).toBeVisible();
    expect(screen.getByText('observer')).toBeVisible();
    const field = screen.getByLabelText('message');
    fireEvent.change(field, { target: { value: '  Action stations  ' } });
    fireEvent.keyDown(field, { key: 'Enter' });

    expect(send).toHaveBeenCalledWith('Action stations');
    expect(field).toHaveValue('');
  });

  it('lets whoever only watches a finished game read', () => {
    useChatStore.setState({ messages: [line(1, 'at the table')] });
    renderPanel({ writes: false });

    expect(screen.getByText('at the table')).toBeVisible();
    expect(screen.queryByLabelText('message')).toBeNull();
    expect(screen.getByText('Only the players and observers of the game write here.')).toBeVisible();
  });

  it('draws the red line under the last message seen before the chat opened', () => {
    useChatStore.setState({ messages: [line(1, 'old'), line(2, 'new one'), line(3, 'newer')] });
    renderPanel({ seenBefore: 1 });

    const marker = screen.getByLabelText('new since you last looked');
    expect(marker.nextElementSibling).toHaveTextContent('new one');
  });
});

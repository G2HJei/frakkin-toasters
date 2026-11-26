import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import type { LobbyLine } from '../api/queries';
import { LobbyChat } from './LobbyChat';

const line = (id: number, userId: string, name: string, text: string): LobbyLine => ({ id, userId, name, text, at: '2026-11-26T12:00:00Z' });

// the server: who is logged in, the chat's lines, and what a post answers (a line, or a refusal)
function stubServer(lines: LobbyLine[], post: (text: string) => Response) {
  const fetch = vi.fn(async (path: string, init?: RequestInit) => {
    if (path === '/api/me') {
      return new Response(JSON.stringify({ id: 'u1', name: 'Starbuck', settings: {} }), { status: 200 });
    }
    if (path === '/api/lobby/chat' && init?.method === 'POST') {
      return post((JSON.parse(String(init.body)) as { text: string }).text);
    }
    if (path === '/api/lobby/chat') {
      return new Response(JSON.stringify(lines), { status: 200 });
    }
    return new Response('{}', { status: 200 });
  });
  vi.stubGlobal('fetch', fetch);
  return fetch;
}

function renderChat() {
  return render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <LobbyChat />
    </QueryClientProvider>,
  );
}

const shown = () => [...document.querySelectorAll('.lobby-chat-lines li')].map((li) => [li.querySelector('.lobby-chat-name')?.textContent, li.querySelector('.lobby-chat-text')?.textContent, li.classList.contains('mine')]);
const chatAsks = (fetch: ReturnType<typeof stubServer>) => fetch.mock.calls.filter(([path, init]) => path === '/api/lobby/chat' && !init?.method?.startsWith('P')).length;

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('the play page chat', () => {
  it("shows the last messages, the viewer's own marked, and sends one on Enter", async () => {
    const lines = [line(1, 'u2', 'Apollo', 'Anyone for a game?'), line(2, 'u1', 'Starbuck', 'Me')];
    stubServer(lines, (text) => {
      const posted = line(3, 'u1', 'Starbuck', text);
      lines.push(posted);
      return new Response(JSON.stringify(posted), { status: 200 });
    });
    renderChat();

    await waitFor(() =>
      expect(shown()).toEqual([
        ['Apollo', 'Anyone for a game?', false],
        ['Starbuck', 'Me', true],
      ]),
    );
    const box = screen.getByRole('textbox', { name: 'message' });
    fireEvent.change(box, { target: { value: '  Two seats left  ' } });
    fireEvent.keyDown(box, { key: 'Enter' });

    await waitFor(() => expect(shown().at(-1)).toEqual(['Starbuck', 'Two seats left', true]));
    expect(box).toHaveValue('');
  });

  it('shows why the server refused a message and keeps it in the box', async () => {
    stubServer([], () => new Response(JSON.stringify({ message: 'Slow down: at most 5 messages in 10 seconds' }), { status: 429 }));
    renderChat();

    expect(await screen.findByText('Nothing said yet.')).toBeInTheDocument();
    const box = screen.getByRole('textbox', { name: 'message' });
    fireEvent.change(box, { target: { value: 'Frak' } });
    fireEvent.keyDown(box, { key: 'Enter' });
    expect(screen.queryByRole('button', { name: 'Send' })).toBeNull();

    expect(await screen.findByRole('alert')).toHaveTextContent('Slow down: at most 5 messages in 10 seconds');
    expect(box).toHaveValue('Frak');
  });

  it('asks for the messages again every 10 seconds', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const fetch = stubServer([], () => new Response('{}', { status: 200 }));
    renderChat();
    await waitFor(() => expect(chatAsks(fetch)).toBe(1));

    await act(() => vi.advanceTimersByTimeAsync(9_000));
    expect(chatAsks(fetch)).toBe(1);
    await act(() => vi.advanceTimersByTimeAsync(1_500));
    expect(chatAsks(fetch)).toBe(2);
  });
});

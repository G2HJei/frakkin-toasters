import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ProblemDrawer, ProblemLink, useProblem } from './ProblemDrawer';

afterEach(() => {
  vi.unstubAllGlobals();
  useProblem.getState().hide();
});

describe('ProblemDrawer', () => {
  it('opens from its link, sends the words with the game, and thanks', async () => {
    const fetchMock = vi.fn(async () => new Response(JSON.stringify({ id: 1 }), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);
    render(
      <QueryClientProvider client={new QueryClient()}>
        <ProblemLink />
        <ProblemDrawer gameId="g1" />
      </QueryClientProvider>,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Report a problem' }));
    const field = screen.getByLabelText('problem');
    expect(field).toHaveAttribute('maxLength', '1000');
    fireEvent.change(field, { target: { value: 'The die rolled twice' } });
    fireEvent.click(screen.getByRole('button', { name: 'Send' }));

    expect(await screen.findByRole('status')).toHaveTextContent('Thank you');
    const [url, init] = fetchMock.mock.calls.at(-1) as unknown as [string, RequestInit];
    expect(url).toBe('/api/games/g1/problems');
    expect(JSON.parse(String(init.body))).toEqual({ text: 'The die rolled twice' });
  });
});
